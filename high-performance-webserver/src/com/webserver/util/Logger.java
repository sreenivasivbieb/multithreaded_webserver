package com.webserver.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Structured logging system for production-ready reliability
 * Thread-safe singleton logger with multiple log levels
 */
public class Logger {
    private static Logger instance;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    private LogLevel currentLevel;
    private PrintWriter fileWriter;
    private boolean logToFile;
    private boolean logToConsole;
    private final SimpleDateFormat dateFormat;

    public enum LogLevel {
        DEBUG(0), INFO(1), WARN(2), ERROR(3), FATAL(4);
        
        private final int value;
        
        LogLevel(int value) {
            this.value = value;
        }
        
        public boolean shouldLog(LogLevel level) {
            return level.value >= this.value;
        }
    }

    private Logger() {
        this.currentLevel = LogLevel.INFO;
        this.logToConsole = true;
        this.logToFile = false;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    /**
     * Get singleton instance
     */
    public static Logger getInstance() {
        if (instance == null) {
            lock.writeLock().lock();
            try {
                if (instance == null) {
                    instance = new Logger();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return instance;
    }

    /**
     * Configure logger
     */
    public void configure(String logLevel, boolean logToFile, String logFilePath) {
        try {
            this.currentLevel = LogLevel.valueOf(logLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.currentLevel = LogLevel.INFO;
        }

        this.logToFile = logToFile;
        
        if (logToFile && logFilePath != null) {
            try {
                fileWriter = new PrintWriter(new FileWriter(logFilePath, true), true);
            } catch (IOException e) {
                System.err.println("Failed to open log file: " + e.getMessage());
                this.logToFile = false;
            }
        }
    }

    /**
     * Set log level
     */
    public void setLevel(LogLevel level) {
        this.currentLevel = level;
    }

    /**
     * Log debug message
     */
    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }

    /**
     * Log info message
     */
    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }

    /**
     * Log warning message
     */
    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }

    /**
     * Log error message
     */
    public void error(String message) {
        log(LogLevel.ERROR, message, null);
    }

    /**
     * Log error message with exception
     */
    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    /**
     * Log fatal message
     */
    public void fatal(String message) {
        log(LogLevel.FATAL, message, null);
    }

    /**
     * Core logging method
     */
    private void log(LogLevel level, String message, Throwable throwable) {
        if (!currentLevel.shouldLog(level)) {
            return;
        }

        String timestamp = dateFormat.format(new Date());
        String threadName = Thread.currentThread().getName();
        String logMessage = String.format("[%s] [%s] [%s] %s", 
            timestamp, level.name(), threadName, message);

        lock.readLock().lock();
        try {
            // Log to console
            if (logToConsole) {
                if (level == LogLevel.ERROR || level == LogLevel.FATAL) {
                    System.err.println(logMessage);
                    if (throwable != null) {
                        throwable.printStackTrace(System.err);
                    }
                } else {
                    System.out.println(logMessage);
                }
            }

            // Log to file
            if (logToFile && fileWriter != null) {
                fileWriter.println(logMessage);
                if (throwable != null) {
                    throwable.printStackTrace(fileWriter);
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Close logger and cleanup resources
     */
    public void close() {
        lock.writeLock().lock();
        try {
            if (fileWriter != null) {
                fileWriter.close();
                fileWriter = null;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Enable/disable console logging
     */
    public void setLogToConsole(boolean logToConsole) {
        this.logToConsole = logToConsole;
    }
}
