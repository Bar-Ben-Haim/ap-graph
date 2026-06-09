package project_biu.utils;

/**
 * Replaces untrusted text at output (with the same safe values):
 * {@code html()} for HTML content, {@code js()} for single-quoted JS strings.
 */
public final class ReplaceUntrustedChars {
    private ReplaceUntrustedChars() {
    }

    public static String html(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

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
