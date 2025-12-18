package com.webserver.core;

import com.webserver.util.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages bounded thread pool for handling client connections
 */
public class ThreadPoolManager {
    private final ThreadPoolExecutor executor;
    private final Logger logger;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger totalConnections = new AtomicInteger(0);

    public ThreadPoolManager(ServerConfig config) {
        this.logger = Logger.getInstance();
        
        // Create bounded thread pool
        this.executor = new ThreadPoolExecutor(
            config.getCorePoolSize(),
            config.getMaxPoolSize(),
            config.getKeepAliveTime(),
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(config.getQueueCapacity()),
            new CustomThreadFactory(),
            new CustomRejectionHandler()
        );
        
        // Allow core threads to timeout
        executor.allowCoreThreadTimeOut(true);
        
        logger.info(String.format("Thread pool initialized: core=%d, max=%d, queue=%d",
            config.getCorePoolSize(), config.getMaxPoolSize(), config.getQueueCapacity()));
    }

    /**
     * Submit a task to the thread pool
     */
    public void submit(Runnable task) {
        totalConnections.incrementAndGet();
        activeConnections.incrementAndGet();
        
        executor.execute(() -> {
            try {
                task.run();
            } finally {
                activeConnections.decrementAndGet();
                logPoolStatus();
            }
        });
    }

    /**
     * Graceful shutdown of thread pool
     */
    public void shutdown() {
        logger.info("Shutting down thread pool...");
        executor.shutdown();
        
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Thread pool did not terminate gracefully, forcing shutdown");
                executor.shutdownNow();
                
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.error("Thread pool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            logger.error("Thread pool shutdown interrupted");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info(String.format("Thread pool shutdown complete. Total connections served: %d", 
            totalConnections.get()));
    }

    /**
     * Log thread pool status
     */
    private void logPoolStatus() {
        if (totalConnections.get() % 10 == 0) { // Log every 10 connections
            logger.debug(String.format(
                "Pool status - Active: %d, Pool size: %d, Queue size: %d, Completed: %d",
                activeConnections.get(),
                executor.getPoolSize(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount()
            ));
        }
    }

    /**
     * Get current pool statistics
     */
    public PoolStats getStats() {
        return new PoolStats(
            executor.getPoolSize(),
            executor.getActiveCount(),
            executor.getQueue().size(),
            executor.getCompletedTaskCount(),
            totalConnections.get(),
            activeConnections.get()
        );
    }

    /**
     * Custom thread factory for naming threads
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;

        CustomThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, "Worker-" + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    /**
     * Custom rejection handler for when queue is full
     */
    private class CustomRejectionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            logger.error("Connection rejected - thread pool exhausted");
            // Could implement backpressure strategy here
        }
    }

    /**
     * Thread pool statistics
     */
    public static class PoolStats {
        public final int poolSize;
        public final int activeThreads;
        public final int queueSize;
        public final long completedTasks;
        public final int totalConnections;
        public final int activeConnections;

        public PoolStats(int poolSize, int activeThreads, int queueSize, 
                        long completedTasks, int totalConnections, int activeConnections) {
            this.poolSize = poolSize;
            this.activeThreads = activeThreads;
            this.queueSize = queueSize;
            this.completedTasks = completedTasks;
            this.totalConnections = totalConnections;
            this.activeConnections = activeConnections;
        }

        @Override
        public String toString() {
            return String.format(
                "PoolStats{poolSize=%d, active=%d, queue=%d, completed=%d, total=%d, current=%d}",
                poolSize, activeThreads, queueSize, completedTasks, totalConnections, activeConnections
            );
        }
    }
}
