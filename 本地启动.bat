@echo off
echo ========================================
echo 测试任务跟踪系统 - 本地开发环境启动
echo ========================================

echo.
echo 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请先安装JDK 8+
    pause
    exit /b 1
)

echo.
echo 检查Maven环境...
mvn -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven环境，请先安装Maven 3.6+
    pause
    exit /b 1
)

echo.
echo 清理并编译项目...
mvn clean compile
if %errorlevel% neq 0 (
    echo 错误: 项目编译失败
    pause
    exit /b 1
)

echo.
echo 启动后端服务...
echo 使用开发环境配置连接到虚拟机数据库
echo MySQL: 10.18.40.48:3306
echo Redis: 10.18.40.48:6379
echo.
echo 服务启动后访问: http://localhost:8080
echo API文档: http://localhost:8080/swagger-ui.html
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=dev

pause 