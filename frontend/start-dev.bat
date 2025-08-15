@echo off
echo 启动前端开发服务器...
echo 请确保已安装Node.js和npm
echo.

REM 检查是否安装了Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo 错误：未找到Node.js，请先安装Node.js
    pause
    exit /b 1
)

REM 检查是否安装了npm
npm --version >nul 2>&1
if errorlevel 1 (
    echo 错误：未找到npm，请先安装npm
    pause
    exit /b 1
)

REM 安装依赖（如果node_modules不存在）
if not exist "node_modules" (
    echo 正在安装依赖...
    npm install
    if errorlevel 1 (
        echo 错误：依赖安装失败
        pause
        exit /b 1
    )
)

echo 启动开发服务器...
echo 前端服务将在 http://localhost:3000 启动
echo 请确保已正确配置后端服务地址
echo.
npm run dev

pause
