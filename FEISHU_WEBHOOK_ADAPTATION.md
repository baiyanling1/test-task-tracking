# 飞书Webhook适配完成说明

## 已完成的工作

### 1. 后端适配

#### 新增服务类
- **FeiShuNotificationService.java** - 飞书通知服务，支持发送富文本卡片消息
  - 支持签名验证
  - 自动消息格式化
  - 支持告警类型过滤
  
- **FeiShuConfigService.java** - 飞书配置管理服务
  - 支持动态配置加载
  - 配置持久化到数据库

#### 修改的服务类
- **NotificationService.java** - 修改所有通知方法，同时支持钉钉和飞书
- **AlertNotificationConfigService.java** - 添加飞书通知开关检查
- **AlertNotificationConfig.java** - 实体类添加feishuEnabled字段

#### 新增控制器接口
- **NotificationController**
  - GET /api/notifications/feishu-config - 获取飞书配置
  - POST /api/notifications/feishu-config - 保存飞书配置
  - POST /api/notifications/test-feishu - 测试飞书通知
  
- **AlertNotificationConfigController**
  - PUT /api/admin/alert-notification-config/{alertType} - 更新告警配置（支持飞书字段）

### 2. 前端适配

#### 统一配置页面 ⭐
- **WebhookConfig.vue** - 统一的Webhook配置管理页面
  - 集成钉钉和飞书配置
  - 配置查看和编辑
  - 测试消息发送
  - 分页显示配置说明文档
  - 更简洁的用户界面

#### 修改页面
- **AlertNotificationConfig.vue** - 添加飞书通知开关列
- **MainLayout.vue** - 添加统一的"Webhook配置"菜单项（替代原来的钉钉和飞书单独菜单）

#### 新增API
- **notifications.js**
  - getFeiShuConfig() - 获取飞书配置
  - testFeiShuNotification() - 测试飞书通知
  - saveFeiShuConfig() - 保存飞书配置

#### 路由配置
- 新增统一路由: /webhook-config（替代原来的 /dingtalk-config 和 /feishu-config）

### 3. 数据库适配

#### 新增数据库脚本
- **12-add-feishu-support.sql**
  - 添加 alert_notification_config.feishu_enabled 字段
  - 添加系统配置: feishu.enabled, feishu.webhook, feishu.secret

### 4. 配置文件更新
- **application.yml** - 添加飞书配置项
- **application-prod.yml** - 添加飞书环境变量配置
- **docker-compose.prod.yml** - 添加飞书环境变量

## 功能特点

### 1. 消息格式
- **钉钉**: Markdown格式
- **飞书**: 富文本卡片格式（Interactive Card）
  - 支持标题、内容、字段、分隔线
  - 根据优先级设置卡片颜色
  - 更美观的显示效果

### 2. 签名验证
- **钉钉**: 使用timestamp + sign方式
- **飞书**: 使用timestamp + sign方式（HmacSHA256）

### 3. 配置管理
- 支持独立的钉钉和飞书配置
- 可同时启用两个平台
- 支持动态配置，无需重启服务
- 配置持久化到数据库

### 4. 告警类型控制
- 每种告警类型可独立配置是否发送到钉钉/飞书
- 支持以下告警类型：
  - 任务跟踪表填写提醒
  - 任务分配通知
  - 任务超时提醒
  - 任务完成通知
  - 系统维护通知

## 使用说明

### 1. 配置飞书机器人

1. 在飞书群中添加自定义机器人
2. 获取webhook地址（格式: https://open.feishu.cn/open-apis/bot/v2/hook/xxxxxxxx）
3. 如需使用签名验证，获取签名密钥
4. 在系统中配置飞书webhook地址和密钥

### 2. 配置告警通知

1. 进入"告警通知配置"页面
2. 为每种告警类型选择是否启用飞书通知
3. 保存配置

### 3. 测试配置

1. 进入"飞书配置"页面
2. 点击"发送测试消息"按钮
3. 检查飞书群是否收到测试消息

## 注意事项

1. 飞书webhook地址必须是 `https://open.feishu.cn/open-apis/bot/v2/hook/` 开头
2. 签名密钥是可选的，如果配置了签名，系统会自动添加签名验证
3. 钉钉和飞书可以同时启用，系统会向两个平台都发送消息
4. 配置更改后立即生效，无需重启服务
5. 建议使用签名验证以提高安全性

## 部署说明

### 开发环境
配置文件: `application.yml`
```yaml
feishu:
  enabled: false
  webhook:
    url: 
  secret: 
```

### 生产环境
配置文件: `application-prod.yml`
环境变量:
- FEISHU_ENABLED
- FEISHU_WEBHOOK_URL
- FEISHU_SECRET

Docker环境配置:
```yaml
environment:
  FEISHU_ENABLED: false
  FEISHU_WEBHOOK_URL: https://open.feishu.cn/open-apis/bot/v2/hook/your-webhook-id
  FEISHU_SECRET: your_secret
```

## 测试建议

1. 测试飞书配置和测试消息发送
2. 测试告警通知配置的飞书开关
3. 测试实际任务通知（任务分配、完成等）
4. 测试钉钉和飞书同时启用的情况
5. 测试签名验证功能

## 技术细节

### 飞书消息格式
使用Interactive Card（卡片消息）格式，结构如下：
```json
{
  "msg_type": "interactive",
  "card": {
    "header": {
      "title": {"tag": "plain_text", "content": "标题"},
      "template": "颜色"
    },
    "elements": [
      {
        "tag": "div",
        "fields": [{"tag": "lark_md", "content": "内容"}]
      }
    ]
  }
}
```

### 签名算法
```java
timestamp = System.currentTimeMillis() / 1000
stringToSign = timestamp + "\n" + secret
sign = Base64.encode(HmacSHA256(stringToSign))
```

## 下一步优化建议

1. 添加消息发送失败重试机制
2. 添加消息发送历史记录
3. 支持更多的消息格式（文本、图片等）
4. 添加消息模板配置
5. 支持@特定用户功能
6. 添加消息发送统计和监控

## 版本信息

- 适配日期: 2025-11-03
- 支持的飞书API版本: v2
- 支持的消息类型: Interactive Card (富文本卡片)

