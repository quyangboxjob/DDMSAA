# データベース接続・運用設計

## 1. 接続方針

- PostgreSQL 16 を使用し、アプリケーションは環境変数で接続情報を受け取る。
- 接続文字列、DB ユーザー、パスワードを Git に保存しない。
- スキーマ変更は Flyway migration をアプリケーション起動時またはデプロイ工程で適用する。

## 2. 必須設定

| 環境変数 | 用途 |
|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | 接続先 |
| `DB_USER` / `DB_PASSWORD` | 最小権限の接続ユーザー |
| `DB_SSLMODE` | 本番での TLS 接続設定 |
| `APP_SESSION_SECRET` | セッション Cookie 署名・暗号化に使う秘密値 |

## 3. データ保護

- パスワードは `users.password_hash` に BCrypt ハッシュだけを保存する。
- セッションはランダム値そのものを保存せず、`user_sessions.token_hash` を保存する。
- 添付は DB BLOB に置かず、暗号化対応ストレージのキーを `order_attachments.storage_key` に保存する。
- バックアップは日次、復元試験は少なくとも四半期ごとに実施する。
