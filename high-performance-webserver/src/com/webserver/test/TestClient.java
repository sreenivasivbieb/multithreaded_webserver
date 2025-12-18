package com.webserver.test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test client for benchmarking the web server
 * Can simulate multiple concurrent connections
 */
public class TestClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("Web Server Test Client");
        System.out.println("======================\n");

        if (args.length > 0 && "load".equals(args[0])) {
            // Load test mode
            int numClients = args.length > 1 ? Integer.parseInt(args[1]) : 50;
            int requestsPerClient = args.length > 2 ? Integer.parseInt(args[2]) : 10;
            runLoadTest(numClients, requestsPerClient);
        } else {
            // Simple test mode
            runSimpleTest();
        }
    }

    /**
     * Run simple test with a few requests
     */
    private static void runSimpleTest() {
        System.out.println("Running simple test...\n");

        String[] testUrls = {
            "/",
            "/index.html",
            "/test.txt",
            "/nonexistent.html"
        };

        for (String url : testUrls) {
            try {
                System.out.println("Testing: " + url);
                String response = sendRequest(url);
                System.out.println("Response preview: " + 
                    response.substring(0, Math.min(200, response.length())));
                System.out.println("Status: SUCCESS\n");
                successCount.incrementAndGet();
            } catch (Exception e) {
                System.err.println("Status: FAILED - " + e.getMessage() + "\n");
                failCount.incrementAndGet();
            }
        }

        printSummary();
    }

    /**
     * Run load test with multiple concurrent clients
     */
    private static void runLoadTest(int numClients, int requestsPerClient) {
        System.out.println(String.format("Running load test with %d clients, %d requests each...\n", 
            numClients, requestsPerClient));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(numClients);
        
        long startTime = System.currentTimeMillis();

        // Create client threads
        for (int i = 0; i < numClients; i++) {
            final int clientId = i;
            new Thread(() -> {
                try {
                    // Wait for start signal
                    startLatch.await();
                    
                    // Execute requests
                    for (int j = 0; j < requestsPerClient; j++) {
                        try {
                            sendRequest("/index.html");
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        }
                        
                        // Small delay between requests
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            }, "Client-" + clientId).start();
        }

        // Start all clients simultaneously
        System.out.println("Starting load test...");
        startLatch.countDown();

        // Wait for completion
        try {
            completeLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("\n======================");
        System.out.println("Load Test Results");
        System.out.println("======================");
        System.out.println("Total requests: " + (numClients * requestsPerClient));
        System.out.println("Successful: " + successCount.get());
        System.out.println("Failed: " + failCount.get());
        System.out.println("Duration: " + duration + " ms");
        System.out.println("Throughput: " + 
            String.format("%.2f", (successCount.get() * 1000.0 / duration)) + " req/sec");
        System.out.println("Average latency: " + 
            String.format("%.2f", duration / (double)(numClients * requestsPerClient)) + " ms");
    }

    /**
     * Send HTTP GET request and return response
     */
    private static String sendRequest(String path) throws IOException {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send HTTP request
            out.println("GET " + path + " HTTP/1.1");
            out.println("Host: " + HOST);
            out.println("Connection: close");
            out.println();

            // Read response
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            return response.toString();
        }
    }

    /**
     * Print test summary
     */
    private static void printSummary() {
        System.out.println("======================");
        System.out.println("Test Summary");
        System.out.println("======================");
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + failCount.get());
        System.out.println("Total requests: " + (successCount.get() + failCount.get()));
    }
}
