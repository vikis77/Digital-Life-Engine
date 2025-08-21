# 贡献指南

感谢你对 Digital Life Engine 项目的关注！我们欢迎所有形式的贡献。

## 🤝 如何贡献

### 报告问题

如果你发现了bug或有功能建议：

1. 检查 [Issues](https://github.com/your-username/digital-life-engine/issues) 确保问题未被报告
2. 创建新的Issue，包含：
   - 清晰的标题和描述
   - 重现步骤（如果是bug）
   - 期望的行为
   - 实际的行为
   - 环境信息（Java版本、操作系统等）

### 提交代码

1. **Fork项目**
   ```bash
   git clone https://github.com/your-username/digital-life-engine.git
   cd digital-life-engine
   ```

2. **创建分支**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **开发和测试**
   - 遵循项目的编码规范
   - 添加必要的测试
   - 确保所有测试通过

4. **提交更改**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

5. **推送分支**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **创建Pull Request**

## 📝 编码规范

### Java代码规范

- 使用4个空格缩进
- 类名使用PascalCase
- 方法名和变量名使用camelCase
- 常量使用UPPER_SNAKE_CASE
- 添加适当的JavaDoc注释

### 通用化原则

这是项目的核心原则，请务必遵循：

- **零硬编码**：不要硬编码任何特定的任务类型、字段名或业务逻辑
- **配置驱动**：通过配置文件定义行为，而不是代码
- **职责分离**：每个类和方法都应该有单一、明确的职责
- **可扩展性**：设计时考虑未来的扩展需求

### 示例：好的通用化代码

```java
// ✅ 好的通用化代码
public boolean isEmptyDataResponse(String response) {
    // 通用的空数据检测，适用于任何API响应
    JSONObject responseObj = JSONObject.parseObject(response);
    Object dataObj = responseObj.get("data");
    return dataObj == null;
}

// ❌ 避免的特例化代码
public boolean isPostDataEmpty(String response) {
    // 特例化的帖子数据检测，只适用于帖子API
    JSONObject responseObj = JSONObject.parseObject(response);
    JSONObject data = responseObj.getJSONObject("data");
    return data == null || data.getString("postId") == null;
}
```

## 🧪 测试

- 为新功能添加单元测试
- 确保现有测试通过
- 测试覆盖率应保持在合理水平

运行测试：
```bash
mvn test
```

## 📚 文档

- 更新相关的README文档
- 为新功能添加使用示例
- 更新API文档（如果适用）

## 🔄 Pull Request流程

1. **描述清晰**：PR标题和描述要清楚说明更改内容
2. **关联Issue**：如果解决了某个Issue，请在PR中引用
3. **代码审查**：耐心等待代码审查，积极回应反馈
4. **持续集成**：确保CI检查通过

### PR模板

```markdown
## 更改类型
- [ ] Bug修复
- [ ] 新功能
- [ ] 文档更新
- [ ] 性能优化
- [ ] 其他

## 更改描述
简要描述你的更改...

## 测试
- [ ] 添加了新的测试
- [ ] 现有测试通过
- [ ] 手动测试通过

## 检查清单
- [ ] 代码遵循项目规范
- [ ] 遵循通用化原则
- [ ] 更新了相关文档
- [ ] 没有硬编码的特例化逻辑
```

## 🎯 开发重点

### 当前需要帮助的领域

1. **任务类型扩展**：添加新的任务类型支持
2. **错误处理**：改进错误检测和恢复机制
3. **性能优化**：提升LLM调用效率
4. **文档完善**：添加更多使用示例和教程
5. **测试覆盖**：增加测试用例

### 架构改进方向

1. **插件系统**：支持动态加载新的能力模块
2. **监控面板**：Web界面显示执行状态
3. **多模型支持**：支持更多LLM提供商
4. **分布式执行**：支持多实例协同工作

## 💬 交流

- 加入我们的讨论：[GitHub Discussions](https://github.com/your-username/digital-life-engine/discussions)
- 提问和建议：[Issues](https://github.com/your-username/digital-life-engine/issues)

## 🙏 致谢

感谢所有贡献者的努力！你们的贡献让这个项目变得更好。

---

再次感谢你的贡献！🎉
