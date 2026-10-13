# 基盤基本設計 — 共通コンポーネント（common / infrastructure）

> 上位：[`spec-00-architecture-ui.md`](spec-00-architecture-ui.md)。実装規約：`開発規約/spec-backend-coding-rule.md`。
> 横断 jar（`common` / `infrastructure`）の各コンポーネント設計。各サービスは依存で再利用し、**feature パッケージ内で重複実装しない**。
> 各クラスの公開シグネチャ・コード本体は機読 spec [`インタフェース仕様/`](インタフェース仕様/)（`spec-03-exception-handler` / `spec-04-unified-response` / `spec-05-trace-message` / `spec-07-infrastructure` / `spec-08-security-util` / `spec-02-authorization`）を参照。

---

## 生成元

| 項目 | 内容 |
|------|------|
| テンプレート | `基盤テンプレート/spec-backend-foundation-temp.md` |
| バインディング | `基盤テンプレート/spec-java-springboot-mybatis-temp.yaml` |
| 言語 / FW | Java 21 LTS / Spring Boot 3.5.13 / MyBatis 3.0.4 |
| JWT ライブラリ | jjwt 0.13.0（HS256） |
| ページング | PageHelper 2.1.1 |
| 認証モード | テンプレート §9.5 モード②（Magic Link パスワードレス）のみ採用 |

---

## ドキュメント情報

| 項目 | 内容 |
|------|------|
| ドキュメント名称 | 基盤基本設計 — 共通コンポーネント |
| システム名 | DDMSAA |
| 上位ドキュメント | `spec-00-architecture-ui.md` |
| ドキュメントバージョン | V1.2 |
| 作成日 | 2026-06-30 |

---

## §1 統一応答（common · response）

フィールド構造の正本は `共通/spec-07-api-list.yaml` の `components.schemas`（`Result` / `PageResult` / `ErrorResponse` / `FieldError`）とする。本節は実装形態（`record` / `class`）と実装規約のみを示す。

### §1.1 型定義

<!-- rule: resp.value-object-form -->
| 型 | 種別 | 説明 |
|------|------|------|
| `Result<T>` | `record`（イミュータブル） | 正常系統一ラップ |
| `PageResult<T>` | `record`（イミュータブル） | ページング結果ラップ |
| `ErrorResponse` | `class`（可変） | 異常系統一ラップ |
| `FieldError` | `record`（イミュータブル） | バリデーション失敗の単一フィールドエラー |

> バインディング `value_object_form`：`record` はアクセサ `fieldName()` 形式（`getXxx()` 禁止）。`ErrorResponse` のみ `class`（`getFieldName()` / `setFieldName()`）。`record ⇄ class` の変換禁止（呼び出し側を壊す）。

### §1.2 `Result<T>` 詳細

- **フィールド**：`Result` に従う（`traceId` は MDC から取得、`timestamp` は `OffsetDateTime.now()`、`data` 省略時は null）
- **シリアライゼーション**：null フィールドは JSON 出力から省略（`@JsonInclude(NON_NULL)`）
- **factory メソッド**：
  - `Result.ok(data)` — data のみ成功応答
  - `Result.ok(data, messageId, args...)` — messageId 付き成功応答

### §1.3 `PageResult<T>` 詳細

- **フィールド**：`PageResult` に従う（`pages` は総件数とページサイズから切り上げ除算で自動計算）
- **factory メソッド**：`PageResult.of(records, total, pageNum, pageSize)`
- **禁止別名**：`getItems()` / `getSize()` 等の別名定義禁止（アクセサ名揺れ防止）
- `pages` 計算：`(int) Math.ceil((double) total / pageSize)`（total=0 時は 0）

### §1.4 `ErrorResponse` 詳細

- **フィールド**：`ErrorResponse` に従う（`timestamp` は `OffsetDateTime.now()`、`path` は `HttpServletRequest.getRequestURI()`、`traceId` は `MDC.get("traceId")`、`details[]` は 400 時のみ）
- **factory メソッド**：`ErrorResponse.of(messageId, path, traceId)` ＋ setter で `messageArgs` / `details` を追加

### §1.5 `FieldError` 詳細

- **定義**：`record FieldError(String field, String reason)`
- 用途：`ErrorResponse.details[]` に格納するフィールド単位バリデーション失敗情報

### §1.6 ラップ方式・その他規約

