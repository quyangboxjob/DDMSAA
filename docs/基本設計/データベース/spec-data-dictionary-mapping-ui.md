<!-- devtool:file-region id="document-header" -->
# データ辞書：命名マッピングと型規約

> 全テーブルの項目（列）について、論理名 → DB 物理名 → Backend/API 名 → 型の対応を整理し、バックエンド/API 実装の命名を統一する。
> 項目の定義（DI-xxx）の正は [`spec-13-data-dictionary-ui.md`](../共通/spec-13-data-dictionary-ui.md)、物理名・テーブル構成の正は [`spec-06-db-physical-ui.md`](../共通/spec-06-db-physical-ui.md)。
> 命名規約は [`spec-naming-rule.md`](../../開発規約/spec-naming-rule.md) と [`spec-database-rule.md`](../../開発規約/spec-database-rule.md) に従う。

（文書ヘッダは `spec-00-doc-standard-ui.md` を参照）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.database.postgresql.data-dictionary-mapping-ui.s01" -->
## 1. 命名規則

| 層 | スタイル | 例（`order_date` の場合） |
|---|---|---|
| DB 物理名 | snake_case | `order_date` |
| Backend（Entity/DTO のフィールド、変数） | camelCase | `orderDate` |
| API（JSON のリクエスト/レスポンスキー、クエリパラメータ） | camelCase（Backend と同一） | `"orderDate"` |

- **機械的変換**：Backend フィールド名 = API JSON キー = DB 物理名からアンダースコアを除いた camelCase。
- **日時**：DB は `timestamptz`。API は `+09:00` オフセット付き ISO 8601 文字列（`"2026-01-01T00:00:00+09:00"`）。日付のみは `"2026-01-01"`。
- **楽観ロック値 `updatedAt`**：秒未満を切り捨てずマイクロ秒 6 桁で返す（`"2026-06-10T10:00:00.123456+09:00"`）。クライアントは不透明な文字列として保持し、更新・状態変更の要求にそのまま返送する（`spec-07-api-list-ui.md` §1）。
- **真偽値**：DB 列名 `is_xxx` → API `isXxx`（`boolean`）。
- **列挙**：DB は INT コード（T07）→ Java `Enum` → API 値は UPPER_SNAKE_CASE の文字列。値の定義は `開発規約/spec-data-dictionary-project-temp.md` の T07 を正とする。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.data-dictionary-mapping-ui.s02" -->
## 2. 型マッピング

| 基礎型/用途 | DB 型 | Java 型 | API JSON 型 | 備考 |
|---|---|---|---|---|
| 業務キー（文字列） | `varchar(n)` | `String` | string | 採番規則は `spec-00-doc-standard-ui.md` を参照 |
| 数値（整数） | `integer` / `bigint` | `Integer` / `Long` | integer | |
| 金額・小数 | `numeric(p,s)` | `BigDecimal` | number |
| 件数・番号・バイト数（スケールなし） | `numeric`（`spec-database-rule.md` §5「数量·件数」） | `Long` | integer（int64） | 行番号・承認段階・ファイルサイズ・試行回数・件数。小数を持たないため API は integer とする | 丸め規則は `spec-database-rule.md` を参照 |
| 真偽値 | `boolean` | `Boolean` | boolean | |
| 日付 | `date` | `LocalDate` | string(date) | |
| 日時 | `timestamptz` | `OffsetDateTime`（または `Instant`） | string(date-time) | `+09:00` オフセット付き。表示層の共通ユーティリティで書式化 |
| 列挙（T07） | `integer` + コードテーブル FK | `Enum` | string | DB コード ↔ API 値は T07 に従う |
| 長文 | `text` | `String` | string | |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.data-dictionary-mapping-ui.s03" -->
## 3. 共通項目（全テーブル共通）

