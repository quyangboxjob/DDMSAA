# SQLFluff ルール全集一覧（追加可能な制限の完全カタログ）

---

## メタデータ
- version: 1.0.0
- linter: SQLFluff 4.2.2（`sqlfluff rules` 生成時点）
- generated: 2026/07/07
- status: reference（参考・SQLFluff のバージョン更新に追随）

---

## 本ファイルの位置付けと使い方
- 本ファイルは **SQLFluff が適用できるすべての制限（ルール）の完全なカタログ**であり、全 **75 ルール**を収録する。プロジェクトに **SQL の制限を追加**したい場合は、まずここでルール番号を選び、下記の「追加フロー」に進む。
- **ルール採否の正本は [`spec-sql-lint-rule.md`](spec-sql-lint-rule.md) §3**、機械可読な設定の正本はプロジェクトルートの `.sqlfluff` である。本ファイルは**選択肢の一覧**（catalog）であり、設定そのものではない。
- **再生成**（SQLFluff のアップグレード後に本表を更新する）：
  ```bash
  PYTHONUTF8=1 python -m sqlfluff rules
  ```
  ⚠ プロジェクトディレクトリで実行すると **`exclude_rules` のルールが除外される**（表示されるのは全集ではなく「有効セット」）。全集を確認するには、**`.sqlfluff` のないディレクトリ**で実行すること。

## 凡例（本プロジェクトでの状態）
- **✅ 調整**：`.sqlfluff` で**パラメータを明示的に設定**している（7 ルール）——CP01, CP02, CP03, CP05, LT04, LT05, RF04
- **❌ 無効**：`exclude_rules` に登録され、**検査しない**（6 ルール）——AL03, LT01, LT02, LT09, LT14, PG01
- **○ 既定**：**既定で有効**であり、SQLFluff のデフォルト動作に従う（62 ルール）。本プロジェクトは denylist 方式のため、「❌無効」以外は**すべて有効**となる。ただし一部のルール（CV06 セミコロン、CV09 ブロック語など）は**デフォルトでは no-op であり、パラメータを設定して初めて実際に検出する**。

> つまり、「制限を 1 つ追加したい」場合、多くは「無効から有効へ」ではなく、**すでに有効なルールのパラメータを調整する**（例：`CV06 require_final_semicolon=true`）か、**無効にしているルールを再度有効化する**ことになる。

---

## ルール目録（グループ別）

### AL — エイリアス aliasing（10 ルール）

| 状態 | 番号 | ルール名 | 説明 |
|:--:|---|---|---|
| ○ 既定 | AL01 | `aliasing.table` | Implicit/explicit aliasing of table. |
| ○ 既定 | AL02 | `aliasing.column` | Implicit/explicit aliasing of columns. |
| ❌ 無効 | AL03 | `aliasing.expression` | Column expression without alias. Use explicit `AS` clause. |
| ○ 既定 | AL04 | `aliasing.unique.table` | Table aliases should be unique within each clause. |
| ○ 既定 | AL05 | `aliasing.unused` | Tables should not be aliased if that alias is not used. |
| ○ 既定 | AL06 | `aliasing.length` | Enforce table alias lengths in from clauses and join conditions. |
| ○ 既定 | AL07 | `aliasing.forbid` | Avoid table aliases in from clauses and join conditions. |
| ○ 既定 | AL08 | `aliasing.unique.column` | Column aliases should be unique within each clause. |
| ○ 既定 | AL09 | `aliasing.self_alias.column` | Column aliases should not alias to itself, i.e. self-alias. |
| ○ 既定 | AL10 | `aliasing.required` | Derived tables must have an alias. |

### AM — 曖昧性 ambiguous（9 ルール）

| 状態 | 番号 | ルール名 | 説明 |
|:--:|---|---|---|
| ○ 既定 | AM01 | `ambiguous.distinct` | Ambiguous use of 'DISTINCT' in a 'SELECT' statement with 'GROUP BY'. |
| ○ 既定 | AM02 | `ambiguous.union` | 'UNION [DISTINCT\|ALL]' is preferred over just 'UNION'. |
| ○ 既定 | AM03 | `ambiguous.order_by` | Ambiguous ordering directions for columns in order by clause. |
| ○ 既定 | AM04 | `ambiguous.column_count` | Query produces an unknown number of result columns. |
| ○ 既定 | AM05 | `ambiguous.join` | Join clauses should be fully qualified. |
| ○ 既定 | AM06 | `ambiguous.column_references` | Inconsistent column references in 'GROUP BY/ORDER BY' clauses. |
| ○ 既定 | AM07 | `ambiguous.set_columns` | Queries within set query produce different numbers of columns. |
| ○ 既定 | AM08 | `ambiguous.join_condition` | Implicit cross join detected. |
| ○ 既定 | AM09 | `ambiguous.order_by_limit` | Use of LIMIT and OFFSET without ORDER BY may lead to non-deterministic results. |

