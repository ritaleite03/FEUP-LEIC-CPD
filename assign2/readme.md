# Hangman Game with Concurrency – Project 2

**Hangman Game with Concurrency** is a multiplayer, simplified Hangman game designed to handle **concurrent access, fault tolerance, and ranking-based matchmaking**.

> ⚠️ **Note:** The full, detailed report of this project is available in [`report.md`](report.md).

## 📌 Project Description

- Players guess a word using letter hints or the full word.
- Points system: **+30** for a win, **-10** for a loss.
- Supports multiple concurrent players with **robust client-server architecture**.
- Ensures **fault tolerance** if clients disconnect mid-game.

## 🎮 Game Modes

| Mode             | Description                                                                                                                      |
| ---------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| **Simple Mode**  | New games start when enough players are on the waiting list.                                                                     |
| **Ranking Mode** | Games start only for players with **similar rankings**; waiting time increases acceptance range (`50 + waitingTimeSeconds * 5`). |

## ⚙ Features

- **Concurrency:** virtual threads for each game, matchmaking, and client pings.
- **Thread-safety:** locks on waiting list and player objects to prevent race conditions.
- **Fault tolerance:** disconnected players are skipped; can rejoin if the game hasn't ended.
- **User authentication:** unique username and password; stored in `players.txt`.
- **Ranking system:** updated after each game.

## 🏃 How to Run

### Build

```bash
javac GameClient.java
javac Game.java
javac Player.java
javac GameServer.java
```

### Start Server

```bash
java GameServer <port> <mode> <players/game>
```

- `<mode>`: `0` for simple mode, `1` for ranking mode
- `<players/game>`: number of players per game (minimum 2)

### Start Client

```bash
java GameClient <host> <port>
```

- Connects to the server and allows player interaction.

## 🔒 Concurrency & Fault Tolerance

- **Virtual threads** for client pings and game matchmaking
- **Locks** ensure safe access to shared resources (waiting list, player sockets).
- **Disconnected players** are skipped during games and removed from waiting queue after 60s of inactivity

## 📝 User Registration & Authentication

- Players must **register** with a unique username and password.
- Authentication is required to join games.
- User data and rankings are stored in `players.txt`.
