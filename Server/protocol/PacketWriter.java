package Server.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
// Classe que escreve os dados dos pacotes enviados para os clientes
public class PacketWriter {

    public static void sendSuccess(
            DataOutputStream out,            // Envia uma mensagem de sucesso para o cliente
            String message
    ) throws IOException {

        out.writeUTF(
                MessageType.SUCCESS            // Escreve o tipo da mensagem (SUCCESS)
        );

        out.writeUTF(message);                 // Escreve a mensagem de sucesso

        out.flush();
    }

    public static void sendError(
            DataOutputStream out,            // Envia uma mensagem de erro para o cliente       
            String message
    ) throws IOException {

        out.writeUTF(
                MessageType.ERROR
        );

        out.writeUTF(message);

        out.flush();
    }

    public static void sendFile(
            DataOutputStream out,               // Envia os dados de um arquivo para o cliente
            byte[] data
    ) throws IOException {

        out.writeInt(data.length);

        out.write(data);

        out.flush();
    }
}