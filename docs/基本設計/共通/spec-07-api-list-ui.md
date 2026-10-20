<!-- devtool:file-region id="document-header" -->
# 07 API 一覧・定義

<!-- rule: api.operation-id-identifier -->
> REST API の**人間可読な概要**。機械可読の SSOT は `spec-07-api-list.yaml`（OpenAPI 3.0。リクエスト/レスポンスの完全なスキーマを含む）であり、本 md ではスキーマを重複記載せず、共通規約とエンドポイント一覧のみを示して検索とトレーサビリティに供する。
> **API の正式な識別子 = `operationId`**（OpenAPI ネイティブで、コード生成がそのまま利用する）。**`API-0NN` 形式の採番は使用しない**。

（文書ヘッダは `spec-00-doc-standard-ui.md` を参照）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.common.07-api-list-ui.s01" -->
## 1. 共通規約

<!-- rule: api.base-path-v1 -->
<!-- rule: api.auth-jwt-bearer -->
<!-- rule: time.api-iso8601-offset -->
| 項目 | 内容 |
|------|------|
| ベースパス | `/api/v1` |
| データ形式 | JSON（UTF-8） |
| 認証 | JWT Bearer（`Authorization: Bearer <token>`。公開エンドポイントを除く） |
| 日付形式 | `+09:00` オフセット付き ISO 8601（`2026-06-10T10:00:00+09:00`）。`Z` / UTC 表記は用いない |
| 命名 | リソースパスは複数形・kebab（`/users`、`/approval-requests`）、**項目は camelCase**、列挙値は UPPER_SNAKE |
| 楽観ロック値 | `updatedAt` は秒未満を切り捨てず**マイクロ秒 6 桁**で返す（`2026-06-10T10:00:00.123456+09:00`）。クライアントは不透明な文字列として保持し、更新・状態変更の要求にそのまま返送する。不一致は 409（`E0051`）（BR-ORD-006） |
| 範囲外データ | 1 件の参照・操作は 404（`E0005`）、一覧・検索は自動除外（BR-AUTH-006） |
| 並び順 | 利用者が並び順を指定できるのは `pageOrders`（`sort`）だけ。その他の一覧は各 API の description に記載した固定順で返す |

### 共通レスポンスエンベロープ（yaml の `components.schemas` を正とする）

- 正常：`Result<T>` / ページング `PageResult<T>` —— `{ code, message, messageId, messageArgs[], traceId, timestamp, data }`
- 異常：`ErrorResponse` —— `{ messageId, messageArgs[], message, timestamp, path, traceId, details[] }`
<!-- rule: api.returns-message-id-only -->
- **表示文言は API から直接返さない**：API は `messageId`(+`messageArgs`) のみを返し、フロントエンドが `spec-09-message-list`（多言語時は `.ja/.zh/.en` ディレクトリ）に従って解決する。
<!-- rule: api.file-response-unwrapped -->
- ファイル系レスポンス（Excel/ZIP/画像）はバイナリと `Content-Disposition` をそのまま返し、Result で包まない。

<!-- rule: api.http-status-set -->
<!-- rule: api.status-independent-of-message-id -->
### HTTP ステータス（固定集合。messageId とは独立）

| コード | 意味 |
|----|------|
| 200 | 成功 |
| 400 | 入力チェックエラー |
| 401 | 未認証/認証失敗 |
| 403 | 権限不足 |
| 404 | リソースなし |
| 409 | 業務状態の競合（状態不可 `E0006`、楽観ロック不一致 `E0051` ほか） |
| 413 | ファイルサイズ超過（1 ファイル `E0010`、注文合計 `E0052`） |
| 429 | ロック・流量制限 |
| 500 / 502 / 504 | システム / 下流到達不可 / タイムアウト |

例外型ごとのステータスと既定 messageId は `開発規約/spec-backend-coding-rule.md` §4 に従う。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.07-api-list-ui.s02" -->
## 2. API 一覧（tag 単位でグループ化。operationId が正式）

