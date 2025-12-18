package com.webserver.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Server configuration management
 */
public class ServerConfig {
    private int port;
    private String documentRoot;
    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    private int keepAliveTime;
    private int socketTimeout;
    private int acceptTimeout;
    private int backlog;
    private String logLevel;

    private ServerConfig() {
        // Default values
        this.port = 8080;
        this.documentRoot = "www";
        this.corePoolSize = 10;
        this.maxPoolSize = 50;
        this.queueCapacity = 100;
        this.keepAliveTime = 60;
        this.socketTimeout = 30000;
        this.acceptTimeout = 1000;
        this.backlog = 50;
        this.logLevel = "INFO";
    }

    public static ServerConfig loadDefault() {
        ServerConfig config = new ServerConfig();
        
        // Try to load from config file if exists
        File configFile = new File("config.properties");
        if (configFile.exists()) {
            try {
                config.loadFromFile(configFile);
            } catch (IOException e) {
                System.err.println("Warning: Could not load config file, using defaults");
            }
        }
        
        // Create document root if not exists
        File docRoot = new File(config.documentRoot);
        if (!docRoot.exists()) {
            docRoot.mkdirs();
        }
        
        return config;
    }

    private void loadFromFile(File file) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        }
        
        this.port = Integer.parseInt(props.getProperty("server.port", String.valueOf(port)));
        this.documentRoot = props.getProperty("server.documentRoot", documentRoot);
        this.corePoolSize = Integer.parseInt(props.getProperty("threadPool.coreSize", String.valueOf(corePoolSize)));
        this.maxPoolSize = Integer.parseInt(props.getProperty("threadPool.maxSize", String.valueOf(maxPoolSize)));
        this.queueCapacity = Integer.parseInt(props.getProperty("threadPool.queueCapacity", String.valueOf(queueCapacity)));
        this.keepAliveTime = Integer.parseInt(props.getProperty("threadPool.keepAliveTime", String.valueOf(keepAliveTime)));
        this.socketTimeout = Integer.parseInt(props.getProperty("socket.timeout", String.valueOf(socketTimeout)));
        this.acceptTimeout = Integer.parseInt(props.getProperty("socket.acceptTimeout", String.valueOf(acceptTimeout)));
        this.backlog = Integer.parseInt(props.getProperty("socket.backlog", String.valueOf(backlog)));
        this.logLevel = props.getProperty("log.level", logLevel);
    }

    // Getters
    public int getPort() { return port; }
    public String getDocumentRoot() { return documentRoot; }
    public int getCorePoolSize() { return corePoolSize; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public int getQueueCapacity() { return queueCapacity; }
    public int getKeepAliveTime() { return keepAliveTime; }
    public int getSocketTimeout() { return socketTimeout; }
    public int getAcceptTimeout() { return acceptTimeout; }
    public int getBacklog() { return backlog; }
    public String getLogLevel() { return logLevel; }

    // Setters for testing
    public void setPort(int port) { this.port = port; }
    public void setDocumentRoot(String documentRoot) { this.documentRoot = documentRoot; }
    public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }
    public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
}
