# 测试任务跟踪系统 - 前端

## 快速开始

### 1. 环境要求
- Node.js (版本 16 或更高)
- npm 或 yarn

### 2. 一键启动（推荐）

双击运行 `quick-start.bat` 文件，脚本会自动：
- 检查Node.js环境
- 安装依赖
- 创建环境配置文件
- 启动开发服务器

### 3. 配置后端服务地址

编辑 `.env.development` 文件，设置您的后端服务地址：

```env
VITE_API_BASE_URL=http://your-backend-server:8080
```

### 4. 测试连接

运行 `test-backend.bat` 测试后端连接是否正常。

### 5. 访问应用

启动成功后访问：`http://localhost:3000`

## 详细说明

请查看 `本地开发配置说明.md` 文件获取详细的配置和故障排除信息。

## 项目结构

```
frontend/
├── src/                    # 源代码
│   ├── api/               # API接口
│   ├── components/        # 组件
│   ├── layouts/           # 布局
│   ├── router/            # 路由
│   ├── stores/            # 状态管理
│   ├── views/             # 页面
│   └── main.js            # 入口文件
├── public/                # 静态资源
├── vite.config.js         # Vite配置
├── package.json           # 依赖配置
├── quick-start.bat        # 快速启动脚本
├── test-backend.bat       # 连接测试脚本
└── 本地开发配置说明.md     # 详细配置说明
```

## 开发命令

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

## 技术栈

- Vue 3 (Composition API)
- Element Plus (UI组件库)
- ECharts (图表库)
- Axios (HTTP客户端)
- Vite (构建工具)
