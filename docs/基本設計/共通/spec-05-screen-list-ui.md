<!-- devtool:file-region id="document-header" -->
# 05 画面一覧

> システムの**全画面の索引**。画面 ID（`SCR-`）はここで採番する。各画面の項目・レイアウトは `spec-16-screen-design-ui.md`、遷移は `spec-10-screen-transition-ui.md`、共通 UI 方針は `spec-12-screen-common-ui.md` を参照し、本書では再定義しない。
> **帰属：システム全体で唯一 → `共通/`**。

（文書ヘッダは `spec-00-doc-standard-ui.md` を参照）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.common.05-screen-list-ui.s01" -->
## 1. 画面一覧

> ID 形式は `spec-00-doc-standard-ui.md` §3（`SCR-99`）に従う。廃止画面は行を残しステータスを「廃止」とする。
> 関連機能 ID は `spec-02-function-list-ui.md` の機能 ID（`F-xxx`）を記載する（旧 TBD-SCR-01 は解消済み。§5 参照）。

| 画面 ID | 画面名 | 種別 | URL パス | 関連機能 ID | 利用ロール | 主な API（operationId） | 画面設計 | ステータス |
|------|------|------|------|------|------|------|------|------|
| SCR-01 | ログイン | 認証 | `/login` | F-AUTH-01 | 全ロール（未認証可） | `loginInit`, `resendMagicLink` | `業務設計/user/spec-user-screen-ui.md` | 新規 |
| SCR-02 | ホーム | ダッシュボード | `/` | F-AUTH-02, F-APR-01, F-ORD-01 | R-ADMIN、R-EMPLOYEE、R-CUSTOMER | `getMe`, `pageApprovalRequests`, `pageOrders` | `業務設計/user/spec-user-screen-ui.md` | 新規 |
| SCR-03 | パスワード変更（廃止） | — | — | — | — | — | — | 廃止 |
| SCR-04 | ユーザー一覧 | 一覧 | `/users` | F-USR-01, F-USR-04, F-USR-05 | R-ADMIN | `pageUsers`, `disableUser`, `enableUser`, `lockUser`, `unlockUser` | `業務設計/user/spec-user-screen-ui.md` | 新規 |
| SCR-05 | ユーザー登録/編集 | 登録・編集 | `/users/new`, `/users/:userId/edit` | F-USR-02, F-USR-03 | R-ADMIN | `createUser`, `getUser`, `updateUser`, `pageCustomers` | `業務設計/user/spec-user-screen-ui.md` | 新規 |
| SCR-06 | 顧客一覧 | 一覧 | `/customers` | F-CUS-01 | R-ADMIN、R-EMPLOYEE（担当） | `pageCustomers` | `業務設計/customer/spec-customer-screen-ui.md` | 新規 |
| SCR-07 | 顧客詳細 | 詳細 | `/customers/:customerId` | F-CUS-02, F-CUS-05 | R-ADMIN、R-EMPLOYEE（担当）、R-CUSTOMER（自社） | `getCustomer`, `activateCustomer`, `deactivateCustomer` | `業務設計/customer/spec-customer-screen-ui.md` | 新規 |
| SCR-08 | 顧客登録/編集 | 登録・編集 | `/customers/new`, `/customers/:customerId/edit` | F-CUS-03, F-CUS-04 | R-ADMIN | `createCustomer`, `getCustomer`, `updateCustomer`, `pageUsers` | `業務設計/customer/spec-customer-screen-ui.md` | 新規 |
| SCR-09 | 商品一覧 | 一覧 | `/products` | F-PRD-01, F-PRD-05 | R-ADMIN、R-EMPLOYEE | `pageProducts`, `activateProduct`, `deactivateProduct` | `業務設計/product/spec-product-screen-ui.md` | 新規 |
| SCR-10 | 商品登録/編集 | 登録・編集 | `/products/new`, `/products/:productId/edit` | F-PRD-01, F-PRD-02, F-PRD-03, F-PRD-04 | R-ADMIN、R-EMPLOYEE（参照のみ） | `createProduct`, `getProduct`, `updateProduct`, `listTaxRates` | `業務設計/product/spec-product-screen-ui.md` | 新規 |
| SCR-11 | 注文一覧 | 一覧 | `/orders` | F-ORD-01, F-ORD-05 | R-ADMIN、R-EMPLOYEE（担当）、R-CUSTOMER（自社） | `pageOrders`, `cancelOrder`, `pageCustomers` | `業務設計/order/spec-order-screen-ui.md` | 新規 |
| SCR-12 | 注文登録/編集 | 登録・編集 | `/orders/new`, `/orders/:orderId/edit` | F-ORD-02, F-ORD-03, F-ORD-04, F-PRD-01 | R-ADMIN、R-EMPLOYEE（担当） | `createOrder`, `getOrder`, `updateOrder`, `submitOrder`, `pageCustomers`, `pageProducts`, `getCustomer`, `listTaxRates` | `業務設計/order/spec-order-screen-ui.md` | 新規 |
| SCR-13 | 注文詳細 | 詳細 | `/orders/:orderId` | F-ORD-01, F-ORD-04, F-ORD-05, F-APR-02, F-ATT-01〜04, F-RPT-01 | R-ADMIN、R-EMPLOYEE（担当）、R-CUSTOMER（自社） | `getOrder`, `listApprovalHistories`, `submitOrder`, `cancelOrder`, `approveApprovalRequest`, `returnApprovalRequest`, `listOrderAttachments`, `uploadOrderAttachment`, `downloadAttachment`, `deleteAttachment`, `downloadOrderPdf` | `業務設計/order/spec-order-screen-ui.md` | 新規 |
| SCR-14 | 承認待ち一覧 | 一覧 | `/approvals` | F-APR-01, F-APR-02 | R-ADMIN | `pageApprovalRequests`, `approveApprovalRequest`, `returnApprovalRequest`, `pageCustomers` | `業務設計/approval/spec-approval-screen-ui.md` | 新規 |
| SCR-15 | 承認ルール管理 | 一覧 | `/approval-rules` | F-APR-03 | R-ADMIN | `listApprovalRules`, `createApprovalRule`, `updateApprovalRule` | `業務設計/approval/spec-approval-screen-ui.md` | 新規 |
| SCR-16 | 集計・CSV 出力 | 一覧 | `/reports` | F-RPT-02, F-RPT-03 | R-ADMIN、R-EMPLOYEE（担当） | `getOrderSummary`, `getCustomerSummary`, `getProductSummary`, `createExportJob`, `pageExportJobs`, `getExportJob`, `downloadExportJob`, `pageCustomers` | `業務設計/report/spec-report-screen-ui.md` | 新規 |
| SCR-17 | 監査ログ | 一覧 | `/audit-logs` | F-AUD-01 | R-ADMIN | `pageAuditLogs` | `業務設計/user/spec-user-screen-ui.md` | 新規 |
| SCR-18 | 通知履歴 | 一覧 | `/notification-logs` | F-NTF-01 | R-ADMIN | `pageNotificationLogs`, `retryNotification` | `業務設計/user/spec-user-screen-ui.md` | 新規 |
| SCR-19 | 権限エラー（403） | エラー | `/403` | — | 全ロール（認証済みのみ。未認証は SCR-01） | — | `業務設計/user/spec-user-screen-ui.md` | 新規 |
| SCR-20 | 未存在エラー（404） | エラー | `/404`（未定義パスの受け皿） | — | 全ロール（認証済みのみ。未認証は SCR-01） | — | `業務設計/user/spec-user-screen-ui.md` | 新規 |
| SCR-21 | ログイン認証（Magic Link） | 認証 | `/auth/verify?token=` | F-AUTH-01 | 全ロール（未認証可） | `verifyMagicLink`, `getMe` | `業務設計/user/spec-user-screen-ui.md` | 新規 |

