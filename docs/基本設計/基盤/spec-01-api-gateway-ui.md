# 基盤基本設計 — api-gateway

> **生成元**：`基盤テンプレート/spec-backend-foundation-temp.md`（§3.2 / §4.1 / §4.3.1 / §4.3.2 / §4.3.4 / §9.4 / §9.5）＋ `基盤テンプレート/spec-java-springboot-mybatis-temp.yaml`（`stack.gateway_*` / `framework_mappings.security_config_reactive` / `framework_mappings.cors_reactive` / `framework_mappings.request_filter_reactive` / `framework_mappings.reactive_cleanup`）を入力として生成。
> 上位：[`spec-00-architecture-ui.md`](spec-00-architecture-ui.md)
> api-gateway = **Spring Cloud Gateway（リアクティブ / WebFlux）**、`spring-cloud-starter-gateway-server-webflux` に依存。境界制御のみ（JWT 検証 / ルーティング / CORS / セキュリティヘッダ / エラー整形 / traceId 貫通）、**業務を実装せず、JDBC / ブロッキング処理を実行しない**。ポート `8080`。
> 具体コード（クラス契約・コード本体）は機読 spec [`インタフェース仕様/spec-06-gateway.yaml`](インタフェース仕様/spec-06-gateway.yaml)。

## ドキュメント情報

| 項目 | 内容 |
|------|------|
| ドキュメント名称 | 基盤基本設計 — api-gateway |
| システム名 | DDMSAA |
| 上位ドキュメント | `spec-00-architecture-ui.md` |
| ドキュメントバージョン | V1.1 |
| 作成日 | 2026-06-22 |
| 最終更新日 | 2026-06-30 |

---

## §1 責務と制約

### §1.1 責務一覧

| # | 責務 | 概要 |
|---|------|------|
| 1 | JWT 検証 | `/api/v1/auth/**` および `/actuator/health` を除く全パスで Bearer Token を検証（署名 / exp / issuer）。JWT は**発行しない**（発行は auth-service が担当）。 |
| 2 | ルーティング | 設定駆動でリクエストを auth-service（8081）または business-service-core（8082）へ転送。サービスディスカバリなし。 |
| 3 | CORS | `CorsWebFilter @Bean`（Java 実装）でオリジン制御・プリフライト処理を統一。 |
| 4 | セキュリティヘッダ | `X-Content-Type-Options` / `X-Frame-Options` / `Cache-Control` / `Strict-Transport-Security` を全応答に付与。 |
| 5 | エラー整形 | リアクティブ例外を統一 JSON 形式（`messageId` / `timestamp` / `path` / `traceId`）へ変換。 |
| 6 | traceId 貫通 | 受信 `X-Request-Id` を受信（なければ UUID no-hyphen 生成）→ MDC へ格納 → 下流透過 → 応答 `X-Trace-Id`。 |

### §1.2 禁止事項

以下の実装を api-gateway モジュールに含めることを**禁止**する。

| 禁止項目 | 理由 |
|----------|------|
| 業務ロジックの実装 | api-gateway は境界制御専用。業務は各業務サービスが担う（テンプレート §3.2）。 |
| JDBC / リレーショナル DB アクセス | WebFlux ランタイム上でのブロッキング IO 禁止（テンプレート §3.2）。 |
| ブロッキング処理全般 | イベントループをブロックするコード（`Thread.sleep` / 同期 HTTP 呼び出し等）禁止。JWT 検証は CPU 軽量操作のため許容。 |
| JWT 発行 | 発行は auth-service の責務。Gateway は検証のみ行う（テンプレート §4.1.2）。 |
| BCrypt / パスワード検証 | 本プロジェクトはモード②（Magic Link パスワードレス）採用。パスワード列・BCrypt は生成しない。 |

---

## §2 ルーティング

### §2.1 設計方針

