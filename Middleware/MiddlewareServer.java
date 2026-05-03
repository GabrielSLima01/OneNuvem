import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
            Socket socket = clientSocket;
            BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
            BufferedWriter output = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
            )
        ) {
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
        NodeManager nodeManager = new NodeManager();

        nodeManager.addNode(new Node("server-1", "localhost", 5001));
        nodeManager.addNode(new Node("server-2", "localhost", 5002));
        nodeManager.addNode(new Node("server-3", "localhost", 5003));

        Middleware middleware = new Middleware(nodeManager);
        MiddlewareServer server = new MiddlewareServer(8000, middleware);

        server.start();
    }
}
