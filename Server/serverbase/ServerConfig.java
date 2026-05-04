package Server.serverbase;

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