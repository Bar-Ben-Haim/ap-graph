package project_biu.configs.agents;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_biu.graph.Message;
import project_biu.graph.TopicManagerSingleton;
import project_biu.testutil.TopicCollector;

import java.util.List;
import java.util.function.BinaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the abstract {@link BinOpAgent} base class through a local test subclass,
 * independent of the concrete Plus/Minus/Mul agents. Covers the logic that lives
 * in the base itself: operator application, two-input gating, math representation,
 * reset and close.
 */
class BinOpAgentTest {
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

    private static final class TestBinOpAgent extends BinOpAgent {
        TestBinOpAgent(String[] subs, String[] pubs, BinaryOperator<Double> op) {
            super("TestBinOpAgent", subs[0], subs[1], pubs[0], op);
        }

        @Override
        public String getMathPattern(String... inputs) {
            return inputs.length == 2 ? "f(" + inputs[0] + ", " + inputs[1] + ")" : getName();
        }
    }

    private TestBinOpAgent newSumAgent() {
        return new TestBinOpAgent(new String[]{"A", "B"}, new String[]{"C"}, Double::sum);
    }

    @Test
    void binOpNormalInputTest() {
        final List<Double> published = TopicCollector.collectOutputOf("C");
        new TestBinOpAgent(new String[]{"A", "B"}, new String[]{"C"}, (x, y) -> x * 10 + y);

        tm.getTopic("A").publish(new Message(3.0));
        tm.getTopic("B").publish(new Message(4.0));

        assertEquals(34.0, published.getLast());
    }

    @Test
    void nonNumericTest() {
        final List<Double> published = TopicCollector.collectOutputOf("C");
        newSumAgent();

        tm.getTopic("A").publish(new Message("not a number"));
        tm.getTopic("B").publish(new Message(5.0));

        assertTrue(published.isEmpty(), "a NaN input must suppress publishing");
    }

    @Test
    void mathPatternTest() {
        final TestBinOpAgent agent = newSumAgent();
        assertEquals("C = f(A, B)", agent.getMathRepresentation());
    }

    @Test
    void resetTest() {
        final List<Double> published = TopicCollector.collectOutputOf("C");
        final TestBinOpAgent agent = newSumAgent();
        tm.getTopic("A").publish(new Message(10.0));
        agent.reset();
        tm.getTopic("B").publish(new Message(20.0));

        assertEquals(20.0, published.getLast());
    }

    @Test
    void closedAgentsDoNotReactToInputTopicsTest() {
        final List<Double> published = TopicCollector.collectOutputOf("C");
        final TestBinOpAgent agent = newSumAgent();
        agent.close();
        tm.getTopic("A").publish(new Message(1.0));
        tm.getTopic("B").publish(new Message(2.0));

        assertTrue(published.isEmpty(), "a closed agent must not react to its input topics");
    }
}