<!-- rule: resp.explicit-wrap -->
- **ラップ方式**：Controller が明示的に `Result.ok()` でラップする。`ResponseBodyAdvice` による自動ラップは採用しない。
- **ファイルストリーム応答**：`基本設計/共通/spec-07-api-list-ui.md` §1 に従う（`Result` ラップを経由しない）。
- **traceId 取得元**：MDC（`TraceIdFilter` が設定、§5 参照）。
- **timestamp**：`OffsetDateTime.now()`（サーバ現在時刻）。
- messageId（+`messageArgs`）の返却と文言解決は `基本設計/共通/spec-09-message-list-ui.md` に従う。

---

## §2 メッセージ機構（common · message）

### §2.1 正本・配置

- **SSOT**：`共通/spec-09-message-list.yaml`（+`.zh` / `.en`）
- **配置**：`common/src/main/resources/messages/` に 1 コピーのみ配置（重複コピー禁止）
- **改行コード**：`開発規約/spec-backend-coding-rule.md` §12 に従う
- 各実行サービス（auth-service / business-service-core）は `common` jar への依存で classpath 共有し、ローカルコピーを持たない

### §2.2 `MessageCatalog` インタフェース

```
インタフェース: MessageCatalog
メソッド:
  String resolve(String messageId, Object... args)  // サーバ側ログ用文言解決
  boolean exists(String messageId)                  // 生成門 G-Msg 検証用
```

- Bean 登録なし（実装クラスを `@Component` で登録する）

### §2.3 `YamlMessageCatalog` 実装

- アノテーション：`@Component`
- 起動時に `messages/*.yaml` を全件ロード（存在しない場合は起動失敗）
- `{0}` `{1}` プレースホルダを `args` 配列で順次置換して返却
- スレッドセーフ（ロード済みマップは不変保持）

### §2.4 採番ルール

messageId の形式・区分・番号帯は `基本設計/共通/spec-00-doc-standard-ui.md` §3 および `基本設計/共通/spec-09-message-list-ui.md` に従う。動的値は `{0}` `{1}` プレースホルダで表現し、バックエンドは `messageArgs[]` に値を格納して返却する。

### §2.5 生成門（G-Msg）

`messageId` の存在要件は `基本設計/共通/spec-09-message-list-ui.md` §1 に従う。存在しない `messageId` を使用するコードは生成門により拒否される。

---

## §3 グローバル例外処理（common · exception）

`@RestControllerAdvice`（`GlobalExceptionHandler`）で統一捕捉し `ResponseEntity<ErrorResponse>` を返却する。

### §3.1 例外マッピング

例外型 → HTTP ステータスと messageId の固定マッピングは `開発規約/spec-backend-coding-rule.md` §4 に従う。Gateway 側の変換は `基本設計/基盤/spec-01-api-gateway-ui.md` §6 を参照。

### §3.2 カスタム例外クラス

業務失敗の表現は以下 3 クラスに限定し、リターンコード・マジック文字列・`null` 返却での暗黙エラーを禁止する。

| クラス | 基底 | コンストラクタシグネチャ | 主要メソッド |
|--------|------|------------------------|------------|
| `AuthException` | `RuntimeException` | `AuthException(String messageId, String... args)` | `getMessageId()` / `getArgs()` |
| `BusinessException` | `RuntimeException` | `BusinessException(String messageId, String... args)` | `getMessageId()` / `getArgs()` |
| `EntityNotFoundException` | `RuntimeException` | `EntityNotFoundException(String messageId, String... args)` | `getMessageId()` / `getArgs()` |

- `BusinessException(messageId, args...)` が業務失敗の唯一の手段
- 3 クラスとも `(String messageId, String... args)` コンストラクタ＋`getMessageId()` / `getArgs()` を持つ

### §3.3 `details[]` 格納規約

- `details[]` への `FieldError` 格納は **400 バリデーション失敗時のみ**
- 各エントリは `FieldError{field: "フィールド名", reason: "失敗理由"}` の単位で格納
- 他の HTTP ステータスでは `details[]` を設定しない（null または空リスト）

### §3.4 `path` / `traceId` 取得

- `path`：`HttpServletRequest.getRequestURI()` から取得
- `traceId`：`MDC.get("traceId")` から取得（`TraceIdFilter` が事前に設定、§5 参照）

---

## §4 認証（infrastructure · jwt）

