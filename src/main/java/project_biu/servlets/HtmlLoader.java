package project_biu.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.server.RequestParser;
import project_biu.server.reponse.ResponseUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlLoader implements Servlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlLoader.class);
    private final ResponseUtils responseUtils;
    private final String htmlFilesPath;

    public HtmlLoader(ResponseUtils responseUtils, String htmlFilesPath) {
        this.responseUtils = responseUtils;
        this.htmlFilesPath = htmlFilesPath;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        final String[] segments = ri.uriSegments();
        if (segments.length == 0)
            return;

        final String filename = segments[segments.length - 1];
        final Path filePath = Paths.get(htmlFilesPath, filename);

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            responseUtils.okHtml(toClient, Files.readAllBytes(filePath));
        } else {
            LOGGER.warn("An html file not found: {}", filePath);
            responseUtils.notFound(toClient);
        }
    }

    @Override
    public void close() throws IOException {
        // Nothing to close
    }
}