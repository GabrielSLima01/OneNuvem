import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

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
        if (startsWithCommand(request, "UPLOAD")) {
            return replicateUpload(request);
        }

        if (startsWithCommand(request, "DOWNLOAD")) {
            return downloadFromAvailableNode(request);
        }

        if (startsWithCommand(request, "LIST_FILES")) {
            return listFilesFromNodes(request);
        }

        int maxAttempts = nodeManager.size();

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Node node = nodeManager.getNextNode();

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

    private String replicateUpload(String request) {
        int successCount = 0;
        int maxAttempts = nodeManager.size();

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Node node = nodeManager.getNextNode();

            if (node == null) {
                break;
            }

            try {
                String response = sendToNode(node, request);
                nodeManager.markAsHealthy(node);

                if (startsWithCommand(response, "OK") || startsWithCommand(response, "SUCCESS")) {
                    successCount++;
                }
            } catch (IOException error) {
                System.out.println("Falha no " + node.getName() + ": " + error.getMessage());
                nodeManager.markAsFailed(node);
            }
        }

        if (successCount == 0) {
            return "ERRO: nenhum servidor salvou o arquivo";
        }

        return "OK replicado_em=" + successCount;
    }

    private String downloadFromAvailableNode(String request) {
        int maxAttempts = nodeManager.size();
        String lastResponse = "ERRO: arquivo nao encontrado nos servidores saudaveis";

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Node node = nodeManager.getNextNode();

            if (node == null) {
                return "ERRO: nenhum servidor saudavel disponivel";
            }

            try {
                String response = sendToNode(node, request);
                nodeManager.markAsHealthy(node);

                if (startsWithCommand(response, "FILE_DATA")) {
                    return response;
                }

                lastResponse = response;
            } catch (IOException error) {
                System.out.println("Falha no " + node.getName() + ": " + error.getMessage());
                nodeManager.markAsFailed(node);
            }
        }

        return lastResponse;
    }

    private String listFilesFromNodes(String request) {
        Set<String> files = new LinkedHashSet<>();
        int maxAttempts = nodeManager.size();

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Node node = nodeManager.getNextNode();

            if (node == null) {
                break;
            }

            try {
                String response = sendToNode(node, request);
                nodeManager.markAsHealthy(node);

                if (startsWithCommand(response, "FILES")) {
                    addFiles(files, response);
                }
            } catch (IOException error) {
                System.out.println("Falha no " + node.getName() + ": " + error.getMessage());
                nodeManager.markAsFailed(node);
            }
        }

        if (files.isEmpty()) {
            return "FILES";
        }

        return "FILES " + String.join(",", files);
    }

    private void addFiles(Set<String> files, String response) {
        String payload = commandPayload(response);

        if (payload.isEmpty()) {
            return;
        }

        String[] fileNames = payload.split(",");

        for (String fileName : fileNames) {
            String trimmedFileName = fileName.trim();

            if (!trimmedFileName.isEmpty()) {
                files.add(trimmedFileName);
            }
        }
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

    private boolean startsWithCommand(String text, String command) {
        return text.equals(command) || text.startsWith(command + " ");
    }

    private String commandPayload(String response) {
        int firstSpace = response.indexOf(' ');

        if (firstSpace < 0) {
            return "";
        }

        return response.substring(firstSpace + 1).trim();
    }
}
