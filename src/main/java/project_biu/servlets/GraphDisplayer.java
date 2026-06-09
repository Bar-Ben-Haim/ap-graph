package project_biu.servlets;

import project_biu.configs.ConfigError;
import project_biu.configs.Graph;
import project_biu.server.RequestParser;
import project_biu.server.reponse.ResponseUtils;
import project_biu.service.GraphService;
import project_biu.views.HtmlGraphWriter;

import java.io.IOException;
import java.io.OutputStream;

public class GraphDisplayer implements Servlet {
    private final ResponseUtils responseUtils;
    private final GraphService graphService;

    public GraphDisplayer(ResponseUtils responseUtils, GraphService graphService) {
        this.responseUtils = responseUtils;
        this.graphService = graphService;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        final Graph graph = graphService.get();
        if (graph != null) {
            responseUtils.okHtml(toClient, String.join("\n", HtmlGraphWriter.getGraphHTML(graph)));
        } else {
            responseUtils.okHtml(toClient, HtmlGraphWriter.getErrorHtml(ConfigError.NOT_LOADED));
        }
    }

    @Override
    public void close() {
        // Nothing to Impl
    }
}