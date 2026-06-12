package project_biu.configs;

/**
 * Lists every error condition that can occur while loading, validating, or
 * rendering a computational-graph configuration.
 */
public enum ConfigError {
    NOT_LOADED(Severity.INFO, "No computational graph has been deployed yet." +
            " Upload a .conf file using the form on the left panel."),
    EMPTY_CONFIG(Severity.WARNING,
            "The configuration file is empty. Add at least one agent definition."),
    INVALID_FORMAT(Severity.WARNING,
            "The configuration is malformed. " +
                    "Each agent must be described by exactly three lines: class name, input topics, output topics."),
    UNSAFE_CONTENT(Severity.WARNING,
            "The configuration contains markup or script-like content and was rejected."),
    UNKNOWN_AGENT(Severity.WARNING,
            "The configuration references an agent class that could not be found. Check the fully-qualified class name."),
    INVALID_AGENT(Severity.WARNING,
            "An agent class is not usable: it must implement Agent and expose a (String[], String[]) constructor."),
    AGENT_INITIALIZATION_FAILED(Severity.WARNING,
            "An agent could not be initialized. Check that each agent has the expected number of input and output topics."),
    CYCLES_DETECTED(Severity.WARNING,
            "The uploaded configuration contains cyclic agent dependencies. " +
                    "All agents must form a directed acyclic graph (DAG)."),
    FILE_ERROR(Severity.ERROR,
            "The configuration file could not be read or written on the server."),
    RENDER_ERROR(Severity.ERROR,
            "The server encountered an error while generating the graph visualization."),
    INTERNAL_ERROR(Severity.ERROR,
            "An unexpected error occurred. Check the server logs for details.");

    /**
     * Drives the error page's color and icon; not tied to a specific error type.
     */
    public enum Severity {
        INFO, WARNING, ERROR
    }

    private final Severity severity;
    private final String defaultMessage;

    ConfigError(Severity severity, String defaultMessage) {
        this.severity = severity;
        this.defaultMessage = defaultMessage;
    }

    public Severity severity() {
        return severity;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}