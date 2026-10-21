<!-- devtool:file-region id="document-header" -->
# 17 データ移行設計

> 現行システム（または Excel・紙台帳など）から本システムへ**業務データを移行する方針・範囲・対応付け・切替手順**を定義する。非機能要件 `spec-11-non-functional-ui.md` §4「移行」の要求を具体化する。
> **帰属：システム全体で唯一 → `共通/`**。移行データの投入**機構**（Flyway `V1` 初期データ）は `データベース/spec-db-migration-ui.md` を正とし、本書では再定義しない。本書は「何を・どこから・どう変換して・いつ切り替えるか」と、移行ではない**初期データ（運用開始に必要な最小データ）の内容**を定める。

（文書ヘッダは `spec-00-doc-standard-ui.md` を参照）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.common.17-data-migration-ui.s01" -->
## 1. 移行方針

| 項目 | 方針 |
|------|------|
| 移行の有無 | **なし**（2026-10-21 確定。新規構築で、現行システムからの業務データ移行は行わない）。将来移行が必要になった場合は、移行対象一覧（移行 ID・移行元・移行先・件数）、項目対応、移行検証、切替スケジュール、切り戻し方針の各節を本書に追加する（旧ひな形は git 履歴の同ファイルを参照） |
| 移行元 | なし |
| 移行方式 | なし |
| 投入機構（初期データ） | Flyway `V1`（初期データ）と `R__`（参照マスタ）。`データベース/spec-db-migration-ui.md` に従う |
| 責任分担 | 初期データの内容：本書（業務側）／ Flyway スクリプトの作成：`spec-db-migration` ／ 環境ごとの値（初期管理者のメール等）：運用担当 |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.17-data-migration-ui.s02" -->
## 2. 初期データ一覧（運用開始に必要な最小データ）

> 業務データの移行ではなく、システムが動くために必要なデータ。Docker 初期化（機構A）は schema と開発用サンプルだけを作り、本表のデータは Flyway で投入する（`spec-db-migration-ui.md` §2）。

| 初期データ ID | 対象テーブル（`TBL-`） | 内容 | 投入方式 | 件数 | 出典 |
|------|------|------|------|------|------|
| INI-01 | `TBL-user-role-codes` ほか 12 の `*_codes` | T07-1〜T07-13 の列挙コード（1〜13 の各コード表。値は T07 を正とする） | `R__`（upsert。再実行しても結果が同じ） | 13 表 | `開発規約/spec-data-dictionary-project-temp.md` T07、`spec-06-db-physical.yaml` `master_data` |
| INI-02 | `TBL-tax-rates` | STANDARD=10.00、REDUCED=8.00、EXEMPT=0.00 | `R__`（upsert） | 3 件 | `spec-06-db-physical.yaml` `master_data`、BR-ORD-004 |
| INI-03 | `TBL-approval-rules` | 初版 1 行：`min_amount=0`、`max_amount=NULL`、`approval_step=1`、`approver_role=1`（ADMIN）、`is_active=TRUE`、`effective_from_date=2026-01-01` | `V1` | 1 件 | `spec-06-db-physical.yaml` `master_data`、BR-APR-001 |
| INI-04 | `TBL-users` | 初期管理者：`user_id=A0001`、`user_name`＝「システム管理者」、`role=1`（ADMIN）、`status=1`（ACTIVE）、`customer_id=NULL` | `V1` | 1 件 | `spec-06-db-physical.yaml` `master_data`、BR-USR-003 |
| INI-05 | `TBL-user-contacts` | `user_id=A0001`、`email`＝環境ごとの値（Flyway プレースホルダー `${initial_admin_email}` で注入。リポジトリに実メールを書かない） | `V1` | 1 件 | 同上 |

> 共通列：`created_by`・`updated_by` は `SYSTEM`（`MetaObjectHandler` は使えない SQL なので `V1`・`R__` の SQL では `'SYSTEM'` を明示する）。`created_at`・`updated_at` は DB の既定値。
> ID 採番：A0001 は固定値で投入する（`users` は DEFAULT 採番なし）。以降の管理者・利用者は `createUser` が採番する（`next_business_id('A'|'E'|'C', 4)` が A0001 の次を返すよう、シーケンスの開始値の扱いは TBD-MIG-02）。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.17-data-migration-ui.s03" -->
## 3. 開発・テスト用サンプルデータ

- 開発用サンプル（顧客・商品・EMPLOYEE・CUSTOMER 利用者・注文など）は Docker 初期化の `setup/20_seed.sql`（機構A。本番には適用しない）に置く。内容は `spec-scaffold-db` 実行時に決める（TBD-MIG-03）。
- テスト用データはテストコードの fixture で用意し、本書の初期データ（INI-01〜05）を前提にしてよい。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.17-data-migration-ui.s04" -->
## 4. 初期データの検証

| 検証観点 | 方法 | 合格基準 |
|------|------|------|
| 件数 | 投入後に件数を確認 | INI-01 の 13 表の件数が T07 の定義と一致、INI-02 が 3 件、INI-03 が 1 件、INI-04・05 が各 1 件 |
| 整合 | 外部キー・CHECK を満たす | 投入でエラーなし（`chk_users_customer_id`、`chk_apr_rules_approver_role` など） |
| 再実行 | `R__` を 2 回実行 | 結果が変わらない（upsert） |
| ログイン | 初期管理者のメールに Magic Link が届き、A0001 でログインできる | ログイン成功（`spec-it-api` の疎通確認でも可） |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.17-data-migration-ui.s05" -->
## 5. 確認事項（TBD）

> 解決済み：TBD-MIG-01（業務データ移行の有無）は「なし」に確定（2026-10-21）。

| # | 事項 |
|---|---|
| TBD-MIG-02 | A0001 を固定投入した後、`next_business_id('A', 4)` が A0002 から始まるようにするシーケンスの扱い（`spec-database-rule.md` §4.1-SEQ） |
| TBD-MIG-03 | 開発用サンプルデータの内容（`spec-scaffold-db` 実行時に決める） |
| TBD-MIG-04 | INI-03（承認ルール）と INI-02（税率）を `V1` と `R__` のどちらに置くかの最終確認（本書は税率＝`R__`、承認ルール＝`V1`） |

<!-- devtool:endblock -->
