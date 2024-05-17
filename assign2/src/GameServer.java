import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class GameServer {

    // List of all players registered on the server
    private static List<Player> players = new ArrayList<>();

    // List of players waiting to play
    private static ArrayList<Player> waiting = new ArrayList<>(); 

    // To ensure thread safety when accessing shared lists
    private static ReentrantLock lock = new ReentrantLock();

    // Number of players required to start a game
    private static final int N = 2;

    // Adds a player to the queue and starts a game if there are enough players
    public static void addToQueue(Player p) {
        // To ensure that operations on the waiting list are thread-safe, preventing race conditions
        lock.lock();
        cleanup();
        // If a player with the same name is already in the queue, the method returns immediately, avoiding duplicates
        for (Player waitingP : waiting) {
            if (waitingP.getName().equals(p.getName())) return;
        }
        // If there are enough players in the queue
        if (waiting.size() == N - 1) {
            // Create a gamePlayers list and add all queued players to that list
            List<Player> gamePlayers = new ArrayList<>();
            for (Player wPlayer : waiting) {
                gamePlayers.add(wPlayer);
            }
            waiting.clear();
            gamePlayers.add(p);
            Game game = new Game(gamePlayers);
            // Starting a new game
            new Thread(() -> {
                game.run();
                for (Player player : gamePlayers) {
                    addToQueue(player);
                }
            }).start();
        } else {
            waiting.add(p);
        }
        lock.unlock();
    }

    // Add a player to the queue with ranking-based matchmaking
    public static void addToQueue2(Player p) {
        lock.lock();
        for (Player waitingP : waiting) {
            if (waitingP.getName().equals(p.getName()))
                return;
        }
        waiting.add(p);
        p.waitingSince = System.currentTimeMillis();
        lock.unlock();
    }

    // Try to pair up players with similar rankings and start new games
    private static void matchMacking() {
        lock.lock();
        cleanup();
        List<Player> gamePlayers = new ArrayList<>();
        for (Player first : waiting) {
            // Clears the gamePlayers list and adds first to that list
            gamePlayers.clear();
            gamePlayers.add(first);
            // Iterates through the queue to find players who are within the rank range
            for (Player candidate : waiting) {
                if (first == candidate)
                    continue;
                if (first.inRange(candidate)) {
                    gamePlayers.add(candidate);
                    if (gamePlayers.size() == N) {
                        break;
                    }
                }
            }
            // If gamePlayers reaches size N, it stops the loop
            if (gamePlayers.size() == N) {
                break;
            }
        }
        if (gamePlayers.size() == N) {
            // Removes selected players from the waiting list
            for (Player player : gamePlayers){
                waiting.remove(player);
            }
            // Match creation and start
            Game game = new Game(gamePlayers);
            new Thread(() -> {
                game.run();
                for (Player player : gamePlayers) {
                    addToQueue2(player);
                }
            }).start();
            matchMacking();
        }
        lock.unlock();
    }

    // Removes disconnected players from the queue
    private static void cleanup() {
        lock.lock();
        List<Player> toDelete = new ArrayList<>();
        for (Player waitingP : waiting) {
            if (System.currentTimeMillis() - waitingP.deadSince > 60 * 1000) {
                toDelete.add(waitingP);
            }
        }
        for (Player waitingP : toDelete) {
            waiting.remove(waitingP);
        }
        lock.unlock();
    }

    // Starts the server, listens for new connections and authenticates players
    public static void main(String[] args) {
        if (args.length < 2) return;
        int port = Integer.parseInt(args[0]);
        int mode = Integer.parseInt(args[1]);
        if (mode < 0 || mode > 1) {
            System.out.println("Invalid mode!");
            return;
        }
        // Matchmaking and Ping Thread
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1 * 1000); // sleep for 1 second
                } catch (InterruptedException e) {
                }
                // If the mode is 1, call the matchMacking method to try to pair players
                if (mode == 1) matchMacking();
                // Pings each player in the queue to check connectivity
                for (Player p : waiting) {
                    p.ping();
                }
            }
        }).start();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // Accepts the connection and sets a 45 second timeout for the socket
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(45 * 1000);
                System.out.println("New client connected: ");
                // Creates a new Player instance and connects it to the socket
                Player p = new Player();
                p.connect(socket);
                String username = p.readLine("Username: ");
                String password = p.readLine("Password: ");
                // Uses a lock to ensure that the player lists and queue are accessed safely between threads
                lock.lock();
                boolean exists = false;
                for (Player existing : players) {
                    if (existing.username.equals(username)) {
                        exists = true;
                        existing.deadSince = -1;
                        // If registered and the password is correct, it reconnects the player and adds him to the waiting queue
                        if (existing.password.equals(password)) {
                            existing.connect(socket);
                            if (mode == 0)
                                addToQueue(p);
                            else
                                addToQueue2(p);
                        } 
                        // If the password is incorrect, it sends an error message and closes the socket
                        else {
                            existing.writeLine("PASSWORD ERRADA");
                            socket.close();
                        }
                    }
                }
                // If the player is not registered, add him to the player list and queue
                if (!exists) {
                    p.username = username;
                    p.password = password;
                    players.add(p);
                    if (mode == 0)
                        addToQueue(p);
                    else
                        addToQueue2(p);
                }
                lock.unlock();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