- **設定駆動**：ルーティングは `application.yml` で宣言し、サービスディスカバリを使用しない。
- **URL 注入**：宛先 URL は環境変数経由の設定値（`app.services.auth.base-url` / `app.services.core.base-url`）で注入する。
- **優先順位**：`/api/v1/auth/**`（auth ルート）が `/api/v1/**`（core ルート）より**先に評価**される。Spring Cloud Gateway はルート定義の記載順に評価するため、auth ルートを先に記述する。

### §2.2 ルーティング設定

```yaml
app:
  services:
    auth:
      base-url: ${SVC_AUTH_URL:http://localhost:8081}
    core:
      base-url: ${SVC_CORE_URL:http://localhost:8082}

spring:
  cloud:
    gateway:
      routes:
        - id: auth
          uri: ${app.services.auth.base-url}
          predicates:
            - Path=/api/v1/auth/**
        - id: business-core
          uri: ${app.services.core.base-url}
          predicates:
            - Path=/api/v1/**
```

### §2.3 ルーティング対応表

| ルート ID | パスプレフィックス | 転送先 | ポート |
|-----------|------------------|--------|--------|
| `auth` | `/api/v1/auth/**` | auth-service | 8081 |
| `business-core` | `/api/v1/**` | business-service-core | 8082 |

> 注：JWT 検証鍵（`JWT_SECRET`）と issuer は auth-service と api-gateway で共有する（環境変数 `JWT_SECRET` / `JWT_ISSUER` を両サービスに注入）。

---

## §3 GlobalFilter 構成

バインディング `framework_mappings.request_filter_reactive` = `GlobalFilter (implements, WebFlux)` / `framework_mappings.filter_order_highest` = `Ordered.HIGHEST_PRECEDENCE` を適用。

### §3.1 TraceIdGlobalFilter

| 項目 | 値 |
|------|-----|
| クラス | `filter.TraceIdGlobalFilter` |
| 実装 | `GlobalFilter` implements + `Ordered` implements |
| order | `Ordered.HIGHEST_PRECEDENCE`（最高優先度） |

**処理フロー：**

1. 受信ヘッダ `X-Request-Id` を取得。
2. 値が存在しない場合は UUID（ハイフン除去・32文字）を生成する。
3. `MDC.put("traceId", value)` でログコンテキストに格納。
4. 下流リクエストに `X-Request-Id` ヘッダを付与して透過する。
5. 応答ヘッダに `X-Trace-Id` を付与して返す。
6. バインディング `framework_mappings.reactive_cleanup` = `doFinally` に従い、`doFinally` で `MDC.remove("traceId")` を呼び出してクリアする（メモリリーク防止）。

> ログパターンは `logback-spring.xml` で `%X{traceId:--}` / `%X{userId:--}` を含める（バインディング `log_pattern_trace` 参照）。

### §3.2 JwtAuthGlobalFilter

| 項目 | 値 |
|------|-----|
| クラス | `filter.JwtAuthGlobalFilter` |
| 実装 | `GlobalFilter` implements + `Ordered` implements |
| order | `Ordered.HIGHEST_PRECEDENCE + 1`（TraceId の次） |

**permit-all（検証スキップ）パス：**

| パス | 理由 |
|------|------|
| `/api/v1/auth/**` | Magic Link 認証エンドポイント全体（`login-init` / `magic-link/verify` / `magic-link/resend` 等）。テンプレート §9.5 モード② に基づく。 |
| `/actuator/health` | ヘルスチェック（テンプレート §9.1）。 |

**保護パスの処理フロー：**

```
Authorization ヘッダ取得
│
├─ 欠落 → 401 / E0040（Token 欠落）
│
└─ "Bearer <token>" あり
    │
    ├─ 期限切れ（ExpiredJwtException）→ 401 / E0001
    │
    ├─ 署名不正 / issuer 不一致 / その他 → 401 / E0041
    │
    └─ 検証成功
        → 下流リクエストに以下ヘッダを付与して透過：
           X-User-Id   = JWT sub（ユーザーID）
           X-User-Role = JWT role claim（例：ADMIN）
```

