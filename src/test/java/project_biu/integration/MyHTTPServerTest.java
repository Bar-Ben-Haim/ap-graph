package project_biu.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import project_biu.server.HTTPServer;
import project_biu.server.MyHTTPServer;
import project_biu.server.RequestParser;
import project_biu.servlets.Servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyHTTPServerTest {

    @AfterEach
    void waitForServerThreadsToStop() throws InterruptedException {
        for (int i = 0; i < 10 && countServerRunThreads() > 0; i++) {
            Thread.sleep(100);
        }
    }

    @Test
    void serverLifecycleTest() throws Exception {
        final int port = findFreePort();
        final int beforeCount = countServerRunThreads();

        final HTTPServer server = new MyHTTPServer(port, 5);
        server.addServlet("GET", "/publish", new SumServlet());
        server.start();

        Thread.sleep(300);
        final int afterStartCount = countServerRunThreads();
        assertEquals(beforeCount + 1, afterStartCount, "start() should add exactly one server thread");

        final String request = "GET /publish?a=10&b=4 HTTP/1.1\r\nHost: localhost\r\n\\r\n\\r\n";

        final String response;
        try (Socket client = new Socket("localhost", port)) {
            client.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
            client.shutdownOutput();

            final byte[] bytes = client.getInputStream().readAllBytes();
            response = new String(bytes, StandardCharsets.UTF_8);
        }

        assertTrue(response.contains("14"), "Expected servlet result in server response");

        server.close();
        Thread.sleep(2200);

        final int afterCloseCount = countServerRunThreads();
        assertEquals(beforeCount, afterCloseCount, "Server thread should be closed after close()");
    }

    @Test
    void longestPrefixMatchTest() throws Exception {
        final int port = findFreePort();
        final HTTPServer server = new MyHTTPServer(port, 3);
        server.addServlet("GET", "/app/", new FixedResponseServlet("APP"));
        server.addServlet("GET", "/download/app/", new FixedResponseServlet("DOWNLOAD_APP"));
        server.start();

        Thread.sleep(200);

        final String response = sendSimpleGet(port, "/download/app/users.html");
        assertTrue(response.contains("DOWNLOAD_APP"), "Expected longest-prefix servlet to handle request");

        server.close();
    }

    @Test
    void prefixMatchPublishAndAppTest() throws Exception {
        final int port = findFreePort();
        final HTTPServer server = new MyHTTPServer(port, 3);
        server.addServlet("GET", "/publish", new FixedResponseServlet("PUBLISH"));
        server.addServlet("GET", "/app/", new FixedResponseServlet("APP"));
        server.start();

        Thread.sleep(200);

        final String publishResponse = sendSimpleGet(port, "/publish?topic=A&message=5");
        final String appResponse = sendSimpleGet(port, "/app/main.html");
        assertTrue(publishResponse.contains("PUBLISH"));
        assertTrue(appResponse.contains("APP"));

        server.close();
    }

    @Test
    void commandSpecificRoutingTest() throws Exception {
        final int port = findFreePort();
        final AtomicInteger getCalls = new AtomicInteger(0);
        final AtomicInteger postCalls = new AtomicInteger(0);
        final HTTPServer server = new MyHTTPServer(port, 3);
        server.addServlet("GET", "/publish", new CounterServlet(getCalls, "GET"));
        server.addServlet("POST", "/publish", new CounterServlet(postCalls, "POST"));
        server.start();

        Thread.sleep(200);

        final String getResponse = sendRawRequest(port,
                "GET /publish HTTP/1.1\r\nHost: localhost\r\n\r\n\r\n");

        final String postResponse = sendRawRequest(port,
                "POST /publish HTTP/1.1\r\n Host: localhost\r\n\r\n\r\n");

        assertTrue(getResponse.contains("GET"));
        assertTrue(postResponse.contains("POST"));
        assertEquals(1, getCalls.get());
        assertEquals(1, postCalls.get());

        server.close();
    }

    @Test
    void noMatchingServletTest() throws Exception {
        final int port = findFreePort();
        final HTTPServer server = new MyHTTPServer(port, 2);
        server.addServlet("GET", "/app/", new FixedResponseServlet("APP"));
        server.start();

        Thread.sleep(200);

        final String response = sendSimpleGet(port, "/unknown/path");
        assertEquals("", response, "Expected empty response when no servlet matches");

        server.close();
    }

    private static String sendSimpleGet(int port, String uri) throws IOException {
        final String request = "GET " + uri + " HTTP/1.1\r\nHost: localhost\r\n\r\n\r\n";
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
        for (final Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            final Thread thread = entry.getKey();
            if (!thread.isAlive()) {
                continue;
            }
            final StackTraceElement[] stack = entry.getValue();
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
            final int a = Integer.parseInt(ri.parameters().getOrDefault("a", "0"));
            final int b = Integer.parseInt(ri.parameters().getOrDefault("b", "0"));
            final int sum = a + b;
            final String body = Integer.toString(sum);
            final String response = "HTTP/1.1 200 OK\r\n"
                    + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "\r\n"
                    + body;
            toClient.write(response.getBytes(StandardCharsets.UTF_8));
            toClient.flush();
        }

        @Override
        public void close() {
            // Nothing to close
        }
    }

    private record FixedResponseServlet(String body) implements Servlet {

        @Override
        public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
            final String response = "HTTP/1.1 200 OK\r\n"
                    + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "\r\n"
                    + body;
            toClient.write(response.getBytes(StandardCharsets.UTF_8));
            toClient.flush();
        }

        @Override
        public void close() {
            // Nothing to close
        }
    }

    private record CounterServlet(AtomicInteger counter, String body) implements Servlet {

        @Override
        public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
            counter.incrementAndGet();
            final String response = "HTTP/1.1 200 OK\r\n"
                    + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "\r\n"
                    + body;
            toClient.write(response.getBytes(StandardCharsets.UTF_8));
            toClient.flush();
        }

        @Override
        public void close() {
            // Nothing to close
        }
    }
}
