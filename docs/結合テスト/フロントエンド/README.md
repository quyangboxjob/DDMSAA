<!-- devtool:file-region id="document-header" -->
# 画面結合テスト 利用手順

> 画面結合テストの前提・実行方法を記す。式様書・品質の規約は [`開発規約/spec-test-rule.md`](../../開発規約/spec-test-rule.md)、式様書テンプレートは [`式様書/spec-it-fe-temp.md`](式様書/spec-it-fe-temp.md) を参照。

<!-- devtool:end-file-region -->
<!-- devtool:block id="it.frontend.readme.s01" -->
## 1. 前提・方針

| 項目 | 内容 |
|------|------|
| 被テスト | 画面（SCR）単位。コンポーネント木・状態・ルーティング・API 配線の協調 |
| モック範囲 | ネットワーク層のみ。Context／子コンポーネント／ルーターは実物 |
| i18n | `t()` をキー返しに単純化し、画面文言ではなく i18n キー／`t()` 引数を検証 |
| 配置 | ネットワークモックのハンドラ・フィクスチャ・テスト本体はフロントエンドプロジェクトの `src/` 配下 |

<!-- devtool:endblock -->
<!-- devtool:block id="it.frontend.readme.s02" -->
## 2. 実行方法

- Vitest + Testing Library で実行する。
- 結果レポートは `report/spec-it-fe-testreport-<date>.md`。データ源は Vitest の junit reporter 出力。
- 環境が未起動で実行できない場合は結果を作らず「実行待ち（NOT RUN）」と明記する。

```
# 例（プロジェクトの実コマンドに合わせて置換）
〇〇
```

<!-- devtool:endblock -->
<!-- devtool:block id="it.frontend.readme.s03" -->
## 3. 関連ファイル

| ファイル | 用途 |
|------|------|
| `式様書/spec-it-fe-temp.md` | 式様書テンプレート |
| `式様書/spec-it-fe-<SCR-id>.md` | 式様書（人手審査対象の正本） |
| `課題.md` | 契約と実装の乖離の集約 |
| `report/spec-it-fe-testreport-<date>.md` | 結果レポート |
<!-- devtool:endblock -->
