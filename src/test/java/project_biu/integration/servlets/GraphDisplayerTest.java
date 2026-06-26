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
import project_biu.servlets.GraphDisplayer;
import project_biu.testutil.ServletHandler;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphDisplayerTest {
    private GraphService graphService;
    private GraphDisplayer servlet;

    @BeforeEach
    void setUp(@TempDir Path dir) throws IOException {
        TopicManagerSingleton.get().clear();
        graphService = new GraphService(new LocalGraphRepository(), new LocalFilesRepository(dir));
        servlet = new GraphDisplayer(new ResponseUtils(), graphService);
    }

    @AfterEach
    void tearDown() {
        graphService.deleteAll();
        TopicManagerSingleton.get().clear();
    }

    @Test
    void notLoadedGraphTest() throws IOException {
        final String resp = ServletHandler.handle("GET /graph HTTP/1.1\nHost: x\n\n\n", servlet);

        assertTrue(resp.contains("200"), resp);
        assertTrue(resp.contains("NOT_LOADED"), resp);
    }

    @Test
    void displayGraphTest() throws IOException {
        graphService.deploy("ok.conf", "project_biu.configs.agents.PlusAgent\nA,B\nC\n");
        final String resp = ServletHandler.handle("GET /graph HTTP/1.1\nHost: x\n\n\n", servlet);

        assertTrue(resp.contains("200"), resp);
        assertTrue(resp.contains("id=\"graph\""), resp);
    }
}