| 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | 備考 |
|---|---|---|---|---|
| 主キー | `id`（`users` は `user_id`） | `id`（`users` は `userId`） | string | 採番（prefix+連番）。テーブルごとの方針は `spec-06-db-physical-ui.md` を参照 |
| 作成日時 | `created_at` | `createdAt` | string(date-time) | ISO 8601（`+09:00`） |
| 作成者 | `created_by` | `createdBy` | string | 操作者の業務 ID。システム処理は `SYSTEM` |
| 更新日時 | `updated_at` | `updatedAt` | string(date-time) | ISO 8601（`+09:00`） |
| 更新者 | `updated_by` | `updatedBy` | string | |
| 削除日時 | `deleted_at` | `deletedAt` | string(date-time) \| null | 論理削除。通常は API で公開しない |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.data-dictionary-mapping-ui.s04" -->
## 4. テーブル別マッピング

> テーブルごとに節を立てて記載する。DI-xxx は `spec-13-data-dictionary-ui.md` の項目 ID と一致させる。共通列（§3）は省略する。
> 列挙（INT）は API では T07 の英語名（UPPER_SNAKE）を返す。API の項目名が DB と異なるものは備考に記す。

### 4.1 users（ユーザ）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-037 | ユーザID | `user_id` | `userId` | string | varchar(10) |  |
| DI-038 | 表示名 | `user_name` | `userName` | string | varchar(100) |  |
| DI-039 | ロール | `role` | `role` | string（列挙） | int | T07-1 ロール |
| DI-040 | 状態 | `status` | `status` | string（列挙） | int | T07-2 利用者状態 |
| DI-041 | 顧客ID | `customer_id` | `customerId` | string | varchar(10) |  |

### 4.2 user_contacts（ユーザ連絡先）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-042 | ユーザID | `user_id` | `userId` | string | varchar(10) |  |
| DI-043 | メールアドレス | `email` | `email` | string | varchar(255) |  |

### 4.3 system_locks（システムロック）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-044 | ユーザID | `user_id` | `userId` | string | varchar(10) |  |
| DI-045 | ロック開始日時 | `lock_started_at` | `lockStartedAt` | string(date-time) | timestamptz |  |
| DI-046 | ロック理由 | `lock_reason` | `lockReason` | string | varchar(500) |  |

### 4.4 customers（顧客）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-047 | 顧客ID | `id` | `id` | string | varchar(10) |  |
| DI-048 | 顧客コード | `customer_code` | `customerCode` | string | varchar(30) |  |
| DI-049 | 顧客名 | `customer_name` | `customerName` | string | varchar(200) |  |
| DI-050 | メールアドレス | `email` | `email` | string | varchar(255) |  |
| DI-051 | 電話番号 | `phone_number` | `phoneNumber` | string | varchar(50) |  |
| DI-052 | 状態 | `status` | `status` | string（列挙） | int | T07-3 顧客状態 |

### 4.5 customer_contacts（顧客連絡先）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-053 | 顧客連絡先ID | `id` | `id` | string | varchar(10) |  |
| DI-054 | 顧客ID | `customer_id` | `customerId` | string | varchar(10) |  |
| DI-055 | 連絡先名 | `contact_name` | `contactName` | string | varchar(100) |  |
| DI-056 | メールアドレス | `email` | `email` | string | varchar(255) |  |
| DI-057 | 通知先フラグ | `is_notification_recipient` | `isNotificationRecipient` | boolean | boolean |  |
| DI-058 | 有効フラグ | `is_active` | `isActive` | boolean | boolean |  |

### 4.6 customer_assignments（担当割当）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-059 | 担当割当ID | `id` | `id` | string | varchar(10) |  |
| DI-060 | 顧客ID | `customer_id` | `customerId` | string | varchar(10) |  |
| DI-061 | ユーザID | `user_id` | `userId` | string | varchar(10) |  |
| DI-062 | 有効フラグ | `is_active` | `isActive` | boolean | boolean |  |
| DI-063 | 割当日 | `assigned_date` | `assignedDate` | string(date) | date |  |

