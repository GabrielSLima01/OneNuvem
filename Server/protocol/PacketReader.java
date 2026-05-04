package Server.protocol;

import java.io.DataInputStream;
import java.io.IOException;
// Classe que lê os dados dos pacotes recebidos dos clientes
public class PacketReader {

    public static String readMessageType(
            DataInputStream in          // Lê o tipo da mensagem (UPLOAD, DOWNLOAD, etc.)
    ) throws IOException {

        return in.readUTF();
    }

    public static String readFileName(
            DataInputStream in        // Lê o nome do arquivo para upload/download
    ) throws IOException {

        return in.readUTF();
    }

    public static byte[] readFileData(
            DataInputStream in              // Lê os dados do arquivo enviado pelo cliente
    ) throws IOException {

        int size =
                in.readInt();           // Lê o tamanho dos dados do arquivo

        byte[] data =
                new byte[size];              // Cria um array de bytes para armazenar os dados do arquivo

        in.readFully(data);             // Lê os dados do arquivo e armazena no array

        return data;
    }
}