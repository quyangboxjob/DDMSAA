<!-- devtool:file-region id="document-header" -->
# 基盤基本設計 — business-service（層構成・実装規約）

<!-- 生成元
  テンプレート : 基盤テンプレート/spec-backend-foundation-temp.md
  バインディング: 基盤テンプレート/spec-java-springboot-mybatis-temp.yaml
  プロジェクト  : DDMSAA
  生成日        : 2026-06-30
-->

> 上位：[`spec-00-architecture-ui.md`](spec-00-architecture-ui.md)。実装層規約 SSOT：`開発規約/spec-backend-coding-rule.md`（層構成 / Result / 例外 / MyBatis / トランザクション / 権限 / 暗号化）。
> 本書はマイクロサービス着地と共通機構の差異のみを示し、`spec-backend-coding-rule` を重複しない。**業務領域 Entity/Mapper/ロジックは各機能の詳細設計に属する**、本書は骨格と共通機構のみ定義。ポート `8082`。
> 共通コンポーネントの実装詳細は [`spec-04-common-component-ui.md`](spec-04-common-component-ui.md)。骨格コードは [`インタフェース仕様/spec-10-business-core.yaml`](インタフェース仕様/spec-10-business-core.yaml)。

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.foundation.springboot.03-business-service-ui.s01" -->
## ドキュメント情報

| 項目 | 内容 |
|------|------|
| ドキュメント名称 | 基盤基本設計 — business-service |
| 上位ドキュメント | `spec-00-architecture-ui.md` |
| ドキュメントバージョン | V1.1 |
| 作成日 | 2026-06-22 |
| 最終更新日 | 2026-06-30 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.foundation.springboot.03-business-service-ui.s02" -->
## §1 レイヤー構成とパッケージ構造（バインディング適用済み）

### §1.1 七層モデル

層の責務・禁止事項は `開発規約/spec-backend-coding-rule.md` §2 に従う。Feature 単位のクラス名は §1.2 のパッケージ構造のとおりとする（Converter は MapStruct の `@Mapper`）。

### §1.2 パッケージ構造

`business-service-core` 配下のパッケージ構造（FQCN prefix: `com.example.ddmsaa.core`）：

```
business-service-core/
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/ddmsaa/core/
    │   │       ├── <feature>/
    │   │       │   ├── controller/   <Feature>Controller
    │   │       │   ├── service/      <Feature>Service
    │   │       │   │   └── impl/     <Feature>ServiceImpl
    │   │       │   ├── mapper/       <Feature>Mapper
    │   │       │   ├── entity/       <Feature>
    │   │       │   ├── dto/          <Feature>CreateDto / <Feature>UpdateDto / <Feature>Dto
    │   │       │   └── convert/      <Feature>Converter
    │   │       └── BusinessServiceCoreApplication.java
    │   └── resources/
    │       ├── application.yml
    │       └── mapper/
    │           └── <Feature>Mapper.xml
    └── test/
        └── java/
            └── com/example/ddmsaa/core/
                └── <feature>/
                    └── service/impl/  <Feature>ServiceImplTest
```

**パッケージ配置原則：**

- `<feature>` は業務ドメイン単位（例: `user`, `department`, `request`）
- 疎通スケルトン（`PingController` 等）も `<feature>` 中間パッケージを挟まず `controller` 直下に配置する
- `common` / `infrastructure` は jar 依存とし、`<feature>` 内での重複実装を禁止する（例外/Result/マスキング/暗号化/JWT フィルタ/トレース/DB）

### §1.3 Mapper XML の配置

- XML ファイルは `src/main/resources/mapper/<Feature>Mapper.xml` に配置する
- namespace は対応する Mapper インタフェースの FQCN（`com.example.ddmsaa.core.<feature>.mapper.<Feature>Mapper`）
- Mapper と XML の対応は `開発規約/spec-backend-coding-rule.md` §6 に従う

### §1.4 モジュール依存方針

- `common` jar：例外/応答/ログ/トレース/マスキング/メッセージ — `compile` 依存
- `infrastructure` jar：DB/Redis/JWT/ストレージ/RestClient/設定 — `compile` 依存
- **差異（vs spec-backend-coding-rule 単体）**：本基盤はマイクロサービスであり、各ドメインが独立した実行可能モジュール。サービス間呼び出しは `RestClient`（§8 参照）、プロセス内で他サービスの Mapper を直接呼ばない（Mapper 直接呼び出し禁止）。

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.foundation.springboot.03-business-service-ui.s03" -->
## §2 統一応答（バインディング適用済み）

### §2.1 応答型の SSOT

