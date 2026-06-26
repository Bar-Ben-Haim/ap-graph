package project_biu.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @Test
    void sanitizeFileName_StripsDirectoryTraversalAndKeepsOnlyTheName() {
        assertEquals("evil.conf", FileUtils.sanitizeFileName("../../etc/evil.conf", "d.conf"));
        assertEquals("c.conf", FileUtils.sanitizeFileName("C:\\tmp\\c.conf", "d.conf"));
    }

    @Test
    void sanitizeFileName_ReplacesIllegalCharactersWithUnderscore() {
        assertEquals("a_b.conf", FileUtils.sanitizeFileName("a b.conf", "d.conf"));
    }

    @Test
    void sanitizeFileName_DropsLeadingDotsToBlockHiddenFiles() {
        assertEquals("bashrc", FileUtils.sanitizeFileName("...bashrc", "d.conf"));
    }

    @Test
    void sanitizeFileName_UsesFallbackWhenNothingUsableRemains() {
        assertEquals("d.conf", FileUtils.sanitizeFileName(null, "d.conf"));
        assertEquals("d.conf", FileUtils.sanitizeFileName("///", "d.conf"));
    }

    @Test
    void containsScript_FlagsMarkupScriptHandlersAndJavascriptUris() {
        assertTrue(FileUtils.containsScript("<script>alert(1)</script>"));
        assertTrue(FileUtils.containsScript("javascript:doEvil()"));
        assertTrue(FileUtils.containsScript("<div onclick=x>"));
        assertTrue(FileUtils.containsScript("<anytag>"));
    }

    @Test
    void containsScript_AllowsPlainConfigText() {
        assertFalse(FileUtils.containsScript("project_biu.configs.agents.PlusAgent\nA,B\nC"));
        assertFalse(FileUtils.containsScript(null));
    }

    @Test
    void readFileContent_ReturnsTextForFilesAndEmptyForMissingOrDirectories(@TempDir Path dir) throws IOException {
        final Path file = dir.resolve("x.txt");
        Files.writeString(file, "hello");

        assertEquals(Optional.of("hello"), FileUtils.readFileContent(file));
        assertEquals(Optional.empty(), FileUtils.readFileContent(dir.resolve("missing.txt")));
        assertEquals(Optional.empty(), FileUtils.readFileContent(dir));
    }
}
