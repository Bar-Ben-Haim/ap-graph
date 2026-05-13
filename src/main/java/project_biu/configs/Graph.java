package project_biu.configs;

import project_biu.graph.Agent;
import project_biu.graph.Topic;
import project_biu.graph.TopicManagerSingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Graph extends ArrayList<Node> {
    private final Map<String, Node> topicNodes = new HashMap<>();
    private final Map<String, Node> agentNodes = new HashMap<>();

    public void createFromTopics() {
        clear();
        topicNodes.clear();
        agentNodes.clear();

        TopicManagerSingleton.get().getTopics().forEach(this::mapTopicToNode);
        addAll(topicNodes.values());
        addAll(agentNodes.values());
    }

    public boolean hasCycles() {
        return this.stream().anyMatch(Node::hasCycles);
    }

    private void mapTopicToNode(Topic topic) {
        topicNodes.computeIfAbsent(topic.name(), name -> {
            final Node topicNode = new Node("T" + name);
            topic.subs().stream().map(this::mapAgentToNode).forEach(topicNode::addEdge);
            topic.pubs().stream().map(this::mapAgentToNode).forEach(agentNode -> agentNode.addEdge(topicNode));
            return topicNode;
        });
    }

    private Node mapAgentToNode(Agent agent) {
        return agentNodes.computeIfAbsent(agent.getName(), name -> new Node("A" + name));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Graph nodes)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(topicNodes, nodes.topicNodes) && Objects.equals(agentNodes, nodes.agentNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), topicNodes, agentNodes);
    }
}