`JwtAuthGlobalFilter` が返すエラーの HTTP / messageId は §6.3 のエラーマッピング表に従う。

**制約事項：**

- Gateway は**業務を復号せず DB を照会しない**（JWT claims 解析のみ）。
- 停用判定およびロック判定（`system_locks.lock_started_at IS NOT NULL AND lock_started_at <= now`）は業務サービス側が担う。Gateway は JWT の有効性のみ確認する。
- `X-User-Role` に設定する値は JWT role claim の ENUM 文字列（例：`ADMIN`）。DB の INT 値はこのレイヤでは扱わない。

---

## §4 CORS

バインディング `framework_mappings.cors_reactive` = `CorsWebFilter @Bean` を適用。

### §4.1 実装方針

- **Java 実装**（`config.CorsConfig` クラス）で `@Bean CorsWebFilter` を定義する。`spring.cloud.gateway.globalcors.*` の YAML 設定は使用しない。
- `CorsWebFilter` は `JwtAuthGlobalFilter` よりも先に評価されるため、OPTIONS プリフライトリクエストは認証 Filter を通過しない。

### §4.2 CORS 設定値

| 設定項目 | 値 |
|----------|-----|
| `allowedOriginPatterns` | フロントドメインのホワイトリスト（設定値 `app.cors.allowed-origins`、環境変数注入） |
| `allowedMethods` | `GET` / `POST` / `PUT` / `DELETE` / `PATCH` / `OPTIONS` |
| `allowedHeaders` | `*`（全許可） |
| `exposedHeaders` | `X-Trace-Id` |
| `allowCredentials` | `true` |
| `maxAge` | `3600`（秒） |

### §4.3 プリフライト処理

OPTIONS リクエストは `CorsWebFilter` で即座に `200 OK` を返し、認証 Filter（`JwtAuthGlobalFilter`）を通過させない。これはバインディング `framework_mappings.cors_reactive` の `CorsWebFilter` が Spring WebFlux のフィルタチェーンで認証より先に処理されることで実現する。

---

## §5 セキュリティヘッダ

全応答（成功・エラー問わず）に以下のヘッダを付与する。実装は `GlobalFilter` または `WebFilter` で行う。

