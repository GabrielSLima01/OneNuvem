package Server.storage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileStorageService {

    private final String storagePath;

    public FileStorageService(
            String storagePath
    ) {

        this.storagePath =
                storagePath;

        File directory =
                new File(storagePath);

        if (!directory.exists()) {

            directory.mkdirs();
        }
    }

    public void saveFile(
            String fileName,
            byte[] data
    ) throws IOException {

        String filePath =
                storagePath
                        + "/"
                        + fileName;

        FileOutputStream fos =
                new FileOutputStream(
                        filePath
                );

        fos.write(data);

        fos.close();

        System.out.println(
                "Arquivo salvo: "
                        + fileName
        );
    }

    public byte[] readFile(
            String fileName
    ) throws IOException {

        File file =
                new File(
                        storagePath
                                + "/"
                                + fileName
                );

        return java.nio.file.Files
                .readAllBytes(
                        file.toPath()
                );
    }
}