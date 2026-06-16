package project_biu.configs.agents;

import project_biu.graph.*;

/**
 * Tracks the running maximum of all numeric values seen on the input topic.
 * A new message is published only when the maximum is updated.
 * <p> Non-numeric messages are ignored.
 */
public class MaxTrackerAgent implements Agent, MathematicalDescribable {
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
    public String getMathPattern(String... inputs) {
        if (inputs.length == 1)
            return "MAX(" + inputs[0] + ")";
        return getName();
    }

    @Override
    public String getMathRepresentation() {
        return String.format("%s = %s", outputTopic.name(), getMathPattern(inputTopic.name()));
    }

    @Override
    public void close() {
        inputTopic.unsubscribe(this);
        outputTopic.removePublisher(this);
    }
}