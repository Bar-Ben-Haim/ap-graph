package project_biu.configs.agents;

public class MulAgent extends BinOpAgent {
    public MulAgent(String[] subs, String[] pubs) {
        super("MulAgent", subs[0], subs[1], pubs[0], (x, y) -> (x * y));
    }

    @Override
    public String getMathPattern(String... inputs) {
        if (inputs.length == 2)
            return String.format("(%s * %s)", inputs[0], inputs[1]);
        return getName();
    }
}