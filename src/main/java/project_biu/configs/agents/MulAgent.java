package project_biu.configs.agents;

public class MulAgent extends BinOpAgent {
    public MulAgent(String[] subs, String[] pubs) {
        super("MulAgent", subs[0], subs[1], pubs[0], (x, y) -> (x * y));
    }
}