package com.webserver.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * HTTP/1.1 Response Builder
 * Constructs and sends HTTP responses according to RFC 2616
 */
public class HttpResponse {
    private int statusCode;
    private String statusMessage;
    private Map<String, String> headers;
    private byte[] body;
    
    private static final Map<Integer, String> STATUS_MESSAGES = new HashMap<>();
    static {
        STATUS_MESSAGES.put(200, "OK");
        STATUS_MESSAGES.put(201, "Created");
        STATUS_MESSAGES.put(204, "No Content");
        STATUS_MESSAGES.put(301, "Moved Permanently");
        STATUS_MESSAGES.put(304, "Not Modified");
        STATUS_MESSAGES.put(400, "Bad Request");
        STATUS_MESSAGES.put(403, "Forbidden");
        STATUS_MESSAGES.put(404, "Not Found");
        STATUS_MESSAGES.put(405, "Method Not Allowed");
        STATUS_MESSAGES.put(500, "Internal Server Error");
        STATUS_MESSAGES.put(501, "Not Implemented");
        STATUS_MESSAGES.put(503, "Service Unavailable");
    }

    public HttpResponse() {
        this.headers = new LinkedHashMap<>();
        this.statusCode = 200;
        this.statusMessage = "OK";
        
        // Add default headers
        addDefaultHeaders();
    }

    private void addDefaultHeaders() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        headers.put("Date", dateFormat.format(new Date()));
        headers.put("Server", "Java-WebServer/1.0");
        headers.put("Connection", "close");
    }

    /**
     * Set response status
     */
    public HttpResponse setStatus(int code) {
        this.statusCode = code;
        this.statusMessage = STATUS_MESSAGES.getOrDefault(code, "Unknown");
        return this;
    }

    /**
     * Add response header
     */
    public HttpResponse addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Set response body (text)
     */
    public HttpResponse setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(this.body.length));
        if (!headers.containsKey("Content-Type")) {
            headers.put("Content-Type", "text/html; charset=UTF-8");
        }
        return this;
    }

    /**
     * Set response body (binary)
     */
    public HttpResponse setBody(byte[] body) {
        this.body = body;
        headers.put("Content-Length", String.valueOf(body.length));
        return this;
    }

    /**
     * Set content type
     */
    public HttpResponse setContentType(String contentType) {
        headers.put("Content-Type", contentType);
        return this;
    }

    /**
     * Enable keep-alive
     */
    public HttpResponse setKeepAlive(boolean keepAlive) {
        headers.put("Connection", keepAlive ? "keep-alive" : "close");
        return this;
    }

    /**
     * Send response to output stream
     */
    public void send(OutputStream out) throws IOException {
        BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
        
        // Status line
        String statusLine = String.format("HTTP/1.1 %d %s\r\n", statusCode, statusMessage);
        bufferedOut.write(statusLine.getBytes(StandardCharsets.US_ASCII));
        
        // Headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            String headerLine = String.format("%s: %s\r\n", header.getKey(), header.getValue());
            bufferedOut.write(headerLine.getBytes(StandardCharsets.US_ASCII));
        }
        
        // Empty line between headers and body
        bufferedOut.write("\r\n".getBytes(StandardCharsets.US_ASCII));
        
        // Body
        if (body != null && body.length > 0) {
            bufferedOut.write(body);
        }
        
        bufferedOut.flush();
    }

    /**
     * Create error response
     */
    public static HttpResponse error(int statusCode, String message) {
        HttpResponse response = new HttpResponse();
        response.setStatus(statusCode);
        
        String html = String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head><title>%d %s</title></head>\n" +
            "<body>\n" +
            "<h1>%d %s</h1>\n" +
            "<p>%s</p>\n" +
            "<hr>\n" +
            "<p><em>Java-WebServer/1.0</em></p>\n" +
            "</body>\n" +
            "</html>",
            statusCode, STATUS_MESSAGES.get(statusCode),
            statusCode, STATUS_MESSAGES.get(statusCode),
            message
        );
        
        response.setBody(html);
        return response;
    }

    /**
     * Create success response with HTML body
     */
    public static HttpResponse ok(String html) {
        HttpResponse response = new HttpResponse();
        response.setStatus(200);
        response.setBody(html);
        return response;
    }

    /**
     * Get MIME type from file extension
     */
    public static String getMimeType(String filename) {
        String extension = "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = filename.substring(dotIndex + 1).toLowerCase();
        }
        
        switch (extension) {
            case "html":
            case "htm":
                return "text/html; charset=UTF-8";
            case "css":
                return "text/css; charset=UTF-8";
            case "js":
                return "application/javascript; charset=UTF-8";
            case "json":
                return "application/json; charset=UTF-8";
            case "xml":
                return "application/xml; charset=UTF-8";
            case "txt":
                return "text/plain; charset=UTF-8";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml";
            case "ico":
                return "image/x-icon";
            case "pdf":
                return "application/pdf";
            case "zip":
                return "application/zip";
            default:
                return "application/octet-stream";
        }
    }
}
