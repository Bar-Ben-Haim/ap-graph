package project_biu.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.configs.ConfigError;
import project_biu.configs.ConfigException;
import project_biu.configs.Graph;
import project_biu.server.RequestParser;
import project_biu.server.reponse.ResponseUtils;
import project_biu.service.GraphService;
import project_biu.views.HtmlGraphWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

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
        final MultipartResult result = parseMultipart(new String(ri.content()));
        if (!isMultipartValid(result, toClient)) return;

        try {
            final Graph graph = graphService.deploy(result.filename(), result.content());
            responseUtils.okHtml(toClient, String.join("\n", HtmlGraphWriter.getGraphHTML(graph)));
        } catch (ConfigException e) {
            LOGGER.info("Configuration rejected ({}): {}", e.getError(), e.getMessage());
            responseUtils.okHtml(toClient, HtmlGraphWriter.getErrorHtml(e.getError(), e.getMessage()));
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected error loading config", e);
            responseUtils.okHtml(toClient, HtmlGraphWriter.getErrorHtml(ConfigError.INTERNAL_ERROR));
        }
    }

    /**
     * Parses a multipart/form-data body, returning the filename and content
     * of the first file part.
     */
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
            return false; //TODO: change to ok request but the content is empty and add error using Config.error = EMPTY_CONFIG later
        }

        return true;
    }

    @Override
    public void close() throws IOException {
        graphService.deleteAll();
    }
}