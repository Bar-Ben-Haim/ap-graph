package project_biu.graph;

import java.util.concurrent.*;

/**
 * An active decorator to an agent for processesing messages asynchronously.
 */
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

    /**
     * Adds a message to the queue to be processed asynchronously by the original agent.
     *
     * @param topic the topic that triggered the msg
     * @param msg   the msg sent in that topic
     */
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

    /**
     * Processes messages asynchronously by the original agent.
     *
     * @param agent the agent to process messages for
     * @return a future representing the asynchronous processing task
     */
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