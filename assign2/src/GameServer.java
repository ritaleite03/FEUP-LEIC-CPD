import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class GameServer {
    private static List<Player> players = new ArrayList<>();
    private static ArrayList<Player> waiting = new ArrayList<>();
    private static ReentrantLock lock = new ReentrantLock();
    private static final int N = 2;

    public static void addToQueue2(Player p) {
        lock.lock();
        waiting.add(p);
        p.waitingSince = System.currentTimeMillis();
        // matchMacking();
        lock.unlock();
    }

    private static void matchMacking() {
        lock.lock();
        List<Player> gamePlayers = new ArrayList<>();
        for (Player first : waiting) {
            gamePlayers.clear();
            gamePlayers.add(first);
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
            if (gamePlayers.size() == N) {
                break;
            }

        }
        if (gamePlayers.size() == N) {
            for (Player player : gamePlayers) {
                waiting.remove(player);
            }

            Game game = new Game(gamePlayers);
            new Thread(() -> {
                game.run();
                for (Player player : gamePlayers) {
                    if (System.currentTimeMillis() - player.deadSince < 60 * 1000)
                        addToQueue(player);
                }
            }).start();
            matchMacking();
        }
        lock.unlock();
    }

    private static void cleanup() {
        List<Player> toDelete = new ArrayList<>();

        for (Player waitingP : waiting) {
            if (System.currentTimeMillis() - waitingP.deadSince < 60 * 1000) {
                toDelete.add(waitingP);
            }
        }
        for (Player waitingP : toDelete) {
            waiting.remove(waitingP);
        }
    }

    public static void addToQueue(Player p) {
        lock.lock();
        cleanup();
        for (Player waitingP : waiting) {
            if (waitingP == p)
                return;
        }
        if (waiting.size() == N - 1) {
            List<Player> gamePlayers = new ArrayList<>();
            for (Player wPlayer : waiting) {
                gamePlayers.add(wPlayer);
            }
            waiting.clear();
            gamePlayers.add(p);
            Game game = new Game(gamePlayers);

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

    public static void main(String[] args) {
        if (args.length < 2)
            return;

        int port = Integer.parseInt(args[0]);
        int mode = Integer.parseInt(args[1]);

        if (mode < 0 || mode > 1) {
            System.out.println("Invalid mode!");
            return;
        }
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1 * 1000);
                } catch (InterruptedException e) {
                }
                lock.lock();
                if (mode == 1) {
                    matchMacking();
                }
                lock.unlock();
                for (Player p : players) {
                    p.ping();
                }
            }
        }).start();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(45 * 1000);
                System.out.println("New client connected: ");
                Player p = new Player();
                p.connect(socket);
                String username = p.readLine("Username: ");
                String password = p.readLine("Password: ");
                lock.lock();
                boolean exists = false;
                for (Player existing : players) {
                    if (existing.username.equals(username)) {
                        exists = true;
                        if (existing.password.equals(password)) {
                            existing.connect(socket);
                            if (mode == 0)
                                addToQueue(p);
                            else
                                addToQueue2(p);
                        } else {
                            existing.writeLine("PASSWORD ERRADA");
                            socket.close();
                        }
                    }
                }
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
