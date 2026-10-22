---
name: spec-wf-codegen
description: 根据 DDMSAA feature 详细设计生成 Spring Boot/MyBatis 后端业务代码，并保持与当前 SSOT 和脚手架一致。
---

# DDMSAA 后端 Feature 代码生成

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 输入、输出与边界

- 输入：`docs/詳細設計/業務/<feature>/spec-<feature>-detail-sd.md` 及当前 API、消息、认证、schema SSOT。
- 清单：`docs/詳細設計/業務/<feature>/spec-<feature>-filelist.md`。
- 业务代码通常写入 `ddmsaa-backend/business-service-core/src/main/java/com/example/ddmsaa/core/<feature>/`；认证相关代码只在设计明确要求时写入 `auth-service`。
- 本技能不创建或修改数据库 DDL、迁移、共通脚手架或前端代码。

## 前置检查（缺任何一项即停止，并说明先运行哪个 skill）

| 条件 | 缺失时 |
|---|---|
| `ddmsaa-backend/` 多模块工程及 `common`/`infrastructure` 共通类存在（`Result`、`PageResult`、`BusinessException`、`MaskingService`、`UserContext`、`MetaObjectHandler`） | 先运行 `spec-scaffold-backend` |
| 目标 feature 的详细设计存在且含 API 处理步骤、状态迁移、测试清单 | 先运行 `spec-detail-design`（现有 7 份详细设计较简略，需按 `spec-detail-temp.md` 补全） |
| `spec-06-db-physical.yaml` / `spec-07-api-list.yaml` / `spec-09-message-list.yaml` 可解析 | 先修复机读文件 |
| 共通组件缺少所需类（如 `PayloadTooLargeException`） | 报告缺口，交由脚手架补齐，不自行发明 |

## 生成规则

- 按 `assets/` 的模板逐层生成，顺序：枚举 → Entity → Mapper（接口 + XML）→ DTO → Converter → ServiceImpl → Controller → Batch。模板与 `docs/開発規約/spec-backend-coding-rule.md` 冲突时以规约为准，并报告模板需回写。
- 关键裁定（详见 `assets/README.md`）：Converter 用 **MapStruct**；Mapper XML 以 **resultMap** 明示映射（开启下划线映射）；`created_by`/`updated_by` 由 `MetaObjectHandler` 自动写入，`created_at`/`updated_at` 由数据库（DEFAULT/触发器）设置，Mapper 的 INSERT/UPDATE 不写时间列，Service 不手填；枚举为 INT + `*_codes`，Entity 用 `Integer`；乐观锁 `updated_at`，冲突 `E0051`；业务规则按 `spec-08-business-rule-list.md` 的 BR 编号执行。

1. 先确认详细设计、目标模块和必要脚手架均存在；缺少任何一项时停止并说明依赖。
2. Entity、Mapper、DTO、Controller、服务逻辑和授权仅从 schema、OpenAPI、消息与认证设计导出。不要预设表、主键、用户标识、角色或 REST 路径。
3. 使用真实的 `com.example.ddmsaa` 类与方法签名；若所需共通能力不存在，停止并交由脚手架工作流补齐。
4. 生成后更新本 feature 的 `spec-<feature>-filelist.md` 和详细设计中的追溯信息，采用合并方式，不覆盖人工内容。

## 质量门

- 路径与包：业务代码在 `com.example.ddmsaa.core.<feature>`。
- 无手写 Converter 实现类；Mapper XML 无 `${}`、所有查询带 `deleted_at IS NULL`（无该列的表除外）；Service 中没有设置 `createdBy/updatedBy`。
- Javadoc 随代码一起生成（coding-rule §11.1）；换行与编码遵循 `spec-generation-rule.md` §2。

- API operationId、请求/响应、消息和授权规则可追溯到 SSOT。
- 数据库映射逐列匹配 schema；发现需要 schema 变更时停止，先完成设计与迁移流程。
- 编译、格式化和项目现有测试按实际可用命令执行；若工程尚未落地，报告未执行项。
