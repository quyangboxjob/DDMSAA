---
name: spec-uttest-backend
description: 为 DDMSAA 后端公共基础类编写或更新单元测试规格，不直接生成测试代码。
---

# DDMSAA 后端单元测试规格

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 范围

- 面向 `common`、`infrastructure`、gateway/auth 的公共类和已存在实现，产出 Markdown 测试项。
- feature 业务代码的用例由 feature 详细设计与 `spec-wf-testgen` 管理，避免重复定义。

## 规则

- 从实际签名、认证设计、消息、schema 和现有测试读取事实；未实现类不得假设其行为。
- 认证与身份相关规格以 Magic Link/JWT 和当前角色模型为准，不使用旧用户标识或角色示例。
- 规格逐项写明前置条件、输入、期望结果、异常和对应来源。
