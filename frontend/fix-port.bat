@echo off
echo Fixing port configuration in .env.development...
echo.

if not exist ".env.development" (
    echo .env.development not found, creating from env.example...
    if exist "env.example" (
        copy "env.example" ".env.development" >nul
        echo Created .env.development
    ) else (
        echo env.example not found!
        pause
        exit /b 1
    )
)

echo Checking current port setting...
findstr /C:"VITE_DEV_PORT" ".env.development"
echo.

echo Updating VITE_DEV_PORT to 5173...
powershell -Command "(Get-Content '.env.development') -replace 'VITE_DEV_PORT=\d+', 'VITE_DEV_PORT=5173' | Set-Content '.env.development'"

echo.
echo Updated configuration:
findstr /C:"VITE_DEV_PORT" ".env.development"
echo.
echo Done! You can now run quick-start-fixed.bat
pause
