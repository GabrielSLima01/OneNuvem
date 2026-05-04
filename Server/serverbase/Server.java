package Server.serverbase;

import Server.storage.FileStorageService;
import java.net.ServerSocket;
import java.net.Socket;

// Classe principal do servidor que inicia o servidor, aceita conexões de clientes e cria threads para lidar com cada cliente
public class Server {

    public static void main(
            String[] args
    ) {

        try {

            FileStorageService
                    storageService =
                    new FileStorageService(             // Cria uma instância do serviço de armazenamento de arquivos, passando o caminho de armazenamento definido na configuração do servidor
                            ServerConfig
                                    .STORAGE_PATH
                    );

            ServerSocket serverSocket =
                    new ServerSocket(                   // Cria um ServerSocket para ouvir na porta definida na configuração do servidor
                            ServerConfig.PORT
                    );

            System.out.println(
                    "Servidor iniciado na porta "
                            + ServerConfig.PORT
            );

            while (true) {

                Socket clientSocket =
                        serverSocket.accept();          // Aceita uma conexão de cliente e retorna um Socket para comunicação com o cliente

                System.out.println(
                        "Cliente conectado"
                );

                Thread thread =
                        new Thread(
                                new ClientHandler(
                                        clientSocket,           // Cria uma nova thread para lidar com a comunicação com o cliente, passando o Socket do cliente e o serviço de armazenamento de arquivos
                                        storageService
                                )
                        );

                thread.start();
            }

            

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}