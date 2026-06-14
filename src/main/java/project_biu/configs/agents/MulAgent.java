package project_biu.configs.agents;

/**
 * An agent that takes two inputs and sends the multipcation to an output {@link project_biu.graph.Topic}.
 * <p> Non-numeric messages are ignored.
 */
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