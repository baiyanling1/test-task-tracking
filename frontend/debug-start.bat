@echo off
REM 调试模式启动脚本
echo ========================================
echo 调试模式 - 前端启动脚本
echo ========================================
echo.
echo 这个脚本会显示每一步的执行结果
echo 如果脚本异常退出，请查看最后显示的信息
echo.
echo 按任意键开始...
pause
echo.
echo ========================================
echo 步骤 1: 检查脚本目录
echo ========================================
echo 脚本路径: %~dp0
echo 当前目录: %CD%
echo.
cd /d "%~dp0"
if errorlevel 1 (
    echo [错误] 无法切换到脚本目录
    pause
    exit /b 1
)
echo [成功] 已切换到: %CD%
echo.
pause
echo.

echo ========================================
echo 步骤 2: 检查必要文件
echo ========================================
if exist "package.json" (
    echo [√] package.json 存在
) else (
    echo [X] package.json 不存在
    echo 当前目录: %CD%
    pause
    exit /b 1
)

if exist "vite.config.js" (
    echo [√] vite.config.js 存在
) else (
    echo [X] vite.config.js 不存在
    pause
    exit /b 1
)
echo.
pause
echo.

echo ========================================
echo 步骤 3: 检查Node.js
echo ========================================
where node
if errorlevel 1 (
    echo [X] 未找到 node 命令
    pause
    exit /b 1
)
echo [√] 找到 node 命令
node --version
if errorlevel 1 (
    echo [X] node --version 执行失败
    pause
    exit /b 1
)
echo [√] Node.js 正常
echo.
pause
echo.

echo ========================================
echo 步骤 4: 检查npm
echo ========================================
where npm
if errorlevel 1 (
    echo [X] 未找到 npm 命令
    pause
    exit /b 1
)
echo [√] 找到 npm 命令
npm --version
if errorlevel 1 (
    echo [X] npm --version 执行失败
    pause
    exit /b 1
)
echo [√] npm 正常
echo.
pause
echo.

echo ========================================
echo 步骤 5: 检查依赖
echo ========================================
if exist "node_modules" (
    echo [√] node_modules 存在
) else (
    echo [!] node_modules 不存在，需要安装
    echo 是否现在安装？(Y/N)
    set /p INSTALL=
    if /i "%INSTALL%"=="Y" (
        echo 正在安装依赖...
        npm install
        if errorlevel 1 (
            echo [X] 安装失败
            pause
            exit /b 1
        )
        echo [√] 安装完成
    )
)
echo.
pause
echo.

echo ========================================
echo 步骤 6: 启动开发服务器
echo ========================================
echo 准备启动 npm run dev...
echo 如果这里退出，说明启动失败
echo.
pause
npm run dev
set ERR=%errorlevel%
echo.
echo ========================================
echo 服务器已停止
echo ========================================
echo 退出代码: %ERR%
echo.
pause
exit /b %ERR%
