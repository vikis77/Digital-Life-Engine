# 🚀 快速开始指南

本指南将帮助你在5分钟内运行起Digital Life Engine。

## 📋 前置要求

- ☑️ Java 17 或更高版本
- ☑️ Maven 3.6 或更高版本
- ☑️ 通义千问API密钥 ([获取地址](https://dashscope.aliyun.com/))

## 🛠️ 安装步骤

### 1. 克隆项目

```bash
git clone https://github.com/your-username/digital-life-engine.git
cd digital-life-engine
```

### 2. 配置API密钥

复制配置模板：
```bash
cp src/main/resources/application.yml.template src/main/resources/application.yml
```

编辑 `src/main/resources/application.yml`：
```yaml
spring:
  ai:
    dashscope:
      # 替换为你的API密钥
      api-key: your_dashscope_api_key_here
```

### 3. 配置认证Token（可选）

如果你有目标API的认证token，可以配置：
```yaml
digital-life:
  auth:
    permanent-token: "Bearer your_token_here"
```

### 4. 启动应用

```bash
mvn spring-boot:run
```

## 🎯 验证运行

### 查看启动日志

应用启动后，你会看到类似的日志：
```
🚀 数字生命自动启动已启用，将在 5 秒后启动
🤖 随机选择任务: 发布一个帖子
📊 上一步获得的数据：...
```

### 访问Web界面

打开浏览器访问：`http://localhost:8088`

## 📝 自定义配置

### 修改任务列表

编辑 `src/main/resources/tasks.txt`：
```
发布一个帖子；审核帖子；审核评论；查看个人资料
```

### 修改能力配置

编辑 `src/main/resources/ability.txt`，添加新的任务能力：
```json
[
  {
    "任务": "你的新任务",
    "步骤": [
      {
        "描述": "任务步骤描述",
        "动作": {
          "url": "http://your-api.com/endpoint",
          "method": "POST",
          "params": {},
          "body": {
            "param1": "参数说明",
            "param2": "参数说明"
          }
        }
      }
    ]
  }
]
```

## 🔧 常见问题

### Q: API密钥无效
**A**: 确保你的通义千问API密钥正确，并且账户有足够的额度。

### Q: 应用启动失败
**A**: 检查Java版本是否为17+，Maven依赖是否正确下载。

### Q: 任务执行失败
**A**: 检查目标API是否可访问，认证token是否正确。

### Q: 中文显示乱码
**A**: 确保控制台支持UTF-8编码，或使用Windows Terminal。

## 🎮 使用示例

### 示例1：基本任务执行

系统会自动：
1. 从任务列表中选择任务
2. 分析任务要求
3. 执行相应的API调用
4. 处理响应数据
5. 判断任务是否完成

### 示例2：添加自定义任务

1. **定义任务**：在 `tasks.txt` 中添加 `"发送邮件"`
2. **配置能力**：在 `ability.txt` 中添加邮件API配置
3. **重启应用**：系统会自动识别新任务

### 示例3：监控执行状态

查看实时日志：
```bash
tail -f logs/application.log
```

## 🔄 下一步

- 📖 阅读 [架构文档](ARCHITECTURE.md) 了解系统设计
- 🤝 查看 [贡献指南](../CONTRIBUTING.md) 参与开发
- 💬 加入 [讨论区](https://github.com/your-username/digital-life-engine/discussions) 交流

## 🆘 获取帮助

如果遇到问题：

1. 查看 [常见问题](FAQ.md)
2. 搜索 [Issues](https://github.com/your-username/digital-life-engine/issues)
3. 创建新的Issue描述问题
4. 加入社区讨论

---

🎉 恭喜！你已经成功运行了Digital Life Engine！
