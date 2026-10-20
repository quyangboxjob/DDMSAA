---
name: spec-basic-design
description: 根据 DDMSAA 要件定义起草或更新共通、数据库、画面或 feature 业务基本设计；仅使用当前设计文档的 SSOT。
---

# DDMSAA 基本设计

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。其中列出的路径、角色、端口和技术约束优先于本技能及任何旧示例。

## 范围

- 共通设计写入 `docs/基本設計/共通/`；基础设施设计写入 `docs/基本設計/基盤/`；数据库设计写入 `docs/基本設計/データベース/`。
- feature 业务设计写入 `docs/基本設計/業務設計/<feature>/spec-<feature>-business-ui.md`；画面设计写入同目录的 `spec-<feature>-screen-ui.md`。
- `<feature>` 必须是已有业务目录。当前目录或需求未能确定 feature 时，停止并询问；不使用旧工作流编号或推测映射。

## 工作方式

1. 读取需求、关联的共通设计，以及本次涉及的 API、消息、认证和 schema SSOT。
2. 只在当前设计尚未决定的事项上请求用户决策；发现文档冲突时报告冲突，不自行选择旧规则。
3. API 以 `spec-07-api-list.yaml` 为准，消息以 `spec-09-message-list.yaml` 为准，表/列以 `spec-06-db-physical.yaml` 为准（`schema-definition.yaml` 是其派生物，冲突时以 06 为准），业务规则编号以 `spec-08-business-rule-list.md` 为准，权限以 `spec-03-auth-ui.md` 和每个 operation 的 `x-roles` 为准。
4. 更新已有文档时按章节合并并保留人工内容；新建文档时遵循同目录现有文档的格式。
5. 共通文档（`spec-00`〜`spec-18`）已基本存在，默认走**更新模式**：先比较差异，按章节合并，保留人工内容；仍是模板的文档（如 spec-14/17/18、spec-01 §3–§6）才从模板起草。`devtool` 的 block/endblock 标记必须保持成对。
6. 变更影响 API、消息、表结构、BR 编号或 SCR 时，按顺序同步：06 yaml → schema-definition → mapping / spec-13 → 07 → 09 三语 → 02/05/10 → 业务设计和详细设计，并在完成时列出已同步和未同步的文件。

## 完成检查

- 所有引用均能解析到当前仓库中的文件或已定义的 operationId/messageId/SCR-ID/BR 编号/表和列。
- 机读文件（06/07/09 yaml、schema-definition.yaml）能被 YAML 解析器解析；09 的 ja/zh/en 三份 key 与 category 一致；`numeric(p,s)` 这类含逗号的类型在 flow mapping 中必须加引号。
- 不引入旧项目目录、工作流编号、旧角色、员工/部门示例或废弃数据库 SSOT。
- 若变更影响 API、消息、认证或 schema，明确列出需同步确认的设计文件。
