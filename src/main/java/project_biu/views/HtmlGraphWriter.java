package project_biu.views;

import project_biu.configs.Graph;
import project_biu.configs.Node;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HtmlGraphWriter {
    private static final String[] GRAPH_HTML_SEARCH_PATHS = {
            "html_files/graph.html",
            "src/main/java/html_files/graph.html"
    };

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
            String html = loadTemplate();
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
     * Loads the static graph HTML template.
     *
     * @return template content.
     * @throws IOException if loading fails.
     */
    private static String loadTemplate() throws IOException {
        for (String path : GRAPH_HTML_SEARCH_PATHS) { // TODO: change 1 path and not a file search
            java.nio.file.Path p = java.nio.file.Paths.get(path);
            if (java.nio.file.Files.exists(p)) {
                return java.nio.file.Files.readString(p, StandardCharsets.UTF_8);
            }
        }
        // TODO: change to real exeption handling like spring with a new created exception!!
        throw new RuntimeException("graph.html template not found in: " + Arrays.toString(GRAPH_HTML_SEARCH_PATHS));
    }

    /**
     * Creates an HTML error page.
     *
     * @param e the exception.
     * @return error HTML.
     */
    private static List<String> createErrorHtml(Exception e) {
        final List<String> errorHtml = new ArrayList<>();
        errorHtml.add("<html><body>");
        errorHtml.add("<h1>Error creating graph HTML</h1>");
        errorHtml.add("<p>" + e.getMessage() + "</p>");
        errorHtml.add("</body></html>");
        return errorHtml;
    }
}