> ロール：ADMIN / EMPLOYEE / CUSTOMER（`spec-03-auth-ui.md`）、公開＝認証不要。データ範囲（EMPLOYEE は担当顧客、CUSTOMER は自社）は全 API で Service 層が強制する。
> リクエスト・レスポンスの詳細は `spec-07-api-list.yaml` を正とする。`/auth/**` は auth-service、それ以外は business-service-core が提供する（`基盤/spec-00-architecture-ui.md`）。
> 一覧の `Page` は `pageNum`/`pageSize` による `PageResult`、`List` はページングなしの配列、`file` は Result で包まないファイル応答。

### auth — 認証（F-AUTH）— auth-service

| operationId | メソッド | パス | 概要 | F-ID | ロール | 応答 |
|------|------|------|------|------|------|------|
| loginInit | POST | `/auth/login-init` | ログイン開始（ユーザID → Magic Link 送信） | F-AUTH-01 | 公開 | Void |
| verifyMagicLink | POST | `/auth/magic-link/verify` | Magic Link 検証・JWT 発行 | F-AUTH-01 | 公開 | TokenResponse |
| resendMagicLink | POST | `/auth/magic-link/resend` | Magic Link 再送 | F-AUTH-01 | 公開 | Void |
| getMe | GET | `/me` | ログイン利用者情報 | F-AUTH-02 | ADMIN / EMPLOYEE / CUSTOMER | MeVO |

### users — ユーザ管理（F-USR）

| operationId | メソッド | パス | 概要 | F-ID | ロール | 応答 |
|------|------|------|------|------|------|------|
| pageUsers | GET | `/users` | ユーザ一覧 | F-USR-01 | ADMIN | Page<UserVO> |
| createUser | POST | `/users` | ユーザ登録・採番（ロールに応じ A/E/C） | F-USR-02 | ADMIN | ID |
| getUser | GET | `/users/{userId}` | ユーザ詳細 | F-USR-03 | ADMIN | UserVO |
| updateUser | PUT | `/users/{userId}` | ユーザ更新 | F-USR-03 | ADMIN | Void |
| disableUser | POST | `/users/{userId}/disable` | 停用 | F-USR-04 | ADMIN | Void |
| enableUser | POST | `/users/{userId}/enable` | 停用解除 | F-USR-04 | ADMIN | Void |
| lockUser | POST | `/users/{userId}/lock` | ロック（system_locks） | F-USR-05 | ADMIN | Void |
| unlockUser | POST | `/users/{userId}/unlock` | ロック解除 | F-USR-05 | ADMIN | Void |

### customers — 顧客管理（F-CUS）

| operationId | メソッド | パス | 概要 | F-ID | ロール | 応答 |
|------|------|------|------|------|------|------|
| pageCustomers | GET | `/customers` | 顧客一覧（EMPLOYEE は担当顧客のみ） | F-CUS-01 | ADMIN / EMPLOYEE | Page<CustomerSummaryVO> |
| createCustomer | POST | `/customers` | 顧客登録（連絡先・担当社員を含む） | F-CUS-03 | ADMIN | ID |
| getCustomer | GET | `/customers/{customerId}` | 顧客詳細（CUSTOMER は自社のみ。担当社員は返さない） | F-CUS-02 | ADMIN / EMPLOYEE / CUSTOMER | CustomerVO |
| updateCustomer | PUT | `/customers/{customerId}` | 顧客更新（連絡先・担当社員を全件置換） | F-CUS-04 | ADMIN | Void |
| activateCustomer | POST | `/customers/{customerId}/activate` | 顧客を有効化 | F-CUS-05 | ADMIN | Void |
| deactivateCustomer | POST | `/customers/{customerId}/deactivate` | 顧客を無効化（新規注文の対象外） | F-CUS-05 | ADMIN | Void |

