package Server.storage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStorageService {

    private final String storagePath;

    public FileStorageService(String storagePath) {
        this.storagePath = storagePath;
        File directory = new File(storagePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void saveChunk(String fileId, int chunkIndex, byte[] data) throws IOException {
        Path path = chunkPath(fileId, chunkIndex);
        Files.createDirectories(path.getParent());
        Files.write(path, data);
    }

    public byte[] readChunk(String fileId, int chunkIndex) throws IOException {
        return Files.readAllBytes(chunkPath(fileId, chunkIndex));
    }

    private Path chunkPath(String fileId, int chunkIndex) {
        return Path.of(storagePath, fileId, chunkIndex + ".chk");
    }
}
