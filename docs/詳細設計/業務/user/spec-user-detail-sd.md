# 詳細設計 — ユーザー・認証

> 認証フロー（Magic Link・JWT・Redis 一時状態）の実装は `基本設計/基盤/spec-02-auth-service-ui.md` を正とし、本書は業務側（ユーザー管理・ロール・停用/ロック）を定める。
> テーブル・API・メッセージは `spec-06-db-physical.yaml`・`spec-07-api-list.yaml`・`spec-09-message-list.yaml` を参照し、再定義しない。業務ルールの番号・内容は `基本設計/共通/spec-08-business-rule-list.md` を正とする。

## 1. データ

| テーブル | 主な列 | 制約 |
|---|---|---|
| `users` | user_id, user_name, role, status, customer_id | user_id はロール別 prefix（A/E/C）+4 桁。CUSTOMER は customer_id 必須、ADMIN / EMPLOYEE は NULL（chk_users_customer_id） |
| `user_contacts` | user_id, email | 共有主キー。email は全行で一意（論理削除なし）。Magic Link の送信先 |
| `system_locks` | user_id, lock_started_at, lock_reason | 共有主キー（論理削除なし）。lock_started_at が NULL でなければロック中。ロック時は現在時刻を設定し、解除は NULL に更新する |
| `audit_logs` | actor_user_id, audit_action, target_type, target_id, trace_id, audit_detail | Magic Link トークンを保存しない。操作名は BR-AUD-001。ログイン系は auth-service が INSERT |

## 2. API

| operationId | API | 権限 | 処理 |
|---|---|---|---|
| loginInit | POST /api/v1/auth/login-init | 公開 | auth-service。ログイン可否を判定し Magic Link を送信。応答は常に `I0001` |
| verifyMagicLink | POST /api/v1/auth/magic-link/verify | 公開 | auth-service。トークンを消費・状態再検証し JWT を発行 |
| resendMagicLink | POST /api/v1/auth/magic-link/resend | 公開 | auth-service。旧トークンを失効し再送 |
| getMe | GET /api/v1/me | 全ログイン者 | ユーザー ID、表示名、ロール、customerId を返す |
| pageUsers | GET /api/v1/users | ADMIN | 検索・ページング（ロック状態を含む） |
| createUser | POST /api/v1/users | ADMIN | ロール別 prefix で採番し、users と user_contacts を登録 |
| getUser | GET /api/v1/users/{userId} | ADMIN | 詳細 |
| updateUser | PUT /api/v1/users/{userId} | ADMIN | 表示名、メール、ロール、顧客を更新（updatedAt で楽観ロック。ロール変更は §4） |
| disableUser / enableUser | POST /api/v1/users/{userId}/disable・/enable | ADMIN | status を DISABLED / ACTIVE に変更 |
| lockUser / unlockUser | POST /api/v1/users/{userId}/lock・/unlock | ADMIN | system_locks の lock_started_at を設定 / NULL に戻す |
| pageAuditLogs | GET /api/v1/audit-logs | ADMIN | 監査ログの検索 |

## 3. 登録・更新処理

1. ロールが CUSTOMER のときは customerId が有効な顧客であることを検証する。ADMIN / EMPLOYEE には customerId を設定しない。
2. 登録時は `next_business_id(prefix, 4)`（prefix=A/E/C）でユーザー ID を採番する。ロール変更時もユーザー ID は変えない。
3. メールアドレスは user_contacts に保存し、未削除行で重複する場合は 409（`E0007`）とする。
4. users・user_contacts・audit_logs（`USER_CREATE` / `USER_UPDATE`）を同一トランザクションで保存する。更新時は `updated_at` を照合し、不一致は 409（`E0051`）。成功 messageId は登録 `I0010`、更新 `I0011`。

## 4. 停用・ロックの反映