| 列 | 記入要領 |
|------|------|
| 種別 | 認証 / ダッシュボード / 一覧 / 詳細 / 登録・編集 / 確認 / 完了 / ポップアップ / エラー |
| URL パス | フロントエンドのルーティングパス。`routes.tsx` で画面 ID とトレース可能にする（`spec-frontend-coding-rule.md` §9） |
| 利用ロール | `spec-03-auth-ui.md` と一致させる。「担当」は担当顧客のみ、「自社」は自社のみ。権限外はメニュー非表示（`spec-12` §7） |
| 主な API | `spec-07-api-list.yaml` の `operationId` を参照する |

> 添付（M-attachment）は独立画面を持たず、SCR-13 の添付パネルで提供する。

### 1.1 内部ダイアログ

独立した SCR-ID は採番せず、`SCR-nn/DLG-m` と表記する（本書限りの表記）。

| ID | 親画面 | 内容 | 操作ロール | operationId |
|---|---|---|---|---|
| SCR-13/DLG-1 | SCR-13、SCR-11 | 注文取消し（理由入力） | R-ADMIN、R-EMPLOYEE（担当） | `cancelOrder` |
| SCR-13/DLG-2 | SCR-13、SCR-14 | 承認確認 | R-ADMIN（申請者以外） | `approveApprovalRequest` |
| SCR-13/DLG-3 | SCR-13、SCR-14 | 差戻し（理由 1〜500 文字必須の入力後、確認 `W0028`） | R-ADMIN（申請者以外） | `returnApprovalRequest` |
| SCR-13/DLG-4 | SCR-13 | 添付アップロード（公開範囲指定） | R-ADMIN、R-EMPLOYEE（担当） | `uploadOrderAttachment` |
| SCR-15/DLG-1 | SCR-15 | 承認ルール登録/編集 | R-ADMIN | `createApprovalRule`, `updateApprovalRule` |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.05-screen-list-ui.s02" -->
## 2. 画面 × ロール アクセスマトリクス

