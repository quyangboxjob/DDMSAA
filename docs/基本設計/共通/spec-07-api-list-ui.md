# DDMSAA API 一覧

## 1. 正式な定義

API の単一の正式定義は [spec-07-api-list.yaml](spec-07-api-list.yaml) である。本書は一覧表示用であり、リクエスト・レスポンス定義を重複しない。

## 2. 共通方針

- ベース URL は `/api`。ログイン以外は `DDMSAA_SESSION` Cookie による認証が必要。
- ロールは `ADMIN`、`EMPLOYEE`、`CUSTOMER` の 3 種だけを使用する。
- API はロールに加え、顧客所属・担当顧客・注文担当者による行単位認可を必ず行う。
- 業務エラーは `400`、権限外は `403`、重複・状態・楽観ロック競合は `409`、認証失敗は `401` とする。

## 3. モジュール別 operationId

| モジュール | operationId |
| --- | --- |
| 認証・利用者 | `login`、`logout`、`getMe`、`changePassword`、`listUsers`、`createUser`、`updateUser`、`listAuditLogs` |
| 顧客 | `listCustomers`、`createCustomer`、`getCustomer`、`updateCustomer`、`updateCustomerStatus` |
| 商品 | `listProducts`、`createProduct`、`getProduct`、`updateProduct`、`updateProductStatus` |
| 注文 | `listOrders`、`createOrder`、`getOrder`、`updateOrder`、`submitOrder`、`cancelOrder` |
| 承認・通知 | `listPendingApprovals`、`approveOrder`、`returnOrder`、`getApprovalHistory`、`listApprovalRules`、`createApprovalRule`、`updateApprovalRule`、`listNotifications`、`retryNotification` |
| 添付 | `listOrderAttachments`、`uploadOrderAttachment`、`downloadAttachment`、`deleteAttachment` |
| 帳票 | `getOrderSummary`、`getCustomerReport`、`getProductReport`、`createOrderExport`、`getExportJob`、`downloadExport`、`downloadOrderDocument` |

## 4. 改訂履歴

| 版数 | 日付 | 内容 |
| --- | --- | --- |
| 1.0 | 2026-10-14 | DDMSAA の詳細設計に合わせて API 正式定義を作成。 |
