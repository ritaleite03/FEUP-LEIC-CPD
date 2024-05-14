import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;

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

    public Player() {
    }

    public void connect(Socket socket) throws IOException {
        this.socket = socket;
        OutputStream output = socket.getOutputStream();
        writer = new PrintWriter(output, true);
        InputStream input = socket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(input));
    }

    boolean connected() {
        return !socket.isClosed();
    }

    String readLine(String s) {
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
    }

    void ping() {
        if (deadSince > 0)
            return;
        writer.println("ping");
        try {
            String res = reader.readLine();
            if (res != null) {
                deadSince = -1;
                return;
            }
        } catch (IOException e) {
        }
        deadSince = System.currentTimeMillis();
    }

    void writeLine(String s) {
        if (socket.isClosed())
            return;
        writer.println(s);
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
