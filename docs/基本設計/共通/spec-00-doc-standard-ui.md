<!-- devtool:file-region id="document-header" -->
# 00 文書規約・共通ヘッダ

> 本ファイルは、基本設計の全文書で共通に用いる文書ヘッダ、命名規約、版数と改訂履歴、ID 命名規則、表記規約を定義する。
> 各設計書の冒頭には「文書ヘッダ」を貼り付けて記入すること。

---

<!-- rule: doc.header-block -->
<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.common.00-doc-standard-ui.s01" -->
## 1. 文書ヘッダ（各文書の冒頭にこのブロックを貼り付ける）

### 文書情報

| 項目 | 内容 |
|------|------|
| システム名 | 〇〇 |
| 文書名 | 〇〇設計書 |
| 文書版数 | V0.1 |
| ステータス | 草案 / レビュー中 / 確定 |
| 作成者 | 〇〇 |
| 作成日 | YYYY-MM-DD |
| レビュー者 | 〇〇 |
| 承認者 | 〇〇（ステータスを「確定」にする判断と責任を持つ人） |
| 承認日 | YYYY-MM-DD |

### 改訂履歴

| 版数 | 改訂日 | 改訂者 | 改訂内容 |
|------|--------|--------|---------|
| V0.1 | YYYY-MM-DD | 〇〇 | 初版 |
| | | | |

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.00-doc-standard-ui.s02" -->
## 2. ファイル・文書の命名規約

| 対象 | 規則 | 例 |
|------|------|------|
| 設計書ファイル | `連番_文書名.md` | `spec-03-auth-ui.md` |
| 成果物の文書名 | `<システム名>_<文書名>_<版数>` | `DDMSAA_基本設計_V1.0` |
| 出力ファイル（帳票など）**【正本】** | `<帳票名>_YYYYMMDD.<拡張子>`。複数ファイルは ZIP にまとめ `<帳票名>_YYYYMMDD.zip`。帳票固有の規則は `spec-14-report-design-ui.md` §2 の帳票別設計に記載する | `ユーザ一覧_YYYYMMDD.xlsx` |

<!-- rule: doc.output-file-naming -->

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.00-doc-standard-ui.s03" -->
## 3. ID 命名規則（全文書で一貫させる）

<!-- rule: doc.id-prefix-scheme -->
<!-- rule: doc.id-role-prefix -->
| 対象 | プレフィックス | 形式 | 例 |
|------|------|------|------|
| 機能 | `F-` | `F-大分類-連番` | `F-USR-01` |
| 画面 | `SCR-` | `SCR-99` | `SCR-01` |
| API | `operationId` | `createUser` | `pageUsers` |
| テーブル | `TBL-` | `TBL-物理名（複数形 snake_case）` | `TBL-users` |
| バッチ | `BAT-` | `BAT-99` | `BAT-01` |
| 帳票 | `RPT-` | `RPT-99` | `RPT-01` |
| 業務フロー | `BF-` | `BF-99` | `BF-01` |
| 業務ルール | `BR-` | `BR-99` | `BR-01` |
| データ移行 | `MIG-` | `MIG-99` | `MIG-01` |
| 運用作業 | `OPS-` | `OPS-99` | `OPS-01` |
| 外部インタフェース | `IF-` | `IF-99` | `IF-01` |
| ロール | `R-` | `R-`+列挙値 | 例：`R-ADMIN`（= R- + API `Role` 列挙値。ロールはプロジェクトで定義する） |
| メッセージ（messageId） | — | 形式と区分は `spec-09-message-list-ui.md` §1 に従う | — |

> ID は一度採番したら再利用しない。廃止した機能は ID を残し「廃止」と明記する。
> 列挙値・コード値はこの採番体系の対象外とし、`../../開発規約/spec-data-dictionary-project-temp.md` の T07 列挙型（category_code）で一元管理する。
> メッセージ ID の形式・区分と文言の正本は `spec-09-message-list-ui.md` に従う。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.00-doc-standard-ui.s04" -->
## 4. 表記規約

| 記号 | 意味 |
|------|------|
| ○ / × | 可 / 不可 |
| 必須 | 必須入力項目 |
| R / W / — | 参照可 / 更新可 / 権限なし（権限マトリクス用） |

> プレースホルダには `〇〇`（置換対象）を統一して用いる。記号 `○`（可）とは別の文字であり、混同しないこと。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.00-doc-standard-ui.s05" -->
## 5. 共通用語集（プロジェクトごとに追補）

| 用語 | 意味 |
|------|------|
| マスタ | システム運用に必要な基礎設定データ |
| 論理削除 | 物理削除せず、削除フラグ（例：`deleted_at`）を設定すること |
| 営業日 | 土日・祝日を除いた稼働日 |
| 〇〇 | 〇〇 |
<!-- devtool:endblock -->