### products — 商品管理（F-PRD）

| operationId | メソッド | パス | 概要 | F-ID | ロール | 応答 |
|------|------|------|------|------|------|------|
| pageProducts | GET | `/products` | 商品一覧 | F-PRD-01 | ADMIN / EMPLOYEE | Page<ProductVO> |
| createProduct | POST | `/products` | 商品登録 | F-PRD-03 | ADMIN | ID |
| getProduct | GET | `/products/{productId}` | 商品詳細 | F-PRD-02 | ADMIN / EMPLOYEE | ProductVO |
| updateProduct | PUT | `/products/{productId}` | 商品更新 | F-PRD-04 | ADMIN | Void |
| activateProduct | POST | `/products/{productId}/activate` | 商品を有効化 | F-PRD-05 | ADMIN | Void |
| deactivateProduct | POST | `/products/{productId}/deactivate` | 商品を無効化（新規注文で選択不可） | F-PRD-05 | ADMIN | Void |
| listTaxRates | GET | `/tax-rates` | 税率一覧 | F-PRD-01 | ADMIN / EMPLOYEE | List<TaxRateVO> |

### orders — 注文管理（F-ORD）

| operationId | メソッド | パス | 概要 | F-ID | ロール | 応答 |
|------|------|------|------|------|------|------|
| pageOrders | GET | `/orders` | 注文検索（データ範囲で絞り込み） | F-ORD-01 | ADMIN / EMPLOYEE / CUSTOMER | Page<OrderSummaryVO> |
| createOrder | POST | `/orders` | 下書き注文登録（単価は商品から複写、金額はサーバー再計算） | F-ORD-02 | ADMIN / EMPLOYEE | OrderVO |
| getOrder | GET | `/orders/{orderId}` | 注文詳細（明細・状態履歴・現在の承認依頼ID・承認可否 `canApprove` を含む） | F-ORD-01 | ADMIN / EMPLOYEE / CUSTOMER | OrderVO |
| updateOrder | PUT | `/orders/{orderId}` | 注文更新（DRAFT / RETURNED のみ。明細を再複写・再計算） | F-ORD-03 | ADMIN / EMPLOYEE | OrderVO |
| submitOrder | POST | `/orders/{orderId}/submit` | 承認依頼（申請・再申請。申請時検証 BR-ORD-010） | F-ORD-04 | ADMIN / EMPLOYEE | Void |
| cancelOrder | POST | `/orders/{orderId}/cancel` | 注文取消し（APPROVED は ADMIN のみ。BR-ORD-007） | F-ORD-05 | ADMIN / EMPLOYEE | Void |
| listApprovalHistories | GET | `/orders/{orderId}/approval-histories` | 承認履歴（CUSTOMER には操作者・理由を返さない） | F-ORD-01 | ADMIN / EMPLOYEE / CUSTOMER | List<ApprovalHistoryVO> |

### approvals — 承認（F-APR）

| operationId | メソッド | パス | 概要 | F-ID | ロール | 応答 |
|------|------|------|------|------|------|------|
| pageApprovalRequests | GET | `/approval-requests` | 承認待ち一覧（有効な ADMIN 全員が参照） | F-APR-01 | ADMIN | Page<ApprovalRequestVO> |
| approveApprovalRequest | POST | `/approval-requests/{approvalRequestId}/approve` | 承認（申請者以外の ADMIN。BR-APR-002） | F-APR-02 | ADMIN | Void |
| returnApprovalRequest | POST | `/approval-requests/{approvalRequestId}/return` | 差戻し（理由必須。申請者以外の ADMIN） | F-APR-02 | ADMIN | Void |
| listApprovalRules | GET | `/approval-rules` | 承認ルール一覧 | F-APR-03 | ADMIN | List<ApprovalRuleVO> |
| createApprovalRule | POST | `/approval-rules` | 承認ルール登録 | F-APR-03 | ADMIN | ID |
| updateApprovalRule | PUT | `/approval-rules/{approvalRuleId}` | 承認ルール更新（申請済み注文には遡及しない） | F-APR-03 | ADMIN | Void |

