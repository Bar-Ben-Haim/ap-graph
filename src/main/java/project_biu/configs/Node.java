package project_biu.configs;

import project_biu.graph.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Node {
    private String name;
    private List<Node> edges = new ArrayList<>();
    private Message msg;

    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getEdges() {
        return edges;
    }

    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }

    public void addEdge(Node node) {
        edges.add(node);
    }

    public boolean hasCycles() {
        return edges.stream().anyMatch(n -> n.hasNodeReference(this, n, new HashSet<>()));
    }

    /**
     * Checks if the parentNode is referenced by edgeNode or any of its edges.
     *
     * @param parentNode The node to check if it is referenced in edgeNode or any of its edges.
     * @param edgeNode   The node to check if it has the parentNode as an inner reference.
     * @param visited    The nodes that already have been visited, used to prevent infinite loops.
     * @return true if the parentNode is referenced, false otherwise
     */
    private boolean hasNodeReference(Node parentNode, Node edgeNode, Set<Node> visited) {
        if (parentNode == edgeNode) return true;
        if (!visited.add(edgeNode)) return false;
        return edgeNode.getEdges().stream().anyMatch(node -> hasNodeReference(parentNode, node, visited));
    }
}