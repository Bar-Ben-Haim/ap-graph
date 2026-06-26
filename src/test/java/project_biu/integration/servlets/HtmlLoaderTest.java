package project_biu.integration.servlets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import project_biu.server.response.ResponseUtils;
import project_biu.servlets.HtmlLoader;
import project_biu.testutil.ServletHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlLoaderTest {

    @Test
    void loadHtmlTest(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("page.html"), "<html>hello</html>");
        final HtmlLoader servlet = new HtmlLoader(new ResponseUtils(), dir.toString());

        final String response = ServletHandler.handle("GET /app/page.html HTTP/1.1\nHost: x\n\n\n", servlet);

        assertTrue(response.contains("200"), response);
        assertTrue(response.contains("text/html"), response);
        assertTrue(response.contains("hello"), response);
    }

    @Test
    void missingHtmlTest(@TempDir Path dir) throws IOException {
        final HtmlLoader servlet = new HtmlLoader(new ResponseUtils(), dir.toString());
        final String response = ServletHandler.handle("GET /app/missing.html HTTP/1.1\nHost: x\n\n\n", servlet);

        assertTrue(response.contains("404"), response);
    }
}
