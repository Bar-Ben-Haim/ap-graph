package project_biu.configs.agents;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_biu.graph.Message;
import project_biu.graph.TopicManagerSingleton;
import project_biu.testutil.TopicCollector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CounterAgentTest {
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
    void countsEveryMessageIncludingNonNumericOnes() {
        final List<Double> published = TopicCollector.collectOutputOf("out");
        new CounterAgent(new String[]{"in"}, new String[]{"out"});

        tm.getTopic("in").publish(new Message("anything"));
        tm.getTopic("in").publish(new Message(7.0));

        assertEquals(List.of(1.0, 2.0), published);
    }
}
