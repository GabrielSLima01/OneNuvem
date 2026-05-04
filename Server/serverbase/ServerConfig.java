package Server.serverbase;


// Classe de configuração do servidor que carrega as variáveis de ambiente usando o EnvLoader e define as constantes de configuração do servidor, como a porta e o caminho de armazenamento
public class ServerConfig {

    public static final int PORT =
            Integer.parseInt(
                    EnvLoader.get(
                            "SERVER_PORT"
                    )
            );

    public static final String
            STORAGE_PATH =
            EnvLoader.get(
                    "STORAGE_PATH"
            );
}