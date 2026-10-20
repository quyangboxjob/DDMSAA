<!-- devtool:file-region id="document-header" -->
# 単体テストテンプレート

> 単体テスト仕様書（単体テスト仕様書）の文書テンプレート。バックエンド（Spring Boot）とフロントエンド（React）に分かれる。
> テスト階層・ID 規則・ディレクトリ・命名・テストデータの原則は [`開発規約/spec-test-rule.md`](../開発規約/spec-test-rule.md) を**唯一の正（SSOT）**とする。本テンプレートは規約を参照・具体化するのみで、規約を再定義しない。

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="unittest.readme.s01" -->
## 1. 位置付け

```
①要件定義 → ②基本設計 → ③詳細設計 → ④コード+テスト → ⑤UT（本テンプレートの対象）→ ⑥リリース
```

- **用例の唯一の源泉**：バックエンドの UT 用例は詳細設計 §5.1「UT 用例一覧」（`詳細設計/<機能名>/spec-<feature>-detail-sd.md`）である。テスト工程で用例を追加・発明しない。不足は詳細設計へ差し戻す。
- **期待値の根拠は SSOT**：messageId ← `spec-09-message-list.yaml`、HTTP 状態 ← 例外型ごとの固定マッピング、レスポンス構造 ← `spec-07-api-list.yaml`、マスク ← `spec-03-auth-ui.md`。実装の挙動をそのまま正解にしない。

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.readme.s02" -->
## 2. ファイル一覧

| ファイル | 用途 |
|------|------|
| `README.md`（本ファイル） | 位置付け・ファイル命名・ディレクトリ構成 |
| `バックエンド/spec-backend-unittest-temp.md` | バックエンド単体テスト仕様書テンプレート（L1／L2／L3） |
| `フロントエンド/spec-frontend-unittest-temp.md` | フロントエンド単体テスト仕様書テンプレート（純粋関数／フック／コンポーネント） |

**使い方**：対象（クラス群／機能）ごとに該当テンプレートをコピーし、`-temp` を外して `spec-<対象 kebab-case>-unittest-sd.md` として同じ層のフォルダに保存する。

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.readme.s03" -->
## 3. ファイル命名（`開発規約/spec-file-naming-rule.md` §3〜§5）

| 文書 | ファイル名 | 配置 |
|------|------|------|
| 単体テスト仕様書（テンプレート） | `spec-{backend,frontend}-unittest-temp.md` | `単体テスト/{バックエンド,フロントエンド}/` |
| 単体テスト仕様書（成果物） | `spec-<対象>-unittest-sd.md` | `単体テスト/{バックエンド,フロントエンド}/` |
| 単体テスト結果レポート | `spec-ut-be-testreport-<date>.md`／`spec-ut-fe-testreport-<date>.md` | `単体テスト/{バックエンド,フロントエンド}/report/` |
| JUnit XML（フロント） | `junit-<対象>-<date>.xml` | `単体テスト/フロントエンド/report/` |

> `-temp`＝テンプレート、`-unittest-sd`＝単体テスト仕様書（SD 工程の成果物）、`-testreport`＝結果レポートの各分類接尾辞に従う。

### ディレクトリ構成

```
単体テスト/
├── README.md
├── バックエンド/
│   ├── spec-backend-unittest-temp.md           テンプレート
│   ├── spec-<対象>-unittest-sd.md              成果物（コピーして記入）
│   └── report/spec-ut-be-testreport-<date>.md  結果レポート（直近 1 回分）
└── フロントエンド/
    ├── spec-frontend-unittest-temp.md          テンプレート
    ├── spec-<対象>-unittest-sd.md              成果物（コピーして記入）
    └── report/
        ├── spec-ut-fe-testreport-<date>.md     結果レポート（直近 1 回分）
        └── junit-<対象>-<date>.xml             JUnit XML（レポートのデータ源）
```

> テストコード本体の配置は `spec-test-rule.md` §3（バックエンドは `src/test/java`、フロントエンドは対象と同階層の `__tests__/`）に従う。本フォルダには仕様書・結果レポートのみを置く。
<!-- devtool:endblock -->
