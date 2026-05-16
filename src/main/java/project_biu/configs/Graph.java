package project_biu.configs;

import project_biu.graph.Agent;
import project_biu.graph.Topic;
import project_biu.graph.TopicManagerSingleton;

import java.io.Closeable;
import java.util.*;

public class Graph extends ArrayList<Node> implements Closeable {
    private final Map<String, Node> topicNodes = new HashMap<>();
    private final Map<String, Node> agentNodes = new HashMap<>();
    private final List<Agent> nodeUpdaters = new ArrayList<>();

    public void createFromTopics() {
        close();
        final Collection<Topic> topics = TopicManagerSingleton.get().getTopics();

        topics.forEach(this::mapTopicToNode);
        topics.stream()
                .map(topic -> createNodeUpdaterAgent(topic, topicNodes.get(topic.name())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(nodeUpdaters::add);

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

    /**
     * Creates a node-updater agent for the given {@code topic} and updates
     * the given {@code topicNode} with the latest message.
     *
     * @param topic     the topic to create the node-updater agent for.
     * @param topicNode the node that represents the topic, which will be updated by the node-updater agent.
     * @return the created node-updater agent, or empty if the topic or topicNode is null.
     */
    private Optional<Agent> createNodeUpdaterAgent(Topic topic, Node topicNode) {
        if (topicNode == null || topic == null) return Optional.empty();

        return Optional.of(new Agent() {
            {
                topic.subscribe(this);
            }

            @Override
            public String getName() {
                return "NodeUpdater_" + topic.name();
            }

            @Override
            public void reset() {
                // Nothing to do
            }

            @Override
            public void callback(String t, project_biu.graph.Message msg) {
                topicNode.setMsg(msg);
            }

            @Override
            public void close() {
                topic.unsubscribe(this);
            }
        });
    }

    @Override
    public void close() {
        clear();
        topicNodes.clear();
        agentNodes.clear();
        nodeUpdaters.forEach(Agent::close);
        nodeUpdaters.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Graph nodes)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(topicNodes, nodes.topicNodes) &&
                Objects.equals(agentNodes, nodes.agentNodes) &&
                Objects.equals(nodeUpdaters, nodes.nodeUpdaters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), topicNodes, agentNodes, nodeUpdaters);
    }
}