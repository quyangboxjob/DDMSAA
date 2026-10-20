---
name: spec-uttest-backend-codegen
description: 为 DDMSAA 后端公共基础类生成 JUnit 5/Mockito 单元测试，不覆盖 feature 测试流水线。
---

# DDMSAA 后端单元测试代码

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 范围与规则

- 为已实现的公共类创建 JUnit 5/Mockito 测试；feature Service/Controller 的测试交给 `spec-wf-testgen`。
- 以实际包、构造器、方法和配置为准；不得为了示例假设 `UserContext` API、用户 ID、角色或 endpoint。
- 涉及 JWT/授权时，角色仅可来自当前认证设计，测试数据从当前 schema/fixture 策略派生。
- 每个测试应验证可观察结果或交互契约，清理 ThreadLocal、mock 和临时状态。

## 验证

- 使用项目已有 Maven 测试命令；测试未能运行时报告原因，不虚报通过。