- disableUser は status=DISABLED、lockUser は system_locks に lock_started_at=現在時刻と理由を保存する（行がなければ作成）。監査は `USER_DISABLE` / `USER_ENABLE` / `USER_LOCK` / `USER_UNLOCK`。成功 messageId は停用 `I0032`、停用解除 `I0033`、ロック `I0025`、ロック解除 `I0029`。
- 利用者は API で削除しない（停用で代替する）。`users.deleted_at` は誤登録の除去など運用作業でだけ設定し、設定された利用者はログインできない（BR-AUTH-001）。
- auth-service は loginInit・verifyMagicLink でログイン可否（停用・ロック・論理削除）を判定する（基盤 spec-02 §6.1）。
- business-service-core はリクエストごとの状態照会を行わない。発行済みの JWT は有効期限（最長 60 分）まで有効で、停用・ロックはその後のログインから反映される（`spec-03-auth-ui.md` §5-2）。
- 自分自身の停用・ロック・ロール変更は 409（`E0006`）とする（BR-USR-005）。
- 対象が ADMIN で、停用・ロック・ロール変更の後に有効（ACTIVE かつ未ロック）な ADMIN が 0 名になる場合は 409（`E0055`）とする（BR-USR-005）。判定は ADMIN 行を `SELECT … FOR UPDATE` でロックしてから行い、同時操作で 0 名にならないようにする。

## 4.1 ロール変更（BR-USR-004）

1. 変更できるのは ADMIN ⇔ EMPLOYEE だけ。CUSTOMER から、又は CUSTOMER への変更は 400（`E0008`）。ユーザー ID は変えない。
2. EMPLOYEE から ADMIN へ変更する場合、有効な担当割当、又は未完了（DRAFT / RETURNED / PENDING_APPROVAL）注文の担当者（`orders.sales_user_id`）が残っていれば 409（`E0006`）とし、先に担当を付け替える。
3. ADMIN から EMPLOYEE への変更は BR-USR-005 の判定を行う。
4. 発行済み JWT のロールは有効期限（最長 60 分）まで変わらない（`spec-03-auth-ui.md` §5-2 と同じ扱い）。

## 5. バリデーション

- userName：1～100 文字。email：形式チェック・255 文字以内。
- role：ADMIN / EMPLOYEE / CUSTOMER。CUSTOMER は customerId 必須、ADMIN / EMPLOYEE は customerId を指定できない（いずれも 400 `E0008`）。
- lockReason：500 文字以内。

## 6. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-USR-01 | 有効なユーザー ID で loginInit | 200 `I0001`、Magic Link 送信 |
| UT-USR-02 | 存在しない・停用・ロック中のユーザー ID で loginInit | 200 `I0001`、送信なし（応答で区別できない） |
| UT-USR-03 | 期限切れ・使用済みトークンで verify | 401 `E0004` |
| UT-USR-04 | 停用後に loginInit → verifyMagicLink | 200 `I0001`（送信なし）／ verify は 401 `E0004` |
| UT-USR-05 | CUSTOMER に customerId なしで登録 | 400 `E0008` |
| UT-USR-06 | EMPLOYEE が users API | 403 `E0002` |
| UT-USR-07 | EMPLOYEE として登録 | ユーザー ID が `E` で始まる |
| UT-USR-08 | 有効な ADMIN が 1 名のとき、その ADMIN を停用（他の ADMIN から） | 409 `E0055` |
| UT-USR-09 | 自分自身をロック | 409 `E0006` |
| UT-USR-10 | EMPLOYEE を CUSTOMER に変更 | 400 `E0008` |
| UT-USR-11 | 担当割当が残る EMPLOYEE を ADMIN に変更 | 409 `E0006` |
| UT-USR-12 | loginInit・verify 成功・verify 失敗 | audit_logs に `LOGIN_REQUESTED` / `LOGIN_SUCCEEDED` / `LOGIN_FAILED`（token なし） |

## 7. 適用する業務ルール

BR-AUTH-001〜BR-AUTH-006、BR-USR-001〜BR-USR-005、BR-AUD-001（内容は `spec-08-business-rule-list.md`）。
