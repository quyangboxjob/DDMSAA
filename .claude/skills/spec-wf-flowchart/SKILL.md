---
name: spec-wf-flowchart
description: 从 DDMSAA feature 的需求、业务基本设计和详细设计生成或更新 Mermaid 业务流程图。
---

# DDMSAA 业务流程图

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 输入与输出

- 输入：`docs/要件定義/requirements.md` 中该 feature 的部分、`spec-<feature>-business-ui.md`、`spec-<feature>-screen-ui.md`（若存在）、`docs/基本設計/共通/spec-08-business-rule-list.md` 的相关 BR，以及 `spec-<feature>-detail-sd.md`。
- 前置：不要求详细设计已含 §0 等完整章节；只要有 API/状态迁移/处理步骤即可作图，缺失的章节登记为课题，不停止。
- 输出：`docs/詳細設計/業務/<feature>/spec-<feature>-flow.md`。
- 同步更新 `docs/詳細設計/業務/<feature>/spec-<feature>-filelist.md`，仅记录本次流程图产物。

## 规则

- 用 Mermaid 活动图或状态图表达已定义的业务步骤、分支、异常与状态变化。
- 角色、状态、错误和 API 名称必须来自当前设计；不可写入固定旧角色、人员编号或旧 feature 示例。
- 三层文档（需求 / 基本设计 / 详细设计）存在矛盾、欠落或 BR 编号不一致时，登记到 `docs/詳細設計/業務/spec-business-issue.md`（业务总课题表），不能自行取舍或掩盖。
- 更新既有图时先比较后合并，不盲目覆盖人工修改。

## 检查

- 文件名、目录和清单均使用 `spec-<feature>-*` 命名。
- Mermaid 语法可解析，且所有节点都可回溯到输入设计。
