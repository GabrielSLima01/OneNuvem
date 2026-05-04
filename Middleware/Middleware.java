import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Middleware {
    private final NodeManager nodeManager;
    private final int connectionTimeoutMs;
    private final int readTimeoutMs;

    public Middleware(NodeManager nodeManager) {
        this(nodeManager, 2000, 2000);
    }

    public Middleware(NodeManager nodeManager, int connectionTimeoutMs, int readTimeoutMs) {
        this.nodeManager = nodeManager;
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    public String handleRequest(String request) {
        int maxAttempts = nodeManager.size();

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Node node = nodeManager.getNextHealthyNode();

            if (node == null) {
                return "ERRO: nenhum servidor saudavel disponivel";
            }

            try {
                String response = sendToNode(node, request);
                nodeManager.markAsHealthy(node);
                return response;
            } catch (IOException error) {
                System.out.println("Falha no " + node.getName() + ": " + error.getMessage());
                nodeManager.markAsFailed(node);
            }
        }

        return "ERRO: todos os servidores falharam";
    }

    private String sendToNode(Node node, String request) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(node.getHost(), node.getPort()), connectionTimeoutMs);
            socket.setSoTimeout(readTimeoutMs);

            BufferedWriter output = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
            );
            BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );

            output.write(request);
            output.newLine();
            output.flush();

            String response = input.readLine();

            if (response == null) {
                throw new IOException("servidor fechou a conexao sem responder");
            }

            return response;
        }
    }
}
