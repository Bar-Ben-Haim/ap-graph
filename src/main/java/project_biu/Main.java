package project_biu;

import project_biu.server.HTTPServer;
import project_biu.server.MyHTTPServer;
import project_biu.server.reponse.ResponseUtils;
import project_biu.servlets.ConfLoader;
import project_biu.servlets.HtmlLoader;
import project_biu.servlets.TopicDisplayer;

public class Main {
    static void main() throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);
        final ResponseUtils responseUtils = new ResponseUtils();

        server.addServlet("GET", "/publish", new TopicDisplayer(responseUtils));
        server.addServlet("POST", "/upload", new ConfLoader(responseUtils));
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));

        server.start();
        System.in.read();
        server.close();
        System.out.println("done"); //TODO: change to real logger!!!!!!!!
    }
}