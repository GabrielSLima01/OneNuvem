package Server.serverbase;

import Server.queue.UploadConsumer;
import Server.queue.UploadQueue;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(
            String[] args
    ) {

        try {

            UploadQueue uploadQueue =
                    new UploadQueue();          //Cria uma nova fila de upload compartilhada para armazenar as tarefas de upload recebidas dos clientes.

            for (                              
                    int i = 0;
                    i <
                    ServerConfig                
                            .CONSUMER_THREADS; 
                    i++
            ) {                                 //Inicia um número configurável de threads de consumidores para processar as tarefas de upload na fila de upload compartilhada.

                Thread consumer =
                        new Thread(
                                new UploadConsumer(
                                        uploadQueue     //Cria um pool de threads de consumidores para processar as tarefas de upload na fila de upload compartilhada.
                                )
                        );

                consumer.start();
            }

            ServerSocket serverSocket =
                    new ServerSocket(                   //Cria um socket de servidor para ouvir as conexões dos clientes na porta configurada.
                            ServerConfig.PORT
                    );

            System.out.println(
                    "Servidor iniciado na porta "
                            + ServerConfig.PORT         
            );

            while (true) {

                Socket clientSocket =
                        serverSocket.accept();
                                                        //Aguarda por conexões de clientes. Quando um cliente se conecta, aceita a conexão e cria um novo socket para se comunicar com o cliente.
                System.out.println(
                        "Cliente conectado"
                );

                Thread thread =
                        new Thread(
                                new ClientHandler(
                                        clientSocket,
                                        uploadQueue             //Cria uma nova thread para lidar com a conexão do cliente usando o ClientHandler, passando o socket do cliente e a fila de upload compartilhada.
                                )
                        );

                thread.start();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}