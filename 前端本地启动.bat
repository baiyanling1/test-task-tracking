@echo off
echo ========================================
echo 测试任务跟踪系统 - 前端本地开发
echo ========================================

echo.
echo 检查Node.js环境...
node --version
if %errorlevel% neq 0 (
    echo 错误: 未找到Node.js环境，请先安装Node.js 16+
    pause
    exit /b 1
)

echo.
echo 检查npm环境...
npm --version
if %errorlevel% neq 0 (
    echo 错误: 未找到npm环境
    pause
    exit /b 1
)

echo.
echo 进入前端目录...
cd frontend

echo.
echo 安装依赖...
npm install
if %errorlevel% neq 0 (
    echo 错误: 依赖安装失败
    pause
    exit /b 1
)

echo.
echo 启动前端开发服务器...
echo 前端服务启动后访问: http://localhost:3000
echo API代理到: http://localhost:8080
echo.

npm run dev

pause 