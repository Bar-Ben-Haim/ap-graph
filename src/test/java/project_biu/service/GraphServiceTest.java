package project_biu.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import project_biu.configs.ConfigError;
import project_biu.configs.ConfigException;
import project_biu.configs.Graph;
import project_biu.graph.TopicManagerSingleton;
import project_biu.repository.LocalFilesRepository;
import project_biu.repository.LocalGraphRepository;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("resource")
class GraphServiceTest {
    private GraphService graphService;

    @BeforeEach
    void setUp(@TempDir Path dir) throws IOException {
        TopicManagerSingleton.get().clear();
        graphService = new GraphService(new LocalGraphRepository(), new LocalFilesRepository(dir));
    }

    @AfterEach
    void tearDown() {
        graphService.deleteAll();
        TopicManagerSingleton.get().clear();
    }

    @Test
    void validConfigIsDeployed() {
        final Graph g = graphService.deploy("ok.conf", "project_biu.configs.agents.PlusAgent\nA,B\nC\n");
        assertNotNull(g);
        assertFalse(g.isEmpty());
        assertSame(g, graphService.get(), "the deployed graph should be the one the service hands back");
    }

    @Test
    void rejectsConfigsWithScriptTags() {
        final ConfigException ex = assertThrows(ConfigException.class,
                () -> graphService.deploy("x.conf", "<script>alert(1)</script>"));

        assertEquals(ConfigError.UNSAFE_CONTENT, ex.getError());
    }

    @Test
    void invalidConfigTest() {
        final ConfigException ex = assertThrows(ConfigException.class,
                () -> graphService.deploy("x.conf", "only.one.line\n"));

        assertEquals(ConfigError.INVALID_FORMAT, ex.getError());
    }

    @Test
    void unknownAgentTest() {
        final ConfigException ex = assertThrows(ConfigException.class,
                () -> graphService.deploy("x.conf", "com.does.NotExist\nA\nB\n"));

        assertEquals(ConfigError.UNKNOWN_AGENT, ex.getError());
    }

    @Test
    void cyclicConfigTest() {
        final ConfigException ex = assertThrows(ConfigException.class,
                () -> graphService.deploy("x.conf", "project_biu.configs.agents.PlusAgent\nA,C\nC\n"));

        assertEquals(ConfigError.CYCLES_DETECTED, ex.getError());
    }

    @Test
    void resettingBeforeDeployedTest() {
        final ConfigException ex = assertThrows(ConfigException.class, () -> graphService.reset());

        assertEquals(ConfigError.NOT_LOADED, ex.getError());
    }

    @Test
    void resetTest() {
        graphService.deploy("ok.conf", "project_biu.configs.agents.PlusAgent\nA,B\nC\n");
        final Graph rebuilt = graphService.reset();

        assertNotNull(rebuilt);
        assertSame(rebuilt, graphService.get());
    }

    @Test
    void deleteAllTest() {
        graphService.deploy("ok.conf", "project_biu.configs.agents.PlusAgent\nA,B\nC\n");
        graphService.deleteAll();

        assertNull(graphService.get());
    }
}
