package project_biu.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.configs.Config;
import project_biu.configs.GenericConfig;
import project_biu.configs.Graph;
import project_biu.graph.TopicManagerSingleton;
import project_biu.repository.GraphRepository;
import project_biu.server.RequestParser;
import project_biu.server.reponse.ResponseUtils;
import project_biu.views.HtmlGraphWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ConfLoader implements Servlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfLoader.class);
    private static final String CONFIG_FILE_SUFFIX = ".conf";
    private final ResponseUtils responseUtils;
    private final GraphRepository graphRepository;
    private Config config;

    public ConfLoader(ResponseUtils responseUtils, GraphRepository graphRepository) {
        this.responseUtils = responseUtils;
        this.graphRepository = graphRepository;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            final MultipartResult result = parseMultipart(new String(ri.content()));
            final boolean isValid = isMultipartValid(result, toClient);
            if (!isValid) return;
            if (config != null) config.close();
            TopicManagerSingleton.get().clear();

            //TODO: decide what to do with sonarcube comment
            final Path tempConfFile = Files.createTempFile("config_", CONFIG_FILE_SUFFIX);
            assert result != null;
            Files.writeString(tempConfFile, result.content());

            final GenericConfig genericConfig = new GenericConfig();
            genericConfig.setConfFile(tempConfFile.toString());
            genericConfig.create();
            config = genericConfig;

            final Graph graph = new Graph();
            graph.createFromTopics();
            if (graph.hasCycles()) {
                LOGGER.info("A user requested to load a graph with cycles, sending error response");
                responseUtils.okHtml(toClient, HtmlGraphWriter.getErrorHtml(
                        "CYCLES_DETECTED",
                        "The uploaded configuration contains cyclic agent dependencies. All agents must form a directed acyclic graph (DAG)."
                ));
                graph.close();
                return;
            }
            graphRepository.delete();
            graphRepository.save(graph);
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
        final StringBuilder content = new StringBuilder();
        boolean inFilePart = false;
        boolean pastPartHeaders = false;
        String filename = null;
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
        if (idx == -1) return UUID.randomUUID() + CONFIG_FILE_SUFFIX;
        final int start = idx + 10;
        final int end = contentDisposition.indexOf("\"", start);
        return end != -1 ? contentDisposition.substring(start, end) : UUID.randomUUID() + CONFIG_FILE_SUFFIX;
    }

    private record MultipartResult(String filename, String content) {
    }

    private boolean isMultipartValid(MultipartResult result, OutputStream toClient) throws IOException {
        //TODO: add logging
        if (result == null) {
            responseUtils.badRequest(toClient, "Could not parse multipart body");
            return false;
        }

        if (!result.filename().endsWith(CONFIG_FILE_SUFFIX)) {
            responseUtils.badRequest(toClient,
                    String.format("Invalid file type: %s, Only .conf files are accepted.", result.filename()));
            return false;
        }

        if (result.content() == null || result.content().isEmpty()) {
            responseUtils.badRequest(toClient, "Config file is empty.");
            return false;
        }

        return true;
    }

    @Override
    public void close() throws IOException {
        graphRepository.delete();
        if (config != null) config.close();
    }
}