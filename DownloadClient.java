import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;

public class DownloadClient {

    public static void main(String[] args) {

        try {

            Socket socket =
                    new Socket(
                            "localhost",
                            8080
                    );

            DataOutputStream out =
                    new DataOutputStream(
                            socket.getOutputStream()
                    );

            DataInputStream in =
                    new DataInputStream(
                            socket.getInputStream()
                    );

            out.writeUTF("DOWNLOAD");

            out.writeUTF("gatinho.png");

            out.writeInt(2);

            out.flush();

            int size =
                    in.readInt();

            byte[] data =
                    new byte[size];

            in.readFully(data);

            FileOutputStream fos =
                    new FileOutputStream(
                            "arquivo_recuperado.png"
                    );

            fos.write(data);

            fos.close();

            System.out.println(
                    "Arquivo reconstruído!"
            );

            socket.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}