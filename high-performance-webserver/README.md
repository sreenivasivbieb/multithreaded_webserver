# Multi-Threaded Java Web Server

A simple HTTP/1.1 web server built in Java that can handle multiple clients at the same time using a thread pool.

## Features

- HTTP/1.1 protocol support
- Multi-threaded with thread pool (10-50 threads)
- Serves static files (HTML, CSS, JavaScript, images, etc.)
- Handles multiple concurrent connections
- Connection timeout handling
- Simple logging system
- Directory listing

## What I Learned

Building this project helped me understand:
- How web servers work under the hood
- Multi-threaded programming and thread pools
- Socket programming in Java
- HTTP protocol basics
- Resource management and cleanup
- Handling concurrent requests safely

## Project Structure

```
src/com/webserver/
├── WebServer.java              # Main server
├── core/
│   ├── ServerConfig.java       # Configuration
│   └── ThreadPoolManager.java  # Thread pool
├── handler/
│   └── ConnectionHandler.java  # Handles each request
├── http/
│   ├── HttpRequest.java        # Request parser
│   └── HttpResponse.java       # Response builder
├── util/
│   └── Logger.java             # Logging
└── test/
    └── TestClient.java         # Testing tool

www/                            # Web files
config.properties              # Settings
pom.xml                        # Maven config
```

## 🛠️ Technical Implementation

### Build System
- **Maven** - Industry-standard dependency management and build automation
- **Automated Compilation** - Single-command build process
- *How It Works

1. Server starts and listens on port 8080
2. When a client connects, the connection is given to a worker thread from the pool
3. The worker thread reads the HTTP request
4. It finds and serves the requested file from the `www` folder
5. Sends the response back to the client
6. Connection is closed and thread returns to the pool

The thread pool ensures we don't create too many threads and crash the system. or higher
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

### Testing

Open in browser:
- http://localhost:8080/
- http://localhost:8080/about.html
- http://localhost:8080/test.txt

Run load test (in another terminal):
```bash
# Simple test
java -cp bin com.webserver.test.TestClient

# Load test with 50 clients
java -cp bin com.webserver.test.TestClient load 50 10
```

## Configuration

You can change settings in `config.properties`:

```properties
server.port=8080                # Change port
server.documentRoot=www         # Where web files are
threadPool.coreSize=10          # Starting threads
threadPool.maxSize=50           # Max threads
socket.timeout=30000            # Connection timeout
log.level=INFO                  # Logging detail
```

## Performance

The thread pool handles around 50 concurrent clients pretty well without crashing.

Example test result:
```
50 clients x 10 requests = 500 total requests
Successful: 500
Failed: 0
Time: ~2-3 seconds
```

## Technical Details

**Thread Pool:**
- Starts with 10 threads
- Can grow up to 50 threads
- Queues up to 100 requests

**Features:**
- Parses HTTP requests properly
- Serves different file types (HTML, CSS, JS, images)
- Handles timeouts so connections don't hang forever
- Cleans up resources properly
- Simple logging to track what's happening

## Challenges I Faced

- Getting thread synchronization right took some time
- Understanding how HTTP requests are structured
- Making sure resources get cleaned up properly
- Preventing directory traversal security issues
- Debugging concurrent issues

## Future Improvements

- Add HTTPS support
- Implement keep-alive connections
- Add caching for better performance
- Support for larger files
- Better error pages
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

## Common Issues

**Port 8080 already in use?**
Change the port in `config.properties` to something like 8081.

**Can't find config.properties?**
Make sure you're running from the project root folder.

**Compilation errors?**
Make sure the `bin` directory exists: `mkdir bin`

## What's Next

This was a fun learning project! It helped me understand how real web servers like Apache or Nginx work behind the scenes. The concurrent programming part was challenging but rewarding.
