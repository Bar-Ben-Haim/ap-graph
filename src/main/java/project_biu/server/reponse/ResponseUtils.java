package project_biu.server.reponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ResponseUtils {
    private final ObjectMapper objectMapper;

    public ResponseUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResponseUtils() {
        this(new ObjectMapper());
    }

    public void ok(OutputStream out) throws IOException {
        ok(out, null);
    }

    public void ok(OutputStream out, Object body) throws IOException {
        response(out, StatusCode.OK, MediaType.APPLICATION_JSON, body);
    }

    public void okHtml(OutputStream out, Object body) throws IOException {
        response(out, StatusCode.OK, MediaType.TEXT_HTML, body);
    }

    public void notFound(OutputStream out) throws IOException {
        notFound(out, null);
    }

    public void notFound(OutputStream out, String message) throws IOException {
        response(out, StatusCode.NOT_FOUND, MediaType.APPLICATION_JSON, message);
    }

    public void internalServerError(OutputStream out, Throwable e) throws IOException {
        response(out, StatusCode.INTERNAL_SERVER_ERROR, MediaType.APPLICATION_JSON, e.getMessage());
    }

    public void badRequest(OutputStream out, String message) throws IOException {
        response(out, StatusCode.BAD_REQUEST, MediaType.APPLICATION_JSON, message);
    }

    public void response(OutputStream out,
                         StatusCode statusCode,
                         MediaType mediaType,
                         Object body) throws IOException {
        final StringBuilder response = new StringBuilder();
        final byte[] bodyBytes = body != null ? objectMapper.writeValueAsBytes(body) : null;
        response.append("HTTP/1.1 ")
                .append(statusCode.getCode())
                .append(" ").append(statusCode.getReasonPhrase())
                .append("\r\n");

        if (mediaType != null) {
            response.append("Content-Type: ").append(mediaType).append("\r\n");
        }
        if (body != null) {
            response.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
        }
        response.append("Connection: close\r\n\r\n");

        out.write(response.toString().getBytes(StandardCharsets.UTF_8));
        if (body != null) {
            out.write(bodyBytes);
        }
        out.flush();
    }
}