本プロジェクトはテンプレート §9.5 **モード②（Magic Link パスワードレス）のみ**を採用する。BCrypt / パスワード列 / refresh token は一切生成・使用しない。

### §4.1 コンポーネント表

| コンポーネント | 配置モジュール | 責務 |
|------|------|------|
| `JwtUtil` | `infrastructure · jwt` | JWT 発行（auth-service）/ 検証（各サービス）；HS256 |
| `JwtAuthenticationFilter` | `infrastructure · jwt` | Bearer トークン検証・コンテキスト設定；`SecurityConfig @Bean` で登録 |
| `UserContext` | `infrastructure · jwt` | ログイン者情報の ThreadLocal 保持 |
| `AuthStateStore` | `infrastructure · jwt` | Redis を通じた認証一時状態（Magic Link / 失敗カウント）管理 |

### §4.2 `JwtUtil`

- 実装形式：`@Component`（または `SecurityConfig` が `@Bean` 登録）
- JWT ライブラリ：jjwt 0.13.0（`io.jsonwebtoken:jjwt-api` / `jjwt-impl` / `jjwt-jackson`）
- アルゴリズム：HS256
- 鍵：環境変数 `JWT_SECRET`（`app.jwt.secret: ${JWT_SECRET}`）— リポジトリ・ハードコード禁止
- **claim 仕様**：`基本設計/基盤/spec-02-auth-service-ui.md` §6.2 に従う（`sub` / `role` / `iss` / `exp`）
- **公開メソッド**：
  - `String issue(String userId, String role)` — JWT 生成（auth-service が呼び出す）
  - `boolean validate(String token)` — 署名・有効期限・issuer 検証
  - `String getSubject(String token)` — `sub` claim 取得（ユーザーID）
  - `String getRole(String token)` — `role` claim 取得（ENUM 文字列）
- **expiration-seconds**：設定キー `app.jwt.expiration-seconds`
- role は auth-service 内で DB 値（INT）から ENUM 文字列にマッピングして JWT claim に格納し、各サービスは claim から直接取得する

### §4.3 `JwtAuthenticationFilter`

- 基底クラス：`extends OncePerRequestFilter`
- **登録方式**：`@Component` を付与しない。`SecurityConfig` が `@Bean` として登録する（Spring Security フィルタチェーンへの組み込みを明示制御するため）
- **処理フロー**：
  1. `Authorization: Bearer <token>` ヘッダを抽出
  2. `JwtUtil.validate(token)` で検証（失敗時は 401、フィルタ処理継続しない）
  3. 検証成功時：
     - `UserContext.set(userId, role)` でスレッドローカルに設定
     - `SecurityContextHolder` に `UsernamePasswordAuthenticationToken`（authorities=`[ROLE_<role>]`）を設定
     - `MDC.put("userId", userId)` でログコンテキストに設定
  4. `finally` ブロックで `UserContext.clear()`（スレッドリーク防止）
- **`shouldNotFilter` 実装**：permitAll パス（§4.7）はフィルタをスキップ

### §4.4 `UserContext`

- 実装：`ThreadLocal<UserInfo>` を static で保持
- **公開メソッド**：
  - `void set(String userId, String role)` — ログイン者情報を設定
  - `String getUserId()` — ユーザーID取得（非 HTTP スレッドは null 返却、例外なし）
  - `String getRole()` — ロール ENUM 文字列取得（非 HTTP スレッドは null 返却、例外なし）
  - `void clear()` — ThreadLocal をクリア
- **必ず `finally` で `clear()` を呼び出すこと**（`JwtAuthenticationFilter` が担う）
- Service の行レベルフィルタ・`created_by` / `updated_by` 取得に利用する

### §4.5 `AuthStateStore`

- 実装場所：`infrastructure · jwt`
- 依存：`spring-boot-starter-data-redis`（Lettuce クライアント）
- 責務：Magic Link 資格情報・失敗カウント・ロック状態の Redis 入出力
- **Magic Link 操作**（GETDEL で一回使い切り）：
  - キー：`auth:magic-link:{token}`
  - TTL：3600 秒（`app.auth.magic-link.ttl-seconds: 3600`）
  - 生成：`SecureRandom` 32 バイト → Base64URL エンコード（パディングなし）
  - 検証：`GETDEL`（原子操作、取得と同時に削除）
