# Docker网络冲突修复说明

## 问题描述

虚拟机中的Docker网段（172.18.0.0）与深圳本地网段冲突，导致网络路由问题。

### 当前路由表
```
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
0.0.0.0         10.18.40.254    0.0.0.0         UG    0      0        0 eth0
10.18.40.0      0.0.0.0         255.255.255.0   U     0      0        0 eth0
172.17.0.0      0.0.0.0         255.255.0.0     U     0      0        0 docker0
172.18.0.0      0.0.0.0         255.255.0.0     U     0      0        0 br-f9ce7d4b830e
```

## 解决方案

### 1. 修改Docker Daemon配置

在 `daemon.json` 中添加了自定义网段配置：
```json
{
  "default-address-pools": [
    {
      "base": "192.168.0.0/16",
      "size": 24
    }
  ]
}
```

### 2. 修改Docker Compose配置

在 `docker-compose.prod.yml` 中指定了项目网络网段：
```yaml
networks:
  test_tracking_network:
    driver: bridge
    ipam:
      config:
        - subnet: 192.168.100.0/24
```

## 修复步骤

### 方法一：使用脚本（推荐）

**Linux/Mac:**
```bash
chmod +x fix-docker-network.sh
./fix-docker-network.sh
```

**Windows:**
```cmd
fix-docker-network.bat
```

### 方法二：手动执行

1. 停止所有容器：
   ```bash
   docker-compose -f docker-compose.prod.yml down
   ```

2. 清理网络：
   ```bash
   docker network prune -f
   ```

3. 重启Docker服务：
   ```bash
   # Linux
   sudo systemctl restart docker
   
   # Windows
   net stop docker
   net start docker
   ```

4. 重新启动服务：
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

## 验证修复

修复后，新的网络配置应该是：
- Docker默认网段：192.168.0.0/16
- 项目网络：192.168.100.0/24

检查路由表确认冲突已解决：
```bash
route -n
```

## 注意事项

1. 修复过程中会短暂中断服务
2. 需要重启Docker服务以应用新配置
3. 确保没有其他服务依赖172.18网段
4. 如果仍有问题，可能需要检查防火墙设置

## 故障排除

如果修复后仍有问题：

1. 检查Docker服务状态：
   ```bash
   docker info
   ```

2. 查看网络列表：
   ```bash
   docker network ls
   ```

3. 检查容器网络：
   ```bash
   docker network inspect test_tracking_test_tracking_network
   ```

4. 查看Docker日志：
   ```bash
   docker logs test-tracking-app
   docker logs test-tracking-mysql
   ```
