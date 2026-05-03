package Server.storage;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

//Classe responsável por reconstruir um arquivo a partir dos chunks armazenados no servidor.
public class ChunkRebuilder {

    public byte[] rebuildFile(
            String fileName,        //Reconstroi um arquivo a partir dos chunks armazenados no servidor. Ele recebe o nome do arquivo e o número total de chunks, e então lê cada chunk do diretório de armazenamento, concatenando os dados em um ByteArrayOutputStream para criar o arquivo completo.
            int totalChunks
    ) throws IOException {

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();        //Usa um ByteArrayOutputStream para armazenar os dados do arquivo reconstruído, permitindo que os dados sejam escritos em um array de bytes de forma eficiente.

        for (
            int i = 0;
            i < totalChunks;        //Percorre o número total de chunks, lendo cada chunk do diretório de armazenamento usando um FileInputStream e escrevendo os dados no ByteArrayOutputStream.
            i++
        ) {

            String chunkPath =
                    "Server/storage/chunks/"
                    + fileName
                    + ".part"       //Constrói o caminho do chunk atual com base no nome do arquivo e no índice do chunk, seguindo o formato "Server/storage/chunks/{fileName}.part{i}".
                    + i;

            FileInputStream fis =
                    new FileInputStream(
                            chunkPath
                    );      //Lê o chunk atual do diretório de armazenamento usando um FileInputStream, e escreve os dados do chunk no ByteArrayOutputStream para reconstruir o arquivo completo.

            byte[] buffer =
                    fis.readAllBytes();
                        //Lê todos os bytes do chunk atual usando readAllBytes, e escreve os dados no ByteArrayOutputStream usando o método write.
            output.write(buffer);
                        //Escreve os dados do chunk no ByteArrayOutputStream usando o método write, e depois fecha o FileInputStream para liberar os recursos associados ao arquivo.
            fis.close();
        }

        return output.toByteArray();
    }
}