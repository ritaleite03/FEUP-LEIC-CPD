import java.net.*;
import java.io.*;

public class GameClient {
    public static void main(String[] args) {
        if (args.length < 2)
            return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {
            socket.close();
            return;/*
                    * OutputStream output = socket.getOutputStream();
                    * PrintWriter writer = new PrintWriter(output, true);
                    * InputStream input = socket.getInputStream();
                    * BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    * BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                    * String res = reader.readLine();
                    * System.out.println(res);
                    * String in = r.readLine();
                    * writer.println(in);
                    */

        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex);
        }
    }
}
