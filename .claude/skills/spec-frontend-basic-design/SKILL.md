---
name: spec-frontend-basic-design
description: 基于 DDMSAA 现有需求、画面、认证和 API 设计起草或更新前端基本设计。
---

# DDMSAA 前端基本设计

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 输入与输出

- 使用需求、`docs/基本設計/共通/spec-05-screen-list-ui.md`、对应 feature 业务/画面设计、认证设计、OpenAPI 和消息 SSOT。
- 在现有 `docs/基本設計/` 结构中更新相应设计；新增文档或目录需要与现有设计体系一致，不能借用旧编号体系。

## 规则

- 画面、路由、操作、权限、字段、错误和多语言消息分别从当前 screen、业务、认证、API 和消息设计取得。
- 直接用 API `operationId` 及契约建立前后端关联。仓库当前没有独立 ID 映射表时，不把它设为前置条件；如确实需要新增此类产物，先说明理由并请求确认。
- 不重复定义后端契约，不建立旧式 SCR/IF 或工作流编号映射。
- 设计尚未决定的 UX、状态或权限范围须停止并询问。

## 检查

- 每个引用的 API、角色、消息和画面均能在当前 SSOT 中找到。
- 不依赖不存在的前端详细设计树或已废弃的映射文件。
