package project_biu.views;

import project_biu.configs.ConfigError;
import project_biu.configs.ConfigException;
import project_biu.utils.FileUtils;
import project_biu.utils.ReplaceUntrustedChars;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class HtmlErrorWriter {
    private static final Path ERROR_HTML_PATH = Path.of("html_files/error.html");

    private HtmlErrorWriter() {
    }

    public static String getErrorHtml(ConfigError error) {
        return getErrorHtml(error, error.defaultMessage());
    }

    public static String getErrorHtml(ConfigError error, String message) {
        try {
            String html = FileUtils.readFileContent(ERROR_HTML_PATH).orElseThrow(() ->
                    new ConfigException(ConfigError.INTERNAL_ERROR, "Could not load HTML file: " + ERROR_HTML_PATH));

            html = html.replace("__ERROR_TYPE__", ReplaceUntrustedChars.html(error.name()));
            html = html.replace("__ERROR_SEVERITY__", error.severity().name().toLowerCase());
            html = html.replace("__ERROR_MESSAGE__", ReplaceUntrustedChars.html(message));
            return html;
        } catch (IOException | RuntimeException e) {
            return "<html><body><h2>" + ReplaceUntrustedChars.html(e.getClass().getSimpleName())
                    + "</h2><p>" + ReplaceUntrustedChars.html(e.getMessage()) + "</p></body></html>";
        }
    }

    /**
     * Creates an HTML error page.
     *
     * @param e the exception.
     * @return error HTML.
     */
    public static List<String> createErrorHtml(Exception e) {
        return List.of(getErrorHtml(ConfigError.RENDER_ERROR,
                e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
    }
}