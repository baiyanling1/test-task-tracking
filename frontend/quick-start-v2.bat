@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

pushd "%~dp0" 2>nul
if errorlevel 1 (
    echo [Error] Cannot change to script directory
    pause
    exit /b 1
)

if not exist "package.json" (
    echo [Error] package.json not found
    echo Current directory: %CD%
    pause
    popd
    exit /b 1
)

echo ========================================
echo Test Task Tracking - Frontend Dev Server
echo ========================================
echo.

echo [1/5] Checking Node.js...
where node >nul 2>&1
if errorlevel 1 (
    echo [X] Node.js not found
    echo Please install Node.js from https://nodejs.org/
    pause
    popd
    exit /b 1
)
for /f "tokens=*" %%i in ('node --version 2^>^&1') do echo [OK] Node.js: %%i
echo.

echo [2/5] Checking npm...
where npm >nul 2>&1
if errorlevel 1 (
    echo [X] npm not found
    pause
    popd
    exit /b 1
)
for /f "tokens=*" %%i in ('npm --version 2^>^&1') do echo [OK] npm: %%i
echo.

echo [3/5] Checking dependencies...
if not exist "node_modules" (
    echo [!] Installing dependencies...
    call npm install
    if errorlevel 1 (
        echo [X] Installation failed
        pause
        popd
        exit /b 1
    )
) else (
    echo [OK] Dependencies installed
)
echo.

echo [4/5] Checking environment config...
if not exist ".env.development" (
    if exist "env.example" (
        copy "env.example" ".env.development" >nul 2>&1
        echo [OK] Created .env.development
    )
)
REM Fix port if it's set to 3000
findstr /C:"VITE_DEV_PORT=3000" ".env.development" >nul 2>&1
if not errorlevel 1 (
    echo [!] Fixing port from 3000 to 5173...
    powershell -Command "(Get-Content '.env.development') -replace 'VITE_DEV_PORT=3000', 'VITE_DEV_PORT=5173' | Set-Content '.env.development'"
    echo [OK] Port updated
)
echo [OK] Config ready
echo.

echo [5/5] Starting dev server...
echo ========================================
echo Frontend: http://localhost:5173
echo Press Ctrl+C to stop
echo ========================================
echo.

call npm run dev
set ERR=%errorlevel%

if %ERR% neq 0 (
    echo.
    echo ========================================
    echo [Error] Server failed to start
    echo Exit code: %ERR%
    echo ========================================
    echo.
    echo Common issues:
    echo 1. Port 5173 is occupied
    echo 2. Dependencies not installed correctly
    echo 3. Configuration error
    echo.
    echo Try running: fix-port.bat
    echo.
)

popd
pause
exit /b %ERR%
