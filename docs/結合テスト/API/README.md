<!-- devtool:file-region id="document-header" -->
# API 結合テスト 利用手順

> API 結合テスト（ブラックボックス）の起動前提・実行方法・token 取得手順を記す。式様書・テストデータ・コードの規約は [`開発規約/spec-test-rule.md`](../../開発規約/spec-test-rule.md)、式様書テンプレートは [`式様書/spec-it-api-temp.md`](式様書/spec-it-api-temp.md) を参照。

<!-- devtool:end-file-region -->
<!-- devtool:block id="it.api.readme.s01" -->
## 1. 起動前提

| 項目 | 内容 |
|------|------|
| 対象 | デプロイ済み Gateway（ベース URL：〇〇） |
| DB | 実 DB。schema／seed は初期化 SQL を一次供給元とする |
| 副作用 | メール・外部連携・ストレージはスタブ／サンドボックスで実リクエストを送らない |

<!-- devtool:endblock -->
<!-- devtool:block id="it.api.readme.s02" -->
## 2. 実行方法

- テストコードは `it-api-runner/`（バックエンド reactor に含めない独立プロジェクト）に置き、クラス名は `*IT` とする。
- failsafe（`integration-test`／`verify` フェーズ）で実行する。
- 結果レポートは `report/spec-it-testreport-<date>.md`。データ源は JUnit XML（コンソール出力ではない）。
- 環境が未起動で実行できない場合は結果を作らず「実行待ち（NOT RUN）」と明記する。

```
# 例（プロジェクトの実コマンドに合わせて置換）
〇〇
```

<!-- devtool:endblock -->
<!-- devtool:block id="it.api.readme.s03" -->
## 3. token 取得（オフライン署名 JWT）

- 本番と同一の署名設定で JWT を**テスト側でオフライン署名**して使用する（ログインフロー・メール・ワンタイム資格情報に依存しない）。
- 固定暗号鍵・JWT 署名設定は `src/test`（テスト用プロファイル）にのみ置く。`src/main` にハードコードしない。
- 種子の暗号化に使う鍵と実行時の鍵は同一値とする（不一致による復号失敗を防ぐ）。

<!-- devtool:endblock -->
<!-- devtool:block id="it.api.readme.s04" -->
## 4. 関連ファイル

| ファイル | 用途 |
|------|------|
| `式様書/spec-it-api-temp.md` | 式様書テンプレート |
| `式様書/spec-it-<operationId>.md` | 式様書（人手審査対象の正本） |
| `fixtures/{seed,request,expected}/` | テストデータ |
| `課題.md` | 契約と実装の乖離の集約 |
| `report/spec-it-testreport-<date>.md` | 結果レポート |
<!-- devtool:endblock -->
