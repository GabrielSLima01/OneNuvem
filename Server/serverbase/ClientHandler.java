package Server.serverbase;
import Server.protocol.MessageType;
import Server.protocol.Packet;
import Server.protocol.PacketReader;
import Server.protocol.PacketWriter;
import Server.storage.FileStorageService;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final FileStorageService storageService;

    public ClientHandler(Socket clientSocket, FileStorageService storageService) {

        this.clientSocket = clientSocket;
        this.storageService = storageService;
    }

    @Override
    public void run() {

        try(Socket socket = clientSocket) {

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            Packet packet = PacketReader.readPacket(in);

            if (MessageType.STORE_CHUNK.equals(packet.type())) {
                handleStoreChunk(packet, out);
            } else if (MessageType.READ_CHUNK.equals(packet.type())) {
                handleReadChunk(packet, out);
            } else if (MessageType.HEALTH.equals(packet.type())) {
                PacketWriter.sendSuccess(out, "healthy");
            } else {
                PacketWriter.sendError(out, "Tipo de mensagem nao suportado");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleStoreChunk(Packet packet, DataOutputStream out) throws Exception {
        String fileId = packet.headers().get("fileId");
        int chunkIndex = Integer.parseInt(packet.headers().get("chunkIndex"));
        storageService.saveChunk(fileId, chunkIndex, packet.payload());
        PacketWriter.sendSuccess(out, "Chunk salvo");
    }

    private void handleReadChunk(Packet packet, DataOutputStream out) throws Exception {
        String fileId = packet.headers().get("fileId");
        int chunkIndex = Integer.parseInt(packet.headers().get("chunkIndex"));
        byte[] data = storageService.readChunk(fileId, chunkIndex);
        PacketWriter.sendFile(out, data);
    }
}
