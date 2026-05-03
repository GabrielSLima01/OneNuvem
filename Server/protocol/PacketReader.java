package Server.protocol;

import java.io.DataInputStream;
import java.io.IOException;


//Classe responsável por ler os dados enviados pelos clientes, como o tipo da mensagem, nome do arquivo e os dados do arquivo.
public class PacketReader {

    public static String readMessageType(
            DataInputStream in
    ) throws IOException {

        return in.readUTF();
    }

    public static String readFileName(
            DataInputStream in
    ) throws IOException {

        return in.readUTF();
    }

    public static byte[] readFileData(
            DataInputStream in
    ) throws IOException {

        int size = in.readInt();

        byte[] data =
                new byte[size];

        in.readFully(data);

        return data;
    }

    public static int readChunkCount(
            DataInputStream in
    ) throws IOException {

        return in.readInt();
    }
}