# Simple Text Adventure Game (STAG)

## Project Overview
This is a Java-based text adventure game engine, inspired by classic text RPGs such as Zork.  
Players interact with the game world using natural language commands to explore, collect items, interact with characters, and solve tasks.

這是一個基於Java開發的文字冒險遊戲引擎，靈感來自經典文字RPG（例如 Zork）。  
玩家可以透過自然語言指令探索世界、收集物品、與角色互動並解決任務。

---

## Features 
- Multi-player support / 支援多人遊玩
- Configurable game world with DOT/XML / 可配置遊戲世界（DOT/XML）
- Health system and revival mechanism / 生命值與復活機制
- Dynamic production & consumption of entities / 動態生成與消耗實體
- Clean, modular design for education & testing / 模組化設計，方便教育與測試

---

## Tech Stack 
- Java 17+
- Maven
- JUnit (for testing) / 單元測試
- JPGD Parser (for DOT parsing)
- JAXP + DOM (for XML parsing)
- Socket-based client-server architecture / 基於 Socket 的客戶端-伺服器架構

---

## How to Run 
1. Start the game server / 啟動伺服器
   ```bash
   ./mvnw exec:java@server

2. In a new terminal window, start the client / 在新終端機啟動客戶端
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
| `inventory` or `inv` | List items in inventory / 顯示物品清單 |
| `get <item>`         | Pick up an item / 撿起物品 |
| `drop <item>`        | Drop an item / 丟下物品 |
| `goto <location>`    | Move to another location / 移動到其他地點 |
| `look`               | Show details of current location / 顯示當前地點資訊 |
| `health`             | Display current health (max 3) / 顯示血量（最多 3） |


---

## Notes 
- Commands support flexible natural language parsing / 指令支援靈活自然語言解析
- Commands are case-insensitive / 指令不分大小寫
- When a player's health reaches zero:
  - All items are dropped at the current location / 玩家死亡時物品會掉落
  - Player respawns at start location with full health / 玩家會在起始地點滿血復活
