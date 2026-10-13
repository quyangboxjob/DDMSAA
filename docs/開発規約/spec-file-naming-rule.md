# ファイル命名規範（spec-file-naming-rule）

> 本プロジェクトの「文書/仕様系」ファイルの命名規範である。コード/設定/フレームワーク固定名は一律対象外とする。
> 本ファイル自身も本規範に従う（自己証明）。
> 工程略号は**体系B：V字工程コード**（RD / UI / SD / UT / IT / ST）を採用し、「開発ワークフロー」と一致させる。

---

## 1. 適用範囲

**適用対象（文書/仕様系ファイル）**：規約、テンプレート、基本設計、詳細設計、業務処理フロー図、各種一覧/レポート、SSOT の `.md`／`.yaml`。

**対象外（改名不可。フレームワーク/ツールの固定名に従う）**：

- `SKILL.md`（Claude Code は `skills/<名>/SKILL.md` によってスキルを検出する）
- `*.java`、`pom.xml`、`application.yml`、`*.tmpl`
- `docker-compose.yml`、`nginx.conf`、`.env`、`*.sql`（`10_schema.sql` のような実行順序プレフィックスを含む）
- `README.md`（各ディレクトリの慣例的な入口名として維持）

---

<!-- rule: fname.doc-file-naming -->
## 2. 共通規則

1. **すべて小文字**とし、単語間はハイフン `-`（kebab-case）で区切る。空白、アンダースコア、中国語・日本語、大文字は使用しない。
2. **並び順の数字を保持**し、`spec-` の直後に置く：`spec-00-…`、`spec-06-…`。
3. **統一プレフィックス `spec-`**（適用範囲内の文書に限る）。
4. **命名構造の式**：

   ```
   spec-[序号-][<feature>-]<名称slug>-<分類接尾辞>.<ext>
   ```

   - `序号`：共通文書など番号付きのものにのみ使用（00~13）。
   - `<feature>`：詳細設計など機能単位で帰属するものに使用（機能名の英語 slug）。
   - `名称slug`：英語の短い語（§4 用語表を参照）。
   - `分類接尾辞`：§3 を参照。

---

## 3. 分類接尾辞（工程略号＝体系B）

> 体系B（V字工程コード）：**UI = 基本設計（外部設計）**、**SD = 詳細設計（内部設計）**、UT/IT/ST = 各テスト工程。

| 分類 | 接尾辞 | 拡張子 | 対応工程 |
|---|---|---|---|
| 規約（rule） | `-rule` | `.md` | 横断 |
| テンプレート（template） | `-temp` | `.md` / `.yaml` | 横断 |
| 基本設計 | `-ui` | `.md` | UI |
| 詳細設計 | `-sd` | `.md` | SD |
| 業務処理フロー図 | `-flow` | `.md` | SD（可視化・補助） |
| 生成ファイル一覧 | `-filelist` | `.md` | SD |
| 単体テスト仕様書 | `-unittest-sd` | `.md` | UT |
| テスト一覧 | `-testlist` | `.md` | UT/IT |
| テストレポート | `-testreport` | `.md` | UT/IT |
| 課題管理 | `-issue` | `.md` | 横断 |
| SSOT 機械可読 | （分類接尾辞なし。拡張子 `.yaml` で機械可読であることを示す） | `.yaml` | — |

> 対となる「文書 `.md` + 機械可読 `.yaml`」は**同一の slug** を使用し、拡張子のみで区別する。
> 例：`spec-07-api-list-ui.md` ＋ `spec-07-api-list.yaml`。

---

## 4. 推奨用語対照（手作業での改名の一貫性を保証する）

