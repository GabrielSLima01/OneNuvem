package Server.serverbase;
import Server.protocol.MessageType;
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

            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            String type = PacketReader.readMessageType(in);

            if (MessageType.UPLOAD.equals(type)) {

                handleUpload(in,out);

            } else if (MessageType.DOWNLOAD.equals(type)) {

                handleDownload(in, out);
            }
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpload(DataInputStream in, DataOutputStream out) throws Exception {

        String fileName =PacketReader.readFileName(in);
        byte[] data =PacketReader.readFileData(in);

        storageService.saveFile(fileName,data);

        System.out.println("Upload recebido: " + fileName);
        PacketWriter.sendSuccess(out,"Arquivo salvo");
    }

    private void handleDownload(DataInputStream in, DataOutputStream out) throws Exception {

        String fileName = PacketReader.readFileName(in);
        byte[] data = storageService.readFile(fileName);

        PacketWriter.sendFile(out, data);

        System.out.println("Download enviado: " + fileName);
    }
}