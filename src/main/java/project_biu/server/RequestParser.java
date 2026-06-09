package project_biu.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestParser.class);

    private RequestParser() {
    }

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        final String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            return null;
        }
        final String httpCommand = requestParts[0];
        final String uri = requestParts[1];
        final int queryIndex = uri.indexOf('?');
        final Map<String, String> parameters = (queryIndex == -1) ? new HashMap<>() :
                Arrays.stream(uri.substring(queryIndex + 1).split("&"))
                        .map(pair -> pair.split("=", 2))
                        .filter(kv -> kv.length > 0)
                        .collect(Collectors.toMap(kv -> decode(kv[0]), kv -> kv.length > 1 ? decode(kv[1]) : "",
                                (existing, _) -> existing));
        final String[] uriSegments = calculateUriSegments(uri, queryIndex);
        //noinspection StatementWithEmptyBody
        while ((requestLine = reader.readLine()) != null && !requestLine.isEmpty()) {
            // This is a header line, we will ignore them
        }

        boolean metadataPhase = true;
        boolean isMultipart = false;
        final StringBuilder contentBuilder = new StringBuilder();
        String line;
        while (reader.ready() && (line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if (metadataPhase) {
                    metadataPhase = false;
                    continue;
                }
                // Multipart bodies have empty lines inside them (between part headers and content).
                // For regular payloads we keep the original behavior: stop on empty line.
                if (isMultipart) {
                    contentBuilder.append("\n");
                    continue;
                }
                break;
            }

            if (metadataPhase && line.contains("=")) {
                extractMetadata(line, parameters);
            } else {
                // Detect multipart: boundary lines start with '--'
                if (metadataPhase && line.startsWith("--")) {
                    isMultipart = true;
                }
                metadataPhase = false;
                contentBuilder.append(line).append("\n");
            }
        }

        return new RequestInfo(httpCommand, uri, uriSegments, parameters, contentBuilder.toString().getBytes());
    }

    private static void extractMetadata(String metadataLine, Map<String, String> parameters) {
        if (metadataLine != null && metadataLine.contains("=")) {
            String[] kv = metadataLine.split("=", 2);
            if (kv.length > 0) parameters.put(decode(kv[0]), kv.length > 1 ? decode(kv[1]) : "");
        }
    }

    /**
     * Decodes the URL-encoded string using UTF-8 encoding.
     *
     * @param value the URL-encoded string to decode
     * @return the decoded string, or the original string if decoding fails
     */
    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            LOGGER.atWarn().setCause(e).log("An error has occurred while decoding {}", value);
            return value;
        }
    }

    private static String[] calculateUriSegments(String uri, int queryIndex) {
        final String uriWithoutQuery = queryIndex == -1 ? uri : uri.substring(0, queryIndex);
        return Arrays.stream(uriWithoutQuery.split("/"))
                .filter(segment -> !segment.isEmpty())
                .toArray(String[]::new);
    }

    /**
     * A basic pojo for holding request info.
     *
     * @param httpCommand the http command (GET, POST, etc.)
     * @param uri         the uri of the request
     * @param uriSegments the uri segments of the request
     * @param parameters  the parameters of the request
     * @param content     the content of the request (body)
     */
    public record RequestInfo(String httpCommand,
                              String uri,
                              String[] uriSegments,
                              Map<String, String> parameters,
                              byte[] content) {
    }
}