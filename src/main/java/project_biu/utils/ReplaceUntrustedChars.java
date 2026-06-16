package project_biu.utils;

/**
 * Replaces untrusted text at output (with the same safe values):
 * {@code html()} for HTML content, {@code js()} for single-quoted JS strings.
 */
public final class ReplaceUntrustedChars {
    private ReplaceUntrustedChars() {
    }

    /**
     * Escapes special characters in a given text to prevent HTML injection.
     *
     * @param text The input text to be sanitized; if null, an empty string is returned.
     * @return A sanitized string with special characters replaced by their HTML entity equivalents.
     */
    public static String html(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Escapes special characters in a given text to prevent JavaScript injection.
     *
     * @param text The input text to be sanitized; if null, an empty string is returned.
     * @return A sanitized string with special characters replaced by their escaped equivalents for safe JavaScript.
     */
    public static String js(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace(String.valueOf((char) 0x2028), "\\u2028")
                .replace(String.valueOf((char) 0x2029), "\\u2029")
                .replace("<", "\\u003C")
                .replace(">", "\\u003E");
    }
}
