# `.claude/` — DDMSAA AI 开发命令总览

> 本目录存放 DDMSAA 项目用 **Claude Code** 做 AI 开发的命令（skills），共 21 个。每个 skill 对应开发流水线的一个阶段，用斜杠调用（如 `/spec-basic-design 06`、`/spec-wf-codegen order`）。
> 本文件是**人看的索引**。各 skill 的执行指令见 `skills/<name>/SKILL.md`；**所有 skill 开工前先读 [`skills/DDMSAA-PROJECT-CONTEXT.md`](skills/DDMSAA-PROJECT-CONTEXT.md)**（路径、权威顺序、技术约束、已知文档冲突都在那里，只写一处）。
> DDMSAA 目前**只有文档，没有代码**（`ddmsaa-backend/`、`frontend/`、建表 SQL 均未创建），所以写代码和测试的 skill 都有未满足的前置，见 §5。

---

## 1. 开发流水线

```
docs/要件定義/requirements.md
   │  /spec-basic-design <id|feature>     ① 要件 → 基本设计（共通 spec-00..18 已基本存在 → 以更新模式 + 一致性检查为主）
   ▼
docs/基本設計/{共通, 基盤, データベース, 業務設計/<feature>}
   │  /spec-detail-design <feature>       ② 基本设计 → 详细设计（现有 7 份是简版，需按 spec-detail-temp.md 补全）
   ▼
docs/詳細設計/業務/<feature>/spec-<feature>-detail-sd.md
   │  /spec-wf-flowchart <feature>        ②' 业务流程图（旁支，只读文档）
   │
   │  /spec-scaffold-db                   ⓪ 建库 SQL + Docker（生成 docs/基本設計/データベース/setup/*.sql）
   │  /spec-scaffold-backend              ⓪ 后端基盘（ddmsaa-backend/）
   │  /spec-scaffold-frontend             ⓪ 前端骨架（frontend/）
   │  /spec-scaffold-test                 ⓪ 后端测试基盘
   │  /spec-scaffold-deploy               ⓪' 应用容器化（可选）
   │  /spec-db-migration                  Flyway 增量迁移（V2+/R__）
   │  /spec-sql-lint                      SQL 静态检查
   │  /spec-wf-codegen <feature>          ③ 详细设计 → 后端业务代码
   │  /spec-frontend-codegen <feature>    ③' 画面设计 → 前端代码
   ▼
代码 + Docker 环境
   │  /spec-wf-testgen <feature>          ④ 详细设计测试清单 → 后端测试（L1/L2/L3）
   │  /spec-uttest-backend[-codegen]      共通基盘类的单体测试（规格 / 代码）
   │  /spec-uttest-frontend[-codegen]     前端单体测试（规格 / 代码）
   │  /spec-it-api                        API 结合测试（黑盒，经网关 :8080）
   │  /spec-it-frontend                   画面结合测试（MSW）
   ▼
测试代码 + testlist / testreport
```

`<feature>` = `user` / `customer` / `product` / `order` / `approval` / `attachment` / `report`（取代旧的 WFxx 编号）。

## 2. 命令一览（21 个）