`Result<T>` / `PageResult<T>` / `ErrorResponse` のフィールド定義は `共通/spec-07-api-list.yaml` の `components.schemas` を正とし、型の実装形態（`record` / `class`）は [`spec-04-common-component-ui.md`](spec-04-common-component-ui.md) §1 に従う。

### §2.2 messageId 返却・フロント解決方式

messageId の返却と文言解決は `基本設計/共通/spec-09-message-list-ui.md` および [`spec-04-common-component-ui.md`](spec-04-common-component-ui.md) §2 に従う。

### §2.3 ラップ方式

ラップ方式は [`spec-04-common-component-ui.md`](spec-04-common-component-ui.md) §1.6 に従う。

```java
// 単一リソース取得
return Result.ok(dto);

// ページングリスト取得
return Result.ok(PageResult.of(list, total, pageNum, pageSize));

// 作成成功
return Result.ok(createdDto, "I0010");
```

### §2.4 ファイル応答

ファイル系レスポンスの扱いは [`spec-04-common-component-ui.md`](spec-04-common-component-ui.md) §1.6 に従う。

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.foundation.springboot.03-business-service-ui.s04" -->
## §3 例外処理とバリデーション（バインディング適用済み）

### §3.1 GlobalExceptionHandler（統一捕捉）

`@RestControllerAdvice`（`common` モジュールの `GlobalExceptionHandler`）で統一捕捉する。例外型 → HTTP ステータスと messageId の固定マッピングは `開発規約/spec-backend-coding-rule.md` §4 に従う（messageId はステータスから独立）。

### §3.2 業務例外の送出規則

業務失敗の表現は `開発規約/spec-backend-coding-rule.md` §4 に従う（`BusinessException` のみ）。

```java
// 正しい実装例
throw new BusinessException("E0007", userId);

// 禁止例（リターンコード返却）
return Result.error("DUPLICATE");  // 禁止

// 禁止例（マジック文字列）
throw new RuntimeException("user not found");  // 禁止
```

### §3.3 入力バリデーション

Bean Validation アノテーションを DTO フィールドに付与する。制約は `共通/spec-07-api-list.yaml` の `required`/`maxLength`/`enum` に整合させる。

| アノテーション | 用途例 |
|---------------|--------|
| `@NotNull` | 必須フィールド |
| `@NotBlank` | 文字列必須（空白禁止） |
| `@Email` | メールアドレス形式 |
| `@Size(min, max)` | 文字列長制約 |
| `@Pattern(regexp)` | 正規表現制約 |

### §3.4 業務ルールバリデーション

業務ルール（存在確認・重複確認・状態遷移確認等）は ServiceImpl で明示チェックし、条件不満足時に `BusinessException` を送出する。フロントへの判定委譲禁止。

```java
// ServiceImpl での業務ルールチェック例
User user = userMapper.selectById(userId);
if (user == null) {
    throw new EntityNotFoundException("E0005", userId);
}
if (user.isDisabled()) {
    throw new BusinessException("E0006", userId);
}
```

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.foundation.springboot.03-business-service-ui.s05" -->
## §4 MyBatis と永続化（バインディング: `mybatis-spring-boot-starter` 使用、MyBatis Plus 不使用）

### §4.1 データアクセス層の基本規則

- データアクセス層は**ネイティブ MyBatis**（`mybatis-spring-boot-starter` v3.0.4）を使用する。**MyBatis Plus は使用しない。**
- 各 Mapper インタフェースに `@Mapper` を付与する。`*Application` クラスへの `@MapperScan` は付けない。
- Mapper の構成・SQL 記述（1 Entity = 1 Mapper + 同名 XML、動的条件、ページング）は `開発規約/spec-backend-coding-rule.md` §6 に従う。

### §4.2 resultMap と列-フィールドマッピング

`map-underscore-to-camel-case: true`（application.yml `mybatis.configuration.map-underscore-to-camel-case`）を有効化する。複合マッピング・型変換が必要な場合は `resultMap` で明示的に定義する。

```xml
<resultMap id="userResultMap" type="com.example.ddmsaa.core.user.entity.User">
  <id property="userId" column="user_id"/>
  <result property="fullName" column="full_name"/>
  <!-- 暗号化列は暗号文のまま取得し、service 層で FieldEncryptor.decrypt する（spec-04 §6・TypeHandler は不採用）-->
  <result property="idNumber" column="id_number"/>
</resultMap>
```

### §4.3 論理削除

