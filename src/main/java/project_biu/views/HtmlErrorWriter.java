package project_biu.views;

import project_biu.configs.ConfigError;
import project_biu.configs.ConfigException;
import project_biu.utils.FileUtils;
import project_biu.utils.ReplaceUntrustedChars;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Html helper class for displaying the error messages to the client in a pleasing way.
 */
public class HtmlErrorWriter {
    private static final Path ERROR_HTML_PATH = Path.of("html_files/error.html");

    private HtmlErrorWriter() {
    }

    /**
     * Generates HTML that visually represents the error.
     *
     * @param error The {@code ConfigError} instance representing the type of error to be displayed.
     * @return A string containing the HTML representation of the error message.
     */
    public static String getErrorHtml(ConfigError error) {
        return getErrorHtml(error, error.defaultMessage());
    }

    /**
     * Generates HTML that visually represents the error.
     *
     * @param error   The {@code ConfigError} instance representing the type of error to be displayed
     * @param message The detailed error message to be rendered in the HTML output.
     * @return A string containing the HTML representation of the error message, or a fallback HTML
     * with exception details in case of an error while generating the output.
     */
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