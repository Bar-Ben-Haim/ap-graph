package project_biu.graph;

import java.util.ArrayList;
import java.util.List;

public record Topic(String name, List<Agent> pubs, List<Agent> subs) {
    Topic(String name) {
        this(name, new ArrayList<>(), new ArrayList<>());
    }

    public void subscribe(Agent agent) {
        subs.add(agent);
    }

    public void unsubscribe(Agent agent) {
        subs.remove(agent);
    }

    public void addPublisher(Agent agent) {
        pubs.add(agent);
    }

    public void removePublisher(Agent agent) {
        pubs.remove(agent);
    }

    public void publish(Message message) {
        subs.forEach(agent -> agent.callback(name, message));
    }
}