### CP — 大文字小文字 capitalisation（5 ルール）

| 状態 | 番号 | ルール名 | 説明 |
|:--:|---|---|---|
| ✅ 調整 | CP01 | `capitalisation.keywords` | Inconsistent capitalisation of keywords. |
| ✅ 調整 | CP02 | `capitalisation.identifiers` | Inconsistent capitalisation of unquoted identifiers. |
| ✅ 調整 | CP03 | `capitalisation.functions` | Inconsistent capitalisation of function names. |
| ○ 既定 | CP04 | `capitalisation.literals` | Inconsistent capitalisation of boolean/null literal. |
| ✅ 調整 | CP05 | `capitalisation.types` | Inconsistent capitalisation of datatypes. |

### CV — 規約 convention（12 ルール）

| 状態 | 番号 | ルール名 | 説明 |
|:--:|---|---|---|
| ○ 既定 | CV01 | `convention.not_equal` | Consistent usage of '!=' or '<>' for "not equal to" operator. |
| ○ 既定 | CV02 | `convention.coalesce` | Use 'COALESCE' instead of 'IFNULL' or 'NVL'. |
| ○ 既定 | CV03 | `convention.select_trailing_comma` | Trailing commas within select clause. |
| ○ 既定 | CV04 | `convention.count_rows` | Use consistent syntax to express "count number of rows". |
| ○ 既定 | CV05 | `convention.is_null` | Comparisons with NULL should use "IS" or "IS NOT". |
| ○ 既定 | CV06 | `convention.terminator` | Statements must end with a semi-colon. |
| ○ 既定 | CV07 | `convention.statement_brackets` | Top-level statements should not be wrapped in brackets. |
| ○ 既定 | CV08 | `convention.left_join` | Use 'LEFT JOIN' instead of 'RIGHT JOIN'. |
| ○ 既定 | CV09 | `convention.blocked_words` | Block a list of configurable words from being used. |
| ○ 既定 | CV10 | `convention.quoted_literals` | Consistent usage of preferred quotes for quoted literals. |
| ○ 既定 | CV11 | `convention.casting_style` | Enforce consistent type casting style. |
| ○ 既定 | CV12 | `convention.join_condition` | Use `JOIN ... ON ...` instead of `WHERE ...` for join conditions. |

### JJ — Jinja テンプレート（1 ルール）

| 状態 | 番号 | ルール名 | 説明 |
|:--:|---|---|---|
| ○ 既定 | JJ01 | `jinja.padding` | Jinja tags should have a single whitespace on either side. |

### LT — レイアウト layout（15 ルール）

| 状態 | 番号 | ルール名 | 説明 |
|:--:|---|---|---|
| ❌ 無効 | LT01 | `layout.spacing` | Inappropriate Spacing. L039, L048, L071 |
| ❌ 無効 | LT02 | `layout.indent` | Incorrect Indentation. |
| ○ 既定 | LT03 | `layout.operators` | Operators should follow a standard for being before/after newlines. |
| ✅ 調整 | LT04 | `layout.commas` | Leading/Trailing comma enforcement. |
| ✅ 調整 | LT05 | `layout.long_lines` | Line is too long. |
| ○ 既定 | LT06 | `layout.functions` | Function name not immediately followed by parenthesis. |
| ○ 既定 | LT07 | `layout.cte_bracket` | 'WITH' clause closing bracket should be on a new line. |
| ○ 既定 | LT08 | `layout.cte_newline` | Blank line expected but not found after CTE closing bracket. |
| ❌ 無効 | LT09 | `layout.select_targets` | Select targets should be on a new line unless there is only one select target. |
| ○ 既定 | LT10 | `layout.select_modifiers` | 'SELECT' modifiers (e.g. 'DISTINCT') must be on the same line as 'SELECT'. |
| ○ 既定 | LT11 | `layout.set_operators` | Set operators should be surrounded by newlines. |
| ○ 既定 | LT12 | `layout.end_of_file` | Files must end with a single trailing newline. |
| ○ 既定 | LT13 | `layout.start_of_file` | Files must not begin with newlines or whitespace. |
| ❌ 無効 | LT14 | `layout.keyword_newline` | Keyword clauses should follow a standard for being before/after newlines. |
| ○ 既定 | LT15 | `layout.newlines` | Too many consecutive blank lines. |

### OR — Oracle 方言（1 ルール）

| 状態 | 番号 | ルール名 | 説明 |
|:--:|---|---|---|
| ○ 既定 | OR01 | `oracle.empty_batch` | Remove empty batches. |

### PG — PostgreSQL 方言（1 ルール）

