<!-- devtool:file-region id="document-header" -->
# Flyway マイグレーション規約（SQL ファイル作成・命名規約）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="rules.postgresql.flyway-migration-rule.s01" -->
## メタデータ
- version: 1.0.0
- target_db: PostgreSQL（バージョンは `基本設計/共通/spec-01-system-architecture-ui.md` §2.3 に従う）
- flyway_version: `基本設計/共通/spec-01-system-architecture-ui.md` §2.2 に従う（Spring Boot BOM で管理）
- created: 2026/06/22
- author: nebulasoft Co., Ltd
- status: approved

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.flyway-migration-rule.s02" -->
## 利用方法
- DB 変更用のマイグレーションスクリプトを作成する際は、本規約を遵守しなければならない。AI がマイグレーションスクリプトを生成する際は、本ファイルを Prompt に含めること。
- 本ドキュメントは機械的に判定可能な規則として記述する。例外は `EXCEPTION:` タグで明示する。
- 上位設計：[`基本設計/データベース/spec-db-migration-ui.md`](../基本設計/データベース/spec-db-migration-ui.md)、[`基本設計/基盤/spec-04-common-component-ui.md`](../基本設計/基盤/spec-04-common-component-ui.md) §10（基盤側統合）。機械可読契約：[`基本設計/基盤/インタフェース仕様/spec-project-structure.yaml`](../基本設計/基盤/インタフェース仕様/spec-project-structure.yaml)・[`基本設計/基盤/インタフェース仕様/spec-09-resource-file.yaml`](../基本設計/基盤/インタフェース仕様/spec-09-resource-file.yaml)。
- DDL 物理設計の正本：[`基本設計/共通/spec-06-db-physical.yaml`](../基本設計/共通/spec-06-db-physical.yaml)。カラム/型/主キーの規約：[`spec-database-rule.md`](spec-database-rule.md)。
- **実行前の SQL 静的チェック（SQLFluff）：[`spec-sql-lint-rule.md`](spec-sql-lint-rule.md)**——`V*`/`R__`/`setup/*.sql` は Flyway/docker の実行前に本 lint 規約を通過しなければならない。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.flyway-migration-rule.s03" -->
## 1. ファイルの配置（唯一）

- マイグレーションスクリプトは一律 `ddmsaa-backend/infrastructure/src/main/resources/db/migration/` に配置する。
- 当該ディレクトリは `infrastructure` jar にパッケージ化され、`auth-service` / `business-service-core` で共有される（同一ソース、分岐させない）。
- `EXCEPTION:` テスト/環境専用データは本ディレクトリに配置**しない**（§5 参照）。

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.flyway-migration-rule.s04" -->
## 2. 命名規約

### 2.1 バージョン式マイグレーション（一回限り）
<!-- rule: mig.version-file-naming -->
```
V<version>__<description>.sql
```
- 接頭辞は `V`（大文字）。
- `<version>`：単調増加する整数バージョン。**本プロジェクトでは `V1` = 初期移行データ**（Flyway が最初に実行するマイグレーション、§4 参照）とし、以降のテーブル構造変更は `V2`、`V3`…とする。例：`V1`、`V2`、`V10`。
  - `EXCEPTION:` 厳密な順序付けが必要な場合はドット区切りバージョン `V2.1` を使用してよいが、**同一の変更バッチ内で**整数とドット区切りを混在させない。
- 区切り文字：バージョンと説明の間は**アンダースコア 2 つ `__`**（アンダースコア 1 つは不可）。
- `<description>`：`snake_case`（小文字 + アンダースコア）、動詞始まりで変更内容を簡潔に記述する。例：`add_user_nickname`、`alter_change_requests_add_index`。
- ファイル拡張子：`.sql`（小文字）。
- 完全な例：`V2__add_user_nickname.sql`、`V3__create_audit_logs_table.sql`。

### 2.2 反復可能マイグレーション（repeatable）
```
R__<description>.sql
```
- 接頭辞は `R__`（バージョン番号なし）。
- 用途：checksum が変化するたびに再実行するスクリプト——**ビュー、関数、トリガー、参照マスタデータの upsert** など冪等なオブジェクト。
- すべてのバージョン式マイグレーションの後に実行される。
- `description` は 2.1 と同様（snake_case）。例：`R__seed_reference_master.sql`、`R__view_user_summary.sql`。

### 2.3 禁止事項
- ファイル名にスペース、全角文字、大文字の説明を含めない。
- 使用済みのバージョン番号を再利用しない（`V1` は初期移行データで使用済み、§4 参照）。

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.flyway-migration-rule.s05" -->
## 3. 作成規約（スクリプトの内容）

