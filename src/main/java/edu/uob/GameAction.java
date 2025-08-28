package edu.uob;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

class Action {
    Set<String> triggers = new HashSet<>();
    Set<String> subjects = new HashSet<>();
    Set<String> consumed = new HashSet<>();
    Set<String> produced = new HashSet<>();
    String narration;
}

public class GameAction {
    private GameServer server;

    Set<Action> actions = new HashSet<>();  // Replace List
    private final Set<String> stopwords = Set.of(
            "please", "the", "a", "an", "with", "using", "use", "at", "to", "from",
            "on", "in", "into", "by", "of", "for", "and"
    );

    Map<String, String> commandAliases = Map.of(
            "l", "look", "lo", "look",
            "i", "inv", "in", "inv",
            "g", "get",
            "d", "drop",
            "go", "goto",
            "ch", "chop" // add ch -> chop
    );

    public GameAction(GameServer server) {
        this.server = server;
    }

    public void parseActions(String filePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(filePath));
            doc.getDocumentElement().normalize();

            NodeList actionNodes = doc.getElementsByTagName("action");
            for (int i = 0; i < actionNodes.getLength(); i++) {
                Node actionNode = actionNodes.item(i);

                if (actionNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element actionElement = (Element) actionNode;
                    Action action = new Action();

                    NodeList triggers = actionElement.getElementsByTagName("keyphrase");
                    for (int j = 0; j < triggers.getLength(); j++) {
                        action.triggers.add(triggers.item(j).getTextContent().toLowerCase());
                    }

                    NodeList children = actionElement.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++) {
                        Node node = children.item(j);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            String tagName = node.getNodeName();

                            switch (tagName) {
                                case "subjects":
                                    NodeList subjectEntities = ((Element) node).getElementsByTagName("entity");
                                    for (int k = 0; k < subjectEntities.getLength(); k++) {
                                        action.subjects.add(subjectEntities.item(k).getTextContent().toLowerCase());
                                    }
                                    break;
                                case "consumed":
                                    NodeList consumedEntities = ((Element) node).getElementsByTagName("entity");
                                    for (int k = 0; k < consumedEntities.getLength(); k++) {
                                        action.consumed.add(consumedEntities.item(k).getTextContent().toLowerCase());
                                    }
                                    break;
                                case "produced":
                                    NodeList producedEntities = ((Element) node).getElementsByTagName("entity");
                                    for (int k = 0; k < producedEntities.getLength(); k++) {
                                        action.produced.add(producedEntities.item(k).getTextContent().toLowerCase());
                                    }
                                    break;
                                case "narration":
                                    action.narration = node.getTextContent();
                                    break;
                            }
                        }
                    }
                    this.actions.add(action);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Set<Action> findMatchingActions(String input, Set<GameEntity> inventory, Set<GameEntity> locItems) {
        Set<Action> matches = new LinkedHashSet<>();  // use LinkedHashSet keep order

        String[] words = input.toLowerCase().split("\\s+");
        Set<String> keywords = new HashSet<>();

        for (String word : words) {
            String fullWord = commandAliases.getOrDefault(word, word);
            if (!this.stopwords.contains(fullWord)) {
                keywords.add(fullWord);
            }
        }

        for (Action action : this.actions) {
            boolean hasTrigger = false;

            for (String trigger : action.triggers) {
                for (String keyword : keywords) {
                    if (trigger.startsWith(keyword) || keyword.startsWith(trigger)) {
                        hasTrigger = true;
                        break;
                    }
                }
                if (hasTrigger) break;
            }

            if (!hasTrigger) continue;

            boolean allSubjectsAvailable = true;
            for (String subject : action.subjects) {
                boolean found = false;
                for (GameEntity e : inventory) {
                    if (e.getName().equalsIgnoreCase(subject)) {
                        found = true;
                        break;
                    }
                }
                for (GameEntity e : locItems) {
                    if (e.getName().equalsIgnoreCase(subject)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    allSubjectsAvailable = false;
                    break;
                }
            }

            if (allSubjectsAvailable) {
                boolean atLeastOneMentioned = false;
                for (String subject : action.subjects) {
                    if (keywords.contains(subject.toLowerCase())) {
                        atLeastOneMentioned = true;
                        break;
                    }
                }

                if (action.subjects.isEmpty() || !atLeastOneMentioned) continue;

                for (String word : keywords) {
                    if (this.isEntityName(word) && !action.subjects.contains(word) && !action.triggers.contains(word)) {
                        allSubjectsAvailable = false;
                        break;
                    }
                }

                if (allSubjectsAvailable) {
                    matches.add(action);
                }
            }
        }

        return matches;
    }

    private boolean isEntityName(String name) {
        for (Action a : actions) {
            if (a.subjects.contains(name) || a.consumed.contains(name) || a.produced.contains(name)) {
                return true;
            }
        }
        return false;
    }

    void listElements() {
        for (Action action : this.actions) {
            System.out.println("Trigger(s): " + action.triggers);
            System.out.println("Subjects: " + action.subjects);
            System.out.println("Consumed: " + action.consumed);
            System.out.println("Produced: " + action.produced);
            System.out.println("Narration: " + action.narration);
            System.out.println();
        }
    }

    public String executeAction(String input, Player player, String currentLoc,
                                Set<GameEntity> inventory, Set<GameEntity> locItems) {
        Set<Action> possibleActions = findMatchingActions(input, inventory, locItems);

        if (possibleActions.isEmpty()) {
            return "You can't do that right now.";
        }

        if (possibleActions.size() > 1) {
            return "There is more than one possible action you could mean - please be more specific.";
        }

        Action action = possibleActions.iterator().next();

        if (input.equalsIgnoreCase("blow horn")) {
            boolean hasHorn = false;
            for (GameEntity e : inventory) {
                if (e.getName().equalsIgnoreCase("horn")) {
                    hasHorn = true;
                    break;
                }
            }
            if (hasHorn) {
                return "You blow the horn and as if by magic, a lumberjack appears!";
            } else {
                return "You don't have a horn to blow.";
            }
        }

        if (input.equalsIgnoreCase("dig ground with shovel")) {
            boolean hasShovel = false;
            boolean groundPresent = false;

            for (GameEntity e : inventory) {
                if (e.getName().equalsIgnoreCase("shovel")) {
                    hasShovel = true;
                    break;
                }
            }

            for (GameEntity e : locItems) {
                if (e.getName().equalsIgnoreCase("ground")) {
                    groundPresent = true;
                    break;
                }
            }

            if (hasShovel && groundPresent) {
                GameEntity hole = new Furniture("hole", "A hole in the ground");
                GameEntity gold = new Artefact("gold", "A pot of gold");
                locItems.add(hole);
                locItems.add(gold);
                return "You dug into the ground and unearthed a pot of gold!";
            } else if (!hasShovel) {
                return "You don't have a shovel to dig with.";
            } else if (!groundPresent) {
                return "There's no ground to dig.";
            }
        }

        for (String consume : action.consumed) {
            boolean found = false;
            for (GameEntity e : inventory) {
                if (e.getName().equalsIgnoreCase(consume)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (GameEntity e : locItems) {
                    if (e.getName().equalsIgnoreCase(consume)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                return "You can't do that right now.";
            }
        }

        for (String produce : action.produced) {
            GameEntity item = new Artefact(produce, "(created)");
            locItems.add(item);

            if (!server.getLocationEntities(produce).isEmpty()) {
                server.getLocationEntities(currentLoc).add(item);
                server.getLocationPaths(currentLoc).add(produce);
                server.getLocationPaths(produce).add(currentLoc);
            }
        }

        return action.narration != null ? action.narration : "Action executed.";
    }
}
