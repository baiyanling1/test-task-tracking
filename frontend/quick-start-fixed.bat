@echo off
REM 确保窗口不会立即关闭
setlocal enabledelayedexpansion

REM 设置编码
chcp 65001 >nul 2>&1

REM 切换到脚本所在目录
pushd "%~dp0" 2>nul
if errorlevel 1 (
    echo [错误] 无法切换到脚本目录: %~dp0
    echo 当前目录: %CD%
    echo.
    echo 按任意键退出...
    pause
    exit /b 1
)

REM 检查是否在正确的目录
if not exist "package.json" (
    echo [错误] 未找到 package.json 文件
    echo 当前目录: %CD%
    echo 请确保在 frontend 目录下运行此脚本
    echo.
    echo 按任意键退出...
    pause
    exit /b 1
)

echo ========================================
echo 测试任务跟踪系统 - 前端开发服务器
echo ========================================
echo.
echo 当前目录: %CD%
echo.

REM 检查Node.js - 使用更安全的方式
echo [1/6] 检查Node.js环境...
where node >nul 2>&1
if errorlevel 1 (
    echo [X] Node.js 未安装或未添加到PATH
    echo.
    echo 请安装Node.js: https://nodejs.org/
    echo 安装时请选择"Add to PATH"选项
    echo.
    echo 按任意键退出...
    pause
    popd
    exit /b 1
)

REM 显示Node.js版本
for /f "tokens=*" %%i in ('node --version 2^>^&1') do (
    set NODE_VERSION=%%i
    echo [√] Node.js已安装: !NODE_VERSION!
)
echo.

REM 检查npm
echo [2/6] 检查npm环境...
where npm >nul 2>&1
if errorlevel 1 (
    echo [X] npm 未找到
    echo 请重新安装Node.js
    echo.
    echo 按任意键退出...
    pause
    popd
    exit /b 1
)

for /f "tokens=*" %%i in ('npm --version 2^>^&1') do (
    set NPM_VERSION=%%i
    echo [√] npm已安装: !NPM_VERSION!
)
echo.

REM 检查node_modules
echo [3/6] 检查依赖包...
if not exist "node_modules" (
    echo [!] 未找到 node_modules 目录
    echo 正在安装依赖，这可能需要几分钟...
    echo.
    call npm install
    if errorlevel 1 (
        echo.
        echo [X] 依赖安装失败
        echo 请检查网络连接
        echo.
        echo 按任意键退出...
        pause
        popd
        exit /b 1
    )
    echo.
    echo [√] 依赖安装完成
) else (
    echo [√] 依赖已安装
)
echo.

REM 检查环境配置文件
echo [4/6] Checking environment config...
if not exist ".env.development" (
    if exist "env.example" (
        echo [!] Creating .env.development file...
        copy "env.example" ".env.development" >nul 2>&1
        if errorlevel 1 (
            echo [Warning] Failed to create, but can continue
        ) else (
            echo [OK] Created .env.development
            echo [Tip] Please edit this file to set backend URL
        )
    ) else (
        echo [Warning] env.example not found
    )
) else (
    echo [OK] Environment config exists
    REM 检查并修复端口配置
    findstr /C:"VITE_DEV_PORT=3000" ".env.development" >nul 2>&1
    if not errorlevel 1 (
        echo [Warning] Found VITE_DEV_PORT=3000, updating to 5173...
        powershell -Command "(Get-Content '.env.development') -replace 'VITE_DEV_PORT=3000', 'VITE_DEV_PORT=5173' | Set-Content '.env.development'"
        echo [OK] Port updated to 5173
    )
)
echo.

REM 检查vite配置
echo [5/6] 检查Vite配置...
if not exist "vite.config.js" (
    echo [X] 未找到 vite.config.js
    echo.
    echo 按任意键退出...
    pause
    popd
    exit /b 1
)
echo [√] Vite配置正常
echo.

REM 启动服务器
echo [6/6] Starting dev server...
echo ========================================
echo Frontend URL: http://localhost:5173
echo If port is occupied, Vite will try next available port
echo.
echo Press Ctrl+C to stop server
echo ========================================
echo.

REM 启动开发服务器
call npm run dev
set EXIT_CODE=%errorlevel%

REM 如果服务器正常启动，这里不会执行（因为npm run dev会一直运行）
REM 只有当启动失败时才会到这里
if %EXIT_CODE% neq 0 (
    echo.
    echo ========================================
    echo [错误] 开发服务器启动失败
    echo 错误代码: %EXIT_CODE%
    echo ========================================
    echo.
    echo Possible reasons:
    echo 1. Port is occupied
    echo 2. Dependency issues
    echo 3. Configuration error
    echo.
)

popd
echo.
echo Press any key to exit...
pause >nul
exit /b %EXIT_CODE%
