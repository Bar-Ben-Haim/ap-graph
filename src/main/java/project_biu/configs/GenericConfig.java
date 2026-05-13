package project_biu.configs;

import project_biu.graph.Agent;
import project_biu.graph.ParallelAgent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenericConfig implements Config {
    private String confFile;
    private final Set<Agent> agents = new HashSet<>();

    @Override
    public void create() {
        try {
            if (confFile == null) return;
            final List<String> lines = Files.readAllLines(Paths.get(confFile));
            if (lines.size() % 3 != 0) return;
            for (int i = 0; i < lines.size(); i += 3) {
                final Object instance = Class.forName(lines.get(i))
                        .getConstructor(String[].class, String[].class)
                        .newInstance(lines.get(i + 1).split(","), lines.get(i + 2).split(","));
                if (instance instanceof Agent agent) {
                    agents.add(new ParallelAgent(agent, 1000));
                }
            }
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "GenericConfig";
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void close() {
        agents.forEach(Agent::close);
    }

    public void setConfFile(String confFile) {
        this.confFile = confFile;
    }
}