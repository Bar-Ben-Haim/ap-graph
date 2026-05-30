package project_biu.repository;

import project_biu.configs.Graph;

public class LocalGraphRepository implements GraphRepository {
    private Graph graph;

    @Override
    public void save(Graph newGraph) {
        graph = newGraph;
    }

    @Override
    public void delete() {
        if (graph != null) graph.close();
        graph = null;
    }

    @Override
    public Graph get() {
        return graph;
    }
}
