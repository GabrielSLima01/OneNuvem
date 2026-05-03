package Server.storage;

import java.io.FileOutputStream;
import java.io.IOException;


/// Serviço responsável por dividir arquivos em chunks e armazená-los no servidor.
public class ChunkService {

    private static final int CHUNK_SIZE =
            65536;

    public void splitAndStore(
            String fileName,
            byte[] data             //Divide um arquivo em chunks e os armazena no servidor. Ele percorre os dados do arquivo em blocos do tamanho definido por CHUNK_SIZE, criando chunks e salvando-os usando o método saveChunk.
    ) throws IOException {

        int chunkIndex = 0;

        for (
            int i = 0;
            i < data.length;        //Percorre os dados do arquivo em blocos do tamanho definido por CHUNK_SIZE, criando chunks e salvando-os usando o método saveChunk.
            i += CHUNK_SIZE
        ) {

            int length =
                    Math.min(
                            CHUNK_SIZE,
                            data.length - i     //Calcula o tamanho do chunk atual, que pode ser menor que CHUNK_SIZE para o último chunk.
                    );

            byte[] chunk =
                    new byte[length];       //Cria um array de bytes para armazenar o chunk atual.

            System.arraycopy(
                    data,
                    i,
                    chunk,              //Copia os dados do arquivo para o array de bytes do chunk usando System.arraycopy.
                    0,
                    length
            );

            saveChunk(
                    fileName,
                    chunkIndex,     //Salva o chunk atual usando o método saveChunk, passando o nome do arquivo, o índice do chunk e os dados do
                    chunk
            );

            chunkIndex++;
        }

        System.out.println(
                "Arquivo dividido em "
                + chunkIndex
                + " chunks"
        );
    }

    private void saveChunk(
            String fileName,
            int chunkIndex,     //Salva um chunk no servidor, criando um arquivo para o chunk com um nome baseado no nome do arquivo original e no índice do chunk.
            byte[] chunk
    ) throws IOException {

        String chunkPath =
                "Server/storage/chunks/"
                + fileName          //O nome do arquivo original é usado como base para o nome do chunk, seguido por ".part" e o índice do chunk para garantir que cada chunk tenha um nome único.
                + ".part"
                + chunkIndex;

        FileOutputStream fos =
                new FileOutputStream(
                        chunkPath           //Cria um FileOutputStream para o caminho do chunk e escreve os dados do chunk no arquivo usando fos.write, depois fecha o FileOutputStream.
                );

        fos.write(chunk);       //Escreve os dados do chunk no arquivo usando fos.write.

        fos.close();

        System.out.println(
                "Chunk salvo: "
                + chunkPath
        );
    }
}