package Server.serverbase;

import Server.protocol.MessageType;
import Server.protocol.PacketReader;
import Server.protocol.PacketWriter;
import Server.storage.FileStorageService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;


// Classe que lida com a comunicação com um cliente
public class ClientHandler
        implements Runnable {

    private final Socket clientSocket;

    private final FileStorageService
            storageService;

    public ClientHandler(
            Socket clientSocket,
            FileStorageService storageService           // Serviço de armazenamento de arquivos para salvar/ler os arquivos enviados/solicitados pelos clientes
    ) {

        this.clientSocket =
                clientSocket;                   // Armazena o socket do cliente para comunicação

        this.storageService =
                storageService;                 // Armazena o serviço de armazenamento para uso nas operações de upload/download
    }

    @Override
    public void run() {

        try {

            DataInputStream in =
                    new DataInputStream(                // Cria um DataInputStream para ler os dados enviados pelo cliente
                            clientSocket                        
                                    .getInputStream()
                    );

            DataOutputStream out =
                    new DataOutputStream(               // Cria um DataOutputStream para enviar os dados de volta para o cliente
                            clientSocket
                                    .getOutputStream()
                    );

            String type =
                    PacketReader                        //Lê o tipo da mensagem enviada pelo cliente (UPLOAD, DOWNLOAD, etc.)
                            .readMessageType(in);

            if (
                    MessageType.UPLOAD                  // Verifica se o tipo da mensagem é UPLOAD    
                            .equals(type)
            ) {

                handleUpload(
                        in,                            // Lida com a operação de upload, lendo o nome do arquivo e os dados enviados pelo cliente, salvando o arquivo usando o serviço de armazenamento e enviando uma resposta de sucesso para o cliente       
                        out
                );

            } else if (
                    MessageType.DOWNLOAD
                            .equals(type)               // Verifica se o tipo da mensagem é DOWNLOAD
            ) {

                handleDownload(
                        in,                             // Lida com a operação de download, lendo o nome do arquivo solicitado pelo cliente, lendo os dados do arquivo usando o serviço de armazenamento e enviando os dados de volta para o cliente
                        out
                );
            }

            clientSocket.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void handleUpload(
            DataInputStream in,                 // Lida com a operação de upload, lendo o nome do arquivo e os dados enviados pelo cliente, salvando o arquivo usando o serviço de armazenamento e enviando uma resposta de sucesso para o cliente
            DataOutputStream out
    ) throws Exception {

        String fileName =
                PacketReader                    // Lê o nome do arquivo enviado pelo cliente
                        .readFileName(in);

        byte[] data =
                PacketReader                    // Lê os dados do arquivo enviado pelo cliente
                        .readFileData(in);

        storageService.saveFile(
                fileName,                       // Salva o arquivo usando o serviço de armazenamento, passando o nome do arquivo e os dados do arquivo
                data
        );

        System.out.println(
                "Upload recebido: "
                        + fileName
        );

        PacketWriter.sendSuccess(
                out,                            // Envia uma resposta de sucesso para o cliente, indicando que o arquivo foi salvo com sucesso
                "Arquivo salvo"
        );
    }

    private void handleDownload(
            DataInputStream in,                 // Lida com a operação de download, lendo o nome do arquivo solicitado pelo cliente, lendo os dados do arquivo usando o serviço de armazenamento e enviando os dados de volta para o cliente
            DataOutputStream out
    ) throws Exception {

        String fileName =
                PacketReader                    // Lê o nome do arquivo solicitado pelo cliente
                        .readFileName(in);

        byte[] data =
                storageService.readFile(         // Lê os dados do arquivo usando o serviço de armazenamento, passando o nome do arquivo solicitado pelo cliente
                        fileName
                );

        PacketWriter.sendFile(
                out,                            // Envia os dados do arquivo de volta para o cliente usando o PacketWriter, passando o DataOutputStream e os dados do arquivo lidos do serviço de armazenamento
                data
        );

        System.out.println(
                "Download enviado: "
                        + fileName
        );
    }
}