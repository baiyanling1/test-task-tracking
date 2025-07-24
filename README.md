# 测试任务跟踪系统 (Test Task Tracking System)

一个基于Spring Boot和Vue.js的完整任务管理系统，支持任务创建、编辑、删除、筛选，以及用户和部门管理。

## 🚀 功能特性

### 核心功能
- ✅ **用户认证**: JWT token认证，支持登录/登出
- ✅ **任务管理**: 创建、编辑、删除、筛选任务
- ✅ **用户管理**: 用户CRUD操作，支持角色权限
- ✅ **部门管理**: 部门CRUD操作
- ✅ **仪表板**: 任务统计和可视化
- ✅ **通知系统**: 未读通知提醒
- ✅ **响应式设计**: 支持移动端和桌面端

### 技术特性
- 🔐 **安全认证**: Spring Security + JWT
- 📊 **数据可视化**: ECharts图表
- 🎨 **现代化UI**: Element Plus组件库
- 📱 **响应式布局**: 适配各种屏幕尺寸
- 🐳 **容器化部署**: Docker + Docker Compose
- 🔄 **实时更新**: 定时刷新数据

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 2.7.x
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA
- **构建工具**: Maven

### 前端
- **框架**: Vue 3
- **UI库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **构建工具**: Vite
- **图表**: ECharts

### 部署
- **容器化**: Docker
- **编排**: Docker Compose
- **Web服务器**: Nginx
- **数据库**: MySQL

## 📦 快速开始

### 环境要求
- Java 11+
- Node.js 16+
- Docker & Docker Compose
- MySQL 8.0

### 1. 克隆项目
```bash
git clone https://github.com/baiyanling1/test-task-tracking.git
cd test-task-tracking
```

### 2. 使用Docker快速启动
```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 访问应用
- **前端**: http://localhost:3000
- **后端API**: http://localhost:8080
- **数据库**: localhost:3306

### 4. 默认账户
- **管理员**: admin / admin123
- **项目经理**: manager / manager123
- **普通用户**: user / user123

## 🔧 开发环境搭建

### 后端开发
```bash
# 进入后端目录
cd src

# 安装依赖
mvn install

# 启动开发服务器
mvn spring-boot:run
```

### 前端开发
```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 📁 项目结构

```
test-task-tracking/
├── frontend/                 # Vue.js前端
│   ├── src/
│   │   ├── api/             # API接口
│   │   ├── components/      # 组件
│   │   ├── layouts/         # 布局组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # 状态管理
│   │   └── views/           # 页面组件
│   ├── package.json
│   └── vite.config.js
├── src/                      # Spring Boot后端
│   └── main/java/com/testtracking/
│       ├── config/          # 配置类
│       ├── controller/      # 控制器
│       ├── dto/            # 数据传输对象
│       ├── entity/         # 实体类
│       ├── repository/     # 数据访问层
│       ├── security/       # 安全配置
│       └── service/        # 业务逻辑层
├── mysql/                   # 数据库配置
├── nginx/                   # Nginx配置
├── docker-compose.yml       # Docker编排
└── README.md
```

## 🔐 权限系统

### 用户角色
- **ADMIN**: 系统管理员，拥有所有权限
- **MANAGER**: 项目经理，可以管理用户和部门
- **USER**: 普通用户，只能管理自己的任务

### 功能权限
| 功能 | ADMIN | MANAGER | USER |
|------|-------|---------|------|
| 任务管理 | ✅ | ✅ | ✅ |
| 用户管理 | ✅ | ✅ | ❌ |
| 部门管理 | ✅ | ✅ | ❌ |
| 系统设置 | ✅ | ❌ | ❌ |

## 🚀 部署指南

### 生产环境部署
```bash
# 构建生产镜像
docker-compose -f docker-compose.prod.yml build

# 启动生产环境
docker-compose -f docker-compose.prod.yml up -d
```

### 环境变量配置
```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=test_tracking
DB_USER=root
DB_PASSWORD=password

# JWT配置
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

## 📊 API文档

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出

### 任务接口
- `GET /api/tasks` - 获取任务列表
- `POST /api/tasks` - 创建任务
- `PUT /api/tasks/{id}` - 更新任务
- `DELETE /api/tasks/{id}` - 删除任务

### 用户接口
- `GET /api/users` - 获取用户列表
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户

## 🐛 故障排除

### 常见问题

1. **数据库连接失败**
   ```bash
   # 检查MySQL服务
   docker-compose logs mysql
   ```

2. **前端无法访问后端**
   ```bash
   # 检查后端服务
   docker-compose logs backend
   ```

3. **权限错误**
   - 确认JWT token有效
   - 检查用户角色权限

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- **项目维护者**: [您的姓名]
- **邮箱**: [您的邮箱]
- **项目链接**: https://github.com/baiyanling1/test-task-tracking

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和用户！

---

⭐ 如果这个项目对您有帮助，请给我们一个星标！ 