論理削除の列定義は `開発規約/spec-database-rule.md` §4.3、クエリ・削除操作の書き方は `開発規約/spec-backend-coding-rule.md` §6 に従う。履歴テーブル等の例外は `データベース/schema/schema-definition.yaml` に明記する。

```xml
<!-- 論理削除クエリ例 -->
<select id="selectById" resultMap="userResultMap">
  SELECT * FROM users
  WHERE user_id = #{userId}
    AND deleted_at IS NULL
</select>

<update id="deleteById">
  UPDATE users
  SET deleted_at = now()
  WHERE user_id = #{userId}
    AND deleted_at IS NULL
</update>
```

### §4.4 監査列

監査列（`created_at` / `created_by` / `updated_at` / `updated_by`）の設定主体は `spec-04-common-component-ui.md` §9.3 に従う。

### §4.5 主キー戦略

主キーの形式・採番・例外（`users` の `user_id`、共有主キー、自然キー）は `開発規約/spec-database-rule.md` §4.1 に従い、Entity の主キーの持ち方は `開発規約/spec-backend-coding-rule.md` §5 に従う。採番が必要なテーブルでは ServiceImpl が prefix を指定して採番関数を呼び出す。

採番関数・DDL・トリガーの構築はスクリプトの機構分担（初期 schema と後続変更）に従い、マイグレーション SQL はマイクロサービス間で共有する（`infrastructure` jar の `db/migration` に一元配置）。分担は `データベース/spec-db-migration-ui.md` を参照する。

### §4.6 動的条件とページング

動的 SQL とページングは `開発規約/spec-backend-coding-rule.md` §6 に従う（ページングの PageHelper は `pagehelper-spring-boot-starter` v2.1.1）。

```xml
<!-- 動的条件例 -->
<select id="search" resultMap="userResultMap">
  SELECT * FROM users
  <where>
    <if test="departmentId != null">
      AND department_id = #{departmentId}
    </if>
    <if test="fullNameLike != null and fullNameLike != ''">
      AND full_name LIKE CONCAT('%', #{fullNameLike}, '%')
    </if>
    AND deleted_at IS NULL
  </where>
  ORDER BY user_id
</select>
```

```java
// PageHelper 使用例（ServiceImpl）
PageHelper.startPage(pageNum, pageSize);
List<User> list = userMapper.search(condition);
PageInfo<User> pageInfo = new PageInfo<>(list);
return PageResult.of(
    list.stream().map(converter::toDto).toList(),
    pageInfo.getTotal(), pageNum, pageSize
);
```

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.foundation.springboot.03-business-service-ui.s06" -->
## §5 トランザクション（バインディング: `@Transactional`）

### §5.1 基本規則

- `@Transactional` は **ServiceImpl メソッド単位**にのみ付与する（Controller・Mapper への付与禁止）
- 複数テーブルへの書き込みを含む操作は同一トランザクション内で実行し、原子ロールバックを保証する

```java
@Service
@Transactional(readOnly = true)   // クラスデフォルトは読み取り専用
public class UserServiceImpl implements UserService {

    @Override
    @Transactional                // 書き込みメソッドはオーバーライド
    public UserDto create(UserCreateDto dto) {
        // ... 複数テーブルへの書き込みを同一トランザクション内で実施
    }
}
```

### §5.2 副作用の後処理

トランザクション内は DB 操作のみに限定する。メール送信・Magic Link 発行・セッション失効等の副作用は**コミット後**に実行する。

```java
// TransactionSynchronization による副作用の後処理
TransactionSynchronizationManager.registerSynchronization(
    new TransactionSynchronizationAdapter() {
        @Override
        public void afterCommit() {
            // メール送信・外部システム通知等
            notificationService.sendMagicLink(email, token);
        }
    }
);
```

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.foundation.springboot.03-business-service-ui.s07" -->
## §6 権限モデル（三層）

権限制御は **機能レベル ∩ データ範囲（行レベル）∩ フィールド閲覧（列レベル）** の三層で構成する（ロールごとの内容は `共通/spec-03-auth-ui.md` を参照）。

### §6.1 機能レベル（Controller）

`@PreAuthorize` を Controller メソッドに付与する。ロールは JWT claim `role` の ENUM 文字列を使用する（ロールはプロジェクトで定義する。以下の ADMIN / MANAGER / USER は例）。**`@EnableMethodSecurity` を SecurityFilterChain Bean の設定クラスに付与すること（省略禁止）。**

| ロール（例） | DB 値 | JWT/API 文字列 | デフォルト |
|--------|-------|---------------|----------|
| ADMIN | 1 | `ADMIN` | — |
| MANAGER | 2 | `MANAGER` | — |
| USER | 3 | `USER` | デフォルト |