| ヘッダ名 | 値 | 目的 |
|----------|----|------|
| `X-Content-Type-Options` | `nosniff` | MIME スニッフィング防止 |
| `X-Frame-Options` | `DENY` | クリックジャッキング防止 |
| `Cache-Control` | `no-store, no-cache, must-revalidate` | 機微応答のキャッシュ禁止 |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` | HTTPS 強制（HTTPS 終端時のみ有効） |

> `Strict-Transport-Security` はリバースプロキシ（nginx 等）が HTTPS 終端を行う場合に有効。ローカル開発環境では無効化可。

---

## §6 エラー整形

### §6.1 実装クラス

| クラス | 役割 |
|--------|------|
| `error.GatewayErrorHandler` | `ErrorWebExceptionHandler` を実装。全リアクティブ例外を統一 JSON へ変換する。`ObjectMapper` で直書きし、`common` モジュールの `ErrorResponse` クラスには依存しない。 |

バインディング `framework_mappings.gateway_error_handler` = `ErrorWebExceptionHandler (WebFlux)` を適用。

### §6.2 統一エラー応答形式

```json
{
  "messageId": "E0040",
  "timestamp": "2026-06-30T10:00:00+09:00",
  "path": "/api/v1/users",
  "traceId": "a1b2c3d4e5f6..."
}
```

| フィールド | 型 | 説明 |
|------------|-----|------|
| `messageId` | string | エラー識別子。`共通/spec-09-message-list.yaml` に存在必須。 |
| `timestamp` | string | `+09:00` オフセット付き ISO 8601 形式。 |
| `path` | string | リクエストパス。 |
| `traceId` | string | MDC の traceId（`TraceIdGlobalFilter` が設定）。 |

### §6.3 エラーマッピング表（全シナリオ）

<!-- rule: err.gateway-http-mapping -->
| シナリオ | 例外 / 発生源 | HTTP | messageId |
|----------|--------------|:----:|-----------|
| Token 欠落（Authorization ヘッダなし） | `JwtAuthGlobalFilter` 判定 | 401 | `E0040` |
| JWT 期限切れ | `ExpiredJwtException` | 401 | `E0001` |
| JWT 署名不正 / 不正形式 / issuer 不一致 | `JwtException` 等 | 401 | `E0041` |
| ルート未一致（404） | `NotFoundException` / Gateway 組み込み | 404 | `E0042` |
| 下流サービス接続到達不可 | `ConnectException` / `ResponseStatusException(502)` | 502 | `E0013` |
| 下流サービス応答タイムアウト | `TimeoutException` / `ResponseStatusException(504)` | 504 | `E0043` |
| Gateway 内部エラー | `Exception`（その他） | 500 | `E0012` |

### §6.4 二重ラップ防止

下流サービス（auth-service / business-service-core）がすでに `ErrorResponse` 形式で返却した応答は、`GatewayErrorHandler` で二重ラップしない。下流からの応答ボディをそのまま返却する。

> 全 `messageId` の定義は `共通/spec-09-message-list.yaml` を参照。

---

## §7 セキュリティ設定

バインディング `framework_mappings.security_config_reactive` = `@EnableWebFluxSecurity SecurityWebFilterChain` を適用。

### §7.1 SecurityConfig

| 項目 | 設定値 |
|------|--------|
| クラス | `config.SecurityConfig` |
| アノテーション | `@EnableWebFluxSecurity` |
| CSRF | 無効（`csrf.disable()`）。純 API シナリオのため（テンプレート §4.3.2）。 |
| デフォルト認可 | `anyExchange().permitAll()`。認可は `JwtAuthGlobalFilter` に完全委譲。 |

**WebFlux 制約（バインディング `stack.gateway_runtime = webflux` に基づく）：**

- `@EnableWebSecurity`（MVC 用）を使用しない。`@EnableWebFluxSecurity` を使用する。
- `BCryptPasswordEncoder` / `UserDetailsService` / `JDBC` 等のブロッキング依存は一切含めない。
- `SessionCreationPolicy.STATELESS` 相当（セッションレス）。WebFlux では `ServerHttpSecurity.sessionManagement()` で設定。

### §7.2 JWT 設定

```yaml
app:
  jwt:
    secret: ${JWT_SECRET}               # 必須・環境変数
    issuer: ${JWT_ISSUER:ddmsaa}
    expiration-seconds: 3600            # HS256 / access token のみ / refresh なし
