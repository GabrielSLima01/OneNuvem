package Server.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


//Responsável por gerenciar o armazenamento dos arquivos enviados pelos clientes. Ele cria um diretório para armazenar os arquivos e fornece um método para salvar os arquivos recebidos.
public class FileStorageService {

    private final String storagePath;

    public FileStorageService(String storagePath) {

        this.storagePath = storagePath;

        createDirectory();
    }

    private void createDirectory() {

        File directory = new File(storagePath);

        if (!directory.exists()) {      //Verifica se o diretório de armazenamento existe, e se não existir, ele é criado.
            directory.mkdirs();
        }
    }

    public void saveFile(String fileName,
                         byte[] data)
            throws IOException {

        File file =                                         //Cria um arquivo com o nome recebido e o caminho do diretório de armazenamento.
                new File(storagePath + fileName);

        try (FileOutputStream fos =
                     new FileOutputStream(file)) {          //Salva o arquivo no diretório de armazenamento usando um FileOutputStream.

            fos.write(data);
        }
    }
}