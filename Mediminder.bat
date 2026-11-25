@echo off
setlocal
cd /d "%~dp0"

where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven is required. Install Maven and Java, then double-click this file again.
    pause
    exit /b 1
)

where javaw >nul 2>nul
if errorlevel 1 (
    echo Java is required. Install Java and add it to PATH, then try again.
    pause
    exit /b 1
)

mvn -q -DskipTests package
if errorlevel 1 (
    echo MediMinder could not be built. Review the Maven output above.
    pause
    exit /b 1
)

start "" javaw -jar "target\mediminder-1.0-SNAPSHOT.jar"
