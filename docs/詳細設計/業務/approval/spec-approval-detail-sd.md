# 詳細設計 — 承認・通知

> 業務ルールの番号・内容は `基本設計/共通/spec-08-business-rule-list.md` を正とし、本書は実装手順を定める。

## 1. データ

| テーブル | 主な列 | 用途 |
|---|---|---|
| `approval_rules` | id（AR+8 桁）, min_amount, max_amount, approval_step, approver_role, is_active, effective_from_date, effective_to_date | 経路定義。初版は ADMIN・1 段の 1 行だけ |
| `approval_requests` | id（AQ+8 桁）, order_id, approval_step, approver_user_id, rule_snapshot, status（T07-7）, acted_at。申請者・申請日時は created_by / created_at | 申請時点の経路スナップショット。再申請ごとに新しい行。approver_user_id は実際に処理した ADMIN（申請時は NULL） |
| `approval_histories` | id（AH+8 桁）, approval_request_id, approval_action（T07-8）, reason。操作者・日時は created_by / created_at | 承認/差戻しの不変履歴 |
| `notification_logs` | id（NL+8 桁）, notification_event（T07-11）, order_id, export_job_id, recipient_email, status（T07-12）, attempt_count, next_attempt_at, error_message, sent_at | 業務メールのアウトボックス |

初期データは `min_amount=0 / max_amount=NULL / approval_step=1 / approver_role=1(ADMIN) / effective_from_date=2026-01-01` の 1 段経路とする。

## 2. API

| operationId | API | 権限 |
|---|---|---|
| pageApprovalRequests | GET /api/v1/approval-requests | ADMIN（PENDING の依頼すべて。行ごとに canApprove を返す） |
| approveApprovalRequest | POST /api/v1/approval-requests/{approvalRequestId}/approve | 有効な ADMIN のうち申請者以外 |
| returnApprovalRequest | POST /api/v1/approval-requests/{approvalRequestId}/return | 有効な ADMIN のうち申請者以外 |
| listApprovalHistories | GET /api/v1/orders/{orderId}/approval-histories | 注文の参照権限。CUSTOMER には操作者・理由を null |
| listApprovalRules | GET /api/v1/approval-rules | ADMIN |
| createApprovalRule | POST /api/v1/approval-rules | ADMIN |
| updateApprovalRule | PUT /api/v1/approval-rules/{approvalRuleId} | ADMIN |
| pageNotificationLogs | GET /api/v1/notification-logs | ADMIN |
| retryNotification | POST /api/v1/notification-logs/{notificationLogId}/retry | ADMIN |

## 3. 申請処理（submitOrder から呼ばれる）

1. 注文側の申請時検証（`spec-order-detail-sd.md` §4）が済んでいることを前提とする。
2. 申請日時点で有効なルール（is_active、適用期間内、金額帯に該当）を選ぶ。該当がない場合は 409 `E0006` とし、注文状態を変えない。
3. 選んだルールを JSON で rule_snapshot に保存し、approval_requests を作る（approval_step=1、approver_user_id=NULL、status=PENDING、created_by=申請者）。承認者は特定しない（BR-APR-002）。
4. 注文を PENDING_APPROVAL にし、状態履歴、監査、通知（ORDER_SUBMITTED。宛先は申請者以外の有効な ADMIN 全員）を同一トランザクションで保存する。

## 4. 操作処理

