<!-- devtool:file-region id="document-header" -->
# SQL Lint 規約（Flyway 実行前 静的検査）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="rules.postgresql.sql-lint-rule.s01" -->
## メタデータ
- version: 1.0.0
- target_db: PostgreSQL（バージョンは `基本設計/共通/spec-01-system-architecture-ui.md` §2.3 に従う）
- linter: SQLFluff（Python 製 SQL リンター）
- created: 2026/07/07
- author: nebulasoft Co., Ltd
- status: approved

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.sql-lint-rule.s02" -->
## 利用方法

<!-- rule: lint.pre-execution-gate -->
- Flyway で実行する SQL（`V*`/`R__` マイグレーション + `データベース/setup/*.sql`）を**実行前**に静的検査するための規約。
- **SQLFluff のみを使用する**：構文/スタイルの自動検査（大文字小文字・インデント・命名の一貫性・行長など）。**マイグレーションの安全性**（ロック・破壊的変更）は本規約の対象外とし、`spec-flyway-migration-rule.md` R-01/R-10 の人手によるレビューで担保する（将来の Squawk 導入は GAP として保留）。
- 本ドキュメントは機械的に判定可能な規則として記述する。例外は `EXCEPTION:` タグで明示する。
- 上位規約：[`spec-flyway-migration-rule.md`](spec-flyway-migration-rule.md)（作成規約 R-01〜R-11）、[`spec-database-rule.md`](spec-database-rule.md)（カラム/型/命名）、[`spec-postgresql-column-types-rule.md`](spec-postgresql-column-types-rule.md)（型の正本）。
- DDL 物理設計の正本：[`基本設計/共通/spec-06-db-physical.yaml`](../基本設計/共通/spec-06-db-physical.yaml)。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.sql-lint-rule.s03" -->
## 1. 適用範囲

| 区分 | パス | Lint 対象か |
|------|------|:--:|
| Flyway バージョン式マイグレーション | `ddmsaa-backend/infrastructure/src/main/resources/db/migration/V*__*.sql` | ✅ |
| Flyway 反復可能マイグレーション | 同上 `R__*.sql` | ✅ |
| 機構A schema 構築 | `基本設計/データベース/setup/*.sql`（`00_bootstrap`〜`10_schema`） | ✅ |
| 初期/参照データ | `setup/20_seed.sql`・`R__seed_*.sql` | ✅ |
| テスト専用 seed | `setup/21_*`〜`26_*_test_seed.sql` | `EXCEPTION:` 任意（本番非対象。CI では除外可） |
| クエリ設計 | `基本設計/データベース/queries/*.sql`（設計参照用） | `EXCEPTION:` 任意（実行物ではない） |

- **原則**：Flyway が実際に実行する SQL と、docker init が実行する schema 構築 SQL は**すべて必須**とする。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.sql-lint-rule.s04" -->
## 2. ツールと設定

- **ツール**：SQLFluff 4.x（検証時 4.2.2）（`lint` = 検査、`fix` = 自動修正）。
- **dialect**：`postgres`。
- **templater**：`raw`（Flyway/psql は素の SQL。Jinja/dbt テンプレートは不使用）。
- **設定の正本**：プロジェクトルートの `.sqlfluff`（§5）。すべての lint はこの単一ファイルの設定に従う（SSOT）。
- **導入**：`pip install sqlfluff`（`requirements-dev.txt` / CI にバージョン固定で記載）。
- **追加可能なルールの全集一覧**：[`spec-sqlfluff-rules-catalog.md`](spec-sqlfluff-rules-catalog.md)（SQLFluff 全 75 ルールのカタログ + 本プロジェクトでの採用状況）。制限を追加する際はまず一覧から選び、`/spec-sql-lint add-rule` で §3 + `.sqlfluff` の両方に反映する。

---

<!-- rule: lint.rule-set -->
<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.sql-lint-rule.s05" -->
## 3. Lint ルールセット

SQLFluff 4.x のルール ID で管理する。方針は既存の `setup/10_schema.sql`・`V1` の実態に合わせて確定した（**キーワード/型は大文字、識別子/関数は小文字**）。全 5 SQL（`10_schema`/`00_bootstrap`/`01_schema_grants`/`20_seed`/`V1`）で **違反 0 件で通過済み**（2026/07/07 実測）。

### 3.1 有効（enforce）

