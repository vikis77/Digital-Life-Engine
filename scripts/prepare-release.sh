#!/bin/bash

# Digital Life Engine - 发布准备脚本
# 用于清理项目并准备上传到开源仓库

echo "🚀 开始准备Digital Life Engine发布..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查是否在项目根目录
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}❌ 错误：请在项目根目录运行此脚本${NC}"
    exit 1
fi

echo -e "${BLUE}📋 检查项目结构...${NC}"

# 1. 清理编译产物
echo -e "${YELLOW}🧹 清理编译产物...${NC}"
mvn clean > /dev/null 2>&1
rm -rf target/
rm -rf logs/
rm -rf *.log
echo -e "${GREEN}✅ 编译产物清理完成${NC}"

# 2. 检查敏感信息
echo -e "${YELLOW}🔍 检查敏感信息...${NC}"

# 检查API密钥
if grep -r "sk-" src/ 2>/dev/null; then
    echo -e "${RED}⚠️  警告：发现可能的API密钥，请检查并移除${NC}"
fi

# 检查真实token
if grep -r "Bearer ey" src/ 2>/dev/null; then
    echo -e "${RED}⚠️  警告：发现可能的真实token，请检查并移除${NC}"
fi

# 检查IP地址
if grep -r "192\.168\." src/ 2>/dev/null; then
    echo -e "${YELLOW}⚠️  发现内网IP地址，请确认是否需要替换为示例地址${NC}"
fi

echo -e "${GREEN}✅ 敏感信息检查完成${NC}"

# 3. 验证配置文件
echo -e "${YELLOW}📝 验证配置文件...${NC}"

# 检查是否有配置模板
if [ ! -f "src/main/resources/application.yml.template" ]; then
    echo -e "${RED}❌ 缺少配置模板文件${NC}"
    exit 1
fi

# 检查主配置文件是否包含敏感信息
if [ -f "src/main/resources/application.yml" ]; then
    if grep -q "your_.*_here" src/main/resources/application.yml; then
        echo -e "${GREEN}✅ 配置文件使用占位符，安全${NC}"
    else
        echo -e "${YELLOW}⚠️  请确认application.yml中没有真实的API密钥${NC}"
    fi
fi

echo -e "${GREEN}✅ 配置文件验证完成${NC}"

# 4. 检查文档完整性
echo -e "${YELLOW}📚 检查文档完整性...${NC}"

required_files=(
    "README.md"
    "LICENSE"
    "CONTRIBUTING.md"
    "CHANGELOG.md"
    "docs/ARCHITECTURE.md"
    "docs/QUICK_START.md"
)

for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✅ $file${NC}"
    else
        echo -e "${RED}❌ 缺少 $file${NC}"
    fi
done

# 5. 验证代码质量
echo -e "${YELLOW}🔍 验证代码质量...${NC}"

# 检查是否有TODO或FIXME
todo_count=$(find src/ -name "*.java" -exec grep -l "TODO\|FIXME" {} \; 2>/dev/null | wc -l)
if [ $todo_count -gt 0 ]; then
    echo -e "${YELLOW}⚠️  发现 $todo_count 个文件包含TODO/FIXME注释${NC}"
    find src/ -name "*.java" -exec grep -l "TODO\|FIXME" {} \; 2>/dev/null
fi

# 检查是否有调试代码
debug_count=$(find src/ -name "*.java" -exec grep -l "System\.out\.print\|printStackTrace" {} \; 2>/dev/null | wc -l)
if [ $debug_count -gt 0 ]; then
    echo -e "${YELLOW}⚠️  发现 $debug_count 个文件包含调试代码${NC}"
fi

echo -e "${GREEN}✅ 代码质量检查完成${NC}"

# 6. 编译测试
echo -e "${YELLOW}🔨 编译测试...${NC}"
if mvn compile -q; then
    echo -e "${GREEN}✅ 编译成功${NC}"
else
    echo -e "${RED}❌ 编译失败，请修复后重试${NC}"
    exit 1
fi

# 7. 生成项目统计
echo -e "${YELLOW}📊 生成项目统计...${NC}"

java_files=$(find src/ -name "*.java" | wc -l)
java_lines=$(find src/ -name "*.java" -exec wc -l {} \; | awk '{sum+=$1} END {print sum}')
config_files=$(find src/main/resources/ -name "*.yml" -o -name "*.txt" -o -name "*.xml" | wc -l)

echo -e "${BLUE}📈 项目统计：${NC}"
echo -e "   Java文件：$java_files 个"
echo -e "   代码行数：$java_lines 行"
echo -e "   配置文件：$config_files 个"

# 8. 创建发布检查清单
echo -e "${YELLOW}📋 创建发布检查清单...${NC}"

cat > RELEASE_CHECKLIST.md << EOF
# 🚀 发布检查清单

## 📋 发布前检查

### 代码质量
- [ ] 所有代码已提交到Git
- [ ] 没有调试代码和临时注释
- [ ] 代码遵循项目规范
- [ ] 编译无错误和警告

### 安全检查
- [ ] 移除了所有真实的API密钥
- [ ] 移除了所有真实的认证token
- [ ] 配置文件使用占位符
- [ ] 没有敏感的IP地址或域名

### 文档完整性
- [ ] README.md 内容完整
- [ ] 架构文档已更新
- [ ] 快速开始指南可用
- [ ] 更新日志已更新
- [ ] 贡献指南完整

### 功能测试
- [ ] 基本功能正常运行
- [ ] 配置文件格式正确
- [ ] 错误处理机制有效
- [ ] 日志输出正常

### 发布准备
- [ ] 版本号已更新
- [ ] Git标签已创建
- [ ] 发布说明已准备
- [ ] 许可证文件存在

## 🎯 发布步骤

1. 最终代码审查
2. 创建发布分支
3. 更新版本号
4. 创建Git标签
5. 推送到远程仓库
6. 创建GitHub Release
7. 更新文档链接

## 📝 发布后

- [ ] 验证GitHub页面显示正常
- [ ] 测试克隆和运行流程
- [ ] 更新相关文档链接
- [ ] 通知社区和用户

---
生成时间：$(date)
EOF

echo -e "${GREEN}✅ 发布检查清单已创建：RELEASE_CHECKLIST.md${NC}"

# 9. 最终提示
echo -e "${BLUE}🎉 发布准备完成！${NC}"
echo -e "${YELLOW}📋 请查看 RELEASE_CHECKLIST.md 完成最终检查${NC}"
echo -e "${YELLOW}🔍 建议手动检查以下内容：${NC}"
echo -e "   1. 所有敏感信息已移除"
echo -e "   2. 配置文件使用模板格式"
echo -e "   3. 文档链接正确"
echo -e "   4. 版本号已更新"

echo -e "${GREEN}✨ 准备上传到开源仓库！${NC}"
