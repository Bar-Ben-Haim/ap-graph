package project_biu.server;

import project_biu.servlets.Servlet;

/**
 * Represents an HTTP server that can be used to handle HTTP requests
 * with specific servlets registered for different HTTP commands and URI paths.
 * <p>
 * The server enables adding/removing servlets for handling specific
 * HTTP commands (e.g., GET, POST, DELETE) and URI paths.
 */
public interface HTTPServer extends Runnable {
    /**
     * Adds a servlet to the server for handling requests with the specified HTTP command and URI.
     *
     * @param httpCommand The HTTP command (e.g., GET, POST, DELETE) that the servlet should handle.
     * @param uri         The URI path that the servlet should handle.
     * @param servlet     The servlet that handles the requests.
     */
    void addServlet(String httpCommand, String uri, Servlet servlet);

    /**
     * Removes the servlet associated with the specified HTTP command and URI.
     *
     * @param httpCommand The HTTP command (e.g., GET, POST, DELETE) that the servlet was serving.
     * @param uri         The URI path that the servlet was serving.
     */
    void removeServlet(String httpCommand, String uri);

    /**
     * Starts the HTTP server and begins listening for incoming HTTP requests.
     */
    void start();

    /**
     * Closes the HTTP server and closes any open connections.
     */
    void close();
}