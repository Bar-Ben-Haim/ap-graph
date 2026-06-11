package project_biu.configs.agents;

import project_biu.graph.Agent;
import project_biu.graph.Message;
import project_biu.graph.Topic;
import project_biu.graph.TopicManagerSingleton;

/**
 * Counts the total number of messages received on the input topic since creation or last reset.
 * The counter-value (as a double) is published on every received message, regardless of content.
 *
 * <pre>
 * subs[0] = trigger topic (any message increments the counter)
 * pubs[0] = count output topic
 * </pre>
 */
public class CounterAgent implements Agent {
    private final Topic inputTopic;
    private final Topic outputTopic;
    private int count = 0;

    public CounterAgent(String[] subs, String[] pubs) {
        final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        this.inputTopic = topicManager.getTopic(subs[0]);
        this.outputTopic = topicManager.getTopic(pubs[0]);

        inputTopic.subscribe(this);
        outputTopic.addPublisher(this);
    }

    @Override
    public String getName() {
        return "CounterAgent";
    }

    @Override
    public void reset() {
        count = 0;
    }

    @Override
    public void callback(String topic, Message msg) {
        outputTopic.publish(new Message(++count));
    }

    @Override
    public void close() {
        inputTopic.unsubscribe(this);
        outputTopic.removePublisher(this);
    }
}