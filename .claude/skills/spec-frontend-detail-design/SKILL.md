---
name: spec-frontend-detail-design
description: 根据 DDMSAA 当前前端基本设计和实际脚手架，为 feature 或画面编写可实现的前端详细设计。
---

# DDMSAA 前端详细设计

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 工作方式

- 从当前 feature 的业务/画面设计、OpenAPI、消息、认证规则和现有 `frontend/` 代码读取事实。
- 将组件、页面、状态、路由、表单校验、错误呈现和 i18n 行为记录到与现有详细设计目录一致的位置；目录未确定时先询问。
- React 19、TypeScript strict、React Router、Context + Hooks、Ant Design 和 `react-i18next` 是当前技术约束；不要引入未经设计确认的状态库或框架。

## 约束

- 请求和响应以 OpenAPI 为准，权限和可见范围以认证/API 定义为准。
- 复用实际已存在的组件、类型与路由；实现与设计冲突时报告而非猜测。
- 不依赖旧编号、旧目录、旧角色或不存在的前端设计文档。
