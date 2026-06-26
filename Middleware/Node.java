package Middleware;

public record Node(
        String name,
        String host,
        int port,
        String storagePath,
        boolean healthy,
        int failCount
) {

    public Node withHealthy(boolean value) {
        return new Node(name, host, port, storagePath, value, value ? 0 : failCount + 1);
    }
}