### 4.7 products（商品）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-064 | 商品ID | `id` | `id` | string | varchar(10) |  |
| DI-065 | 商品コード | `product_code` | `productCode` | string | varchar(50) |  |
| DI-066 | 商品名 | `product_name` | `productName` | string | varchar(200) |  |
| DI-067 | 商品カテゴリ | `product_category` | `productCategory` | string | varchar(100) |  |
| DI-068 | 標準単価 | `standard_unit_price` | `standardUnitPrice` | number | numeric(15,2) |  |
| DI-069 | 税区分 | `tax_category` | `taxCategory` | string（列挙） | int | T07-5 税区分 |
| DI-070 | 状態 | `status` | `status` | string（列挙） | int | T07-4 商品状態 |

### 4.8 tax_rates（税率）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-071 | 税区分 | `tax_category` | `taxCategory` | string（列挙） | int | T07-5 税区分 |
| DI-072 | 税率 | `tax_rate` | `taxRate` | number | numeric(5,2) |  |

### 4.9 orders（注文）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-073 | 注文ID（注文番号） | `id` | `id` | string | varchar(10) | 注文番号として表示 |
| DI-074 | 顧客ID | `customer_id` | `customerId` | string | varchar(10) |  |
| DI-075 | 担当者ユーザID | `sales_user_id` | `salesUserId` | string | varchar(10) |  |
| DI-076 | 注文日 | `order_date` | `orderDate` | string(date) | date |  |
| DI-077 | 状態 | `status` | `status` | string（列挙） | int | T07-6 注文状態 |
| DI-078 | 小計 | `subtotal_amount` | `subtotalAmount` | number | numeric(15,2) |  |
| DI-079 | 税額 | `tax_amount` | `taxAmount` | number | numeric(15,2) |  |
| DI-080 | 合計 | `total_amount` | `totalAmount` | number | numeric(15,2) |  |
| DI-081 | 訂正元注文ID | `revised_from_order_id` | `revisedFromOrderId` | string | varchar(10) |  |
| DI-082 | 取消し理由 | `cancel_reason` | `cancelReason` | string | varchar(500) |  |

### 4.10 order_items（注文明細）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-083 | 注文明細ID | `id` | `id` | string | varchar(10) |  |
| DI-084 | 注文ID | `order_id` | `orderId` | string | varchar(10) |  |
| DI-085 | 行番号 | `line_number` | `lineNumber` | integer | numeric |  |
| DI-086 | 商品ID | `product_id` | `productId` | string | varchar(10) |  |
| DI-087 | 商品コード（複写） | `product_code` | `productCode` | string | varchar(50) |  |
| DI-088 | 商品名（複写） | `product_name` | `productName` | string | varchar(200) |  |
| DI-089 | 数量 | `quantity` | `quantity` | number | numeric(15,3) |  |
| DI-090 | 単価 | `unit_price` | `unitPrice` | number | numeric(15,2) |  |
| DI-091 | 税区分（複写） | `tax_category` | `taxCategory` | string（列挙） | int | T07-5 税区分 |
| DI-092 | 税率（複写） | `tax_rate` | `taxRate` | number | numeric(5,2) |  |
| DI-093 | 明細金額 | `line_amount` | `lineAmount` | number | numeric(15,2) |  |
| DI-094 | 明細税額 | `tax_amount` | `taxAmount` | number | numeric(15,2) |  |

### 4.11 order_status_histories（注文状態履歴）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-095 | 注文状態履歴ID | `id` | `id` | string | varchar(10) |  |
| DI-096 | 注文ID | `order_id` | `orderId` | string | varchar(10) |  |
| DI-097 | 変更前状態 | `from_status` | `fromStatus` | string（列挙） | int | T07-6 注文状態。API `fromStatus` |
| DI-098 | 変更後状態 | `to_status` | `toStatus` | string（列挙） | int | T07-6 注文状態 |
| DI-099 | 理由 | `reason` | `reason` | string | varchar(500) |  |

