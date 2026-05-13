package project_biu.configs.agents;

public class IncAgent extends UnaryOpAgent {
    public IncAgent(String[] subs, String[] pubs) {
        super("IncAgent", subs[0], pubs[0], x -> x + 1);
    }
}