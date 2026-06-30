package Server.serverbase;

import java.util.Optional;

public class ServerConfig {

    public static final int PORT = Integer.parseInt(Optional.ofNullable(EnvLoader.get("SERVER_PORT")).orElse("3000"));
    public static final String STORAGE_PATH = EnvLoader.get("STORAGE_PATH");
    public static final int WORKER_THREADS = Integer.parseInt(Optional.ofNullable(EnvLoader.get("WORKER_THREADS")).orElse("10"));
}
