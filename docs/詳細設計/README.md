<!-- devtool:file-region id="document-header" -->
# 詳細設計テンプレート

> 再利用可能な「詳細設計（内部設計）」フェーズの文書テンプレート。**詳細設計 = 基本設計の「外部に対して何をするか」を、コードレベルの「内部でどう実現するか」に落とし込むもの**であり、「基本設計 → コード」をつなぐ唯一の橋渡しである。
> 本テンプレートの成果物はそのまま AI によるコード生成・テストコード生成の根拠となるため、**厳密・機械可読・検証可能**であることを重視する。

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="detail.readme.s01" -->
## 1. 本テンプレートの開発プロセス上の位置付け

```
①要件定義 → ②基本設計 → ③詳細設計 → ④コード+テスト → ⑤UT → ⑥リリース・運用
                          ↑ 本テンプレートの対象範囲
```

| | 基本設計（外部設計） | 詳細設計（内部設計・本テンプレート） |
|---|---|---|
| 視点 | ユーザー・外部：システムが**外部に対して何をするか、どう見えるか** | 実装・内部：**内部でどう実現するか** |
| バックエンド | API 一覧（operationId）、リクエスト/レスポンス（OpenAPI） | クラス/メソッドシグネチャ、シーケンス、業務ロジック手順、SQL、トランザクション、例外マッピング |
| フロントエンド | 画面一覧、表示項目、遷移 | コンポーネントツリー分解、状態、イベント→API、クライアント検証、ロール別表示制御 |
| データ | 論理/物理データモデル | `spec-06-db-physical.yaml` のテーブル・カラム参照、論理削除/楽観ロック条件、SQL |
| 成果物の引渡し先 | 詳細設計へ引渡し | **AI によるコード・テスト生成へ引渡し** |

---

<!-- devtool:endblock -->
<!-- devtool:block id="detail.readme.s02" -->
## 2. 基本原則：単一情報源を参照し、決して再定義しない

詳細設計は下記の機械可読な単一情報源（SSOT）を**参照するのみで、複製しない**。項目、列挙値、メッセージ、エンドポイントはすべて SSOT に従い、詳細設計が補うのは SSOT に存在しない**実装詳細**（クラス構造、アルゴリズム手順、シーケンス、トランザクション、検証ルール、例外マッピング）である。

| SSOT | ファイル | 詳細設計での使い方 |
|------|------|--------------|
| DB テーブル・カラム | `基本設計/共通/spec-06-db-physical.yaml` | `テーブル名.カラム名` を参照し、カラム定義を再転記しない |
| API リクエスト・レスポンス・列挙値 | `基本設計/共通/spec-07-api-list.yaml` | `operationId` / スキーマ名を参照し、項目を再転記しない |
| メッセージ ID | `基本設計/共通/spec-09-message-list.yaml` | `messageId` を参照し、文言を再記述しない |
| 列挙コード値 | `開発規約/spec-data-dictionary-project-temp.md`（T07） | category_code を参照し、コード値を再列挙しない |
| 層横断の命名 | `開発規約/spec-naming-rule.md` | 命名は必ず本規約に従う。意味単位が変わる場合は登録必須 |
| アーキテクチャ・技術スタック | `基本設計/基盤/spec-00-architecture-ui.md` | クラス階層、例外→HTTP の固定集合、Result ラッパー |

> 詳細設計と SSOT が矛盾する場合は、**SSOT を正とする**。SSOT に不備（DB yaml の記入漏れ等）を発見した場合は、先に SSOT を補完してから詳細設計を記述する。

---

<!-- devtool:endblock -->
<!-- devtool:block id="detail.readme.s03" -->
## 3. ファイル一覧

| ファイル | 用途 |
|------|------|
| `README.md`（本ファイル） | 位置付け、原則、生成順序、AI コンテキスト組み立て、プレースホルダー規約 |
| `spec-detail-temp.md` | 詳細設計メインテンプレート。**機能ごとに 1 部コピーして記入する。** バックエンド/フロントエンド/バッチ/横断/トレーサビリティ（§5.1 UT 用例一覧を含む）/生成指示（§6.1 codegen 就緒ゲートを含む）の各ブロックを含む |

