package project_biu.configs;

import project_biu.graph.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a node in the graph.
 */
public class Node {
    private String name;
    //Edges to the parent nodes
    private final List<Node> outEdges;
    //Edges to the son's nodes
    private final List<Node> inEdges;
    private Message msg;
    private final List<String> mathematicalDescription;

    public Node(String name) {
        this.name = name;
        this.mathematicalDescription = new ArrayList<>();
        this.outEdges = new ArrayList<>();
        this.inEdges = new ArrayList<>();
    }

    public Node(String name, List<String> mathematicalDescription) {
        this.name = name;
        this.mathematicalDescription = mathematicalDescription;
        this.outEdges = new ArrayList<>();
        this.inEdges = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getOutEdges() {
        return outEdges;
    }

    public void addOutEdge(Node node) {
        outEdges.add(node);
    }

    public List<Node> getInEdges() {
        return inEdges;
    }

    public void addInEdge(Node node) {
        inEdges.add(node);
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }

    public List<String> getMathematicalDescription() {
        return mathematicalDescription;
    }

    public boolean isRoot() {
        return outEdges.isEmpty();
    }

    public boolean isLeaf() {
        return inEdges.isEmpty();
    }

    public boolean hasCycles() {
        return outEdges.stream().anyMatch(n -> n.hasNodeReference(this, n, new HashSet<>()));
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
        return edgeNode.getOutEdges().stream().anyMatch(node -> hasNodeReference(parentNode, node, visited));
    }
}