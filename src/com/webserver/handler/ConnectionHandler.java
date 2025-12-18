package com.webserver.handler;

import com.webserver.core.ServerConfig;
import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;
import com.webserver.util.Logger;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Handles each client connection in a separate thread
public class ConnectionHandler implements Runnable {
    private final Socket clientSocket;
    private final ServerConfig config;
    private final Logger logger;
    private final long connectionStartTime;

    public ConnectionHandler(Socket clientSocket, ServerConfig config) {
        this.clientSocket = clientSocket;
        this.config = config;
        this.logger = Logger.getInstance();
        this.connectionStartTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        String clientInfo = String.format("%s:%d", 
            clientSocket.getInetAddress().getHostAddress(), 
            clientSocket.getPort());

        try {
            handleConnection(clientInfo);
        } catch (SocketTimeoutException e) {
            logger.warn(String.format("Connection timeout: %s", clientInfo));
        } catch (Exception e) {
            logger.error(String.format("Error handling connection %s: %s", clientInfo, e.getMessage()));
        } finally {
            closeConnection(clientInfo);
        }
    }

    // Process the request and send response
    private void handleConnection(String clientInfo) throws IOException {
        InputStream input = clientSocket.getInputStream();
        OutputStream output = clientSocket.getOutputStream();

        // Parse HTTP request
        HttpRequest request = HttpRequest.parse(input);

        if (!request.isValid()) {
            logger.warn(String.format("Invalid request from %s", clientInfo));
            HttpResponse.error(400, "Bad Request").send(output);
            return;
        }

        logger.info(String.format("Request from %s: %s", clientInfo, request.toString()));

        // Route request
        HttpResponse response;
        try {
            response = routeRequest(request);
        } catch (Exception e) {
            logger.error(String.format("Error processing request: %s", e.getMessage()));
            response = HttpResponse.error(500, "Internal Server Error");
        }

        // Send response
        response.setKeepAlive(false); // Keep-alive disabled for simplicity
        response.send(output);

        long duration = System.currentTimeMillis() - connectionStartTime;
        logger.debug(String.format("Request completed in %d ms", duration));
    }

    /**
     * Route request to appropriate handler
     */
    private HttpResponse routeRequest(HttpRequest request) throws IOException {
        String method = request.getMethod();

        // Only support GET and HEAD methods
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            return HttpResponse.error(405, "Method Not Allowed");
        }

        // Handle file serving
        return serveStaticFile(request);
    }

    /**
     * Serve static files with efficient streaming I/O
     */
    private HttpResponse serveStaticFile(HttpRequest request) throws IOException {
        String uri = request.getNormalizedUri();
        Path filePath = Paths.get(config.getDocumentRoot(), uri.substring(1)); // Remove leading '/'

        // Security check: ensure file is within document root
        Path documentRoot = Paths.get(config.getDocumentRoot()).toAbsolutePath().normalize();
        Path absoluteFilePath = filePath.toAbsolutePath().normalize();
        
        if (!absoluteFilePath.startsWith(documentRoot)) {
            logger.warn(String.format("Directory traversal attempt: %s", uri));
            return HttpResponse.error(403, "Forbidden");
        }

        // Check if file exists
        File file = absoluteFilePath.toFile();
        if (!file.exists()) {
            logger.debug(String.format("File not found: %s", uri));
            return HttpResponse.error(404, "Not Found");
        }

        // Check if it's a directory
        if (file.isDirectory()) {
            // Try index.html
            File indexFile = new File(file, "index.html");
            if (indexFile.exists() && indexFile.isFile()) {
                file = indexFile;
            } else {
                return generateDirectoryListing(file, uri);
            }
        }

        // Check if readable
        if (!file.canRead()) {
            logger.warn(String.format("Cannot read file: %s", uri));
            return HttpResponse.error(403, "Forbidden");
        }

        // Read file content efficiently
        byte[] fileContent = Files.readAllBytes(file.toPath());
        
        // Create response
        HttpResponse response = new HttpResponse();
        response.setStatus(200);
        response.setContentType(HttpResponse.getMimeType(file.getName()));
        response.setBody(fileContent);
        response.addHeader("Last-Modified", new java.util.Date(file.lastModified()).toString());
        response.addHeader("Content-Length", String.valueOf(fileContent.length));

        logger.debug(String.format("Serving file: %s (%d bytes)", uri, fileContent.length));

        return response;
    }

    /**
     * Generate directory listing
     */
    private HttpResponse generateDirectoryListing(File directory, String uri) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head><title>Directory Listing: ").append(escapeHtml(uri)).append("</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;}");
        html.append("h1{border-bottom:1px solid #ccc;}");
        html.append("ul{list-style:none;padding:0;}");
        html.append("li{padding:5px 0;}");
        html.append("a{text-decoration:none;color:#0066cc;}");
        html.append("a:hover{text-decoration:underline;}");
        html.append(".dir::before{content:'📁 ';}");
        html.append(".file::before{content:'📄 ';}");
        html.append("</style></head>\n");
        html.append("<body>\n");
        html.append("<h1>Directory Listing: ").append(escapeHtml(uri)).append("</h1>\n");
        html.append("<ul>\n");

        // Parent directory link
        if (!"/".equals(uri)) {
            String parentUri = uri.substring(0, Math.max(uri.lastIndexOf('/'), 1));
            html.append("<li><a href='").append(parentUri).append("' class='dir'>..</a></li>\n");
        }

        // List files and directories
        File[] files = directory.listFiles();
        if (files != null) {
            java.util.Arrays.sort(files);
            for (File file : files) {
                String fileName = file.getName();
                String fileUri = uri.endsWith("/") ? uri + fileName : uri + "/" + fileName;
                String cssClass = file.isDirectory() ? "dir" : "file";
                
                html.append("<li><a href='").append(fileUri);
                if (file.isDirectory()) {
                    html.append("/");
                }
                html.append("' class='").append(cssClass).append("'>")
                    .append(escapeHtml(fileName));
                if (file.isDirectory()) {
                    html.append("/");
                } else {
                    html.append(" (").append(formatFileSize(file.length())).append(")");
                }
                html.append("</a></li>\n");
            }
        }

        html.append("</ul>\n");
        html.append("<hr>\n");
        html.append("<p><em>Java-WebServer/1.0</em></p>\n");
        html.append("</body>\n");
        html.append("</html>");

        return HttpResponse.ok(html.toString());
    }

    /**
     * Escape HTML special characters
     */
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * Format file size in human-readable format
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }

    /**
     * Close connection and cleanup resources
     */
    private void closeConnection(String clientInfo) {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                logger.debug(String.format("Connection closed: %s", clientInfo));
            }
        } catch (IOException e) {
            logger.error(String.format("Error closing connection %s: %s", clientInfo, e.getMessage()));
        }
    }
}
