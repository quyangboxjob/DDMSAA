<!-- devtool:file-region id="document-header" -->
# `<operationId>` API 結合テスト式様書

> API 結合テスト（IT）の式様書テンプレート。**operationId ごとに本ファイルをコピーして記入し**、`結合テスト/API/式様書/spec-it-<operationId>.md` として保存する。
> テスト階層・ID 規則・テストデータの原則は [`開発規約/spec-test-rule.md`](../../../開発規約/spec-test-rule.md) を正とする。本ファイルは SSOT を参照するのみで、項目・列挙値・メッセージを再定義しない。
> **本式様書は人手審査対象の正本**である。審査状態が「承認済み」になってから、テストデータ（fixtures）・テストコードを生成する（`spec-test-rule.md` §9）。
> **ケースの源泉は契約（SSOT）**：`spec-07-api-list.yaml`／`spec-06-db-physical.yaml`／`spec-09-message-list.yaml`／`spec-03-auth-ui.md`。契約が未定義の分岐は創作せず、`課題.md` に記録して上流へ報告する。

<!-- devtool:end-file-region -->
<!-- devtool:block id="integrationtest.it-api-temp.s01" -->
## 文書情報

| 項目 | 内容 |
|------|------|
| システム名称 | 〇〇システム |
| 文書名称 | `<operationId>` API 結合テスト式様書 |
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
<!-- devtool:block id="integrationtest.it-api-temp.s02" -->
## 0. 対象と根拠

| 項目 | 内容 |
|------|------|
| operationId | `<operationId>`（`spec-07-api-list.yaml` と一致） |
| エンドポイント | `METHOD /path` |
| 対応機能 | F-〇〇-0N |
| ロール・権限 | `spec-03-auth-ui.md` 参照（例：`ADMIN` のみ可） |
| 契約 SSOT | `spec-07`（入出力・列挙値・境界）／`spec-06`（テーブル・カラム）／`spec-09`（messageId）／`spec-03`（ロール・マスク） |

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.it-api-temp.s03" -->
## 1. 前提・実行環境

> ブラックボックス：デプロイ済みの API 入口（Gateway）を実システム・実 DB で呼ぶ。被テストコードには依存しない。

| 項目 | 内容 |
|------|------|
| 対象環境 | デプロイ済み Gateway（ベース URL：〇〇） |
| DB | 実 DB（初期化 SQL の schema／seed を一次供給元とする） |
| 認証トークン | 本番と同一署名設定で**オフライン署名した JWT** を使用（ログインフロー・メール・ワンタイム資格情報に依存しない）。取得手順は `API/README.md` 参照 |
| 副作用の隔離 | メール・外部連携・ファイルストレージ等はスタブ／サンドボックスで実リクエストを送らない |

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.it-api-temp.s04" -->
## 2. テストケース表

> **各行 ＝ 1 つの IT-ID ＝ 1 つのテストメソッド**。IT-ID は `IT-<operationId>-NNN`（operationId 内の 3 桁連番）。`@DisplayName("IT-<operationId>-NNN <条件→期待>")` の先頭に ID を必須で付与する。
> 期待値は SSOT 参照のみ（HTTP 状態 ← 例外型ごとの固定マッピング、messageId ← `spec-09`、レスポンス構造 ← `spec-07`、マスク ← `spec-03`）。実装の挙動を正解にしない。