### attachments — 添付（F-ATT）

| operationId | メソッド | パス | 概要 | F-ID | ロール | 応答 |
|------|------|------|------|------|------|------|
| listOrderAttachments | GET | `/orders/{orderId}/attachments` | 添付一覧（CUSTOMER は公開かつ SAFE のみ） | F-ATT-01 | ADMIN / EMPLOYEE / CUSTOMER | List<AttachmentVO> |
| uploadOrderAttachment | POST | `/orders/{orderId}/attachments` | 添付アップロード（PENDING で登録） | F-ATT-02 | ADMIN / EMPLOYEE | ID |
| downloadAttachment | GET | `/attachments/{attachmentId}/download` | 添付ダウンロード（SAFE のみ・再認可） | F-ATT-03 | ADMIN / EMPLOYEE / CUSTOMER | file |
| deleteAttachment | DELETE | `/attachments/{attachmentId}` | 添付削除（論理削除） | F-ATT-04 | ADMIN / EMPLOYEE | Void |

### reports — 帳票・集計・CSV（F-RPT）

| operationId | メソッド | パス | 概要 | F-ID | ロール | 応答 |
|------|------|------|------|------|------|------|
| downloadOrderPdf | GET | `/orders/{orderId}/pdf` | 注文書 PDF（APPROVED のみ） | F-RPT-01 | ADMIN / EMPLOYEE / CUSTOMER | file |
| getOrderSummary | GET | `/reports/order-summary` | 注文集計（月別） | F-RPT-02 | ADMIN / EMPLOYEE | List<OrderSummaryReportVO> |
| getCustomerSummary | GET | `/reports/customer-summary` | 顧客別集計 | F-RPT-02 | ADMIN / EMPLOYEE | List<CustomerSummaryReportVO> |
| getProductSummary | GET | `/reports/product-summary` | 商品別集計 | F-RPT-02 | ADMIN / EMPLOYEE | List<ProductSummaryReportVO> |
| createExportJob | POST | `/export-jobs` | CSV 出力依頼（すべて記録。10,000 件以下は同期生成で READY、10,001 件以上は QUEUED） | F-RPT-03 | ADMIN / EMPLOYEE | ExportJobVO |
| pageExportJobs | GET | `/export-jobs` | 出力ジョブ一覧（ADMIN は全員分、EMPLOYEE は自分の分） | F-RPT-03 | ADMIN / EMPLOYEE | Page<ExportJobVO> |
| getExportJob | GET | `/export-jobs/{exportJobId}` | 出力ジョブ状態 | F-RPT-03 | ADMIN / EMPLOYEE | ExportJobVO |
| downloadExportJob | GET | `/export-jobs/{exportJobId}/download` | CSV ダウンロード（UTF-8 BOM・READY のみ） | F-RPT-03 | ADMIN / EMPLOYEE | file |

### operations — 通知履歴・監査（F-NTF / F-AUD）

| operationId | メソッド | パス | 概要 | F-ID | ロール | 応答 |
|------|------|------|------|------|------|------|
| pageNotificationLogs | GET | `/notification-logs` | 通知履歴 | F-NTF-01 | ADMIN | Page<NotificationLogVO> |
| retryNotification | POST | `/notification-logs/{notificationLogId}/retry` | 失敗通知の再送 | F-NTF-01 | ADMIN | Void |
| pageAuditLogs | GET | `/audit-logs` | 監査ログ | F-AUD-01 | ADMIN | Page<AuditLogVO> |

### 2.1 旧草案からの主な変更

