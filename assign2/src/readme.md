# CPD 2023/2024 - Distributed Systems Assignmen

Made by:

- José Ribeiro
- Rita Leite
- Tiago Azevedo

## 1 - Description

The implemented game is a simplified version of the hangman game. The player's objective is to guess the proposed word, using the number of letters in the word as a hint and the game ends when the word is guessed. The player trying to guess the word can choose between suggesting a letter or a word.

If the player loses, `10` points are removed from the ranking, and if he wins, `30` points are added.

## 2 - Game Modes

When starting the server, you choose the mode in which it will operate, whether `simple` or `ranking`.

The difference between them is that `simple mode` starts a new game whenever there are enough people on the waiting list to start, and `ranking mode` only creates games with people who have small differences between their personal rankings.

To avoid the problem of a player being eternally on the waiting list because there are no new players at his level, his ranking acceptance interval will be proportional to his waiting time. The logic is as follows, for example, if a player with rank 0 is waiting for 1 second, he will accept playing with people with ranking between -1 and 1, but if he is waiting for 2 seconds, he will accept between -2 and 2 .

## 3 - How to Play / Setup Client-Server connection

## 4 - Fault Tolerance

## 5 - Concurrency

We use a `thread` that iterates over each player on the waiting list, checking whether they are connected or not. To carry out this check, the server sends a message to the player (in this case, "ping"), and waits for the player to respond. Since there is a change in the player's state within this method, we use `ReentrantLock` to ensure that operations are carried out safely.
Also in this thread, if the game mode is ranking, the matchMacking function is executed to try to initialize new games. Since this function performs changes to the waiting list, we used a `ReentrantLock` to ensure that operations on the waiting list are thread-safe, preventing race conditions

`For each game, we also use threads`. After finishing each of them, players return to the waiting queue, using the addToQueue or addToQueue2 method (depending on the mode) for this purpose. Once again we use `ReentrantLock`, locking and unlocking at the beginning and end, respectively, of these methods, to ensure that no problems occur.

## 6 - User registration and authentication