| 番号 | ルール | SQLFluff ルール | 設定 | 対応する上位規約 |
|------|------|---------------|------|--------------|
| **L-01** | SQL **キーワードは大文字**（`CREATE TABLE`/`NOT NULL`/`DEFAULT`） | `capitalisation.keywords`（CP01） | `capitalisation_policy=upper` | 既存の `10_schema.sql` のスタイルに合わせる |
| **L-02** | 識別子（テーブル/カラム名）は**小文字**（snake_case） | `capitalisation.identifiers`（CP02） | `extended_capitalisation_policy=lower` | spec-database-rule / spec-06-db-physical |
| **L-03** | 関数名は**小文字**（`now()`）。keyword-like な特殊値（`CURRENT_TIMESTAMP`/`CURRENT_DATE` など）は大文字のまま**免除** | `capitalisation.functions`（CP03） | `extended_capitalisation_policy=lower` + `ignore_words` | Postgres の慣例（括弧なしの特殊値はキーワード扱い） |
| **L-04** | データ型は大文字（`VARCHAR`/`TIMESTAMPTZ`/`INT`） | `capitalisation.types`（CP05） | `extended_capitalisation_policy=upper` | spec-postgresql-column-types-rule |
| **L-05** | 行長 ≤ **200** | `layout.long_lines`（LT05） | `max_line_length=200` | 多カラム INSERT のデータ行は本来的に長い（最長 約184）。DDL は上限を大きく下回る |
| **L-06** | カンマ位置の一貫性（trailing） | `layout.commas`（LT04） | `line_position=trailing` | 可読性 |
| **L-07** | キーワードを識別子として使用することを禁止。`role`（users のカラム名・意図的）は免除 | `references.keywords`（RF04） | `ignore_words=role` | データ辞書 DI-xxx |
| **L-08** | 解析可能性・末尾改行など、既定の構造/衛生チェック | 既定で有効なルール群 | 既定 | コーディング衛生 |

### 3.2 無効（exclude）——理由付き

手書き SQL（DDL + 手動メンテナンスのデータ）に対して、**レイアウト/空白**をリンターに強制させない方針とする。以下は `exclude_rules` で明示的にオフにする。

| SQLFluff ルール | 無効化の理由 |
|---------------|----------|
| `layout.spacing`（LT01） | `VALUES`/等号の**手作業によるカラム揃え**（複数スペース）を許可する |
| `layout.indent`（LT02） | `VALUES` 行の**手作業によるインデント**を許可する |
| `layout.select_targets`（LT09） | `INSERT...SELECT` の**コンパクトな 1 行**での射影を許可する |
| `layout.keyword_newline`（LT14） | `FROM ... WHERE ...` の 1 行記述を許可する |
| `aliasing.expression`（AL03） | `INSERT...SELECT` の射影カラムは、対象カラム名が INSERT 側で既定 → `AS` 別名は不要 |
| `PG01`（index/FK ロック安全） | **マイグレーションのロック安全性は本 lint の対象外**（§7。人手レビュー / 将来の Squawk）。`10_schema` は空 DB への初期構築であり、`CONCURRENTLY` は適用できず誤検知となる |

- **オブジェクト名/列挙コードテーブル/型の正しさ**（VARCHAR(10) 採番主キー・コードテーブル + 外部キーの列挙など）は SQLFluff の管轄外。`spec-database-rule` + `spec-06-db-physical.yaml` との整合は、マイグレーション生成 skill（`spec-scaffold-db`/`spec-db-migration`）のゲート（DS6/MG6）で担保する。SQLFluff は**スタイル/構文のみ**を対象とする。
- 文字コード/改行は本 lint の対象外とし、`開発規約/spec-backend-coding-rule.md` §12 に従う。
- `10_schema.sql`（約30KB）は既定の 20000 バイトのスキップ上限を超えるため、`large_file_skip_byte_limit=0` により**必ず検査する**（未設定の場合は無言でスキップされ、未検査となる）。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.sql-lint-rule.s06" -->
## 4. 実行とゲート

### 4.1 ローカル（マイグレーション作成時）

Windows 日本語環境では UTF-8 の強制が必要（`.sqlfluff` の paths/日本語ファイル名対策）：
```bash
# 検査（生成/編集した SQL に対して）
PYTHONUTF8=1 python -m sqlfluff lint ddmsaa-backend/infrastructure/src/main/resources/db/migration/

# 自動修正（大文字小文字など。※下記の注意を参照）
PYTHONUTF8=1 python -m sqlfluff fix ddmsaa-backend/infrastructure/src/main/resources/db/migration/V<n>__<desc>.sql
```

- `sqlfluff fix` は**大文字小文字/カンマ位置**など安全な整形に留める。**ロジックは変えない**。
- ⚠️ **本規約はインデント(LT02)/空白(LT01)を無効化しているため、`fix` が「改行を追加する系」の是正（LT09/LT14 等。本規約では無効だが他ルールからの波及時）を行うと、インデントを補正できず、行頭詰めの崩れた体裁になる**。手書きのデータスクリプトは原則**手動修正**を推奨する（実測で確認済み）。
- **適用済みの `V*` は lint 違反であっても修正禁止**（`spec-flyway-migration-rule` R-01：checksum 保護）。違反は新規 `V*` で是正する。

### 4.2 skill ゲートへの組み込み（実装済み）
- `spec-db-migration` Step 4 の準備完了ゲートに **MG-LINT** を設ける：生成/修正した `V*`/`R__` は `sqlfluff lint` を通過しなければならない（未通過の場合は停止・報告）。
- `spec-scaffold-db` Step 5 の DDL ゲートに **DS-LINT** を設ける：生成した `setup/*.sql`（`00`/`01`/`10`/`20`）は `sqlfluff lint` を通過しなければならない。
- 両ゲートとも、sqlfluff 未導入または設定ファイル欠落の場合は、先に `/spec-sql-lint init` を促し、lint をスキップして納品しない。
- **適用済みの `V*` は lint 対象外**（R-01 を優先）。違反は例外として記録するか、新規 `V*` で是正する。