| 命令 | 阶段 | 主要输入 | 主要输出 | 当前状态（DDMSAA） |
|---|---|---|---|---|
| `/spec-basic-design` | ① 基本设计 | requirements.md + 06/07/08/09 等 SSOT | `docs/基本設計/…`（更新模式） | ✅ 可用（文档已存在，主要用于更新与一致性检查） |
| `/spec-detail-design` | ② 详细设计 | 业务/画面设计 + `spec-detail-temp.md` | `docs/詳細設計/業務/<feature>/spec-<feature>-detail-sd.md` | ✅ 可用（需先补全 7 份简版） |
| `/spec-wf-flowchart` | ②' 流程图 | 要件 + 基本设计 + 详细设计 + BR | `spec-<feature>-flow.md`、`spec-business-issue.md`、`spec-<feature>-filelist.md` | ✅ 可用 |
| `/spec-scaffold-db` | ⓪ DB/Docker | 06 yaml、spec-db-connection、spec-01 | `docs/基本設計/データベース/setup/*.sql`、Compose 等 | ⚠ spec-01 §3 拓扑仍是占位，需先确认 |
| `/spec-scaffold-backend` | ⓪ 后端基盘 | `基盤/インタフェース仕様/*.yaml`、spec-01 §2.2 | `ddmsaa-backend/` 多模块骨架 | ✅ 可用（版本和接口规格已定） |
| `/spec-scaffold-frontend` | ⓪ 前端骨架 | spec-01 §2.1、frontend 规约、07/09 | `frontend/` 骨架 | ✅ 可用 |
| `/spec-scaffold-test` | ⓪ 测试基盘 | 真实后端框架、spec-test-rule | 测试基类、测试 profile、JWT 测试工具 | ⛔ 需先有 `ddmsaa-backend` 与建库 SQL |
| `/spec-scaffold-deploy` | ⓪' 容器化 | 后端模块、Compose | Dockerfile、app profile | ⛔ 需先有后端代码与 Compose；spec-01 §3 占位 |
| `/spec-db-migration` | DB 增量 | 06 yaml、迁移规约 | `db/migration/V*`、`R__*` | ⛔ 需先有迁移目录和 setup SQL；初始数据依据 spec-17（模板） |
| `/spec-sql-lint` | DB 检查 | SQL + lint 规约 | lint 报告、`.sqlfluff` | ⚠ 目前没有 SQL 可检查（可先做 init） |
| `/spec-wf-codegen` | ③ 后端代码 | 详细设计 + SSOT + `assets/` 模板 | `ddmsaa-backend/…/core/<feature>/` | ⛔ 需先有后端骨架；详细设计需补全 |
| `/spec-wf-testgen` | ④ 后端测试 | 详细设计测试清单 + 被测代码 | `src/test`、testlist、testreport | ⛔ 需先有代码和测试基盘 |
| `/spec-uttest-backend` | UT 规格 | 共通基盘类 | `docs/単体テスト/バックエンド/…` | ⚠ 可按规格起草；无代码可反查 |
| `/spec-uttest-backend-codegen` | UT 代码 | 共通基盘类 | JUnit 5/Mockito 测试 | ⛔ 需先有代码 |
| `/spec-uttest-frontend` | UT 规格 | 前端源码 | `docs/単体テスト/フロントエンド/…` | ⛔ 需先有前端代码 |
| `/spec-uttest-frontend-codegen` | UT 代码 | 前端源码 | Vitest 测试 + 报告 | ⛔ 需先有前端代码 |
| `/spec-it-api` | API IT | 07/06/09/03 + 运行中的系统 | 式样书（先审查）→ fixtures → 测试 → 报告 | ⚠ 式样书阶段可先做；其余需系统可运行 |
| `/spec-it-frontend` | 画面 IT | 画面设计 + 07/09 + 前端代码 | 式样书（先审查）→ MSW → 测试 → 报告 | ⛔ 需先有前端代码 |
| `/spec-frontend-basic-design` | 前端基本设计 | spec-05、画面设计、07/09 | 更新 `docs/基本設計/` 相应设计 | ⚠ DDMSAA 已用 spec-05/10/12/16 + 各 screen-ui 覆盖，原则上不另建体系 |
| `/spec-frontend-detail-design` | 前端详细设计 | 前端基本设计 + 脚手架 | 详细设计中的前端部分（`spec-detail-temp.md` §2） | ⛔ 需先有前端脚手架 |
| `/spec-frontend-codegen` | ③' 前端代码 | 画面设计 + 07/09 + 脚手架 | `frontend/src/…` | ⛔ 需先有前端脚手架 |

状态图例：✅ 前置已满足，可直接使用；⚠ 部分可用或需先确认；⛔ 前置未就绪，调用时会停止并说明先做什么。

## 3. 业务模块与出码顺序

按外键依赖，建议顺序（出码前以 `spec-06-db-physical.yaml` 的 FK 为准再核对一次）：

| 序 | feature | 服务 | 理由 |
|:--:|---|---|---|
| 1 | `user` | auth-service（`/auth/**`）+ business-service-core（用户管理） | 全局前提：登录、角色、停用与锁定；其他模块的操作者和数据范围依赖它 |
| 2 | `customer` | business-service-core | 顾客与担当割当；EMPLOYEE 的数据范围由担当割当决定 |
| 3 | `product` | business-service-core | 商品与税率；订单明细依赖 |
| 4 | `order` | business-service-core | 依赖 customer / product / user |
| 5 | `approval` | business-service-core | 依赖 order；含通知（`notification_logs`）与 BAT-01 |
| 6 | `attachment` | business-service-core | 依赖 order；扫描结果与保留期限批处理（BAT-04/05） |
| 7 | `report` | business-service-core | 读取订单汇总；CSV 作业（BAT-02/03） |