<!-- rule: mig.applied-script-immutable -->
| 番号 | ルール |
|------|------|
| R-01 | **適用済みの `V*` は決して修正しない**（空白/コメントを含む）。Flyway は checksum を検証するため、変更すると起動失敗となる。修正は一律、より高いバージョンの `V*` を新規追加して行う。 |
| R-02 | **1 マイグレーション = 1 目的**：1 ファイルは 1 つの変更（1 テーブル/関連カラム群）に絞り、追跡とロールバックを容易にする。 |
| R-03 | **できる限り冪等にする**：DDL は `IF NOT EXISTS` / `IF EXISTS`（`ADD COLUMN IF NOT EXISTS` など）を用いる。`R__` のデータスクリプトは `INSERT ... ON CONFLICT DO NOTHING` または `UPDATE` を用いる。 |
| R-04 | **DB 作成・ユーザー作成・権限付与を行わない**：`CREATE DATABASE/ROLE`、`GRANT` は bootstrap（`データベース/setup/00_bootstrap.sql`、`01_schema_grants.sql`）の対象であり、マイグレーションに含めない。 |
| R-05 | **schema は明示的に `public`**：オブジェクトに schema 接頭辞を付けない場合は `default-schema=public` に依存する。schema をまたぐ操作は明示的に修飾しなければならない。 |
| R-06 | **物理設計の正本に従う**：カラム名・型・主キー・列挙コードテーブル・暗号化カラムは `基本設計/共通/spec-06-db-physical.yaml` と `開発規約/spec-database-rule.md` に一致させる。 |
| R-07 | **トランザクション**：PostgreSQL では Flyway は既定で各マイグレーションをトランザクション内で実行する（DDL のトランザクション安全）。非トランザクション操作（`CREATE INDEX CONCURRENTLY` など）が必要な場合は、単独のマイグレーションファイルとし、その旨を明記する。 |
| R-08 | **トリガー/監査項目**：テーブルを新規追加する際は、共通列と `updated_at` トリガーを `開発規約/spec-database-rule.md` §4.3 に従って追加しなければならない。 |
| R-09 | **文字コード/改行**：`開発規約/spec-backend-coding-rule.md` §12 に従う。 |
| R-10 | **ロールバック方針**：自動 undo には依存しない（コミュニティ版には存在しない）。ロールバックが必要な場合は**逆方向の `V*` を新規追加**する（例：`V5__revert_v4_xxx.sql`）。履歴スクリプトは削除・変更しない。 |
| R-11 | **コメント**：ファイル先頭行に `-- V<n>__<desc>：<目的・関連チケット>` の形式で意図を記載する。 |

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.flyway-migration-rule.s06" -->
## 4. バージョンベースライン（baseline）の取り決め

- **schema（14 テーブル）は機構A = docker で構築する**（`setup/10_schema.sql`）。**Flyway のバージョンには含めない**。
- Flyway は `baseline-on-migrate=true` / `baseline-version=0` で設定する。既存（docker で schema 構築済み）の DB を初めて接続する際に `baseline(0)`（空のベースライン）を記録し、その後 **`V1`** から適用する。
- したがって **`V1` = 初期移行データ**（Flyway が最初に実行するマイグレーション）とし、以降のテーブル構造変更は **`V2` 以降**とする。

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.flyway-migration-rule.s07" -->
## 5. 管理対象 / 対象外

| 区分 | `db/migration` に含めるか | 配置先 |
|------|:--:|------|
| schema の初期構築（14 テーブル/関数/シーケンス/トリガー） | `EXCEPTION:` 含めない | docker 初期化 `setup/10_schema.sql`（機構A、Flyway のバージョンには含めない） |
| 初期移行データ（システムの初期業務データ） | ✅ 含める（V1） | `V1__initial_data_migration.sql` |
| テーブル構造の後続変更（カラム追加/制約変更/インデックス追加/新規テーブル） | ✅ 含める | `V2+__*.sql` |
| ビュー/関数/参照マスタデータの upsert | ✅ 含める | `R__*.sql` |
| DB/ユーザー作成・権限付与（bootstrap） | `EXCEPTION:` 含めない | `setup/00_bootstrap.sql` / `01_schema_grants.sql` |
| 開発サンプル・単体(UT)テストデータ（`20_seed` のサンプル、`21_*`〜`26_*`） | `EXCEPTION:` 含めない | 手動 psql / test profile。**主マイグレーションディレクトリには含めない**（本番を汚染するため） |

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.flyway-migration-rule.s08" -->
## 6. レビュー・提出ルール
- `V*`/`R__` を新規追加する場合はレビューを経ること：命名の適合、checksum リスク（履歴を変更しない）、冪等性、`spec-06-db-physical.yaml` との整合。
- 複数人での協働ではバージョン番号の衝突を避ける：マージ前に `db/migration/` 配下に同番号がないことを確認し、衝突する場合はバージョンを繰り下げる。
- 物理設計の変更は、**先に** `spec-06-db-physical.yaml` + `データベース` に反映してから、それに基づいてマイグレーションを作成する（SSOT の一致を保つ）。

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.flyway-migration-rule.s09" -->
## 7. 例

`V2__add_user_nickname.sql`
```sql
-- V2__add_user_nickname：ユーザーにニックネーム列を追加（チケット #xxx）
ALTER TABLE users ADD COLUMN IF NOT EXISTS nickname VARCHAR(50);
COMMENT ON COLUMN users.nickname IS 'ニックネーム';
```

`R__seed_reference_master.sql`（参照マスタデータ、冪等）
```sql
-- R__seed_reference_master：申請種別時限マスタ（全環境共通）
INSERT INTO request_type_limits (request_type, review_limit_days, process_limit_days) VALUES
  (1, 5, 20),
  (2, 5, 5)
ON CONFLICT DO NOTHING;
```
<!-- devtool:endblock -->
