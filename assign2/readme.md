# CPD 2023/2024 - Distributed Systems Assignmen

Made by:

-   José Ribeiro
-   Rita Leite
-   Tiago Azevedo

## 1 - Description

The implemented game is a simplified version of the hangman game. The player's objective is to guess the proposed word, using the number of letters in the word as a hint and the game ends when the word is guessed. The player trying to guess the word can choose between suggesting a letter or a word.

If the player loses, `10` points are removed from the ranking, and if he wins, `30` points are added.

## 2 - Game Modes

When starting the server, you choose the mode in which it will operate, whether `simple` or `ranking` and the number of players per game.

The difference between them is that `simple mode` starts a new game whenever there are enough people on the waiting list to start, and `ranking mode` only creates games with people who have small differences between their personal rankings.

To avoid the problem of a player being eternally on the waiting list because there are no new players at his level, his ranking acceptance interval will be proportional to his waiting time. The logic is as follows, for example, if a player with rank 0 is waiting for 1 second, he will accept playing with people with ranking between -55 and 55, but if he is waiting for 2 seconds, he will accept between -60 and 60.
The acceptance range follows the formula: 50 + waitingTimeSeconds \* 5

## 3 - How to Play / Setup Client-Server connection

### Build

```
javac GameClient.java
javac Game.java
javac Player.java
javac GameServer.java
```

### Client

```
java GameClient <host> <port>
```

### Server

To run the server you need to define the `port`, the game `mode` and the `number of players per game`, which must always be greater than 2. To run in simple mode, the mode parameter must be equal to 0, and to run in ranking mode, it must be equal to 1.

```
java GameServer <port> <mode> <players/game>
```

## 4 - Fault Tolerance

If the client doesn't respond halfway through a game, he is skipped and marked as disconected. He can connect again, joining the same game if it has not finished in the meantime. If he is in a disconnected state while on the waiting list to join a new game, then if he is still disconected after 60 seconds since the last ping/game input not received and the start of a matchmaking atempt, he is removed from the queue.

It is also important to highlight that, to ensure that there are no games where all players are disconnected, a game ends, without winners or losers, when all players didn't respond consecutively (1 round of skips).

## 5 - Concurrency

We use a virtual thread that iterates over each player on the waiting list, checking whether they are connected or not. To carry out this check, the server calls the player's ping method, that, in a new virtual thread, sends a "ping" message and waits for the player to respond. Since there is an interaction in the player's socket within this method, we use a lock to ensure that operations are carried out safely.

We also use another virtual thread, if the game mode is ranking, to execute the matchMackingRanking function, that is responsible to try to initialize new games.

Since the matchMackingSimple and matchMackingRanking functions perform changes to the waiting list, we used a lock to ensure that operations on the waiting list are thread-safe, preventing race conditions

We also use one virtual thread for each game. After finishing the game and before thread termination, the players return to the waiting queue, using the addToQueueSimple or addToQueueRanking method (depending on the mode) for this purpose. Once again we use the waiting lock, locking and unlocking at the beginning and end, respectively, of these methods, to ensure that no problems occur.

Within each game it is not necessary to use locks since the game takes place in turns, that is, only one player interacts with it at a time.

Each player has it's own lock because they can be accessed by up to 3 threads at a time (login thread, ping thread and game thread ) to secure access to the shared state (socket, reader and writter) with the remaining fields not needing since they can only be read or can only be accessed in one thread at a time.

For each new connection we create a virtual thread to enable simultaneous logins, we also employ a lock for the player list only when verifying the password and registering new players

## 6 - User registration and authentication

For a player to register, they must choose a unique username and password.

To authenticate, it is also necessary to use the username and password, and it is not possible to change these parameters.

The username, password and ranking of each player is stored in the `players.txt` file, with the rankings being updated after the end of a game.
