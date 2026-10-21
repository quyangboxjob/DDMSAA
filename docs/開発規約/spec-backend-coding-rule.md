<!-- devtool:file-region id="document-header" -->
# Backend 開発規約（spec-backend-coding-rule）

> バックエンド（Spring Boot）層のアーキテクチャと実装規約。AI が任意のバックエンドコードを生成する際は、**必ず**本ファイルを [`spec-naming-rule.md`](spec-naming-rule.md)、[`spec-database-rule.md`](spec-database-rule.md) と同時にプロンプトへ含めること。
> 本ファイルは「基本設計/詳細設計 → バックエンドコード」間の実装層 SSOT である。

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s01" -->
## メタデータ
- version: 1.0.0
- target: Spring Boot / JDK / MyBatis / PostgreSQL（バージョンは `基本設計/共通/spec-01-system-architecture-ui.md` §2.2・§2.3 に従う）
- created: 2026-06-11
- status: approved

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s02" -->
## 0. 使用方法と優先順位

- AI がバックエンドコードを生成する際は、**必ず**次を読み込み厳密に遵守する：本ファイル + `spec-naming-rule.md` + `spec-database-rule.md` + 当該機能の詳細設計 + 3 つの SSOT yaml（`spec-06-db-physical.yaml` / `spec-07-api-list.yaml` / `spec-09-message-list.yaml`）。
- **優先順位（競合時）**：[`spec-generation-rule.md`](spec-generation-rule.md) §1 に従う（本ファイルは「各層の開発規約」の位置にある）。
- 命名規則は一律 `spec-naming-rule.md` を唯一の情報源とし、本ファイルでは命名を再定義せず、バックエンドでの適用例のみを示す。
- DB のテーブル・カラム・型・制約は `spec-database-rule.md` + `spec-06-db-physical.yaml` を正とし、本ファイルでは再定義しない。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s03" -->
## 1. 技術スタックとモジュール構成

> **技術スタックとバージョンは `基本設計/共通/spec-01-system-architecture-ui.md` §2.2 を唯一の情報源とする**（Spring Boot / JDK / MyBatis / MapStruct / PageHelper / Flyway / Spring Security(JWT, access-only) / Redis(認証一時状態: Magic Link token + 失敗カウント/ロック) / Bean Validation / Spring Scheduler / JUnit 5+Mockito / Maven）。本節ではバージョンを重複宣言せず、実装層の約束事のみを示す：

| 実装層の約束事 | 説明 |
|------|------|
| 永続層 | MyBatis（Mapper インタフェース + XML）。**JPA/Hibernate は禁止** |
| マイグレーション | Flyway による DDL のバージョン管理。DB の手動変更は禁止 |
| ページング | PageHelper を用い、`PageResult<T>` に統一してラップ |
| オブジェクトマッピング | MapStruct（Entity ↔ DTO） |
| 認証 | Spring Security + JWT（ステートレス、Gateway で署名検証） |

**サービス・パッケージ構成**（詳細設計 §0.2 と整合）

```
ddmsaa-backend/                       （マルチモジュール・マイクロサービス構成のルート。frontend/ と対称）
├── api-gateway/                     WebFlux、JWT 検証/ルーティング（:8080）
├── auth-service/                    パスワードレス Magic Link 認証 + JWT 発行（MVC、:8081）
├── business-service-core/           業務ドメイン（MVC + MyBatis、:8082）
│   ├── src/main/java/com/example/ddmsaa/core/<feature>/
│   │   ├── controller/  <Feature>Controller
│   │   ├── service/     <Feature>Service（インタフェース） + impl/ <Feature>ServiceImpl（実装・トランザクション境界）
│   │   ├── mapper/      <Feature>Mapper（MyBatis DAO インタフェース）
│   │   ├── entity/      <Feature>（テーブルマッピング、単数形 PascalCase）
│   │   ├── dto/         <Feature>CreateDto 等（OpenAPI スキーマに準拠）
│   │   └── convert/     <Feature>Converter（MapStruct）
│   └── src/main/resources/mapper/<Feature>Mapper.xml
├── common/                          jar：例外/Result/メッセージ/マスキング/暗号化/トレース（サービス横断の依存）
└── infrastructure/                  jar：DB/Redis(Lettuce)/ファイルストレージ/JWT ユーティリティ/RestClient/設定
```

