package Server.serverbase;

public class ServerConfig {

    public static final int PORT = Integer.parseInt(EnvLoader.getOrDefault("SERVER_PORT", "9101"));
    public static final String STORAGE_PATH = EnvLoader.getOrDefault("STORAGE_PATH", "storage/default");

}