| 日本語 | slug | 日本語 | slug |
|---|---|---|---|
| 文書規範 | doc-standard | 要件定義 | requirement |
| システム構成方式設計 | system-architecture | 詳細設計 | detail |
| 権限設計 | auth | 認証機能 | auth |
| DB物理設計 | db-physical | 認可機能 | authorization |
| API一覧 | api-list | 例外ハンドラ | exception-handler |
| 外部インタフェース設計 | external-interface | 統一レスポンス | unified-response |
| 共通メッセージ一覧 | message-list | トレース_メッセージ | trace-message |
| 非機能要件 | non-functional | インフラストラクチャ | infrastructure |
| 画面共通設計 | screen-common | セキュリティユーティリティ | security-util |
| データ辞書 | data-dictionary | リソースファイル | resource-file |
| 画面一覧 | screen-list | ログ設定 | log-config |
| gateway | gateway | business-core骨格 | business-core |
| 単体テスト仕様書 | unittest | 結合テスト式様書 | it |

---

## 5. 機能単位の文書のファイル名例

### 詳細設計（SD 工程） `詳細設計/<機能名>/`

| ファイル名 | 内容 |
|---|---|
| spec-<feature>-detail-sd.md | 詳細設計 |
| spec-<feature>-flow.md | 業務処理フロー図 |
| spec-<feature>-filelist.md | 生成ファイル一覧 |
| spec-<feature>-testlist.md | テスト一覧 |
| spec-<feature>-testreport.md | テスト報告 |
| spec-<feature>-issue.md | 課題管理 |
| spec-business-issue.md | 業務課題管理 |

> 各機能も同じ規則に従う（`<feature>` を対応する機能の slug に置き換える）。
>
> **大規模機能の詳細設計分割**（任意。メインテンプレートの第1階層見出しで分割する。単一ファイルが大きくなりすぎる場合に使用）：`-detail-sd` の前に `backend`/`frontend`/`batch` の中間語を挿入する——
> `spec-<feature>-backend-detail-sd.md`（バックエンド）/ `spec-<feature>-frontend-detail-sd.md`（フロントエンド）/ `spec-<feature>-batch-detail-sd.md`（バッチ）。小規模機能は引き続き単一ファイル `spec-<feature>-detail-sd.md` を使用する。

### テスト文書（UT／IT 工程）

> テスト階層・ディレクトリ・ID 規則の詳細は `spec-test-rule.md` を正とする。

<!-- rule: fname.test-doc-names -->
| 文書 | ファイル名 | 配置 |
|---|---|---|
| 単体テスト仕様書 | spec-<対象>-unittest-sd.md | `単体テスト/{バックエンド,フロントエンド}/` |
| 単体テスト結果レポート | spec-ut-be-testreport-<date>.md／spec-ut-fe-testreport-<date>.md | `単体テスト/{バックエンド,フロントエンド}/report/` |
| API 結合テスト式様書 | spec-it-<operationId>.md | `結合テスト/API/式様書/` |
| 画面結合テスト式様書 | spec-it-fe-<SCR-id>.md | `結合テスト/フロントエンド/式様書/` |
| 結合テスト結果レポート | spec-it-testreport-<date>.md／spec-it-fe-testreport-<date>.md | `結合テスト/{API,フロントエンド}/report/` |

> 機能単位のテスト一覧・テストレポートは上記「詳細設計」表の `spec-<feature>-testlist.md`／`spec-<feature>-testreport.md` を使用する。
> 結合テスト式様書の `<operationId>`（camelCase）および `<SCR-id>`（大文字）は、識別子をそのまま埋め込むための例外として、§2 の小文字規則の適用外とする。

---

## 6. 未定（別途決定しない限り現状維持）

- **フォルダ名**（`基本設計/`、`開発規約/`、`詳細設計/`、`インタフェース仕様/` など）は本規範では当面扱わず、現状を維持する。英語化も行う場合は別途定める。
- 機能単位のテスト文書は当面 `-testlist` / `-testreport` を使用する（1部で UT/IT/ST の各層を網羅）。層ごとに `-ut` / `-it` / `-st` へ分割する必要がある場合は、別途決定する。
- システムテスト（ST）の文書名・配置は未定（`spec-test-rule.md` §11 参照）。
