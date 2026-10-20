---
name: spec-it-frontend
description: 为 DDMSAA 前端页面生成并执行以当前画面设计、OpenAPI 与消息契约为依据的集成测试。
---

# DDMSAA 前端集成测试

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 前置条件与范围

- 输入为当前 feature 的画面/详细设计、实现代码、OpenAPI、消息和认证规则。
- 若页面详细设计尚不存在，先以现有业务/画面设计和实现确认测试范围；信息不足时停止，而不是要求不存在的旧目录或映射文件。
- 用 Vitest、Testing Library、MemoryRouter、实际 Context Provider 和 MSW 覆盖页面、状态、路由和 API 接线；仅 mock 网络边界。

## 规则

- MSW handler 的方法、路径、响应和 messageId 直接从 OpenAPI/消息 SSOT 获取。
- 身份、可见性和权限案例依据当前认证/API 定义；不硬编码旧 SCR/IF 映射、角色或业务数据。
- 先形成可审查的案例规格，再生成代码；测试、fixture、报告写入当前项目既有目录结构。

## 检查

- 测试覆盖关键成功、错误、授权和状态场景，且每个场景可追溯到设计。
- 执行结果必须来自实际测试运行；不能运行时如实记录原因。
