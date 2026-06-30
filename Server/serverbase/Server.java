package Server.serverbase;

import Server.storage.FileStorageService;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(ServerConfig.WORKER_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(ServerConfig.PORT)) {

            FileStorageService storageService = new FileStorageService(ServerConfig.STORAGE_PATH);

            NodeLogger.info("server_start", "Nó iniciado na porta " + ServerConfig.PORT + " com " + ServerConfig.WORKER_THREADS + " threads");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket, storageService));
            }

        } catch (Exception e) {
            NodeLogger.error("server_fatal", "Falha ao iniciar o servidor: " + e.getMessage());
        } finally {
            threadPool.shutdown(); // libera as threads ao encerrar
        }
    }
}
