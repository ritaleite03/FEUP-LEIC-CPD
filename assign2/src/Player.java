import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

public class Player {
    String username;
    String password;
    Socket socket;
    PrintWriter writer;
    BufferedReader reader;
    int ranking = 500;
    public long deadSince = -1;
    public boolean removed = false;
    public long waitingSince;
    private ReentrantLock lock;

    public Player() {
        lock = new ReentrantLock();
    }

    public void connect(Socket socket) throws IOException {
        lock.lock();
        try {
            this.socket = socket;
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } finally {
            lock.unlock();
        }
    }

    boolean connected() {
        lock.lock();
        try {
            return !socket.isClosed();
        } finally {
            lock.unlock();
        }
    }

    String readLine(String s) {
        lock.lock();
        try {
            if (socket.isClosed())
                return null;
            writer.println(s);
            writer.println("escreve");
            try {
                String res = reader.readLine();
                if (res != null) {
                    return res;
                }
            } catch (IOException e) {
            }
            deadSince = System.currentTimeMillis();
            return null;
        } finally {
            lock.unlock();
        }
    }

    void ping() {
        lock.lock();
        try {
            if (deadSince > 0)
                return;
            System.out.println("ping sent to: " + username);
            socket.setSoTimeout(1 * 1000);
            writer.println("ping");
            try {
                String res = reader.readLine();
                if (res != null) {
                    System.out.println("pong received from: " + username);
                    deadSince = -1;
                    return;
                }
            } catch (IOException e) {
            }
            System.out.println("pong not received from: " + username);
            deadSince = System.currentTimeMillis();
        } catch (SocketException e) {
        } finally {
            try {
                socket.setSoTimeout(45 * 1000);
            } catch (SocketException e) {
            }
            lock.unlock();
        }
    }

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

    boolean inRange(Player other) {
        return Math.abs(ranking - other.ranking) < Math.min(getRange(), other.getRange());
    }

    float getRange() {
        return 50 + (System.currentTimeMillis() - waitingSince) * (10 / 1000);
    }
}
