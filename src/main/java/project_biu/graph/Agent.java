package project_biu.graph;

/**
 * Represents an agent that can perform operations in a messaging-based system.
 */
public interface Agent {
    /**
     * Returns the name of the agent
     *
     * @return The name of the agent.
     */
    String getName();

    /**
     * Resets the agent to its initial state.
     */
    void reset();

    /**
     * This is the agent observable to notify upon change
     *
     * @param topic the topic that triggered the msg
     * @param msg   the msg sent in that topic
     */
    void callback(String topic, Message msg);

    /**
     * Closes the agent and releases any resources it may be using.
     */
    void close();
}