```java
// Controller メソッド例
@GetMapping("/{userId}")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
public Result<UserDto> getUser(@PathVariable String userId) {
    return Result.ok(userService.getById(userId));
}

@PostMapping
@PreAuthorize("hasRole('ADMIN')")
public Result<UserDto> createUser(@RequestBody @Valid UserCreateDto dto) {
    return Result.ok(userService.create(dto), "I0010");
}
```

### §6.2 データ範囲（行レベル）

`UserContext`（`sub` = ユーザーID、`role` = ENUM 文字列）から取得した情報に基づき、Service / SQL で**強制フィルタ**する。フロントからの入力値に依存してはならない。

| ロール（例） | アクセス可能範囲 | フィルタ条件 |
|--------|----------------|-------------|
| ADMIN | 全件 | フィルタなし |
| MANAGER | 自部署 | `department_id = #{userContext.departmentId}` |
| USER | 本人のみ | `user_id = #{userContext.sub}` |

```java
// ServiceImpl での行レベルフィルタ例
public List<UserDto> search(SearchCondition condition) {
    UserContext ctx = UserContextHolder.get();
    if (ctx.getRole() == Role.MANAGER) {
        condition.setDepartmentId(ctx.getDepartmentId());
    } else if (ctx.getRole() == Role.USER) {
        condition.setUserId(ctx.getSub());
    }
    // ADMIN はフィルタなし
    return userMapper.search(condition).stream()
        .map(converter::toDto)
        .map(dto -> maskingService.maskByRole(dto, ctx.getRole(), ctx.getSub().equals(dto.userId())))
        .toList();
}
```

### §6.3 フィールド（列レベル）

`MaskingService.maskByRole(dto, viewerRole, isSelf)` を呼び出し、`共通/spec-03-auth-ui.md` §3 のマスクマトリクスに従いフィールドを `null` 化する。

- **基盤は `no-op` フック署名（`MaskingService` インタフェース）を提供する**
- 業務 DTO ごとに具体的なマスクマトリクスを実装する（詳細は各機能の詳細設計に属する）

### §6.4 暗号化列（フィールドレベル暗号化）

- 機微フィールド（`id_number` / `account_number` / `request_content` 等）は `FieldEncryptor`（AES-GCM）で保存前に暗号化する
- 読み取り時はロールに応じて復号またはマスクする
- アルゴリズム：AES-GCM、IV=12 byte、tag=128 bit、出力形式=`Base64(iv || cipher)`（パディングなし）
- 暗号化キーは環境変数 `AES_KEY` から `app.crypto.aes-key` 経由で注入する（ソースリポジトリに含めない）
- 実装詳細は [`spec-04-common-component-ui.md`](spec-04-common-component-ui.md) §6 参照

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.foundation.springboot.03-business-service-ui.s08" -->
## §7 バリデーション

### §7.1 入力バリデーション

Bean Validation（§3.3 参照）を DTO に付与し、Controller の `@Valid` で起動する。制約は `共通/spec-07-api-list.yaml` の OpenAPI schema に整合させる。

### §7.2 業務ルールバリデーション

ServiceImpl で明示チェックし `BusinessException` を送出する（§3.4 参照）。主要な業務ルールチェック例：

| チェック対象 | 失敗時の例外 |
|-------------|-------------|
| リソース存在確認 | `EntityNotFoundException` |
| 重複チェック | `BusinessException` |
| 状態遷移合法性 | `BusinessException` |
| 権限範囲内確認 | `BusinessException` または `AccessDeniedException` |

### §7.3 主キー妥当性

主キー（`VARCHAR(10)` 業務採番）の検証規則は `データベース/schema/schema-definition.yaml`（spec-database-rule SSOT）を正とする。採番関数 `next_business_id(prefix)` が払い出した値以外を PK として使用することを禁止する。

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.foundation.springboot.03-business-service-ui.s09" -->
## §8 サービス間通信（バインディング: Spring RestClient）

### §8.1 基本構成

- HTTP クライアントは `infrastructure` モジュールの `RestClientFactory` が提供する `Spring RestClient` を使用する
- サービスディスカバリは使用しない。接続先 URL は設定注入で解決する

```yaml
# application.yml（business-service-core）
app:
  services:
    auth:
      base-url: ${SVC_AUTH_URL:http://localhost:8081}
    core:
      base-url: ${SVC_CORE_URL:http://localhost:8082}
```

### §8.2 ヘッダ透過

サービス間呼び出し時に以下のヘッダを透過する：