### 4.12 approval_rules（承認ルール）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-100 | 承認ルールID | `id` | `id` | string | varchar(10) |  |
| DI-101 | 金額下限 | `min_amount` | `minAmount` | number | numeric(15,2) |  |
| DI-102 | 金額上限 | `max_amount` | `maxAmount` | number | numeric(15,2) |  |
| DI-103 | 承認段階 | `approval_step` | `approvalStep` | integer | numeric |  |
| DI-104 | 承認者ロール | `approver_role` | `approverRole` | string（列挙） | int | T07-1 ロール |
| DI-106 | 有効フラグ | `is_active` | `isActive` | boolean | boolean |  |
| DI-107 | 適用開始日 | `effective_from_date` | `effectiveFromDate` | string(date) | date |  |
| DI-108 | 適用終了日 | `effective_to_date` | `effectiveToDate` | string(date) | date |  |

> DI-105（承認ルールの承認者ユーザID）は欠番。承認者は特定せず、有効な ADMIN のうち申請者以外が承認する（BR-APR-002）。

### 4.13 approval_requests（承認依頼）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-109 | 承認依頼ID | `id` | `id` | string | varchar(10) |  |
| DI-110 | 注文ID | `order_id` | `orderId` | string | varchar(10) |  |
| DI-111 | 承認段階 | `approval_step` | `approvalStep` | integer | numeric |  |
| DI-112 | 承認者ユーザID | `approver_user_id` | `approverUserId` | string | varchar(10) | 実際に承認・差戻しした ADMIN。申請時は null |
| DI-113 | ルールスナップショット | `rule_snapshot` | `ruleSnapshot` | object | jsonb |  |
| DI-114 | 状態 | `status` | `status` | string（列挙） | int | T07-7 承認依頼状態 |
| DI-115 | 処理日時 | `acted_at` | `actedAt` | string(date-time) | timestamptz |  |

### 4.14 approval_histories（承認履歴）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-116 | 承認履歴ID | `id` | `id` | string | varchar(10) |  |
| DI-117 | 承認依頼ID | `approval_request_id` | `approvalRequestId` | string | varchar(10) |  |
| DI-118 | 操作 | `approval_action` | `approvalAction` | string（列挙） | int | T07-8 承認操作 |
| DI-119 | 理由 | `reason` | `reason` | string | varchar(500) |  |

### 4.15 order_attachments（注文添付）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-120 | 添付ID | `id` | `id` | string | varchar(10) |  |
| DI-121 | 注文ID | `order_id` | `orderId` | string | varchar(10) |  |
| DI-122 | 元ファイル名 | `original_file_name` | `originalFileName` | string | varchar(255) |  |
| DI-123 | 保存パス | `storage_path` | `storagePath` | string | varchar(512) | API では返さない |
| DI-124 | コンテンツタイプ | `content_type` | `contentType` | string | varchar(100) |  |
| DI-125 | ファイルサイズ | `file_size` | `fileSize` | integer | numeric | int64 |
| DI-126 | 公開範囲 | `visibility` | `visibility` | string（列挙） | int | T07-9 添付公開範囲 |
| DI-127 | スキャン状態 | `scan_status` | `scanStatus` | string（列挙） | int | T07-10 スキャン状態 |
| DI-154 | ファイル削除日時 | `storage_purged_at` | — | — | timestamptz | API に出さない（バッチ管理用） |

### 4.16 notification_logs（通知ログ）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-128 | 通知ID | `id` | `id` | string | varchar(10) |  |
| DI-129 | 通知イベント | `notification_event` | `notificationEvent` | string（列挙） | int | T07-11 通知イベント |
| DI-130 | 注文ID | `order_id` | `orderId` | string | varchar(10) |  |
| DI-131 | 宛先メール | `recipient_email` | `recipientEmail` | string | varchar(255) |  |
| DI-132 | 状態 | `status` | `status` | string（列挙） | int | T07-12 通知状態 |
| DI-153 | 出力ジョブID | `export_job_id` | `exportJobId` | string | varchar(10) |  |
| DI-133 | 試行回数 | `attempt_count` | `attemptCount` | integer | numeric | 初回を含む |
| DI-152 | 次回送信日時 | `next_attempt_at` | `nextAttemptAt` | string(date-time) | timestamptz |  |
| DI-134 | エラー内容 | `error_message` | `errorMessage` | string | text |  |
| DI-135 | 送信日時 | `sent_at` | `sentAt` | string(date-time) | timestamptz |  |

