public class Node {
    private final String name;
    private final String host;
    private final int port;
    private boolean healthy;
    private int failCount;

    public Node(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.healthy = true;
        this.failCount = 0;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public int getFailCount() {
        return failCount;
    }

    public void markHealthy() {
        this.healthy = true;
        this.failCount = 0;
    }

    public void markUnhealthy() {
        this.healthy = false;
        this.failCount++;
    }

    @Override
    public String toString() {
        return name + " (" + host + ":" + port + ") healthy=" + healthy;
    }
}
