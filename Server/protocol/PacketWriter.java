package Server.protocol;

import java.io.DataOutputStream;
import java.io.IOException;


//Classe responsável por escrever os dados a serem enviados para o cliente, como mensagens de sucesso, erro e arquivos.
public class PacketWriter {

    public static void sendSuccess(
            DataOutputStream out,
            String message
    ) throws IOException {

        out.writeUTF(
                MessageType.SUCCESS
        );

        out.writeUTF(message);

        out.flush();
    }

    public static void sendError(
            DataOutputStream out,
            String message
    ) throws IOException {

        out.writeUTF(
                MessageType.ERROR
        );

        out.writeUTF(message);

        out.flush();
    }

    public static void sendFile(
            DataOutputStream out,
            byte[] data
    ) throws IOException {

        out.writeInt(data.length);

        out.write(data);

        out.flush();
    }
}