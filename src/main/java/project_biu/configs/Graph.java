package project_biu.configs;

import project_biu.graph.*;

import java.io.Closeable;
import java.util.*;

/**
 * Represents a directed graph of topics and agents.
 */
public class Graph extends ArrayList<Node> implements Closeable {
    private final Map<String, Node> topicNodes = new HashMap<>();
    private final Map<Agent, Node> agentNodes = new HashMap<>();
    private final List<Agent> nodeUpdaters = new ArrayList<>();
    private final List<String> graphFormulas = new ArrayList<>();

    public void createFromTopics() {
        close();
        final Collection<Topic> topics = TopicManagerSingleton.get().getTopics();

        topics.forEach(this::mapTopicToNode);
        topics.stream()
                .map(topic -> createNodeUpdaterAgent(topic, topicNodes.get(topic.name())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(nodeUpdaters::add);

        findGraphFormulas();
        addAll(topicNodes.values());
        addAll(agentNodes.values());
    }

    public boolean hasCycles() {
        return this.stream().anyMatch(Node::hasCycles);
    }

    @Override
    public void close() {
        clear();
        topicNodes.clear();
        agentNodes.clear();
        nodeUpdaters.forEach(Agent::close);
        nodeUpdaters.clear();
        graphFormulas.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Graph nodes)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(topicNodes, nodes.topicNodes) &&
                Objects.equals(agentNodes, nodes.agentNodes) &&
                Objects.equals(nodeUpdaters, nodes.nodeUpdaters);
    }

    public List<String> getGraphFormulas() {
        return graphFormulas;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), topicNodes, agentNodes, nodeUpdaters);
    }

    private void mapTopicToNode(Topic topic) {
        topicNodes.computeIfAbsent(topic.name(), name -> {
            final Node topicNode = new Node("T" + name);
            topic.subs().stream().map(this::mapAgentToNode).forEach(subAgent -> {
                topicNode.addOutEdge(subAgent);
                subAgent.addInEdge(topicNode);
            });
            topic.pubs().stream().map(this::mapAgentToNode).forEach(pubAgent -> {
                pubAgent.addOutEdge(topicNode);
                topicNode.addInEdge(pubAgent);
            });

            return topicNode;
        });
    }

    private Node mapAgentToNode(Agent agent) {
        return agentNodes.computeIfAbsent(agent, newAgent -> {
            if (newAgent instanceof MathematicalDescribable mathematicalDescribable)
                return new Node("A" + newAgent.getName(), List.of(mathematicalDescribable.getMathRepresentation()));
            return new Node("A" + newAgent.getName());
        });
    }

    private void findGraphFormulas() {
        topicNodes.values().stream()
                .filter(Node::isRoot)
                .forEach(rootTopic -> {
                    final String topicName = rootTopic.getName().replaceFirst("T", "");
                    rootTopic.getInEdges().stream()
                            .filter(a -> !a.getMathematicalDescription().isEmpty())
                            .flatMap(a -> findGraphFormulas(a, new HashSet<>()).stream())
                            .map(formula -> topicName + " = " + unwrapFormula(formula))
                            .distinct()
                            .forEach(graphFormulas::add);
                });
    }

    /**
     * Resolves mathematical formulas associated with an agent node.
     *
     * @param agentNode the node representing an agent for which formulas need to be resolved.
     * @param visited   a set of nodes that have already been visited, used to avoid infinite loops.
     * @return a list of mathematical formula strings resolved for the given agent node.
     */
    private List<String> findGraphFormulas(Node agentNode, Set<Node> visited) {
        if (!visited.add(agentNode)) return List.of(agentNode.getName().substring(1));

        final List<List<String>> nodeInputsFormulas = agentNode.getInEdges().stream()
                .map(inputTopic -> inputTopic.isLeaf() ?
                        List.of(inputTopic.getName().replaceFirst("^T", "")) :
                        inputTopic.getInEdges().stream()
                                .flatMap(inputAgent ->
                                        findGraphFormulas(inputAgent, new HashSet<>(visited)).stream())
                                .toList())
                .toList();

        // Finds the mathematical description of the current agent node
        final MathematicalDescribable agent = findAgentNode(agentNode)
                .filter(MathematicalDescribable.class::isInstance)
                .map(MathematicalDescribable.class::cast)
                .orElseThrow();
        // Create a list of all possible combinations of input formulas
        return cartesianProduct(nodeInputsFormulas).stream()
                .map(inputFormulas -> agent.getMathPattern(inputFormulas.toArray(String[]::new)))
                .toList();
    }

    /**
     * Finds the agent that corresponds to the given {@code agentNode}.
     *
     * @param agentNode the node that is the origin of the agent.
     * @return the agent that corresponds to the given {@code agentNode}, or empty if not found.
     */
    private Optional<Agent> findAgentNode(Node agentNode) {
        return agentNodes.entrySet().stream()
                .filter(e -> e.getValue() == agentNode)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Computes the Cartesian product of a list of lists of strings.
     * The Cartesian product is a set of all possible combinations of
     * elements where one element is taken from each list.
     *
     * @param lists a list of lists of strings for which the Cartesian product will be computed.
     *              Each inner list represents a set of possible options at a specific position.
     * @return a list of lists of strings, where each inner list represents one possible
     * combination from the Cartesian product.
     */
    private List<List<String>> cartesianProduct(List<List<String>> lists) {
        List<List<String>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (List<String> options : lists) {
            result = result.stream().flatMap(current ->
                    options.stream().map(opt -> {
                        final List<String> next = new ArrayList<>(current);
                        next.add(opt);
                        return next;
                    })).toList();
        }

        return result;
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
            public void callback(String t, Message msg) {
                topicNode.setMsg(msg);
            }

            @Override
            public void close() {
                topic.unsubscribe(this);
            }
        });
    }

    private String unwrapFormula(String formula) {
        if (formula != null && formula.startsWith("(") && formula.endsWith(")"))
            return formula.substring(1, formula.length() - 1);

        return formula;
    }
}