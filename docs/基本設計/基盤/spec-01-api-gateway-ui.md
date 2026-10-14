# 基盤基本設計 — HTTP API 入口

## 1. 役割

初版では独立した API Gateway を設けない。Spring Boot アプリケーションが `/api` 配下の HTTP API の入口となる。認証、CSRF、入力サイズ制限、相関 ID、例外の標準応答を Filter/Interceptor で一元化する。

## 2. 通信ルール

| 項目 | 決定 |
|---|---|
| 認証 | `DDMSAA_SESSION` の HttpOnly/Secure/SameSite=Lax Cookie |
| CSRF | 変更系メソッドで `X-CSRF-Token` を必須 |
| Content-Type | JSON は `application/json`、添付は `multipart/form-data` |
| エラー | `{ code, message, requestId }`。認証失敗時は利用者存在を区別しない |
| 監査 | 状態変更、認可拒否、出力、添付ダウンロードを記録 |
| API 正本 | `../共通/spec-07-api-list.yaml` |

## 3. 制限

- 本番は HTTPS のみ。
- JSON は 1 MiB、添付は 10 MiB を上限とする。
- 添付のダウンロードはファイル ID だけでストレージへ直行させず、API が再認可する。
- ログインはアカウント単位の失敗回数と IP 単位のレート制限を適用する。
