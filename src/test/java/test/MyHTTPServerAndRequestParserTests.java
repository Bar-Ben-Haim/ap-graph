package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import project_biu.server.HTTPServer;
import project_biu.server.MyHTTPServer;
import project_biu.server.RequestParser;
import project_biu.servlets.Servlet;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MyHTTPServerAndRequestParserTests {

    @Test
    void parseRequest_withQueryMetadataAndBody_extractsAllFields() throws IOException {
        final String request = "GET /api/sum?a=2&b=5 HTTP/1.1\nHost: localhost\n\nfilename=result.txt\n\npayload-line\n\n";

        BufferedReader input = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8))));
        RequestParser.RequestInfo info = RequestParser.parseRequest(input);

        Assertions.assertNotNull(info);
        assertEquals("GET", info.httpCommand());
        assertEquals("/api/sum?a=2&b=5", info.uri());
        assertArrayEquals(new String[]{"api", "sum"}, info.uriSegments());

        Map<String, String> expectedParameters = new HashMap<>();
        expectedParameters.put("a", "2");
        expectedParameters.put("b", "5");
        expectedParameters.put("filename", "result.txt");
        assertEquals(expectedParameters, info.parameters());
        assertArrayEquals("payload-line\n".getBytes(StandardCharsets.UTF_8), info.content());
    }

    @Test
    void parseRequest_withoutQuery_keepsUriAndSegments() throws IOException {
        final String request = "GET /publish HTTP/1.1\nHost: localhost\n\n\n";

        BufferedReader input = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8))));
        RequestParser.RequestInfo info = RequestParser.parseRequest(input);

        Assertions.assertNotNull(info);
        assertEquals("GET", info.httpCommand());
        assertEquals("/publish", info.uri());
        assertArrayEquals(new String[]{"publish"}, info.uriSegments());
    }

    @Test
    void parseRequest_withoutQuery_withMetadata_shouldStillParseParameters() throws IOException {
        final String request = "GET /publish HTTP/1.1\nHost: localhost\n\nx=7\n\n\n";

        BufferedReader input = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8))));
        RequestParser.RequestInfo info = RequestParser.parseRequest(input);
        Assertions.assertNotNull(info);
        assertEquals("7", info.parameters().get("x"));
    }

    @Test
    void myHttpServer_servletFlow_andThreadLifecycle() throws Exception {
        int port = findFreePort();
        int beforeCount = countServerRunThreads();

        HTTPServer server = new MyHTTPServer(port, 5);
        server.addServlet("GET", "/publish", new SumServlet());
        server.start();

        Thread.sleep(300);
        int afterStartCount = countServerRunThreads();
        assertEquals(beforeCount + 1, afterStartCount, "start() should add exactly one server thread");

        final String request = "GET /publish?a=10&b=4 HTTP/1.1\r\nHost: localhost\r\n\\r\n\\r\n";

        String response;
        try (Socket client = new Socket("localhost", port)) {
            client.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
            client.shutdownOutput();

            byte[] bytes = client.getInputStream().readAllBytes();
            response = new String(bytes, StandardCharsets.UTF_8);
        }

        assertTrue(response.contains("14"), "Expected servlet result in server response");

        server.close();
        Thread.sleep(2200);

        int afterCloseCount = countServerRunThreads();
        assertEquals(beforeCount, afterCloseCount, "Server thread should be closed after close()");
    }

    @Test
    void myHttpServer_longestPrefixMatch_prefersMoreSpecificServlet() throws Exception {
        int port = findFreePort();
        HTTPServer server = new MyHTTPServer(port, 3);
        server.addServlet("GET", "/app/", new FixedResponseServlet("APP"));
        server.addServlet("GET", "/download/app/", new FixedResponseServlet("DOWNLOAD_APP"));
        server.start();

        Thread.sleep(200);

        String response = sendSimpleGet(port, "/download/app/users.html");
        assertTrue(response.contains("DOWNLOAD_APP"), "Expected longest-prefix servlet to handle request");

        server.close();
        Thread.sleep(1200);
    }

    @Test
    void myHttpServer_longestPrefixMatch_forPublishAndAppExamples() throws Exception {
        int port = findFreePort();
        HTTPServer server = new MyHTTPServer(port, 3);
        server.addServlet("GET", "/publish", new FixedResponseServlet("PUBLISH"));
        server.addServlet("GET", "/app/", new FixedResponseServlet("APP"));
        server.start();

        Thread.sleep(200);

        String publishResponse = sendSimpleGet(port, "/publish?topic=A&message=5");
        String appResponse = sendSimpleGet(port, "/app/main.html");
        assertTrue(publishResponse.contains("PUBLISH"));
        assertTrue(appResponse.contains("APP"));

        server.close();
        Thread.sleep(1200);
    }

    @Test
    void myHttpServer_commandSpecificMap_doesNotCrossBetweenGetAndPost() throws Exception {
        int port = findFreePort();
        AtomicInteger getCalls = new AtomicInteger(0);
        AtomicInteger postCalls = new AtomicInteger(0);
        HTTPServer server = new MyHTTPServer(port, 3);
        server.addServlet("GET", "/publish", new CounterServlet(getCalls, "GET"));
        server.addServlet("POST", "/publish", new CounterServlet(postCalls, "POST"));
        server.start();

        Thread.sleep(200);

        String getResponse = sendRawRequest(port,
                "GET /publish HTTP/1.1\r\nHost: localhost\r\n\r\n\r\n");

        String postResponse = sendRawRequest(port,
                "POST /publish HTTP/1.1\r\n Host: localhost\r\n\r\n\r\n");

        assertTrue(getResponse.contains("GET"));
        assertTrue(postResponse.contains("POST"));
        assertEquals(1, getCalls.get());
        assertEquals(1, postCalls.get());

        server.close();
        Thread.sleep(1200);
    }

    @Test
    void myHttpServer_noMatchingServlet_returnsNoBody() throws Exception {
        int port = findFreePort();
        HTTPServer server = new MyHTTPServer(port, 2);
        server.addServlet("GET", "/app/", new FixedResponseServlet("APP"));
        server.start();

        Thread.sleep(200);

        String response = sendSimpleGet(port, "/unknown/path");
        assertEquals("", response, "Expected empty response when no servlet matches");

        server.close();
        Thread.sleep(1200);
    }

    private static String sendSimpleGet(int port, String uri) throws IOException {
        String request = "GET " + uri + " HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "\r\n"
                + "\r\n";
        return sendRawRequest(port, request);
    }

    private static String sendRawRequest(int port, String request) throws IOException {
        try (Socket client = new Socket("localhost", port)) {
            client.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
            client.shutdownOutput();
            return new String(client.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static int countServerRunThreads() {
        int count = 0;
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            Thread thread = entry.getKey();
            if (!thread.isAlive()) {
                continue;
            }
            StackTraceElement[] stack = entry.getValue();
            if (Arrays.stream(stack).anyMatch(frame ->
                    "project_biu.server.MyHTTPServer".equals(frame.getClassName()) && "run".equals(frame.getMethodName()))) {
                count++;
            }
        }
        return count;
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket ss = new ServerSocket(0)) {
            return ss.getLocalPort();
        }
    }

    private static class SumServlet implements Servlet {
        @Override
        public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
            int a = Integer.parseInt(ri.parameters().getOrDefault("a", "0"));
            int b = Integer.parseInt(ri.parameters().getOrDefault("b", "0"));
            int sum = a + b;
            String body = Integer.toString(sum);
            String response = "HTTP/1.1 200 OK\r\n"
                    + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "\r\n"
                    + body;
            toClient.write(response.getBytes(StandardCharsets.UTF_8));
            toClient.flush();
        }

        @Override
        public void close() {
        }
    }

    private static class FixedResponseServlet implements Servlet {
        private final String body;

        private FixedResponseServlet(String body) {
            this.body = body;
        }

        @Override
        public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
            String response = "HTTP/1.1 200 OK\r\n"
                    + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "\r\n"
                    + body;
            toClient.write(response.getBytes(StandardCharsets.UTF_8));
            toClient.flush();
        }

        @Override
        public void close() {
        }
    }

    private static class CounterServlet implements Servlet {
        private final AtomicInteger counter;
        private final String body;

        private CounterServlet(AtomicInteger counter, String body) {
            this.counter = counter;
            this.body = body;
        }

        @Override
        public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
            counter.incrementAndGet();
            String response = "HTTP/1.1 200 OK\r\n"
                    + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "\r\n"
                    + body;
            toClient.write(response.getBytes(StandardCharsets.UTF_8));
            toClient.flush();
        }

        @Override
        public void close() {
        }
    }
}
