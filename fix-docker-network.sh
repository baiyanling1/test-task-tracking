#!/bin/bash

echo "正在修复Docker网络配置冲突..."

# 停止所有运行的容器
echo "1. 停止所有运行的容器..."
docker-compose -f docker-compose.prod.yml down

# 删除现有的Docker网络
echo "2. 删除现有的Docker网络..."
docker network prune -f

# 重启Docker服务以应用新的daemon.json配置
echo "3. 重启Docker服务..."
sudo systemctl restart docker

# 等待Docker服务完全启动
echo "4. 等待Docker服务启动..."
sleep 10

# 重新创建网络和启动服务
echo "5. 重新启动服务..."
docker-compose -f docker-compose.prod.yml up -d

echo "6. 检查网络配置..."
docker network ls
docker network inspect test_tracking_test_tracking_network

echo "修复完成！"
echo "新的网络配置："
echo "- Docker默认网段：192.168.0.0/16"
echo "- 项目网络：192.168.100.0/24"
echo ""
echo "请检查路由表确认冲突已解决："
echo "route -n"
