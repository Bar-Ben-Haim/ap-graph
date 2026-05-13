package project_biu.graph;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {
    private TopicManagerSingleton() {
    }

    public static class TopicManager {
        private static final TopicManager instance = new TopicManager();
        private final Map<String, Topic> topics = new ConcurrentHashMap<>();

        public synchronized Topic getTopic(String name) {
            return topics.computeIfAbsent(name, Topic::new);
        }

        public Collection<Topic> getTopics() {
            return topics.values();
        }

        public void clear() {
            topics.clear();
        }
    }

    public static TopicManager get() {
        return TopicManager.instance;
    }
}