> 権限の正本は `spec-03-auth-ui.md`（操作権限・データ範囲・項目参照）。本表は画面単位の到達可否のみを示す。

| 画面 ID | R-ADMIN | R-EMPLOYEE | R-CUSTOMER | 未認証 |
|------|:---:|:---:|:---:|:---:|
| SCR-01 | ○ | ○ | ○ | ○ |
| SCR-02 | ○ | ○ | ○ | × |
| SCR-03 | × | × | × | × |
| SCR-04 | ○ | × | × | × |
| SCR-05 | ○ | × | × | × |
| SCR-06 | ○ | 担当 | × | × |
| SCR-07 | ○ | 担当 | 自社 | × |
| SCR-08 | ○ | × | × | × |
| SCR-09 | ○ | ○ | × | × |
| SCR-10 | ○ | 参照のみ | × | × |
| SCR-11 | ○ | 担当 | 自社 | × |
| SCR-12 | ○ | 担当 | × | × |
| SCR-13 | ○ | 担当 | 自社 | × |
| SCR-14 | ○ | × | × | × |
| SCR-15 | ○ | × | × | × |
| SCR-16 | ○ | 担当 | × | × |
| SCR-17 | ○ | × | × | × |
| SCR-18 | ○ | × | × | × |
| SCR-19 | ○ | ○ | ○ | × |
| SCR-20 | ○ | ○ | ○ | × |
| SCR-21 | ○ | ○ | ○ | ○ |

> 未認証で SCR-19 / SCR-20 （未定義パスを含む）に到達した場合は、遷移先を `ddmsaa.returnTo` に保存して SCR-01 へ遷移する（`spec-12` §7）。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.05-screen-list-ui.s03" -->
## 3. 画面 × API マトリクス（任意）

> 画面で使用する API の漏れ確認用。`operationId` は `spec-07` に存在することを確認済み。

