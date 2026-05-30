package test;

import org.junit.jupiter.api.Test;
import project_biu.configs.agents.PlusAgent;
import project_biu.graph.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTests {

    @Test
    void testMessage() {
        final Message msg = new Message("123.45");
        assertEquals("123.45", msg.asText);
        assertEquals(123.45, msg.asDouble, 0.001);
        assertNotNull(msg.date);

        final Message msg2 = new Message("not a number");
        assertTrue(Double.isNaN(msg2.asDouble));
    }

    @Test
    void testTopicAndTopicManager() {
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.clear();
        Topic t1 = tm.getTopic("topic1");
        Topic t2 = tm.getTopic("topic1");
        assertSame(t1, t2);
        assertEquals("topic1", t1.name());

        final List<Message> received = new ArrayList<>();
        Agent a = new Agent() {
            @Override
            public String getName() {
                return "a1";
            }

            @Override
            public void reset() {
            }

            @Override
            public void callback(String topic, Message msg) {
                received.add(msg);
            }

            @Override
            public void close() {
            }
        };

        t1.subscribe(a);
        t1.publish(new Message("hello"));
        assertEquals(1, received.size());
        assertEquals("hello", received.get(0).asText);

        t1.unsubscribe(a);
        t1.publish(new Message("world"));
        assertEquals(1, received.size());
    }

    @Test
    void testParallelAgent() throws InterruptedException {
        final List<Message> received = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Agent mockAgent = new Agent() {
            @Override
            public String getName() {
                return "mock";
            }

            @Override
            public void reset() {
            }

            @Override
            public void callback(String topic, Message msg) {
                received.add(msg);
                latch.countDown();
            }

            @Override
            public void close() {
            }
        };

        ParallelAgent pa = new ParallelAgent(mockAgent, 10);
        pa.callback("test", new Message("parallel"));

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, received.size());
        assertEquals("parallel", received.get(0).asText);
        pa.close();
    }

    @Test
    void testComputationalGraph() {
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.clear();

        // Graph: (A + B) -> C, (C + D) -> E
        new PlusAgent(new String[]{"A", "B"}, new String[]{"C"});
        new PlusAgent(new String[]{"C", "D"}, new String[]{"E"});
        final List<Double> results = new ArrayList<>();
        final Agent resultAgent = new Agent() {
            @Override
            public String getName() {
                return "resultAgent";
            }

            @Override
            public void reset() {
            }

            @Override
            public void callback(String topic, Message msg) {
                if (topic.equals("E")) results.add(msg.asDouble);
            }

            @Override
            public void close() {
            }
        };
        tm.getTopic("E").subscribe(resultAgent);

        tm.getTopic("A").publish(new Message(10.0));
        tm.getTopic("B").publish(new Message(20.0));
        // At this point plus1 should have published 30.0 to topic C

        tm.getTopic("D").publish(new Message(5.0));
        // Now plus2 should have published 30.0 + 5.0 = 35.0 to topic E

        assertEquals(3, results.size());
        assertEquals(35.0, results.get(2));
    }
}