- **失敗カウント / ロック操作**：
  - キー：`auth:login:fail:{ユーザーID}`
  - TTL：1800 秒（`app.auth.login-lockout-seconds: 1800`）
  - 失敗上限：5 回（`app.auth.max-login-failure-count: 5`）
  - 初回 `INCR` 時に `EXPIRE` を設定；上限到達でロック状態とみなす
- 詳細な行動契約は `インタフェース仕様/spec-08-security-util.yaml` `behavior_contracts.AuthStateStore` を参照
- **access token のみ**（refresh token なし）；期限切れは再ログイン

### §4.6 ロール定義

ロールはプロジェクトで定義する（プロジェクト固有の規約）。下表は例。

| ロール名（ENUM 文字列 / JWT claim） | DB 値（INT） | 備考 |
|------|------|------|
| `ADMIN` | 1 | 管理者 |
| `MANAGER` | 2 | 部門責任者 |
| `USER` | 3 | 一般ユーザー（デフォルト） |

- DB 列は `INT`、API レスポンス / JWT claim は ENUM 文字列（DB 値との変換は auth-service が担う）

### §4.7 公開エンドポイント

認証不要（permitAll）のパスは `基本設計/基盤/spec-02-auth-service-ui.md` §4 に従い、Gateway および各サービスの `SecurityFilterChain` で `permitAll()` とする。

---

## §5 ログ・トレース・マスキング（common · web / security）

### §5.1 `TraceIdFilter`

- **MVC 実装**（auth-service / business-service-core）：`@Component`、`extends OncePerRequestFilter`、`@Order(Ordered.HIGHEST_PRECEDENCE)`
- **Gateway 実装**（api-gateway）：`implements GlobalFilter`（WebFlux、リアクティブ）
- **処理フロー**：
  1. 受信ヘッダ `X-Request-Id` を取得；なければ UUID（ハイフン除去、UUID no-hyphen）を自動生成
  2. `MDC.put("traceId", traceId)`
  3. ログイン済みの場合は `MDC.put("userId", userId)` も設定
  4. リクエスト処理の `doFinally`（MVC は `finally` ブロック）で `MDC.clear()` を実行（スレッドリーク防止）
  5. 応答ヘッダ `X-Trace-Id` に traceId を設定して返却

### §5.2 logback 設定

- **ログパターン**：`%X{traceId:--} %X{userId:--}` を含める
- 統一出力項目：`traceId`、`userId`（ユーザーID）、`level`、`message`
- **`logback-spring.xml` の配置先**：実行モジュール（api-gateway / auth-service / business-service-core）にのみ配置
- 共有 jar（`common` / `infrastructure`）には `logback-spring.xml` を配置しない

### §5.3 `DataMaskUtil`

- 実装形式：`class`（static ユーティリティメソッド）
- **公開メソッド**：
  - `String maskKeepLast(String value, int last)` — 末尾 `last` 文字を残し、前方を `*` でマスク
  - `String maskAll(String value)` — 全文字を `*` に置換
- null または空文字はそのまま返却（例外なし）

### §5.4 `MaskingService`

- 実装形式：`@Component`（no-op フック署名）
- フィールドマスクマトリクス（ロール × フィールド）は業務 DTO 内で実装する（基盤は no-op のフック署名のみ提供）
- マスク対象例：電話番号 / メール / 個人番号（id_number） / 銀行口座番号（account_number）
- **機微情報の平文をログに書き込むことを禁止**

### §5.5 監査 / 履歴方針

- 重要業務操作の状態変更は業務履歴テーブル（`request_status_histories` 等）に記録する（業務 DTO / Mapper に属する）
- 認証失敗カウント / ロック等の一時状態はキャッシュ（Redis、`AuthStateStore`）で管理する
- 一般操作は統一ログ（traceId / userId）で痕跡を残す
- **`sys_audit_log` テーブルは単独で設けない**（プロジェクト方針）

---

## §6 暗号化（common · security · FieldEncryptor）

### §6.1 暗号化対象列

暗号化対象は `データベース/schema/schema-definition.yaml` の `encrypted: true` 列が正本。代表例：

| 列名 | テーブル | DB 型 | 備考 |
|------|---------|-------|------|
| `id_number` | users | `VARCHAR(512)` | 個人番号 |
| `account_number` | （業務テーブル） | `VARCHAR(512)` | 銀行口座番号 |
| `request_content` | （業務テーブル） | `VARCHAR(512)` | 申請内容（機微フィールド） |

