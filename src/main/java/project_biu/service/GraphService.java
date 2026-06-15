package project_biu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.configs.*;
import project_biu.graph.TopicManagerSingleton;
import project_biu.repository.FilesRepository;
import project_biu.repository.GraphRepository;
import project_biu.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Service class responsible for managing and operating the graph data structure.
 * Provides functionalities for building, resetting, and deleting graph configurations.
 */
public class GraphService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphService.class);
    private static final TopicManagerSingleton.TopicManager TOPIC_MANAGER = TopicManagerSingleton.get();
    private final GraphRepository graphRepository;
    private final FilesRepository filesRepository;
    private Config config;
    private String activeConfigName;

    public GraphService(GraphRepository graphRepository, FilesRepository filesRepository) {
        this.graphRepository = graphRepository;
        this.filesRepository = filesRepository;
    }

    /**
     * Stores the configuration under {@code fileName} and builds the graph from it.
     */
    public synchronized Graph deploy(String fileName, String configContent) {
        deleteActiveConfig();
        if (FileUtils.containsScript(configContent)) {
            throw new ConfigException(ConfigError.UNSAFE_CONTENT);
        }
        final String safeName = FileUtils.sanitizeFileName(fileName, "config.conf");
        try {
            filesRepository.save(safeName, configContent);
            return build(safeName);
        } catch (IOException e) {
            throw new ConfigException(ConfigError.FILE_ERROR, "Could not save the configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Rebuilds the graph from the active configuration, clearing all state.
     */
    public synchronized Graph reset() {
        if (activeConfigName == null || !filesRepository.exists(activeConfigName)) {
            throw new ConfigException(ConfigError.NOT_LOADED);
        }
        return build(activeConfigName);
    }

    public synchronized Graph get() {
        return graphRepository.get();
    }

    /**
     * Tears down the graph and removes every stored configuration file.
     */
    public synchronized void deleteAll() {
        try {
            releaseGraph();
            filesRepository.deleteAll();
            activeConfigName = null;
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Could not delete all configuration files", e);
        }
    }

    /**
     * Tears down the graph and removes only the active configuration file.
     */
    public synchronized void deleteActiveConfig() {
        try {
            releaseGraph();
            if (activeConfigName != null) {
                filesRepository.delete(activeConfigName);
                activeConfigName = null;
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Could not delete the active config file", e);
        }
    }

    /**
     * Builds the graph from the active configuration.
     *
     * @param newConfigName The new configuration file to build from.
     * @return The built graph.
     */
    private Graph build(String newConfigName) {
        final Path configPath = filesRepository.location(newConfigName)
                .orElseThrow(() -> new ConfigException(ConfigError.FILE_ERROR,
                        "Configuration file is not accessible: " + newConfigName));

        final GenericConfig genericConfig = new GenericConfig();
        genericConfig.setConfFile(configPath);
        genericConfig.create();

        final Graph graph = new Graph();
        graph.createFromTopics();
        if (graph.hasCycles()) {
            graph.close();
            genericConfig.close();
            throw new ConfigException(ConfigError.CYCLES_DETECTED);
        }

        config = genericConfig;
        activeConfigName = newConfigName;
        graphRepository.save(graph);
        return graph;
    }

    private synchronized void releaseGraph() {
        if (config != null) config.close();
        config = null;
        TOPIC_MANAGER.clear();
        graphRepository.delete();
    }
}