package Server.queue;

import Server.storage.ChunkService;


//Consumidor de tarefas de upload. Ele fica em loop infinito, esperando por novas tarefas na fila de upload. Quando uma tarefa é recebida, ele processa o upload dividindo o arquivo em chunks e armazenando-os usando o serviço de chunk.
public class UploadConsumer
        implements Runnable {

    private final UploadQueue uploadQueue;

    private final ChunkService chunkService;

    public UploadConsumer(
            UploadQueue uploadQueue     //recebe a fila de upload compartilhada e inicializa o serviço de chunk.
    ) {

        this.uploadQueue =
                uploadQueue;

        this.chunkService =
                new ChunkService();
    }

    @Override
    public void run() {

        while (true) {

            try {

                UploadTask task =
                        uploadQueue.getTask(); //Aguarda por uma tarefa de upload na fila. Se a fila estiver vazia, a thread de consumidor ficará bloqueada até que uma nova tarefa seja adicionada.

                chunkService.splitAndStore(
                        task.getFileName(),  //processa o upload dividindo o arquivo em chunks e armazenando-os usando o serviço de chunk.
                        task.getData()
                );

                System.out.println(
                        "Upload processado: "
                                + task.getFileName()
                );

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}