package Server.serverbase;

//Configurações do servidor, como porta, número de threads e caminho de armazenamento dos arquivos enviados pelos clientes.
public class ServerConfig {

    public static final int PORT = 8080;

    public static final int CONSUMER_THREADS = 2;       

    public static final String STORAGE_PATH =
        "Server/storage/files/";

}