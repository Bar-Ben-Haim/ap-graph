package project_biu.configs.agents;

public class MinusAgent extends BinOpAgent {
    public MinusAgent(String[] subs, String[] pubs) {
        super("MinusAgent", subs[0], subs[1], pubs[0], (x, y) -> (x - y));
    }

    @Override
    public String getMathPattern(String... inputs) {
        if (inputs.length == 2)
            return String.format("(%s - %s)", inputs[0], inputs[1]);
        return getName();
    }
}