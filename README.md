# Simple Text Adventure Game (STAG) in Java

## Project Overview
This project is a Java-based socket server engine designed for running text adventure games. Inspired by classic games such as Zork, the engine allows multiple players to connect, explore locations, interact with entities, and perform actions defined in configuration files.

---

## Features
- Built-in commands: look, goto, get, drop, inventory 
- Custom game actions defined via XML & DOT config files
- Flexible natural language parsing (case-insensitive, word order variation, decorative words)
- Multiplayer support – players share the same world and can see each other
- Player health system (poisons, potions, respawn mechanism)
---

## Technical Details 
- Language / Framework: Java 17, Maven
- Networking: TCP Socket Server
- Parsing:
  - DOT (GraphViz) for game entities (locations, artefacts, characters, furniture, etc.)
  - XML (JAXP) for game actions
- Constraints: No use of Lambdas, Arrays, ArrayLists, Ternary operators, unqualified method calls, or String concatenation.

---

## Project Structure
```
src/
├── main/java/edu/uob/   # Core game server implementation
├── test/java/edu/uob/   # Unit tests
config/
├── basic-entities.dot   # Basic game entities
├── extended-entities.dot
├── basic-actions.xml    # Basic game actions
└── extended-actions.xml
```

---

## Usage
1. Start the server 
   ```bash
   mvnw exec:java@server

2. Start the client 
   ```bash
   mvnw exec:java@client -Dexec.args="playername"

3. Run test
    ```bash
   mvnw test

---

## Example Commands
- look: Show the details of the current location
- goto <location>: Move to another place 
- get <item>: Drop an item
- unlock <object> with <item>: Unlock a locked object using a key or tool
- inventory: Show item carried by the player
- health: Display health status (max 3)