1. 承認依頼と対象注文を行ロック（`SELECT … FOR UPDATE`）で取得する。範囲外・不存在は 404 `E0005`。
2. 承認依頼が PENDING で、注文が PENDING_APPROVAL であること。違えば 409 `E0006`（BR-APR-005。先に別の ADMIN が処理した場合を含む）。
3. 操作者が申請者（approval_requests.created_by）なら 409 `E0056`（BR-APR-002）。
4. approve：approval_action=APPROVE を履歴保存し、依頼を APPROVED（approver_user_id=操作者、acted_at=現在時刻）、注文を APPROVED にする（`I0022`、通知 ORDER_APPROVED）。
5. return：1～500 文字の理由必須（400 `E0008`）。approval_action=RETURN を履歴保存し、依頼・注文を RETURNED にする（`I0023`、通知 ORDER_RETURNED）。
6. 注文の状態履歴と監査ログ（`ORDER_APPROVE` / `ORDER_RETURN`）を同一トランザクションで保存する。通知を重複発行しない。
7. canApprove の算出：ログイン者が ADMIN、依頼が PENDING、ログイン者 ≠ 申請者のとき true。

## 5. 承認ルールの登録・更新

- approverRole は ADMIN、approvalStep は 1 だけを受け付ける（それ以外は 400 `E0008`。BR-APR-001）。DB でも `chk_approval_rules_approver_role` で保証する。
- maxAmount は minAmount 以上、effectiveToDate は effectiveFromDate 以降（400 `E0008`。DB でも `chk_approval_rules_max_amount` / `chk_approval_rules_effective_to_date`）。
- 更新は updatedAt で照合し、不一致は 409 `E0051`。申請済みの承認依頼には遡及しない。監査ログは `APPROVAL_RULE_CREATE` / `APPROVAL_RULE_UPDATE`。

## 6. 通知送信

- 通知は業務処理と同じトランザクションで notification_logs に QUEUED（attempt_count=0、next_attempt_at=現在）で作成し、送信は BAT-01（`基本設計/共通/spec-15-batch-design-ui.md`）が行う。
- 送信は初回＋再試行 3 回の最大 4 回。失敗ごとに attempt_count を加算し、4 回未満なら QUEUED のまま next_attempt_at を 1 分・5 分・15 分後にする。4 回目の失敗で FAILED（BR-NTF-002）。
- retryNotification は FAILED のみ対象（他の状態は 409 `E0006`）。attempt_count=0、next_attempt_at=現在で QUEUED に戻し、監査（`NOTIFICATION_RETRY`）を記録する（`I0026`）。
- 宛先は BR-NTF-003 の表に従い、同一メールアドレスへは 1 通にまとめる。Magic Link メールは auth-service が送信し、ここには記録しない。

## 7. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-APR-01 | DRAFT を申請 | ADMIN 1 段の依頼（approver_user_id=NULL）、PENDING_APPROVAL |
| UT-APR-02 | 申請者本人（ADMIN）が承認 | 409 `E0056` |
| UT-APR-03 | 差戻し理由が空 | 400 `E0008` |
| UT-APR-04 | 2 名の ADMIN が同じ依頼を同時に承認 | 一方は成功、他方は 409 `E0006`、履歴 1 件 |
| UT-APR-05 | 経路設定変更後の申請済み注文 | rule_snapshot は不変 |
| UT-APR-06 | 差戻し後に再申請 | 新しい approval_requests 行が作られる |
| UT-APR-07 | 申請者以外の ADMIN が承認 | 200 `I0022`、approver_user_id に操作者を記録 |
| UT-APR-08 | CUSTOMER が承認履歴を取得 | actedBy・actedByName・reason が null |
| UT-APR-09 | approverRole=EMPLOYEE のルールを登録 | 400 `E0008` |
| UT-APR-10 | 承認後の通知 | 申請者・担当者・顧客通知先へ重複なく投入 |
| UT-APR-11 | 通知が 4 回失敗 | FAILED、attempt_count=4 |
| UT-APR-12 | FAILED を手動再送 | QUEUED、attempt_count=0 |
| UT-APR-13 | max_amount < min_amount のルールを登録 | 400 `E0008`（DB は chk_approval_rules_max_amount） |

## 8. 適用する業務ルール

BR-APR-001〜BR-APR-006、BR-ORD-006、BR-ORD-011、BR-NTF-001〜BR-NTF-003、BR-AUD-001（内容は `spec-08-business-rule-list.md`）。
