package project_biu.configs.agents;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_biu.graph.Message;
import project_biu.graph.TopicManagerSingleton;
import project_biu.testutil.TopicCollector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncAgentTest {
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
    void incValueTest() {
        final List<Double> published = TopicCollector.collectOutputOf("out");
        new IncAgent(new String[]{"in"}, new String[]{"out"});

        tm.getTopic("in").publish(new Message(4.0));

        assertEquals(List.of(5.0), published);
    }

    @Test
    void ignoresNonNumericInput() {
        final List<Double> published = TopicCollector.collectOutputOf("out");
        new IncAgent(new String[]{"in"}, new String[]{"out"});
        tm.getTopic("in").publish(new Message("not a number"));

        assertTrue(published.isEmpty());
    }
}
