---
name: spec-scaffold-db
description: 从 DDMSAA 当前数据库和基础设施设计构筑本地 PostgreSQL/Docker 初始化环境。
---

# DDMSAA 数据库脚手架

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 权威输入

- 表、列、约束、索引与种子数据范围：`docs/基本設計/共通/spec-06-db-physical.yaml`（31 张表：13 张 `*_codes` + 18 张业务表；`master_data` 段给出种子范围）。`schema/schema-definition.yaml` 是其派生物，冲突时以 06 为准。
- 初始构筑与 Flyway 职责边界：`docs/基本設計/データベース/spec-db-migration-ui.md`；连接与端口：`spec-db-connection-ui.md`、`spec-db-foundation-temp.md`（主机端口 `5434`）。
- 架构、端口、容器职责：`docs/基本設計/共通/spec-01-system-architecture-ui.md`（§3 拓扑目前仍是占位，缺失时报告并询问）和 `基盤/インタフェース仕様/spec-project-structure.yaml`。

## 生成范围

- 生成或更新 Docker 初始化所需的 SQL、初始化脚本、Compose 配置及本地使用说明。
- SQL 输出到 `docs/基本設計/データベース/setup/`（按建表依赖顺序：函数/序列 → `*_codes` → 其余表；顺序以 06 yaml 的 FK 为准）；Compose/Nginx/Postgres 配置位置在 spec-01 §3 确定前停止并请求确认，不能复用旧项目目录。
- Docker 初始化创建初始 schema；应用运行后的初始数据和增量变更按迁移设计交由 Flyway 处理。

## 规则与检查

- DDL 逐项来自 06 yaml，不假定表数、建表顺序、主键形式、采番函数或种子数据；枚举列必须有对应 `*_codes` 外键（不用 CHECK）；核对表数 = 06 yaml 的表数。
- 种子：T07 的 13 张 `*_codes`、`tax_rates`（STANDARD 10 / REDUCED 8 / EXEMPT 0）、`approval_rules` 初版 1 行、初始管理员 `A0001`（见 06 yaml `master_data`）。
- 敏感配置仅使用环境变量和示例文件，不提交真实密钥。
- 验证 PostgreSQL 版本、映射端口和 Nginx 反代目标与当前架构一致；Host PostgreSQL 端口为 `5434`。
- 结构变更先更新 SSOT，再更新初始化 DDL；上线后变更改走 `spec-db-migration`。
