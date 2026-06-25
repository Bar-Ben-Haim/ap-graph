package project_biu.integration.servlets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import project_biu.graph.TopicManagerSingleton;
import project_biu.repository.LocalFilesRepository;
import project_biu.repository.LocalGraphRepository;
import project_biu.server.response.ResponseUtils;
import project_biu.service.GraphService;
import project_biu.servlets.ConfLoader;
import project_biu.testutil.ServletHandler;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfLoaderServletTest {
    private GraphService graphService;
    private ConfLoader servlet;

    @BeforeEach
    void setUp(@TempDir Path dir) throws IOException {
        TopicManagerSingleton.get().clear();
        graphService = new GraphService(new LocalGraphRepository(), new LocalFilesRepository(dir));
        servlet = new ConfLoader(new ResponseUtils(), graphService);
    }

    @AfterEach
    void tearDown() {
        graphService.deleteAll();
        TopicManagerSingleton.get().clear();
    }

    private static String uploadRequest(String filename, String configContent) {
        final String boundary = "----WebKitFormBoundaryABC123";
        final String body = boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n"
                + "\r\n"
                + configContent + "\r\n"
                + boundary + "--\r\n";
        return "POST /upload HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "Content-Type: multipart/form-data; boundary=" + boundary.substring(2) + "\r\n"
                + "\r\n"
                + body;
    }

    @Test
    void uploadGraphTest() throws IOException {
        final String response = ServletHandler.handle(
                uploadRequest("ok.conf", "project_biu.configs.agents.PlusAgent\nA,B\nC"), servlet);

        assertTrue(response.contains("200"), response);
        assertTrue(response.contains("id=\"graph\""), response);
        assertNotNull(graphService.get());
    }

    @Test
    void invalidConfUploadTest() throws IOException {
        final String resp = ServletHandler.handle(uploadRequest("bad.conf", "only.one.line"), servlet);
        assertTrue(resp.contains("400"), resp);
        assertTrue(resp.contains("INVALID_FORMAT"), resp);
        assertNull(graphService.get());
    }
}
