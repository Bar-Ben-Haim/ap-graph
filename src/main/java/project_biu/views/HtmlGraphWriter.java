package project_biu.views;

import project_biu.configs.ConfigError;
import project_biu.configs.ConfigException;
import project_biu.configs.Node;
import project_biu.graph.Message;
import project_biu.utils.FileUtils;
import project_biu.utils.NumberFormatter;
import project_biu.utils.ReplaceUntrustedChars;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class HtmlGraphWriter {
    private static final Path GRAPH_HTML_PATH = Path.of("html_files/graph.html");

    private HtmlGraphWriter() {
    }

    /**
     * Generates HTML that visually represents the computational graph.
     *
     * @param graph the graph to render.
     * @return list containing the generated HTML page.
     */
    public static List<String> getGraphHTML(List<Node> graph, List<String> formulas) {
        try {
            String html = FileUtils.readFileContent(GRAPH_HTML_PATH).orElseThrow(() ->
                    new ConfigException(ConfigError.INTERNAL_ERROR, "Could not load HTML file: " + GRAPH_HTML_PATH));
            html = html.replace("//__NODES__", buildNodes(graph));
            html = html.replace("//__EDGES__", buildEdges(graph));
            html = html.replace("//__FORMULAS__", buildFormulas(formulas));
            return List.of(html);
        } catch (RuntimeException | IOException e) {
            return HtmlErrorWriter.createErrorHtml(e);
        }
    }

    private static String buildNodes(List<Node> graph) {
        final StringBuilder builder = new StringBuilder();
        graph.forEach(node -> {
            if (node.getName().startsWith("T")) appendTopicNode(builder, node);
            else if (node.getName().startsWith("A")) appendAgentNode(builder, node);
        });

        return builder.toString();
    }

    private static String buildEdges(List<Node> graph) {
        final StringBuilder builder = new StringBuilder();
        graph.forEach(node -> node.getOutEdges().forEach(edge -> builder
                .append("{")
                .append("from: '")
                .append(node)
                .append("', ")
                .append("to: '")
                .append(edge)
                .append("'")
                .append("},\n")));

        return builder.toString();
    }

    private static void appendTopicNode(StringBuilder builder, Node node) {
        final String name = ReplaceUntrustedChars.js(removePrefix(node.getName()));
        final Message msg = node.getMsg();
        final String valueStr = msg != null ?
                "\\n" + ReplaceUntrustedChars.js(NumberFormatter.format(msg.asDouble, msg.asText)) :
                "";

        builder.append("{")
                .append("id: '").append(node).append("', ")
                .append("label: '").append(name)
                .append(valueStr).append("', ")
                .append("group: 'topic'")
                .append("},\n");
    }

    private static void appendAgentNode(StringBuilder builder, Node node) {
        String label = ReplaceUntrustedChars.js(removePrefix(node.getName()));
        if (!node.getMathematicalDescription().isEmpty())
            label += "\\n\\n" + ReplaceUntrustedChars.js(node.getMathematicalDescription().getFirst());

        builder.append("{")
                .append("id: '")
                .append(node)
                .append("', ")
                .append("label: '")
                .append(label)
                .append("', ")
                .append("group: 'agent'")
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
     * Builds the HTML for the formulas.
     *
     * @param formulas the formulas to display
     * @return the HTML for the formulas
     */
    private static String buildFormulas(List<String> formulas) {
        if (formulas.isEmpty()) return "<span style='color:#94a3b8'>None</span>";

        final StringBuilder builder = new StringBuilder();
        formulas.forEach(formula -> builder
                .append("<div class='formula-item'>")
                .append(ReplaceUntrustedChars.html(formula))
                .append("</div>\n"));
        return builder.toString();
    }
}