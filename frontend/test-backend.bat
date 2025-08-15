@echo off
echo ========================================
echo 测试任务跟踪系统 - 后端连接测试
echo ========================================
echo.

REM 检查Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到Node.js，请先安装Node.js
    pause
    exit /b 1
)

REM 检查axios依赖
if not exist "node_modules\axios" (
    echo 正在安装axios依赖...
    npm install axios
)

REM 检查环境配置文件
if exist ".env.development" (
    echo 使用 .env.development 中的配置
) else (
    echo 使用默认配置 (http://localhost:8080)
)

echo.
echo 开始测试后端连接...
echo.

node test-connection.js

echo.
echo 测试完成，按任意键退出...
pause >nul
