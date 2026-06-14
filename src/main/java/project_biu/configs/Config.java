package project_biu.configs;

/**
 * Represents a configuration interface with basic operations for creating,
 * managing, and closing configurations.
 */
public interface Config {
    void create();

    String getName();

    int getVersion();

    void close();
}