| 状態 | 番号 | ルール名 | 説明 |
|:--:|---|---|---|
| ❌ 無効 | PG01 | `postgres.excessive_locks` | Avoid excessive locks in PostgreSQL DDL statements. |

### RF — 参照 references（6 ルール）

| 状態 | 番号 | ルール名 | 説明 |
|:--:|---|---|---|
| ○ 既定 | RF01 | `references.from` | References cannot reference objects not present in 'FROM' clause. |
| ○ 既定 | RF02 | `references.qualification` | References should be qualified if select has more than one referenced table/view. |
| ○ 既定 | RF03 | `references.consistent` | Column references should be qualified consistently in single table statements. |
| ✅ 調整 | RF04 | `references.keywords` | Keywords should not be used as identifiers. |
| ○ 既定 | RF05 | `references.special_chars` | Do not use special characters in identifiers. |
| ○ 既定 | RF06 | `references.quoting` | Unnecessary quoted identifier. |

### ST — 構造 structure（12 ルール）

| 状態 | 番号 | ルール名 | 説明 |
|:--:|---|---|---|
| ○ 既定 | ST01 | `structure.else_null` | Do not specify 'else null' in a case when statement (redundant). |
| ○ 既定 | ST02 | `structure.simple_case` | Unnecessary 'CASE' statement. |
| ○ 既定 | ST03 | `structure.unused_cte` | Query defines a CTE (common-table expression) but does not use it. |
| ○ 既定 | ST04 | `structure.nested_case` | Nested 'CASE' statement in 'ELSE' clause could be flattened. |
| ○ 既定 | ST05 | `structure.subquery` | Join/From clauses should not contain subqueries. Use CTEs instead. |
| ○ 既定 | ST06 | `structure.column_order` | Select wildcards then simple targets before calculations and aggregates. |
| ○ 既定 | ST07 | `structure.using` | Prefer specifying join keys instead of using 'USING'. |
| ○ 既定 | ST08 | `structure.distinct` | 'DISTINCT' used with parentheses. |
| ○ 既定 | ST09 | `structure.join_condition_order` | Joins should list the table referenced earlier/later first. |
| ○ 既定 | ST10 | `structure.constant_expression` | Redundant constant expression. |
| ○ 既定 | ST11 | `structure.unused_join` | Joined table not referenced in query. |
| ○ 既定 | ST12 | `structure.consecutive_semicolons` | Consecutive semicolons detected. |

### TQ — T-SQL 方言（3 ルール）

| 状態 | 番号 | ルール名 | 説明 |
|:--:|---|---|---|
| ○ 既定 | TQ01 | `tsql.sp_prefix` | 'SP_' prefix should not be used for user-defined stored procedures in T-SQL. |
| ○ 既定 | TQ02 | `tsql.procedure_begin_end` | Procedure bodies with multiple statements should be wrapped in BEGIN/END. |
| ○ 既定 | TQ03 | `tsql.empty_batch` | Remove empty batches. |

---

## 制限を追加するフロー（`spec-sql-lint` skill と連携）

1. **ルールを選ぶ**：上表から要件に合う番号を探す（例：「文末のセミコロン必須」→ `CV06`）。
2. **skill を実行する**：`/spec-sql-lint add-rule <説明または番号>`。skill は次を行う——
   - ルールの意味と設定キーを確認する。
   - **双方に反映する**：`.sqlfluff` を変更する（設定セクションの追加 / `exclude_rules` への追加・削除 / `ignore_words` の追加）とともに、[`spec-sql-lint-rule.md`](spec-sql-lint-rule.md) の §3 と §5 の設定正本も同期する。
   - `sqlfluff lint` を**再実行**し、期待どおりに動作し、既存の合格済みスクリプトに誤検知が出ないことを検証する。
   - 本表で当該ルールの状態表記を更新し（○既定/❌無効 → ✅調整）、規約の「改訂」欄に 1 行記録する。
3. **方針の確認**：有効化/無効化/緩和は方針の決定事項であるため、skill は**停止して確認を求めて**から反映する。

### 設定キー早見表（間違えやすいもの）
- キーワード/リテラルの大文字小文字：`capitalisation_policy`
- **識別子/関数/型の大文字小文字：`extended_capitalisation_policy`**（上記とは別物）
- 例外語の免除：該当ルールのセクションに `ignore_words = a,b,c` を追加
- ルールの無効化：`[sqlfluff]` セクションの `exclude_rules = ...`

---

## 関連
- ルール採否の正本：[`spec-sql-lint-rule.md`](spec-sql-lint-rule.md)（§3 有効/無効、§5 `.sqlfluff` 設定の正本）
- 上位の大文字小文字/型：[`spec-database-rule.md`](spec-database-rule.md) / [`spec-postgresql-column-types-rule.md`](spec-postgresql-column-types-rule.md)
- 実行スキル：`.claude/skills/spec-sql-lint/`
