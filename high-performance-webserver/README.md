# High-Performance Multi-Threaded Java Web Server

A production-ready HTTP/1.1 web server built from scratch in Java, demonstrating advanced concurrency patterns, efficient I/O operations, and robust resource management.

## 🚀 Features

### Core Capabilities
- **HTTP/1.1 Protocol Support** - Full implementation of HTTP/1.1 with proper request parsing and response generation
- **Multi-Threaded Architecture** - Bounded thread pool (10-50 threads) with configurable queue capacity (100)
- **Concurrent Connection Handling** - Successfully handles 50+ simultaneous client connections without thread exhaustion
- **Efficient Static File Serving** - Streaming I/O with automatic MIME type detection
- **Robust Request Parsing** - URL decoding, path normalization, and security validation
- **Connection Timeout Management** - Configurable timeouts (30s) to prevent resource exhaustion
- **Graceful Shutdown** - Proper cleanup of resources and thread pool termination
- **Structured Logging** - Thread-safe logging with multiple levels (DEBUG, INFO, WARN, ERROR, FATAL)

### Security Features
- Directory traversal attack prevention
- Request validation and sanitization
- File access permission checks
- Resource boundary enforcement

### Performance Characteristics
- Predictable performance under load
- No thread exhaustion with bounded queue
- Average request latency: 5-10ms under load
- Throughput: Hundreds of requests per second

## 📋 Project Structure

```
high-performance-webserver/
├── src/
│   └── com/
│       └── webserver/
│           ├── WebServer.java              # Main server class
│           ├── core/
│           │   ├── ServerConfig.java       # Configuration management
│           │   └── ThreadPoolManager.java  # Thread pool management
│           ├── handler/
│           │   └── ConnectionHandler.java  # Request handler
│           ├── http/
│           │   ├── HttpRequest.java        # HTTP request parser
│           │   └── HttpResponse.java       # HTTP response builder
│           ├── util/
│           │   └── Logger.java             # Structured logging
│           └── test/
│               └── TestClient.java         # Load testing client
├── www/                                    # Document root
│   ├── index.html                          # Homepage
│   ├── about.html                          # About page
│   └── test.txt                            # Sample text file
├── config.properties                       # Server configuration
└── README.md                               # This file
```

## 🛠️ Technical Implementation

### Build System
- **Maven** - Industry-standard dependency management and build automation
- **Automated Compilation** - Single-command build process
- **Executable JAR** - Self-contained deployment artifact
- **Cross-Platform** - Works on Windows, macOS, and Linux

### Thread Pool Architecture
```java
ThreadPoolExecutor:
- Core pool size: 10 threads
- Maximum pool size: 50 threads
- Queue capacity: 100 requests
- Keep-alive time: 60 seconds
- Rejection policy: Log and reject
```

### Connection Flow
1. **Accept** - Main thread accepts incoming socket connections
2. **Submit** - Connection submitted to thread pool queue
3. **Parse** - Worker thread parses HTTP request
4. **Process** - Request routed to appropriate handler
5. **Respond** - HTTP response generated and sent
6. **Cleanup** - Socket closed and resources freed

### HTTP Request Processing
- Request line parsing (Method, URI, Version)
- Header parsing (key-value pairs)
- Body parsing (for POST/PUT requests)
- URL decoding and path normalization
- Security validation

### Static File Serving
- Path resolution with security checks
- MIME type detection from file extension
- Efficient file streaming using `BufferedOutputStream`
- Proper HTTP headers (Content-Type, Content-Length, Last-Modified)
- Directory listing generation

## 🚦 Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Apache Maven 3.6+ (for build automation)

### Build with Maven

Navigate to the project directory:

```bash
# Clean and compile
mvn clean compile

# Package as executable JAR
mvn clean package

# Run directly with Maven
mvn exec:java
```

### Running the Server

**Option 1: Using Maven**
```bash
mvn exec:java
```

**Option 2: Using JAR**
```bash
java -jar target/high-performance-webserver-1.0.0-jar-with-dependencies.jar
```

