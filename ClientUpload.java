import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class ClientUpload {

    public static void main(String[] args) {

        try {

            Socket socket = new Socket("localhost", 8080);

            DataOutputStream out =
                    new DataOutputStream(socket.getOutputStream());

            DataInputStream in =
                    new DataInputStream(socket.getInputStream());

            // 1. tipo da requisição
            out.writeUTF("UPLOAD");

            // 2. nome do arquivo
            String fileName = "teste.txt";
            out.writeUTF(fileName);

            // 3. conteúdo do arquivo
            File file = new File(fileName);
            byte[] data = Files.readAllBytes(file.toPath());

            out.writeInt(data.length);
            out.write(data);

            out.flush();

            // resposta do servidor
            String responseType = in.readUTF();
            String message = in.readUTF();

            System.out.println(responseType + " - " + message);

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}