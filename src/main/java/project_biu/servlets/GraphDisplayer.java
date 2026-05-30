package project_biu.servlets;

import project_biu.configs.Graph;
import project_biu.repository.GraphRepository;
import project_biu.server.RequestParser;
import project_biu.server.reponse.ResponseUtils;
import project_biu.views.HtmlGraphWriter;

import java.io.IOException;
import java.io.OutputStream;

public class GraphDisplayer implements Servlet {
    private final ResponseUtils responseUtils;
    private final GraphRepository graphRepository;

    public GraphDisplayer(ResponseUtils responseUtils, GraphRepository graphRepository) {
        this.responseUtils = responseUtils;
        this.graphRepository = graphRepository;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        final Graph graph = graphRepository.get();
        if (graph != null) {
            responseUtils.okHtml(toClient, String.join("\n", HtmlGraphWriter.getGraphHTML(graph)));
        } else {
            responseUtils.okHtml(toClient, HtmlGraphWriter.getErrorHtml(
                    "NOT_LOADED",
                    "No computational graph has been deployed yet. Upload a .conf file using the form on the left panel."));
        }
    }

    @Override
    public void close() throws IOException {
        graphRepository.delete();
    }
}