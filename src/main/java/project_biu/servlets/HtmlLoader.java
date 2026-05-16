package project_biu.servlets;

import project_biu.server.RequestParser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlLoader implements Servlet {
    private final String htmlFilesPath;

    public HtmlLoader(String htmlFilesPath) {
        this.htmlFilesPath = htmlFilesPath;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        String[] segments = ri.uriSegments();
        if (segments.length == 0)
            return;

        String filename = segments[segments.length - 1];

        // Use the configured path, but add a fallback to src/main/java if run from
        // project root
        Path filePath = Paths.get(htmlFilesPath, filename);
        if (!Files.exists(filePath)) {
            filePath = Paths.get("src", "main", "java", htmlFilesPath, filename);
        }

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            final byte[] content = Files.readAllBytes(filePath);
            final String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + content.length + "\r\n" +
                    "Connection: close\r\n\r\n";
            toClient.write(response.getBytes());
            toClient.write(content);
        } else {
            final String notFoundHtml = "<html><body><h1>404 File Not Found</h1></body></html>";
            final String response = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + notFoundHtml.length() + "\r\n" +
                    "Connection: close\r\n\r\n" +
                    notFoundHtml;
            toClient.write(response.getBytes());
        }
    }

    @Override
    public void close() throws IOException {
    }
}
