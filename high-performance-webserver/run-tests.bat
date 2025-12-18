@echo off
echo Web Server Test Client
echo ======================
echo.
echo Select test mode:
echo 1. Simple Test (few requests)
echo 2. Load Test (multiple concurrent clients)
echo.

set /p choice="Enter choice (1 or 2): "

if "%choice%"=="1" (
    echo.
    echo Running simple test...
    java -cp bin com.webserver.test.TestClient
) else if "%choice%"=="2" (
    echo.
    set /p clients="Enter number of concurrent clients [default: 50]: "
    set /p requests="Enter requests per client [default: 10]: "
    
    if "%clients%"=="" set clients=50
    if "%requests%"=="" set requests=10
    
    echo.
    echo Running load test with %clients% clients, %requests% requests each...
    java -cp bin com.webserver.test.TestClient load %clients% %requests%
) else (
    echo Invalid choice!
)

echo.
pause
