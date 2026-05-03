package Server.queue;

import java.util.LinkedList;
import java.util.List;


//Fila de upload para gerenciar as tarefas de upload recebidas dos clientes. As tarefas são processadas por um pool de threads, garantindo que o servidor possa lidar com múltiplas conexões simultaneamente sem sobrecarregar os recursos do sistema.
public class UploadQueue {

    private final List<UploadTask> queue =
            new LinkedList<>();

    public synchronized void addTask(       //Impede acesso simultâneo perigoso.
            UploadTask task) {

        queue.add(task); //Adiciona a tarefa à fila.

        notify(); //Notifica uma thread de consumidor que uma nova tarefa está disponível.  
    }

    public synchronized UploadTask getTask()
            throws InterruptedException {

        while (queue.isEmpty()) { //Se a fila estiver vazia, a thread de consumidor aguarda até que uma nova tarefa seja adicionada.

            wait();
        }

        return queue.remove(0);
    }
}