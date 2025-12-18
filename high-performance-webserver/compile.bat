@echo off
echo Compiling High-Performance Web Server...
echo.

if not exist "bin" mkdir bin

javac -d bin src\com\webserver\*.java src\com\webserver\core\*.java src\com\webserver\handler\*.java src\com\webserver\http\*.java src\com\webserver\util\*.java src\com\webserver\test\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compilation successful!
    echo.
    echo To run the server, execute: run-server.bat
    echo To run tests, execute: run-tests.bat
) else (
    echo.
    echo Compilation failed! Please check the error messages above.
)

pause
