#!/bin/bash

echo "=== 在虚拟机上配置Docker镜像源 ==="

# 创建Docker配置目录
mkdir -p /etc/docker

# 创建daemon.json配置文件
cat > /etc/docker/daemon.json << EOF
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com",
    "https://ccr.ccs.tencentyun.com"
  ],
  "insecure-registries": [],
  "debug": false,
  "experimental": false
}
EOF

echo "Docker镜像源配置完成"

# 重启Docker服务
echo "重启Docker服务..."
systemctl daemon-reload
systemctl restart docker

echo "Docker服务重启完成"

# 验证配置
echo "验证Docker配置..."
docker info | grep -A 10 "Registry Mirrors"

echo "=== 开始构建和部署应用 ==="

# 停止现有容器
echo "停止现有容器..."
docker-compose down

# 清理镜像缓存
echo "清理镜像缓存..."
docker system prune -f

# 重新构建
echo "重新构建应用..."
docker-compose build --no-cache

# 启动服务
echo "启动服务..."
docker-compose up -d

echo "=== 部署完成 ==="
echo "检查服务状态..."
docker-compose ps
