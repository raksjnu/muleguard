@echo off
echo Stopping all Java processes...
taskkill /F /IM java.exe /T 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Java processes stopped.
) else (
    echo No Java processes found.
)

echo.
echo Waiting 2 seconds...
timeout /t 2 /nobreak >nul

echo.
echo Building MuleGuard...
mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================
    echo   BUILD SUCCESSFUL!
    echo ============================================================
    echo.
    echo You can now run: start-gui.bat
    echo.
) else (
    echo.
    echo ============================================================
    echo   BUILD FAILED!
    echo ============================================================
    echo.
)

pause
