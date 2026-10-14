# 基盤基本設計 — 共通コンポーネント

## 1. 共通部品

| 部品 | 責務 |
|---|---|
| `PasswordHasher` | BCrypt のハッシュ・照合 |
| `SessionService` | ランダムセッション生成、ハッシュ保存、失効 |
| `AccessScope` | ADMIN/EMPLOYEE/CUSTOMER のサーバー側データ範囲 |
| `TaxCalculator` | 明細行単位の税額計算・四捨五入 |
| `AuditLogger` | 認証、状態変更、出力、添付アクセスの監査 |
| `NotificationDispatcher` | DB の通知ジョブを送信・再試行 |
| `ExportDispatcher` | 大量 CSV/PDF の生成ジョブ |
| `FileValidator` | MIME、拡張子、サイズ、スキャン結果の検証 |

## 2. セキュリティ約束

- パスワードは BCrypt。ログや監査詳細にパスワード、セッション生値、メール本文の機微情報を残さない。
- Cookie は `HttpOnly`、`Secure`、`SameSite=Lax`。変更操作には CSRF 対策を入れる。
- 添付の公開 URL を恒久発行しない。毎回認可し、`SAFE` 以外を配信しない。
- エラー表示は利用者向け文言と内部原因を分離し、内部原因は requestId で追跡する。

## 3. 監査イベント

`LOGIN_SUCCESS`、`LOGIN_FAILURE`、`ORDER_CREATE`、`ORDER_SUBMIT`、`ORDER_APPROVE`、`ORDER_RETURN`、`ORDER_CANCEL`、`ATTACHMENT_UPLOAD`、`ATTACHMENT_DOWNLOAD`、`EXPORT_CREATE`、`USER_UPDATE` を最低限記録する。
