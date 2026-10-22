# docker：ローカル開発環境

PostgreSQL 16・Redis 7・pgAdmin 4・Nginx を起動する。バックエンド（`./mvnw spring-boot:run`）とフロントエンド（`pnpm dev`）はホストで実行する（`docs/基本設計/共通/spec-01-system-architecture-ui.md` §3）。

## 使い方

```bash
cd docker
cp .env.example .env        # パスワードを自分で決める（.env はコミットしない）
docker compose up -d        # 初回のみ DB を初期化する
docker compose ps           # postgres が healthy になれば完了
```

| サービス | ホストのアドレス | 備考 |
|---|---|---|
| PostgreSQL | `localhost:5434`（DB `ddmsaa`、ユーザー `DB_USER`） | JDBC：`jdbc:postgresql://localhost:5434/ddmsaa?currentSchema=public` |
| Redis | `localhost:6379` | パスワードなし（ローカル専用、127.0.0.1 のみ公開） |
| pgAdmin | `http://localhost:5050` | `.env` の `PGADMIN_EMAIL` / `PGADMIN_PASSWORD` でログイン |
| Nginx | `http://localhost:8000` | `/api/` → gateway（ホスト :8080）、`/` → フロントエンド（ホスト :3000） |
| Mailpit（任意） | SMTP `localhost:1025`、UI `http://localhost:8025` | `docker compose --profile mail up -d`。バックエンドは `MAIL_HOST=localhost`、`MAIL_PORT=1025` |

## DB の初期化（機構A）

データボリュームが空のときだけ、`postgres/init/00-setup.sh` が `docs/基本設計/データベース/setup/` の SQL を順に実行する（読み取り専用でマウント）。

| 順 | ファイル | 内容 |
|---|---|---|
| 1 | `00_bootstrap.sql` | 業務ユーザー・DB の作成（UTF8、`C.UTF-8`、タイムゾーン `Asia/Tokyo`） |
| 2 | `01_schema_grants.sql` | schema 権限・既定権限 |
| 3 | `10_schema.sql` | 31 テーブル（13 コード表 + 18 業務表）・共有関数・採番シーケンス・索引・トリガー |
| 4 | `20_seed.sql` | T07 の 13 コード表と `tax_rates`（Flyway V1 の外部キーが参照するため先に投入） |
| 5 | `21_seed_sample.sql` | 開発用サンプル（顧客・商品・EMPLOYEE / CUSTOMER 利用者・下書き注文）。`SEED_SAMPLE_DATA=false` で省略 |

初期管理者 `A0001` と承認ルールは Flyway `V1`（`/spec-db-migration` で作成）が投入する。それまではサンプルの利用者（`E0001`、`C0001` など）しか存在しない。

作り直し：`docker compose down -v`（ボリュームを削除）→ `docker compose up -d`。

## バックエンドとの接続値

| 用途 | 値 |
|---|---|
| business-service-core | `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`（= `DB_USER`）、`SPRING_DATASOURCE_PASSWORD`（= `DB_PASSWORD`） |
| auth-service | `DB_USERNAME`（= `DB_USER`）、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT` |

## 未確認事項

- Nginx のホストポート（既定 8000）と Mailpit の導入は設計書に記載がない（要確認）。
- 本番・テスト環境の構成は `spec-01` TBD-ARCH-01。このディレクトリはローカル開発専用。