### 4.17 export_jobs（CSV 出力ジョブ）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-136 | 出力ジョブID | `id` | `id` | string | varchar(10) |  |
| DI-137 | 抽出条件 | `filter_condition` | `filterCondition` | object | jsonb |  |
| DI-138 | 状態 | `status` | `status` | string（列挙） | int | T07-13 出力状態 |
| DI-139 | 保存パス | `storage_path` | `storagePath` | string | varchar(512) | API では返さない |
| DI-140 | 件数 | `record_count` | `recordCount` | integer | numeric |  |
| DI-141 | 有効期限 | `expires_at` | `expiresAt` | string(date-time) | timestamptz |  |
| DI-142 | エラー内容 | `error_message` | `errorMessage` | string | text |  |
| DI-143 | 完了日時 | `completed_at` | `completedAt` | string(date-time) | timestamptz |  |
| DI-155 | ファイル削除日時 | `storage_purged_at` | — | — | timestamptz | API に出さない（バッチ管理用） |

### 4.18 audit_logs（監査ログ）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-144 | 監査ログID | `id` | `id` | string | varchar(10) |  |
| DI-145 | 操作者ユーザID | `actor_user_id` | `actorUserId` | string | varchar(10) |  |
| DI-146 | 操作 | `audit_action` | `auditAction` | string | varchar(100) |  |
| DI-147 | 対象種別 | `target_type` | `targetType` | string | varchar(100) |  |
| DI-148 | 対象ID | `target_id` | `targetId` | string | varchar(100) |  |
| DI-149 | トレースID | `trace_id` | `traceId` | string | varchar(100) |  |
| DI-150 | IP アドレス | `ip_address` | `ipAddress` | string | varchar(45) |  |
| DI-151 | 監査詳細 | `audit_detail` | `auditDetail` | object | jsonb |  |

### 4.19 列挙コードテーブル（`*_codes`）

各表とも `{enum}_code`（INT・PK）→ Backend の Enum、`{enum}_name`（varchar(50)）→ API 値（UPPER_SNAKE）。値は T07 を正とする。

### 4.20 派生項目（API のみ）

| API 項目 | 由来 | 使用 |
|---|---|---|
| `changedBy / changedAt` | `order_status_histories.created_by / created_at` | OrderStatusHistoryVO |
| `actedBy / actedAt` | `approval_histories.created_by / created_at` | ApprovalHistoryVO |
| `requestedBy / requestedAt` | `approval_requests.created_by / created_at` | ApprovalRequestVO |
| `customerName / totalAmount（ApprovalRequestVO）` | `orders` と `customers` | ApprovalRequestVO |
| `canApprove` | ログイン者が ADMIN かつ PENDING かつ申請者でない（BR-APR-002） | OrderVO / ApprovalRequestVO |
| `requestedBy / requestedByName（ExportJobVO）` | `export_jobs.created_by` と `users.user_name` | ExportJobVO |
| `userId / userName / assignedDate（AssignedEmployeeVO）` | `customer_assignments` と `users` | CustomerVO.assignedEmployees |
| `orderCount / subtotalAmount / taxAmount / totalAmount / quantity / lineAmount（集計 VO）` | `orders` / `order_items` の集計 | OrderSummaryReportVO / CustomerSummaryReportVO / ProductSummaryReportVO |
| `uploadedBy / uploadedAt` | `order_attachments.created_by / created_at` | AttachmentVO |
| `occurredAt` | `audit_logs.created_at` | AuditLogVO |
| `email（UserVO）` | `user_contacts.email` | UserVO / UserCreateDTO |
| `isLocked / lockStartedAt / lockReason` | `system_locks` | UserVO |
| `xxxName（customerName、salesUserName 等）` | 参照先テーブルの名称列 | 各 VO |
| `currentApprovalRequestId` | PENDING の `approval_requests.id` | OrderVO |
| `taxRate（ProductVO）` | `tax_rates.tax_rate` | ProductVO |

<!-- devtool:endblock -->
