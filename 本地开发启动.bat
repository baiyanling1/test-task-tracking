@echo off
echo ========================================
echo 测试任务跟踪系统 - 本地开发环境
echo ========================================

echo.
echo 检查环境...
echo Java版本:
java -version
echo.
echo Maven版本:
mvn -version
echo.
echo Node.js版本:
node --version
echo.

echo 请确保虚拟机服务正常运行:
echo MySQL: 10.18.40.48:3306
echo Redis: 10.18.40.48:6379
echo.

echo 选择启动方式:
echo 1. 启动后端服务 (端口8080)
echo 2. 启动前端服务 (端口3000)
echo 3. 启动完整环境 (后端+前端)
echo 4. 退出
echo.

set /p choice=请输入选择 (1-4): 

if "%choice%"=="1" goto backend
if "%choice%"=="2" goto frontend
if "%choice%"=="3" goto full
if "%choice%"=="4" goto exit
goto invalid

:backend
echo.
echo 启动后端服务...
mvn spring-boot:run -Dspring-boot.run.profiles=dev
goto end

:frontend
echo.
echo 启动前端服务...
cd frontend
npm install
npm run dev
goto end

:full
echo.
echo 启动完整开发环境...
echo 请打开两个命令行窗口，分别运行:
echo 窗口1: 本地启动.bat
echo 窗口2: 前端本地启动.bat
echo.
echo 或者手动执行:
echo 后端: mvn spring-boot:run -Dspring-boot.run.profiles=dev
echo 前端: cd frontend ^&^& npm run dev
echo.
echo 服务启动后:
echo 前端: http://localhost:3000
echo 后端: http://localhost:8080
echo API文档: http://localhost:8080/swagger-ui.html
goto end

:invalid
echo 无效选择，请重新运行脚本
goto end

:exit
echo 退出本地开发环境
goto end

:end
pause 