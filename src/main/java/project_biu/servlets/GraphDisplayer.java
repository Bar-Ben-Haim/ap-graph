package project_biu.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.configs.ConfigError;
import project_biu.configs.ConfigException;
import project_biu.configs.Graph;
import project_biu.server.RequestParser;
import project_biu.server.response.ResponseUtils;
import project_biu.service.GraphService;
import project_biu.views.HtmlErrorWriter;
import project_biu.views.HtmlGraphWriter;

import java.io.IOException;
import java.io.OutputStream;

public class GraphDisplayer implements Servlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphDisplayer.class);
    private final ResponseUtils responseUtils;
    private final GraphService graphService;

    public GraphDisplayer(ResponseUtils responseUtils, GraphService graphService) {
        this.responseUtils = responseUtils;
        this.graphService = graphService;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            final Graph graph = graphService.get();
            if (graph != null) {
                responseUtils.okHtml(toClient, String.join("\n",
                        HtmlGraphWriter.getGraphHTML(graph, graph.getGraphFormulas())));
            } else {
                responseUtils.okHtml(toClient, HtmlErrorWriter.getErrorHtml(ConfigError.NOT_LOADED));
            }
        } catch (ConfigException e) {
            LOGGER.info("Error while getting graph to display", e);
            responseUtils.okHtml(toClient, HtmlErrorWriter.getErrorHtml(e.getError(), e.getMessage()));
        } catch (RuntimeException e) {
            LOGGER.error("Error while getting graph to display", e);
            responseUtils.okHtml(toClient, HtmlErrorWriter.createErrorHtml(e));
        }
    }

    @Override
    public void close() {
        // Nothing to Impl
    }
}