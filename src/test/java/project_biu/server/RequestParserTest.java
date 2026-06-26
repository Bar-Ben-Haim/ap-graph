package project_biu.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestParserTest {

    private static RequestParser.RequestInfo parse(String raw) throws IOException {
        return RequestParser.parseRequest(new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)))));
    }

    @Test
    void withPathParam_withQueryParam_parseTest() throws IOException {
        final RequestParser.RequestInfo info =
                parse("GET /api/sum?a=2&b=5 HTTP/1.1\nHost: localhost\n\nfilename=result.txt\n\npayload-line\n\n");

        Assertions.assertNotNull(info);
        assertEquals("GET", info.httpCommand());
        assertEquals("/api/sum?a=2&b=5", info.uri());
        assertArrayEquals(new String[]{"api", "sum"}, info.uriSegments());

        final Map<String, String> expectedParameters = new HashMap<>();
        expectedParameters.put("a", "2");
        expectedParameters.put("b", "5");
        expectedParameters.put("filename", "result.txt");
        assertEquals(expectedParameters, info.parameters());
        assertArrayEquals("payload-line\n".getBytes(StandardCharsets.UTF_8), info.content());
    }

    @Test
    void noQuery_parseTest() throws IOException {
        final RequestParser.RequestInfo info = parse("GET /publish HTTP/1.1\nHost: localhost\n\n\n");

        Assertions.assertNotNull(info);
        assertEquals("GET", info.httpCommand());
        assertEquals("/publish", info.uri());
        assertArrayEquals(new String[]{"publish"}, info.uriSegments());
    }

    @Test
    void noQuery_withParams_parseTest() throws IOException {
        final RequestParser.RequestInfo info = parse("GET /publish HTTP/1.1\nHost: localhost\n\nx=7\n\n\n");

        Assertions.assertNotNull(info);
        assertEquals("7", info.parameters().get("x"));
    }

    @Test
    void multipartUploadTest() throws IOException {
        final String boundary = "----WebKitFormBoundaryABC123";
        final String fileContent = "project_biu.configs.agents.PlusAgent\nA,B\nC\nproject_biu.configs.agents.IncAgent\nC\nD";
        final String body = boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"simple1.conf\"\r\n"
                + "Content-Type: application/octet-stream\r\n"
                + "\r\n"
                + fileContent + "\r\n"
                + boundary + "--\r\n";

        final RequestParser.RequestInfo info = parse("POST /upload HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "Content-Type: multipart/form-data; boundary=" + boundary.substring(2) + "\r\n"
                + "\r\n"
                + body);

        Assertions.assertNotNull(info);
        assertEquals("POST", info.httpCommand());
        assertEquals("/upload", info.uri());

        final String rawContent = new String(info.content(), StandardCharsets.UTF_8);
        assertTrue(rawContent.contains("PlusAgent"), "Should contain PlusAgent class name");
        assertTrue(rawContent.contains("IncAgent"), "Should contain IncAgent class name");
        assertTrue(rawContent.contains("A,B"), "Should contain input topics");
    }
}
