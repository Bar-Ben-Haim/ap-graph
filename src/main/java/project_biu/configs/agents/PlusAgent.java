package project_biu.configs.agents;

public class PlusAgent extends BinOpAgent {
    public PlusAgent(String[] subs, String[] pubs) {
        super("PlusAgent", subs[0], subs[1], pubs[0], Double::sum);
    }
}