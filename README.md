# Simple Text Adventure Game (STAG) in Java

## Project Overview
A Java-based TCP socket server for text adventure games inspired by Zork. Multiple players can connect, explore locations, interact with entities, and perform both built-in and custom actions defined via configuration files (DOT for entities, XML for actions).

---

## Features
- Built-in commands: `look`, `goto`, `get`, `drop`, `inventory`, `health`
- Custom actions via XML & DOT configs
- Flexible command parsing (case-insensitive, decorative words, word-order variation)
- Multiplayer support (players share the same world and can see each other)
- Player health system (max 3; poison/potion effects; respawn on death)
---

## Technical Details 
- Language / Build： Java 17, Maven
- Networking： TCP Socket Server
- Parsing： DOT (GraphViz) for entities、JAXP (DOM) for actions
- Constraints： No Lambdas, Arrays, ArrayLists, Ternary operators, unqualified method calls, or string concatenation.
---

## Project Structure
```bash
src/
├── main/java/edu/uob/        # Core game server implementation
├── test/java/edu/uob/        # Unit tests
config/
├── basic-entities.dot        # Basic game entities
├── extended-entities.dot
├── basic-actions.xml         # Basic game actions
└── extended-actions.xml
```

---

## Usage
### 1. Start the server 
   ```bash
    # macOS / Linux
    ./mvnw exec:java@server
    # Windows
    mvnw.cmd exec:java@server
```
    
### 2. Start the client 
   ```bash
    # macOS / Linux
    ./mvnw exec:java@client -Dexec.args="playerName"
    # Windows
    mvnw.cmd exec:java@client -Dexec.args="playerName"
```
   Player name may include letters, spaces, apostrophes, and hyphens.

### 3. Run test
```bash
    ./mvnw test
    # Windows
    mvnw.cmd test
```

---

## Example Commands
- `look`— Show the details of the current location
- `goto <location>` — Move to a location reachable by a path
- `drop` <item>— Drop an item to the current location drop <item>
- `unlock <object> with <item>`— Unlock a locked object using a key/tool unlock <object> with <item>
- `inventory`— Show items carried by the player
- `health`— Display current health (max 3)

---
## Customize Your Game
1.Edit entities (DOT): Define `locations`, `paths`, `artefacts`, `characters`, and `furniture` in `config/*.dot`.

2.Edit actions (XML): `Define triggers`, `subjects`, `consumed`, `produced`, and `narration` in `config/*.xml`.

3.Start the server: The server will load the specified DOT / XML files. (If your program supports parameterized loading, document it here.)

4.Tip: Entities without an initial location should be placed in the `storeroom`. They will appear in the game only when triggered by an action.

---
## Development Notes
- Use `StringBuilder` or `printf` instead of string concatenation.
- To check for “illegal constructs,” run the provided strange checker locally (depending on course configuration).
- Always run tests with mvn test and ensure the project compiles and runs correctly in a clean environment.

---
## Academic Integrity & Disclaimer

This project was developed as part of a university coursework assignment.
It is shared publicly for learning and portfolio purposes only.
Do not reuse this code for academic submissions.
