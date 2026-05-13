package project_biu.servlets;

import project_biu.server.RequestParser;

import java.io.IOException;
import java.io.OutputStream;

public class HtmlLoader implements Servlet {
    private final String htmlFilesPath;

    public HtmlLoader(String htmlFilesPath) {
        this.htmlFilesPath = htmlFilesPath;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
