package project_biu.configs.agents;

public class PlusAgent extends BinOpAgent {
    public PlusAgent(String[] subs, String[] pubs) {
        super("PlusAgent", subs[0], subs[1], pubs[0], Double::sum);
    }

    @Override
    public String getMathPattern(String... inputs) {
        if (inputs.length == 2)
            return String.format("(%s + %s)", inputs[0], inputs[1]);
        return getName();
    }
}