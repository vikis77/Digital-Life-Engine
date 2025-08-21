# 动作执行器 (ActionExecutor) 使用说明

## 概述

新的动作执行器能够解析LLM生成的复杂动作指令JSON，提取HTTP请求信息并发送请求。

## 功能特性

1. **解析复杂JSON结构** - 支持包含多个步骤的动作指令
2. **HTTP请求提取** - 自动提取method、url、params、body等信息
3. **自动发送请求** - 使用RestTemplate发送HTTP请求
4. **详细日志记录** - 记录执行过程和结果
5. **错误处理** - 完善的异常处理机制

## JSON格式示例

### 基本格式
```json
{
  "动作类型": {
    "步骤": [
      {
        "描述": "步骤描述",
        "动作": {
          "method": "POST",
          "url": "https://localhost:8080/api/endpoint",
          "params": {
            "key": "value"
          },
          "body": {
            "field1": "value1",
            "field2": "value2"
          }
        }
      }
    ]
  }
}
```

### 完整示例
```json
{
  "发布一个帖子": {
    "步骤": [
      {
        "描述": "打开"校猫日记"APP",
        "动作": ""
      },
      {
        "描述": "编辑帖子内容并发布帖子",
        "动作": {
          "method": "POST",
          "params": {},
          "body": {
            "tag": "校园生活",
            "title": "校园里的小猫咪",
            "content": "今天在校园里看到一只可爱的猫咪，它在阳光下打滚的样子真是太萌了！"
          },
          "url": "https://localhost:8080/sent/post"
        }
      },
      {
        "是否最后步骤": "是",
        "描述": "关闭APP",
        "动作": ""
      }
    ]
  }
}
```

## 使用方法

### 1. 在代码中直接调用
```java
String actionJson = "..."; // 你的JSON字符串
ActionExecutor.executeComplexAction(actionJson);
```

### 2. 通过REST API测试
启动应用后，可以使用以下端点进行测试：

- `POST /api/action-test/test-complex` - 测试基本功能
- `POST /api/action-test/test-multiple` - 测试多动作功能
- `POST /api/action-test/test-all` - 运行所有测试
- `POST /api/action-test/execute` - 直接执行动作指令JSON
- `GET /api/action-test/health` - 健康检查

### 3. 在LLM1中的集成
新的动作执行器已经集成到`llm1.java`中，会自动解析LLM生成的动作指令。

## 核心类说明

### ActionExecutor
- `executeComplexAction(String)` - 解析并执行复杂动作指令
- `executeAction(String)` - 兼容原有的简单动作格式

### HttpRequestInfo
- 封装HTTP请求信息的数据类
- 包含method、url、params、body等字段
- 提供有效性检查和格式化输出

### ActionExecutorTest
- 提供各种测试用例
- 验证动作执行器的功能

### ActionTestController
- REST API控制器
- 提供测试接口

## 日志输出

执行过程中会输出详细的日志信息：
- 动作类型和步骤描述
- HTTP请求详情（method、url、params、body）
- 响应状态和内容
- 错误信息（如果有）

## 注意事项

1. **URL有效性** - 确保提供的URL是可访问的
2. **JSON格式** - 严格按照指定格式提供JSON
3. **网络连接** - 确保网络连接正常
4. **权限认证** - 如果目标API需要认证，需要在代码中添加相应的认证逻辑

## 扩展功能

可以根据需要扩展以下功能：
- 添加请求头设置
- 支持文件上传
- 添加重试机制
- 支持异步请求
- 添加请求缓存