每个 feature 先做预检再出码；同一 feature 内的生成顺序：枚举 → Entity → Mapper → DTO → Converter → ServiceImpl → Controller → Batch（`spec-backend-coding-rule.md` §12）。

## 4. 共同约定（铁律）

1. **只参照本仓库**，文档都在 `docs/` 下；不引用其他项目或机器上的路径。
2. **只引用 SSOT，不重定义**。权威顺序：`spec-naming-rule` / `spec-database-rule` ＞ 各层编码规约 ＞ 详细设计 ＞ yaml 数据源。冲突时报告并向上取齐。
3. **机读数据源**：`spec-06-db-physical.yaml`（表）、`spec-07-api-list.yaml`（API）、`spec-09-message-list(.zh/.en).yaml`（消息）；业务规则编号只认 `spec-08-business-rule-list.md`；画面认 `spec-05-screen-list-ui.md`（SCR-01..21）。
4. **生成前后都过校验门**：引用能解析、YAML 能解析、三语消息一致、`devtool` 标记成对。
5. **human-in-the-loop**：遇业务决策停下询问，未决事项写入 TBD，不静默编造。
6. **不盲目覆盖**：已存在的产物先比较再按节合并。
7. **前置未满足就停止**并说明先运行哪个 skill（见 §5）。

## 5. DDMSAA 目前的前置缺口与建议顺序

缺口：
- 没有任何代码；没有 `docs/基本設計/データベース/setup/*.sql`、`db/migration/`、Compose/Dockerfile。
- `spec-01` §3–§6 仍有占位；`spec-14/17/18/04` 多为模板。
- 7 份详细设计较 `spec-detail-temp.md` 简略（缺 §0、§0.1、§1.1、§5.1 等）。
- 文档自身的不一致（已记入 `DDMSAA-PROJECT-CONTEXT.md` 末尾，按权威顺序处理）：UT 清单位置/编号（§5.1 与 §6、`UT-<FEATURE>-NNN` 与 `UT-ORD-01`）；`spec-codegen-map-rule.md` 的包路径和 Mapper 写法；Flyway 文档残留"14 表"及 §4.1-SEQ 的执行顺序；`devtool.config.yaml` 路径；`spec-project-structure.yaml` 缺 surefire/failsafe。

建议顺序：
1. 补全 spec-01 §3、各详细设计，统一 UT 清单位置与编号规则（先改文档，再让 skill 照做）。
2. `/spec-scaffold-backend` → `/spec-scaffold-db` → `/spec-sql-lint` → `/spec-scaffold-frontend` → `/spec-scaffold-test`。
3. 按 §3 的顺序逐个 feature：`/spec-detail-design` → `/spec-wf-codegen` → `/spec-wf-testgen`，前端对应 `/spec-frontend-codegen`。
4. 系统可运行后：`/spec-it-api`、`/spec-it-frontend`；上线后的结构变更走 `/spec-db-migration`。

## 6. 配套规约（`docs/開発規約/`）

| 文件 | 作用 |
|---|---|
| `spec-naming-rule.md` | 跨层命名唯一来源 |
| `spec-database-rule.md` | DB 命名/类型/约束（最高权威） |
| `spec-backend-coding-rule.md` / `spec-frontend-coding-rule.md` | 后端/前端架构、分层、异常、MyBatis、目录 |
| `spec-generation-rule.md` / `spec-codegen-map-rule.md` | 权威顺序、文件规约、出码索引 |
| `spec-test-rule.md` | 测试分层、ID、目录、报告 |
| `spec-flyway-migration-rule.md` / `spec-sql-lint-rule.md` / `spec-sqlfluff-rules-catalog.md` / `spec-postgresql-column-types-rule.md` | 迁移与 SQL 规约 |
| `spec-backend-scaffold-rule.md` | 后端脚手架规约 |
| `spec-data-dictionary-rule.md` / `spec-data-dictionary-project-temp.md` | 数据字典规范与 T07 枚举 |
| `spec-file-naming-rule.md` | 文件命名 |

## 7. 新增命令的约定

- 放 `.claude/skills/<name>/SKILL.md`，frontmatter 必含 `name` 和 `description`（写清何时使用）。
- 结构：适用与参数 → 前置检查 → 上下文组装（先读 `DDMSAA-PROJECT-CONTEXT.md`）→ 主体 → 校验门 → 边界。
- 落地后回到本 README 第 2 节登记一行，并更新流水线图。
