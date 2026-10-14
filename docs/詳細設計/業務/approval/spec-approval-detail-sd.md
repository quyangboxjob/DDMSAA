# 詳細設計 — 承認

## 1. データ

| テーブル | 主な列 | 用途 |
|---|---|---|
| `approval_rules` | amount_from, amount_to, sequence, approver_role_code, approver_user_id, active, effective_from/to | 将来拡張可能な経路定義 |
| `approval_requests` | order_id, sequence, approver_user_id, rule_snapshot, status, requested_by, requested_at, acted_at | 申請時点の経路スナップショット |
| `approval_actions` | approval_request_id, action, comment, acted_by, acted_at | 承認/差戻しの不変履歴 |

初期データは `amount_from=0 / sequence=1 / approver_role_code=ADMIN` の 1 段経路とする。

## 2. API

| operationId | API | 権限 |
|---|---|---|
| listPendingApprovals | GET /api/approvals/pending | ADMIN |
| approveOrder | POST /api/approval-requests/{approvalRequestId}/approve | 割当済み ADMIN |
| returnOrder | POST /api/approval-requests/{approvalRequestId}/return | 割当済み ADMIN |
| getApprovalHistory | GET /api/orders/{orderId}/approvals | 注文の参照権限 |
| listApprovalRules | GET /api/approval-rules | ADMIN |
| createApprovalRule | POST /api/approval-rules | ADMIN |
| updateApprovalRule | PATCH /api/approval-rules/{ruleId} | ADMIN |
| listNotifications | GET /api/notifications | ADMIN |
| retryNotification | POST /api/notifications/{notificationId}/retry | ADMIN |

## 3. 申請処理

1. 注文が DRAFT 又は RETURNED、かつ version 一致を確認する。
2. 申請日時点で有効なルールを金額・有効期間・sequence 順で選び、承認者を解決する。
3. 初版では ADMIN ロールの有効ユーザー 1 名を承認者にする。解決できない場合は 422 とし、注文状態を変えない。
4. 選んだルールを JSON で rule_snapshot に保存し、approval_requests を作る。
5. 注文を PENDING_APPROVAL にし、状態履歴、監査、通知ジョブを同一トランザクションで保存する。

## 4. 操作処理

- 承認要求が PENDING、操作者が approver_user_id、対象注文が PENDING_APPROVAL であることをロックして検証する。
- approve は action=APPROVED を履歴保存し、最終段階なら注文を APPROVED にする。
- return は 1～500 文字のコメント必須、action=RETURNED を履歴保存し、注文を RETURNED にする。
- 二重操作、別 ADMIN、古い状態での操作は 409 又は 403/404 とし、通知を重複発行しない。
- 代理承認は初版で扱わない。

## 5. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-APR-01 | DRAFT を申請 | ADMIN 1 段の依頼、PENDING_APPROVAL |
| UT-APR-02 | 別 ADMIN が承認 | 403 |
| UT-APR-03 | 差戻しコメント空 | 422 |
| UT-APR-04 | 同じ依頼を二重承認 | 409、履歴 1 件 |
| UT-APR-05 | 経路設定変更後の申請済み注文 | rule_snapshot は不変 |

- **BR-APR-001 初版承認経路**: 金額を問わず ADMIN 1 段承認を適用する。
- **BR-APR-002 経路スナップショット**: 申請時の経路を approval_requests.rule_snapshot に保存し、設定変更を遡及させない。
- **BR-APR-003 承認者本人性**: 割り当てられた ADMIN だけが承認又は差戻しを実行できる。
- **BR-APR-004 差戻し理由**: 差戻しは 1～500 文字の理由必須で、注文を RETURNED にする。
