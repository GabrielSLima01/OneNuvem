package Server.serverbase;

import Server.protocol.MessageType;
import Server.protocol.PacketReader;
import Server.protocol.PacketWriter;
import Server.queue.UploadQueue;
import Server.queue.UploadTask;
import Server.storage.ChunkRebuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;


//Classe que lida com a comunicação com o cliente. Ela é responsável por ler as mensagens enviadas pelo cliente, processar as solicitações de upload e download e enviar as respostas de volta para o cliente.
public class ClientHandler
        implements Runnable {

    private final Socket clientSocket;

    private final UploadQueue uploadQueue;

    public ClientHandler(
            Socket clientSocket,
            UploadQueue uploadQueue
    ) {

        this.clientSocket =
                clientSocket;

        this.uploadQueue =
                uploadQueue;
    }

    @Override
    public void run() {

        try {

            DataInputStream in =
                    new DataInputStream(
                            clientSocket                        
                                    .getInputStream()               //Criação dos fluxos de entrada e saída para comunicação com o cliente
                    );

            DataOutputStream out =
                    new DataOutputStream(
                            clientSocket
                                    .getOutputStream()               
                    );

            String type =
                    PacketReader
                            .readMessageType(in);

            if (
                    MessageType.UPLOAD
                            .equals(type)               //Verifica o tipo da mensagem recebida. Se for um upload, chama o método handleUpload para processar a solicitação de upload.
            ) {

                handleUpload(
                        in,
                        out
                );

            } else if (
                    MessageType.DOWNLOAD
                            .equals(type)               //Se for um download, chama o método handleDownload para processar a solicitação de download.
            ) {

                handleDownload(
                        in,
                        out
                );
            }

            clientSocket.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void handleUpload(
            DataInputStream in,
            DataOutputStream out
    ) throws Exception {

        String fileName =
                PacketReader
                        .readFileName(in);

        byte[] data =
                PacketReader                    //Lê o nome do arquivo e os dados do arquivo enviados pelo cliente usando o PacketReader.
                        .readFileData(in);

        UploadTask task =
                new UploadTask(
                        fileName,    //Cria uma nova tarefa de upload com o nome do arquivo, os dados do arquivo e o socket do cliente que enviou a tarefa.
                        data,
                        clientSocket
                );

        uploadQueue.addTask(task);              //Adiciona a tarefa de upload na fila de upload compartilhada para ser processada pelo UploadConsumer.

        System.out.println(
                "Upload recebido: "
                        + fileName
        );

        PacketWriter.sendSuccess(
                out,
                "Arquivo enviado para fila"
        );
    }

    private void handleDownload(
            DataInputStream in,
            DataOutputStream out
    ) throws Exception {

        String fileName =
                PacketReader
                        .readFileName(in);

        int totalChunks =
                PacketReader
                        .readChunkCount(in);            //Lê o nome do arquivo e o número total de chunks do arquivo que o cliente deseja baixar usando o PacketReader.

        ChunkRebuilder rebuilder =
                new ChunkRebuilder();                   //Cria um novo rebuilder de chunks para reconstruir o arquivo a partir dos chunks armazenados.

        byte[] fileData =
                rebuilder.rebuildFile(
                        fileName,
                        totalChunks             //Reconstroi o arquivo a partir dos chunks armazenados usando o rebuilder de chunks.
                );

        PacketWriter.sendFile(
                out,
                fileData                //Envia os dados do arquivo reconstruído de volta para o cliente usando o PacketWriter.
        );

        System.out.println(
                "Download enviado: "
                        + fileName
        );
    }
}