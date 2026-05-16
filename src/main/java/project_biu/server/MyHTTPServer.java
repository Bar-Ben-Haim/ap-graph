package project_biu.server;

import project_biu.servlets.Servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class MyHTTPServer extends Thread implements HTTPServer {
    private final int port;
    private final ExecutorService executorService;
    private final ConcurrentMap<String, Servlet> getServlets;
    private final ConcurrentMap<String, Servlet> postServlets;
    private final ConcurrentMap<String, Servlet> deleteServlets;
    private volatile boolean stop;

    public MyHTTPServer(int port, int nThreads) {
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(nThreads);
        getServlets = new ConcurrentHashMap<>();
        postServlets = new ConcurrentHashMap<>();
        deleteServlets = new ConcurrentHashMap<>();
    }

    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
        findServletsMap(httpCommand).ifPresent(map -> map.put(uri, s));
    }

    @Override
    public void removeServlet(String httpCommand, String uri) {
        findServletsMap(httpCommand).ifPresent(map -> map.remove(uri));
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(1000);

            while (!stop) {
                try {
                    final Socket socket = server.accept();
                    executorService.submit(() -> handleClient(socket));
                } catch (SocketTimeoutException _) {
                    // Expected every 1 second
                } catch (IOException e) {
                    if (!stop)
                        System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void close() {
        stop = true;
        executorService.shutdown();
        Stream.of(getServlets, postServlets, deleteServlets)
                .map(Map::values)
                .flatMap(Collection::stream)
                .forEach(servlet -> {
                    try {
                        servlet.close();
                    } catch (IOException e) {
                        System.out.println("Error closing servlet with error: " + e.getMessage());
                    }
                });
    }

    private void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             OutputStream out = client.getOutputStream()) {
            final RequestParser.RequestInfo info = RequestParser.parseRequest(in);
            if (info == null) return;

            findBestMatchingServlet(info.httpCommand(), info.uri()).ifPresent(bestMatchingServlet -> {
                try {
                    bestMatchingServlet.handle(info, out);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            });
            client.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Finds the servlet with the longest matching URI prefix.
     */
    private Optional<Servlet> findBestMatchingServlet(String command, String requestUri) {
        final Optional<Map<String, Servlet>> optionalServletMap = findServletsMap(command);
        if (optionalServletMap.isEmpty()) {
            System.out.println("Received unknown command to execute: " + command);
            return Optional.empty();
        }

        final Map<String, Servlet> servletMap = optionalServletMap.get();
        return servletMap.keySet()
                .stream()
                .filter(requestUri::startsWith)
                .max(Comparator.comparing(String::length))
                .map(servletMap::get);
    }

    private Optional<Map<String, Servlet>> findServletsMap(String httpCommand) {
        return switch (httpCommand) {
            case "GET" -> Optional.of(getServlets);
            case "POST" -> Optional.of(postServlets);
            case "DELETE" -> Optional.of(deleteServlets);
            default -> {
                System.out.println("Received unknown command to register: " + httpCommand);
                yield Optional.empty();
            }
        };
    }
}