# M-APR 承認管理 詳細設計書

## 1. 対象と前提

| 項目 | 内容 |
| --- | --- |
| 対象モジュール | M-APR 承認管理 |
| 根拠 | `基本設計/業務設計/approval/spec-approval-business-ui.md` |
| 対象画面 | SCR-APR-01〜05 |
| 関連データ | `approval_rules`、`approval_requests`、`approval_actions`、`notification_logs` |
| 版数・作成日 | 0.1（初稿）・2026-10-14 |
| ステータス | レビュー待ち |

承認金額閾値、段階数、代理承認は業務決定待ちである。本設計は、管理者が有効な承認ルールを設定し、注文提出時にルールのスナップショットを依頼へ保存する方式とする。

## 2. バックエンド詳細設計

| operationId | API | 権限 | 処理概要 |
| --- | --- | --- |
| listPendingApprovals | `GET /api/approvals/pending` | 指定承認者、ADMIN | 自分が判断可能な承認待ちを取得する。 |
| approveOrder | `POST /api/orders/{orderId}/approve` | 指定承認者、ADMIN | 現在段階を承認し、次段階または承認完了へ遷移する。 |
| returnOrder | `POST /api/orders/{orderId}/return` | 指定承認者、ADMIN | 必須コメント付きで注文を差戻しする。 |
| getApprovalHistory | `GET /api/orders/{orderId}/approval-history` | 注文の閲覧権限保有者 | 依頼・判断履歴を時系列で取得する。 |
| listApprovalRules | `GET /api/approval-rules` | ADMIN | 承認ルールを検索する。 |
| createApprovalRule | `POST /api/approval-rules` | ADMIN | 金額帯・順序・承認者を登録する。 |
| updateApprovalRule | `PATCH /api/approval-rules/{ruleId}` | ADMIN | 将来の依頼向けルールを更新する。 |
| listNotifications | `GET /api/notifications` | ADMIN | 通知の送信結果を検索する。 |
| retryNotification | `POST /api/notifications/{notificationId}/retry` | ADMIN | `FAILED` 通知を手動再送キューへ登録する。 |

### 2.1 承認・通知処理

1. `submitOrder` は有効な承認ルールを決定し、ルールの順序・承認者を `approval_requests.rule_snapshot` に保存する。
2. 最初の承認者に対応する `PENDING` 依頼を作成し、通知キューを登録する。
3. `approveOrder` は現行承認者本人か ADMIN だけが実行できる。中間段階なら次段階の承認者を有効化し、最終段階なら注文を `APPROVED` にする。
4. `returnOrder` は理由を必須とし、注文を `RETURNED` にする。以後の承認段階は完了扱いにせず、履歴として残す。
5. 通知は失敗しても注文・承認状態を戻さない。バックグラウンド処理が最大 3 回再送し、上限後は `FAILED` とする。

### 2.2 主要な検証

| 対象 | 検証 | 失敗時 |
| --- | --- | --- |
| 承認 | 依頼が PENDING、実行者が現行承認者、注文が PENDING_APPROVAL。 | 403 / 409 |
| 差戻し | 承認条件に加え、コメント 1〜500 文字。 | 400 / 403 / 409 |
| ルール | 金額帯の開始・終了、順序、承認者は必須。重複する有効期間・金額帯は拒否。 | 400 / 409 |
| 手動再送 | 通知状態が FAILED。宛先は変更不可。 | 409 |

## 3. 画面詳細設計

### SCR-APR-01 承認待ち一覧

注文番号、顧客、申請者、申請日時、金額、現在段階を表示する。現在の利用者が判断できる依頼だけを表示し、対象の SCR-APR-02 へ遷移する。

### SCR-APR-02 承認判断

注文明細と承認履歴を参照し、「承認」「差戻し」を表示する。差戻し時の理由は必須、承認コメントは任意とする。操作完了後は一覧を再取得して二重判断を防ぐ。

### SCR-APR-03 承認ルール設定

| 入力項目 | 必須 | ルール |
| --- | :---: | --- |
| 金額開始・終了 | ○ | 0 以上、開始値は終了値以下。 |
| 承認順 | ○ | 1 以上の整数。ルール内で重複不可。 |
| 承認者 | ○ | 有効利用者。 |
| 適用開始・終了 | ○ / 任意 | 開始日は必須、終了日は開始日以降。 |

既に依頼済み注文には変更後のルールを適用しないことを画面に明示する。

### SCR-APR-04 承認履歴

依頼日時、段階、承認者、結果、コメント、判断日時を時系列表示する。編集・削除は行わない。CUSTOMER が注文閲覧を許可される場合も、承認者の連絡先など内部情報は表示しない。

### SCR-APR-05 通知履歴・再送

検索条件は期間、状態、イベント種別、宛先とする。表示列はイベント、宛先、状態、再送回数、最終送信日時、失敗理由とする。FAILED の行だけ「再送」を表示し、確認後に `retryNotification` を実行する。宛先の編集、成功済み通知の再送、履歴削除は不可とする。

## 4. フロントエンド構成と監査

`ApprovalPendingPage`、`ApprovalDecisionPage`、`ApprovalRulePage`、`ApprovalHistoryPanel`、`NotificationLogPage` を配置する。承認、差戻し、ルール変更、手動再送を監査ログへ記録する。

## 5. 単体テスト用例

| UT-ID | 対象 | 前提・入力 | 期待結果 |
| --- | --- | --- |
| UT-APR-001 | approveOrder | 承認者以外の EMPLOYEE | 403。履歴を追加しない。 |
| UT-APR-002 | returnOrder | コメント未入力 | 400。注文状態を変更しない。 |
| UT-APR-003 | approveOrder | 最終段階の承認 | 注文が APPROVED、作成者通知を登録する。 |
| UT-APR-004 | updateApprovalRule | 承認済み依頼の基準を変更 | 既存依頼の rule_snapshot は変わらない。 |
| UT-APR-005 | retryNotification | SENT 通知を再送 | 409。再送キューへ登録しない。 |
