package com.webserver.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// Parse HTTP requests
public class HttpRequest {
    private String method;
    private String uri;
    private String version;
    private Map<String, String> headers;
    private String body;
    private boolean valid;

    public HttpRequest() {
        this.headers = new HashMap<>();
        this.valid = false;
    }

    // Parse the HTTP request
    public static HttpRequest parse(InputStream inputStream) throws IOException {
        HttpRequest request = new HttpRequest();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Parse request line
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return request; // Invalid request
        }

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            return request; // Invalid request line
        }

        request.method = parts[0];
        request.uri = parts[1];
        request.version = parts[2];

        // Parse headers
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String headerName = line.substring(0, colonIndex).trim();
                String headerValue = line.substring(colonIndex + 1).trim();
                request.headers.put(headerName.toLowerCase(), headerValue);
            }
        }

        // Parse body if present (for POST requests)
        if ("POST".equalsIgnoreCase(request.method) || "PUT".equalsIgnoreCase(request.method)) {
            String contentLengthStr = request.headers.get("content-length");
            if (contentLengthStr != null) {
                try {
                    int contentLength = Integer.parseInt(contentLengthStr);
                    if (contentLength > 0 && contentLength < 1024 * 1024) { // Limit to 1MB
                        char[] bodyChars = new char[contentLength];
                        int read = reader.read(bodyChars, 0, contentLength);
                        if (read > 0) {
                            request.body = new String(bodyChars, 0, read);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Invalid content length
                }
            }
        }

        request.valid = true;
        return request;
    }

    /**
     * Normalize URI path to prevent directory traversal attacks
     */
    public String getNormalizedUri() {
        if (uri == null) {
            return "/";
        }

        // Remove query string
        String path = uri;
        int queryIndex = path.indexOf('?');
        if (queryIndex > 0) {
            path = path.substring(0, queryIndex);
        }

        // Decode URL encoding
        path = urlDecode(path);

        // Normalize path
        path = path.replaceAll("/+", "/");
        path = path.replaceAll("/\\./", "/");
        
        // Prevent directory traversal
        if (path.contains("../") || path.contains("..\\")) {
            return "/";
        }

        // Default to index.html for directory requests
        if (path.endsWith("/")) {
            path += "index.html";
        }

        return path;
    }

    /**
     * Simple URL decoder
     */
    private String urlDecode(String str) {
        try {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c == '%' && i + 2 < str.length()) {
                    String hex = str.substring(i + 1, i + 3);
                    try {
                        result.append((char) Integer.parseInt(hex, 16));
                        i += 2;
                    } catch (NumberFormatException e) {
                        result.append(c);
                    }
                } else if (c == '+') {
                    result.append(' ');
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        } catch (Exception e) {
            return str;
        }
    }

    // Getters
    public String getMethod() { return method; }
    public String getUri() { return uri; }
    public String getVersion() { return version; }
    public Map<String, String> getHeaders() { return headers; }
    public String getHeader(String name) { return headers.get(name.toLowerCase()); }
    public String getBody() { return body; }
    public boolean isValid() { return valid; }
    public boolean isKeepAlive() {
        String connection = getHeader("connection");
        return connection != null && "keep-alive".equalsIgnoreCase(connection);
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", method, uri, version);
    }
}
