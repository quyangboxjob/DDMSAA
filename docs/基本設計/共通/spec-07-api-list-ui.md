<!-- devtool:file-region id="document-header" -->
# 07 API 一覧・定義

<!-- rule: api.operation-id-identifier -->
> REST API の**人間可読な概要**。機械可読の SSOT は `spec-07-api-list.yaml`（OpenAPI 3.0。リクエスト/レスポンスの完全なスキーマを含む）であり、本 md ではスキーマを重複記載せず、共通規約とエンドポイント一覧のみを示して検索とトレーサビリティに供する。
> **API の正式な識別子 = `operationId`**（OpenAPI ネイティブで、コード生成がそのまま利用する）。**`API-0NN` 形式の採番は使用しない**。

（文書ヘッダは `spec-00-doc-standard-ui.md` を参照）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.common.07-api-list-ui.s01" -->
## 1. 共通規約

<!-- rule: api.base-path-v1 -->
<!-- rule: api.auth-jwt-bearer -->
<!-- rule: time.api-iso8601-offset -->
| 項目 | 内容 |
|------|------|
| ベースパス | `/api/v1` |
| データ形式 | JSON（UTF-8） |
| 認証 | JWT Bearer（`Authorization: Bearer <token>`。公開エンドポイントを除く） |
| 日付形式 | `+09:00` オフセット付き ISO 8601（`2026-06-10T10:00:00+09:00`）。`Z` / UTC 表記は用いない |
| 命名 | リソースパスは複数形・kebab（`/users`、`/system-locks`）、**項目は camelCase**、列挙値は UPPER_SNAKE |

### 共通レスポンスエンベロープ（yaml の `components.schemas` を正とする）

- 正常：`Result<T>` / ページング `PageResult<T>` —— `{ code, message, messageId, messageArgs[], traceId, timestamp, data }`
- 異常：`ErrorResponse` —— `{ messageId, messageArgs[], message, timestamp, path, traceId, details[] }`
<!-- rule: api.returns-message-id-only -->
- **表示文言は API から直接返さない**：API は `messageId`(+`messageArgs`) のみを返し、フロントエンドが `spec-09-message-list`（多言語時は `.ja/.zh/.en` ディレクトリ）に従って解決する。
<!-- rule: api.file-response-unwrapped -->
- ファイル系レスポンス（Excel/ZIP/画像）はバイナリと `Content-Disposition` をそのまま返し、Result で包まない。

<!-- rule: api.http-status-set -->
<!-- rule: api.status-independent-of-message-id -->
### HTTP ステータス（固定集合。messageId とは独立）

| コード | 意味 |
|----|------|
| 200 | 成功 |
| 400 | 入力チェックエラー |
| 401 | 未認証/認証失敗 |
| 403 | 権限不足 |
| 404 | リソースなし |
| 409 | 業務状態の競合 |
| 413 | ファイルサイズ超過 |
| 429 | ロック・流量制限 |
| 500 / 502 / 504 | システム / 下流到達不可 / タイムアウト |

例外型ごとのステータスと既定 messageId は `開発規約/spec-backend-coding-rule.md` §4 に従う。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.07-api-list-ui.s02" -->
## 2. API 一覧（tag 単位でグループ化。operationId が正式）

> ロール：`Role`（例：ADMIN / MANAGER / USER。ロールはプロジェクトで定義する）、全員、公開。詳細なリクエスト・レスポンスは `spec-07-api-list.yaml` を参照。以下は `例`（ユーザ系）であり、正式には本システムに合わせてエンドポイントごとに展開する。

| operationId | メソッド | パス | 概要 | F-ID | ロール |
|------|------|------|------|------|------|
| loginInit | POST | `/auth/login-init` | ログイン開始（ユーザID+方式。停止/ロック判定） | F-AUTH-01 | 公開 |
| createUser | POST | `/users` | 新規登録・採番 | F-USR-01 | ADMIN |
| pageUsers | GET | `/users` | 一覧（フィルタ・検索・ページング） | F-USR-05 | 全員 |
| disableUser | POST | `/users/{userId}/disable` | 停止処理（**POST**。PUT ではない） | F-USR-02 | ADMIN |
| {{ エンドポイントごとに展開 }} | | | | | |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.07-api-list-ui.s03" -->
## 3. API 定義テンプレート（必要に応じてエンドポイントごとに記載。詳細スキーマは yaml を正とする）

> 通常、**リクエスト/レスポンスの項目は `spec-07-api-list.yaml` を唯一の出典**とし、本 md では重複記載しない。人が読むための補足説明が必要な場合のみ、下記の雛形を記入する。

### 〇〇（`METHOD /path`）

| 項目 | 内容 |
|------|------|
| operationId | `〇〇`（camelCase の動詞。yaml を参照） |
| 機能 | F-〇〇 |
| メソッド・パス | `POST /xxx`（リソースは複数形 kebab） |
| 認証・権限 | JWT。ロール 〇〇（`spec-03-auth-ui.md` を参照） |
| リクエストスキーマ | `〇〇DTO`（camelCase の項目。yaml を参照） |
| レスポンス | `Result<〇〇>` / `Result<Void>`（成功時 messageId `I00NN`） |

**入力パラメータのチェック**

| 項目 | ルール | 違反時 messageId | HTTP |
|------|------|--------------|------|
| `〇〇`（camelCase） | 必須/形式/長さ（OpenAPI の制約に合わせる） | `E00NN` | 400 |

**エラー（例外→HTTP の固定セット。文言は 09 を参照）**

| ステータスコード | messageId | 発生条件 |
|--------|-----------|---------|
| 400 | `E00NN` | チェックエラー |
| 403 | `E00NN` | ロール/範囲の不足 |
| 404 | `E00NN` | リソースなし |
| 409 | `E00NN` | 状態の競合 |

### 記入要領

<!-- rule: api.operation-id-camel-verb -->
- **operationId は API の唯一の正式識別子**（camelCase の動詞。例：`createUser`）。詳細なスキーマ/example は一律 `spec-07-api-list.yaml` を参照し、本 md では重複記載しない（二重管理による乖離を避ける）。
- 1つの機能が複数のエンドポイント（CRUD 分割）に対応する場合があるため、対応する operationId をすべて列挙する。
<!-- rule: api.paging-params -->
- 一覧系 API はページングパラメータ（`pageNum`/`pageSize`。既定 1/20、`pageSize` 上限 100、超過は `400`）を統一し、`PageResult<T>` でラップする。
- 権限にはロールだけでなく、必要に応じて「本人のみ/自部門」などの行単位ルールも記載する（`spec-03-auth-ui.md` を参照）。
- 項目は一律 camelCase、列挙値は UPPER_SNAKE とする（`spec-naming-rule.md` を参照）。表示文言には messageId のみを用いる（`09` を参照）。
<!-- devtool:endblock -->
