package project_biu.configs;

/**
 * Thrown when a configuration cannot be loaded, validated, or turned into a graph.
 * Carries a {@link ConfigError} so callers can render the appropriate error page.
 */
public class ConfigException extends RuntimeException {
    private final transient ConfigError error;

    public ConfigException(ConfigError error) {
        this(error, error.defaultMessage());
    }

    public ConfigException(ConfigError error, String message) {
        super(message);
        this.error = error;
    }

    public ConfigException(ConfigError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public ConfigError getError() {
        return error;
    }
}