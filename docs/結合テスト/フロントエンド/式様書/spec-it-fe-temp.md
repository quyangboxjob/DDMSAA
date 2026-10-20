<!-- devtool:file-region id="document-header" -->
# `<SCR-id>` 画面結合テスト式様書

> 画面結合テスト（IT）の式様書テンプレート。**画面（SCR）ごとに本ファイルをコピーして記入し**、`結合テスト/フロントエンド/式様書/spec-it-fe-<SCR-id>.md` として保存する。
> テスト階層・ID 規則・品質ルールは [`開発規約/spec-test-rule.md`](../../../開発規約/spec-test-rule.md) を正とする。本ファイルは SSOT を参照するのみで、画面文言・項目・メッセージを再定義しない。
> **本式様書は人手審査対象の正本**である。審査状態が「承認済み」になってから、テストデータ・テストコードを生成する（`spec-test-rule.md` §9）。
> **ケースの源泉は契約（画面のイベント・バリデーション設計）**：詳細設計 §2・`spec-07-api-list.yaml`・`spec-09-message-list.yaml`・`spec-03-auth-ui.md`。契約が未定義の分岐は創作せず、`課題.md` に記録して上流へ報告する。

<!-- devtool:end-file-region -->
<!-- devtool:block id="integrationtest.it-fe-temp.s01" -->
## 文書情報

| 項目 | 内容 |
|------|------|
| システム名称 | 〇〇システム |
| 文書名称 | `<SCR-id>` 画面結合テスト式様書 |
| 文書バージョン | V0.1 |
| **審査状態** | **草案 / レビュー中 / 承認済み**（「承認済み」でテストデータ・コード生成を解禁） |
| 作成者 | TBD |
| 作成日 | YYYY-MM-DD |
| レビュー担当 | TBD |

## 改訂履歴

| バージョン | 改訂日 | 改訂者 | 改訂内容 |
|------|--------|--------|---------|
| V0.1 | YYYY-MM-DD | 〇〇 | 初版 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.it-fe-temp.s02" -->
## 0. 対象と根拠

| 項目 | 内容 |
|------|------|
| 画面 | `<SCR-id>` 〇〇画面 |
| 対応機能 | F-〇〇-0N |
| 関連 operationId | `<operationId>`〜（API 配線の検証対象） |
| 上流根拠 | 詳細設計 §2（コンポーネント分解・イベント→API・クライアント検証・ロール別表示制御） |
| 契約 SSOT | `spec-07`（入出力・列挙値）／`spec-09`（messageId → i18n キー）／`spec-03`（ロール別表示制御） |

**コンポーネントツリー（詳細設計 §2 より）**

```
〇〇Page (src/pages/〇〇/〇〇-page.tsx)
├── 〇〇Table (src/components/〇〇/〇〇-table.tsx)
├── 〇〇Form  (src/components/〇〇/〇〇-form.tsx)
└── 〇〇Modal (src/components/〇〇/〇〇-modal.tsx)
```

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.it-fe-temp.s03" -->
## 1. 前提・方針

> 画面（コンポーネント木・状態・ルーティング・API 配線）の協調を検証する。**ネットワーク層のみモック**し、Context／子コンポーネント／ルーターは実物を使う。

| 項目 | 方針 |
|------|------|
| モック範囲 | ネットワーク層のみ（API の HTTP 応答をハンドラで差し替え）。Context はモジュールごとモックせず、テスト用の値を持つ Provider でラップ |
| i18n | `t()` をキー返しに単純化し、画面文言そのものではなく **i18n キー／`t()` 引数**を検証する |
| 配置 | ネットワークモックのハンドラ・フィクスチャ・テスト本体はフロントエンドプロジェクトの `src/` 配下 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.it-fe-temp.s04" -->
## 2. テストケース表

> **各行 ＝ 1 つの IT-ID ＝ 1 つの `it(...)`**。IT-ID は `IT-<SCR-id>-NNN`（画面 ID 内の 3 桁連番）。`it('IT-<SCR-id>-NNN <条件→期待>', …)` のようにテスト名の先頭に ID を必須で含める。
> 期待値は SSOT 参照のみ（messageId は i18n キーとして検証、API 入出力は `spec-07`）。

| IT-ID | 種別 | 前提（モック応答・Provider 値） | 操作 | 期待（状態 / DOM / i18n キー / API 配線） | `it(...)` 名 | 状況 |
|-------|:---:|------|------|------|------|:---:|
| IT-`<SCR-id>`-001 | 正常 | 一覧 API が 〇件返す | 画面表示 | 一覧が 〇件レンダリングされる | `IT-<SCR-id>-001 正常_一覧表示` | 未 |
| IT-`<SCR-id>`-002 | イベント | 登録 API が成功を返す | フォーム入力→送信 | 成功 Toast に i18n キー `I00NN` が渡り、再読込される | `IT-<SCR-id>-002 登録成功で再読込` | 未 |
| IT-`<SCR-id>`-003 | 異常 | 登録 API が `ErrorResponse(E00NN)` | 送信 | エラー表示に i18n キー `E00NN` が渡る | `IT-<SCR-id>-003 失敗でエラー表示` | 未 |
| IT-`<SCR-id>`-004 | 権限 | ロール=一般 の Provider 値 | 画面表示 | 管理者専用ボタンが非表示 | `IT-<SCR-id>-004 一般ロールで操作非表示` | 未 |
| IT-`<SCR-id>`-005 | 遷移 | — | 〇〇クリック | `<SCR-0M>` へ遷移する | `IT-<SCR-id>-005 〇〇で画面遷移` | 未 |

> ↑ 記入例。正式記入時は削除し、詳細設計 §2 のイベント・検証・ロール別表示制御から導出したケースを展開する。

### 2.1 列定義

| 列 | 内容 |
|------|------|
| IT-ID | `IT-<SCR-id>-NNN`（画面 ID 内の 3 桁連番） |
| 種別 | 正常／異常／権限／検証／遷移 のいずれか |
| 前提 | ネットワークモックの応答と、Provider に渡すテスト用の値（ロール等） |
| 期待 | state 遷移／DOM／i18n キー／API 呼び出し（引数・回数）。画面文言そのものを検証しない |
| 状況 | 未／作成済／更新／スキップ（理由） |

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.it-fe-temp.s05" -->
## 3. 実行とレポート

| 項目 | 内容 |
|------|------|
| フレームワーク | Vitest + Testing Library（ネットワークモックのハンドラを併用） |
| JUnit XML | Vitest の junit reporter 出力を結果レポートのデータ源とする |
| 結果レポート | `結合テスト/フロントエンド/report/spec-it-fe-testreport-<date>.md`（直近 1 回分を上書き。失敗・スキップも事実のまま記載） |
| 実行不可時 | 結果を作らず「実行待ち（NOT RUN）」と明記し、起動方法（`フロントエンド/README.md`）を示す |

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.it-fe-temp.s06" -->
## 4. 課題（契約と実装の乖離）

> 契約（SSOT）が正本。乖離はテストで取り繕わず、RED のまま上流へ報告するか、`結合テスト/フロントエンド/課題.md` に記録して人手で裁定する。セキュリティ上の欠陥を現状の正しい挙動として固定しない。

| # | 乖離内容 | 契約（根拠 SSOT） | 実装の挙動 | 対応方針 |
|---|------|------|------|------|
| 1 | 〇〇 | `spec-0N-…` / 詳細設計 §2 | 〇〇 | 上流修正依頼／課題化 |
<!-- devtool:endblock -->
