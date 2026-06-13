package project_biu.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.server.RequestParser;
import project_biu.server.response.ResponseUtils;
import project_biu.utils.FileUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

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

        final Path filePath = Paths.get(htmlFilesPath, segments[segments.length - 1]);
        final Optional<String> fileContent = FileUtils.readFileContent(filePath);

        if (fileContent.isPresent()) {
            responseUtils.okHtml(toClient, fileContent.get().getBytes());
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