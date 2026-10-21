<!-- devtool:file-region id="document-header" -->
# DB マイグレーション基本設計（Flyway）

> DDMSAAの DB スキーマ進化（マイグレーション）方式の基本設計。
> 上位：[`../../基盤/spec-04-common-component-ui.md`](../基盤/spec-04-common-component-ui.md) §10（基盤側 Flyway 統合）。実装詳細（機読 spec）：[`../../基盤/インタフェース仕様/spec-project-structure.yaml`](../基盤/インタフェース仕様/spec-project-structure.yaml)・[`../../基盤/インタフェース仕様/spec-09-resource-file.yaml`](../基盤/インタフェース仕様/spec-09-resource-file.yaml)。
> 規約：[`../../../開発規約/spec-flyway-migration-rule.md`](../../開発規約/spec-flyway-migration-rule.md)。

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.database.postgresql.db-migration-ui.s01" -->
## ドキュメント情報

| 項目 | 内容 |
|------|------|
| ドキュメント名称 | DB マイグレーション基本設計（Flyway） |
| バージョン / ステータス | V1.0 / 確定（実装・検証済み） |
| 作成日 | 2026-06-22 |
| 対象 DB | PostgreSQL 16（`ddmsaa` / schema `public`） |
| マイグレーションツール | Flyway 11.7.2（Spring Boot BOM 管理） |

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-migration-ui.s02" -->
## 1. 目的・位置づけ

DB の「初期構築」と「構築後の継続的なスキーマ変更」を**別機構**で扱い、後者を Flyway によりバージョン管理する。
これにより、稼働中・データ投入済みの DB に対しても**破壊せず増分で**スキーマを進化させられる。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-migration-ui.s03" -->
## 2. 二つの実行機構と責務分担

<!-- rule: mig.two-mechanism-split -->
| 機構 | 担当 | 実行タイミング | 実行回数 | 対象 |
|------|------|--------------|---------|------|
| **機構A：docker 初期化** | `docker/postgres/init/00-setup.sh` → `setup/{00_bootstrap,01_schema_grants,10_schema,20_seed}.sql` | docker 初回起動かつデータボリュームが空のときのみ | **1 回**（以降スキップ。再実行は `down -v` で空に戻すしかない） | DB/ユーザー作成・**14 表の初期 schema**・主データ・開発サンプル |
| **機構B：Flyway** | 各サービス（auth-service / business-service-core）起動時、または `flyway:migrate` | **アプリ起動の都度**（未適用分のみ適用） | 何度でも（適用済みはスキップ） | **初期移行データ（V1）**・初期構築以降のスキーマ変更（V2 以降）・参照マスタ（R__） |

> 機構Aは「最初に一回だけ作る（DB/ユーザー＋schema）」。機構Bは「schema 構築後の初期データ（V1）と以降の変更（V2+）を当てる」。schema は Flyway のバージョン管理対象外、**baseline=version 0**（§4）。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-migration-ui.s04" -->
## 3. 設計方針（本システムの選択）

- **schema 初期構築は機構Aが行い、Flyway はスキーマを新規作成しない**（schema は Flyway のバージョン管理対象外）。
- Flyway は機構Aで構築済みの DB を **baseline=0（空基線）で認領**し、**`V1`（初期移行データ）以降**を管理する。
<!-- rule: mig.script-location -->
- マイグレーション SQL は `infrastructure` モジュールに一元配置し、両サービスから共有（同一スクリプト＝分岐しない）。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-migration-ui.s05" -->
## 4. baseline 戦略（既存 DB への導入）

<!-- rule: mig.baseline-zero-v1-initial-data -->
| 設定 | 値 | 意味 |
|------|----|------|
| `baseline-on-migrate` | `true` | 履歴テーブルが無い非空 DB に対し自動で baseline を記録 |
| `baseline-version` | `0` | baseline=空。schema は機構A担当でバージョン管理対象外 → Flyway は `V1` から適用 |

- **既存（docker 構築済み）DB**：履歴無 → `baseline(0)` を記録し、続けて **`V1`（初期移行データ）以降**を適用。schema・既存資産は不変。
- **schema は機構A（docker）が作る前提**。空 DB に Flyway だけ当てる運用はしない（V1 はデータ投入で、表は機構Aが用意）。
- **`V1` = 初期移行データ**（Flyway が最初に実行するマイグレーション）。以降の表構造変更は `V2` から。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-migration-ui.s06" -->
## 5. トポロジ（誰が実行するか）

- DB は単一 `ddmsaa`。接続するのは **auth-service(8081)** と **business-service-core(8082)** の 2 サービス（api-gateway は DB 非接続）。
- 両サービスとも `spring.flyway.enabled=true`。スクリプトは `infrastructure` jar 同梱で同一。
- 同時起動時も Flyway の advisory lock + 共有履歴により安全（先発が適用、後発は最新を確認し no-op）。
- 本番厳格運用に移行する際は「マイグレーションを独立ステップ（`flyway:migrate` を CI/デプロイで実行、各サービスは `enabled:false`）」へ切替可能（詳細設計 §6）。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-migration-ui.s07" -->
## 6. 管理対象 / 非対象

| 対象 | Flyway 管理 |
|------|:--:|
| 初期 schema（31 表/関数/シーケンス/トリガー） | ✕（機構A・バージョン管理対象外） |
| 初期移行データ（初期業務データ） | ○（V1） |
| 構築後のスキーマ変更（増列/索引/新表/制約） | ○（V2+） |
| ビュー/関数/参照マスタ upsert | ○（R__） |
| DB/ユーザー作成・権限付与（bootstrap） | ✕（機構A・接続前提のため対象外） |
| 開発サンプル・単体(UT)テストデータ | ✕（環境専用・手動 psql / test profile） |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-migration-ui.s08" -->
## 7. 環境別の振る舞い

| 環境 | 初期構築 | Flyway |
|------|---------|--------|
| ローカル（docker） | 機構A（`down -v` で再構築） | 起動時 baseline → V2+ |
| CI / テスト | 機構A 相当でスキーマ用意 | 同上。テストデータは別途 |
| 本番（将来） | スキーマは Flyway 全量 or 別途構築 | 独立ステップ実行推奨（§5） |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-migration-ui.s09" -->
## 8. 関連成果物

| 成果物 | 役割 |
|--------|------|
| [`基本設計/基盤/インタフェース仕様/spec-project-structure.yaml`](../基盤/インタフェース仕様/spec-project-structure.yaml)・[`基本設計/基盤/インタフェース仕様/spec-09-resource-file.yaml`](../基盤/インタフェース仕様/spec-09-resource-file.yaml) | 機読契約：依存(pom)・plugin・`spring.flyway` 設定・`db/migration` 配置 |
| [`基本設計/基盤/spec-04-common-component-ui.md`](../基盤/spec-04-common-component-ui.md) §10 | 基盤側 Flyway 統合（散文・実行シーケンス） |
| [`開発規約/spec-flyway-migration-rule.md`](../../開発規約/spec-flyway-migration-rule.md) | SQL ファイル作成・命名規約 |
| `migrations/migration-version.yaml` | **設計記録/台帳**（実際に効くのは `infrastructure/.../db/migration/` のスクリプト） |
| `setup/10_schema.sql` | 機構A の初期 schema（Flyway バージョン管理対象外。baseline=0 として扱われ V1 から適用） |
<!-- devtool:endblock -->
