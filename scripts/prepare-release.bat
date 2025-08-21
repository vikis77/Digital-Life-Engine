@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: Digital Life Engine - 发布准备脚本 (Windows版本)
:: 用于清理项目并准备上传到开源仓库

echo 🚀 开始准备Digital Life Engine发布...

:: 检查是否在项目根目录
if not exist "pom.xml" (
    echo ❌ 错误：请在项目根目录运行此脚本
    pause
    exit /b 1
)

echo 📋 检查项目结构...

:: 1. 清理编译产物
echo 🧹 清理编译产物...
call mvn clean >nul 2>&1
if exist "target" rmdir /s /q "target"
if exist "logs" rmdir /s /q "logs"
del /q *.log >nul 2>&1
echo ✅ 编译产物清理完成

:: 2. 检查敏感信息
echo 🔍 检查敏感信息...

:: 检查API密钥
findstr /s /i "sk-" src\*.* >nul 2>&1
if !errorlevel! equ 0 (
    echo ⚠️  警告：发现可能的API密钥，请检查并移除
)

:: 检查真实token
findstr /s /i "Bearer ey" src\*.* >nul 2>&1
if !errorlevel! equ 0 (
    echo ⚠️  警告：发现可能的真实token，请检查并移除
)

:: 检查IP地址
findstr /s /i "192.168." src\*.* >nul 2>&1
if !errorlevel! equ 0 (
    echo ⚠️  发现内网IP地址，请确认是否需要替换为示例地址
)

echo ✅ 敏感信息检查完成

:: 3. 验证配置文件
echo 📝 验证配置文件...

if not exist "src\main\resources\application.yml.template" (
    echo ❌ 缺少配置模板文件
    pause
    exit /b 1
)

if exist "src\main\resources\application.yml" (
    findstr /i "your_.*_here" src\main\resources\application.yml >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ 配置文件使用占位符，安全
    ) else (
        echo ⚠️  请确认application.yml中没有真实的API密钥
    )
)

echo ✅ 配置文件验证完成

:: 4. 检查文档完整性
echo 📚 检查文档完整性...

set "files=README.md LICENSE CONTRIBUTING.md CHANGELOG.md docs\ARCHITECTURE.md docs\QUICK_START.md"

for %%f in (%files%) do (
    if exist "%%f" (
        echo ✅ %%f
    ) else (
        echo ❌ 缺少 %%f
    )
)

:: 5. 验证代码质量
echo 🔍 验证代码质量...

:: 检查TODO或FIXME
set todo_count=0
for /r src %%f in (*.java) do (
    findstr /i "TODO FIXME" "%%f" >nul 2>&1
    if !errorlevel! equ 0 (
        set /a todo_count+=1
    )
)

if !todo_count! gtr 0 (
    echo ⚠️  发现 !todo_count! 个文件包含TODO/FIXME注释
)

:: 检查调试代码
set debug_count=0
for /r src %%f in (*.java) do (
    findstr /i "System.out.print printStackTrace" "%%f" >nul 2>&1
    if !errorlevel! equ 0 (
        set /a debug_count+=1
    )
)

if !debug_count! gtr 0 (
    echo ⚠️  发现 !debug_count! 个文件包含调试代码
)

echo ✅ 代码质量检查完成

:: 6. 编译测试
echo 🔨 编译测试...
call mvn compile -q
if !errorlevel! equ 0 (
    echo ✅ 编译成功
) else (
    echo ❌ 编译失败，请修复后重试
    pause
    exit /b 1
)

:: 7. 生成项目统计
echo 📊 生成项目统计...

set java_files=0
for /r src %%f in (*.java) do (
    set /a java_files+=1
)

set config_files=0
for /r src\main\resources %%f in (*.yml *.txt *.xml) do (
    set /a config_files+=1
)

echo 📈 项目统计：
echo    Java文件：!java_files! 个
echo    配置文件：!config_files! 个

:: 8. 创建发布检查清单
echo 📋 创建发布检查清单...

(
echo # 🚀 发布检查清单
echo.
echo ## 📋 发布前检查
echo.
echo ### 代码质量
echo - [ ] 所有代码已提交到Git
echo - [ ] 没有调试代码和临时注释
echo - [ ] 代码遵循项目规范
echo - [ ] 编译无错误和警告
echo.
echo ### 安全检查
echo - [ ] 移除了所有真实的API密钥
echo - [ ] 移除了所有真实的认证token
echo - [ ] 配置文件使用占位符
echo - [ ] 没有敏感的IP地址或域名
echo.
echo ### 文档完整性
echo - [ ] README.md 内容完整
echo - [ ] 架构文档已更新
echo - [ ] 快速开始指南可用
echo - [ ] 更新日志已更新
echo - [ ] 贡献指南完整
echo.
echo ### 功能测试
echo - [ ] 基本功能正常运行
echo - [ ] 配置文件格式正确
echo - [ ] 错误处理机制有效
echo - [ ] 日志输出正常
echo.
echo ### 发布准备
echo - [ ] 版本号已更新
echo - [ ] Git标签已创建
echo - [ ] 发布说明已准备
echo - [ ] 许可证文件存在
echo.
echo ## 🎯 发布步骤
echo.
echo 1. 最终代码审查
echo 2. 创建发布分支
echo 3. 更新版本号
echo 4. 创建Git标签
echo 5. 推送到远程仓库
echo 6. 创建GitHub Release
echo 7. 更新文档链接
echo.
echo ## 📝 发布后
echo.
echo - [ ] 验证GitHub页面显示正常
echo - [ ] 测试克隆和运行流程
echo - [ ] 更新相关文档链接
echo - [ ] 通知社区和用户
echo.
echo ---
echo 生成时间：%date% %time%
) > RELEASE_CHECKLIST.md

echo ✅ 发布检查清单已创建：RELEASE_CHECKLIST.md

:: 9. 最终提示
echo.
echo 🎉 发布准备完成！
echo 📋 请查看 RELEASE_CHECKLIST.md 完成最终检查
echo 🔍 建议手动检查以下内容：
echo    1. 所有敏感信息已移除
echo    2. 配置文件使用模板格式
echo    3. 文档链接正确
echo    4. 版本号已更新
echo.
echo ✨ 准备上传到开源仓库！

pause
