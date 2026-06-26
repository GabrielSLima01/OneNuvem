package Middleware;

import Common.config.AppConfig;
import Services.NodeSocketService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeManager {

    private final List<Node> nodes = new ArrayList<>();
    private final AtomicInteger pointer = new AtomicInteger();

    public NodeManager() {
        loadNodes();
    }

    public synchronized void loadNodes() {
        nodes.clear();
        for (String rawNode : AppConfig.rawNodes()) {
            String[] parts = rawNode.split(":");
            if (parts.length < 4) {
                continue;
            }
            nodes.add(new Node(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], true, 0));
        }
    }

    public synchronized int size() {
        return nodes.size();
    }

    public synchronized List<Node> pickNodes(int count) {
        List<Node> healthyNodes = nodes.stream()
                .filter(Node::healthy)
                .sorted(Comparator.comparingInt(Node::failCount))
                .toList();
        if (healthyNodes.isEmpty()) {
            return List.of();
        }

        List<Node> selected = new ArrayList<>();
        int start = Math.floorMod(pointer.getAndIncrement(), healthyNodes.size());
        for (int i = 0; i < healthyNodes.size() && selected.size() < count; i++) {
            Node node = healthyNodes.get((start + i) % healthyNodes.size());
            if (selected.stream().noneMatch(item -> item.name().equals(node.name()))) {
                selected.add(node);
            }
        }
        return selected;
    }

    public synchronized Node findByName(String name) {
        return nodes.stream().filter(node -> node.name().equals(name)).findFirst().orElse(null);
    }

    public synchronized void markAsFailed(Node node) {
        replace(node.name(), node.withHealthy(false));
    }

    public synchronized void markAsHealthy(Node node) {
        replace(node.name(), new Node(node.name(), node.host(), node.port(), node.storagePath(), true, 0));
    }

    public synchronized List<Node> nodes() {
        return List.copyOf(nodes);
    }

    public void refreshHealth(NodeSocketService nodeSocketService) {
        for (Node node : nodes()) {
            if (nodeSocketService.ping(node)) {
                markAsHealthy(node);
            } else {
                markAsFailed(node);
            }
        }
    }

    private void replace(String nodeName, Node replacement) {
        for (int index = 0; index < nodes.size(); index++) {
            if (nodes.get(index).name().equals(nodeName)) {
                nodes.set(index, replacement);
                return;
            }
        }
    }
}
