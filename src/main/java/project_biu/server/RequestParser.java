package project_biu.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestParser {
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
                        .collect(Collectors.toMap(kv -> kv[0], kv -> kv.length > 1 ? kv[1] : "",
                                (existing, _) -> existing));
        final String[] uriSegments = calculateUriSegments(uri, queryIndex);
        //noinspection StatementWithEmptyBody
        while ((requestLine = reader.readLine()) != null && !requestLine.isEmpty()) {
            // This is a header line,we will ignore them
        }

        boolean metadataPhase = true;
        StringBuilder contentBuilder = new StringBuilder();
        String line;
        while (reader.ready() && (line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if (metadataPhase) {
                    metadataPhase = false;
                    continue;
                }
                break;
            }

            if (metadataPhase && line.contains("=")) {
                extractMetadata(line, parameters);
            } else {
                metadataPhase = false;
                contentBuilder.append(line).append("\n");
            }
        }

        return new RequestInfo(httpCommand, uri, uriSegments, parameters, contentBuilder.toString().getBytes());
    }

    private static void extractMetadata(String metadataLine, Map<String, String> parameters) {
        if (metadataLine != null && metadataLine.contains("=")) {
            String[] kv = metadataLine.split("=", 2);
            if (kv.length > 0) parameters.put(kv[0], kv.length > 1 ? kv[1] : "");
        }
    }

    private static String[] calculateUriSegments(String uri, int queryIndex) {
        final String uriWithoutQuery = queryIndex == -1 ? uri : uri.substring(0, queryIndex);
        return Arrays.stream(uriWithoutQuery.split("/"))
                .filter(segment -> !segment.isEmpty())
                .toArray(String[]::new);
    }

    public record RequestInfo(String httpCommand,
                              String uri,
                              String[] uriSegments,
                              Map<String, String> parameters,
                              byte[] content) {
    }
}
