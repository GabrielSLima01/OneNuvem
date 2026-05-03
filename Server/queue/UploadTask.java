package Server.queue;
import java.net.Socket;

//Classe que representa uma tarefa de upload, contendo o nome do arquivo, os dados do arquivo e o socket do cliente que enviou a tarefa.
public class UploadTask {

    private final String fileName;

    private final byte[] data;

    private final Socket clientSocket;

    public UploadTask(String fileName,
                      byte[] data,
                      Socket clientSocket) {

        this.fileName = fileName;
        this.data = data;
        this.clientSocket = clientSocket;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}