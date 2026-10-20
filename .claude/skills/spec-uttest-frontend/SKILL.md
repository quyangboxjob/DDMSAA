---
name: spec-uttest-frontend
description: 为 DDMSAA 前端的 API 封装、Hooks 和组件编写或更新单元测试规格，不直接生成测试代码。
---

# DDMSAA 前端单元测试规格

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 范围

- 覆盖 `frontend/src/` 中可独立验证的 API 层、Hooks 和组件；复杂页面流程由前端集成测试处理。
- 依据真实组件 API、OpenAPI、i18n、认证和设计文档形成测试项。

## 规则

- 不使用旧 endpoint、员工标识、旧角色或固定业务样本作为通用模板。
- 每项规格说明测试目的、前置、操作、期望及来源；对未定行为提出问题而非补写假设。
