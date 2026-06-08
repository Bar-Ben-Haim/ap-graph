package project_biu.servlets;

import project_biu.server.RequestParser;
import project_biu.server.reponse.ResponseUtils;
import project_biu.service.GraphService;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Removes the active configuration entirely: closes all agents, clears every topic,
 * and discards the stored graph and configuration.
 */
public class ConfigDelete implements Servlet {
    private final ResponseUtils responseUtils;
    private final GraphService graphService;

    public ConfigDelete(ResponseUtils responseUtils, GraphService graphService) {
        this.responseUtils = responseUtils;
        this.graphService = graphService;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        graphService.deleteActiveConfig();
        responseUtils.ok(toClient);
    }

    @Override
    public void close() throws IOException {
        graphService.deleteAll();
    }
}