package project_biu.configs.agents;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_biu.graph.Message;
import project_biu.graph.TopicManagerSingleton;
import project_biu.testutil.TopicCollector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the concrete {@link BinOpAgent} subclasses: the arithmetic each one wires
 * into the base and the math pattern each one renders.
 */
class BinOpAgentSubclassesTest {
    private TopicManagerSingleton.TopicManager tm;

    @BeforeEach
    void setUp() {
        tm = TopicManagerSingleton.get();
        tm.clear();
    }

    @AfterEach
    void tearDown() {
        tm.clear();
    }

    @Test
    void plusAgentSumsItsInputsAndRendersThePlusPattern() {
        final List<Double> published = TopicCollector.collectOutputOf("C");
        final PlusAgent agent = new PlusAgent(new String[]{"A", "B"}, new String[]{"C"});

        tm.getTopic("A").publish(new Message(10.0));
        tm.getTopic("B").publish(new Message(20.0));

        assertEquals(30.0, published.getLast(), 1e-9);
        assertEquals("(A + B)", agent.getMathPattern("A", "B"));
        assertEquals("C = (A + B)", agent.getMathRepresentation());
    }

    @Test
    void minusAgentSubtractsSecondInputFromFirstAndRendersTheMinusPattern() {
        final List<Double> published = TopicCollector.collectOutputOf("C");
        final MinusAgent agent = new MinusAgent(new String[]{"A", "B"}, new String[]{"C"});

        tm.getTopic("A").publish(new Message(10.0));
        tm.getTopic("B").publish(new Message(3.0));

        assertEquals(7.0, published.getLast(), 1e-9);
        assertEquals("(A - B)", agent.getMathPattern("A", "B"));
        assertEquals("C = (A - B)", agent.getMathRepresentation());
    }

    @Test
    void mulAgentMultipliesItsInputsAndRendersTheMulPattern() {
        final List<Double> published = TopicCollector.collectOutputOf("C");
        final MulAgent agent = new MulAgent(new String[]{"A", "B"}, new String[]{"C"});

        tm.getTopic("A").publish(new Message(6.0));
        tm.getTopic("B").publish(new Message(7.0));

        assertEquals(42.0, published.getLast(), 1e-9);
        assertEquals("(A * B)", agent.getMathPattern("A", "B"));
        assertEquals("C = (A * B)", agent.getMathRepresentation());
    }

    @Test
    void mathPatternFallsBackToTheAgentNameWhenArgCountIsWrong() {
        final PlusAgent agent = new PlusAgent(new String[]{"A", "B"}, new String[]{"C"});

        assertEquals("PlusAgent", agent.getMathPattern("only-one"));
    }
}
