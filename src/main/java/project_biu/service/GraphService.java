package project_biu.service;

import project_biu.configs.*;
import project_biu.graph.TopicManagerSingleton;
import project_biu.repository.FileRepository;
import project_biu.repository.GraphRepository;
import project_biu.utils.InputSanitizer;

import java.nio.file.Path;

/**
 * Owns the lifecycle of the deployed computational graph.
 *
 * <p>A single instance is shared by the servlets, so {@link #deploy}, {@link #reset},
 * and {@link #deleteAll} all act on the same state. Each deployed configuration is stored
 * by file name in the {@link FileRepository}, which lets {@link #reset()} rebuild the
 * active graph without re-uploading. All public methods are synchronized so concurrent
 * requests cannot corrupt the shared {@link TopicManagerSingleton}.
 */
public class GraphService {
    private static final TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
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
        if (InputSanitizer.containsScript(configContent)) {
            throw new ConfigException(ConfigError.UNSAFE_CONTENT);
        }
        final String safeName = InputSanitizer.sanitizeFileName(fileName);
        try {
            fileRepository.save(safeName, configContent);
        } catch (RuntimeException e) {
            throw new ConfigException(ConfigError.FILE_ERROR,
                    "Could not save the configuration: " + e.getMessage(), e);
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
     * Tears down the graph and removes the active configuration.
     */
    public synchronized void deleteAll() {
        closeConfig();
        topicManager.clear();
        graphRepository.delete();
        if (activeConfigName != null) {
            fileRepository.deleteAll();
            activeConfigName = null;
        }
    }

    /**
     * Tears down the graph and removes the active configuration.
     */
    public synchronized void deleteActiveConfig() {
        closeConfig();
        topicManager.clear();
        graphRepository.delete();
        if (activeConfigName != null) {
            fileRepository.delete(activeConfigName);
            activeConfigName = null;
        }
    }

    private Graph build() {
        closeConfig();
        topicManager.clear();
        graphRepository.delete();

        final Path configPath = fileRepository.location(activeConfigName)
                .orElseThrow(() -> new ConfigException(ConfigError.FILE_ERROR,
                        "Configuration file is not accessible: " + activeConfigName));

        final GenericConfig genericConfig = new GenericConfig();
        genericConfig.setConfFile(configPath.toString());
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
}
