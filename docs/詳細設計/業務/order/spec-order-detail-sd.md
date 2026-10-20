# 詳細設計 — 注文

> 業務ルールの番号・内容は `基本設計/共通/spec-08-business-rule-list.md` を正とし、本書は実装手順を定める。

## 1. データ

| テーブル | 主な列 |
|---|---|
| `orders` | id（OD+8 桁＝注文番号）, customer_id, sales_user_id, order_date, status（T07-6）, subtotal_amount, tax_amount, total_amount, revised_from_order_id, cancel_reason, updated_at（楽観ロック） |
| `order_items` | id（OI+8 桁）, order_id, line_number, product_id, product_code / product_name（複写）, quantity, unit_price（複写）, tax_category, tax_rate（複写）, line_amount, tax_amount |
| `order_status_histories` | id（OH+8 桁）, order_id, from_status, to_status, reason。変更者・日時は created_by / created_at |

## 2. API

| operationId | API | 条件 |
|---|---|---|
| pageOrders | GET /api/v1/orders | データ範囲に限定（範囲外は除外） |
| createOrder | POST /api/v1/orders | ADMIN / 担当 EMPLOYEE。保存後の OrderVO を返す |
| getOrder | GET /api/v1/orders/{orderId} | データ範囲に限定（範囲外は 404）。明細・状態履歴・currentApprovalRequestId・canApprove を返す |
| updateOrder | PUT /api/v1/orders/{orderId} | DRAFT / RETURNED、updatedAt 一致。保存後の OrderVO を返す |
| submitOrder | POST /api/v1/orders/{orderId}/submit | DRAFT / RETURNED、updatedAt 一致、申請時検証 |
| cancelOrder | POST /api/v1/orders/{orderId}/cancel | DRAFT / RETURNED は ADMIN・担当 EMPLOYEE、APPROVED は ADMIN |
| listApprovalHistories | GET /api/v1/orders/{orderId}/approval-histories | データ範囲に限定。CUSTOMER には操作者・理由を null |

## 3. 作成・更新アルゴリズム

1. 利用者のデータ範囲と customerId を検証する（範囲外は 404 `E0005`）。新規作成で顧客が無効なら 409 `E0054`（BR-CUS-002）。更新では customerId を変更できない。
2. 担当者を決める（BR-ORD-008）。EMPLOYEE は本人を設定し、要求の salesUserId は無視する。ADMIN は salesUserId 必須で、顧客の有効な担当 EMPLOYEE でなければ 400 `E0008`。更新で担当者を変えられるのは ADMIN だけ。
3. 訂正元（BR-ORD-009）：新規作成時だけ revisedFromOrderId を受け付け、CANCELLED・同一顧客・未訂正でなければ 409 `E0006`。更新時は要求値を無視する。
4. 明細が 1 件以上、数量 > 0 であることを検証する（400 `E0008`）。追加・変更する明細の商品は有効商品であること（無効なら 409 `E0054`）。既存明細のまま残っている無効商品は保存を妨げない（BR-PRD-003）。
5. 商品マスタから商品コード・名称・標準単価・税区分を、`tax_rates` から税率を明細に複写する。単価は要求値を受け付けない（BR-PRD-002）。
6. 各明細について `lineAmount=quantity*unitPrice`、`taxAmount=HALF_UP(lineAmount*taxRate/100)` を計算する。
7. 小計、税額、合計を明細から合算し、画面の集計値を無視する。
8. 更新時は `UPDATE … WHERE id=? AND updated_at=?` で照合し、更新行数 0 は 409 `E0051`（BR-ORD-006）。
9. 注文、明細、状態履歴（作成時）、監査ログ（`ORDER_CREATE` / `ORDER_UPDATE`）を同一トランザクションで保存する。注文 ID は `next_business_id('OD', 8)` で採番し、成功 messageId は作成 `I0010`（{0}=注文番号）、更新 `I0011`。
10. 保存後の注文を再読込して OrderVO（金額・新しい updatedAt を含む）を返す。画面はこの updatedAt で続けて申請できる。

