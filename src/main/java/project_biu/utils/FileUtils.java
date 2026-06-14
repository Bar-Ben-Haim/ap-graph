package project_biu.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A File Utilization class to help read and write files.
 * <p>
 * For saving files: <p>
 * Sanitizes untrusted input received from clients. <p>
 * Also provides a helper method to read the content of a file as a string. <p>
 */
public final class FileUtils {
    private static final Pattern ILLEGAL_FILE_NAME_CHARS = Pattern.compile("[^A-Za-z0-9._-]");
    private static final Pattern LEADING_DOTS = Pattern.compile("^\\.+");
    private static final Pattern SCRIPT_LIKE =
            Pattern.compile("(?i)<\\s*/?\\s*script\\b|javascript:|on\\w+\\s*=|<[^>]*>");

    private FileUtils() {
    }

    /**
     * Sanitizes a file name.
     *
     * @param rawName     the raw file name
     * @param defaultName the default file name if the raw name is null or empty
     * @return the sanitized file name
     */
    public static String sanitizeFileName(String rawName, String defaultName) {
        if (rawName == null) {
            return defaultName;
        }
        String name = rawName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        name = ILLEGAL_FILE_NAME_CHARS.matcher(name).replaceAll("_");
        name = LEADING_DOTS.matcher(name).replaceAll("");
        return name.isBlank() ? defaultName : name;
    }

    /**
     * Reads all the content of a file as a string.
     *
     * @param path the path to the file
     * @return the content of the file as a string, or an empty optional if the file does not exist or is a directory
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static Optional<String> readFileContent(Path path) throws IOException {
        if (Files.exists(path) && !Files.isDirectory(path)) {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        }
        return Optional.empty();
    }

    /**
     * Checks if the given text contains HTML tags, script blocks, "javascript:" URIs, or inline event handlers.
     *
     * @param text the text to check
     * @return true if the text contains script-like content, false otherwise
     */
    public static boolean containsScript(String text) {
        return text != null && SCRIPT_LIKE.matcher(text).find();
    }
}