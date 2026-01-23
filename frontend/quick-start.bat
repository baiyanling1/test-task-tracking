@echo off
REM 禁用命令回显，但保留错误信息
setlocal enabledelayedexpansion

REM 设置编码为UTF-8
chcp 65001 >nul 2>&1

REM 确保在正确的目录下运行
cd /d "%~dp0" 2>nul
if not exist "package.json" (
    echo [错误] 无法找到 package.json，请确保在 frontend 目录下运行此脚本
    echo 当前目录: %CD%
    echo 脚本路径: %~dp0
    echo.
    echo 按任意键退出...
    pause >nul
    exit /b 1
)

REM 添加错误处理，确保窗口不会立即关闭
if errorlevel 1 (
    echo [错误] 无法切换到脚本目录
    echo 脚本路径: %~dp0
    echo 当前目录: %CD%
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
echo 脚本路径: %~dp0
echo.

REM 检查Node.js
echo 检查Node.js环境...
echo 正在执行: node --version
set NODE_CHECK=0
node --version >nul 2>&1
set NODE_CHECK=%errorlevel%
if %NODE_CHECK% neq 0 (
    echo.
    echo ========================================
    echo [错误] 未找到Node.js
    echo ========================================
    echo.
    echo 可能的原因：
    echo 1. Node.js 未安装
    echo 2. Node.js 未添加到系统PATH环境变量
    echo.
    echo 解决方法：
    echo 1. 下载并安装Node.js: https://nodejs.org/
    echo 2. 安装时选择"Add to PATH"选项
    echo 3. 安装后重启命令行窗口
    echo.
    echo 按任意键退出...
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('node --version 2^>^&1') do set NODE_VERSION=%%i
echo [成功] Node.js已安装 - !NODE_VERSION!
echo.

REM 检查npm
echo 检查npm环境...
echo 正在执行: npm --version
set NPM_CHECK=0
npm --version >nul 2>&1
set NPM_CHECK=%errorlevel%
if %NPM_CHECK% neq 0 (
    echo.
    echo ========================================
    echo [错误] 未找到npm
    echo ========================================
    echo.
    echo 可能的原因：
    echo 1. npm 未正确安装（通常随Node.js一起安装）
    echo 2. Node.js 安装不完整
    echo.
    echo 解决方法：
    echo 1. 重新安装Node.js: https://nodejs.org/
    echo 2. 确保安装时包含npm
    echo.
    echo 按任意键退出...
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('npm --version 2^>^&1') do set NPM_VERSION=%%i
echo [成功] npm已安装 - !NPM_VERSION!
echo.

REM 检查并安装依赖
if not exist "node_modules" (
    echo 首次运行，正在安装依赖...
    echo 这可能需要几分钟，请耐心等待...
    echo.
    npm install
    if errorlevel 1 (
        echo.
        echo [错误] 依赖安装失败
        echo 请检查网络连接和npm配置
        echo.
        echo 按任意键退出...
        pause
        exit /b 1
    )
    echo.
    echo [成功] 依赖安装完成
    echo.
) else (
    echo [提示] 检测到已安装的依赖
    echo.
)

REM 检查package.json是否存在
if not exist "package.json" (
    echo [错误] 未找到 package.json 文件
    echo 请确保在 frontend 目录下运行此脚本
    echo 当前目录: %CD%
    echo.
    echo 按任意键退出...
    pause
    exit /b 1
)

REM 检查环境配置文件
if not exist ".env.development" (
    if exist "env.example" (
        echo [提示] 未找到环境配置文件，正在创建...
        copy "env.example" ".env.development" >nul
        if errorlevel 1 (
            echo [警告] 创建 .env.development 文件失败
        ) else (
            echo [成功] 已创建 .env.development 文件
            echo [重要] 请编辑 .env.development 文件，设置正确的后端服务地址
        )
        echo.
    ) else (
        echo [警告] 未找到 env.example 文件
        echo.
    )
)

echo 启动开发服务器...
echo 前端地址：http://localhost:5173
echo 如果端口被占用，Vite会自动尝试下一个可用端口
echo.
echo 按 Ctrl+C 停止服务器
echo ========================================
echo.

REM 启动开发服务器，捕获错误
npm run dev
set EXIT_CODE=!errorlevel!

if !EXIT_CODE! neq 0 (
    echo.
    echo ========================================
    echo [错误] 开发服务器启动失败
    echo 错误代码: !EXIT_CODE!
    echo ========================================
    echo.
    echo 可能的原因：
    echo 1. 端口 5173 已被占用（Vite会自动尝试其他端口）
    echo 2. 依赖包安装不完整
    echo 3. 配置文件有误
    echo.
    echo 建议操作：
    echo 1. 检查是否有其他程序占用端口 5173
    echo 2. 尝试删除 node_modules 文件夹后重新运行此脚本
    echo 3. 检查 package.json 和 vite.config.js 配置
    echo 4. 可以在 .env.development 中设置 VITE_DEV_PORT 使用其他端口
    echo.
)

echo.
echo ========================================
echo 脚本执行完成
echo ========================================
echo.
echo 按任意键退出...
pause >nul
if defined EXIT_CODE (
    exit /b %EXIT_CODE%
) else (
    exit /b 0
)
