import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * First LAN-multiplayer milestone for Dungeon Quest.
 * This verifies that multiple computers can connect and exchange messages.
 * It does not yet run or synchronize the actual Dungeon Quest game state.
 */
public final class LanServer {
    private static final int DEFAULT_PORT = 5000;
    private static final Set<ClientHandler> CLIENTS = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Dungeon Quest LAN test server started on port " + port);
            System.out.println("Keep this window open while clients connect.");

            while (true) {
                Socket socket = server.accept();
                ClientHandler client = new ClientHandler(socket);
                CLIENTS.add(client);
                client.start();
            }
        }
    }

    private static void broadcast(String message) {
        for (ClientHandler client : CLIENTS) {
            client.send(message);
        }
        System.out.println(message);
    }

    private static final class ClientHandler extends Thread {
        private final Socket socket;
        private PrintWriter out;
        private String playerName = "Player";

        private ClientHandler(Socket socket) {
            this.socket = socket;
            setName("LanClient-" + socket.getRemoteSocketAddress());
        }

        @Override
        public void run() {
            try (Socket s = socket;
                 BufferedReader in = new BufferedReader(new InputStreamReader(
                         s.getInputStream(), StandardCharsets.UTF_8))) {
                out = new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8);
                out.println("WELCOME|Enter your player name:");
                String requestedName = in.readLine();
                if (requestedName == null) return;
                requestedName = requestedName.trim();
                if (!requestedName.matches("[A-Za-z0-9_-]{1,20}")) {
                    out.println("ERROR|Name must be 1-20 letters, numbers, underscores, or hyphens.");
                    return;
                }
                playerName = requestedName;
                broadcast("SYSTEM|" + playerName + " joined the LAN session.");
                out.println("SYSTEM|Connected. Messages are currently a network test, not game actions.");

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("/quit")) break;
                    if (line.length() > 500) {
                        send("ERROR|Message too long (maximum 500 characters).");
                        continue;
                    }
                    broadcast("CHAT|" + playerName + ": " + line);
                }
            } catch (IOException e) {
                System.out.println(playerName + " connection ended: " + e.getMessage());
            } finally {
                CLIENTS.remove(this);
                broadcast("SYSTEM|" + playerName + " disconnected.");
            }
        }

        private void send(String message) {
            PrintWriter writer = out;
            if (writer != null) writer.println(message);
        }
    }
}

