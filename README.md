# Simple Text Adventure Game (STAG)

## Project Overview
This is a Java-based text adventure game engine, inspired by classic text RPGs such as Zork.  
Players interact with the game world using natural language commands to explore, collect items, interact with characters, and solve tasks.

---

## Features 
- Multi-player support 
- Configurable game world with DOT/XML 
- Health system and revival mechanism 
- Dynamic production & consumption of entities 
- Clean, modular design for education & testing 

---

## Tech Stack 
- Java 17+
- Maven
- JUnit (for testing) 
- JPGD Parser (for DOT parsing)
- JAXP + DOM (for XML parsing)
- Socket-based client-server architecture 

---

## How to Run 
1. Start the game server 
   ```bash
   ./mvnw exec:java@server

2. In a new terminal window, start the client 
   ```bash
   ./mvnw exec:java@client -Dexec.args="playername"
---

## Example Commands 
- leona: look
- leona: get key
- leona: goto cabin
- leona: unlock trapdoor with key
- leona: goto cellar
- leona: fight elf
- leona: health

---

## Configuration Files

| Filename | Type | Description |
|----------|------|-------------|
| `basic-entities.dot` / `extended-entities.dot` | DOT | Defines locations, furniture, artefacts, characters, and paths |
| `basic-actions.xml` / `extended-actions.xml`   | XML | Defines custom actions, triggers, effects, and narration |

---

## Supported Built-in Commands

| Command | Description |
|---------|-------------|
| `inventory` or `inv` | List items in inventory |
| `get <item>`         | Pick up an item |
| `drop <item>`        | Drop an item |
| `goto <location>`    | Move to another location |
| `look`               | Show details of current location |
| `health`             | Display current health (max 3) |


---

## Notes 
- Commands support flexible natural language parsing 
- Commands are case-insensitive 
- When a player's health reaches zero:
  - All items are dropped at the current location 
  - Player respawns at start location with full health 
