package Services;

import DTOs.FileChunkDTO;
import Middleware.Node;
import Server.protocol.MessageType;
import Server.protocol.Packet;
import Server.protocol.PacketReader;
import Server.protocol.PacketWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class NodeSocketService {

    public void storeChunk(Node node, String fileId, int chunkIndex, String checksum, byte[] bytes) {
        Map<String, String> headers = new HashMap<>();
        headers.put("fileId", fileId);
        headers.put("chunkIndex", String.valueOf(chunkIndex));
        headers.put("checksum", checksum);
        sendExpectSuccess(node, MessageType.STORE_CHUNK, headers, bytes);
    }

    public byte[] readChunk(Node node, String fileId, int chunkIndex) {
        Map<String, String> headers = new HashMap<>();
        headers.put("fileId", fileId);
        headers.put("chunkIndex", String.valueOf(chunkIndex));
        Packet response = send(node, MessageType.READ_CHUNK, headers, new byte[0]);
        if (!MessageType.FILE_DATA.equals(response.type())) {
            throw new IllegalStateException("Falha ao ler chunk do no " + node.name());
        }
        return response.payload();
    }

    public boolean ping(Node node) {
        try {
            Packet response = send(node, MessageType.HEALTH, new HashMap<>(), new byte[0]);
            return MessageType.SUCCESS.equals(response.type());
        } catch (Exception exception) {
            return false;
        }
    }

    private void sendExpectSuccess(Node node, String type, Map<String, String> headers, byte[] payload) {
        Packet response = send(node, type, headers, payload);
        if (!MessageType.SUCCESS.equals(response.type())) {
            throw new IllegalStateException("Falha no no " + node.name());
        }
    }

    private Packet send(Node node, String type, Map<String, String> headers, byte[] payload) {
        try (Socket socket = new Socket(node.host(), node.port());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {
            PacketWriter.writePacket(output, type, headers, payload);
            return PacketReader.readPacket(input);
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao comunicar com no " + node.name(), exception);
        }
    }
}
