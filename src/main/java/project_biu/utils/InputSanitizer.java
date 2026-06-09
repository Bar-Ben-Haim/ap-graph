package project_biu.utils;

import java.util.regex.Pattern;

/**
 * Sanitizes untrusted input received from clients (uploaded file names and contents).
 */
public final class InputSanitizer {
    private static final Pattern ILLEGAL_FILE_NAME_CHARS = Pattern.compile("[^A-Za-z0-9._-]");
    private static final Pattern LEADING_DOTS = Pattern.compile("^\\.+");
    private static final Pattern SCRIPT_LIKE =
            Pattern.compile("(?i)<\\s*/?\\s*script\\b|javascript:|on\\w+\\s*=|<[^>]*>");

    private InputSanitizer() {
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
     * Checks if the given text contains HTML tags, script blocks, "javascript:" URIs, or inline event handlers.
     *
     * @param text the text to check
     * @return true if the text contains script-like content, false otherwise
     */
    public static boolean containsScript(String text) {
        return text != null && SCRIPT_LIKE.matcher(text).find();
    }
}