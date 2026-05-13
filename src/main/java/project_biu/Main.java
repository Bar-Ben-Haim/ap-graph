package project_biu;


import project_biu.server.HTTPServer;
import project_biu.server.MyHTTPServer;

public class Main {
    static void main() throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);
//        server.addServlet("GET", "/publish", new TopicDisplayer());
//        server.addServlet("POST", "/upload", new ConfLoader());
//        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
        server.start();
        System.in.read();
        server.close();
        System.out.println("done");
    }
}