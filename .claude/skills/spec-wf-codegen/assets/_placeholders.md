# 占位符定义

出码前从**当前 feature 的详细设计**和 DDMSAA SSOT 解析每个占位符，不得沿用历史示例的值。无法解析时停止并报告缺少的设计决定。

| 占位符 | 解析来源 |
|---|---|
| `{{basePkg}}` | `com.example.ddmsaa.core`（business-service-core）；认证服务为 `com.example.ddmsaa.auth`（`spec-project-structure.yaml`） |
| `{{feature}}` | 业务目录名：`user` / `customer` / `product` / `order` / `approval` / `attachment` / `report`（小写、无分隔，见 coding-rule §1） |
| `{{Feature}}` | `{{feature}}` 的 PascalCase |
| `{{Entity}}` | 表的单数 PascalCase（`orders` → `Order`、`order_items` → `OrderItem`） |
| `{{table}}` / `{{pkColumn}}` | `spec-06-db-physical.yaml` 的 `physical_name` / 主键列（默认 `id`，`users` 为 `user_id`） |
| `{{pkField}}` / `{{pkType}}` | 主键列的 camelCase 字段名 / Java 类型（默认 `id` / `String`） |
| `{{EnumName}}` / `{{VALUE}}` | `spec-07-api-list.yaml` 的 enum schema 名与值；INT 码来自 `spec-data-dictionary-project-temp.md` 的 T07 |
| `{{SchemaName}}` | `spec-07-api-list.yaml` 的 schema 名（`...DTO` → Java `...Dto`） |
| `{{operationId}}` | `spec-07-api-list.yaml` 的 operationId，对应 Service 方法名 |
| `{{path}}` / `{{httpMethod}}` | `spec-07-api-list.yaml` 的 path（含 `/api/v1` 前缀由 servers.url 提供）与方法 |
| `{{role}}` | 该 operation 的 `x-roles`（`ADMIN` / `EMPLOYEE` / `CUSTOMER`） |
| `{{messageId}}` | `spec-09-message-list.yaml`（成功 I 类在 07 的 operation description 中给出） |
| `{{Bat}}` / `{{cron}}` | `spec-15-batch-design-ui.md` 的 BAT-xx 与执行时机 |

示例中的 Order / `orders` / `OrderStatus` 只是说明形状。
