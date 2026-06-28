package project_biu.server.response;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * A utility class for generating HTTP responses.
 */
public class ResponseUtils {
    private final ObjectMapper objectMapper;

    public ResponseUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResponseUtils() {
        this(new ObjectMapper());
    }

    /**
     * Generates an HTTP response with status code 200 and no body.
     *
     * @param out the output stream to write the response to.
     * @throws IOException if an I/O error occurs while writing the response.
     */
    public void ok(OutputStream out) throws IOException {
        ok(out, null);
    }

    /**
     * Generates an HTTP response with the specified status code and body.
     *
     * @param out  the output stream to write the response to.
     * @param body the body of the response.
     * @throws IOException if an I/O error occurs while writing the response.
     */
    public void ok(OutputStream out, Object body) throws IOException {
        response(out, StatusCode.OK, MediaType.APPLICATION_JSON, body);
    }

    /**
     * Generates an HTTP response with status code 200 and HTML body.
     *
     * @param out  the output stream to write the response to.
     * @param body the body of the response.
     * @throws IOException if an I/O error occurs while writing the response.
     */
    public void okHtml(OutputStream out, Object body) throws IOException {
        response(out, StatusCode.OK, MediaType.TEXT_HTML, body);
    }

    /**
     * Generates an HTTP response with status code 404 and no body.
     *
     * @param out the output stream to write the response to.
     * @throws IOException if an I/O error occurs while writing the response.
     */
    public void notFound(OutputStream out) throws IOException {
        notFound(out, null);
    }

    /**
     * Generates an HTTP response with status code 404 and JSON body.
     *
     * @param out     the output stream to write the response to.
     * @param message the message to be sent in the response body
     * @throws IOException if an I/O error occurs while writing the response.
     */
    public void notFound(OutputStream out, String message) throws IOException {
        response(out, StatusCode.NOT_FOUND, MediaType.APPLICATION_JSON, message);
    }

    /**
     * Generates an HTTP response with status code 400 and HTML body.
     *
     * @param out  the output stream to write the response to.
     * @param body the body of the response.
     * @throws IOException if an I/O error occurs while writing the response.
     */
    public void badRequestHtml(OutputStream out, Object body) throws IOException {
        response(out, StatusCode.BAD_REQUEST, MediaType.TEXT_HTML, body);
    }

    /**
     * Generates an HTTP response with status code 500 and HTML body.
     *
     * @param out  the output stream to write the response to.
     * @param body the body of the response.
     * @throws IOException if an I/O error occurs while writing the response.
     */
    public void internalServerErrorHtml(OutputStream out, Object body) throws IOException {
        response(out, StatusCode.INTERNAL_SERVER_ERROR, MediaType.TEXT_HTML, body);
    }

    /**
     * Generates an HTTP response with the specified status code, media type, and body.
     *
     * @param out        the output stream to write the response to.
     * @param statusCode the status code of the response.
     * @param mediaType  the media type of the response body.
     * @param body       the body of the response.
     * @throws IOException if an I/O error occurs while writing the response.
     */
    private void response(OutputStream out,
                          StatusCode statusCode,
                          MediaType mediaType,
                          Object body) throws IOException {
        final StringBuilder response = new StringBuilder();

        final byte[] bodyBytes = switch (body) {
            case null -> new byte[0];
            case String str when mediaType != MediaType.APPLICATION_JSON -> str.getBytes(StandardCharsets.UTF_8);
            case byte[] bytes -> bytes;
            default -> objectMapper.writeValueAsBytes(body);
        };

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