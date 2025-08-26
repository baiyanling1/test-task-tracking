@echo off
echo 正在修复生产环境Docker网络配置冲突...

REM 检查当前运行状态
echo 1. 检查当前运行状态...
docker-compose -f docker-compose.prod.yml ps

REM 停止所有运行的容器
echo 2. 停止所有运行的容器...
docker-compose -f docker-compose.prod.yml down

REM 删除现有的Docker网络
echo 3. 删除现有的Docker网络...
docker network prune -f

REM 重启Docker服务以应用新的daemon.json配置
echo 4. 重启Docker服务...
net stop docker
net start docker

REM 等待Docker服务完全启动
echo 5. 等待Docker服务启动...
timeout /t 15 /nobreak

REM 重新创建网络和启动服务
echo 6. 重新启动生产环境服务...
docker-compose -f docker-compose.prod.yml up -d

REM 等待服务启动
echo 7. 等待服务启动...
timeout /t 30 /nobreak

echo 8. 检查网络配置...
docker network ls | findstr test_tracking
docker network inspect test_tracking_test_tracking_network

echo 9. 检查服务状态...
docker-compose -f docker-compose.prod.yml ps

echo 修复完成！
echo 新的网络配置：
echo - Docker默认网段：192.168.0.0/16
echo - 项目网络：192.168.100.0/24
echo.
echo 请检查路由表确认冲突已解决：
echo route -n
echo.
echo 检查服务访问：
echo - 前端: http://localhost:3000
echo - 后端API: http://localhost:8080
echo - MySQL: localhost:3306
echo - Redis: localhost:6379

pause
