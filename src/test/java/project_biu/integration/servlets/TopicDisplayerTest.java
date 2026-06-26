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
import project_biu.servlets.TopicDisplayer;
import project_biu.testutil.ServletHandler;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TopicDisplayerTest {
    private GraphService graphService;
    private TopicDisplayer servlet;

    @BeforeEach
    void setUp(@TempDir Path dir) throws IOException {
        TopicManagerSingleton.get().clear();
        graphService = new GraphService(new LocalGraphRepository(), new LocalFilesRepository(dir));
        servlet = new TopicDisplayer(new ResponseUtils(), graphService);
    }

    @AfterEach
    void tearDown() {
        graphService.deleteAll();
        TopicManagerSingleton.get().clear();
    }

    @Test
    void topicDoesNotExistsTest() throws IOException {
        final String resp = ServletHandler.handle("GET /publish?topic=ghost&message=1 HTTP/1.1\nHost: x\n\n\n", servlet);

        assertTrue(resp.contains("404"), resp);
        assertTrue(resp.contains("does not exist"), resp);
    }

    @Test
    void publishToExistingTopicTest() throws IOException {
        TopicManagerSingleton.get().getTopic("A");
        final String resp = ServletHandler.handle("GET /publish?topic=A&message=5 HTTP/1.1\nHost: x\n\n\n", servlet);

        assertTrue(resp.contains("200"), resp);
        assertTrue(resp.contains("text/html"), resp);
        assertTrue(resp.contains("<table"), resp);
    }

    @Test
    void updateTableTest() throws IOException {
        final String resp = ServletHandler.handle("GET /publish HTTP/1.1\nHost: x\n\n\n", servlet);

        assertTrue(resp.contains("200"), resp);
        assertTrue(resp.contains("Topic"), resp);
    }
}
