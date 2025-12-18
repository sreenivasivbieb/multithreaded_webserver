@echo off
echo Starting High-Performance Web Server...
echo.
echo Server Configuration:
echo - Port: 8080
echo - Document Root: www\
echo - Thread Pool: 10-50 threads
echo.
echo Press Ctrl+C to stop the server
echo ================================
echo.

java -cp bin com.webserver.WebServer
