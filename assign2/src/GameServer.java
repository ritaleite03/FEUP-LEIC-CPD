import java.io.*;
import java.net.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class GameServer {
    private static List<Player> players = new ArrayList<>();
    private static ArrayDeque<Player> waiting = new ArrayDeque<>();
    private static ReentrantLock lock = new ReentrantLock();
    private static final int N = 2;

    public static void addToQueue(Player p) {
        lock.lock();
        if (waiting.size() == N - 1) {
            List<Player> gamePlayers = new ArrayList<>();
            while (waiting.size() > 0) {
                gamePlayers.add(waiting.poll());
            }
            gamePlayers.add(p);
            Game game = new Game(gamePlayers);

            new Thread(() -> {
                for (Player player : gamePlayers) {
                    player.isPlaying = true;
                }
                game.run();
                for (Player player : gamePlayers) {
                    player.isPlaying = false;
                    addToQueue(player);
                }
            }).start();
        } else {
            waiting.add(p);
        }
        lock.unlock();
    }

    public static void main(String[] args) {
        if (args.length < 1)
            return;

        int port = Integer.parseInt(args[0]);

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
                    if (existing.username.equals(username) && existing.password.equals(password)) {
                        exists = true;
                        existing.connect(socket);
                    }
                }
                if (!exists) {
                    p.username = username;
                    p.password = password;
                    players.add(p);
                    addToQueue(p);
                }
                lock.unlock();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
