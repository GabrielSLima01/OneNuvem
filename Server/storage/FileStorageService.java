package Server.storage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// Classe que lida com o armazenamento de arquivos no servidor, permitindo salvar e ler arquivos do sistema de arquivos local
public class FileStorageService {

    private final String storagePath;

    public FileStorageService(
            String storagePath          // Construtor que recebe o caminho de armazenamento dos arquivos e cria o diretório se ele não existir
    ) {

        this.storagePath =
                storagePath;            // Armazena o caminho de armazenamento para uso nas operações de salvar/ler arquivos

        File directory =
                new File(storagePath);          // Cria um objeto File para o diretório de armazenamento

        if (!directory.exists()) {

            directory.mkdirs();
        }
    }

    public void saveFile(
            String fileName,            // Salva um arquivo no sistema de arquivos local, recebendo o nome do arquivo e os dados do arquivo em um array de bytes
            byte[] data
    ) throws IOException {

        String filePath =
                storagePath
                        + "/"
                        + fileName;             // Constrói o caminho completo do arquivo a ser salvo, combinando o caminho de armazenamento e o nome do arquivo

        FileOutputStream fos =
                new FileOutputStream(
                        filePath                // Cria um FileOutputStream para escrever os dados do arquivo no caminho especificado
                );

        fos.write(data);

        fos.close();

        System.out.println(
                "Arquivo salvo: "
                        + fileName
        );
    }

    public byte[] readFile(
            String fileName             // Lê um arquivo do sistema de arquivos local, recebendo o nome do arquivo e retornando os dados do arquivo em um array de bytes
    ) throws IOException {

        File file =
                new File(
                        storagePath
                                + "/"
                                + fileName
                );

        return java.nio.file.Files
                .readAllBytes(          // Lê os dados do arquivo usando a classe Files do pacote java.nio.file, passando o caminho completo do arquivo construído a partir do caminho de armazenamento e o nome do arquivo
                        file.toPath()
                );
    }
}