import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

public class Player {

    // Player's username
    String username;

    // Player password
    String password;

    // Player connection to the server
    Socket socket;

    // To send data to the server
    PrintWriter writer;

    // To receive data from the server
    BufferedReader reader;

    // Player ranking
    int ranking = 500;

    // Marks the time since the player was inactive
    public long deadSince = -1;

    public boolean isPlaying = false;

    // Marks the time since the player entered the waiting queue
    public long waitingSince;

    // To ensure thread safety when accessing player attributes.
    private ReentrantLock lock;

    public Player() {
        lock = new ReentrantLock();
    }

    // Connects the player to the server
    public void connect(Socket socket) throws IOException {
        lock.lock();
        try {
            this.socket = socket;
            deadSince = -1;
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } finally {
            lock.unlock();
        }
    }

    // Sends a message to the player and reads the response
    String readLine(String s) {
        // This ensures that the read/write operation on the socket is thread-safe,
        // preventing race conditions
        lock.lock();
        try {
            if (socket.isClosed())
                return null;
            writer.println(s);
            writer.println("escreve");
            try {
                String res = reader.readLine();
                if (res != null)
                    return res;
            } catch (IOException e) {
            }
            // If there is no response or an exception occurs, the player is marked as
            // "disconnected"
            disconnect();
            return null;
        } finally {
            lock.unlock();
        }
    }

    void disconnect() {
        try {
            lock.lock();
            deadSince = System.currentTimeMillis();
            socket.close();
        } catch (IOException e) {
        } finally {
            lock.unlock();
        }
    }

    // Checks the player's connection with a "ping"
    void ping() {
        Thread.ofVirtual().start(() -> {
            lock.lock();
            try {
                // System.out.println("ping sent to: " + username);
                // var range = getRange();
                // System.out.println(String.format("range: %.2f %.2f", ranking - range, ranking
                // + range));
                socket.setSoTimeout(1 * 1000);
                writer.println("ping");
                try {
                    String res = reader.readLine();
                    if (res != null) {
                        // System.out.println("pong received from: " + username);
                        deadSince = -1;
                        return;
                    }
                } catch (IOException e) {
                }
                // System.out.println("pong not received from: " + username);
                disconnect();
            } catch (SocketException e) {
            } finally {
                try {
                    if (!socket.isClosed())
                        socket.setSoTimeout(45 * 1000);
                } catch (SocketException e) {
                }
                lock.unlock();
            }
        });
    }

    // Sends a message to the player
    void writeLine(String s) {
        lock.lock();
        try {
            if (socket.isClosed())
                return;
            writer.println(s);
        } finally {
            lock.unlock();
        }
    }

    String getName() {
        return username;
    }

    void loseGame() {
        ranking -= 10;
    }

    void winGame() {
        ranking += 30;
    }

    // Checks if another player's ranking is within an acceptable range for
    // matchmaking
    boolean inRange(Player other) {
        return Math.abs(ranking - other.ranking) < Math.min(getRange(), other.getRange());
    }

    // Calculates the acceptable range for matchmaking based on wait time
    double getRange() {
        return 50 + (System.currentTimeMillis() - waitingSince) * (5.0 / 1000);
    }
}
