package project_biu.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import project_biu.graph.TopicManagerSingleton;
import project_biu.repository.LocalFilesRepository;
import project_biu.repository.LocalGraphRepository;
import project_biu.server.HTTPServer;
import project_biu.server.MyHTTPServer;
import project_biu.server.response.ResponseUtils;
import project_biu.service.GraphService;
import project_biu.servlets.ConfLoader;
import project_biu.servlets.GraphDisplayer;
import project_biu.servlets.TopicDisplayer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EndToEndHttpTest {
    private HTTPServer server;
    private GraphService graphService;
    private int port;

    @BeforeEach
    void setUp(@TempDir Path dir) throws Exception {
        TopicManagerSingleton.get().clear();
        port = freePort();
        final ResponseUtils responseUtils = new ResponseUtils();
        graphService = new GraphService(new LocalGraphRepository(), new LocalFilesRepository(dir));
        server = new MyHTTPServer(port, 5);
        server.addServlet("GET", "/publish", new TopicDisplayer(responseUtils, graphService));
        server.addServlet("GET", "/graph", new GraphDisplayer(responseUtils, graphService));
        server.addServlet("POST", "/upload", new ConfLoader(responseUtils, graphService));
        server.start();
        Thread.sleep(200);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.close();
        graphService.deleteAll();
        TopicManagerSingleton.get().clear();
        Thread.sleep(1300);
    }

    private static int freePort() throws IOException {
        try (ServerSocket ss = new ServerSocket(0)) {
            return ss.getLocalPort();
        }
    }

    private String send(String request) throws IOException {
        try (Socket c = new Socket("localhost", port)) {
            c.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
            c.shutdownOutput();
            return new String(c.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void deployPublishTableTest() throws Exception {
        final String upload = generateMultipartFormData();

        final String response = send(upload);
        assertTrue(response.contains("id=\"graph\""), response);

        send("GET /publish?topic=A&message=4 HTTP/1.1\r\nHost: localhost\r\n\r\n\r\n");
        Thread.sleep(300);

        final String table = send("GET /publish HTTP/1.1\r\nHost: localhost\r\n\r\n\r\n");
        assertTrue(table.contains("200"), table);
        assertTrue(table.contains("<table"), table);
        assertTrue(table.contains("A") && table.contains("B"), table);
    }

    private static String generateMultipartFormData() {
        final String boundary = "----B";
        final String cfg = "project_biu.configs.agents.IncAgent\nA\nB";
        final String body = boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"e2e.conf\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n"
                + cfg + "\r\n" + boundary + "--\r\n";
        return "POST /upload HTTP/1.1\r\nHost: localhost\r\n"
                + "Content-Type: multipart/form-data; boundary=" + boundary.substring(2) + "\r\n\r\n"
                + body;
    }
}
