package Requests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class GatewayClient {

    private static final int DEFAULT_TIMEOUT_MS = 5000;

    private final String host;
    private final int port;
    private final int timeoutMs;

    public GatewayClient(String host, int port) {
        this(host, port, DEFAULT_TIMEOUT_MS);
    }

    public GatewayClient(String host, int port, int timeoutMs) {
        this.host = requireText(host, "host");
        this.port = requireValidPort(port);
        this.timeoutMs = requirePositiveTimeout(timeoutMs);
    }

    public void registerNode(String nodeId, String dnsName, String nodeHost, int nodePort) throws IOException {
        String response = sendCommand(
                "REGISTER_NODE "
                        + requireToken(nodeId, "nodeId")
                        + " "
                        + requireToken(dnsName, "dnsName")
                        + " "
                        + requireToken(nodeHost, "nodeHost")
                        + " "
                        + requireValidPort(nodePort)
        );

        validateSuccess(response);
    }

    public void heartbeat(String nodeId) throws IOException {
        String response = sendCommand("HEARTBEAT " + requireToken(nodeId, "nodeId"));

        validateSuccess(response);
    }

    public void uploadFile(String fileName, byte[] data) throws IOException {
        String response = sendCommand(
                "UPLOAD "
                        + requireToken(fileName, "fileName")
                        + " "
                        + Base64.getEncoder().encodeToString(requirePayload(data))
        );

        validateSuccess(response);
    }

    public byte[] downloadFile(String fileName) throws IOException {
        String response = sendCommand("DOWNLOAD " + requireToken(fileName, "fileName"));

        if (startsWithCommand(response, "FILE_DATA")) {
            String payload = commandPayload(response);

            if (payload.isEmpty()) {
                return new byte[0];
            }

            try {
                return Base64.getDecoder().decode(payload);
            } catch (IllegalArgumentException error) {
                throw new IOException("Resposta FILE_DATA possui payload invalido", error);
            }
        }

        throw new IOException("Falha no download: " + response);
    }

    public String[] listFiles() throws IOException {
        String response = sendCommand("LIST_FILES");

        if (startsWithCommand(response, "FILES")) {
            String files = commandPayload(response);

            if (files.isEmpty()) {
                return new String[0];
            }

            return files.split(",");
        }

        validateSuccess(response);
        return new String[0];
    }

    public String sendCommand(String command) throws IOException {
        String request = requireSingleLine(command, "command");

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            socket.setSoTimeout(timeoutMs);

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
                throw new IOException("Gateway fechou a conexao sem responder");
            }

            return response;
        }
    }

    private static void validateSuccess(String response) throws IOException {
        if (!startsWithCommand(response, "OK") && !startsWithCommand(response, "SUCCESS")) {
            throw new IOException("Erro no gateway: " + response);
        }
    }

    private static String commandPayload(String response) {
        int firstSpace = response.indexOf(' ');

        if (firstSpace < 0) {
            return "";
        }

        return response.substring(firstSpace + 1).trim();
    }

    private static boolean startsWithCommand(String response, String command) {
        return response.equals(command) || response.startsWith(command + " ");
    }

    private static String requireToken(String value, String fieldName) {
        String text = requireText(value, fieldName);

        if (text.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException(fieldName + " nao pode conter espacos");
        }

        return text;
    }

    private static String requireSingleLine(String value, String fieldName) {
        String text = requireText(value, fieldName);

        if (text.contains("\n") || text.contains("\r")) {
            throw new IllegalArgumentException(fieldName + " deve ter apenas uma linha");
        }

        return text;
    }

    private static String requireText(String value, String fieldName) {
        String text = Objects.requireNonNull(value, fieldName + " nao pode ser nulo").trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " nao pode ser vazio");
        }

        return text;
    }

    private static byte[] requirePayload(byte[] data) {
        return Objects.requireNonNull(data, "data nao pode ser nulo");
    }

    private static int requireValidPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port deve estar entre 1 e 65535");
        }

        return port;
    }

    private static int requirePositiveTimeout(int timeoutMs) {
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs deve ser positivo");
        }

        return timeoutMs;
    }
}
