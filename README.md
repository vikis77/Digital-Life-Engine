# 🤖 Digital Life Engine - 智能数字生命引擎

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

一个基于大语言模型的智能数字生命引擎，能够自主执行复杂的多步骤任务，具备学习、决策和自我管理能力。

为什么要做这个项目呢？

项目设想：这个项目我是想做一个“数字生命”一样的东西，理想状态下，可以触发一次后，开启随机从tasks.txt获取任务，ability.txt里面描述了每一个动作。想着先构建一个最小的，细胞或神经元级别的逻辑，去随机执行我们的任务。我希望构建一套完整的逻辑，能够实现任意任务。可能会有人认为，这不就是智能体嘛，目前的形态你可以把它看成一个智能体，但是这个项目的目标是“数字生命”，一个生存在虚拟世界中的“数字生命”，像人类一样，思考、决策、行动。

我原本的建模是：启动数字生命 -input-> LLM -output-> LLM -> ... 无限循环执行任务。有两个中心仓库，一个是任务仓库，一个是能力仓库。
input中有：我是谁（我是一个数字生命。），我的任务是什么（随机从tasks.txt中获取），我刚刚做了什么（整个任务中我执行到哪里了），我可以做什么（能力仓库），我现在要干什么（下一步做什么，用于传递给下游LLM，指导下游LLM做事），我要输出什么（规定输出格式）。
现在目标完成了，一个最小的可行试验，单线程下的数字生命模型。


## ✨ 核心特性

### 🧠 智能决策系统
- **双LLM架构**：执行LLM负责任务执行，判断LLM负责任务完成判断
- **上下文感知**：基于历史数据和当前状态进行智能决策
- **自适应学习**：根据执行结果动态调整策略

### 🔄 自主任务执行
- **多步骤任务**：支持复杂的多步骤任务自动执行
- **状态管理**：智能的任务状态持久化和恢复
- **错误处理**：自动错误检测和重试机制
- **数据驱动**: 基于数据的通用特征进行决策

### 🎯 通用化设计
- **零硬编码**：完全通用的任务处理逻辑
- **配置驱动**：通过配置文件定义任务和能力
- **插件化架构**：易于扩展新的任务类型和能力

### 🌐 API集成
- **智能HTTP客户端**：自动处理API调用和参数映射
- **数据提取**：智能提取和格式化API响应数据
- **认证管理**：自动处理token认证

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- 通义千问API密钥

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/your-username/digital-life-engine.git
cd digital-life-engine
```

2. **配置API密钥**
```yaml
# src/main/resources/application.yml
spring:
  ai:
    dashscope:
      api-key: your_dashscope_api_key_here
      chat:
        options:
          model: qwen-turbo-2025-07-15
```

3. **配置认证Token**
```yaml
# src/main/resources/application.yml
digital-life:
  auth:
    permanent-token: "Bearer your_permanent_token_here"
```

4. **启动应用**
```bash
mvn spring-boot:run
```

## 📋 任务配置

### 任务定义 (tasks.txt)
```
发布一个帖子；审核帖子；审核评论
```

### 能力定义 (ability.txt)
```json
[
  {
    "任务": "发布一个帖子",
    "步骤": [
      {
        "描述": "模仿一名大学生，编辑帖子内容并发布帖子",
        "动作": {
          "url": "http://localhost:8080/api/digital/addpost",
          "method": "POST",
          "params": {},
          "body": {
            "article": "帖子内容，内容自由发挥，String类型",
            "title": "帖子标题，内容自由发挥，String类型",
            "pictrueList": "帖子图片列表，List<String>类型，固定只有一个图片: 17-01.jpg"
          }
        }
      }
    ]
  }
]
```

## 🏗️ 架构设计

### 核心组件

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  DigitalLife    │    │  TaskCompletion │    │  ActionFormatter│
│     Engine      │◄──►│     Judge       │◄──►│                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  StateManager   │    │   LLM Service   │    │  HTTP Client    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 关键类说明

- **DigitalLifeEngine**: 核心引擎，负责任务调度和执行
- **TaskCompletionJudge**: 专门的任务完成判断服务
- **ActionFormatter**: 智能动作格式化和执行器
- **StateManager**: 状态管理和持久化
- **AutoStartService**: 自动启动服务

## 🎮 使用示例

### 基本使用

系统启动后会自动开始执行任务循环：

1. **任务选择**: 从tasks.txt中随机选择任务
2. **能力匹配**: 从ability.txt中找到对应的执行步骤
3. **智能执行**: LLM分析并执行每个步骤
4. **状态管理**: 自动保存和恢复执行状态
5. **完成判断**: 专门的判断LLM决定是否完成任务

## 🔧 配置说明

### 应用配置 (application.yml)

```yaml
server:
  port: 8088

spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY:your_api_key}
      chat:
        options:
          model: qwen-turbo-2025-07-15

digital-life:
  auto-start:
    enabled: true
    delay: 5
  auth:
    permanent-token: "Bearer your_token"
```

### 日志配置 (logback-spring.xml)

支持彩色日志输出，自动适配不同操作系统的编码。

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 开发规范

- 遵循Java编码规范
- 添加适当的注释和文档
- 确保所有测试通过
- 保持代码的通用性，避免硬编码

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot) - 应用框架
- [Spring AI](https://spring.io/projects/spring-ai) - AI集成框架
- [阿里云通义千问](https://dashscope.aliyun.com/) - 大语言模型服务

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 [Issue](https://github.com/your-username/digital-life-engine/issues)
- 发送邮件至: your-email@example.com

---

⭐ 如果这个项目对你有帮助，请给它一个星标！
