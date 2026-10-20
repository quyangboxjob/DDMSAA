---
name: spec-wf-testgen
description: 根据 DDMSAA feature 详细设计中的测试用例清单，为已生成的后端业务代码创建并验证测试。
---

# DDMSAA Feature 测试生成

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 前置条件

- 读取 `docs/詳細設計/業務/<feature>/spec-<feature>-detail-sd.md` 的测试用例清单：按 `spec-detail-temp.md` 在 §5.1；当前简版文件在 `## 6. テスト`。哪一节存在就读哪一节，沿用文件里的 UT-ID（如 `UT-ORD-01`），不要改写编号；两种位置/编号规则不一致属已知问题，在报告中列出（见 `DDMSAA-PROJECT-CONTEXT.md`）。
- 读取 `docs/詳細設計/業務/<feature>/spec-<feature>-filelist.md`，并确认被测代码真实存在。
- 共通测试 harness、API、消息、认证与 schema SSOT 均可用；缺失时停止并报告，不编造用例、身份或数据。

## 规则

- 将详细设计中的正常流、异常流、权限、数据范围与状态迁移转化为测试；每个测试可追溯到设计用例。
- L2/L3 身份、JWT subject、角色和测试数据必须来自当前认证/API/schema 设计及实际测试配置。不要使用固定旧角色、员工编号或旧领域路径。
- 测试写入目标模块的 `src/test`，`@DisplayName` 以 UT-ID 开头（`docs/開発規約/spec-test-rule.md`）；测试清单写 `docs/詳細設計/業務/<feature>/spec-<feature>-testlist.md`，执行结果写同目录 `spec-<feature>-testreport.md`；不得修改被测业务代码以使测试通过。
- 测试分层 L1（单元）/ L2（Web 切片）/ L3（Testcontainers 结合）依 `spec-test-rule.md`；身份用离线签发的 JWT（`issue(userId, role)`，角色 ADMIN/EMPLOYEE/CUSTOMER），测试种子来自 `spec-scaffold-test` 的 harness 与 06 yaml 的种子策略。

## 质量门

- 无空断言、吞异常或只验证 mock 调用而未验证结果的测试。
- 按项目实际命令执行；读取可用的 JUnit XML 汇总结果。无法运行时如实说明原因和未验证范围。
- 回写详细设计追溯项时保留人工内容。