| IT-ID | 種別 | 前提（seed） | リクエスト（request） | 期待（HTTP / messageId / 主要項目 / マスク） | `@DisplayName` | fixture 参照 | 状況 |
|-------|:---:|------|------|------|------|------|:---:|
| IT-`<operationId>`-001 | 正常 | 〇〇が存在する | 有効なボディ | 200 / `I00NN` / `〇〇` が期待値 | `IT-<operationId>-001 正常_〇〇で200` | `seed/〇〇.sql` `request/001.json` `expected/001.json` | 未 |
| IT-`<operationId>`-002 | 検証 | — | 必須項目欠落 | 400 / `E00NN` | `IT-<operationId>-002 検証_必須欠落で400` | `request/002.json` `expected/002.json` | 未 |
| IT-`<operationId>`-003 | 権限 | — | 権限不足のロールの JWT | 403 / `E0002` | `IT-<operationId>-003 権限_〇〇拒否で403` | `request/003.json` `expected/003.json` | 未 |
| IT-`<operationId>`-004 | 異常 | 対象不存在 | 存在しない ID | 404 / `E0005` | `IT-<operationId>-004 異常_不存在で404` | `request/004.json` `expected/004.json` | 未 |
| IT-`<operationId>`-005 | 境界 | 暗号化カラム有 | my_number 等を含む | 復号往復成立／出力は `****` でマスク | `IT-<operationId>-005 境界_マスク出力` | `seed/005.sql` `expected/005.json` | 未 |

> ↑ 記入例。正式記入時は削除し、契約の分岐から導出したケースを漏れなく展開する。

### 2.1 列定義

| 列 | 内容 |
|------|------|
| IT-ID | `IT-<operationId>-NNN`（operationId 内の 3 桁連番） |
| 種別 | 正常／異常／権限／検証／境界 のいずれか |
| 前提（seed） | `fixtures/seed/` の事前 DB シード。冪等・再実行可能・teardown 付き |
| 期待 | SSOT 参照のみ。共有 DB を叩く場合はアサーションを意味的・包含的に（件数は `>=` 等） |
| 状況 | 未／作成済／更新／スキップ（理由） |

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.it-api-temp.s05" -->
## 3. テストデータ（fixtures）

> 「mock」＝テストデータの意味（独立 mock サーバや外部スタブではない）。構成は 3 点（`spec-test-rule.md` §8）。

| 構成 | 配置 | 内容 |
|------|------|------|
| 事前 DB シード／フィクスチャ | `fixtures/seed/` | 既存の初期化 SQL を一次供給元とし、不足分のみ冪等な SQL で補う。teardown を伴う |
| リクエスト例 | `fixtures/request/` | ケースごとの入力ボディ |
| 期待レスポンス断片 | `fixtures/expected/` | messageId・主要項目・マスク期待値 |

- **暗号化カラムは実暗号文**：実行サービスと同一鍵で暗号化した実暗号文を用いる（プレースホルダ文字列へのアサーション禁止）。
- **鍵・秘密情報**：固定暗号鍵・JWT 署名設定は `src/test`（テスト用プロファイル）にのみ置く。種子の暗号化鍵と実行時の鍵は同一値とする。

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.it-api-temp.s06" -->
## 4. 実行とレポート

| 項目 | 内容 |
|------|------|
| テストコード配置 | `結合テスト/API/it-api-runner/`（バックエンド reactor に含めない独立プロジェクト） |
| クラス名 | `*IT`（failsafe が `integration-test`／`verify` フェーズで実行） |
| JUnit XML | `usePhrasedTestCaseMethodName=true` で `<testcase name>` を `@DisplayName` にする |
| 結果レポート | `結合テスト/API/report/spec-it-testreport-<date>.md`（直近 1 回分を上書き。失敗・スキップも事実のまま記載） |
| 実行不可時 | 結果を作らず「実行待ち（NOT RUN）」と明記し、起動方法（`API/README.md`）を示す |

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.it-api-temp.s07" -->
## 5. 課題（契約と実装の乖離）

> 契約（SSOT）が正本。乖離はテストで取り繕わず、テストを失敗（RED）のまま上流へ報告するか、`結合テスト/API/課題.md` に記録して人手で裁定する。セキュリティ上の欠陥を現状の正しい挙動として固定しない（あるべき安全な挙動を期待値にし、未対応は備考に明記）。

| # | 乖離内容 | 契約（根拠 SSOT） | 実装の挙動 | 対応方針 |
|---|------|------|------|------|
| 1 | 〇〇 | `spec-0N-…` | 〇〇 | 上流修正依頼／課題化 |
<!-- devtool:endblock -->
