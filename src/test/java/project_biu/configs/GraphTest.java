package project_biu.configs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_biu.configs.agents.PlusAgent;
import project_biu.graph.TopicManagerSingleton;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {
    private Graph graph;

    @BeforeEach
    void setUp() {
        TopicManagerSingleton.get().clear();
        graph = new Graph();
    }

    @AfterEach
    void tearDown() {
        TopicManagerSingleton.get().clear();
        graph.close();
    }

    @Test
    void basicGraphTest() {
        new PlusAgent(new String[]{"A", "B"}, new String[]{"C"});
        graph.createFromTopics();

        assertFalse(graph.isEmpty());
        final long topicNodes = graph.stream().filter(n -> n.getName().startsWith("T")).count();
        final long agentNodes = graph.stream().filter(n -> n.getName().startsWith("A")).count();
        assertEquals(3, topicNodes);
        assertEquals(1, agentNodes);
    }

    @Test
    void notCyclicGraphTest() {
        new PlusAgent(new String[]{"A", "B"}, new String[]{"C"});
        graph.createFromTopics();
        assertFalse(graph.hasCycles());
    }

    @Test
    void cyclicGraphTest() {
        new PlusAgent(new String[]{"A", "C"}, new String[]{"C"});
        graph.createFromTopics();
        assertTrue(graph.hasCycles());
    }

    @Test
    void graphFormulaTest() {
        new PlusAgent(new String[]{"A", "B"}, new String[]{"C"});
        graph.createFromTopics();
        assertTrue(graph.getGraphFormulas().contains("C = A + B"));
    }

    @Test
    void complexGraphFormulaTest() {
        new PlusAgent(new String[]{"A", "B"}, new String[]{"C"});
        new PlusAgent(new String[]{"C", "D"}, new String[]{"E"});
        graph.createFromTopics();
        assertTrue(graph.getGraphFormulas().contains("E = (A + B) + D"));
    }

    @Test
    void validateGraphAfterCloseTest() {
        new PlusAgent(new String[]{"A", "B"}, new String[]{"C"});
        graph.createFromTopics();
        graph.close();

        assertTrue(graph.isEmpty());
        assertTrue(graph.getGraphFormulas().isEmpty());
    }
}