暗号文は `VARCHAR(512)` に格納する（Base64 エンコード済み暗号文のサイズを考慮）。

### §6.2 `FieldEncryptor` 仕様

- 配置：`com.example.ddmsaa.common.security.FieldEncryptor`
- **アルゴリズム**：AES-GCM
  - IV（初期化ベクタ）：12 バイト（`SecureRandom` 生成）
  - 認証タグ：128 ビット
  - 出力形式：`Base64(iv‖cipher)`（IV 12 バイト + 暗号文を連結して Base64 エンコード）
- **鍵取得**：`app.crypto.aes-key`（Base64 エンコード済み、環境変数 `AES_KEY` で注入）
  - 未設定時：`IllegalStateException` をスロー（起動時 fail-fast）
  - 鍵はリポジトリ・ハードコード禁止（生成門 G-鍵）
- **null / 空文字の扱い**：
  - null 入力 → null 返却
  - 空文字入力 → 空文字返却
- **公開メソッド**：
  - `String encrypt(String plaintext)` — 平文を暗号化して Base64 文字列を返す
  - `String decrypt(String ciphertext)` — Base64 暗号文を復号して平文を返す

### §6.3 読み取り時の権限制御

読み取り時は `UserContext.getRole()` で取得したロールに応じて以下の処理を行う：

| ロール（例） | `id_number` | `account_number` | 備考 |
|--------|------------|-----------------|------|
| `ADMIN` | 復号して返却 | 復号して返却 | 全フィールドアクセス可 |
| `MANAGER` | マスク（`maskAll`） | マスク（`maskKeepLast(4)`） | フィールドマスクマトリクスは業務側で実装 |
| `USER` | マスク（`maskAll`） | マスク（`maskKeepLast(4)`） | 自己の情報のみアクセス可（データ範囲は §三層権限） |

> 具体的なフィールドマスクマトリクスは業務 DTO 内（`MaskingService` のオーバーライド実装）で定義する。基盤は no-op フック署名を提供するのみ。

---

## §7 ファイルストレージ（infrastructure · storage）

### §7.1 `FileStorageService` インタフェース

- 配置：`com.example.ddmsaa.infrastructure.storage.FileStorageService`
- **公開メソッドシグネチャ**（詳細な行動契約は `インタフェース仕様/spec-07-infrastructure.yaml` `behavior_contracts.FileStorageService` 参照）：

| メソッド | シグネチャ | 説明 |
|--------|-----------|------|
| upload | `void upload(String objectKey, InputStream content, long contentLength, String contentType)` | オブジェクトをアップロードする |
| download | `InputStream download(String objectKey)` | オブジェクトをストリームで返す |
| delete | `void delete(String objectKey)` | オブジェクトを削除する |
| presignedUrl | `String presignedUrl(String objectKey, int expirySeconds)` | 署名付き URL を生成して返す |

- `put` / `get` 等の別名定義禁止（インタフェース契約の一貫性）

### §7.2 `S3FileStorageService` 実装

- アノテーション：`@ConditionalOnProperty(name = "app.file.storage-type", havingValue = "s3")`
- 依存：`software.amazon.awssdk:s3`（AWS SDK for Java v2 2.x、`software.amazon.awssdk:bom` で版数管理）
- 設定プレフィックス：`app.file.s3.*`（`region` / `bucket` / `prefix`（任意）/ `endpoint`（任意）/ `path-style`（任意、既定 false）/ `presign-ttl-seconds`（既定 300））
- 署名付き URL は `S3Presigner` で生成する
- 資格情報は設定・環境変数のアプリ独自項目に持たず、SDK の既定資格情報チェーン（ECS タスクロール / インスタンスロール等）を使用する
- `endpoint` / `path-style` は S3 互換のローカル開発用 MinIO 接続時のみ指定する（コード変更不要）。この場合のみ SDK 標準の `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` 環境変数で資格情報を与える（ローカル開発のみ）

### §7.3 バリデーション規約

- **MIME ホワイトリスト**：許可 MIME タイプを設定化（業務要件に応じて定義）
- **サイズ上限**：設定化（超過時は `MaxUploadSizeExceededException` → HTTP 413 / `E0010`）
- ファイルメタデータと内容は分離して管理する

### §7.4 ライフサイクル管理

