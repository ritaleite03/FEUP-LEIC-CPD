import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class GameServer {
    private List<Socket> userSockets;

    public GameServer(int players, List<Socket> userSockets) {
        this.userSockets = userSockets;
    }

    public void start() {
        System.out.println("Starting game vith " + userSockets.size() + " players");
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
                System.out.println(p.readLine("ola"));
                System.out.println(socket.isClosed());
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
