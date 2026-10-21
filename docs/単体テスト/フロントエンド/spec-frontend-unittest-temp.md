<!-- devtool:file-region id="document-header" -->
# 〇〇〇〇 単体テスト仕様書（フロントエンド）

> フロントエンド（React）の単体テスト仕様書テンプレート。**対象（機能／ドメイン）ごとに本ファイルをコピーして記入し**、`単体テスト/フロントエンド/spec-<対象 kebab-case>-unittest-sd.md` として保存する。
> テスト階層・ディレクトリ・命名・品質ルールは [`開発規約/spec-test-rule.md`](../../開発規約/spec-test-rule.md) を正とする。本ファイルは SSOT を参照するのみで、画面文言・項目・メッセージを再定義しない。
> **フロントエンド単体テストの項目は本仕様書の「実装状況」列で管理する**（`spec-test-rule.md` §11：フロントエンド用の UT-ID 採番規則は未決。UT-ID はバックエンド用例にのみ規定）。対象の挙動は詳細設計 §2（フロントエンド詳細設計）・画面設計から導出する。

<!-- devtool:end-file-region -->
<!-- devtool:block id="unittest.frontend-unittest-temp.s01" -->
## 文書情報

| 項目 | 内容 |
|------|------|
| システム名称 | 〇〇システム |
| 文書名称 | 〇〇〇〇 単体テスト仕様書（フロントエンド） |
| 文書バージョン | V0.1 |
| ステータス | 草案 |
| 作成者 | TBD |
| 作成日 | YYYY-MM-DD |
| レビュー担当 | TBD |

## 改訂履歴

| バージョン | 改訂日 | 改訂者 | 改訂内容 |
|------|--------|--------|---------|
| V0.1 | YYYY-MM-DD | 〇〇 | 初版 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.frontend-unittest-temp.s02" -->
## 0. 対象と根拠

| 項目 | 内容 |
|------|------|
| 対象機能 | F-〇〇-0N |
| 対象画面 | SCR-0N |
| 対象ディレクトリ | `frontend/src/{api,hooks,components,pages}/〇〇/` |
| 上流根拠 | `詳細設計/<機能名>/spec-<feature>-detail-sd.md` §2（フロントエンド詳細設計）・画面のイベント／検証設計 |
| 上流 SSOT | `spec-09-message-list.yaml`（messageId → i18n キー）／`spec-07-api-list.yaml`（入出力・列挙値）／`spec-03-auth-ui.md`（ロール別表示制御） |

### 0.1 テスト生成時のロード一覧（AI コンテキスト組み立て）

```
- 本ファイル（当該対象の単体テスト仕様書）
- 詳細設計/<機能名>/spec-<feature>-detail-sd.md   ← §2 コンポーネント分解・イベント→API・クライアント検証
- 基本設計/共通/spec-09-message-list.yaml          ← messageId（i18n キー検証の根拠）
- 基本設計/共通/spec-07-api-list.yaml              ← 対象 operationId の入出力・列挙値
- 開発規約/spec-test-rule.md / spec-naming-rule.md
- 開発規約/@react/spec-frontend-coding-rule.md
```

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.frontend-unittest-temp.s03" -->
## 1. テスト対象と優先度

> **対象優先度**：純粋関数 → カスタムフック → プレゼンテーショナルなコンポーネント の順（`spec-test-rule.md` §9）。画面本体や重量級依存を合成するだけのコンテナは原則対象外とし、理由を 1 文で明記する（依存の種類＝router／Context／i18n の有無そのものは対象外理由にしない）。

| 対象 | 種別 | ファイル | 対象/対象外 | 対象外の理由（対象外のときのみ） |
|------|------|------|:---:|------|
| `formatXxx()` 等 | 純粋関数 | `src/api/〇〇-api.ts` ほか | 対象 | — |
| `use〇〇()` | カスタムフック | `src/hooks/〇〇/use-〇〇.ts` | 対象 | — |
| `〇〇Form` | コンポーネント（Form） | `src/components/〇〇/〇〇-form.tsx` | 対象 | — |
| `〇〇Table` | コンポーネント（Table） | `src/components/〇〇/〇〇-table.tsx` | 対象 | — |
| `〇〇Page` | コンポーネント（Page／コンテナ） | `src/pages/〇〇/〇〇-page.tsx` | 対象外 | 子コンポーネントを合成するのみで自コードのロジックが無い（結合テストで検証） |

> ↑ 記入例。コンポーネントロール接尾辞（Page/Table/Form/Modal/Card）は `spec-frontend-coding-rule.md` §2 に従う。

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.frontend-unittest-temp.s04" -->
## 2. テスト項目表

> **各行 ＝ 1 つの `it(...)`**。文言そのものではなく **i18n キーまたは `t()` 呼び出し引数**を検証する（多言語対応を壊さない。`spec-test-rule.md` §6）。期待値の messageId は `spec-09-message-list.yaml` を参照。

