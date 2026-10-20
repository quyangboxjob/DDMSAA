<!-- devtool:file-region id="document-header" -->
# API 結合テスト テストデータ（fixtures）

> API 結合テストの「mock」はテストデータの意味であり、独立した mock サーバや外部副作用のスタブではない（[`開発規約/spec-test-rule.md`](../../../開発規約/spec-test-rule.md) §8）。構成は 3 点。

<!-- devtool:end-file-region -->
<!-- devtool:block id="it.api.fixtures.readme.s01" -->
## ディレクトリ

| ディレクトリ | 内容 |
|------|------|
| `seed/` | 事前 DB シード／フィクスチャ（SQL）。既存の初期化 SQL を一次供給元とし、不足分のみ冪等な SQL で補う。teardown を伴う |
| `request/` | ケースごとのリクエスト例（JSON 等） |
| `expected/` | 期待レスポンス断片（messageId・主要項目・マスク期待値） |

<!-- devtool:endblock -->
<!-- devtool:block id="it.api.fixtures.readme.s02" -->
## 原則

1. **既存の初期化スクリプトを再利用**：テスト専用に schema を複製・再定義しない。
2. **冪等・再実行可能・後始末付き**：共有 DB を叩く場合はアサーションを意味的・包含的に（件数は `>=` 等）。厳密な件数が必要なら専用フィクスチャ＋teardown とし、隔離方針を式様書に明記する。
3. **暗号化カラムは実暗号文**：実行サービスと同一鍵で暗号化した実暗号文を用いる（プレースホルダ文字列へのアサーション禁止）。
4. **期待値の根拠は SSOT**：messageId ← `spec-09-message-list.yaml`、HTTP 状態 ← 例外型ごとの固定マッピング、レスポンス構造 ← `spec-07-api-list.yaml`、マスク ← `spec-03-auth-ui.md`。実装の挙動をそのまま正解にしない。

> ケースと fixture の対応は式様書（`式様書/spec-it-<operationId>.md`）の「fixture 参照」列で管理する。
<!-- devtool:endblock -->
