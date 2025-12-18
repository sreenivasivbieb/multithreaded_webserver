# Build & Run Instructions

## Prerequisites
Make sure you have installed:
- **Java JDK 8+**: `java -version`
- **Apache Maven 3.6+**: `mvn -version`

If Maven is not installed, download from: https://maven.apache.org/download.cgi

## Build Commands

### 1. Clean and Compile
```bash
mvn clean compile
```
This compiles all Java source files.

### 2. Package as JAR
```bash
mvn clean package
```
Creates executable JAR in `target/` directory.

### 3. Run Server
```bash
# Option 1: Using Maven
mvn exec:java

# Option 2: Using JAR (after mvn package)
java -jar target/high-performance-webserver-1.0.0-jar-with-dependencies.jar
```

### 4. Run Tests
```bash
# Simple test
mvn exec:java -Dexec.mainClass="com.webserver.test.TestClient"

# Load test with 50 clients, 10 requests each
mvn exec:java -Dexec.mainClass="com.webserver.test.TestClient" -Dexec.args="load 50 10"
```

## Verify Installation

### Check Java
```bash
java -version
```
Expected output: java version "1.8.0_xxx" or higher

### Check Maven
```bash
mvn -version
```
Expected output: Apache Maven 3.x.x

### First Build
```bash
# From project root directory
cd high-performance-webserver
mvn clean package
```

If successful, you'll see:
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXX s
```

## Common Issues

### Issue: "mvn: command not found"
**Solution**: Install Maven or add to PATH
- Windows: Add Maven bin directory to System Environment Variables
- Mac/Linux: `export PATH=/path/to/maven/bin:$PATH`

### Issue: "JAVA_HOME not set"
**Solution**: Set JAVA_HOME environment variable
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_xxx

# Mac/Linux
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
```

### Issue: Port 8080 already in use
**Solution**: 
1. Stop other application using port 8080
2. Or change port in `config.properties`: `server.port=8081`

### Issue: Cannot find config.properties
**Solution**: Run from project root directory where config.properties exists

## Testing the Server

Once running, test in browser:
- http://localhost:8080/
- http://localhost:8080/about.html
- http://localhost:8080/test.html
- http://localhost:8080/test.txt

Or use curl:
```bash
curl http://localhost:8080/
```

## Stopping the Server

Press `Ctrl+C` in the terminal where server is running.

## Project Structure After Build
```
high-performance-webserver/
├── src/                    # Source code
├── target/                 # Build output (generated)
│   ├── classes/           # Compiled .class files
│   └── *.jar              # Executable JARs
├── www/                   # Web content
├── config.properties      # Configuration
└── pom.xml               # Maven build config
```

## For Resume/Portfolio

When showcasing this project:
1. Push to GitHub (excluding target/ directory - see .gitignore)
2. Include README.md with architecture details
3. Mention: "Built with Maven for automated build and dependency management"
4. Highlight: "Zero external runtime dependencies - pure Java implementation"

## Performance Testing

Run load test:
```bash
mvn exec:java -Dexec.mainClass="com.webserver.test.TestClient" -Dexec.args="load 50 10"
```

Expected results:
- Total requests: 500
- Successful: 500
- Throughput: 200+ req/sec
- Average latency: 5-10ms
