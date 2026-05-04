import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoStorageNode {

    private final String name;
    private final int port;
    private final Path storagePath;
    private final ExecutorService executor;

    public DemoStorageNode(String name, int port, Path storagePath) throws IOException {
        this.name = name;
        this.port = port;
        this.storagePath = storagePath;
        this.executor = Executors.newCachedThreadPool();

        Files.createDirectories(storagePath);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(name + " escutando na porta " + port + " storage=" + storagePath);

            while (true) {
                Socket socket = serverSocket.accept();
                executor.submit(() -> handleClient(socket));
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

            if (request == null || request.trim().isEmpty()) {
                writeResponse(output, "ERROR comando vazio");
                return;
            }

            String response = handleRequest(request.trim());
            writeResponse(output, response);
        } catch (IOException error) {
            System.out.println(name + " falhou ao atender cliente: " + error.getMessage());
        }
    }

    private String handleRequest(String request) {
        String[] parts = request.split("\\s+", 3);
        String command = parts[0].toUpperCase();

        if ("HEARTBEAT".equals(command)) {
            return "OK " + name + " ativo";
        }

        if ("REGISTER_NODE".equals(command)) {
            return "OK " + name + " registro recebido";
        }

        if ("UPLOAD".equals(command)) {
            return upload(parts);
        }

        if ("DOWNLOAD".equals(command)) {
            return download(parts);
        }

        if ("LIST_FILES".equals(command)) {
            return listFiles();
        }

        return "ERROR comando desconhecido: " + command;
    }

    private String upload(String[] parts) {
        if (parts.length != 3) {
            return "ERROR uso: UPLOAD nomeArquivo payloadBase64";
        }

        try {
            Files.write(getFilePath(parts[1]), Base64.getDecoder().decode(parts[2]));
            return "OK salvo_em=" + name;
        } catch (IllegalArgumentException error) {
            return "ERROR payload Base64 invalido";
        } catch (IOException error) {
            return "ERROR falha ao salvar em " + name + ": " + error.getMessage();
        }
    }

    private String download(String[] parts) {
        if (parts.length < 2) {
            return "ERROR uso: DOWNLOAD nomeArquivo";
        }

        Path filePath;

        try {
            filePath = getFilePath(parts[1]);
        } catch (IllegalArgumentException error) {
            return "ERROR " + error.getMessage();
        }

        if (!Files.exists(filePath)) {
            return "ERROR arquivo nao encontrado em " + name;
        }

        try {
            return "FILE_DATA " + Base64.getEncoder().encodeToString(Files.readAllBytes(filePath));
        } catch (IOException error) {
            return "ERROR falha ao ler em " + name + ": " + error.getMessage();
        }
    }

    private String listFiles() {
        StringBuilder files = new StringBuilder();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storagePath)) {
            for (Path path : stream) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }

                if (files.length() > 0) {
                    files.append(",");
                }

                files.append(path.getFileName());
            }
        } catch (IOException error) {
            return "ERROR falha ao listar em " + name + ": " + error.getMessage();
        }

        if (files.length() == 0) {
            return "FILES";
        }

        return "FILES " + files;
    }

    private Path getFilePath(String fileName) {
        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            throw new IllegalArgumentException("nome de arquivo invalido");
        }

        return storagePath.resolve(fileName);
    }

    private void writeResponse(BufferedWriter output, String response) throws IOException {
        output.write(response);
        output.newLine();
        output.flush();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Uso: java DemoStorageNode nome porta [storagePath]");
            return;
        }

        String defaultStoragePath = System.getenv().getOrDefault("STORAGE_PATH", "data/" + args[0]);
        Path storagePath = Path.of(args.length == 3 ? args[2] : defaultStoragePath);

        new DemoStorageNode(args[0], Integer.parseInt(args[1]), storagePath).start();
    }
}
