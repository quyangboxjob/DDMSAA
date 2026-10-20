---
name: spec-it-api
description: 为 DDMSAA 已实现的 API 创建、审查并执行基于当前 OpenAPI 契约的黑盒集成测试。
---

# DDMSAA API 集成测试

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 依据与流程

- 按 operationId 或 tag 读取 `spec-07-api-list.yaml`、消息、认证和相关 schema。
- 先产出可审查的测试规格；在需要用户确认的业务数据、破坏性操作或环境影响处停止。确认后再生成 fixtures、测试代码、执行结果和报告。
- 测试通过网关 `:8080` 验证真实已部署 API；不修改被测业务代码。

## 身份与数据

- JWT subject、角色、权限范围和数据准备必须依据当前认证、API 定义、schema 与测试环境的实际配置。
- 角色仅可为当前定义的 `ADMIN`、`EMPLOYEE`、`CUSTOMER`，但每项 API 的授权以其契约为准。
- 不使用固定人员编号、旧角色、旧表、旧 endpoint 或旧种子数据。

## 检查

- 每个案例可追溯到 operationId、认证规则和预期消息/响应。
- 不可运行时报告环境依赖，不能把模拟结果表述为真实集成测试结果。
