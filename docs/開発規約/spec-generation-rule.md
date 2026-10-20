<!-- devtool:file-region id="document-header" -->
# 生成共通規約（spec-generation-rule）

> コード・文書の生成に共通して適用する、技術スタックに依存しない規則を定義する。
> Backend / Frontend / SQL / テストの各規約は、本ファイルの規則を再定義せず参照する。

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="rules.generation-rule.s01" -->
## 1. 権威順序（競合時の優先順位）

<!-- rule: gen.conflict-priority-order -->
規約・設計・データソースが競合した場合は、次の順で上位を正とする。

```
承認済み規約（最高権威）
  ├─ spec-naming-rule.md        命名
  └─ spec-database-rule.md      DB テーブル/列/型/制約/監査列/トリガー
        > 各層の開発規約（spec-backend-coding-rule.md / spec-frontend-coding-rule.md）
              > 機能の詳細設計
                    > yaml（項目/エンドポイント/メッセージのデータソース・下書き）
```

- 競合は常に**承認済み規約**を正として、下位の文書へ修正を書き戻す。
- 例外は `spec-database-rule.md` に `EXCEPTION:` として登録した場合のみ成立する。

<!-- devtool:endblock -->
<!-- devtool:block id="rules.generation-rule.s02" -->
## 2. ファイル規約（改行・文字コード）

<!-- rule: gen.line-ending-lf-no-bom -->
- 生成するすべてのファイルは **LF（`\n`）改行**に統一し（CRLF 禁止）、**UTF-8 BOM なし**とする。
- LF が改変されないよう、リポジトリルートに `.gitattributes` と `.editorconfig`（`end_of_line = lf` / `charset = utf-8`）を配置する。
- 拡張子ごとの `.gitattributes` の具体記述と、CRLF が必須となる例外（Windows スクリプト等）は、各技術スタックの規約に従う。
<!-- devtool:endblock -->
