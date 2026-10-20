---
name: spec-frontend-codegen
description: 根据 DDMSAA 当前前端详细设计和 API 契约生成或更新 React/TypeScript 前端代码。
---

# DDMSAA 前端代码生成

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 前置条件

- 目标 feature 的设计、API 契约、消息和前端脚手架必须存在；缺失或含 TBD 时停止并报告。
- 先检查 `frontend/` 实际结构、现有组件、路由和 i18n 配置，采用合并更新而不是覆盖。

## 生成规则

- 代码写入 `frontend/src/` 下与现有 feature 布局一致的 `types`、`api`、`components`、`pages` 等目录。
- 使用 React 19、TypeScript strict、Ant Design、React Router、Context + Hooks 与 `react-i18next`；请求经项目既有 HTTP 层调用当前 OpenAPI 定义的接口。
- 类型、字段、操作、错误和授权 UI 必须从当前 SSOT 推导；不要生成员工/部门等旧领域 API 或静态旧角色判断。
- 为变更后的逻辑补充合适测试，并执行已有的类型检查/测试命令。

## 完成检查

- 无未定义的 API、消息、路由或翻译键。
- 不覆盖人工改动；无法验证时说明原因与风险。
