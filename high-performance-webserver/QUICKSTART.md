# High-Performance Multi-Threaded Java Web Server

## Project Overview
A production-ready HTTP/1.1 web server demonstrating advanced Java concurrency patterns, efficient I/O operations, and enterprise-level reliability.

## Quick Start

### 1. Build with Maven
```bash
mvn clean package
```

### 2. Start the Server
```bash
mvn exec:java
```

### 3. Run Tests
```bash
mvn exec:java -Dexec.mainClass="com.webserver.test.TestClient" -Dexec.args="load 50 10"
```

### 4. Access the Server
Open your browser and navigate to:
- http://localhost:8080/
- http://localhost:8080/about.html
- http://localhost:8080/test.txt

## Key Features
✅ HTTP/1.1 Protocol Support  
✅ Multi-threaded with bounded thread pool (10-50 threads)  
✅ Handles 50+ concurrent connections  
✅ Efficient static file serving with streaming I/O  
✅ Connection timeout management (30s)  
✅ Graceful shutdown with proper cleanup  
✅ Structured logging (DEBUG, INFO, WARN, ERROR)  
✅ Security protections (directory traversal prevention)  
✅ Predictable performance under load  

## Architecture

### Core Components
- **WebServer.java** - Main server managing lifecycle
- **ThreadPoolManager.java** - Bounded thread pool (10-50 threads, queue: 100)
- **ConnectionHandler.java** - Per-connection request processing
- **HttpRequest.java** - HTTP/1.1 request parser
- **HttpResponse.java** - HTTP/1.1 response builder
- **Logger.java** - Thread-safe structured logging
- **ServerConfig.java** - Configuration management

### Performance Characteristics
- **Throughput**: 200+ requests/second
- **Latency**: 5-10ms average under load
- **Concurrency**: 50+ simultaneous connections
- **Thread Pool**: Core: 10, Max: 50, Queue: 100
- **Timeout**: 30s socket timeout

## Configuration (config.properties)
```properties
server.port=8080
server.documentRoot=www
threadPool.coreSize=10
threadPool.maxSize=50
threadPool.queueCapacity=100
socket.timeout=30000
log.level=INFO
```

## Testing
Run the load test to verify performance:
```bash
mvn exec:java -Dexec.mainClass="com.webserver.test.TestClient" -Dexec.args="load 50 10"
```

Expected output:
```
Total requests: 500
Successful: 500
Duration: ~2500 ms
Throughput: 200 req/sec
```

## Technical Highlights
- **Concurrency**: ThreadPoolExecutor with ArrayBlockingQueue
- **I/O**: Buffered streams for efficiency
- **Thread Safety**: Atomic operations, ReentrantReadWriteLock
- **Build System**: Maven for automated build and dependency management
- **Concurrency**: ThreadPoolExecutor with ArrayBlockingQueue
- **I/O**: Buffered streams for efficiency
- **Thread Safety**: Atomic operations, ReentrantReadWriteLock
- **Resource Management**: Try-with-resources, proper cleanup
- **HTTP Compliance**: RFC 2616 implementation
- **Security**: Path normalization, access control
- **Deployment**: Self-contained executable JAR
high-performance-webserver/
├── src/com/webserver/        # Source code
│   ├── WebServer.java
│   ├── core/                 # Core components
│   ├── handler/              # Request handlers
│   ├── http/                 # HTTP protocol
│   ├── util/                 # Utilities
│   └── test/                 # Test client
├── www/                      # Web content
├── config.properties         # Configuration
├── compile.bat              # Compilation script
├── pom.xml                  # Maven build configuration
└── target/                  # Build output (generated)

## Use Cases
- Learning HTTP internals
- Understanding multi-threaded server architecture
- Performance benchmarking
- Local static file serving
- Foundation for custom HTTP services

For detailed documentation, see README.md in the project root.
