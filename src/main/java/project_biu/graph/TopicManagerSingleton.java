package project_biu.graph;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides global, thread-safe access to the single {@link TopicManager} instance.
 *
 * <p>This class acts as a singleton holder: it cannot be instantiated and simply
 * exposes the shared {@link TopicManager} via {@link #get()}. The manager keeps a
 * registry of {@link Topic topics} keyed by name, creating them on demand.</p>
 */
public class TopicManagerSingleton {
    private TopicManagerSingleton() {
    }

    /**
     * Thread-safe registry of {@link Topic topics}, indexed by their unique names.
     *
     * <p>Topics are created lazily the first time they are requested,
     * allowing concurrent access from multiple threads.</p>
     */
    public static class TopicManager {
        private static final TopicManager instance = new TopicManager();
        private final Map<String, Topic> topics = new ConcurrentHashMap<>();

        /**
         * Returns the topic with the given name, creating and registering a new one
         * if it does not already exist.
         *
         * @param name the unique name of the topic
         * @return the existing topic for {@code name}, or a newly created one
         */
        public synchronized Topic getTopic(String name) {
            return topics.computeIfAbsent(name, Topic::new);
        }

        public Collection<Topic> getTopics() {
            return topics.values();
        }

        public synchronized void clear() {
            topics.clear();
        }
    }

    public static TopicManager get() {
        return TopicManager.instance;
    }
}