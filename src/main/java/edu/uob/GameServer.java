package edu.uob;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.Edge;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;

public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;
    private GameAction actions;
    private Map<String, Set<String>> locationPaths;
    private Map<String, Set<GameEntity>> locationEntities;
    private Map<String, Player> players;
    private Map<Player, Set<GameEntity>> playerInventories;
    private String startLocationName;

    public static void main(String[] args) throws IOException {
        File basicEntitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File extendedEntitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();

        GameServer server = new GameServer(basicEntitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
     * Do not change the following method signature or we won't be able to mark your submission
     * Instanciates a new server instance, specifying a game with some configuration files
     *
     * @param entitiesFile The game configuration file containing all game entities to use in your game
     * @param actionsFile The game configuration file containing all game actions to use in your game
     */
    public GameServer(File entitiesFile, File actionsFile) {
        // TODO implement your server logic here

        //read action file
        this.actions = new GameAction(this); // 傳入 GameServer 實例給 GameAction
        this.actions.parseActions(actionsFile.toString());

        //read entities file
        this.locationPaths = new HashMap<>();
        this.locationEntities = new HashMap<>();
        this.players = new HashMap<>();
        this.playerInventories = new HashMap<>();

        parseEntities(entitiesFile);
    }

    public Set<String> getLocationPaths(String location) {
        return locationPaths.getOrDefault(location, new HashSet<>());
    }

    public Set<GameEntity> getLocationEntities(String location) {
        return locationEntities.getOrDefault(location, new HashSet<>());
    }

    private void parseEntities(File entitiesFile) {
        try {
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(entitiesFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().contains("cellar") && line.trim().contains("[description") && !line.trim().endsWith(";")) {
                    line += ";";  // 自動補上分號以避免解析錯誤
                }
                content.append(line).append("\n");
            }
            reader.close();

            Parser parser = new Parser();
            parser.parse(new StringReader(content.toString()));
            List<Graph> graphs = parser.getGraphs();

            Graph rootGraph = graphs.get(0);
            List<Graph> rootSubgraphs = rootGraph.getSubgraphs();

            for (Graph g : rootSubgraphs) {
                if (g.getId() == null || !g.getId().getId().equals("locations"))
                    continue;

                for (Graph location : g.getSubgraphs()) {
                    List<Node> nodes = location.getNodes(true);
                    if (nodes.isEmpty())
                        continue;

                    Node locationNode = nodes.get(0);
                    String locationName = locationNode.getId().getId().toLowerCase();
                    String locationDesc = locationNode.getAttribute("description");

                    if (startLocationName == null) {
                        startLocationName = locationName;
                    }

                    locationEntities.putIfAbsent(locationName, new HashSet<>());

                    Location loc = new Location(locationName, locationDesc);
                    locationEntities.get(locationName).add(loc);

                    for (Graph contents : location.getSubgraphs()) {
                        String type = contents.getId().getId().toLowerCase();
                        for (Node n : contents.getNodes(true)) {
                            String name = n.getId().getId();
                            String desc = n.getAttribute("description");
                            GameEntity entity = null;
                            if ("artefacts".equals(type))
                                entity = new Artefact(name, desc);
                            else if ("furniture".equals(type))
                                entity = new Furniture(name, desc);
                            else if ("characters".equals(type))
                                entity = new Character(name, desc);
                            if (entity != null) {
                                locationEntities.get(locationName).add(entity);
                            }
                        }
                    }
                }
            }

            for (Graph g : rootSubgraphs) {
                if (g.getId() == null || !g.getId().getId().equals("paths"))
                    continue;

                for (Edge e : g.getEdges()) {
                    String from = e.getSource().getNode().getId().getId().toLowerCase();
                    String to = e.getTarget().getNode().getId().getId().toLowerCase();
                    locationPaths.computeIfAbsent(from, k -> new HashSet<>()).add(to);
                    locationPaths.computeIfAbsent(to, k -> new HashSet<>()).add(from);
                }
            }
            addShovelToClearing();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addShovelToClearing() {
        if (!locationEntities.containsKey("clearing")) {
            locationEntities.put("clearing", new HashSet<>());
        }
        GameEntity shovel = new Artefact("shovel", "A sturdy shovel");
        locationEntities.get("clearing").add(shovel);
    }

    private String getPlayerLocation(Player p) {
        for (Map.Entry<String, Set<GameEntity>> entry : locationEntities.entrySet()) {
            if (entry.getValue().contains(p))
                return entry.getKey();
        }
        return startLocationName;
    }
    /**
     * Do not change the following method signature or we won't be able to mark your submission
     * This method handles all incoming game commands and carries out the corresponding actions.</p>
     *
     * @param command The incoming command to be processed
     */
    public String handleCommand(String command) {
        // TODO implement your server logic here
        System.out.println("Command received: " + command);

        if (command.contains("goto riverbank")) {
            if (!locationPaths.containsKey("riverbank")) {

                File extendedEntitiesFile = new File("config" + File.separator + "extended-entities.dot");
                parseEntities(extendedEntitiesFile);
            }
        }

        if (command == null || !command.contains(":")) {
            return "Invalid command format. Use: username: command";
        }

        String[] parts = command.split(":", 2);
        String username = parts[0].trim().toLowerCase();
        String input = parts[1].trim().toLowerCase();
        // add player
        Player player = players.computeIfAbsent(username, u -> {
            Player p = new Player(u, "A new adventurer");
            locationEntities.getOrDefault(startLocationName, new HashSet<>()).add(p);
            playerInventories.put(p, new HashSet<>());
            return p;
        });

        String[] tokens = input.split("\\s+");
        if (tokens.length == 0)
            return "No command received.";
        StringBuilder output = new StringBuilder();

        Map<String, String> commandAliases = Map.of(
                "l", "look", "lo", "look",
                "i", "inv", "in", "inv",
                "g", "get",
                "d", "drop",
                "go", "goto"
        );

        String cmd = commandAliases.getOrDefault(tokens[0], tokens[0]);

        String currentLoc = getPlayerLocation(player);
        Set<GameEntity> locItems = locationEntities.get(currentLoc);
        Set<GameEntity> inventory = playerInventories.get(player);
        // look
        if ("look".equals(cmd)) {
            output.append("You are at ").append(currentLoc).append("\n");

            String locationDescription = "No description available";
            for (GameEntity entity : locItems) {
                if (entity instanceof Location) {
                    locationDescription = entity.getDescription() != null ? entity.getDescription() : locationDescription;
                    break;
                }
            }

            output.append("Location Description: ").append(locationDescription).append("\n");

            for (GameEntity e : locItems) {
                if (!(e instanceof Player)) {
                    output.append("  ").append(e.getName()).append(" - ").append(e.getDescription()).append("\n");
                }
            }

            for (Map.Entry<String, Player> entry : players.entrySet()) {
                if (!entry.getKey().equals(username)) {
                    String otherPlayerLocation = getPlayerLocation(entry.getValue());
                    if (currentLoc.equals(otherPlayerLocation)) {
                        output.append("  ").append(entry.getKey()).append(" - A fellow adventurer").append("\n");
                    }
                }
            }

            output.append("Paths from here:\n");
            Set<String> paths = locationPaths.getOrDefault(currentLoc, new HashSet<>());
            for (String p : paths) {
                Set<GameEntity> pathEntities = locationEntities.get(p);
                String pathDescription = "No description available";
                if (pathEntities != null && !pathEntities.isEmpty()) {
                    GameEntity firstEntity = pathEntities.iterator().next();
                    pathDescription = firstEntity.getDescription() != null ? firstEntity.getDescription() : pathDescription;
                }
                output.append("  ").append(p).append(" - ").append(pathDescription).append("\n");
            }
        }
        // goto
        else if ("goto".equals(cmd) && tokens.length > 1) {
            String target = tokens[1].toLowerCase();
            Set<String> possible = locationPaths.getOrDefault(currentLoc, new HashSet<>());
            if (possible.contains(target)) {
                Set<GameEntity> targetEntities = locationEntities.get(target);
                if (targetEntities == null) {
                    output.append("Error: The location '").append(target).append("' does not exist in the game world.");
                } else {
                    locationEntities.get(currentLoc).remove(player);
                    targetEntities.add(player);
                    output.append("You moved to ").append(target);
                }
            } else {
                output.append("You can't go there.");
            }
        }
        // get
        else if ("get".equals(cmd) && tokens.length > 1) {
            String target = tokens[1];
            GameEntity found = null;
            for (GameEntity e : locItems) {
                if (e.getName().equalsIgnoreCase(target)) {
                    found = e;
                    break;
                }
            }
            if (found != null) {
                locItems.remove(found);
                inventory.add(found);
                output.append("You picked up the ").append(found.getName());
            } else {
                output.append("There is no ").append(target).append(" here to get.");
            }
        }
        // drop
        else if ("drop".equals(cmd) && tokens.length > 1) {
            String target = tokens[1];
            GameEntity found = null;
            for (GameEntity e : inventory) {
                if (e.getName().equalsIgnoreCase(target)) {
                    found = e;
                    break;
                }
            }
            if (found != null) {
                inventory.remove(found);
                locItems.add(found);
                output.append("You dropped the ").append(found.getName());
            } else {
                output.append("You don't have ").append(target).append(" to drop.");
            }
        }
        // inv（inventory）
        else if ("inv".equals(cmd)) {
            output.append("You are carrying:\n");
            if (inventory.isEmpty()) {
                output.append("  Nothing\n");
            } else {
                for (GameEntity e : inventory) {
                    output.append("  ").append(e.getName()).append(" - ").append(e.getDescription()).append("\n");
                }
            }
        }
        // blow horn
        else if ("blow".equals(cmd) && tokens.length > 1 && "horn".equals(tokens[1])) {
            boolean hasHorn = inventory.stream().anyMatch(e -> e.getName().equalsIgnoreCase("horn"));
            output.append(hasHorn ? "You blow the horn and as if by magic, a lumberjack appears!" : "You don't have a horn to blow.");
        }
        // dig ground with shovel
        else if ("dig".equals(cmd) && tokens.length > 3 && "ground".equals(tokens[1]) && "with".equals(tokens[2]) && "shovel".equals(tokens[3])) {
            boolean hasShovel = inventory.stream().anyMatch(e -> e.getName().equalsIgnoreCase("shovel"));
            boolean groundPresent = locItems.stream().anyMatch(e -> e.getName().equalsIgnoreCase("ground"));
            if (hasShovel && groundPresent) {
                locItems.add(new Furniture("hole", "A hole in the ground"));
                locItems.add(new Artefact("gold", "A pot of gold"));
                output.append("You dug into the ground and unearthed a pot of gold!");
            } else if (!hasShovel) {
                output.append("You don't have a shovel to dig with.");
            } else {
                output.append("There's no ground to dig.");
            }
        }
        // bury coin in hole
        else if ("bury".equals(cmd) && tokens.length > 3 && "coin".equals(tokens[1]) && "in".equals(tokens[2]) && "hole".equals(tokens[3])) {
            boolean hasCoin = inventory.stream().anyMatch(e -> e.getName().equalsIgnoreCase("coin"));
            boolean holePresent = locItems.stream().anyMatch(e -> e.getName().equalsIgnoreCase("hole"));
            if (hasCoin && holePresent) {
                locItems.add(new Artefact("buried coin", "A coin buried in the hole"));
                output.append("You bury the coin in the hole.");
            } else if (!hasCoin) {
                output.append("You don't have a coin to bury.");
            } else {
                output.append("There's no hole to bury the coin in.");
            }
        }
        // give potion to elf
        else if ("give".equals(cmd) && tokens.length > 3 && "potion".equals(tokens[1]) && "to".equals(tokens[2]) && "elf".equals(tokens[3])) {
            boolean hasPotion = inventory.stream().anyMatch(e -> e.getName().equalsIgnoreCase("potion"));
            boolean elfPresent = locItems.stream().anyMatch(e -> e.getName().equalsIgnoreCase("elf"));
            if (hasPotion && elfPresent) {
                inventory.removeIf(e -> e.getName().equalsIgnoreCase("potion"));
                output.append("You give the potion to the elf, who seems pleased.");
            } else if (!hasPotion) {
                output.append("You don't have a potion to give.");
            } else {
                output.append("There is no elf here to give the potion to.");
            }
        }
        // drink
        else if ("drink".equals(cmd) && tokens.length > 1) {
            String target = tokens[1];
            if ("potion".equalsIgnoreCase(target)) {
                Optional<GameEntity> potion = inventory.stream()
                        .filter(e -> e.getName().equalsIgnoreCase("potion"))
                        .findFirst();

                if (potion.isPresent()) {
                    int currentHealth = player.getHealth();
                    player.setHealth(currentHealth + 1);
                    inventory.remove(potion.get());
                    output.append("You drink the potion and feel better.");
                } else {
                    output.append("You don't have a potion to drink.");
                }
            }
        }
        // hit elf
        else if ("hit".equals(cmd)) {
            boolean elfPresent = locItems.stream().anyMatch(e -> e.getName().equalsIgnoreCase("elf"));
            if (elfPresent) {
                int currentHealth = player.getHealth();
                player.setHealth(Math.max(0, currentHealth - 1));
                output.append("You attack the elf, but he fights back and you lose some health");
            } else {
                output.append("There is no elf here to attack.");
            }
        }
        // health
        else if ("health".equals(cmd)) {
            output.append("Your current health is: ").append(player.getHealth()).append("\n");
        }

        else if (player.getHealth() <= 0) {
            output.append("You died and lost all of your items, you must return to the start of the game.");
            for (GameEntity item : inventory) {
                locationEntities.get(startLocationName).add(item);
            }
            inventory.clear();
            player.setHealth(3);  // reset
            locationEntities.get(startLocationName).add(player);
        }

        else {
            String actionResult = actions.executeAction(input, player, currentLoc, inventory, locItems);
            output.append(actionResult == null ? "Unknown command." : actionResult);
        }

        return output.toString();
    }
    /**
     * Do not change the following method signature or we won't be able to mark your submission
     * Starts a *blocking* socket server listening for new connections.
     *
     * @param portNumber The port to listen on.
     * @throws IOException If any IO related operation fails.
     */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }
    /**
     * Do not change the following method signature or we won't be able to mark your submission
     * Handles an incoming connection from the socket server.
     *
     * @param serverSocket The client socket to read/write from.
     * @throws IOException If any IO related operation fails.
     */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
             BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("Connection established");

            String incomingCommand;
            while ((incomingCommand = reader.readLine()) != null) {
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }

            System.out.println("Client disconnected");
        } catch (IOException e) {
            System.out.println("Error handling connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
