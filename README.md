# 测试任务跟踪系统 - 部署指南

## 📋 概述

本系统提供完整的测试任务跟踪功能，包括后端API、前端UI、数据库和缓存服务。

## 🚀 快速开始

### 1. 首次部署（完整设置）

```bash
# 给脚本执行权限
chmod +x *.sh

# 按顺序部署所有服务
./deploy-infrastructure.sh
./deploy-backend.sh
./deploy-frontend.sh
./deploy-nginx.sh
```

### 2. 服务管理器（交互式管理）

```bash
./service-manager.sh
```

## 📁 部署脚本

### 🔧 脚本功能

| 脚本名称 | 用途 | 依赖关系 | 构建时间 | 数据安全 |
|---------|------|----------|----------|----------|
| `deploy-infrastructure.sh` | 部署MySQL + Redis | 无 | 短 | 安全 |
| `deploy-backend.sh` | 部署Spring Boot应用 | 基础设施 | 中 | 安全 |
| `deploy-frontend.sh` | 部署Vue.js + Nginx | 无 | 中 | 安全 |
| `deploy-nginx.sh` | 部署Nginx代理 | 无 | 短 | 安全 |
| `service-manager.sh` | 交互式管理 | 所有脚本 | 灵活 | 安全 |

### 🎯 推荐使用策略

1. **首次部署**: 按顺序运行脚本
2. **代码更新**: 运行特定服务脚本
3. **配置修改**: 使用服务管理器
4. **日常运维**: 使用服务管理器

## 🌐 访问地址

- **前端界面**: http://localhost:3000
- **后端API**: http://localhost:8080
- **Nginx代理**: http://localhost
- **MySQL**: localhost:3306
- **Redis**: localhost:6379

## 📊 服务架构

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Nginx     │    │   Frontend  │    │     App     │
│   (80)      │    │   (3000)    │    │   (8080)    │
└─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
         ┌─────────┐  ┌─────────┐  ┌─────────┐
         │  MySQL  │  │  Redis  │  │  Volumes│
         │ (3306)  │  │ (6379)  │  │         │
         └─────────┘  └─────────┘  └─────────┘
```

## 🔧 服务管理

### 查看服务状态
```bash
docker-compose -f docker-compose.prod.yml ps
```

### 查看服务日志
```bash
# 查看所有服务日志
docker-compose -f docker-compose.prod.yml logs -f

# 查看特定服务日志
docker logs test_tracking_app -f
docker logs test_tracking_frontend -f
docker logs test_tracking_mysql -f
docker logs test_tracking_redis -f
docker logs test_tracking_nginx -f
```

### 重启特定服务
```bash
# 重启后端服务
docker-compose -f docker-compose.prod.yml restart app

# 重启前端服务
docker-compose -f docker-compose.prod.yml restart frontend

# 重启基础设施服务（谨慎使用）
docker-compose -f docker-compose.prod.yml restart mysql redis
```

### 停止所有服务
```bash
docker-compose -f docker-compose.prod.yml down
```

## 🛠️ 故障排除

### 1. 后端服务启动失败

```bash
# 查看后端日志
docker logs test_tracking_app --tail 50

# 检查数据库连接
docker exec test_tracking_mysql mysql -u testuser -pTestUser@2024 -e "SELECT 1;"

# 检查Redis连接
docker exec test_tracking_redis redis-cli ping
```

### 2. 前端服务无法访问

```bash
# 检查前端容器状态
docker ps | grep frontend

# 查看前端日志
docker logs test_tracking_frontend --tail 20

# 检查端口占用
netstat -tlnp | grep 3000
```

### 3. 数据库连接问题

```bash
# 检查MySQL容器状态
docker ps | grep mysql

# 查看MySQL日志
docker logs test_tracking_mysql --tail 20

# 测试数据库连接
docker exec test_tracking_mysql mysql -u testuser -pTestUser@2024 -e "SHOW DATABASES;"
```

### 4. 端口冲突

```bash
# 检查端口占用
netstat -tlnp | grep -E ':(80|3000|8080|3306|6379)'

# 停止占用端口的进程
sudo lsof -ti:8080 | xargs kill -9
```

## 📝 默认配置

### 数据库配置
- **主机**: localhost:3306
- **数据库**: test_tracking
- **用户名**: testuser
- **密码**: TestUser@2024
- **Root密码**: TestTracking@2024

### 应用配置
- **JWT密钥**: TestTrackingSecretKey2024ProductionEnvironment
- **JWT过期时间**: 24小时
- **默认用户**: admin / admin123

### 钉钉配置
- **启用状态**: 默认关闭
- **Webhook**: 需要配置
- **密钥**: 需要配置

## 🔒 安全建议

1. **修改默认密码**: 首次部署后立即修改默认密码
2. **配置HTTPS**: 生产环境建议配置SSL证书
3. **防火墙设置**: 只开放必要端口
4. **定期备份**: 定期备份数据库数据
5. **日志监控**: 监控应用日志，及时发现异常

## 📈 性能优化

1. **数据库优化**: 根据数据量调整MySQL配置
2. **缓存策略**: 合理使用Redis缓存
3. **资源限制**: 为容器设置合理的资源限制
4. **监控告警**: 设置系统监控和告警

## 🤝 技术支持

如遇到问题，请：

1. 查看相关服务日志
2. 检查系统资源使用情况
3. 验证网络连接
4. 参考故障排除章节
5. 联系技术支持团队

---

**注意**: 生产环境部署前，请务必修改默认密码和敏感配置！ 