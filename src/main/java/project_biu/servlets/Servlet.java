package project_biu.servlets;

import project_biu.server.RequestParser.RequestInfo;

import java.io.IOException;
import java.io.OutputStream;

public interface Servlet {
    void handle(RequestInfo ri, OutputStream toClient) throws IOException;

    void close() throws IOException;
}