---
name: spec-scaffold-test
description: 为 DDMSAA 后端构建共享测试基盘，包括测试配置、容器集成测试支持和认证测试辅助工具。
---

# DDMSAA 后端测试脚手架

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 范围

- 为 `ddmsaa-backend` 提供 JUnit 5、Mockito、Spring Security Test 与 Testcontainers 所需依赖和共享基类。
- 提供测试 profile、可控的外部依赖替身、Result/ErrorResponse 断言及按当前 JWT 配置工作的测试辅助工具。
- 不生成 feature 的具体测试用例；它们由 `spec-wf-testgen` 根据详细设计处理。

## 规则

- 测试身份的 subject、角色与权限必须来自当前认证/API 设计和实际 JWT 实现；不使用旧人员编号、旧角色或固定角色编号映射。
- 数据库 fixture 从当前 schema 和已定义种子策略派生，不假设表数或业务样本。
- 密钥只用于测试配置，采用占位或隔离值；不得提交生产密钥。

## 检查

- 测试基盘能被目标模块引用，且不会依赖尚未存在的旧项目路径或类名。
- 记录可运行的验证命令和无法验证的外部依赖。
