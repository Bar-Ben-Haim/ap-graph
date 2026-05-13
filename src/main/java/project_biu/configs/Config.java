package project_biu.configs;

public interface Config {
    void create();

    String getName();

    int getVersion();

    void close();
}
