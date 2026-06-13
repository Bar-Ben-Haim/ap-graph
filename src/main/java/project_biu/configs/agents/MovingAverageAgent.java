package project_biu.configs.agents;

import project_biu.graph.*;

/**
 * Computes a moving average of the input numeric values.
 *
 * <p>Non-numeric messages are silently ignored. The average is published after every accepted sample.
 *
 * <pre>
 * subs[0] = input topic
 * pubs[0] = moving average output topic
 * </pre>
 */
public class MovingAverageAgent implements Agent, MathematicalDescribable {
    private final Topic inputTopic;
    private final Topic outputTopic;
    private int messagesCounter = 0;
    private double sum = 0.0;

    public MovingAverageAgent(String[] subs, String[] pubs) {
        final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        this.inputTopic = topicManager.getTopic(subs[0]);
        this.outputTopic = topicManager.getTopic(pubs[0]);
        inputTopic.subscribe(this);
        outputTopic.addPublisher(this);
    }

    @Override
    public String getName() {
        return "MovingAverageAgent";
    }

    @Override
    public void reset() {
        messagesCounter = 0;
        sum = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {
        final double value = msg.getAsDouble();
        if (Double.isNaN(value)) return;

        messagesCounter++;
        sum = sum + value;
        outputTopic.publish(new Message(sum / messagesCounter));
    }

    @Override
    public void close() {
        inputTopic.unsubscribe(this);
        outputTopic.removePublisher(this);
    }

    @Override
    public String getMathPattern(String... inputs) {
        if (inputs.length == 1)
            return "AVG(" + inputs[0] + ")";
        return getName();
    }

    @Override
    public String getMathRepresentation() {
        return String.format("%s = %s", outputTopic.name(), getMathPattern(inputTopic.name()));
    }
}