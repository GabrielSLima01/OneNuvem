package requests;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class GatewayClient {

    private final String host;
    private final int port;

    public GatewayClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private PacketReader.Packet send(MessageType type, Map<String, String> headers, byte[] payload)
            throws IOException {

        try (Socket socket = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            PacketWriter.writePacket(out, type, headers, payload);

            return PacketReader.readPacket(in);
        }
    }

    public void registerNode(String nodeId, String dns, String host, int port) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(ProtocolConstants.HEADER_NODE_ID, nodeId);
        headers.put(ProtocolConstants.HEADER_DNS_NAME, dns);
        headers.put(ProtocolConstants.HEADER_HOST, host);
        headers.put(ProtocolConstants.HEADER_PORT, String.valueOf(port));

        PacketReader.Packet response = send(MessageType.REGISTER_NODE, headers, new byte[0]);

        validateSuccess(response);
    }

    public void heartbeat(String nodeId) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(ProtocolConstants.HEADER_NODE_ID, nodeId);

        PacketReader.Packet response = send(MessageType.HEARTBEAT, headers, new byte[0]);

        validateSuccess(response);
    }

    public void uploadFile(String fileName, byte[] data) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(ProtocolConstants.HEADER_FILE_NAME, fileName);

        PacketReader.Packet response = send(MessageType.UPLOAD_FILE, headers, data);

        validateSuccess(response);

        String checksum = response.getHeaders().get(ProtocolConstants.HEADER_CHECKSUM);
        String nodeId = response.getHeaders().get(ProtocolConstants.HEADER_NODE_ID);

        System.out.println("Upload OK");
        System.out.println("Checksum: " + checksum);
        System.out.println("Node: " + nodeId);
    }

    public byte[] downloadFile(String fileName) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(ProtocolConstants.HEADER_FILE_NAME, fileName);

        PacketReader.Packet response = send(MessageType.DOWNLOAD_FILE, headers, new byte[0]);

        if (response.getType() != MessageType.FILE_DATA) {
            throw new IOException("Falha no download: " + response.getHeaders());
        }

        return response.getPayload();
    }

    public String[] listFiles() throws IOException {
        PacketReader.Packet response = send(MessageType.LIST_FILES, new HashMap<>(), new byte[0]);

        validateSuccess(response);

        String files = response.getHeaders().get(ProtocolConstants.HEADER_FILES);

        if (files == null || files.isEmpty()) {
            return new String[0];
        }

        return files.split(",");
    }

    private void validateSuccess(PacketReader.Packet response) throws IOException {
        if (response.getType() != MessageType.SUCCESS) {
            throw new IOException("Erro: " + response.getHeaders());
        }
    }
}