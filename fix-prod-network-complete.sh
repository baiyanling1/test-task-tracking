#!/bin/bash

echo "正在执行完整的生产环境Docker网络修复..."

# 检查当前运行状态
echo "1. 检查当前运行状态..."
docker-compose -f docker-compose.prod.yml ps

# 停止所有运行的容器
echo "2. 停止所有运行的容器..."
docker-compose -f docker-compose.prod.yml down

# 删除现有的Docker网络
echo "3. 删除现有的Docker网络..."
docker network prune -f

# 重启Docker服务以应用新的daemon.json配置
echo "4. 重启Docker服务..."
sudo systemctl restart docker

# 等待Docker服务完全启动
echo "5. 等待Docker服务启动..."
sleep 15

# 测试网络连接
echo "6. 测试网络连接..."
ping -c 3 8.8.8.8
ping -c 3 registry.cn-hangzhou.aliyuncs.com

# 清理可能损坏的镜像
echo "7. 清理可能损坏的镜像..."
docker system prune -f

# 预拉取需要的镜像
echo "8. 预拉取需要的镜像..."
echo "拉取 Node.js 镜像..."
docker pull registry.cn-hangzhou.aliyuncs.com/library/node:18-alpine || {
    echo "尝试使用官方镜像..."
    docker pull node:18-alpine
}

echo "拉取 Nginx 镜像..."
docker pull registry.cn-hangzhou.aliyuncs.com/library/nginx:alpine || {
    echo "尝试使用官方镜像..."
    docker pull nginx:alpine
}

echo "拉取 MySQL 镜像..."
docker pull mysql:5.7

echo "拉取 Redis 镜像..."
docker pull redis:7-alpine

# 重新创建网络和启动服务
echo "9. 重新启动生产环境服务..."
docker-compose -f docker-compose.prod.yml up -d --build

# 等待服务启动
echo "10. 等待服务启动..."
sleep 30

echo "11. 检查网络配置..."
docker network ls | grep test_tracking
docker network inspect test_tracking_test_tracking_network

echo "12. 检查服务状态..."
docker-compose -f docker-compose.prod.yml ps

echo "13. 检查容器日志..."
echo "=== 前端容器日志 ==="
docker logs test_tracking_frontend --tail 20

echo "=== 后端容器日志 ==="
docker logs test_tracking_app --tail 20

echo "=== MySQL容器日志 ==="
docker logs test_tracking_mysql --tail 10

echo "修复完成！"
echo "新的网络配置："
echo "- Docker默认网段：192.168.0.0/16"
echo "- 项目网络：192.168.100.0/24"
echo ""
echo "请检查路由表确认冲突已解决："
echo "route -n"
echo ""
echo "检查服务访问："
echo "- 前端: http://localhost:3000"
echo "- 后端API: http://localhost:8080"
echo "- MySQL: localhost:3306"
echo "- Redis: localhost:6379"
echo ""
echo "如果仍有问题，请检查："
echo "1. 网络连接是否正常"
echo "2. Docker服务是否正常运行"
echo "3. 防火墙设置"
