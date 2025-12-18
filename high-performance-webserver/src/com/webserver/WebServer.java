package com.webserver;

import com.webserver.core.ServerConfig;
import com.webserver.core.ThreadPoolManager;
import com.webserver.handler.ConnectionHandler;
import com.webserver.util.Logger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

// Main web server class - handles incoming connections and routes them to worker threads
public class WebServer {
    private final ServerConfig config;
    private final ThreadPoolManager threadPool;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket serverSocket;
    private Thread acceptorThread;
    private final Logger logger;

    public WebServer(ServerConfig config) {
        this.config = config;
        this.threadPool = new ThreadPoolManager(config);
        this.logger = Logger.getInstance();
    }

    // Start the server
    public void start() throws IOException {
        if (running.get()) {
            logger.warn("Server is already running");
            return;
        }

        serverSocket = new ServerSocket(config.getPort(), config.getBacklog());
        serverSocket.setSoTimeout(config.getAcceptTimeout());
        running.set(true);

        logger.info(String.format("Web Server started on port %d", config.getPort()));
        logger.info(String.format("Thread pool size: %d-%d", config.getCorePoolSize(), config.getMaxPoolSize()));
        logger.info(String.format("Document root: %s", config.getDocumentRoot()));

        // Start acceptor thread
        acceptorThread = new Thread(this::acceptConnections, "Acceptor-Thread");
        acceptorThread.start();
    }

    // Keep accepting connections until server stops
    private void acceptConnections() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Configure socket
                clientSocket.setSoTimeout(config.getSocketTimeout());
                clientSocket.setKeepAlive(true);
                
                logger.debug(String.format("Accepted connection from %s:%d", 
                    clientSocket.getInetAddress().getHostAddress(), 
                    clientSocket.getPort()));

                // Submit to thread pool
                ConnectionHandler handler = new ConnectionHandler(clientSocket, config);
                threadPool.submit(handler);

            } catch (SocketTimeoutException e) {
                // Normal timeout, continue accepting
            } catch (IOException e) {
                if (running.get()) {
                    logger.error("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Graceful shutdown of the server
     */
    public void shutdown() {
        if (!running.get()) {
            logger.warn("Server is not running");
            return;
        }

        logger.info("Initiating graceful shutdown...");
        running.set(false);

        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket: " + e.getMessage());
        }

        // Wait for acceptor thread
        if (acceptorThread != null) {
            try {
                acceptorThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Shutdown thread pool
        threadPool.shutdown();

        logger.info("Server shutdown complete");
    }

    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Main entry point
     */
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        
        try {
            // Load configuration
            ServerConfig config = ServerConfig.loadDefault();
            
            // Create and start server
            WebServer server = new WebServer(config);
            
            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received");
                server.shutdown();
            }));
            
            server.start();
            
            // Keep main thread alive
            Thread.currentThread().join();
            
        } catch (Exception e) {
            logger.error("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
