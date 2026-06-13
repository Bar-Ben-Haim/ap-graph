package project_biu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.repository.FileRepository;
import project_biu.repository.GraphRepository;
import project_biu.repository.LocalFileRepository;
import project_biu.repository.LocalGraphRepository;
import project_biu.server.HTTPServer;
import project_biu.server.MyHTTPServer;
import project_biu.server.response.ResponseUtils;
import project_biu.service.GraphService;
import project_biu.servlets.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    static void main() throws Exception {
        final HTTPServer server = new MyHTTPServer(8080, 5);
        final ResponseUtils responseUtils = new ResponseUtils();
        final GraphRepository graphRepository = new LocalGraphRepository();
        final FileRepository fileRepository = new LocalFileRepository(Path.of("uploaded_configs"));
        final GraphService graphService = new GraphService(graphRepository, fileRepository);
        fileRepository.deleteAll();

        server.addServlet("GET", "/publish", new TopicDisplayer(responseUtils, graphService));
        server.addServlet("GET", "/graph", new GraphDisplayer(responseUtils, graphService));
        server.addServlet("POST", "/upload", new ConfLoader(responseUtils, graphService));
        server.addServlet("POST", "/reset", new ConfigReset(responseUtils, graphService));
        server.addServlet("DELETE", "/delete", new ConfigDelete(responseUtils, graphService));
        server.addServlet("GET", "/app/", new HtmlLoader(responseUtils, "html_files"));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> closeResources(server, graphService), "server-shutdown"));
        server.start();
        awaitShutdown();
        closeResources(server, graphService);
        LOGGER.info("done");
    }

    private static void closeResources(HTTPServer httpServer, GraphService graphService) {
        httpServer.close();
        graphService.deleteAll();
    }

    /**
     * Waits for a key pressed (except -1) to shut down the server.
     * {@code CountDownLatch(1)} is used to prevent instant stop when running in a containerized environment.
     *
     * @throws IOException          if the input stream cannot be read
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    private static void awaitShutdown() throws IOException, InterruptedException {
        int result = System.in.read();
        if (result == -1) {
            new CountDownLatch(1).await();
        }

    }
}