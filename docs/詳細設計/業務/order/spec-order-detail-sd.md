# 詳細設計 — 注文

## 1. データ

| テーブル | 主な列 |
|---|---|
| `orders` | order_id, order_number, customer_id, sales_user_id, order_date, status, subtotal_amount, tax_amount, total_amount, version, revised_from_order_id, cancel_reason |
| `order_items` | order_item_id, order_id, line_no, product_id, 商品スナップショット, quantity, unit_price, tax_category_code, tax_rate, line_amount, tax_amount |
| `order_status_histories` | from_status, to_status, reason, changed_by, changed_at |

## 2. API

| operationId | API | 条件 |
|---|---|---|
| listOrders | GET /api/orders | データ範囲に限定 |
| createOrder | POST /api/orders | ADMIN/担当 EMPLOYEE |
| getOrder | GET /api/orders/{orderId} | データ範囲に限定 |
| updateOrder | PATCH /api/orders/{orderId} | DRAFT/RETURNED、version 一致 |
| submitOrder | POST /api/orders/{orderId}/submit | DRAFT/RETURNED、version 一致 |
| cancelOrder | POST /api/orders/{orderId}/cancel | DRAFT/RETURNED は作成者/担当、APPROVED は ADMIN |

## 3. 作成・更新アルゴリズム

1. 利用者の顧客スコープと、customerId の有効性を検証する。
2. 明細が 1 件以上、数量 > 0、単価 >= 0、有効商品であることを検証する。
3. 商品のコード・名称・単価・税区分・税率を明細にスナップショットする。
4. 各明細について `lineAmount=quantity*unitPrice`、`taxAmount=HALF_UP(lineAmount*taxRate/100)` を計算する。
5. 小計、税額、合計を明細から合算し、画面の集計値を無視する。
6. `orders.version` を確認して更新し、+1 する。不一致は 409。
7. 注文、明細、状態履歴、監査ログを同一トランザクションで保存する。

## 4. 状態遷移

| 操作 | 前状態 | 後状態 | 追加処理 |
|---|---|---|---|
| 作成 | — | DRAFT | 状態履歴 |
| 申請 | DRAFT/RETURNED | PENDING_APPROVAL | 承認依頼スナップショット、通知 |
| 承認 | PENDING_APPROVAL | APPROVED | 承認履歴、通知 |
| 差戻し | PENDING_APPROVAL | RETURNED | 理由、承認履歴、通知 |
| 取消し | DRAFT/RETURNED/APPROVED | CANCELLED | 取消し理由、通知 |

APPROVED 注文は更新できない。訂正は取消し後の新規作成とし、訂正元がある場合に revised_from_order_id を保存する。

## 5. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-ORD-01 | 担当外顧客へ作成 | 404 |
| UT-ORD-02 | 明細税率を改ざんして送信 | サーバー再計算値を保存 |
| UT-ORD-03 | 旧 version で更新 | 409 |
| UT-ORD-04 | APPROVED を更新 | 409 |
| UT-ORD-05 | RETURNED を再申請 | PENDING_APPROVAL と新規依頼 |
| UT-ORD-06 | CUSTOMER が作成 | 403 |

- **BR-ORD-001 作成権限**: ADMIN 又は担当 EMPLOYEE だけが注文を作成できる。
- **BR-ORD-002 編集状態**: DRAFT と RETURNED だけを更新又は申請できる。
- **BR-ORD-003 金額再計算**: 明細・小計・税額・合計は常にサーバーで計算する。
- **BR-ORD-004 明細税額**: 税額は明細行ごとの HALF_UP で計算し、税区分は STANDARD 10%、REDUCED 8%、EXEMPT 0% とする。
- **BR-ORD-005 承認済み訂正**: APPROVED は直接更新せず、取消しと必要に応じた revised_from_order_id 付き新規注文で訂正する。