> **API の識別子は operationId に統一する**：詳細設計中の API は `spec-07-api-list.yaml` の `operationId`（camelCase の動詞。命名は `spec-naming-rule.md` 準拠）で識別し、`API-0NN` 形式の連番は使用しない。
> **UT 用例一覧（§5.1）**：テスト生成の唯一の入力として、各機能の詳細設計に必ず記入する。UT-ID は `UT-<FEATURE>-NNN`（`<FEATURE>` は機能 slug の英大文字表記、`NNN` は機能内 3 桁連番）で採番し、§5 追跡マトリクスの UT-ID 列から §5.1 にリンクする。テスト階層・ID 規則・ディレクトリは `開発規約/spec-test-rule.md` を参照。
> **codegen 就緒ゲート（§6.1 D1〜D11）**：記入後に自己検査し、全項目を満たしてからコード生成・テスト生成へ引き渡す。
>
> **小規模機能**：単一ファイル `spec-<feature>-detail-sd.md` を直接使用する。
> **大規模機能（状態遷移が複雑、バッチを含む機能など）**：メインテンプレートの第 1 階層見出しに従い `spec-<feature>-backend-detail-sd.md` / `spec-<feature>-frontend-detail-sd.md` / `spec-<feature>-batch-detail-sd.md` に分割し、単一ファイルの肥大化を避ける。

---

<!-- devtool:endblock -->
<!-- devtool:block id="detail.readme.s04" -->
## 4. 配置ディレクトリ構成

```
詳細設計/                              （インスタンスディレクトリ、本テンプレートから派生）
├── 〇〇機能/spec-〇〇-detail-sd.md
├── △△機能/spec-△△-detail-sd.md
└── □□機能/                            （複雑なため分割）
    ├── spec-□□-backend-detail-sd.md
    ├── spec-□□-frontend-detail-sd.md
    └── spec-□□-batch-detail-sd.md
```

**生成順序**：機能間の依存順（依存される側を先に作成）。

**共用画面/API の帰属**：「主管機能」で詳細設計を行い、他の機能は「主管機能より参照」と記載して重複設計しない（例：ユーザ一覧 SCR-01 はユーザ管理機能に帰属し、他の機能は参照）。

---

<!-- devtool:endblock -->
<!-- devtool:block id="detail.readme.s05" -->
## 5. AI コンテキスト組み立て（詳細設計 / コード生成時に「どのファイルを渡すか」）

詳細設計およびコード生成における最大のリスクは**コンテキストの渡し間違い**（多すぎれば注意が散漫になり、少なすぎれば幻覚を生む）である。各詳細設計の「0. 対象と根拠」節には、当該機能の「ロード一覧」を**必ず**記載すること。ベースライン一覧は下記のとおりで、各機能で必要に応じて取捨選択する。

**「詳細設計」生成時にロードするもの**
1. 当該機能の基本設計：`基本設計/共通/` の関連文書（権限・API・DB・画面共通など）
2. 上流要件の該当章：`要件定義.md`（関連小節のみ。全文ではない）
3. SSOT スライス：`spec-06-db-physical.yaml`（当該機能が扱うテーブル）、`spec-07-api-list.yaml`（当該機能の operationId 部分）、`spec-09-message-list.yaml`（関連 messageId）
4. 共通前提：`spec-03-auth-ui.md`、`spec-naming-rule.md`、`基盤テンプレート/spec-backend-foundation-temp.md`（アーキテクチャ節）

**「コード」生成時にロードするもの**
1. 当該機能の詳細設計（本テンプレートに記入した成果物）
2. 上記と同じ SSOT スライス + `開発規約/`（naming / database / data-dictionary / 各層 coding-spec）
3. `基盤テンプレート/spec-backend-foundation-temp.md`（パッケージ構成、Result/例外、MyBatis 規約）

**「テスト」生成時にロードするもの**
1. 当該機能の詳細設計（特に §5.1 UT 用例一覧。テスト用例の唯一の入力）
2. 対象の SSOT スライス（`spec-07-api-list.yaml` の operationId、`spec-09-message-list.yaml` の messageId、`spec-03-auth-ui.md` のマスキングマトリクス）
3. `開発規約/spec-test-rule.md` + 各層 coding-rule + `spec-naming-rule.md`

---

<!-- devtool:endblock -->
<!-- devtool:block id="detail.readme.s06" -->
## 6. プレースホルダー規約（基本設計テンプレートを踏襲）

| 記号 | 意味 |
|------|------|
| `〇〇` / `<…>` | 置換対象（注意：`〇` ≠ 記号 `○`「可」） |
| `{{ }}` | ブロック全体を機能ごとに書き直す |
| 例 | テンプレート内のサンプル行。正式な記入時は**削除する** |

> テンプレート内の例はすべて「DDMSAA」の機能を借りて示している。正式利用時は置換または削除すること。
> 文書ヘッダー、ID 規則（F-/SCR-/TBL-/BAT-/messageId。API は operationId）は一律 `基本設計/共通/spec-00-doc-standard-ui.md` に従う。
<!-- devtool:endblock -->
