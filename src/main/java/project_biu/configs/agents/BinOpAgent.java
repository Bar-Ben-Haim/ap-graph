package project_biu.configs.agents;

import project_biu.graph.Agent;
import project_biu.graph.Message;
import project_biu.graph.Topic;
import project_biu.graph.TopicManagerSingleton;

import java.util.function.BinaryOperator;

public class BinOpAgent implements Agent {
    private final String name;
    private final Topic firstTopic;
    private final Topic secondTopic;
    private final Topic outputTopic;
    private final BinaryOperator<Double> binaryOperator;
    private double firstInput;
    private double secondInput;

    public BinOpAgent(String name,
                      String firstTopicName,
                      String secondTopicName,
                      String outputTopicName,
                      BinaryOperator<Double> binaryOperator) {
        final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        this.name = name;
        this.firstTopic = topicManager.getTopic(firstTopicName);
        this.secondTopic = topicManager.getTopic(secondTopicName);
        this.outputTopic = topicManager.getTopic(outputTopicName);
        this.binaryOperator = binaryOperator;

        firstTopic.subscribe(this);
        secondTopic.subscribe(this);
        outputTopic.addPublisher(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        firstInput = 0;
        secondInput = 0;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (firstTopic.name().equals(topic)) {
            firstInput = msg.getAsDouble();
        } else if (secondTopic.name().equals(topic)) {
            secondInput = msg.getAsDouble();
        }

        if (!Double.isNaN(firstInput) && !Double.isNaN(secondInput)) {
            outputTopic.publish(new Message(binaryOperator.apply(firstInput, secondInput)));
        }
    }

    @Override
    public void close() {
        firstTopic.unsubscribe(this);
        secondTopic.unsubscribe(this);
        outputTopic.removePublisher(this);
    }
}