```

| JWT パラメータ | 値 |
|----------------|-----|
| アルゴリズム | `HS256`（バインディング `stack.jwt_algorithm`） |
| `sub` | ユーザーID（ユーザーの業務採番 VARCHAR(10)） |
| `role` claim | ENUM 文字列（例：`ADMIN`） |
| `exp` | 発行時刻 + 3600 秒 |
| issuer | `app.jwt.issuer` の値 |

> JWT ライブラリ：`io.jsonwebtoken:jjwt-api / jjwt-impl / jjwt-jackson`（バージョンは `基本設計/共通/spec-01-system-architecture-ui.md` §2.2 に従う、バインディング `stack.jwt_*` 参照）。

---

## §8 ファイル透過

### §8.1 アップロード透過

- マルチパート（`multipart/form-data`）リクエストを業務サービスへそのまま転送する。
- Gateway はボディを変換しない（リアクティブストリームとして透過）。
- MIME タイプとファイルサイズの検証は業務サービス側（または Gateway の `RequestSize` フィルタ）で行う。

### §8.2 ダウンロード透過

- 業務サービスからのバイナリストリーム（Excel / PDF / ZIP / 画像等）を `Content-Disposition` / `Content-Type` を保持したままクライアントへストリーミング転送する。
- Gateway はバイナリボディを変換・バッファリングしない。

### §8.3 MIME / サイズ検証

| 検証項目 | 担当 | 設定 |
|----------|------|------|
| MIME タイプホワイトリスト | 業務サービス側 | 業務要件に応じて設定（テンプレート §4.3.5）。 |
| アップロードサイズ上限 | Spring Cloud Gateway `RequestSize` GatewayFilter または業務サービス側 | 超過時 HTTP 413、`messageId: E0010`（`spec-04-common-component-ui.md` §3.1 と統一。413=E0010。E0012 は 500 系専用）。 |

> ファイルストレージ詳細は `spec-04-common-component-ui.md §7` を参照。

---

## §9 設定台帳（api-gateway）

```yaml
# api-gateway / application.yml（抜粋）
server:
  port: 8080
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
  cloud:
    gateway:
      routes:
        - id: auth
          uri: ${app.services.auth.base-url}
          predicates: [ "Path=/api/v1/auth/**" ]
        - id: business-core
          uri: ${app.services.core.base-url}
          predicates: [ "Path=/api/v1/**" ]

app:
  jwt:
    secret: ${JWT_SECRET}
    issuer: ${JWT_ISSUER:ddmsaa}
    expiration-seconds: 3600
  services:
    auth:
      base-url: ${SVC_AUTH_URL:http://localhost:8081}
    core:
      base-url: ${SVC_CORE_URL:http://localhost:8082}
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

> 設定キーの正本は詳細設計の「生成契約（設定台帳）」（`spec-06-gateway.yaml`）とする（テンプレート §10.3）。

---

## §10 パッケージ構成

```
com.example.ddmsaa.gateway
├── config
│   ├── CorsConfig.java          # CorsWebFilter @Bean
│   └── SecurityConfig.java      # @EnableWebFluxSecurity
├── filter
│   ├── TraceIdGlobalFilter.java # order=HIGHEST_PRECEDENCE
│   └── JwtAuthGlobalFilter.java # order=HIGHEST_PRECEDENCE+1
└── error
    └── GatewayErrorHandler.java # ErrorWebExceptionHandler 実装
```

> モジュール FQCN prefix：`com.example.ddmsaa.gateway`（プロジェクト固有情報より）。

---

## §11 外部 SSOT 参照

| 参照対象 | ファイル | 用途 |
|----------|----------|------|
| API 一覧 | `共通/spec-07-api-list.yaml` | エンドポイント定義・permit-all パスの確認 |
| メッセージ目録 | `共通/spec-09-message-list.yaml` | `messageId` 存在確認（E0001 / E0012 / E0013 / E0040 / E0041 / E0042 / E0043） |
| 命名規約 | `spec-naming-rule.md` | クラス名・設定キー・ヘッダ名の命名 |

---

## 改訂履歴

| バージョン | 日付 | 改訂内容 |
|------------|------|----------|
| V1.0 | 2026-06-22 | `基盤基本設計.md` §5（Gateway）を独立ドキュメント化。ルーティング yaml・GlobalFilter（order）・CORS（Java `@Bean CorsWebFilter`）・セキュリティヘッダ・エラー→messageId 表・`@EnableWebFluxSecurity` を集約。具体コードは機読 spec へ誘導。 |
| V1.1 | 2026-06-30 | テンプレート＋バインディングを明示的に参照元として記載。§1 責務と禁止事項を独立セクション化。§3 GlobalFilter に doFinally / MDC.clear() 詳細追記。§4 CORS を Java 実装方針として明確化。§6 エラーマッピング表を全シナリオに拡充。§7 SecurityConfig の WebFlux 制約と JWT パラメータ詳細追記。§8 ファイル透過セクション追加。§9 設定台帳・§10 パッケージ構成・§11 外部 SSOT 参照追加。 |
