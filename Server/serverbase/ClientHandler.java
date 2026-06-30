package Server.serverbase;

import Server.protocol.MessageType;
import Server.protocol.Packet;
import Server.protocol.PacketReader;
import Server.protocol.PacketWriter;
import Server.storage.FileStorageService;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import Common.util.ChecksumUtil;
import Server.serverbase.NodeLogger;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final FileStorageService storageService;

    public ClientHandler(Socket clientSocket, FileStorageService storageService) {
        this.clientSocket = clientSocket;
        this.storageService = storageService;
    }

    @Override
    public void run() {
        try (Socket socket = clientSocket) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            Packet packet = PacketReader.readPacket(in);

            if (MessageType.HEALTH.equals(packet.type())) {
                PacketWriter.sendSuccess(out, "healthy");
            } else if (MessageType.STORE_CHUNK.equals(packet.type())) {
                handleStoreChunk(packet, out);
            } else if (MessageType.READ_CHUNK.equals(packet.type())) {
                handleReadChunk(packet, out);
            } else {
                PacketWriter.sendError(out, "Tipo de mensagem nao suportado");
            }

        } catch (Exception e) {
            NodeLogger.error("client_handler","Erro ao processar a conexão: " + e.getMessage());
        }
    }

    private void handleStoreChunk(Packet packet, DataOutputStream out) throws Exception {
        String fileId = packet.headers().get("fileId");
        int chunkIndex = Integer.parseInt(packet.headers().get("chunkIndex"));
        String expectedChecksum = packet.headers().get("checksum");
        byte[] data = packet.payload();

        if (expectedChecksum == null) {
            NodeLogger.error("store_chunk", "Checksum ausente para fileId=" + fileId + " chunk=" + chunkIndex);
            PacketWriter.sendError(out, "Checksum ausente");
            return;
        }

        String actualChecksum = ChecksumUtil.sha256(data);
        if (!actualChecksum.equals(expectedChecksum)) {
            NodeLogger.error("store_chunk", "Checksum invalido para fileId=" + fileId + " chunk=" + chunkIndex);
            PacketWriter.sendError(out, "Checksum invalido — chunk corrompido");
            return;
        }

        // se ja existe ent não reescreve, retorna sucesso
        if (storageService.chunkExists(fileId, chunkIndex)) {
            NodeLogger.info("store_chunk", "Chunk já existente, reescrita ignorada: fileId=" + fileId + " chunk=" + chunkIndex);
            PacketWriter.sendSuccess(out, "Chunk ja existe");
            return;
        }

        storageService.saveChunk(fileId, chunkIndex, data);
        NodeLogger.info("store_chunk", "Chunk salvo: fileId=" + fileId + " chunk=" + chunkIndex);
        PacketWriter.sendSuccess(out, "Chunk salvo");
    }

    private void handleReadChunk(Packet packet, DataOutputStream out) throws Exception {
        String fileId = packet.headers().get("fileId");
        int chunkIndex = Integer.parseInt(packet.headers().get("chunkIndex"));
        try {
            byte[] data = storageService.readChunk(fileId, chunkIndex);
            NodeLogger.info("read_chunk", "Chunk lido: fileId=" + fileId + " chunk=" + chunkIndex);
            PacketWriter.sendFile(out, data);
        } catch (Exception e) {
            NodeLogger.error("read_chunk", "Falha ao ler chunk fileId=" + fileId + " chunk=" + chunkIndex + " - " + e.getMessage());
            throw e;
        }
    }
}
