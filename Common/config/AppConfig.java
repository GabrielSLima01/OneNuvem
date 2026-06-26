package Common.config;

import Server.serverbase.EnvLoader;
import java.util.ArrayList;
import java.util.List;

public final class AppConfig {

    private AppConfig() {
    }

    public static String appMode() {
        return EnvLoader.getOrDefault("APP_MODE", "gateway");
    }

    public static String gatewayHost() {
        return EnvLoader.getOrDefault("GATEWAY_HOST", "127.0.0.1");
    }

    public static int gatewayPort() {
        return Integer.parseInt(EnvLoader.getOrDefault("GATEWAY_PORT", "8080"));
    }

    public static String jwtSecret() {
        return EnvLoader.getOrDefault("JWT_SECRET", "change-me");
    }

    public static String jwtIssuer() {
        return EnvLoader.getOrDefault("JWT_ISSUER", "projeto-sd");
    }

    public static int uploadChunkSize() {
        return Integer.parseInt(EnvLoader.getOrDefault("UPLOAD_CHUNK_SIZE", "524288"));
    }

    public static long maxUploadBytes() {
        return Long.parseLong(EnvLoader.getOrDefault("MAX_UPLOAD_BYTES", "1073741824"));
    }

    public static int replicaCount() {
        return Integer.parseInt(EnvLoader.getOrDefault("NODE_REPLICA_COUNT", "2"));
    }

    public static int workerThreads() {
        return Math.max(8, Runtime.getRuntime().availableProcessors() * 2);
    }

    public static List<String> rawNodes() {
        String configured = EnvLoader.getOrDefault("NODES", "");
        List<String> nodes = new ArrayList<>();

        for (String rawNode : configured.split(";")) {
            String trimmed = rawNode.trim();
            if (!trimmed.isEmpty()) {
                nodes.add(trimmed);
            }
        }

        return nodes;
    }
}
