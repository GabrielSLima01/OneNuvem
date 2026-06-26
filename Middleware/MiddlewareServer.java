package Middleware;

import Common.config.AppConfig;
import Common.db.DatabaseInitializer;
import Common.http.HttpParser;
import Common.http.HttpRequest;
import Common.http.HttpResponse;
import Services.NodeSocketService;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MiddlewareServer {

    private final int port;
    private final Middleware middleware;
    private final ExecutorService executorService;

    public MiddlewareServer(int port, Middleware middleware) {
        this.port = port;
        this.middleware = middleware;
        this.executorService = Executors.newFixedThreadPool(AppConfig.workerThreads());
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Gateway HTTP escutando na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClient(clientSocket));
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (Socket socket = clientSocket;
             BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
             OutputStream output = socket.getOutputStream()) {
            HttpRequest request = HttpParser.parse(input);
            HttpResponse response = middleware.handle(request);
            output.write(response.toBytes());
            output.flush();
        } catch (Exception exception) {
            try (Socket socket = clientSocket; OutputStream output = socket.getOutputStream()) {
                output.write(HttpResponse.text(500, "Erro interno").toBytes());
                output.flush();
            } catch (IOException ignored) {
            }
        }
    }

    public static void main(String[] args) throws IOException {
        DatabaseInitializer.initialize();
        NodeManager nodeManager = new NodeManager();
        NodeSocketService nodeSocketService = new NodeSocketService();
        ScheduledExecutorService healthScheduler = Executors.newSingleThreadScheduledExecutor();
        healthScheduler.scheduleAtFixedRate(
                () -> nodeManager.refreshHealth(nodeSocketService),
                0,
                5,
                TimeUnit.SECONDS
        );

        Middleware middleware = new Middleware(nodeManager);
        MiddlewareServer server = new MiddlewareServer(AppConfig.gatewayPort(), middleware);
        server.start();
    }
}
