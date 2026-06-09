package project_biu.configs.agents;

import project_biu.graph.Agent;
import project_biu.graph.Message;
import project_biu.graph.Topic;
import project_biu.graph.TopicManagerSingleton;

/**
 * Tracks the running maximum of all numeric values seen on the input topic.
 * A new message is published only when the maximum is updated.
 * Non-numeric messages are ignored.
 *
 * <pre>
 * subs[0] = input topic
 * pubs[0] = current maximum output topic
 * </pre>
 */
public class MaxTrackerAgent implements Agent {
    private final Topic inputTopic;
    private final Topic outputTopic;
    private double max = Double.NEGATIVE_INFINITY;

    public MaxTrackerAgent(String[] subs, String[] pubs) {
        final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        this.inputTopic = topicManager.getTopic(subs[0]);
        this.outputTopic = topicManager.getTopic(pubs[0]);

        inputTopic.subscribe(this);
        outputTopic.addPublisher(this);
    }

    @Override
    public String getName() {
        return "MaxTrackerAgent";
    }

    @Override
    public void reset() {
        max = Double.NEGATIVE_INFINITY;
    }

    @Override
    public void callback(String topic, Message msg) {
        final double value = msg.getAsDouble();
        if (Double.isNaN(value)) return;
        if (value > max) {
            max = value;
            outputTopic.publish(new Message(max));
        }
    }

    @Override
    public void close() {
        inputTopic.unsubscribe(this);
        outputTopic.removePublisher(this);
    }
}