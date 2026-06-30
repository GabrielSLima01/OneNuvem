package Server.serverbase;

import Server.storage.FileStorageService;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(ServerConfig.PORT)) {

            FileStorageService storageService = new FileStorageService(ServerConfig.STORAGE_PATH);
            NodeLogger.info("server_start", "Nó iniciado na porta: " + ServerConfig.PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(clientSocket, storageService));
                thread.start();
            }

        } catch (Exception e) {
            NodeLogger.error("Server_fatal", "Falha ao iniciar o servidor: " + e.getMessage());
        }
    }
}
