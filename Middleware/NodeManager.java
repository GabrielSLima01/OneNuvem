import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeManager {
    private final List<Node> nodes;
    private int nextIndex;

    public NodeManager() {
        this.nodes = new ArrayList<>();
        this.nextIndex = 0;
    }

    public synchronized void addNode(Node node) {
        nodes.add(node);
    }

    public synchronized int size() {
        return nodes.size();
    }

    public synchronized List<Node> getNodes() {
        return Collections.unmodifiableList(new ArrayList<>(nodes));
    }

    public synchronized Node getNextHealthyNode() {
        if (nodes.isEmpty()) {
            return null;
        }

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(nextIndex);
            nextIndex = (nextIndex + 1) % nodes.size();

            if (node.isHealthy()) {
                return node;
            }
        }

        return null;
    }

    public synchronized Node getNextNode() {
        if (nodes.isEmpty()) {
            return null;
        }

        Node node = nodes.get(nextIndex);
        nextIndex = (nextIndex + 1) % nodes.size();

        return node;
    }

    public synchronized void markAsHealthy(Node node) {
        node.markHealthy();
    }

    public synchronized void markAsFailed(Node node) {
        node.markUnhealthy();
    }
}
