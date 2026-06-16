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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A utility class for parsing HTTP request data from a BufferedReader into a structured format.
 * This class is designed to handle the parsing of request lines, headers, query params,
 * and the body.
 * <p>
 * The class provides a method to parse a full request and returns a {@link RequestInfo} object
 * containing the details of the parsed request.
 * <p>
 */
public final class RequestParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestParser.class);

    private RequestParser() {
    }

    /**
     * Parses a full HTTP request from a BufferedReader and returns a RequestInfo object containing all the details
     * about the request.
     *
     * @param reader the BufferedReader to read the request from
     * @return a RequestInfo object containing the details of the parsed request, or null if the request is not valid
     * @throws IOException if an I/O error occurs while reading the request
     */
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        final String requestLine = reader.readLine();
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
        final Map<String, String> parameters = parseQueryParameters(uri, queryIndex);
        final String[] uriSegments = calculateUriSegments(uri, queryIndex);

        skipHeaders(reader);
        final byte[] content = parseBody(reader, parameters);

        return new RequestInfo(httpCommand, uri, uriSegments, parameters, content);
    }

    /**
     * Parses the query parameters from the given URI starting at the specified query index.
     *
     * @param uri        The full URI containing the query string.
     * @param queryIndex The index in the URI where the query string begins (e.g., the index of '?').
     * @return Map containing the query parameters as key-value pairs.
     */
    private static Map<String, String> parseQueryParameters(String uri, int queryIndex) {
        if (queryIndex == -1) {
            return new HashMap<>();
        }
        return Arrays.stream(uri.substring(queryIndex + 1).split("&"))
                .map(pair -> pair.split("=", 2))
                .filter(kv -> kv.length > 0)
                .collect(Collectors.toMap(kv -> decode(kv[0]),
                        kv -> kv.length > 1 ? decode(kv[1]) : "", (existing, _) -> existing));
    }

    /**
     * Skips the headers of the request.
     *
     * @param reader the BufferedReader to read the headers from
     * @throws IOException if an I/O error occurs while reading the headers
     */
    private static void skipHeaders(BufferedReader reader) throws IOException {
        String headerLine;
        //noinspection StatementWithEmptyBody
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            // Header lines are intentionally ignored.
        }
    }

    /**
     * Parses the body of an HTTP request from the provided BufferedReader and returns it as a byte array.
     *
     * @param reader     The BufferedReader from which the body of the request is read.
     * @param parameters Map to store metadata extracted from the body, with key-value pairs.
     * @return Byte array containing the parsed content of the body.
     * @throws IOException If an I/O error occurs while reading the body.
     */
    private static byte[] parseBody(BufferedReader reader, Map<String, String> parameters) throws IOException {
        final StringBuilder contentBuilder = new StringBuilder();
        final BodyParseState state = new BodyParseState();
        String line;
        while (state.reading && reader.ready() && (line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                handleEmptyLine(state, contentBuilder);
            } else {
                handleContentLine(line, parameters, contentBuilder, state);
            }
        }
        return contentBuilder.toString().getBytes();
    }

    private static void handleEmptyLine(BodyParseState state, StringBuilder contentBuilder) {
        if (state.metadataPhase) {
            // The empty line marks the end of the metadata section.
            state.metadataPhase = false;
        } else if (state.isMultipart) {
            // Multipart bodies have empty lines inside them (between part headers and content).
            contentBuilder.append("\n");
        } else {
            state.reading = false;
        }
    }

    /**
     * Processes a line of content from body's request
     *
     * @param line           The current line from the HTTP request body being processed.
     * @param parameters     A map to store key-value pairs extracted as metadata from the content.
     * @param contentBuilder A StringBuilder to store the parsed body content.
     * @param state          Indicates the current state of the body parsing, including whether
     *                       metadata is still being parsed and whether the body is multipart.
     */
    private static void handleContentLine(String line,
                                          Map<String, String> parameters,
                                          StringBuilder contentBuilder,
                                          BodyParseState state) {
        if (state.metadataPhase && line.contains("=")) {
            extractMetadata(line, parameters);
            return;
        }

        if (state.metadataPhase && line.startsWith("--")) {
            state.isMultipart = true;
        }

        state.metadataPhase = false;
        contentBuilder.append(line).append("\n");
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
     * State used while parsing the body of a request.
     */
    private static final class BodyParseState {
        private boolean metadataPhase = true;
        private boolean isMultipart = false;
        private boolean reading = true;
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
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            RequestInfo that = (RequestInfo) o;
            return Objects.equals(uri, that.uri) &&
                    Objects.deepEquals(content, that.content) &&
                    Objects.equals(httpCommand, that.httpCommand) &&
                    Objects.deepEquals(uriSegments, that.uriSegments) &&
                    Objects.equals(parameters, that.parameters);
        }

        @Override
        public int hashCode() {
            return Objects.hash(httpCommand, uri, Arrays.hashCode(uriSegments), parameters, Arrays.hashCode(content));
        }

        @Override
        public String toString() {
            return "RequestInfo{" +
                    "httpCommand='" + httpCommand + '\'' +
                    ", uri='" + uri + '\'' +
                    ", uriSegments=" + Arrays.toString(uriSegments) +
                    ", parameters=" + parameters +
                    ", content=" + Arrays.toString(content) +
                    '}';
        }
    }
}