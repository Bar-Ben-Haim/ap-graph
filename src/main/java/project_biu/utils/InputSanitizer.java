package project_biu.utils;

import java.util.regex.Pattern;

/**
 * Sanitizes untrusted input received from clients (uploaded file names and contents).
 *
 * <p>Two concerns are handled: turning an arbitrary upload name into a safe, path-free
 * file name, and detecting markup/script content that could lead to injection when the
 * stored text is later rendered in the browser.
 */
public final class InputSanitizer {
    private static final String DEFAULT_FILE_NAME = "config.conf";

    /** Anything outside this set is replaced in file names. */
    private static final Pattern ILLEGAL_FILE_NAME_CHARS = Pattern.compile("[^A-Za-z0-9._-]");
    private static final Pattern LEADING_DOTS = Pattern.compile("^\\.+");

    /** HTML tags, script blocks, "javascript:" URIs, and inline event handlers. */
    private static final Pattern SCRIPT_LIKE =
            Pattern.compile("(?i)<\\s*/?\\s*script\\b|javascript:|on\\w+\\s*=|<[^>]*>");

    private InputSanitizer() {
    }

    /**
     * Reduces an upload name to a safe file name: strips any directory part, replaces
     * unsafe characters with '_', and drops leading dots so it cannot become a hidden
     * file or a traversal sequence.
     */
    public static String sanitizeFileName(String rawName) {
        if (rawName == null) {
            return DEFAULT_FILE_NAME;
        }
        String name = rawName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        name = ILLEGAL_FILE_NAME_CHARS.matcher(name).replaceAll("_");
        name = LEADING_DOTS.matcher(name).replaceAll("");
        return name.isBlank() ? DEFAULT_FILE_NAME : name;
    }

    /** True if the text contains HTML/script-like content that must not be trusted. */
    public static boolean containsScript(String text) {
        return text != null && SCRIPT_LIKE.matcher(text).find();
    }
}
