package Server.serverbase;

import Server.storage.FileStorageService;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(ServerConfig.PORT)) {

            FileStorageService storageService = new FileStorageService(ServerConfig.STORAGE_PATH);
            ExecutorService executorService = Executors.newFixedThreadPool(Math.max(8, Runtime.getRuntime().availableProcessors() * 2));
            System.out.println("No de armazenamento iniciado na porta " + ServerConfig.PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(new ClientHandler(clientSocket, storageService));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