| 画面 ID | 使用 API |
|------|------|
| SCR-01 | `loginInit`, `resendMagicLink` |
| SCR-02 | `getMe`, `pageApprovalRequests`, `pageOrders` |
| SCR-03 | — |
| SCR-04 | `pageUsers`, `disableUser`, `enableUser`, `lockUser`, `unlockUser` |
| SCR-05 | `createUser`, `getUser`, `updateUser`, `pageCustomers` |
| SCR-06 | `pageCustomers` |
| SCR-07 | `getCustomer`, `activateCustomer`, `deactivateCustomer` |
| SCR-08 | `createCustomer`, `getCustomer`, `updateCustomer`, `pageUsers` |
| SCR-09 | `pageProducts`, `activateProduct`, `deactivateProduct` |
| SCR-10 | `createProduct`, `getProduct`, `updateProduct`, `listTaxRates` |
| SCR-11 | `pageOrders`, `cancelOrder`, `pageCustomers` |
| SCR-12 | `createOrder`, `getOrder`, `updateOrder`, `submitOrder`, `pageCustomers`, `pageProducts`, `getCustomer`, `listTaxRates` |
| SCR-13 | `getOrder`, `listApprovalHistories`, `submitOrder`, `cancelOrder`, `approveApprovalRequest`, `returnApprovalRequest`, `listOrderAttachments`, `uploadOrderAttachment`, `downloadAttachment`, `deleteAttachment`, `downloadOrderPdf` |
| SCR-14 | `pageApprovalRequests`, `approveApprovalRequest`, `returnApprovalRequest`, `pageCustomers` |
| SCR-15 | `listApprovalRules`, `createApprovalRule`, `updateApprovalRule` |
| SCR-16 | `getOrderSummary`, `getCustomerSummary`, `getProductSummary`, `createExportJob`, `pageExportJobs`, `getExportJob`, `downloadExportJob`, `pageCustomers` |
| SCR-17 | `pageAuditLogs` |
| SCR-18 | `pageNotificationLogs`, `retryNotification` |
| SCR-19 | — |
| SCR-20 | — |
| SCR-21 | `verifyMagicLink`, `getMe` |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.05-screen-list-ui.s04" -->
## 4. ナビゲーション（メニュー）構成

> ロールごとのメニュー。共通レイアウトは `spec-12` §1 に従う。ヘッダ右上のユーザーメニューに「ログアウト」（JWT を破棄して SCR-01 へ）を置く。

```
DDMSAA 販売システム
├── ホーム              (SCR-02)  [R-ADMIN, R-EMPLOYEE, R-CUSTOMER]
├── 注文                (SCR-11)  [R-ADMIN, R-EMPLOYEE, R-CUSTOMER]
├── 承認待ち            (SCR-14)  [R-ADMIN]
├── 顧客                (SCR-06)  [R-ADMIN, R-EMPLOYEE]
├── 自社情報            (SCR-07)  [R-CUSTOMER]
├── 商品                (SCR-09)  [R-ADMIN, R-EMPLOYEE]
├── 集計・CSV 出力      (SCR-16)  [R-ADMIN, R-EMPLOYEE]
├── ユーザー            (SCR-04)  [R-ADMIN]
├── 承認ルール          (SCR-15)  [R-ADMIN]
├── 監査ログ            (SCR-17)  [R-ADMIN]
└── 通知履歴            (SCR-18)  [R-ADMIN]
```

- 権限のないメニュー項目は表示しない。ロール外の画面を直接 URL で指定した場合はルートガードが SCR-19 へ遷移させる（`spec-12` §7）。ロール内の画面でも API が `403`/`404` を返せば SCR-19/SCR-20 へ遷移する（最終判定は API）。
- 既定ランディングは全ロール SCR-02。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.05-screen-list-ui.s05" -->
## 5. 確認事項（TBD）

| # | 事項 |
|---|---|
| TBD-SCR-04 | ホームの件数表示は専用 API を設けず `pageApprovalRequests` / `pageOrders` の総件数（`total`）で代替する（仮置き） |
| TBD-SCR-05 | 画面ごとの未決事項は各 `業務設計/<module>/spec-<module>-screen-ui.md` 末尾の TBD を参照 |

> 解決済み：TBD-SCR-01（関連機能 ID。§1 は `F-xxx` で記載済み）・TBD-SCR-02（operationId の突き合わせ）は 02・07 の確定により解消した。

<!-- devtool:endblock -->