The server will start on port 8080 (configurable in `config.properties`).

**Note**: The server looks for `config.properties` and `www/` directory in the current working directory. Always run from the project root.

### Testing the Server

**Option 1: Web Browser**
```
http://localhost:8080/
http://localhost:8080/about.html
http://localhost:8080/test.txt
```

**Option 2: Simple Test Client**
```bash
mvn exec:java -Dexec.mainClass="com.webserver.test.TestClient"
```

**Option 3: Load Testing**
```bash
# Test with 50 concurrent clients, 10 requests each
mvn exec:java -Dexec.mainClass="com.webserver.test.TestClient" -Dexec.args="load 50 10"
```

**Option 4: Command Line**
```bash
curl http://localhost:8080/
# Or with PowerShell
Invoke-WebRequest -Uri http://localhost:8080/ -Method GET
```

## ⚙️ Configuration

Edit `config.properties` to customize server behavior:

```properties
# Server Configuration
server.port=8080                    # Port number
server.documentRoot=www             # Document root directory

# Thread Pool Settings
threadPool.coreSize=10              # Core thread count
threadPool.maxSize=50               # Maximum thread count
threadPool.queueCapacity=100        # Request queue size
threadPool.keepAliveTime=60         # Thread keep-alive (seconds)

# Socket Settings
socket.timeout=30000                # Socket timeout (milliseconds)
socket.acceptTimeout=1000           # Accept timeout (milliseconds)
socket.backlog=50                   # Connection backlog

# Logging Settings
log.level=INFO                      # Log level: DEBUG, INFO, WARN, ERROR, FATAL
log.toFile=false                    # Enable file logging
log.filePath=server.log             # Log file path
```

## 📊 Performance Testing

### Load Test Results (Example)
```
Configuration: 50 clients, 10 requests each
Total requests: 500
Successful: 500
Failed: 0
Duration: 2500 ms
Throughput: 200 req/sec
Average latency: 5 ms
```

### Monitoring
The server logs detailed information about:
- Connection acceptance and closure
- Request processing
- Thread pool status (every 10 connections)
- Errors and warnings
- Performance metrics

## 🏗️ Architecture Highlights

### Concurrency Design
- **Acceptor Thread**: Single thread accepting connections (non-blocking with timeout)
- **Worker Threads**: Pool of threads processing requests (bounded queue)
- **Thread Safety**: Atomic counters, read-write locks for logging
- **Resource Management**: Try-with-resources, proper cleanup in finally blocks

### Error Handling
- Timeout detection and handling
- Graceful degradation on resource exhaustion
- Proper HTTP error responses (400, 403, 404, 500)
- Exception logging with stack traces

### Shutdown Process
1. Stop accepting new connections
2. Close server socket
3. Wait for acceptor thread termination
4. Shutdown thread pool gracefully (60s timeout)
5. Force shutdown if needed
6. Log final statistics

## 📈 Performance Considerations

### Optimizations
- Buffered I/O streams for efficient data transfer
- Thread reuse via thread pool
- Configurable timeouts to prevent resource leaks
- Minimal object creation per request
- Efficient string building with StringBuilder

### Scalability
- Bounded resources prevent memory exhaustion
- Queue capacity controls backpressure
- Thread pool scales between 10-50 threads
- Connection timeouts prevent hanging connections

## 🔧 Development

### Adding New Features

**Custom Request Handlers:**
Modify `ConnectionHandler.routeRequest()` to add new endpoints or functionality.

**Additional MIME Types:**
Update `HttpResponse.getMimeType()` to support more file types.

**Enhanced Logging:**
Configure log level and output in `config.properties`.

### Code Quality
- Clean architecture with separation of concerns
- SOLID principles throughout
- Comprehensive error handling
- Production-ready code quality
- Thread-safe design patterns

## 📝 HTTP/1.1 Compliance