| ヘッダ | 内容 | 方向 |
|--------|------|------|
| `Authorization` | `Bearer <JWT>` | 上流から透過 |
| `X-Request-Id` | トレース ID（UUID no-hyphen） | 上流から透過、なければ生成 |

`X-Request-Id` は MDC の `traceId` に格納し、応答ヘッダ `X-Trace-Id` で返す（`TraceIdFilter` が担当）。

### §8.3 下流障害ハンドリング

- 接続エラー・タイムアウトは Gateway 層で 502/504 に変換する
- サービス内で捕捉が必要な場合は `BusinessException` またはシステム例外（`Exception`）へ変換する
- サービス内では Gateway 用の 502/504 エラーを直接生成しない

```java
// RestClient 呼び出し例（infrastructure.RestClientFactory 経由）
try {
    AuthStatusDto status = restClientFactory.forService("auth")
        .get()
        .uri("/internal/users/{id}/auth-status", userId)
        .retrieve()
        .body(AuthStatusDto.class);
} catch (RestClientException e) {
    throw new BusinessException("E0013", userId);
}
```

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.foundation.springboot.03-business-service-ui.s10" -->
## §9 生成順序と境界

### §9.1 生成順序

`spec-backend-coding-rule.md` §12 に従い以下の順で生成する：

```
Entity → Mapper (IF + XML) → DTO → Converter → ServiceImpl → Controller [→ Batch]
```

各ステップの入力と生成物：

| ステップ | 入力 SSOT | 生成物 |
|---------|----------|--------|
| Entity | `データベース/schema/schema-definition.yaml` | 各テーブル対応 Entity クラス |
| Mapper | Entity・schema | `<Feature>Mapper` IF + XML |
| DTO | `共通/spec-07-api-list.yaml` components.schemas | `<Feature>CreateDto` / `<Feature>Dto` 等 |
| Converter | Entity・DTO | `<Feature>Converter`（MapStruct `@Mapper`） |
| ServiceImpl | DTO・Mapper・権限設計 | `<Feature>ServiceImpl`（業務ロジック・権限フィルタ） |
| Controller | ServiceImpl・OpenAPI paths | `<Feature>Controller`（`@PreAuthorize`・Result ラップ） |

検証門は [`spec-00-architecture-ui.md`](spec-00-architecture-ui.md) §10.4 参照。

### §9.2 生成境界

**本ドキュメント（spec-03-business-service-ui.md）が定義する範囲：**
Controller / Service / Mapper / Entity / DTO / Converter の**骨格・機構**（層間プロトコル・命名規約・共通機構の適用方法・禁止事項）。

**各機能の詳細設計が定義する範囲（本書には含めない）：**
- 具体的なドメインフィールド（Entity のカラム定義・DTO のフィールド）
- マスクマトリクス（ロール×フィールドの具体的な `null` 化ルール）
- 行レベルフィルタ条件（各ドメインの SQL WHERE 句の具体内容）
- messageId エントリ（`共通/spec-09-message-list.yaml` が正本）
- 業務フロー・状態遷移ロジック

| 生成境界 | 本書（基盤） | 各機能の詳細設計 |
|---------|------------|---------|
| 層間プロトコル・パッケージ構造 | ✓ | — |
| 共通機構（Result/例外/トレース/暗号化）の使い方 | ✓ | — |
| ドメイン Entity フィールド | — | ✓ |
| マスクマトリクス（具体） | no-op フック署名のみ | 実装 ✓ |
| 行レベルフィルタ条件（具体） | フィルタパターンのみ | 実装 ✓ |
| messageId の具体エントリ | — | ✓ |

---

<!-- devtool:endblock -->
<!-- devtool:block id="basic.foundation.springboot.03-business-service-ui.s11" -->
## 改訂履歴

| バージョン | 日付 | 改訂内容 |
|-----------|------|---------|
| V1.0 | 2026-06-22 | `基盤基本設計.md` §4.10 / §4.9（業務サービス層構成・三層権限）と共通機構の業務側適用を独立ドキュメント化。層構成/パッケージ・統一応答（明示ラップ）・例外送出・MyBatis 永続化規約・トランザクション・三層権限・サービス間呼び出し・生成境界を集約。共通コンポーネント実装詳細は 04 へ誘導。 |
| V1.1 | 2026-06-30 | テンプレート（共通模板）および `spec-java-springboot-mybatis-temp.yaml` バインディングと整合させ全セクションを再構成。七層モデル表・監査列表・ロール定義表・生成境界表を追加。§1〜§9 の見出し体系を必須セクション仕様に準拠。 |
<!-- devtool:endblock -->
