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
    int id;
    int ranking;

    public Player() {
    }

    public void connect(Socket socket) throws IOException {
        this.socket = socket;
        OutputStream output = socket.getOutputStream();
        writer = new PrintWriter(output, true);
        InputStream input = socket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(input));
    }

    String readLine(String s) {
        if (socket.isClosed())
            return null;
        writer.println(s);
        try {
            String res = reader.readLine();
            if (res != null) {
                return res;
            }
        } catch (IOException e) {
        }
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ex) {
            }
        }
        return null;
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

    }

    void winGame() {

    }

}
