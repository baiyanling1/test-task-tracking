@echo off
echo ========================================
echo 测试任务跟踪系统 - 前端开发服务器
echo ========================================
echo.

REM 检查Node.js
echo 检查Node.js环境...
node --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到Node.js，请先安装Node.js
    echo 下载地址：https://nodejs.org/
    pause
    exit /b 1
)

echo [成功] Node.js已安装
echo.

REM 检查并安装依赖
if not exist "node_modules" (
    echo 首次运行，正在安装依赖...
    npm install
    if errorlevel 1 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
    echo [成功] 依赖安装完成
    echo.
)

REM 检查环境配置文件
if not exist ".env.development" (
    echo [提示] 未找到环境配置文件，正在创建...
    copy "env.example" ".env.development" >nul
    echo [成功] 已创建 .env.development 文件
    echo [重要] 请编辑 .env.development 文件，设置正确的后端服务地址
    echo.
)

echo 启动开发服务器...
echo 前端地址：http://localhost:3000
echo.
echo 按 Ctrl+C 停止服务器
echo ========================================
echo.

npm run dev

pause
