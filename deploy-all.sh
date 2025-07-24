#!/bin/bash

echo "开始完整部署..."

# 进入前端目录构建
echo "=== 构建前端 ==="
cd frontend
npm install
npm run build
cd ..

# 重新构建所有镜像
echo "=== 重新构建镜像 ==="
docker-compose -f docker-compose.prod.yml build

# 重启所有服务
echo "=== 重启所有服务 ==="
docker-compose -f docker-compose.prod.yml up -d

echo "完整部署完成！"
echo "等待服务启动..."
sleep 15

# 检查所有服务状态
echo "检查服务状态..."
docker-compose -f docker-compose.prod.yml ps

echo "访问地址: http://localhost" 