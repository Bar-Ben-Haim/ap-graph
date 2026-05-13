package project_biu.views;

import org.apache.commons.io.IOUtils;
import project_biu.configs.Graph;
import project_biu.configs.Node;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HtmlGraphWriter {
    private static final String GRAPH_HTML_PATH = "files_html/graph.html";

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

    /**
     * Builds all graph edges in JavaScript format.
     *
     * @param graph the graph.
     * @return edges JavaScript string.
     */
    private static String buildEdges(Graph graph) {

        StringBuilder builder = new StringBuilder();

        for (Node node : graph) {

            for (Node edge : node.getEdges()) {

                builder.append("{")
                        .append("from: '")
                        .append(node.getName())
                        .append("', ")

                        .append("to: '")
                        .append(edge.getName())
                        .append("'")
                        .append("},\n");
            }
        }

        return builder.toString();
    }

    /**
     * Appends a topic node.
     *
     * @param builder target builder.
     * @param node    topic node.
     */
    private static void appendTopicNode(StringBuilder builder, Node node) {
        builder.append("{")
                .append("id: '")
                .append(node.getName()).append("', ")
                .append("label: '")
                .append(removePrefix(node.getName()))
                .append("', ")
                .append("shape: 'box', ")
                .append("color: '#007bff'")
                .append("},\n");
    }

    /**
     * Appends an agent node.
     *
     * @param builder target builder.
     * @param node    agent node.
     */
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
        final InputStream inputStream = HtmlGraphWriter.class
                .getClassLoader()
                .getResourceAsStream(GRAPH_HTML_PATH);

        if (inputStream == null) {
            throw new RuntimeException("graph.html template not found");
        }
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
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