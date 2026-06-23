package Server.serverbase;

import Server.storage.FileStorageService;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(ServerConfig.PORT)) {

            FileStorageService storageService = new FileStorageService(ServerConfig.STORAGE_PATH);
            System.out.println("Servidor iniciado na porta " + ServerConfig.PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado");
                Thread thread = new Thread(new ClientHandler(clientSocket, storageService));
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}