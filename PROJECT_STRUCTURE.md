# 📁 项目结构说明

```
digital-life-engine/
├── 📄 README.md                           # 项目介绍和快速开始
├── 📄 LICENSE                             # MIT许可证
├── 📄 CONTRIBUTING.md                     # 贡献指南
├── 📄 CHANGELOG.md                        # 更新日志
├── 📄 PROJECT_STRUCTURE.md               # 项目结构说明（本文件）
├── 📄 .gitignore                         # Git忽略文件配置
├── 📄 pom.xml                            # Maven项目配置
│
├── 📁 docs/                              # 文档目录
│   ├── 📄 ARCHITECTURE.md                # 架构设计文档
│   └── 📄 QUICK_START.md                 # 快速开始指南
│
├── 📁 scripts/                           # 脚本目录
│   ├── 📄 prepare-release.sh             # Linux/Mac发布准备脚本
│   └── 📄 prepare-release.bat            # Windows发布准备脚本
│
├── 📁 src/                               # 源代码目录
│   ├── 📁 main/
│   │   ├── 📁 java/
│   │   │   └── 📁 com/randb/digitaldemo1/
│   │   │       ├── 📄 Digitaldemo1Application.java    # Spring Boot启动类
│   │   │       │
│   │   │       ├── 📁 config/                         # 配置类目录
│   │   │       │   ├── 📄 AuthConfig.java             # 认证配置
│   │   │       │   └── 📄 SpringAIChatStarterConfig.java # AI配置
│   │   │       │
│   │   │       ├── 📁 core/                           # 核心引擎目录
│   │   │       │   └── 📄 DigitalLifeEngine.java      # 数字生命核心引擎
│   │   │       │
│   │   │       ├── 📁 entity/                         # 实体类目录
│   │   │       │   └── 📄 Prompt.java                 # 提示词实体
│   │   │       │
│   │   │       └── 📁 service/                        # 服务层目录
│   │   │           ├── 📄 ActionFormatter.java        # 动作格式化服务
│   │   │           ├── 📄 AutoStartService.java       # 自动启动服务
│   │   │           ├── 📄 StateManager.java           # 状态管理服务
│   │   │           └── 📄 TaskCompletionJudge.java    # 任务完成判断服务
│   │   │
│   │   └── 📁 resources/                              # 资源文件目录
│   │       ├── 📄 application.yml                     # 应用配置文件
│   │       ├── 📄 application.yml.template            # 配置模板文件
│   │       ├── 📄 ability.txt                         # 能力配置文件
│   │       ├── 📄 tasks.txt                          # 任务配置文件
│   │       ├── 📄 logback-spring.xml                 # 日志配置文件
│   │       └── 📁 static/                            # 静态资源目录
│   │           └── 📄 index.html                     # 首页文件
│   │
│   └── 📁 test/                                      # 测试代码目录
│       └── 📁 java/
│           └── 📁 com/randb/digitaldemo1/
│               └── 📄 Digitaldemo1ApplicationTests.java # 测试类
│
└── 📁 target/                                        # Maven编译输出目录（.gitignore中）
```

## 📋 目录说明

### 🏗️ 核心架构

#### `/src/main/java/com/randb/digitaldemo1/core/`
- **DigitalLifeEngine.java**: 系统的核心引擎，负责：
  - 任务生命周期管理
  - LLM交互和响应处理
  - 系统状态协调

#### `/src/main/java/com/randb/digitaldemo1/service/`
- **ActionFormatter.java**: 动作格式化和执行服务
  - 智能解析LLM输出的动作指令
  - 执行HTTP请求
  - 处理API响应数据

- **TaskCompletionJudge.java**: 任务完成判断服务
  - 专门判断任务是否应该完成
  - 避免执行LLM的思维惯性
  - 防止无限循环

- **StateManager.java**: 状态管理服务
  - 任务状态持久化
  - 状态恢复和清理
  - 跨会话状态保持

- **AutoStartService.java**: 自动启动服务
  - 应用启动后自动开始任务执行
  - 可配置的启动延迟

### ⚙️ 配置管理

#### `/src/main/java/com/randb/digitaldemo1/config/`
- **AuthConfig.java**: 认证配置管理
- **SpringAIChatStarterConfig.java**: AI模型配置

#### `/src/main/resources/`
- **application.yml**: 主配置文件
- **application.yml.template**: 配置模板（用于开源发布）
- **ability.txt**: 能力和API配置
- **tasks.txt**: 任务定义配置
- **logback-spring.xml**: 日志配置

### 📚 文档系统

#### `/docs/`
- **ARCHITECTURE.md**: 详细的系统架构设计文档

#### 根目录文档
- **README.md**: 项目主要介绍和使用说明
- **CONTRIBUTING.md**: 贡献指南和开发规范
- **CHANGELOG.md**: 版本更新日志
- **LICENSE**: MIT开源许可证

## 🎯 关键文件说明

### 配置文件

| 文件 | 用途 | 重要性 |
|------|------|--------|
| `application.yml` | 主配置文件，包含API密钥等 | ⭐⭐⭐ |
| `ability.txt` | 定义系统能力和API接口 | ⭐⭐⭐ |
| `tasks.txt` | 定义可执行的任务列表 | ⭐⭐⭐ |
| `logback-spring.xml` | 日志输出格式和级别 | ⭐⭐ |

### 核心代码

| 文件 | 职责 | 重要性 |
|------|------|--------|
| `DigitalLifeEngine.java` | 核心引擎，任务调度 | ⭐⭐⭐ |
| `TaskCompletionJudge.java` | 任务完成判断 | ⭐⭐⭐ |
| `ActionFormatter.java` | 动作执行和HTTP处理 | ⭐⭐⭐ |
| `StateManager.java` | 状态管理 | ⭐⭐ |
| `AutoStartService.java` | 自动启动 | ⭐⭐ |

