package project_biu.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.configs.Config;
import project_biu.configs.GenericConfig;
import project_biu.configs.Graph;
import project_biu.graph.TopicManagerSingleton;
import project_biu.server.RequestParser;
import project_biu.server.reponse.ResponseUtils;
import project_biu.views.HtmlGraphWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfLoader implements Servlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfLoader.class);
    protected static Graph activeGraph = null; // TODO: Change to repository
    private Config config;
    private final ResponseUtils responseUtils;

    public ConfLoader(ResponseUtils responseUtils) {
        this.responseUtils = responseUtils;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            final MultipartResult result = parseMultipart(new String(ri.content()));
            final boolean isValid = isMultipartVaid(result, toClient);
            if (!isValid) return;
            if (activeGraph != null) activeGraph.close();
            if (config != null) config.close();
            TopicManagerSingleton.get().clear();

            final Path tempConfFile = Files.createTempFile("config_", ".conf");
            Files.writeString(tempConfFile, result.content());

            final GenericConfig genericConfig = new GenericConfig();
            genericConfig.setConfFile(tempConfFile.toString());
            genericConfig.create();
            config = genericConfig;

            final Graph graph = new Graph();
            graph.createFromTopics();
            if (graph.hasCycles()) {
                LOGGER.info("A user requested to load a graph with cycles, sending error response");
                responseUtils.badRequest(toClient, "Graph has cycles!");
                graph.close();
                return;
            }
            activeGraph = graph;
            responseUtils.okHtml(toClient, String.join("\n", HtmlGraphWriter.getGraphHTML(graph)));
        } catch (RuntimeException e) {
            LOGGER.error("Error loading config", e);
            responseUtils.internalServerError(toClient, e);
        }
    }

    /**
     * Parses a multipart/form-data body, returning the filename and content
     * of the first file part.
     */
    //TODO:change from here
    private static MultipartResult parseMultipart(String raw) {
        final String[] lines = raw.split("\n");
        if (lines.length < 1) return null;

        final String boundary = lines[0].trim();
        String filename = "upload.conf";
        final StringBuilder content = new StringBuilder();
        boolean inFilePart = false;
        boolean pastPartHeaders = false;

        for (int i = 1; i < lines.length; i++) {
            final String line = lines[i];
            final String trimmed = line.trim();

            if (trimmed.startsWith(boundary)) {
                if (inFilePart) break;
                continue;
            }

            if (!inFilePart) {
                if (trimmed.toLowerCase().startsWith("content-disposition:")) {
                    filename = extractFilename(trimmed);
                    inFilePart = true;
                }
            } else if (!pastPartHeaders) {
                if (trimmed.isEmpty()) pastPartHeaders = true;
                // skip Content-Type and other part headers
            } else {
                content.append(line).append("\n");
            }
        }

        return new MultipartResult(filename, content.toString().trim());
    }

    private static String extractFilename(String contentDisposition) {
        final int idx = contentDisposition.indexOf("filename=\"");
        if (idx == -1) return "upload.conf";
        final int start = idx + 10;
        final int end = contentDisposition.indexOf("\"", start);
        return end != -1 ? contentDisposition.substring(start, end) : "upload.conf";
    }

    private record MultipartResult(String filename, String content) {
    }

    private boolean isMultipartVaid(MultipartResult result, OutputStream toClient) throws IOException {
        if (result == null) {
            responseUtils.badRequest(toClient, "Could not parse multipart body");
            return false;
        }

        if (!result.filename().endsWith(".conf")) {
            responseUtils.badRequest(toClient,
                    String.format("Invalid file type: %s, Only .conf files are accepted.", result.filename()));
            return false;
        }

        if (result.content().isEmpty()) {
            responseUtils.badRequest(toClient, "Config file is empty.");
            return false;
        }

        return true;
    }

    @Override
    public void close() throws IOException {
        if (activeGraph != null) activeGraph.close();
        if (config != null) config.close();
    }
}