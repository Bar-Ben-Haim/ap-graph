package project_biu.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.configs.ConfigError;
import project_biu.configs.ConfigException;
import project_biu.configs.Graph;
import project_biu.server.MultipartParser;
import project_biu.server.RequestParser;
import project_biu.server.response.ResponseUtils;
import project_biu.service.GraphService;
import project_biu.utils.FileUtils;
import project_biu.views.HtmlErrorWriter;
import project_biu.views.HtmlGraphWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Loads and deploys a configuration from a multipart request with a .conf file.
 * This will create the computational graph.
 */
public class ConfLoader implements Servlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfLoader.class);
    private static final String CONFIG_FILE_SUFFIX = ".conf";
    private final ResponseUtils responseUtils;
    private final GraphService graphService;

    public ConfLoader(ResponseUtils responseUtils, GraphService graphService) {
        this.responseUtils = responseUtils;
        this.graphService = graphService;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            final MultipartParser.MultipartResult result = MultipartParser
                    .parseMultipart(new String(ri.content()), CONFIG_FILE_SUFFIX);
            validateMultipart(result);
            final Graph graph = graphService.deploy(result.filename(), result.content());
            responseUtils.okHtml(toClient, String.join("\n",
                    HtmlGraphWriter.getGraphHTML(graph, graph.getGraphFormulas())));
        } catch (ConfigException e) {
            graphService.deleteActiveConfig();
            LOGGER.info("Configuration rejected ({}): {}", e.getError(), e.getMessage());
            responseUtils.badRequestHtml(toClient, HtmlErrorWriter.getErrorHtml(e.getError(), e.getMessage()));
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected error loading config", e);
            responseUtils.internalServerErrorHtml(toClient, HtmlErrorWriter.createErrorHtml(e));
        }
    }

    private void validateMultipart(MultipartParser.MultipartResult result) {
        if (result == null) {
            throw new ConfigException(ConfigError.INTERNAL_ERROR);
        }

        if (result.filename() == null || !result.filename().endsWith(CONFIG_FILE_SUFFIX)) {
            throw new ConfigException(ConfigError.INVALID_FORMAT, "Invalid file name: "
                    + FileUtils.sanitizeFileName(result.filename(), "config.conf"));
        }

        if (result.content() == null || result.content().isEmpty()) {
            throw new ConfigException(ConfigError.EMPTY_CONFIG);
        }
    }

    @Override
    public void close() {
        // Nothing to Impl
    }
}