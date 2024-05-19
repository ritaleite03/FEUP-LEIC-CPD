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
    private static ReentrantLock waitingLock = new ReentrantLock();
    private static ReentrantLock playersLock = new ReentrantLock();

    // Number of players required to start a game
    private static int N = 2;

    // Adds a player to the queue and starts a game if there are enough players
    public static void addToQueueSimple(Player p) {
        // To ensure that operations on the waiting list are thread-safe, preventing
        // race conditions
        waitingLock.lock();
        try {
            // If a player with the same name is already in the queue, the method returns
            // immediately, avoiding duplicates
            boolean duplicate = false;
            for (Player waitingP : waiting) {
                if (waitingP.getName().equals(p.getName()))
                    duplicate = true;
            }
            if (!duplicate)
                waiting.add(p);

            matchMackingSimple();
        } finally {
            waitingLock.unlock();
        }
    }

    private static void matchMackingSimple() {
        waitingLock.lock();
        try {
            cleanup();
            // If there are enough players in the queue
            if (waiting.stream().filter(x -> x.deadSince < 0).count() == N) {
                // Create a gamePlayers list and add all queued players to that list
                List<Player> gamePlayers = new ArrayList<>();
                for (Player wPlayer : waiting) {
                    if (wPlayer.deadSince < 0) {
                        gamePlayers.add(wPlayer);
                        wPlayer.isPlaying = true;
                    }
                }
                waiting.removeIf(x -> x.deadSince < 0);
                Game game = new Game(gamePlayers);
                // Starting a new game
                Thread.ofVirtual().start(() -> {
                    game.run();
                    savePlayers();
                    for (Player player : gamePlayers) {
                        addToQueueSimple(player);
                    }
                });
            }
        } finally {
            waitingLock.unlock();
        }
    }

    // Add a player to the queue with ranking-based matchmaking
    public static void addToQueueRanking(Player p) {
        waitingLock.lock();
        try {
            for (Player waitingP : waiting) {
                if (waitingP.getName().equals(p.getName()))
                    return;
            }
            waiting.add(p);
            p.waitingSince = System.currentTimeMillis();
        } finally {
            waitingLock.unlock();
        }
    }

    // Try to pair up players with similar rankings and start new games
    private static void matchMackingRanking() {
        waitingLock.lock();
        try {
            cleanup();
            List<Player> gamePlayers = new ArrayList<>();
            for (Player first : waiting) {
                if (first.deadSince > 0)
                    continue;
                // Clears the gamePlayers list and adds first to that list
                gamePlayers.clear();
                gamePlayers.add(first);
                // Iterates through the queue to find players who are within the rank range
                for (Player candidate : waiting) {
                    if (candidate.deadSince > 0)
                        continue;
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
                for (Player player : gamePlayers) {
                    player.isPlaying = true;
                    waiting.remove(player);
                }
                // Match creation and start
                Game game = new Game(gamePlayers);
                Thread.ofVirtual().start(() -> {
                    game.run();
                    savePlayers();
                    for (Player player : gamePlayers) {
                        addToQueueRanking(player);
                    }
                });
                matchMackingRanking();
            }
        } finally {
            waitingLock.unlock();
        }
    }

    // Removes disconnected players from the queue
    private static void cleanup() {
        waitingLock.lock();
        try {
            List<Player> toDelete = new ArrayList<>();
            for (Player waitingP : waiting) {
                if (waitingP.deadSince > 0 && System.currentTimeMillis() - waitingP.deadSince > 60 * 1000) {
                    toDelete.add(waitingP);
                }
            }
            for (Player waitingP : toDelete) {
                System.out.println("removing " + waitingP.getName() + " from waiting");
                waiting.remove(waitingP);
            }
        } finally {
            waitingLock.unlock();
        }
    }

    // Starts the server, listens for new connections and authenticates players
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Invalid number of arguments!");
            System.out.println("Usage: java GameServer <port> <mode> <players/game>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        int mode = Integer.parseInt(args[1]);
        N = Integer.parseInt(args[2]);
        if (mode < 0 || mode > 1) {
            System.out.println("Invalid mode!");
            return;
        }
        if (N < 2) {
            System.out.println("Invalid number of players!");
            return;
        }
        System.out.println("Starting server with:");
        System.out.println("    port: " + port);
        System.out.println("    mode: " + (mode == 0 ? "Simple" : "Ranking"));
        System.out.println("    players/game: " + N);
        loadPlayers();
        // Matchmaking and Ping Thread
        Thread.ofVirtual().start(() -> {
            while (true) {
                try {
                    Thread.sleep(1 * 1000); // sleep for 1 second
                } catch (InterruptedException e) {
                }
                // Pings each player in the queue to check connectivity
                waitingLock.lock();
                var waitingC = new ArrayList<>(waiting);
                waitingLock.unlock();
                for (Player p : waitingC) {
                    p.ping();
                }
            }
        });
        // If the mode is 1, call the matchMacking method every second to try to pair
        // players
        if (mode == 1)
            Thread.ofVirtual().start(() -> {
                while (true) {
                    try {
                        Thread.sleep(1 * 1000); // sleep for 1 second
                    } catch (InterruptedException e) {
                    }
                    matchMackingRanking();
                }
            });

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // Accepts the connection and sets a 45 second timeout for the socket
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(45 * 1000);
                System.out.println("New client connected");
                // Creates a new Player instance and connects it to the socket
                Thread.ofVirtual().start(() -> {
                    Player p = new Player();
                    try {
                        p.connect(socket);
                    } catch (IOException ex) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                        }
                        return;
                    }
                    String username = p.readLine("Username: ");
                    String password = p.readLine("Password: ");
                    if (username == null || username.length() < 1 || password == null || password.length() < 1) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                        }
                        System.out.println("invalid");

                        return;
                    }
                    System.out.println("pass: " + username + " " + password);
                    // Uses a lock to ensure that the player lists and queue are accessed safely
                    // between threads
                    try {
                        playersLock.lock();
                        boolean exists = false;
                        for (Player existing : players) {
                            if (existing.username.equals(username)) {
                                exists = true;
                                // If registered and the password is correct, it reconnects the player and adds
                                // him to the waiting queue
                                if (existing.password.equals(password)) {
                                    existing.connect(socket);
                                    if (!existing.isPlaying) {
                                        if (mode == 0)
                                            addToQueueSimple(existing);
                                        else
                                            addToQueueRanking(existing);
                                    }
                                }
                                // If the password is incorrect, it sends an error message and closes the socket
                                else {
                                    p.writeLine("PASSWORD ERRADA");
                                    socket.close();
                                }
                                break;
                            }
                        }
                        // If the player is not registered, add him to the player list and queue
                        if (!exists) {
                            p.username = username;
                            p.password = password;
                            players.add(p);
                            savePlayers();
                            if (mode == 0)
                                addToQueueSimple(p);
                            else
                                addToQueueRanking(p);
                        }
                    } catch (IOException e) {
                    } finally {
                        playersLock.unlock();
                    }
                });
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void savePlayers() {
        playersLock.lock();
        try (
                FileWriter file = new FileWriter("players.txt");
                var writer = new BufferedWriter(file)) {
            for (Player p : players) {
                System.out.println("Saving player: " + p.username + " " + p.ranking);
                writer.write(p.username);
                writer.write('\n');
                writer.write(p.password);
                writer.write('\n');
                writer.write("" + p.ranking);
                writer.write('\n');
                writer.write('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            playersLock.unlock();
        }
    }

    private static void loadPlayers() {
        try (FileReader file = new FileReader("players.txt")) {
            var reader = new BufferedReader(file);
            while (true) {
                Player p = new Player();
                p.username = reader.readLine();
                if (p.username == null)
                    return;
                p.password = reader.readLine();
                p.ranking = Integer.parseInt(reader.readLine());
                reader.readLine();
                System.out.println("Loading player: " + p.username + " " + p.ranking);
                players.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
