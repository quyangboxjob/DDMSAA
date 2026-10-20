---
name: spec-uttest-frontend-codegen
description: 为 DDMSAA React/TypeScript 前端的函数、Hooks 和组件生成并执行 Vitest 单元测试。
---

# DDMSAA 前端单元测试代码

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 规则

- 为实际存在的纯函数、Hooks 与组件编写 Vitest/Testing Library 测试；页面级路由与多组件流程交由 `spec-it-frontend`。
- 使用实际 HTTP、Context、i18n 和类型契约。mock 仅隔离网络或外部边界，不能复制旧领域 API、用户 ID 或角色。
- 测试名称表达可观察行为；避免空断言和只断言实现细节。

## 验证与报告

- 通过项目已有 pnpm/Vitest 配置运行相关测试；必要时读取 JUnit XML 生成简要报告。
- 无法运行时保留测试文件并如实记录环境阻塞，不宣称已验证。
