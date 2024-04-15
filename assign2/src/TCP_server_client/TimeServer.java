import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */
public class TimeServer {
    static int gSum = 0;

    public static void main(String[] args) {
        if (args.length < 1)
            return;

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);
            ReentrantLock lock = new ReentrantLock();
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: ");
                Thread t = new Thread() {
                    public void run() {
                        try {
                            int sum = 0;
                            InputStream input = socket.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                            OutputStream output = socket.getOutputStream();
                            PrintWriter writer = new PrintWriter(output, true);

                            while (true) {
                                String number = reader.readLine();
                                if (number == null || number.length() == 0)
                                    break;
                                int x = Integer.parseInt(number);
                                sum += x;
                                lock.lock();
                                gSum += x;
                                lock.unlock();
                                writer.println("sum: " + sum);
                            }
                            writer.println("Total so far: " + gSum);
                            System.out.println("Client disconnected");
                        } catch (IOException e) {
                        }
                    }
                };
                t.start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}