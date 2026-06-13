package project_biu.configs.agents;

public class IncAgent extends UnaryOpAgent {
    public IncAgent(String[] subs, String[] pubs) {
        super("IncAgent", subs[0], pubs[0], x -> x + 1);
    }

    @Override
    public String getMathPattern(String... inputs) {
        if (inputs.length == 1)
            return String.format("(%s + 1)", inputs[0]);
        return getName();
    }
}