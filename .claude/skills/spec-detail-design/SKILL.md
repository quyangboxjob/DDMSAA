---
name: spec-detail-design
description: 将 DDMSAA 某个 feature 的业务基本设计落成可实现、可测试的详细设计。
---

# DDMSAA Feature 详细设计

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 输入与输出

- 输入：`docs/基本設計/業務設計/<feature>/spec-<feature>-business-ui.md`，相关画面设计、需求、共通/基础设施设计及当前 SSOT。
- 输出：`docs/詳細設計/業務/<feature>/spec-<feature>-detail-sd.md`。
- `<feature>` 必须存在于当前业务设计目录；不得使用固定工作流编号或从旧项目推断领域。

## 必须遵循

- 表、列、主键、外键、枚举和审计字段只从 `docs/基本設計/共通/spec-06-db-physical.yaml` 获取（`schema/schema-definition.yaml` 为派生物）；不预设主键形式、表名或采番策略。
- 业务规则引用 `docs/基本設計/共通/spec-08-business-rule-list.md` 的 BR 编号，不另起编号；画面行为引用 `spec-<feature>-screen-ui.md` 与 `spec-05/10/12`。
- API、请求/响应、operationId 从 `spec-07-api-list.yaml` 获取；消息从 `spec-09-message-list.yaml` 获取；角色和数据范围从 `spec-03-auth-ui.md` 与 API 定义获取。
- 已有后端脚手架时，以实际类、包和方法签名校验设计；脚手架尚不存在时记录所需接口，不编造实现签名。
- 对事务、并发、状态迁移、权限范围等未决定事项停止并请求决策。

## 模板与更新模式

- 结构以 `docs/詳細設計/spec-detail-temp.md` 为准（§0 / §0.1 / §0.2 / §1.1 / §1.2 / §5.1 / §6.1 等；先读该模板）。
- 现有 7 份 `detail-sd` 只有简版（约 40–80 行，测试表在 `## 6. テスト`）。已存在时走更新模式：补全模板缺少的章节，保留已有内容和 BR 编号，把测试表整理到模板的 §5.1 并保持原有 `UT-<MOD>-NN` 编号；整理前后的差异在完成时报告。
- 与 `spec-test-rule.md` 的 `UT-<FEATURE>-NNN`（3 位）编号规则不一致：不擅自改编号，列入待确认。
- 代码尚不存在时，共通类和包路径以 `基盤/インタフェース仕様/*.yaml`（`spec-project-structure.yaml`、`spec-10-business-core.yaml`、`spec-07-infrastructure.yaml`）为准。

## 产物内容

详细设计应包含涉及的契约与数据、逐 API 处理步骤、异常和权限规则、模块归属、外部依赖、以及可追溯的测试用例清单。每个测试用例应来自设计中的正常流、异常流或授权规则，而不是固定的旧领域样例。

## 完成检查

- 输出文件名和目录遵循 `spec-<feature>-detail-sd.md`。
- 设计中的表、角色、operationId、messageId 均存在于当前 SSOT。
- 不含旧工作流、旧目录、员工/部门或旧角色的默认规则。
