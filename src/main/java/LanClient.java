import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Client for the first Dungeon Quest LAN networking test.
 * It verifies connection and message exchange; it is not yet the game client.
 */
public final class LanClient {
    private static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        String host = args.length > 0 ? args[0] : ask(console, "Server IP (use 127.0.0.1 on the same PC): ");
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        try (Socket socket = new Socket(host, port);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {

            String welcome = serverIn.readLine();
            if (welcome == null) {
                System.out.println("Server closed the connection.");
                return;
            }
            System.out.println(welcome.replace('|', ' '));
            String name = ask(console, "Player name: ").trim();
            serverOut.println(name);

            Thread receiver = new Thread(() -> {
                try {
                    String message;
                    while ((message = serverIn.readLine()) != null) {
                        System.out.println("\n" + message.replace('|', ' '));
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    System.out.println("\nDisconnected from server.");
                }
            }, "LanServerReceiver");
            receiver.setDaemon(true);
            receiver.start();

            System.out.println("Type a message and press Enter. Type /quit to leave.");
            while (console.hasNextLine()) {
                String line = console.nextLine();
                serverOut.println(line);
                if (line.equalsIgnoreCase("/quit")) break;
                System.out.print("> ");
            }
        } catch (IOException e) {
            System.out.println("Could not connect to " + host + ":" + port);
            System.out.println("Check the server is running, the IP/port are correct, and the firewall allows Java.");
            System.out.println("Details: " + e.getMessage());
        }
    }

    private static String ask(Scanner console, String prompt) {
        System.out.print(prompt);
        return console.hasNextLine() ? console.nextLine() : "";
    }
}
