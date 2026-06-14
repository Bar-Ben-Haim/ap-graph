package project_biu.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.configs.ConfigException;
import project_biu.server.RequestParser;
import project_biu.server.response.ResponseUtils;
import project_biu.service.GraphService;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Rebuilds the deployed graph from its saved configuration, clearing all topic
 * values and agent state.
 */
public class ConfigReset implements Servlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReset.class);
    private final ResponseUtils responseUtils;
    private final GraphService graphService;

    public ConfigReset(ResponseUtils responseUtils, GraphService graphService) {
        this.responseUtils = responseUtils;
        this.graphService = graphService;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            //noinspection resource
            graphService.reset();
        } catch (ConfigException e) {
            LOGGER.info("Reset skipped ({}): {}", e.getError(), e.getMessage());
        }
        responseUtils.ok(toClient);
    }

    @Override
    public void close() {
        // Nothing to Impl
    }
}
