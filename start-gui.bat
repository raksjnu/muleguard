@echo off
REM MuleGuard GUI Launcher for Windows
REM You can change the port by setting the PORT environment variable
REM Example: set PORT=9090 before running this script

echo ============================================================
echo           MuleGuard GUI Launcher
echo ============================================================
echo.

REM Check if JAR exists
if not exist "target\muleguard-1.0.0-jar-with-raks.jar" (
    echo ERROR: JAR file not found!
    echo Please run: mvn clean package
    echo.
    pause
    exit /b 1
)

REM Set default port if not specified
if "%MULEGUARD_PORT%"=="" set MULEGUARD_PORT=8080

echo Starting MuleGuard GUI on port %MULEGUARD_PORT%...
echo.
echo Press Ctrl+C to stop the server
echo.

java -cp target\muleguard-1.0.0-jar-with-raks.jar com.raks.muleguard.gui.MuleGuardGUI %MULEGUARD_PORT%

REM Keep window open if there was an error
if errorlevel 1 (
    echo.
    echo ERROR: Failed to start MuleGuard GUI
    echo.
    pause
)
