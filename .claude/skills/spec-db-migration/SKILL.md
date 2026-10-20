---
name: spec-db-migration
description: 为 DDMSAA 已构筑数据库生成或维护受控的 Flyway 增量迁移。
---

# DDMSAA Flyway 迁移

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 范围

- 迁移目录：`ddmsaa-backend/infrastructure/src/main/resources/db/migration/`。
- 初始 schema 由 Docker 初始化流程构筑；Flyway `baseline-version=0`，初始数据为 `V1`，后续结构变更从 `V2+` 开始。
- 以 `docs/基本設計/共通/spec-06-db-physical.yaml` 为数据库结构依据（`schema/schema-definition.yaml` 为其派生物，冲突时以 06 为准），以 `docs/基本設計/データベース/spec-db-migration-ui.md` 为职责边界，以 `docs/開発規約/spec-flyway-migration-rule.md` 和 `spec-database-rule.md` 为 SQL 与命名规范。
- `*_codes` 代码表（T07，共 13 张）的种子由 `R__` 脚本 upsert（见 `spec-06-db-physical.yaml` 的 `master_data`）；初始数据（`V1`，如初始管理员 A0001 与连络先）的内容依据 `spec-17-data-migration-ui.md`，该文档目前仍是模板，缺失时停止并要求先补全。

## 工作规则

1. 在编写结构变更前，先确认 schema 定义及相关设计已更新；未更新或有冲突时停止。
2. 已应用的 `V*` 文件不可修改；新变更创建新的版本迁移。重复执行对象使用适当的 `R__` 迁移。
3. 不把初始建表、数据库/角色创建或权限 bootstrap 写入增量迁移；它们属于初始构筑流程。
4. 破坏性或不可逆变更、数据迁移策略和初始数据内容需要用户决策后才执行。

## 前置条件（缺失即停止并说明先做什么）

- `ddmsaa-backend/infrastructure/` 与迁移目录必须已存在（由 `spec-scaffold-backend` 创建）。
- Docker 初始化 SQL（`docs/基本設計/データベース/setup/`）必须已存在（由 `spec-scaffold-db` 创建），否则无法确定 `V1` 之前的基线。
- 已知文档问题（按优先级处理并报告）：`spec-flyway-migration-rule.md` 与 `spec-db-migration-ui.md` 仍写“14 表”（实际 31 张）；`spec-database-rule.md` §4.1-SEQ 把 `init_functions.sql` 放在 Flyway 执行顺序里，与“Docker 初始化创建 schema”的分工矛盾。

## 检查

- 迁移与 schema、命名规约及当前 Flyway 配置一致。
- 明确列出依赖、回滚限制及未执行的验证；不引用已废弃的目录或文件名。
