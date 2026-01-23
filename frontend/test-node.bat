@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo Node.js 环境测试
echo ========================================
echo.

echo [测试1] 检查当前目录...
echo 当前目录: %CD%
echo.

echo [测试2] 尝试执行 node 命令...
where node >nul 2>&1
if errorlevel 1 (
    echo [失败] 系统PATH中未找到 node.exe
    echo.
    echo 请检查：
    echo 1. Node.js 是否已安装
    echo 2. Node.js 是否添加到系统PATH
    echo.
) else (
    echo [成功] 找到 node.exe
    for /f "tokens=*" %%i in ('where node') do echo 路径: %%i
    echo.
)

echo [测试3] 执行 node --version...
node --version
if errorlevel 1 (
    echo [失败] node --version 执行失败
    echo 错误代码: %errorlevel%
) else (
    echo [成功] node --version 执行成功
)
echo.

echo [测试4] 执行 npm --version...
npm --version
if errorlevel 1 (
    echo [失败] npm --version 执行失败
    echo 错误代码: %errorlevel%
) else (
    echo [成功] npm --version 执行成功
)
echo.

echo ========================================
echo 测试完成
echo ========================================
echo.
echo 按任意键退出...
pause
