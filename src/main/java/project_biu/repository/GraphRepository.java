package project_biu.repository;

import project_biu.configs.Graph;

public interface GraphRepository {
    void save(Graph graph);

    void delete();

    Graph get();
}