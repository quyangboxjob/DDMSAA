---
name: spec-scaffold-backend
description: 按 DDMSAA 当前基础设施设计构建 Spring Boot 多模块后端公共基盘，不生成 feature 业务功能。
---

# DDMSAA 后端脚手架

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 范围

- 在 `ddmsaa-backend/` 构筑 `api-gateway`、`auth-service`、`business-service-core`、`common`、`infrastructure` 模块及共享配置。
- Java 包根为 `com.example.ddmsaa`。技术版本、模块依赖、端口和配置键以当前架构与项目结构设计为准。
- 只创建公共能力与空的 feature 扩展点；Entity、Mapper、Controller 和业务服务交由 feature 工作流生成。

## 规则

- 认证实现为 Magic Link + JWT；不生成密码、密码哈希、刷新令牌或旧项目身份模型。
- JWT 角色仅使用 `ADMIN`、`EMPLOYEE`、`CUSTOMER`；每个 API 的权限从契约读取。
- 共通类、包和方法签名应来自当前设计；未定义的能力记录为缺口，不从旧示例臆造。
- 数据源、Flyway、Redis、文件存储等配置使用环境变量占位，不提交密钥。

## 检查

- Maven 多模块可解析，模块和包名与架构文档一致。
- 网关、认证和业务服务端口分别为 `8080`、`8081`、`8082`，除非当前设计明确变更。