| No. | 対象 | 種別 | 前提／Provider | 入力・操作 | 期待結果（i18n キー / 状態 / DOM / 呼び出し） | `it(...)` 説明 | テストファイル | 実装状況 |
|:---:|------|:---:|------|------|------|------|------|:---:|
| 001 | `formatXxx()` | 純粋関数 | — | 正常な引数 | 期待の文字列を返す | `正常値を整形して返す` | `__tests__/〇〇-api.test.ts` | 未 |
| 002 | `formatXxx()` | 純粋関数（境界） | — | null／空 | 既定値を返す／例外を投げない | `空入力で既定値を返す` | 同上 | 未 |
| 003 | `use〇〇()` | カスタムフック | テスト用 Provider でラップ | 〇〇をトリガ | state が 〇→〇 に遷移 | `〇〇で状態が更新される` | `__tests__/use-〇〇.test.ts` | 未 |
| 004 | `〇〇Form` | コンポーネント（検証） | i18n の `t()` はキー返しに単純化 | 必須未入力で送信 | エラー表示に i18n キー `〇〇` が渡る | `必須未入力でキー〇〇のエラーを表示` | `__tests__/〇〇-form.test.tsx` | 未 |
| 005 | `〇〇Form` | コンポーネント（イベント） | onSubmit をスパイ | 有効値で送信 | `onSubmit` が 1 回・期待引数で呼ばれる | `有効値でonSubmitを呼ぶ` | 同上 | 未 |

> ↑ 記入例。正式記入時は削除し、詳細設計 §2 のイベント／クライアント検証から導出した項目を記入する。
> 契約が未定義の分岐は創作せず、課題として上流へ報告する（`spec-test-rule.md` §5）。

### 2.1 列定義

| 列 | 内容 |
|------|------|
| No. | 対象内の連番（フロントエンドの項目 ID 採番は未決のため本仕様書内で一意な連番で管理） |
| 種別 | 純粋関数／カスタムフック／コンポーネント（検証・イベント・表示・境界 等の小分類を併記してよい） |
| 前提／Provider | Context はモジュールごとモックせず、テスト用の値を持つ Provider でラップ。i18n は `t()` をキー返しに単純化 |
| 期待結果 | i18n キー／`t()` 引数、state 遷移、DOM、モック呼び出し。画面文言そのものを検証しない |
| 実装状況 | 未／作成済／更新／スキップ（理由）。既存の人手テストは盲目的に上書きしない |

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.frontend-unittest-temp.s05" -->
## 3. テストコードの配置と命名

| 項目 | 規則 |
|------|------|
| 配置 | 対象ファイルと同じディレクトリ直下の `__tests__/` |
| ファイル名 | `<対象ファイル名>.test.ts`（コンポーネントは `.test.tsx`） |
| テスト名 | `it('<ケース説明>', …)`。各テストは有効なアサーションを最低 1 つ持つ |
| フレームワーク | Vitest + Testing Library（`spec-frontend-coding-rule.md` §1） |

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.frontend-unittest-temp.s06" -->
## 4. モック・i18n 方針

> 詳細は `spec-test-rule.md` §9。

| 項目 | 方針 |
|------|------|
| Context | モジュールごとモックせず、テスト用の値を持つ Provider でラップする |
| i18n | `t()` をキー返し（`t('x') === 'x'`）に単純化し、キー／`t()` 引数を検証する。言語ごとの文言は検証対象にしない |
| ネットワーク | 単体ではネットワークをモックせず、呼び出し先（API client 関数）を差し替える（`spec-test-rule.md` §2） |
| 子コンポーネント | プレゼンテーショナルな対象は実物を使う。重量級依存の合成のみのコンテナは §1 の方針で対象外 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.frontend-unittest-temp.s07" -->
## 5. 実行とレポート

| 項目 | 内容 |
|------|------|
| 実行 | Vitest（プロジェクトの実行コマンドに従う） |
| JUnit XML | Vitest の junit reporter 出力を結果レポートのデータ源とする |
| 結果レポート | `単体テスト/フロントエンド/report/spec-ut-fe-testreport-<date>.md`（直近 1 回分を上書き。失敗・スキップも事実のまま記載） |
| JUnit XML 配置 | `単体テスト/フロントエンド/report/junit-<対象>-<date>.xml` |
| 実行環境 | `jsdom`（`spec-test-rule.md` §7。バージョンは固定） |
| 実行不可時 | 結果を作らず「実行待ち（NOT RUN）」と明記し、起動方法を示す |

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.frontend-unittest-temp.s08" -->
## 6. 品質ルール（`spec-test-rule.md` §9）

- 空のアサーション・恒真アサーション・コメントアウトされたテスト・理由のない skip・ゲート通過だけの空実装は禁止。
- 画面文言そのものではなく i18n キー／`t()` 引数を検証し、多言語対応を壊さない。
- 契約（SSOT）が正本。契約が未定義の分岐は創作せず、課題として上流へ報告する。実装不具合を本依頼でコード修正しない。
- 対象外とする対象は理由を 1 文で明記する（自コードのロジックが無い／ライブラリ内部動作の検証になる）。
- 既存の人手テストは盲目的に上書きしない（新規／更新／スキップを区別）。
- 改行・文字コードはフロントエンドコーディング規約に従う。
<!-- devtool:endblock -->
