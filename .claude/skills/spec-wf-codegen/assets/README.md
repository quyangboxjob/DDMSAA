# spec-wf-codegen 代码骨架模板（assets/）

> 本目录是 `/spec-wf-codegen` 出码时**逐层套用的代码骨架（样板）**。骨架依据 DDMSAA 的开发规约和基盘接口规格抽取：
> `docs/開発規約/spec-backend-coding-rule.md`、`docs/基本設計/基盤/インタフェース仕様/spec-10-business-core.yaml`（各层契约）、`spec-04-unified-response.yaml`、`spec-07-infrastructure.yaml`。
> DDMSAA **目前还没有任何后端代码**，没有可编译的参考实现；模板以规约和接口规格为准，出码前先确认 `ddmsaa-backend/` 脚手架已存在（见 SKILL.md）。

## SSOT 边界（铁律 · 防两处维护漂移）

| | 規約（`docs/開発規約/`） | 模板（本目录） |
|---|---|---|
| 管什么 | **规则 / Why·What**：命名、PK 策略、异常→HTTP、事务、messageId、权限 | **代码长相 / How**：骨架形状、占位符、本层 must-list |
| 改动触发 | 规则变 → 改規約 | 代码形状变 → 改模板 |
| 重复内容 | — | **零**：一律「见 規約 §X」，**绝不复述规则文字** |

模板与規約冲突时**以規約为准**，并报告模板需回写。

## 项目级裁定（来自 DDMSAA 規約，固化进模板）

| 层 | 裁定 | 出处 |
|---|---|---|
| Converter | **MapStruct**：`@Mapper(componentModel = "spring")` 接口，不手写实现 | coding-rule §2；spec-10-business-core.yaml §6 |
| Mapper XML | 开启 `map-underscore-to-camel-case`；**Entity 映射优先用 `resultMap` 明示**，JOIN/复合映射必须 `resultMap`（简单单表查询可自动映射） | coding-rule §6；spec-10-business-core.yaml §5 |
| Entity | 普通 POJO（getter/setter），不含业务方法；含审计列字段（`createdAt/createdBy/updatedAt/updatedBy`）供拦截器写入 | coding-rule §2、spec-07-infrastructure.yaml `MetaObjectHandler` |
| 审计列 | **Service 不手填**。`MetaObjectHandler`（MyBatis Interceptor）在 INSERT 时填 `created_*`/`updated_*`，UPDATE 时填 `updated_*`，取值 `UserContext.getUserId()`（空则 `SYSTEM`） | spec-07-infrastructure.yaml |
| 主键 | 默认 `VARCHAR(10)`，由 `next_business_id` 采番；`users` 例外（前缀 A/E/C + 4 位，应用层传前缀） | spec-database-rule §4.1 |
| 枚举 | DB 为 INT + `*_codes` 表；API 为 UPPER_SNAKE 字符串；Java enum 自带 `int code` + `fromCode`；Entity 用 `Integer` | spec-database-rule §4.1-CODE、T07 |
| 输入 DTO | `record` + Bean Validation，字段严格对齐 07 schema；类名 `<X>Dto` | coding-rule §10、§11 |
| 出参 DTO | 简单出参用 `record`；需按角色置 `null` 的详情出参用可变类 | spec-03-auth-ui.md §3、BR-APR-006 |
| 数据范围 | 在 ServiceImpl/SQL 强制（ADMIN 全件、EMPLOYEE 担当顾客、CUSTOMER 自社），范围外 404 | spec-03、BR-AUTH-003/006 |
| 乐观锁 | `WHERE … AND updated_at = #{expectedUpdatedAt}`，0 行 → `BusinessException("E0051")` | coding-rule §6 |
| 响应 | `Result<T>`（record）；`Result.ok(data, "I00NN", args…)`；分页 `PageResult.of(records, total, pageNum, pageSize)` | spec-04-unified-response.yaml |

## 模板清单（单 feature 内生成顺序 = coding-rule §12）

```
enum → entity → mapper.interface → mapper.xml → dto.input-record → dto.detail-class
     → converter → service.interface → service-impl → controller → batch(可选)
```

| 文件 | 层 | 落位 |
|---|---|---|
| `enum.java.tmpl` | 枚举 | `…/core/{{feature}}/dto/` |
| `entity.java.tmpl` | Entity | `…/core/{{feature}}/entity/` |
| `mapper.interface.tmpl` | Mapper 接口 | `…/core/{{feature}}/mapper/` |
| `mapper.xml.tmpl` | Mapper XML | `ddmsaa-backend/business-service-core/src/main/resources/mapper/` |
| `dto.input-record.tmpl` | 输入 DTO | `…/core/{{feature}}/dto/` |
| `dto.detail-class.tmpl` | 出参 DTO | `…/core/{{feature}}/dto/` |
| `converter.java.tmpl` | 转换 | `…/core/{{feature}}/convert/` |
| `service.interface.tmpl` | Service 接口 | `…/core/{{feature}}/service/` |
| `service-impl.java.tmpl` | ServiceImpl | `…/core/{{feature}}/service/impl/` |
| `controller.java.tmpl` | Controller | `…/core/{{feature}}/controller/` |
| `batch.java.tmpl` | 批处理（可选） | `…/core/{{feature}}/batch/` |

`…` = `ddmsaa-backend/business-service-core/src/main/java/com/example/ddmsaa`。认证相关（`/auth/**`）在 `auth-service`，包为 `com.example.ddmsaa.auth`，仅在设计明确要求时生成。占位符定义见 [`_placeholders.md`](_placeholders.md)。

## 每个模板的固定结构（4 段）

1. **适用层 & 何时套用**
2. **依据（SSOT 指针，只指不抄）**
3. **骨架（带占位符的代码）**
4. **本层硬约束（must / must-not）**

## 示例域

模板里的示例统一用 **Order（注文）**：表 `orders`（主键 `id` = OD+8 位）、枚举 `OrderStatus`（`order_status_codes`）、operationId `getOrder` / `updateOrder`。仅为说明形状；实际名称一律由当前 feature 的详细设计、`spec-06-db-physical.yaml`、`spec-07-api-list.yaml` 解析，不得照抄示例。

## 已决定事项（2026-10-21）

- Mapper：resultMap 优先（同时开启 underscore 映射）；`spec-codegen-map-rule.md` §3 已同步修改。
- 业务代码路径 `com.example.ddmsaa.core.<feature>`；`spec-codegen-map-rule.md` 已同步修改。
