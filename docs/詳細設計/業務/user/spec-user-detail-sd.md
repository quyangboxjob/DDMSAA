# 詳細設計 — ユーザー・認証

## 1. データ

| テーブル | 主な列 | 制約 |
|---|---|---|
| `users` | user_id, login_id, display_name, email, password_hash, role_code, customer_id, status, failed_login_count, locked_until | login_id/email は一意、CUSTOMER は customer_id 必須 |
| `user_sessions` | session_id, user_id, token_hash, expires_at, revoked_at | token_hash は一意 |
| `audit_logs` | actor_user_id, action, target_type, target_id, occurred_at | パスワード・セッション生値は保存禁止 |

## 2. API

| operationId | API | 権限 | 処理 |
|---|---|---|---|
| login | POST /api/auth/login | 匿名 | loginId/password を照合し Cookie を発行 |
| logout | POST /api/auth/logout | 全ログイン者 | セッションを失効 |
| getMe | GET /api/auth/me | 全ログイン者 | 自分の ID、表示名、ロール、customerId を返す |
| listUsers | GET /api/users | ADMIN | 検索・ページング |
| createUser | POST /api/users | ADMIN | パスワードを BCrypt 化して作成 |
| updateUser | PATCH /api/users/{userId} | ADMIN | 表示名、メール、ロール、顧客、状態を更新 |
| listAuditLogs | GET /api/audit-logs?userId={userId} | ADMIN | ユーザーの監査を返す |

## 3. ログイン処理

1. `login_id` で利用者を取得する。存在しない場合も同じダミー BCrypt 照合を行う。
2. status=ACTIVE、locked_until が現在以前、BCrypt 照合成功をすべて満たした場合に成功とする。
3. 128 bit 以上の乱数セッションを作り、ハッシュを `user_sessions` に保存する。生値は `DDMSAA_SESSION` Cookie にだけ設定する。
4. 成功時は失敗回数を 0、失敗時は +1 する。5 回で `locked_until=now+30min`。
5. 認証結果を `audit_logs` に記録する。失敗応答は常に 401 の同一メッセージ、ロック中は 423 とする。

## 4. バリデーション

- loginId: 3～100 文字、空白のみ禁止。
- パスワード: 作成/変更時は 12～128 文字、英字・数字・記号を各 1 文字以上。
- CUSTOMER ロールには customerId 必須。ADMIN/EMPLOYEE には customerId を設定しない。
- 無効化又はパスワード変更時は当該ユーザーの有効セッションを revoke する。

## 5. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-USR-01 | 正しい ID/パスワード | 200、HttpOnly Cookie、監査記録 |
| UT-USR-02 | 不正 ID 又はパスワード | 同一の 401 応答、存在漏えいなし |
| UT-USR-03 | 5 回連続失敗 | 30 分ロック、423 |
| UT-USR-04 | CUSTOMER に customerId なし | 422 |
| UT-USR-05 | EMPLOYEE が users API | 403 |

- **BR-USR-001 管理者限定**: ユーザー作成、更新、無効化、ロール変更は ADMIN だけが実行する。
- **BR-USR-002 パスワード非保存**: 平文パスワードと生のセッショントークンを DB・監査・ログへ保存しない。
- **BR-USR-003 顧客アカウント紐付け**: CUSTOMER は必ず customer_id を持ち、その顧客のデータだけを参照する。
