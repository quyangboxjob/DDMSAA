<!-- devtool:file-region id="document-header" -->
# データ辞書：命名マッピングと型規約

> 全テーブルの項目（列）について、論理名 → DB 物理名 → Backend/API 名 → 型の対応を整理し、バックエンド/API 実装の命名を統一する。
> 項目の定義（DI-xxx）の正は [`spec-13-data-dictionary-ui.md`](../共通/spec-13-data-dictionary-ui.md)、物理名・テーブル構成の正は [`spec-06-db-physical-ui.md`](../共通/spec-06-db-physical-ui.md)。
> 命名規約は [`spec-naming-rule.md`](../../開発規約/spec-naming-rule.md) と [`spec-database-rule.md`](../../開発規約/spec-database-rule.md) に従う。

（文書ヘッダは `spec-00-doc-standard-ui.md` を参照）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.database.postgresql.data-dictionary-mapping-ui.s01" -->
## 1. 命名規則

| 層 | スタイル | 例（`employment_type` の場合） |
|---|---|---|
| DB 物理名 | snake_case | `employment_type` |
| Backend（Entity/DTO のフィールド、変数） | camelCase | `employmentType` |
| API（JSON のリクエスト/レスポンスキー、クエリパラメータ） | camelCase（Backend と同一） | `"employmentType"` |

- **機械的変換**：Backend フィールド名 = API JSON キー = DB 物理名からアンダースコアを除いた camelCase。
- **日時**：DB は `timestamptz`。API は `+09:00` オフセット付き ISO 8601 文字列（`"2026-01-01T00:00:00+09:00"`）。日付のみは `"2026-01-01"`。
- **真偽値**：DB 列名 `is_xxx` → API `isXxx`（`boolean`）。
- **列挙**：DB は INT コード（T07）→ Java `Enum` → API 値は UPPER_SNAKE_CASE の文字列。値の定義は `開発規約/spec-data-dictionary-project-temp.md` の T07 を正とする。
- **Principle 3 の変換**（`externalId` → `id` など）が登録されている項目は、プロジェクトデータ辞書のホワイトリストを優先する。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.data-dictionary-mapping-ui.s02" -->
## 2. 型マッピング

| 基礎型/用途 | DB 型 | Java 型 | API JSON 型 | 備考 |
|---|---|---|---|---|
| 業務キー（文字列） | `varchar(n)` | `String` | string | 採番規則は `spec-00-doc-standard-ui.md` を参照 |
| 数値（整数） | `integer` / `bigint` | `Integer` / `Long` | integer | |
| 金額・小数 | `numeric(p,s)` | `BigDecimal` | string または number | 丸め規則は `spec-database-rule.md` を参照 |
| 真偽値 | `boolean` | `Boolean` | boolean | |
| 日付 | `date` | `LocalDate` | string(date) | |
| 日時 | `timestamptz` | `OffsetDateTime`（または `Instant`） | string(date-time) | `+09:00` オフセット付き。表示層の共通ユーティリティで書式化 |
| 列挙（T07） | `integer` + コードテーブル FK | `Enum` | string | DB コード ↔ API 値は T07 に従う |
| 長文 | `text` | `String` | string | |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.data-dictionary-mapping-ui.s03" -->
## 3. 共通項目（全テーブル共通）

| 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | 備考 |
|---|---|---|---|---|
| 主キー | `id` | `id` | string | テーブルごとの主キー方針は `spec-06-db-physical-ui.md` を参照 |
| 作成日時 | `created_at` | `createdAt` | string(date-time) | ISO 8601（`+09:00`） |
| 作成者 | `created_by` | `createdBy` | string | 操作者の業務 ID。システム処理は `SYSTEM` |
| 更新日時 | `updated_at` | `updatedAt` | string(date-time) | ISO 8601（`+09:00`） |
| 更新者 | `updated_by` | `updatedBy` | string | |
| 削除日時 | `deleted_at` | `deletedAt` | string(date-time) \| null | 論理削除。通常は API で公開しない |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.data-dictionary-mapping-ui.s04" -->
## 4. テーブル別マッピング

> テーブルごとに節を立てて記載する。DI-xxx は `spec-13-data-dictionary-ui.md` の項目 ID と一致させる。

### 4.1 {{ テーブル物理名 }}（{{ 論理名 }}）

| データ項目 ID | 論理名 | DB 物理名 | Backend / API（camelCase） | API JSON 型 | DB 型 | 備考 |
|---|---|---|---|---|---|---|
| DI-0xx | {{ 論理名 }} | `{{ physical_name }}` | `{{ camelName }}` | string | varchar(100) | |
<!-- devtool:endblock -->