| 旧草案（`21a123a`） | 新基線 | 理由 |
|---|---|---|
| `login`（ID+パスワード）/ `logout` / `changePassword` | `loginInit` / `verifyMagicLink` / `resendMagicLink`。ログアウトはクライアントのトークン破棄 | パスワードレス Magic Link・JWT（`基盤/spec-02-auth-service-ui.md`） |
| `list*`（一覧） | `page*`（ページング）/ `list*`（小件数の配列） | 命名規約・`PageResult` |
| `PATCH` 更新、`/status` で状態変更 | `PUT` 更新、`/activate`・`/deactivate`・`/disable`・`/lock` 等の明示アクション | `基盤/spec-00-architecture-ui.md` §3（`/{id}/{action}`） |
| `201` / `202` / `204` | すべて `200`（Result） | 同上 |
| `approveOrder` / `returnOrder` | `approveApprovalRequest` / `returnApprovalRequest` | 操作対象リソース（承認依頼）に合わせた命名 |
| `createOrderExport`（/exports） | `createExportJob`（/export-jobs。全件を記録し、10,000 件以下は同期生成） | リソース名を DB と一致 |
| `downloadOrderDocument`（/document） | `downloadOrderPdf`（/pdf） | 名称の明確化 |
| `listNotifications` / `listAuditLogs` | `pageNotificationLogs` / `pageAuditLogs` | ページング・リソース名 |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.07-api-list-ui.s03" -->
## 3. API 定義テンプレート（必要に応じてエンドポイントごとに記載。詳細スキーマは yaml を正とする）

> 通常、**リクエスト/レスポンスの項目は `spec-07-api-list.yaml` を唯一の出典**とし、本 md では重複記載しない。人が読むための補足説明が必要な場合のみ、下記の雛形を記入する。

### 〇〇（`METHOD /path`）

| 項目 | 内容 |
|------|------|
| operationId | `〇〇`（camelCase の動詞。yaml を参照） |
| 機能 | F-〇〇 |
| メソッド・パス | `POST /xxx`（リソースは複数形 kebab） |
| 認証・権限 | JWT。ロール 〇〇（`spec-03-auth-ui.md` を参照） |
| リクエストスキーマ | `〇〇DTO`（camelCase の項目。yaml を参照） |
| レスポンス | `Result<〇〇>` / `Result<Void>`（成功時 messageId `I00NN`） |

**入力パラメータのチェック**

| 項目 | ルール | 違反時 messageId | HTTP |
|------|------|--------------|------|
| `〇〇`（camelCase） | 必須/形式/長さ（OpenAPI の制約に合わせる） | `E00NN` | 400 |

**エラー（例外→HTTP の固定セット。文言は 09 を参照）**

| ステータスコード | messageId | 発生条件 |
|--------|-----------|---------|
| 400 | `E00NN` | チェックエラー |
| 403 | `E00NN` | ロール/範囲の不足 |
| 404 | `E00NN` | リソースなし |
| 409 | `E00NN` | 状態の競合 |

### 記入要領

<!-- rule: api.operation-id-camel-verb -->
- **operationId は API の唯一の正式識別子**（camelCase の動詞。例：`createUser`）。詳細なスキーマ/example は一律 `spec-07-api-list.yaml` を参照し、本 md では重複記載しない（二重管理による乖離を避ける）。
- 1つの機能が複数のエンドポイント（CRUD 分割）に対応する場合があるため、対応する operationId をすべて列挙する。
<!-- rule: api.paging-params -->
- 一覧系 API はページングパラメータ（`pageNum`/`pageSize`。既定 1/20、`pageSize` 上限 100、超過は `400`）を統一し、`PageResult<T>` でラップする。
- 権限にはロールだけでなく、必要に応じて「本人のみ/自部門」などの行単位ルールも記載する（`spec-03-auth-ui.md` を参照）。
- 項目は一律 camelCase、列挙値は UPPER_SNAKE とする（`spec-naming-rule.md` を参照）。表示文言には messageId のみを用いる（`09` を参照）。
<!-- devtool:endblock -->