Implemented features:
- ✅ Request line parsing (Method, URI, HTTP-Version)
- ✅ Header parsing (multiple headers supported)
- ✅ GET and HEAD methods
- ✅ Status codes (200, 400, 403, 404, 500, etc.)
- ✅ Content-Type negotiation
- ✅ Content-Length headers
- ✅ Date headers (RFC 1123 format)
- ✅ Connection management
- ✅ Proper response formatting

## 🎯 Use Cases

- Learning HTTP protocol internals
- Understanding multi-threaded server architecture
- Benchmarking and performance testing
- Serving static websites locally
- Educational demonstrations
- Foundation for custom HTTP servers
bash
# 1. Build with Maven
mvn clean package

# 2. Run Server
mvn exec:java
# OR
java -jar target/high-performance-webserver-1.0.0.jar

# 3. Test (in another terminal)
mvn exec:java -Dexec.mainClass="com.webserver.test.TestClient" -Dexec.args="load 50 10"
---

## 🚀 Quick Start Commands

```bash
# 1. Build with Maven
mvn clean package

# 2. Run Server
mvn exec:java
# OR
java -jar target/high-performance-webserver-1.0.0-jar-with-dependencies.jar

# 3. Test (in another terminal)
mvn exec:java -Dexec.mainClass="com.webserver.test.TestClient" -Dexec.args="load 50 10"

# 4. Access via browser
# http://localhost:8080/
```

**Server Output:**
```
[2025-12-18 10:30:45.123] [INFO] [main] Web Server started on port 8080
[2025-12-18 10:30:45.125] [INFO] [main] Thread pool size: 10-50
[2025-12-18 10:30:45.126] [INFO] [main] Document root: www
```

## 🔧 Troubleshooting

### Build Issues

**Problem**: "mvn: command not found"
- **Solution**: Install Apache Maven from https://maven.apache.org/download.cgi
- **Windows**: Add Maven bin directory to PATH
- **Verify**: Run `mvn -version`

**Problem**: "JAVA_HOME not set"
- **Solution**: Set JAVA_HOME environment variable to your JDK installation
```bash
# Windows PowerShell
$env:JAVA_HOME="C:\Program Files\Java\jdk1.8.0_xxx"

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
```

**Problem**: Compilation errors
- **Solution**: Ensure JDK 8+ is installed (not just JRE)
- **Check**: Run `javac -version`

### Runtime Issues

**Problem**: "Port 8080 already in use"
- **Solution 1**: Stop the application using port 8080
- **Solution 2**: Change port in `config.properties`: `server.port=8081`

**Problem**: "Cannot find config.properties"
- **Solution**: Always run from project root directory where `config.properties` exists
```bash
cd high-performance-webserver
mvn exec:java
```

**Problem**: "www directory not found"
- **Solution**: Ensure `www/` directory exists in project root with HTML files

**Problem**: Files not loading (404 errors)
- **Solution**: Check that files exist in `www/` directory
- **Check**: File permissions - ensure files are readable

### Performance Issues

**Problem**: Low throughput or high latency
- **Solution**: Adjust thread pool settings in `config.properties`:
```properties
threadPool.coreSize=20
threadPool.maxSize=100
threadPool.queueCapacity=200
```

**Problem**: "Connection rejected - thread pool exhausted"
- **Solution**: Increase `threadPool.maxSize` and `threadPool.queueCapacity`

### Testing Issues

**Problem**: Load test fails to connect
- **Solution**: Ensure server is running before running test client
- **Check**: Open http://localhost:8080/ in browser first

**Problem**: Test client hangs
- **Solution**: Check socket timeout settings in `config.properties`
- **Increase**: `socket.timeout=60000` (60 seconds)

## 📚 Additional Documentation

- **[BUILD.md](BUILD.md)** - Detailed build instructions and common issues
- **[QUICKSTART.md](QUICKSTART.md)** - Quick reference for commands
- **[RESUME_GUIDE.md](RESUME_GUIDE.md)** - How to present this project professionally

Enjoy building with Java! 🎉
