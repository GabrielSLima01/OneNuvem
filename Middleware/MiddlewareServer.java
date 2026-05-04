
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MiddlewareServer {

    private final int port;
    private final Middleware middleware;

    public MiddlewareServer(int port, Middleware middleware) {
        this.port = port;
        this.middleware = middleware;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Middleware escutando na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                Socket socket = clientSocket; BufferedReader input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                ); BufferedWriter output = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
                )) {
            String request = input.readLine();

            if (request == null) {
                return;
            }

            String response = middleware.handleRequest(request);
            output.write(response);
            output.newLine();
            output.flush();
        } catch (IOException error) {
            System.out.println("Erro atendendo cliente: " + error.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        Map<String, String> config = loadDotEnv();
        NodeManager nodeManager = new NodeManager();

        loadNodes(nodeManager, config);

        Middleware middleware = new Middleware(nodeManager);
        MiddlewareServer server = new MiddlewareServer(getRequiredPort(config, "MIDDLEWARE_PORT"), middleware);

        server.start();
    }

    private static Map<String, String> loadDotEnv() throws IOException {
        Map<String, String> config = new HashMap<>();
        Path envPath = Path.of(".env");

        for (String line : Files.readAllLines(envPath, StandardCharsets.UTF_8)) {
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                continue;
            }

            String[] parts = trimmedLine.split("=", 2);

            if (parts.length != 2) {
                continue;
            }

            String key = parts[0].trim();
            String value = stripQuotes(parts[1].trim());
            config.put(key, value);
        }

        return config;
    }

    private static String stripQuotes(String value) {
        return value.replace("\"", "").replace("'", "");
    }

    private static void loadNodes(NodeManager nodeManager, Map<String, String> config) {
        String[] nodeKeys = getRequiredValue(config, "MIDDLEWARE_NODES").split(",");

        for (String nodeKey : nodeKeys) {
            String prefix = nodeKey.trim();

            if (prefix.isEmpty()) {
                continue;
            }

            nodeManager.addNode(new Node(
                    getRequiredValue(config, prefix + "_NAME"),
                    getRequiredValue(config, prefix + "_HOST"),
                    getRequiredPort(config, prefix + "_PORT")
            ));
        }
    }

    private static String getRequiredValue(Map<String, String> config, String key) {
        return config.get(key);
    }

    private static int getRequiredPort(Map<String, String> config, String key) {
        return Integer.parseInt(getRequiredValue(config, key));
    }
}