- 処理済みファイルは保持する（即時削除しない）
- 期限切れクリーンアップの閾値：`app.export.file-expiry-days`（日数単位）
- 業務ルール（申請画像の保持期間等）は業務 DTO / 各機能の詳細設計を参照

---

## §8 サービス間通信（infrastructure · rest · RestClientFactory）

### §8.1 設計方針

- HTTP クライアント：Spring `RestClient`（Spring Boot 付属）
- **サービスディスカバリなし**；宛先 URL は設定で注入（`app.services.<name>.base-url`）

| サービス | 設定キー | デフォルト URL |
|---------|---------|--------------|
| auth-service | `app.services.auth.base-url` | `http://localhost:8081` |
| business-service-core | `app.services.core.base-url` | `http://localhost:8082` |

### §8.2 `RestClientFactory` 責務

- 配置：`com.example.ddmsaa.infrastructure.rest.RestClientFactory`
- **自動透過ヘッダ**（下流への伝播）：
  - `X-User-Id` / `X-User-Role` — `UserContext.getUserId()` / `getRole()`（認証済みスレッドのみ）。Gateway の下流伝播方式と統一。生 Bearer は `UserContext` に保持しないため `Authorization` 転送は行わない。
  - `X-Request-Id` — `MDC.get("traceId")`。空なら UUID（ハイフンなし）を生成して常時付与。
- **タイムアウト設定**：接続 / 読み取りタイムアウトを設定化（`app.services.connect-timeout-seconds` / `app.services.read-timeout-seconds`、秒単位）。接続失敗→`BusinessException(E0013)`、読み取りタイムアウト→`BusinessException(E0043)`、下流 4xx/5xx→ラップ。

### §8.3 障害時の変換規約

| 発生箇所 | 障害種別 | 変換後 |
|---------|---------|-------|
| api-gateway | 下流到達不可 | HTTP 502 / `ErrorResponse` |
| api-gateway | 下流タイムアウト | HTTP 504 / `ErrorResponse` |
| auth-service / business-service-core | 下流エラー応答 | `BusinessException` または システム例外へ変換 |

---

## §9 MyBatis と DB 基盤（infrastructure）

### §9.1 接続・マッパー設定

- **接続プール**：HikariCP（設定プレフィックス `spring.datasource.hikari.*`）
- **SQL マッパー**：ネイティブ MyBatis（`mybatis-spring-boot-starter:3.0.4`）。MyBatis Plus は使用しない。
- **列↔フィールドマッピング**：`resultMap` を使用。`map-underscore-to-camel-case: true` で snake_case → camelCase を自動変換。

### §9.2 論理削除

論理削除の列定義は `開発規約/spec-database-rule.md` §4.3、クエリ条件は `開発規約/spec-backend-coding-rule.md` §6 に従う。

### §9.3 監査列

<!-- rule: be.audit-columns-population -->
| 列名 | 設定主体 | タイミング |
|------|---------|-----------|
| `created_by` | `MetaObjectHandler`（MyBatis プラグイン） | INSERT 時、`UserContext.getUserId()` から取得 |
| `updated_by` | `MetaObjectHandler`（MyBatis プラグイン） | UPDATE 時、`UserContext.getUserId()` から取得 |
| `created_at` | DB デフォルト値 | INSERT 時 |
| `updated_at` | DB トリガー `set_updated_at` | UPDATE 時 |

### §9.4 主キー戦略

主キーの形式・採番・採番関数（DB 側で定義し、Flyway スクリプトで作成する）は `開発規約/spec-database-rule.md` §4.1 に従う。

### §9.5 ページング

- プラグイン：PageHelper（`pagehelper-spring-boot-starter:2.1.1`）
- 戻り値：`PageResult<T>`（§1.3 参照）
- クエリパラメータ：`pageNum` / `pageSize`（既定値・上限は `共通/spec-07-api-list-ui.md` に従う）

### §9.6 トランザクション

- `@Transactional` を Service 実装クラスのメソッド単位で宣言する
- 読み取り専用処理：`@Transactional(readOnly = true)`

### §9.7 DDL / 関数 / トリガー

- スキーマ定義・採番関数・トリガーはすべて `データベース` の Flyway スクリプトで作成する
- 永続化の業務側規約は `spec-03-business-service-ui.md §4` を参照

### §9.8 三層権限とデータアクセス

本プロジェクトは以下の三層権限制御を実装する：

