# 基盤基本設計 — アーキテクチャ

## 1. 初版の技術決定

| 領域 | 採用 |
|---|---|
| フロントエンド | React + TypeScript + Vite |
| バックエンド | Spring Boot、MyBatis、Bean Validation |
| DB | PostgreSQL 16、Flyway |
| 認証 | `login_id` + BCrypt パスワード、サーバー側セッション Cookie |
| ファイル | S3 互換オブジェクトストレージ + マルウェアスキャン |
| 非同期 | DB ジョブテーブルをポーリングする Worker |
| メール | SMTP/メールサービスによる業務通知 |

## 2. モジュール境界

- `auth`: ログイン、ログアウト、セッション、ロック。
- `user / customer / product`: マスタ管理。
- `order`: 注文・明細・税計算・状態履歴。
- `approval`: 承認経路スナップショット、承認、差戻し。
- `attachment`: 保存前検証、スキャン状態、認可付き配信。
- `report`: 集計、PDF、CSV 出力ジョブ。
- `common`: 監査、例外、日時、ID、権限制御。

## 3. 横断ルール

- API の先頭は `/api`。認証済み Cookie と CSRF トークンを変更操作で必須にする。
- 金額・税・承認・権限をフロントエンドだけで決定しない。
- 外部副作用は DB 更新のコミット後に実行し、失敗は通知/出力ジョブに記録して再試行する。
- 物理 DB の唯一の正は `../共通/spec-06-db-physical.yaml`、API の唯一の正は `../共通/spec-07-api-list.yaml` とする。
