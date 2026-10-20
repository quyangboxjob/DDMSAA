---
name: spec-scaffold-frontend
description: 按 DDMSAA 当前前端架构创建 React/TypeScript 公共工程骨架，不生成 feature 页面。
---

# DDMSAA 前端脚手架

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 范围

- 在 `frontend/` 构筑 Vite、React 19、TypeScript strict、Ant Design、React Router、Context + Hooks、axios 和 `react-i18next` 的共享配置。
- 初始化 API 客户端、JWT 注入、通用响应/错误类型、多语言资源、路由壳和 feature 目录；不生成具体业务页面或接口。

## 规则

- API base URL、错误结构、角色、消息和 i18n 资源从当前架构、OpenAPI、认证与消息 SSOT 推导。
- 仅支持 `ADMIN`、`EMPLOYEE`、`CUSTOMER` 当前角色体系；不保留旧领域用户或接口示例。
- 不引入未经设计确认的状态管理库。

## 检查

- 类型检查、构建配置和本地开发端口均与当前设计一致；开发服务器端口为 `3000`，除非设计已变更。
- 所有敏感值以环境变量示例提供。
