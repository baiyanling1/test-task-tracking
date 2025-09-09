# 未活跃用户API 404错误排除指南

## 问题现象
前端调用 `/api/dashboard/inactive-users` 接口时返回404错误：
```
AxiosError: Request failed with status code 404
```

## 可能的原因和解决方案

### 1. 后端服务器未启动 ⭐ 最常见
**检查方法:**
```bash
# Windows
netstat -an | findstr :8080

# Linux/Mac
netstat -an | grep :8080
```

**解决方案:**
```bash
# 进入项目根目录，启动后端服务
cd /path/to/Test_Task_Tracking
mvn spring-boot:run

# 或者使用IDE直接运行 TestTaskTrackingApplication.java
```

### 2. 前端代理配置错误
**检查方法:**
查看 `frontend/vite.config.js` 中的代理配置

**当前配置问题:**
```javascript
proxy: {
  '/api': {
    target: 'http://10.18.40.45:8080',  // 这个IP可能不正确
    changeOrigin: true,
    secure: false
  }
}
```

**解决方案:**
运行修复脚本：
```bash
node fix-vite-config.js
```

或者手动修改为：
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',  // 改为localhost
    changeOrigin: true,
    secure: false
  }
}
```

### 3. Controller路径映射问题
**检查方法:**
确认 `DashboardController.java` 中的注解正确：

```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    @GetMapping("/inactive-users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getLastWeekInactiveUsers() {
        // ...
    }
}
```

**完整路径应该是:** `http://localhost:8080/api/dashboard/inactive-users`

### 4. 权限问题
**检查方法:**
确认当前登录用户具有 `ADMIN` 或 `MANAGER` 权限

**解决方案:**
- 使用具有管理员权限的账户登录
- 检查JWT token是否有效
- 确认用户角色设置正确

### 5. 网络/防火墙问题
**检查方法:**
```bash
# 测试后端服务器连接
curl http://localhost:8080/api/dashboard
# 或
ping localhost
```

## 快速诊断工具

### 1. 运行API诊断脚本
```bash
node diagnose-api-issue.js
```
这个脚本会：
- 测试多个可能的后端地址
- 检查各个API端点的可达性
- 提供具体的修复建议

### 2. 运行配置修复脚本
```bash
node fix-vite-config.js
```
这个脚本会：
- 修复vite代理配置
- 创建正确的环境变量文件
- 备份原始配置

### 3. 手动测试API
```bash
# 直接测试后端API (需要有效的JWT token)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/dashboard/inactive-users
```

## 分步解决流程

### 第一步：确认后端服务状态
1. 检查后端Spring Boot应用是否启动
2. 查看控制台日志确认无启动错误
3. 确认监听端口8080

### 第二步：修复前端配置
1. 运行 `node fix-vite-config.js`
2. 重启前端开发服务器
3. 清除浏览器缓存

### 第三步：验证API路径
1. 检查Controller注解
2. 确认方法签名正确
3. 验证权限配置

### 第四步：测试连接
1. 使用诊断脚本测试
2. 查看网络请求详情
3. 检查浏览器开发者工具

## 成功标志

当问题解决后，您应该看到：

1. **后端日志显示API调用:**
```
INFO  - 检查 2024-01-15 到 2024-01-21 期间的用户活动，共 10 个活跃用户
INFO  - 检查完成，共 3 个用户无活动记录
```

2. **前端成功获取数据:**
```javascript
// 控制台输出
上周未活跃用户统计: {
  totalUsers: 10,
  inactiveCount: 3,
  inactiveUsers: [...],
  startDate: "2024-01-15",
  endDate: "2024-01-21"
}
```

3. **仪表盘显示统计卡片:**
- 总用户数、未活跃用户数、活跃率等数据正常显示
- 时间范围选择器工作正常
- 用户列表显示完整信息

## 联系支持

如果以上步骤都无法解决问题，请提供：
1. 后端启动日志
2. 前端控制台错误信息
3. 网络请求详细信息
4. 浏览器开发者工具截图