## 4. 申請時検証（BR-ORD-010）

1. 状態が DRAFT / RETURNED であること（他は 409 `E0006`）、updatedAt が一致すること（409 `E0051`）。
2. 顧客が有効、全明細の商品が有効、（EMPLOYEE の場合）担当割当が有効、担当者が顧客の有効な担当であることを確認する。満たさなければ 409 `E0054`。
3. 明細の商品コード・名称・単価・税区分・税率をもう一度複写して再計算し、この値で確定する（BR-PRD-002）。
4. 承認依頼を作成し（`spec-approval-detail-sd.md` §3）、注文を PENDING_APPROVAL にして、状態履歴・監査ログ（`ORDER_SUBMIT`）・通知（ORDER_SUBMITTED）を同一トランザクションで保存する。

## 5. 状態遷移

すべての遷移で状態履歴と監査ログを同一トランザクションに記録する（BR-ORD-011）。

| 操作 | 前状態 | 後状態 | 追加処理 | 監査 `audit_action` | 成功 messageId |
|---|---|---|---|---|---|
| 作成 | — | DRAFT | 状態履歴 | `ORDER_CREATE` | `I0010` |
| 申請 | DRAFT / RETURNED | PENDING_APPROVAL | 申請時検証、再複写、承認依頼スナップショット、通知 | `ORDER_SUBMIT` | `I0020` |
| 承認 | PENDING_APPROVAL | APPROVED | 承認履歴、通知 | `ORDER_APPROVE` | `I0022` |
| 差戻し | PENDING_APPROVAL | RETURNED | 理由、承認履歴、通知 | `ORDER_RETURN` | `I0023` |
| 取消し | DRAFT / RETURNED / APPROVED | CANCELLED | 取消し理由、通知 | `ORDER_CANCEL` | `I0021` |

- 取消し（BR-ORD-007）：APPROVED の取消しを EMPLOYEE が要求したら 403 `E0002`。PENDING_APPROVAL・CANCELLED は 409 `E0006`。取消し理由は 1〜500 文字必須（400 `E0008`）。
- APPROVED 注文は更新できない。訂正は取消し後の新規作成とし、訂正元を revised_from_order_id に保存する（BR-ORD-005 / BR-ORD-009）。
- 通知の宛先は BR-NTF-003 に従う。

## 6. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-ORD-01 | 担当外顧客へ作成 | 404 `E0005` |
| UT-ORD-02 | 明細の単価・税率・合計を改ざんして送信 | 商品マスタの単価とサーバー再計算値を保存 |
| UT-ORD-03 | 古い updatedAt で更新 | 409 `E0051` |
| UT-ORD-04 | APPROVED を更新 | 409 `E0006` |
| UT-ORD-05 | RETURNED を再申請 | PENDING_APPROVAL と新規承認依頼 |
| UT-ORD-06 | CUSTOMER が作成 | 403 `E0002` |
| UT-ORD-07 | 作成 | 注文番号が `OD` + 8 桁、応答に updatedAt と合計を含む |
| UT-ORD-08 | 無効商品を含む DRAFT を申請 | 409 `E0054` |
| UT-ORD-09 | 保存後に商品の標準単価を変更し、再保存 | 明細単価が新しい標準単価になる |
| UT-ORD-10 | EMPLOYEE が APPROVED を取消し | 403 `E0002` |
| UT-ORD-11 | PENDING_APPROVAL を取消し | 409 `E0006` |
| UT-ORD-12 | ADMIN が salesUserId なしで作成 | 400 `E0008` |
| UT-ORD-13 | 訂正済みの CANCELLED 注文を再び訂正元に指定 | 409 `E0006` |
| UT-ORD-14 | 無効顧客で作成 | 409 `E0054` |

## 7. 適用する業務ルール

BR-AUTH-003、BR-AUTH-006、BR-CUS-001、BR-CUS-002、BR-PRD-002、BR-PRD-003、BR-ORD-001〜BR-ORD-011、BR-NTF-003、BR-AUD-001（内容は `spec-08-business-rule-list.md`）。
