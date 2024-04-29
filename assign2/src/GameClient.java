import java.net.*;
import java.io.*;

public class GameClient {
    public static void main(String[] args) {
        if (args.length < 2)
            return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String res = reader.readLine();
                if (res == null)
                    continue;
                if (res.equals("escreve")) {
                    String in = r.readLine();
                    writer.println(in);
                } else {
                    System.out.println(res);
                }
            }

        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex);
        }
    }
}
