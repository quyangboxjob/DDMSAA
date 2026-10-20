<!-- devtool:file-region id="document-header" -->
# 結合テスト（IT）テンプレート

> 結合テスト（IT）の文書テンプレート。**API 結合**（operationId 単位・ブラックボックス）と**画面結合**（SCR 単位）の 2 種類に分かれる。
> テスト階層・ID 規則・ディレクトリ・命名・テストデータの原則は [`開発規約/spec-test-rule.md`](../開発規約/spec-test-rule.md) を**唯一の正（SSOT）**とする。本テンプレートは規約を参照・具体化するのみで、規約を再定義しない。

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="integrationtest.readme.s01" -->
## 1. 位置付けと UT との違い

```
①要件定義 → ②基本設計 → ③詳細設計 → ④コード+テスト → ⑤UT（単体）→ ⑥IT（結合・本テンプレート）→ ⑦リリース
```

| 観点 | UT（単体） | IT（結合・本テンプレート） |
|------|------|------|
| 被テスト対象 | クラス／関数単位 | API 入口（Gateway）単位・画面（SCR）単位 |
| 用例の源泉 | 詳細設計 §5.1 UT 用例一覧 | **契約（SSOT）から導出**（API：`spec-07`／`spec-06`／`spec-09`／`spec-03`、画面：画面のイベント・検証設計） |
| 外部依存 | モック | API 結合は実システム・実 DB（ブラックボックス）。画面結合はネットワーク層のみモック（Context／子／router は実物） |
| 人手審査門 | なし | **あり**：式様書が「承認済み」になってからテストデータ・コードを生成する |
| ランナー | `*Test`（surefire） | `*IT`（failsafe、`verify` フェーズ） |

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.readme.s02" -->
## 2. ディレクトリ構成（`spec-test-rule.md` §3）

```
結合テスト/
├── README.md（本ファイル）
├── API/
│   ├── README.md                          利用手順（起動前提・実行方法・token 取得）
│   ├── 課題.md                            契約と実装の乖離の集約
│   ├── 式様書/
│   │   ├── spec-it-api-temp.md            テンプレート
│   │   └── spec-it-<operationId>.md       成果物（人手審査対象・正本）
│   ├── fixtures/
│   │   ├── README.md
│   │   ├── seed/                          事前 DB シード／フィクスチャ
│   │   ├── request/                       リクエスト例
│   │   └── expected/                      期待レスポンス断片（messageId・主要項目・マスク）
│   ├── it-api-runner/                     API テストコード（独立プロジェクト。reactor に含めない）
│   └── report/spec-it-testreport-<date>.md
└── フロントエンド/
    ├── README.md
    ├── 課題.md
    ├── 式様書/
    │   ├── spec-it-fe-temp.md             テンプレート
    │   └── spec-it-fe-<SCR-id>.md         成果物
    └── report/spec-it-fe-testreport-<date>.md
```

**使い方**：対象（operationId／SCR）ごとに該当テンプレートをコピーし、`-temp` を外して実識別子を埋めた名前（`spec-it-<operationId>.md`／`spec-it-fe-<SCR-id>.md`）で `式様書/` に保存する。

---

<!-- devtool:endblock -->
<!-- devtool:block id="integrationtest.readme.s03" -->
## 3. ファイル命名（`開発規約/spec-file-naming-rule.md` §4〜§5）

| 文書 | ファイル名 | 配置 |
|------|------|------|
| API 結合テスト式様書（テンプレート） | `spec-it-api-temp.md` | `結合テスト/API/式様書/` |
| API 結合テスト式様書（成果物） | `spec-it-<operationId>.md` | `結合テスト/API/式様書/` |
| 画面結合テスト式様書（テンプレート） | `spec-it-fe-temp.md` | `結合テスト/フロントエンド/式様書/` |
| 画面結合テスト式様書（成果物） | `spec-it-fe-<SCR-id>.md` | `結合テスト/フロントエンド/式様書/` |
| 結合テスト結果レポート | `spec-it-testreport-<date>.md`／`spec-it-fe-testreport-<date>.md` | `結合テスト/{API,フロントエンド}/report/` |

> 成果物名の `<operationId>`（camelCase）および `<SCR-id>`（大文字）は、識別子をそのまま埋め込むための**例外**として、命名規約 §2 の「すべて小文字」規則の適用外（`spec-file-naming-rule.md` §5）。テンプレート名自体は小文字規則に従う。
<!-- devtool:endblock -->
