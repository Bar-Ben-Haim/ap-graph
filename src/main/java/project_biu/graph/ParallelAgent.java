package project_biu.graph;

import java.util.concurrent.*;

public class ParallelAgent implements Agent {
    private final Agent agent;
    private final BlockingQueue<MessageWrapper> messageQueue;
    private final ExecutorService executorService;
    private final Future<?> sendMessagesTask;

    public ParallelAgent(Agent agent, int maxQueueSize) {
        this.agent = agent;
        this.messageQueue = new ArrayBlockingQueue<>(maxQueueSize);
        this.executorService = Executors.newSingleThreadExecutor();
        this.sendMessagesTask = processMessagesAsync(agent);
    }

    @Override
    public String getName() {
        return agent.getName();
    }

    @Override
    public void reset() {
        messageQueue.clear();
        agent.reset();
    }

    @Override
    public void callback(String topic, Message msg) {
        try {
            messageQueue.put(new MessageWrapper(topic, msg));
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        sendMessagesTask.cancel(true);
        executorService.shutdown();
        agent.close();
    }

    private Future<?> processMessagesAsync(Agent agent) {
        return executorService.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    final MessageWrapper messageWrapper = messageQueue.take();
                    agent.callback(messageWrapper.topic(), messageWrapper.message());
                }
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private record MessageWrapper(String topic, Message message) {
    }
}