---
name: spec-scaffold-deploy
description: 为 DDMSAA 已落地的后端模块创建或维护应用容器化部署配置。
---

# DDMSAA 应用部署脚手架

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 范围

- 面向已存在的 `ddmsaa-backend` 模块生成 Dockerfile、Compose 应用 profile 和运行说明。
- 拓扑、模块、端口、镜像基础和环境变量均以当前基础设施设计和实际 `application.yml` 为准。
- 前端生产托管、域名、TLS、真实密钥和未设计的外部平台不在范围内。

## 规则

- 默认本地数据库环境与应用容器化配置保持职责分离；不破坏现有仅基础设施的启动路径。
- 配置使用环境变量名和示例值，绝不复制真实密钥。
- 若现有容器配置或架构设计冲突，停止并报告。

## 检查

- 模块、网络、端口、健康检查和挂载路径均与当前架构一致。
- 生成后验证可解析的 Compose/Docker 配置；实际构建或运行仅在用户授权的环境中执行。
