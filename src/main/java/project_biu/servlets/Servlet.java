package project_biu.servlets;

import project_biu.server.RequestParser.RequestInfo;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Defines a generic interface for handling HTTP requests and generating responses.
 * Implementations of this interface must provide mechanisms for processing
 * incoming requests and writing appropriate responses to the client.
 * <p>
 * This is the controller layer of the MVC architecture.
 */
public interface Servlet {
    /**
     * Handles an incoming HTTP request and generates a response.
     *
     * @param ri       The request info with all the information of the request.
     * @param toClient The output stream to write the response to.
     * @throws IOException If an I/O error occurs during the handling process.
     */
    void handle(RequestInfo ri, OutputStream toClient) throws IOException;

    /**
     * Close the servlet and free all the resources.
     *
     * @throws IOException If an I/O error occurs during the closing process.
     */
    void close() throws IOException;
}