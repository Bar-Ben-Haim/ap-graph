package project_biu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.configs.*;
import project_biu.graph.TopicManagerSingleton;
import project_biu.repository.FileRepository;
import project_biu.repository.GraphRepository;
import project_biu.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

public class GraphService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphService.class);
    private static final TopicManagerSingleton.TopicManager TOPIC_MANAGER = TopicManagerSingleton.get();
    private final GraphRepository graphRepository;
    private final FileRepository fileRepository;
    private Config config;
    private String activeConfigName;

    public GraphService(GraphRepository graphRepository, FileRepository fileRepository) {
        this.graphRepository = graphRepository;
        this.fileRepository = fileRepository;
    }

    /**
     * Stores the configuration under {@code fileName} and builds the graph from it.
     */
    public synchronized Graph deploy(String fileName, String configContent) {
        if (FileUtils.containsScript(configContent)) {
            throw new ConfigException(ConfigError.UNSAFE_CONTENT);
        }
        final String safeName = FileUtils.sanitizeFileName(fileName, "config.conf");
        try {
            fileRepository.save(safeName, configContent);
        } catch (RuntimeException | IOException e) {
            throw new ConfigException(ConfigError.FILE_ERROR, "Could not save the configuration: " + e.getMessage(), e);
        }

        activeConfigName = safeName;
        return build();
    }

    /**
     * Rebuilds the graph from the active configuration, clearing all state.
     */
    public synchronized Graph reset() {
        if (activeConfigName == null || !fileRepository.exists(activeConfigName)) {
            throw new ConfigException(ConfigError.NOT_LOADED);
        }
        return build();
    }

    public synchronized Graph get() {
        return graphRepository.get();
    }

    /**
     * Tears down the graph and removes <em>every</em> stored configuration file.
     */
    public synchronized void deleteAll() {
        try {
            releaseGraph();
            fileRepository.deleteAll();
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
                fileRepository.delete(activeConfigName);
                activeConfigName = null;
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Could not delete the active config file", e);
        }
    }

    private Graph build() {
        releaseGraph();

        final Path configPath = fileRepository.location(activeConfigName)
                .orElseThrow(() -> new ConfigException(ConfigError.FILE_ERROR,
                        "Configuration file is not accessible: " + activeConfigName));

        final GenericConfig genericConfig = new GenericConfig();
        genericConfig.setConfFile(configPath);
        genericConfig.create();
        config = genericConfig;

        final Graph graph = new Graph();
        graph.createFromTopics();
        if (graph.hasCycles()) {
            deleteActiveConfig();
            throw new ConfigException(ConfigError.CYCLES_DETECTED);
        }
        graphRepository.save(graph);
        return graph;
    }

    private void closeConfig() {
        if (config != null) config.close();
        config = null;
    }

    private synchronized void releaseGraph() {
        closeConfig();
        TOPIC_MANAGER.clear();
        graphRepository.delete();
    }
}