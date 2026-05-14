package project_biu.configs;

import java.io.Closeable;

public interface Config extends Closeable {
    void create();

    String getName();

    int getVersion();
}
