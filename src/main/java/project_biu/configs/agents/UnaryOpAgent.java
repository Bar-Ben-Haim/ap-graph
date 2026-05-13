package project_biu.configs.agents;

import project_biu.graph.Agent;
import project_biu.graph.Message;
import project_biu.graph.Topic;
import project_biu.graph.TopicManagerSingleton;

import java.util.function.UnaryOperator;

public class UnaryOpAgent implements Agent {
    private final String name;
    private final Topic inputTopic;
    private final Topic outputTopic;
    private final UnaryOperator<Double> operator;

    public UnaryOpAgent(String name,
                        String inputTopicName,
                        String outputTopicName,
                        UnaryOperator<Double> operator) {

        final TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        this.name = name;
        this.inputTopic = tm.getTopic(inputTopicName);
        this.outputTopic = tm.getTopic(outputTopicName);
        this.operator = operator;

        inputTopic.subscribe(this);
        outputTopic.addPublisher(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        // Nothing to impl
    }

    @Override
    public void callback(String topic, Message msg) {
        final double value = msg.getAsDouble();
        if (!Double.isNaN(value)) outputTopic.publish(new Message(operator.apply(value)));
    }

    @Override
    public void close() {
        inputTopic.unsubscribe(this);
        outputTopic.removePublisher(this);
    }
}