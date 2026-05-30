package project_biu.views;

import project_biu.configs.Graph;
import project_biu.configs.Node;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class HtmlGraphWriter {
    private static final Path GRAPH_HTML_SEARCH_PATH = Path.of("html_files/graph.html");

    private static final Path ERROR_HTML_SEARCH_PATH = Path.of("html_files/error.html");

    private HtmlGraphWriter() {
    }

    /**
     * Generates HTML that visually represents the computational graph.
     *
     * @param graph the graph to render.
     * @return list containing the generated HTML page.
     */
    public static List<String> getGraphHTML(Graph graph) {
        try {
            String html = loadHtmlFile(GRAPH_HTML_SEARCH_PATH);
            final String nodesHtml = buildNodes(graph);
            final String edgesHtml = buildEdges(graph);
            html = html.replace("//__NODES__", nodesHtml);
            html = html.replace("//__EDGES__", edgesHtml);
            return List.of(html);
        } catch (Exception e) {
            return createErrorHtml(e);
        }
    }

    private static String buildNodes(Graph graph) {
        final StringBuilder builder = new StringBuilder();
        graph.forEach(node -> {
            if (node.getName().startsWith("T")) appendTopicNode(builder, node);
            else if (node.getName().startsWith("A")) appendAgentNode(builder, node);
        });

        return builder.toString();
    }

    private static String buildEdges(Graph graph) {
        final StringBuilder builder = new StringBuilder();
        graph.forEach(node -> node.getEdges().forEach(edge -> builder
                .append("{")
                .append("from: '")
                .append(node.getName())
                .append("', ")
                .append("to: '")
                .append(edge.getName())
                .append("'")
                .append("},\n")));

        return builder.toString();
    }

    private static void appendTopicNode(StringBuilder builder, Node node) {
        final String name = removePrefix(node.getName());
        final String valueStr = (node.getMsg() != null) ? "\\n" + node.getMsg().asText : "";

        builder.append("{")
                .append("id: '").append(node.getName()).append("', ")
                .append("label: '").append(name).append(valueStr).append("', ")
                .append("shape: 'box', ")
                .append("color: '#007bff'")
                .append("},\n");
    }

    private static void appendAgentNode(StringBuilder builder, Node node) {
        builder.append("{")
                .append("id: '")
                .append(node.getName())
                .append("', ")
                .append("label: '")
                .append(removePrefix(node.getName()))
                .append("', ")
                .append("shape: 'circle', ")
                .append("color: '#28a745'")
                .append("},\n");
    }

    /**
     * Removes the type prefix from the node name.
     * <p>
     * Example:
     * TA -> A
     * APlusAgent -> PlusAgent
     *
     * @param name original name.
     * @return cleaned display name.
     */
    private static String removePrefix(String name) {
        if (name == null || name.length() <= 1) return name;
        return name.substring(1);
    }

    /**
     * Generates an error page HTML by injecting the error type and message into error.html.
     *
     * @param errorType    one of: NOT_LOADED, CYCLES_DETECTED, RENDER_ERROR, INTERNAL_ERROR
     * @param errorMessage human-readable description of the error
     * @return rendered error HTML string
     */
    public static String getErrorHtml(String errorType, String errorMessage) {
        try {
            String html = loadHtmlFile(ERROR_HTML_SEARCH_PATH);
            html = html.replace("__ERROR_TYPE__", errorType);
            html = html.replace("__ERROR_MESSAGE__", errorMessage);
            return html;
        } catch (IOException | RuntimeException e) {
            return "<html><body><h2>" + e.getClass() + "</h2><p>" + e.getMessage() + "</p></body></html>";
        }
    }


    /**
     * Loads the static graph HTML template.
     *
     * @return template content.
     * @throws IOException if loading fails.
     */
    private static String loadHtmlFile(Path filePath) throws IOException {
        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        }
        //TODO: change to logging and return normal html
        throw new RuntimeException("HTML template not found in or is a directory: " + filePath);
    }

    /**
     * Creates an HTML error page.
     *
     * @param e the exception.
     * @return error HTML.
     */
    private static List<String> createErrorHtml(Exception e) {
        return List.of(getErrorHtml("RENDER_ERROR", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
    }
}