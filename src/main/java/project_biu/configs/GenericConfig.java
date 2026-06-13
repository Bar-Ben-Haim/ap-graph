package project_biu.configs;

import project_biu.graph.Agent;
import project_biu.graph.ParallelAgent;
import project_biu.utils.FileUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class GenericConfig implements Config {
    private static final int LINES_PER_AGENT = 3;
    private Path confFile;
    private final Set<Agent> agents = new HashSet<>();

    @Override
    public void create() {
        if (confFile == null) {
            throw new ConfigException(ConfigError.EMPTY_CONFIG);
        }

        final List<String> lines;
        try {
            lines = FileUtils.readFileContent(confFile).map(String::lines).map(Stream::toList)
                    .orElseThrow(() -> new ConfigException(ConfigError.FILE_ERROR,
                            "Could not read the configuration file: " + confFile.getFileName()));
        } catch (IOException e) {
            throw new ConfigException(ConfigError.FILE_ERROR,
                    "Could not read the configuration file: " + confFile.getFileName(), e);
        }

        if (lines.isEmpty()) {
            throw new ConfigException(ConfigError.EMPTY_CONFIG);
        }
        if (lines.size() % LINES_PER_AGENT != 0) {
            throw new ConfigException(ConfigError.INVALID_FORMAT,
                    "Configuration must contain a multiple of " + LINES_PER_AGENT
                            + " lines (class, inputs, outputs per agent), but found " + lines.size() + ".");
        }

        for (int i = 0; i < lines.size(); i += LINES_PER_AGENT) {
            final String className = lines.get(i).trim();
            final String[] subs = splitTopics(lines.get(i + 1));
            final String[] pubs = splitTopics(lines.get(i + 2));
            agents.add(new ParallelAgent(instantiateAgent(className, subs, pubs), 1000));
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

    public void setConfFile(Path confFile) {
        this.confFile = confFile;
    }

    private Agent instantiateAgent(String className, String[] subs, String[] pubs) {
        final Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ConfigException(ConfigError.UNKNOWN_AGENT, "Unknown agent class: " + className, e);
        }

        if (!Agent.class.isAssignableFrom(clazz)) {
            throw new ConfigException(ConfigError.INVALID_AGENT, "Class " + className + " does not implement Agent.");
        }

        final Constructor<?> constructor;
        try {
            constructor = clazz.getConstructor(String[].class, String[].class);
        } catch (NoSuchMethodException e) {
            throw new ConfigException(ConfigError.INVALID_AGENT,
                    "Agent class " + className + " has no (String[], String[]) constructor.", e);
        }

        final Object instance;
        try {
            instance = constructor.newInstance(subs, pubs);
        } catch (ReflectiveOperationException e) {
            throw new ConfigException(ConfigError.AGENT_INITIALIZATION_FAILED,
                    "Failed to initialize agent " + className, e);
        }

        return (Agent) instance;
    }

    private static String[] splitTopics(String line) {
        return Arrays.stream(line.split(",")).map(String::trim).toArray(String[]::new);
    }
}