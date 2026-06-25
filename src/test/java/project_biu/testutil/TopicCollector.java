package project_biu.testutil;

import project_biu.graph.Agent;
import project_biu.graph.Message;
import project_biu.graph.TopicManagerSingleton;

import java.util.ArrayList;
import java.util.List;

public final class TopicCollector {
    private TopicCollector() {
    }

    /**
     * Subscribes a collector to {@code topic} and returns the list it fills.
     *
     * @param topic the topic to observe
     * @return a live list that receives each published value (as a double)
     */
    public static List<Double> collectOutputOf(String topic) {
        final List<Double> values = new ArrayList<>();
        TopicManagerSingleton.get().getTopic(topic).subscribe(new Agent() {
            public String getName() {
                return "collector_" + topic;
            }

            public void reset() { /* Nothing to impl */ }

            public void callback(String t, Message msg) {
                values.add(msg.asDouble);
            }

            public void close() { /* Nothing to impl */ }
        });
        return values;
    }
}
