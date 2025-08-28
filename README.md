# Simple Text Adventure Game (STAG)

## 📖 Project Overview
This project implements a text-based adventure engine written in Java.  
Inspired by classic interactive fiction games, the system allows players to explore a virtual world, interact with objects, and complete tasks through natural language commands.

---

## ✨ Features
- Configurable game world with DOT/XML 
- Health system and revival mechanism 
- Dynamic production & consumption of entities 
- Clean, modular design for education & testing 

---

## 🛠️ Tech Stack 
- **Java 17+** — Core programming language
- **Maven** — Project build & dependency management
- **JUnit** — Unit testing framework
- **JPGD Parser** — Used for DOT file parsing

---

## 🚀 How to Run 
1. Start the game server 
   ```bash
   ./mvnw exec:java@server

2. In a new terminal window, start the client 
   ```bash
   ./mvnw exec:java@client -Dexec.args="playername"

---

## 📂 Example World Files

| Filename | Type | Description |
|----------|------|-------------|
| `basic-entities.dot` / `extended-entities.dot` | DOT | Defines locations, objects, characters, and connections |
| `basic-actions.xml` / `extended-actions.xml`   | XML | Describes available actions, triggers, and effects |

---

## 🎮 Commands

| Command | Description |
|---------|-------------|
| `inventory` / `inv` | Show items carried by the player |
| `get <item>` | Pick up an item |
| `drop <item>` | Drop an item |
| `goto <location>` | Move to another place |
| `look` | Show the details of the current location |
| `health` | Display health status (max 3) |

---

## 📝 Notes 
- Commands support flexible natural language parsing 
- Commands are case-insensitive 
- When a player's health reaches zero:
  - All items are dropped at the current location 
  - Player respawns at start location with full health 
