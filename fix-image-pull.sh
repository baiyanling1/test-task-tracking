#!/bin/bash

echo "正在解决镜像拉取问题..."

# 检查网络连接
echo "1. 检查网络连接..."
if ping -c 1 8.8.8.8 > /dev/null 2>&1; then
    echo "网络连接正常"
else
    echo "网络连接异常，请检查网络设置"
    exit 1
fi

# 测试阿里云镜像仓库连接
echo "2. 测试阿里云镜像仓库连接..."
if ping -c 1 registry.cn-hangzhou.aliyuncs.com > /dev/null 2>&1; then
    echo "阿里云镜像仓库连接正常"
else
    echo "阿里云镜像仓库连接异常，将使用官方镜像"
    # 备份原Dockerfile
    cp frontend/Dockerfile frontend/Dockerfile.aliyun
    # 使用备用Dockerfile
    cp frontend/Dockerfile.backup frontend/Dockerfile
fi

# 清理Docker缓存
echo "3. 清理Docker缓存..."
docker system prune -f

# 尝试拉取镜像
echo "4. 尝试拉取镜像..."

# 尝试拉取Node.js镜像
echo "拉取 Node.js 镜像..."
if docker pull registry.cn-hangzhou.aliyuncs.com/library/node:18-alpine; then
    echo "成功拉取阿里云Node.js镜像"
else
    echo "尝试拉取官方Node.js镜像..."
    docker pull node:18-alpine
fi

# 尝试拉取Nginx镜像
echo "拉取 Nginx 镜像..."
if docker pull registry.cn-hangzhou.aliyuncs.com/library/nginx:alpine; then
    echo "成功拉取阿里云Nginx镜像"
else
    echo "尝试拉取官方Nginx镜像..."
    docker pull nginx:alpine
fi

# 拉取其他镜像
echo "拉取 MySQL 镜像..."
docker pull mysql:5.7

echo "拉取 Redis 镜像..."
docker pull redis:7-alpine

echo "镜像拉取完成！"
echo ""
echo "现在可以尝试重新构建服务："
echo "docker-compose -f docker-compose.prod.yml up -d --build"
