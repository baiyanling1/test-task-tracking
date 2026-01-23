@echo off
chcp 65001 >nul
echo ========================================
echo 前端环境诊断工具
echo ========================================
echo.

echo [1] 检查Node.js...
node --version
if errorlevel 1 (
    echo [X] Node.js 未安装
) else (
    echo [√] Node.js 已安装
)
echo.

echo [2] 检查npm...
npm --version
if errorlevel 1 (
    echo [X] npm 未安装
) else (
    echo [√] npm 已安装
)
echo.

echo [3] 检查当前目录...
cd
echo 当前目录: %CD%
echo.

echo [4] 检查package.json...
if exist "package.json" (
    echo [√] package.json 存在
) else (
    echo [X] package.json 不存在
    echo 请确保在 frontend 目录下运行此脚本
)
echo.

echo [5] 检查node_modules...
if exist "node_modules" (
    echo [√] node_modules 存在
    dir /b node_modules | find /c /v "" > temp_count.txt
    set /p MODULE_COUNT=<temp_count.txt
    del temp_count.txt
    echo 已安装 !MODULE_COUNT! 个模块
) else (
    echo [X] node_modules 不存在
    echo 需要运行 npm install
)
echo.

echo [6] 检查端口5173占用情况（开发服务器默认端口）...
netstat -ano | findstr ":5173" >nul
if errorlevel 1 (
    echo [√] 端口 5173 未被占用
) else (
    echo [X] 端口 5173 已被占用
    echo 占用端口的进程：
    netstat -ano | findstr ":5173"
    echo [提示] Vite会自动尝试下一个可用端口
)
echo.
echo [6.1] 检查端口3000占用情况（备用）...
netstat -ano | findstr ":3000" >nul
if errorlevel 1 (
    echo [√] 端口 3000 未被占用
) else (
    echo [!] 端口 3000 已被占用
)
echo.

echo [7] 检查vite.config.js...
if exist "vite.config.js" (
    echo [√] vite.config.js 存在
) else (
    echo [X] vite.config.js 不存在
)
echo.

echo [8] 检查环境配置文件...
if exist ".env.development" (
    echo [√] .env.development 存在
) else (
    echo [!] .env.development 不存在
    if exist "env.example" (
        echo 可以复制 env.example 创建
    )
)
echo.

echo [9] 测试vite命令...
npx vite --version >nul 2>&1
if errorlevel 1 (
    echo [X] vite 命令不可用
    echo 可能需要重新安装依赖: npm install
) else (
    echo [√] vite 命令可用
    for /f "tokens=*" %%i in ('npx vite --version') do echo Vite版本: %%i
)
echo.

echo ========================================
echo 诊断完成
echo ========================================
echo.
echo 按任意键退出...
pause >nul