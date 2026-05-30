package project_biu.servlets;

import project_biu.configs.Graph;
import project_biu.configs.Node;
import project_biu.graph.Message;
import project_biu.graph.TopicManagerSingleton;
import project_biu.repository.GraphRepository;
import project_biu.server.RequestParser;
import project_biu.server.reponse.ResponseUtils;

import java.io.IOException;
import java.io.OutputStream;

public class TopicDisplayer implements Servlet {
    private final ResponseUtils responseUtils;
    private final GraphRepository graphRepository;

    public TopicDisplayer(ResponseUtils responseUtils, GraphRepository graphRepository) {
        this.responseUtils = responseUtils;
        this.graphRepository = graphRepository;
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        final String topicName = ri.parameters().get("topic");
        final String messageVal = ri.parameters().get("message");

        if (topicName != null && messageVal != null && !topicName.isEmpty()) {
            TopicManagerSingleton.get().getTopic(topicName).publish(new Message(messageVal));
        }

        final StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
                .append("table { width: 100%; border-collapse: collapse; } ")
                .append("th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }")
                .append("th { background-color: #f4f4f4; }")
                .append("</style></head><body>")
                .append("<table><tr><th>Topic</th><th>Last Value</th></tr>");

        final Graph graph = graphRepository.get();
        if (graph != null) {
            for (Node node : graph) {
                if (node.getName().startsWith("T")) {
                    final String cleanName = node.getName().substring(1);
                    final String lastValue = (node.getMsg() != null) ? node.getMsg().asText : "";
                    html.append("<tr><td>").append(cleanName).append("</td><td>").append(lastValue).append("</td></tr>");
                }
            }

        }

        html.append("</table>")
                .append("<script>")
                .append("if (window.parent && window.parent.frames['graphFrame']) {")
                .append("    window.parent.frames['graphFrame'].location.href = '/graph?t=' + new Date().getTime();")
                .append("}")
                .append("</script>")
                .append("</body></html>");

        responseUtils.okHtml(toClient, html.toString());
    }

    @Override
    public void close() throws IOException {
        graphRepository.delete();
    }
}