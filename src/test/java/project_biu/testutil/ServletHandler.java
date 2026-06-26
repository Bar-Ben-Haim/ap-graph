package project_biu.testutil;

import project_biu.server.RequestParser;
import project_biu.servlets.Servlet;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ServletHandler {
    private ServletHandler() {
    }

    public static String handle(String raw, Servlet servlet) throws IOException {
        final RequestParser.RequestInfo ri = RequestParser.parseRequest(new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)))));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        servlet.handle(ri, out);
        return out.toString(StandardCharsets.UTF_8);
    }
}