| 層 | 実装手段 | 説明 |
|----|---------|------|
| ①機能レベル | `@PreAuthorize("hasRole('ROLE_NAME')")` | エンドポイント単位の認可 |
| ②データ範囲 | `UserContext` → SQL フィルタ条件 | 行レベルフィルタ（部門・自己参照等） |
| ③フィールド | `FieldEncryptor` + `MaskingService` | 暗号化列の復号・マスク |

---

## §10 DB マイグレーション（Flyway · infrastructure 統合）

### §10.1 基本方針

- **SSOT**：`データベース`（スクリプト正本・移行戦略の基本設計も同所）
- **バージョン管理規約**：`開発規約/spec-flyway-migration-rule.md`
- 基盤（バックエンド）側はスクリプトの**実行統合のみ**を担う
- 機読契約の正本：`基本設計/基盤/インタフェース仕様/spec-project-structure.yaml`（依存・plugin・配置）＋ `インタフェース仕様/spec-09-resource-file.yaml`（`spring.flyway` 設定）

### §10.2 依存設定

- **配置モジュール**：`infrastructure`
- **依存スコープ**：`compile`（推移的に実行サービスの classpath に載るため）
- **依存アーティファクト**：

| アーティファクト | 用途 |
|--------------|------|
| `flyway-core` | Flyway コア |
| `flyway-database-postgresql` | PostgreSQL 対応ドライバ |

- バージョンは Spring Boot BOM 管理（`基本設計/共通/spec-01-system-architecture-ui.md` §2.2 に従う）
- `api-gateway` は `infrastructure` 非依存のため Flyway を搭載しない（データソースなし）

### §10.3 スクリプト配置

- **配置先**：`infrastructure/src/main/resources/db/migration/`
- **ファイル命名**：`開発規約/spec-flyway-migration-rule.md` §2 に従う
- auth-service / business-service-core は `infrastructure` への依存で共有（同源のため分岐しない）

### §10.4 `spring.flyway` 設定（auth-service / business-service-core 共通）

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    schemas: public
    default-schema: public
    baseline-on-migrate: true
    baseline-version: 0
```

- 接続スキーマは `public` に統一（auth-service の datasource も `currentSchema=public`）

### §10.5 Baseline 戦略

- スキーマ構築は別機構（bootstrap）が担い、Flyway の版管理対象外とする
- 既存の非空 DB は `baseline(version=0)` で認領し、**V1 以降のみ適用**する（初期移行データ = V1）
- 起動時自動 migrate（advisory lock で多重起動を直列化）
- 独立実行：`mvn -pl infrastructure flyway:migrate`
- UT / 開発サンプルデータは Flyway 管理外（test profile / 手動 psql）

---

## モジュール FQCN プレフィックス一覧

> 導出元：`基盤テンプレート/spec-java-springboot-mybatis-temp.yaml` `project.module_packages`
> 変更時はバインディングファイルを修正し、本表を再生成する。

| モジュール | FQCN プレフィックス |
|---------|-----------------|
| api-gateway | `com.example.ddmsaa.gateway` |
| auth-service | `com.example.ddmsaa.auth` |
| business-service-core | `com.example.ddmsaa.core` |
| common | `com.example.ddmsaa.common` |
| infrastructure | `com.example.ddmsaa.infrastructure` |

---

## ポート一覧

| サービス | ポート |
|---------|------|
| api-gateway | 8080 |
| auth-service | 8081 |
| business-service-core | 8082 |

---

## 改訂履歴

| バージョン | 日付 | 改訂内容 |
| --- | --- | --- |
| V1.2 | 2026-06-30 | テンプレート（共通模板）・バインディング（spec-java-springboot-mybatis-temp.yaml）を参照して全セクションを再構成。§1〜§10 を必須セクション指示に準拠して拡充。モード②（Magic Link）のみ採用を明示し、BCrypt / パスワード列 / refresh token の除外を全セクションに反映。ロール定義・JWT claim・AuthStateStore・三層権限・停用/ロック判定の DDMSAA 固有情報を統合。 |
| V1.1 | 2026-06-22 | §10「DB マイグレーション（Flyway · infrastructure 統合）」を追加。auth-service datasource を `currentSchema=public` に統一。 |
| V1.0 | 2026-06-22 | `基盤基本設計.md` §4（共通基盤コンポーネント）を独立ドキュメント化。common / infrastructure の責務・契約を集約。 |