### 4.3 CI（推奨）
- PR で `db/migration/` `データベース/setup/` 配下の SQL に変更があった場合、`sqlfluff lint` が失敗したら CI も失敗とする。
- テスト専用 seed（`21_*`〜`26_*`）は `.sqlfluffignore` で除外してよい。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.sql-lint-rule.s07" -->
## 5. 設定の正本（`.sqlfluff`）

プロジェクトルートに配置する。すべての lint はこのファイルに従う（SSOT）。

> ⚠️ INI は日本語ロケール（cp932）で読み込まれるため、`.sqlfluff` 内の**コメントは ASCII のみ**とする（マルチバイト文字は `UnicodeDecodeError` を引き起こす）。

```ini
[sqlfluff]
dialect = postgres
templater = raw
# 0 = no skip: 10_schema.sql (~30KB) exceeds the 20000-byte default and would otherwise be skipped unchecked
large_file_skip_byte_limit = 0
# 200 (not 120): multi-column INSERT data rows are inherently wide (up to ~184 chars); DDL stays well under
max_line_length = 200
# test-only seed etc. excluded via .sqlfluffignore
# Layout rules disabled: DDL + hand-maintained data inserts; enforce naming/casing/types, NOT whitespace layout.
#   LT01 spacing / LT02 indent : allow hand-aligned VALUES & column alignment
#   LT09 select-target newline / LT14 keyword newline : compact one-line INSERT...SELECT is intended
#   AL03 expression alias : INSERT...SELECT projection needs no AS (target columns already named)
#   PG01 index/FK lock-safety : migration lock-safety out of scope (human review / future Squawk); N/A on empty-DB build
exclude_rules = LT01, LT02, LT09, LT14, AL03, PG01

[sqlfluff:indentation]
tab_space_size = 2
indent_unit = space

[sqlfluff:rules:capitalisation.keywords]
capitalisation_policy = upper

[sqlfluff:rules:capitalisation.identifiers]
extended_capitalisation_policy = lower

[sqlfluff:rules:capitalisation.functions]
extended_capitalisation_policy = lower
# keyword-like special values (no parens) stay UPPER to match keyword style; real functions (now()) still lower
ignore_words = current_timestamp,current_date,current_time,current_user,session_user,localtimestamp

[sqlfluff:rules:capitalisation.types]
extended_capitalisation_policy = upper

[sqlfluff:rules:references.keywords]
# 'role' is an intentional column name (users.role, per data dictionary); valid unquoted in Postgres
ignore_words = role

[sqlfluff:layout:type:comma]
line_position = trailing
```

`.sqlfluffignore`（プロジェクトルート）の例：
```
# 実行物ではない設計参照 SQL
基本設計/データベース/queries/
# 本番非対象のテスト seed
基本設計/データベース/setup/21_*
基本設計/データベース/setup/22_*
基本設計/データベース/setup/23_*
基本設計/データベース/setup/24_*
基本設計/データベース/setup/25_*
基本設計/データベース/setup/26_*
```

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.sql-lint-rule.s08" -->
## 6. 既存スクリプトへの適合（2026/07/07 実施済み）

初回導入時に全対象へ `sqlfluff lint` を実行し、**ルール側をプロジェクトの実態に合わせて調整**して整合させた（スクリプトの大幅な改修は不要だった）。結果：**5 SQL すべて違反 0 件**。

- **スクリプトの変更は 1 箇所のみ**：`setup/20_seed.sql` の `FROM departments d` → `FROM departments AS d`（AL01・明示的な `AS`）。
- **`V1__initial_data_migration.sql` は一切改変なし** → checksum への影響ゼロ（`CURRENT_DATE` 等の keyword-like な特殊値は L-03 の `ignore_words` で免除し、大文字のまま保持）。R-01 と整合する。
- **`10_schema.sql`**（14 テーブル DDL の正本）は `large_file_skip_byte_limit=0` により実際に検査され、`CURRENT_TIMESTAMP`（免除）・`role`（免除）の調整後に違反 0 件となった。
- 以降は、生成/編集のたびに `sqlfluff lint` を通してから commit する。適用済みの `V*` が違反した場合は checksum 保護（R-01）のため修正せず、新規 `V*` で是正する。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.sql-lint-rule.s09" -->
## 7. 対象外（本規約でカバーしないもの）

| 項目 | 担保先 |
|------|--------|
| マイグレーションの安全性（テーブルロック・破壊的 DDL・`NOT NULL` の追加） | `spec-flyway-migration-rule` R-01/R-10 の人手によるレビュー（将来の Squawk 導入は GAP 保留） |
| カラム/型/列挙コードテーブル/主キーの設計整合 | `spec-database-rule` + `spec-06-db-physical.yaml`（skill ゲート DS6/MG6） |
| 文字コード・改行 | `開発規約/spec-backend-coding-rule.md` §12 + `spec-flyway-migration-rule` R-09 |
| SQL の実行結果・データの正当性 | 単体/結合テスト |
<!-- devtool:endblock -->
