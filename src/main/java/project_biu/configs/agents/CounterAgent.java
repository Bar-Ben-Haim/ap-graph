package project_biu.configs.agents;

import project_biu.graph.*;

/**
 * Counts the total number of messages received on the input topic since creation or last reset.
 */
public class CounterAgent implements Agent, MathematicalDescribable {
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

    @Override
    public String getMathPattern(String... inputs) {
        if (inputs.length == 1)
            return "COUNT(" + inputs[0] + ")";
        return getName();
    }

    @Override
    public String getMathRepresentation() {
        return String.format("%s = %s", outputTopic.name(), getMathPattern(inputTopic.name()));
    }
}