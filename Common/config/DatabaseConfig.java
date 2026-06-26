package Common.config;

import Server.serverbase.EnvLoader;

public final class DatabaseConfig {

    private DatabaseConfig() {
    }

    public static String url() {
        return EnvLoader.getRequired("DATABASE_URL");
    }

    public static String user() {
        return EnvLoader.getRequired("DATABASE_USER");
    }

    public static String password() {
        return EnvLoader.getRequired("DATABASE_PASSWORD");
    }
}
