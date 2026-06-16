package project_biu.repository;

import project_biu.configs.Graph;

/**
 * A repository for managing graphs.
 */
public interface GraphRepository {
    /**
     * Save the graph to the repository.
     *
     * @param graph The graph to save.
     */
    void save(Graph graph);

    /**
     * Delete the graph from the repository.
     */
    void delete();

    /**
     * Get the graph from the repository.
     *
     * @return The graph.
     */
    Graph get();
}