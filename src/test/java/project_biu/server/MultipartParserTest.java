package project_biu.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultipartParserTest {

    private static String generateMultiPartMock(String boundaryNoDashes, String filename, String content) {
        final String b = "--" + boundaryNoDashes;
        return b + "\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\n"
                + "Content-Type: application/octet-stream\n"
                + "\n"
                + content + "\n"
                + b + "--\n";
    }

    @Test
    void extractsFilenameAndContentFromMultipartBody() {
        final String raw = generateMultiPartMock("XBOUND",
                "simple1.conf",
                "project_biu.configs.agents.PlusAgent\nA,B\nC");
        final MultipartParser.MultipartResult r = MultipartParser.parseMultipart(raw, ".conf");

        assertNotNull(r);
        assertEquals("simple1.conf", r.filename());
        assertTrue(r.content().contains("PlusAgent"));
        assertTrue(r.content().contains("A,B"));
        assertFalse(r.content().contains("Content-Disposition"), "part headers must be stripped");
        assertFalse(r.content().contains("XBOUND"), "boundary must be stripped");
    }

    @Test
    void fileNameNotPresentInBody() {
        final String raw = "--B\nContent-Disposition: form-data; name=\"file\"\n\ndata\n--B--\n";
        final MultipartParser.MultipartResult r = MultipartParser.parseMultipart(raw, ".conf");

        assertNotNull(r);
        assertTrue(r.filename().endsWith(".conf"));
        assertEquals("data", r.content());
    }
}