- パッケージ名は全て小文字・区切りなし（spec-naming-rule §1.2）。`common`（例外/Result/マスキング/暗号化/トレースの基底クラス）、`infrastructure`（DB/ファイルストレージ/JWT ユーティリティ/**Redis**(Lettuce・認証一時状態: Magic Link token + 失敗カウント/ロック)/RestClient/設定）はサービス横断の依存とし、各 feature パッケージ内で重複実装しない。

---

<!-- rule: be.layering-responsibilities -->
<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s04" -->
## 2. 階層と責務（必須）

<!-- rule: be.converter-mapstruct -->
| 層 | 種別 | 責務 | 禁止事項 |
|----|------|------|------|
| Controller | `@RestController` | REST の受付、パラメータバインド、Service 呼び出し、`Result<T>` でのラップのみ | 業務ロジックの記述禁止、Mapper の直接呼び出し禁止 |
| Service（インタフェース） | interface | 業務契約 | — |
| ServiceImpl | `@Service` | 業務ロジック、**トランザクション境界**、Mapper/共通コンポーネントのオーケストレーション | HTTP/Servlet の概念の持ち込み禁止 |
| Mapper | interface + XML | 単一テーブル/単一集約の CRUD（SQL は XML） | 集約をまたぐ join による業務オーケストレーション禁止（オーケストレーションは Service） |
| Entity | POJO | テーブルマッピング。フィールド camelCase ↔ カラム snake_case | 業務メソッド禁止 |
| DTO | POJO | 入力/出力。**項目は OpenAPI スキーマに準拠** | Entity の直接公開禁止 |
| Converter | MapStruct | Entity ↔ DTO | 誤りやすい手書きマッピング禁止 |

- **Controller は Entity を返してはならない**。一律 DTO を `Result<T>` でラップして返す。
- **機能（feature）をまたぐ呼び出し**：feature パッケージは他の feature の Service・Mapper を直接参照しない。他の機能に処理を依頼する必要があるとき（例：承認が注文の状態を変える、注文の申請が承認依頼を作る、各機能が通知・監査を記録する）は、共通の中立パッケージ `com.example.ddmsaa.core.shared.port` に**ポート（interface）**を置き、呼ぶ側はポートに依存し、実装は担当する feature が提供する（依存方向を一方向に保ち、feature 間の循環を作らない）。ポートの一覧・責務・担当は `詳細設計/業務/order/spec-order-detail-sd.md` §1.1.1 を正とする。
- **列挙（T07）の置き場所**：`Role`（T07-1）は auth-service と business-service-core の両方で使うため `com.example.ddmsaa.common.enums`。それ以外の T07 列挙（`UserStatus`・`OrderStatus` など）は複数の feature から参照されるため `com.example.ddmsaa.core.shared.enums` に置く（feature の `dto` 配下には置かない）。列挙は値型なので、feature から `shared.enums` を参照してよい。
- 1 つの Controller は 1 組のリソースに対応させる（REST パスは複数形 kebab-case。spec-naming-rule §2.1 参照）。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s05" -->
## 3. 統一レスポンス

バックエンドは**表示文言を直接返さず**、`messageId`（+ `messageArgs`）のみを返す。フロントエンドが `messages.yaml` に基づいて解決する（仕組みは `spec-09-message-list.yaml` のヘッダーコメント参照）。

```java
// 正常：Result<T>
Result.ok(data, "I0010", List.of(userId));   // messageId + messageArgs
Result.ok(data);                                  // 通知文言が不要な場合
// ページング：PageResult<T>（list/total/pageNum/pageSize をカプセル化）
Result.ok(PageResult.of(list, total, pageNum, pageSize));
```

- `Result<T>` / `PageResult<T>` / `ErrorResponse` の項目構造は [`spec-07-api-list.yaml`](../基本設計/共通/spec-07-api-list.yaml) の `components.schemas`（`Result` / `ErrorResponse` / `FieldError`）を正とし、別途定義しない。
- ファイル系レスポンス（Excel/ZIP/画像バイナリ）は `Result` でラップせず、ストリームを直接書き込み + `Content-Disposition` を付与し、`@SkipResponseWrapper` 等の仕組みで統一エンベロープをスキップする。

---

<!-- rule: err.exception-to-http-mapping -->
<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s06" -->
## 4. 例外処理と 例外 → HTTP 固定マッピング（必須）

- グローバルな `@RestControllerAdvice` で一括捕捉し、`ErrorResponse(messageId, messageArgs, …)` に変換する。
- **HTTP ステータスは例外型で決定し、messageId とは独立**（同一 messageId は常に同一文言）。固定集合は次のとおり：

| 例外型 | HTTP | messageId |
|---------|:----:|---------------|
| `MethodArgumentNotValidException` / `ConstraintViolationException` | 400 | `E0008` |
| `MethodArgumentTypeMismatchException`（クエリ・パスの型変換失敗。不正な列挙値を含む）/ `HttpMessageNotReadableException`（JSON・ボディの列挙値・日付の形式不正）/ `MissingServletRequestParameterException` / `MissingServletRequestPartException`（multipart の必須パート欠落）/ `MissingPathVariableException` | 400 | `E0008`。`details[]` に項目（形式不正の詳細・パーサのメッセージは返さない） |
| `InvalidRequestException`（messageId 保持・既定 `E0008`。Service 層の業務判定で見つかった入力不整合。`BusinessException` を継承しない） | 400 | 例外の messageId（既定 `E0008`、空ファイルは `E0053`）。`details[]` に項目 |
| `AuthException`（messageId 保持）/ `AuthenticationException`（Spring Security）/ JWT 期限切れ | 401 | `AuthException` は例外の messageId（既定 `E0004`）、`AuthenticationException` は `E0004`、JWT 期限切れは `E0001` |
| `AccessDeniedException`（ロール/データ範囲の不足） | 403 | `E0002` |
| `EntityNotFoundException`（messageId 保持。リソース不存在） | 404 | 例外の messageId（既定 `E0005`） |
| `BusinessException`（messageId 保持。状態不正/業務競合） | 409 | 例外の messageId（例：`E0006` / `E0007`） |
| `MaxUploadSizeExceededException`（ファイルサイズ超過） | 413 | `E0010` |
| `PayloadTooLargeException`（messageId 保持。Service 層で判定するサイズ超過。`BusinessException` を継承しない） | 413 | 例外の messageId（1 ファイル `E0010`、注文合計 `E0052`） |
| ロック・レート制限（予約。Magic Link 認証では失敗ロックを使わないため現状未使用） | 429 | `E0003`（`E0005` は流用不可） |
| `DataAccessException` / `Exception`（未捕捉のシステム例外。スタックトレースを error ログ出力） | 500 | `E0012` |
| 下流到達不可 / タイムアウト（Gateway 層で変換。個別の対応は Gateway 設計が定める） | 502 / 504 | `E0013` / `E0043` |

- **messageId の存在要件**は `基本設計/共通/spec-09-message-list-ui.md` §1 に従う（生成ゲートで検証される）。
<!-- rule: err.business-exception-only -->
- 業務例外は一律 `BusinessException(messageId, args...)` をスローし、戻りコードやマジック文字列で失敗を表現してはならない。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s07" -->
## 5. 主キー・外部キー方針（Backend 側の適用）

- 主キー（既定の `VARCHAR(10)` 業務採番・`users` の `user_id`・共有主キー・自然キー master）の採番と外部キー列の命名は `開発規約/spec-database-rule.md` §4.1 / §4.2 に従う。テーブルごとの実際の主キーは `基本設計/共通/spec-06-db-physical.yaml` を正とする。
- **Entity**：主キーフィールドは業務キー（例：`String userId`）とし、**独立した代理キー `Long id` や `user_no` カラムは持たない**。API パスの `{userId}` は DB の `user_id` そのものであり、`id` の解決は不要。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s08" -->
## 6. MyBatis 規約

<!-- rule: be.mybatis-mapper-xml -->
- 1 Entity につき 1 つの `<Feature>Mapper` インタフェース + 同名の XML（`resources/mapper/`）。
- カラム↔フィールドは `resultMap` で明示的にマッピングする（または `mapUnderscoreToCamelCase` を有効化してもよいが、`resultMap` を優先し可読性を確保する）。
<!-- rule: be.soft-delete-query -->
- **論理削除**：すべてのクエリはデフォルトで `WHERE deleted_at IS NULL` を付与する（自動付与機構は持たず、XML 内に明示する）。削除は `UPDATE … SET deleted_at = now()` とし、物理 `DELETE` は禁止（spec-database-rule §4.3 の例外テーブルを除く）。
- **楽観ロック**（必要な場合）：`UPDATE … WHERE id = #{id} AND updated_at = #{updatedAt}`。更新行数 = 0 の場合は `BusinessException`(`E0051`) をスローする（`E0006` は「現在の状態では実行できない」専用）。`updatedAt` は秒未満を切り捨てずマイクロ秒 6 桁で入出力する（Jackson の書式 `yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX`）。楽観ロック競合は利用者操作の通常結果のため、監査ログには記録せず、アプリケーションログに INFO（traceId・対象 ID 付き）で出力する。
<!-- rule: be.paging-pagehelper-pageresult -->
- ページングは一律 `PageHelper`/ラッパー経由とし、`PageResult<T>` を返す。
- 動的条件には `<where>`/`<if>` を用い、文字列連結による SQL 組み立ては禁止（インジェクションリスク）。
- **カラムレベルでの機微データ分離**：例えば「一覧では個人情報を絶対に返さない」場合、上位層での非表示に依存せず、SELECT のカラムを絞り込んで実現する（詳細は各機能の詳細設計を参照）。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s09" -->
## 7. トランザクション

<!-- rule: be.transaction-serviceimpl-only -->
- `@Transactional` は **ServiceImpl のメソッドにのみ付与する**（Controller/Mapper には付与しない）。
- テーブル横断/集約の書き込み（例：「新規登録 = 4 テーブルへの INSERT」「レコード変更 = 旧レコードの無効化 + 新規登録」）は同一トランザクションで実行し、原子的にロールバックする。
- トランザクション内では **DB 操作のみ**を行う。メール送信やセッション無効化などの副作用は**トランザクションのコミット後**（`TransactionSynchronization` / イベント）に配置し、ロールバック後の誤送信を避ける。
- いずれかの手順で `BusinessException` をスローした場合は全体をロールバックする。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s10" -->
## 8. セキュリティ・権限（`spec-03-auth-ui.md` と整合）

権限 = **操作権限（機能レベル）∩ データ範囲（行レベル）∩ 項目閲覧（列レベル）** の 3 層の重ね合わせ。

<!-- rule: be.method-security-preauthorize -->
- **機能レベル**：`@PreAuthorize("hasRole('ADMIN')")` 等を Controller メソッドに付与する（ロール列挙はプロジェクトで定義する。例：`ADMIN`。API yaml の `Role` 参照）。
- **データ範囲（行レベル）**：ロールごとの範囲はプロジェクトで定義する（例：部門長は自部門のみ＝`department_id`、ユーザーは本人のみ＝`user_id` で絞り込み）。現在のログイン者の JWT（UserContext）から role と主体を取得し、Service/SQL で強制的に絞り込む。**フロントエンドに依存しない**。
- **権限拒否の監査（`PERMISSION_DENIED`。BR-AUD-001）**：403 は business-service-core の `PermissionDeniedAuditListener`（Spring Security の `AuthorizationDeniedEvent` を受ける）が `AuditLogPort.recordDenied` で記録する。範囲外の 1 件参照・操作による 404（BR-AUTH-006）は、範囲判定を行う `OrderAccessPort` などが `EntityNotFoundException` を投げる直前に同じ `recordDenied` を呼ぶ。`recordDenied` は呼び出し元の事務が巻き戻っても残るよう別事務（`REQUIRES_NEW`）で記録する。一覧での自動除外は記録しない。
- **項目閲覧（列レベル）**：詳細等は `spec-03-auth-ui.md` §3 の項目マトリクスに従い、共通の `MaskingService.maskByRole(dto, viewerRole, isSelf)` で一括処理する。マスク対象項目は `null` に設定し、Controller 内に分散させてはならない。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s11" -->
## 9. 暗号化・マスキング

- 暗号化カラム（`my_number` / `account_number` 等。yaml の `encrypted: true` 参照）は、DB 保存前に共通の `FieldEncryptor`（AES-GCM または基盤指定の方式）で暗号化し、読み取り後に権限に応じて復号する。
- 出力時にはさらにマスクを適用する（例：`accountNumber → "****4567"`、`myNumber → "****"`）。`MaskingService` と連携する。
- **鍵は決してリポジトリに含めない**（環境変数/鍵管理）。生成コードに鍵をハードコードしてはならない（生成ゲート G5 がスキャンする）。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s12" -->
## 10. 検証

- 入力は Bean Validation アノテーション（`@NotNull`/`@Email`/`@Size`/`@Pattern`）で検証し、制約は **OpenAPI スキーマの `required`/`maxLength`/`enum` に準拠**させる。
- 検証失敗 → 400 + `E0008` + `details[]`（項目レベルの `FieldError`）。
- 業務ルール検証（例：「同種の申請が審査中」「状態不正」）は Service 内で明示的に判定し、`BusinessException` とする。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s13" -->
## 11. 命名の適用（spec-naming-rule を引用し、再定義しない）

| 対象 | ルール | 例 |
|------|------|------|
| Entity | 単数形 PascalCase | `User`, `UserContact` |
| クラス接尾辞 | 単数形 + 接尾辞 | `UserService`, `UserServiceImpl`, `UserMapper`, `UserConverter` |
| DTO | `<Entity><用途>Dto` | `UserCreateDto`, `UserListItem` |
| メソッド/フィールド | camelCase | `findByUserId`, `userId` |
| 定数 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| API パス | 複数形 kebab-case | `/users`, `/system-locks` |
| テーブル | 複数形 snake_case | `users`, `user_contacts` |

- 層をまたぐ意味単位の順序を一致させる（spec-naming-rule Principle 1）。意味単位が変わる場合（広い語の絞り込み/予約語の置換）は、データ辞書に登録する（Principle 3）。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s14" -->
## 11.1 コメント・Javadoc 規約（必須）

- **すべての `public` 型**（class/interface/record/enum）に型レベルの Javadoc を付与する：責務の一文説明 + 主要な制約/境界/スレッドセーフティ等。
- **すべての `public` / `protected` メソッド**にメソッドレベルの Javadoc を付与する：動作説明 + `@param`（全引数）/ `@return`（`void` 以外）/ `@throws`（検査例外または規約上スローする例外。例：`BusinessException` の messageId の意味）。
- **例外（Javadoc の簡略化または省略可）**：自明な getter/setter、`@Override` メソッド（`{@inheritDoc}` を用いるか省略）、record の自動 accessor（record の型レベルで `@param` により各コンポーネントを説明すれば足りる）。
- private メソッド：複雑なロジックには一文の簡単な注記を付ける。自明な private は省略可。
- コメントの言語は本プロジェクトの文書と一致させる。**情報量のないプレースホルダーコメントは禁止**（例：`// getter`、説明のない `// TODO`）。
- **コード生成時に Javadoc も同時に出力する**（事後補完ではない）。機械検証は §13 G-javadoc を参照。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s15" -->
## 12. コード生成順序と出力先

**生成順序**（依存されるものを先に）：

```
Entity → Mapper(インタフェース+XML) → DTO → Converter(MapStruct) → [採番/ドメインサービス] → ServiceImpl → Controller → Batch
```

**出力先**：

| 成果物 | 配置先 |
|------|------|
| バックエンドコード | `ddmsaa-backend/business-service-core/src/main/java/com/example/ddmsaa/core/<feature>/…`（基盤＝common/infrastructure/api-gateway/auth-service の各モジュール）|
| MyBatis XML | `…/business-service-core/src/main/resources/mapper/<Feature>Mapper.xml` |
| Flyway DDL | `データベース` の Flyway マイグレーション（スクリプトの正本は 02 に帰属）|

**ファイル規約**：改行・文字コードは [`spec-generation-rule.md`](spec-generation-rule.md) §2 に従う。コード生成時は、リポジトリルートに下記の `.gitattributes`（**フロントエンド・バックエンド共通**）を**必ず**書き込む：

```gitattributes
* text=auto

# バックエンド（Java / Spring Boot）
*.java       text eol=lf
*.xml        text eol=lf
*.properties text eol=lf
*.wsdl       text eol=lf

# フロントエンド（React / Vite / TS）
*.ts   text eol=lf
*.tsx  text eol=lf
*.js   text eol=lf
*.jsx  text eol=lf
*.css  text eol=lf
*.html text eol=lf

# データベース / スクリプト / 文書 / 設定
*.sql  text eol=lf
*.ps1  text eol=lf
*.sh   text eol=lf
*.md   text eol=lf
*.yml  text eol=lf
*.yaml text eol=lf
*.json text eol=lf
*.txt  text eol=lf
.gitignore     text eol=lf
.gitattributes text eol=lf
.editorconfig  text eol=lf
Dockerfile     text eol=lf

# Windows スクリプト（例外：CRLF 必須）
*.bat text eol=crlf
*.cmd text eol=crlf

# バイナリ（正規化しない。破損防止）
*.jar      binary
*.war      binary
*.class    binary
*.xlsx     binary
*.docx     binary
*.zip      binary
*.pdf      binary
*.png      binary
*.jpg      binary
*.jpeg     binary
*.gif      binary
*.ico      binary
*.webp     binary
*.woff     binary
*.woff2    binary
*.ttf      binary
*.eot      binary
*.otf      binary
*.keystore binary
*.jks      binary
*.p12      binary
```

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s16" -->
## 13. 生成後の検証ゲート（機械検査可能、SSOT に対応）

| ゲート | チェック | 照合先 |
|----|------|------|
| G-列 | Entity/Mapper のカラム名・型・`encrypted` がテーブル定義と一致。PK=`VARCHAR(10)` の業務採番（users=`user_id`）、共有主キー/自然キー master | `spec-06-db-physical.yaml` + spec-database-rule |
| G-API | エンドポイントの path/method/operationId、リクエスト/レスポンスの項目・列挙値が一致 | `spec-07-api-list.yaml` |
| G-msg | 使用する `messageId` がすべて存在 | `spec-09-message-list.yaml` |
| G-命名 | ケース/単複/略語/意味単位 | `spec-naming-rule.md` + データ辞書 |
| G-アーキ | 階層/Result ラップ/例外→HTTP 固定集合/トランザクションが ServiceImpl にあること | 本ファイル §2–§7 |
| G-鍵 | ハードコードされた鍵/パスワードがない | 本ファイル §9 |
| G-改行 | ファイルが LF(`\n`) 改行、UTF-8 BOM なし、CRLF なし | `spec-generation-rule.md` §2 / `.gitattributes`・`.editorconfig` |
| G-javadoc | public 型/メソッドすべてに Javadoc あり（メソッドレベルは `@param`/`@return`/`@throws` を含む。自明な getter/setter・`@Override` は例外） | 本ファイル §11.1 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s17" -->
## 14. AI プロンプトテンプレート

```
## 前提（必ずすべて読み込み、厳密に遵守すること）
- spec-backend-coding-rule.md（本ファイル）
- spec-naming-rule.md / spec-database-rule.md
- 当該機能の詳細設計（§0.1 ロード一覧を含む）
- spec-06-db-physical.yaml / spec-07-api-list.yaml / spec-09-message-list.yaml（当該機能のスライス）

## タスク
詳細設計 §6 の生成順序に従って、クラスごとにバックエンドコードを生成する。競合時は承認済み規約を正とする。
生成後、§13 の検証ゲートを項目ごとにすべて通過させること。
```

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-coding-rule.s18" -->
## 改訂履歴

| バージョン | 日付 | 作成者 | 変更内容 |
|------|------|------|---------|
| 1.0.0 | 2026-06-11 | AI 草案 | 初版（裁定：代理キー id + 業務キー unique；MyBatis；例外→HTTP 固定集合） |
<!-- devtool:endblock -->
