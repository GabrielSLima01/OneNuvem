import java.io.*;
import java.net.Socket;

public class ClientDownload {

    public static void main(String[] args) {

        try {

            Socket socket = new Socket("localhost", 8080);

            DataOutputStream out =
                    new DataOutputStream(socket.getOutputStream());

            DataInputStream in =
                    new DataInputStream(socket.getInputStream());

            // 1. tipo da requisição
            out.writeUTF("DOWNLOAD");

            // 2. nome do arquivo
            out.writeUTF("teste.txt");

            out.flush();

            // 3. resposta: tamanho + dados
            int size = in.readInt();

            byte[] data = new byte[size];
            in.readFully(data);

            System.out.println("Arquivo recebido:");
            System.out.println(new String(data));

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}