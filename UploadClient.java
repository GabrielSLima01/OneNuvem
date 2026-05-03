import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public class UploadClient {

    public static void main(String[] args) {

        try {

            Socket socket =
                    new Socket(
                            "localhost",
                            8080
                    );

            System.out.println(
                    "Conectado ao servidor"
            );

            DataOutputStream out =
                    new DataOutputStream(
                            socket.getOutputStream()
                    );

            DataInputStream in =
                    new DataInputStream(
                            socket.getInputStream()
                    );

            File file =
                    new File(
                        "C:\\Users\\deyvi\\Desktop\\gatinho.png"
                    );

            FileInputStream fis =
                    new FileInputStream(file);

            byte[] data =
                    new byte[(int) file.length()];

            fis.read(data);

            out.writeUTF("UPLOAD");

            out.writeUTF(file.getName());

            out.writeInt(data.length);

            out.write(data);

            out.flush();

            String responseType =
                    in.readUTF();

            String responseMessage =
                    in.readUTF();

            System.out.println(
                    responseType
            );

            System.out.println(
                    responseMessage
            );

            fis.close();

            socket.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}