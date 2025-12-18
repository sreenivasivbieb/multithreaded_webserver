# Multi-Threaded Java Web Server

A simple HTTP/1.1 web server I built to understand how web servers work and learn about concurrent programming in Java.

## What This Project Does

This is a basic web server that can serve files (HTML, CSS, images, etc.) to multiple users at the same time. When you visit `http://localhost:8080/index.html`, the server reads the file from the `www` folder and sends it back to your browser.

## Key Concepts I Learned

### 1. Socket Programming
- A socket is like a phone connection between the server and client
- The server listens on a port (8080) waiting for connections
- When a client connects, both sides can send/receive data

```java
ServerSocket serverSocket = new ServerSocket(8080);  // Listen on port 8080
Socket clientSocket = serverSocket.accept();          // Wait for a client
```

### 2. Multi-Threading
- Without threads, the server can only handle one client at a time
- With threads, multiple clients can be served simultaneously
- Each client connection runs in its own thread

**Problem:** If 50 people connect, we'd create 50 threads. Too many threads = crash!

### 3. Thread Pool (The Important Part)
- Instead of creating unlimited threads, we use a **thread pool**
- Think of it like a restaurant with 10-50 waiters (threads)
- When a customer (client) arrives, an available waiter serves them
- When done, the waiter becomes available again
- If all waiters are busy, customers wait in line (queue)

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    10,    // Start with 10 threads
    50,    // Max 50 threads
    60,    // Kill idle threads after 60 seconds
    TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100)  // Queue up to 100 requests
);
```

### 4. HTTP Protocol Basics
HTTP is how browsers and servers talk. A request looks like:
```
GET /index.html HTTP/1.1
Host: localhost
```

A response looks like:
```
HTTP/1.1 200 OK
Content-Type: text/html
Content-Length: 1234

<html>...</html>
```

### 5. Resource Management
- Every socket connection uses system resources (memory, file handles)
- Must close connections properly or you'll run out of resources
- Used `try-with-resources` to ensure cleanup happens

## Project Structure

```
src/com/webserver/
├── WebServer.java              # Main server - accepts connections
├── core/
│   ├── ServerConfig.java       # Reads config file
│   └── ThreadPoolManager.java  # Manages the thread pool
├── handler/
│   └── ConnectionHandler.java  # Handles one client request
├── http/
│   ├── HttpRequest.java        # Parses HTTP requests
│   └── HttpResponse.java       # Builds HTTP responses
├── util/
│   └── Logger.java             # Prints log messages
└── test/
    └── TestClient.java         # Tests the server with multiple clients

www/                # Files the server serves (HTML, CSS, etc.)
config.properties   # Server settings (port, threads, etc.)
```

## How It Works (Step by Step)

```
1. Server starts and listens on port 8080
   ↓
2. Browser connects: "GET /index.html"
   ↓
3. Server gives connection to a thread from the pool
   ↓
4. Thread reads the request
   ↓
5. Thread finds www/index.html and reads it
   ↓
6. Thread sends: "HTTP/1.1 200 OK" + file content
   ↓
7. Connection closes, thread returns to pool
   ↓
8. Thread is ready for next client
```

**Key Point:** Multiple clients can be at different steps simultaneously because each has their own thread!

## How to Run

**Requirements:** Java JDK 8 or newer

**Step 1: Compile**
```bash
javac -d bin src/com/webserver/*.java src/com/webserver/core/*.java src/com/webserver/handler/*.java src/com/webserver/http/*.java src/com/webserver/util/*.java src/com/webserver/test/*.java
```

**Step 2: Run**
```bash
java -cp bin com.webserver.WebServer
```

**Step 3: Test**
Open http://localhost:8080 in your browser

Press `Ctrl+C` to stop the server.

### Load Testing
Test with multiple clients (in another terminal):
```bash
java -cp bin com.webserver.test.TestClient load 50 10
```
This simulates 50 clients making 10 requests each.

## Configuration

Edit `config.properties` to change settings:

```properties
server.port=8080           # Server port
threadPool.coreSize=10     # Starting number of threads
threadPool.maxSize=50      # Maximum threads
threadPool.queueCapacity=100  # How many requests can wait in queue
socket.timeout=30000       # 30 second timeout for connections
```

## What I Learned About Concurrency

### The Problem Without Thread Pool:
```
Client 1 connects → Create Thread 1
Client 2 connects → Create Thread 2
Client 3 connects → Create Thread 3
...
Client 1000 connects → Create Thread 1000 → CRASH! (Out of memory)
```

### The Solution With Thread Pool:
```
Start with 10 threads ready to work
Client 1 connects → Use Thread 1
Client 2 connects → Use Thread 2
...
Client 11 connects → Wait in queue
When Thread 1 finishes → Thread 1 handles Client 11
```

This is much more efficient!

## Security Features

**Directory Traversal Prevention:**
Prevents requests like `GET /../../../etc/passwd` from accessing files outside the `www` folder.

**Timeout Handling:**
If a client connects but doesn't send a request for 30 seconds, the connection is closed to free resources.

## Common Issues

**Port already in use?**
Change `server.port` in config.properties to 8081 or another port.

**Can't find www folder?**
Make sure you run from the project root where `www/` and `config.properties` exist.

**Compilation error?**
Create the bin directory first: `mkdir bin`

## Challenges I Faced

1. **Thread synchronization** - Making sure multiple threads don't mess up shared data
2. **Resource cleanup** - Ensuring sockets close properly even when errors occur
3. **HTTP parsing** - Understanding request/response format
4. **Debugging concurrent issues** - Much harder than single-threaded programs!

## What This Project Demonstrates

✓ Socket programming (network communication)  
✓ Multi-threaded programming (concurrency)  
✓ Thread pool pattern (efficient resource use)  
✓ HTTP protocol understanding  
✓ Resource management (cleanup, timeouts)  
✓ Basic security (directory traversal prevention)
