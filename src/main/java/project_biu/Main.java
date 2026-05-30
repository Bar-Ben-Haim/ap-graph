package project_biu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project_biu.repository.GraphRepository;
import project_biu.repository.LocalGraphRepository;
import project_biu.server.HTTPServer;
import project_biu.server.MyHTTPServer;
import project_biu.server.reponse.ResponseUtils;
import project_biu.servlets.ConfLoader;
import project_biu.servlets.GraphDisplayer;
import project_biu.servlets.HtmlLoader;
import project_biu.servlets.TopicDisplayer;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    static void main() throws Exception {
        final HTTPServer server = new MyHTTPServer(8080, 5);
        final ResponseUtils responseUtils = new ResponseUtils();
        final GraphRepository graphRepository = new LocalGraphRepository();

        server.addServlet("GET", "/publish", new TopicDisplayer(responseUtils, graphRepository));
        server.addServlet("GET", "/graph", new GraphDisplayer(responseUtils, graphRepository));
        server.addServlet("POST", "/upload", new ConfLoader(responseUtils, graphRepository));
        server.addServlet("GET", "/app/", new HtmlLoader(responseUtils, "html_files"));

        server.start();
        //noinspection ResultOfMethodCallIgnored
        System.in.read();
        server.close();
        LOGGER.info("done");
    }
}