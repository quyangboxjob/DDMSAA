# DB 構築共通テンプレート

## 1. 目的

本テンプレートは、SPA + マイクロサービス + Spring Boot + MyBatis + PostgreSQL のバックエンド基盤に適用できる**汎用 DB 設計フレームワーク**を提供する。特定の業務シナリオから独立し、統一的なデータベースアーキテクチャ原則、Schema 構成、テーブル設計規約、初期化およびマイグレーション方針を定義することで、再利用・レビュー・実践を容易にする。

## 2. 適用範囲

- PostgreSQL ベースのリレーショナルデータベース設計
- Spring Boot + MyBatis によるデータアクセス
- 複数環境へのデプロイ（Windows / Linux）に対応
- データベースのバージョンマイグレーションと初期化に対応
- プラットフォーム標準テーブル（認証 / 権限 / 監査 / パラメータテーブル等）と業務テーブルを含む
- 文字エンコーディングは UTF-8、タイムゾーンはシステムタイムゾーン（`共通/spec-01-system-architecture-ui.md`）に統一

## 3. DB アーキテクチャ概要

### 3.1 アーキテクチャ階層

```
┌─────────────────────────────────┐
│   Application Layer             │
│   (Spring Boot + MyBatis)       │
└────────────┬────────────────────┘
             │ JDBC / DataSource
┌────────────▼────────────────────┐
│   Connection Pool               │
│   (HikariCP)                    │
├─────────────────────────────────┤
│   PostgreSQL Database           │
│  ┌───────────┐ ┌─────────────┐ │
│  │ Platform  │ │ Business    │ │
│  │ Schema    │ │ Schema(s)   │ │
│  │ (sys_*)   │ │             │ │
│  └───────────┘ └─────────────┘ │
└─────────────────────────────────┘
```

### 3.2 Schema 構成

- **Platform Schema**（基盤）：認証・権限・監査・設定などの基礎テーブルを統一的に格納する
- **Business Schema(s)**：業務領域ごとに分割する。例：`user_schema`（ユーザー領域）、`app_schema`（申請領域）など

### 3.3 設計原則

| 原則 | 説明 |
| --- | --- |
| **文字エンコーディング** | ENCODING='UTF8'、LC_COLLATE='C.UTF-8'、LC_CTYPE='C.UTF-8'（日本向けプロジェクト） |
| **タイムゾーン** | システムタイムゾーンに統一（`共通/spec-01-system-architecture-ui.md`）；DB セッションとアプリケーションを一致させる |
| **主キー** | 主キー規約は `開発規約/spec-database-rule.md` に従う；業務キーと技術主キーは分離する |
| **論理削除** | 通常テーブルは `deleted_at` 列（論理削除）を使用；履歴テーブルは削除操作なし |
| **監査列** | 全テーブルに `created_at`、`created_by`、`updated_at`、`updated_by` を必須とする |
| **トランザクション分離** | 既定は `READ_COMMITTED`；強い一貫性が必要な場合はアプリケーション層で対処する |
| **制約** | 外部キー・一意性・非 NULL 等を明示的に宣言する；NOT NULL 列には既定値を持たせるかアプリケーション層で保証する |
| **インデックス** | 頻繁に検索する列・FK・ソート列にインデックスを作成する；過剰なインデックスは避ける |

## 4. 汎用 DB 設計

### 4.1 接続と初期化

#### 4.1.1 データベース初期化規約

- **ENCODING**：`UTF8` 必須
- **LC_COLLATE / LC_CTYPE**：`C.UTF-8` に統一（日本向けプロジェクト）
- **TEMPLATE**：`template0` を使用して環境の一貫性を保証する
- **文字セットと照合順序**：`LC_CTYPE` が文字分類を決定し、LIKE / ORDER BY 等の操作に影響することを確認する

#### 4.1.2 初期 DB 作成例

```sql
-- 1. スーパーユーザーでログイン
psql -U postgres

-- 2. アプリケーション専用ユーザーを作成
CREATE USER "{{DB_USER}}" WITH PASSWORD '{{DB_PASSWORD}}';

-- 3. データベースを作成
CREATE DATABASE "{{DB_NAME}}"
  WITH OWNER = "{{DB_USER}}"
       ENCODING = 'UTF8'
       LC_COLLATE = 'C.UTF-8'
       LC_CTYPE = 'C.UTF-8'
       TEMPLATE = template0;

-- 4. 権限を付与
GRANT ALL PRIVILEGES ON DATABASE "{{DB_NAME}}" TO "{{DB_USER}}";

-- 5. Schema 権限を付与
GRANT ALL PRIVILEGES ON SCHEMA public TO "{{DB_USER}}";

-- 6. テーブル権限を付与（初期化後）
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO "{{DB_USER}}";
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO "{{DB_USER}}";

-- 7. 接続の切り替え
\c "{{DB_NAME}}" "{{DB_USER}}"
```

#### 4.1.3 接続文字列規約

**PostgreSQL JDBC URL：**

```
jdbc:postgresql://{{HOST}}:{{PORT}}/{{DB_NAME}}
  ?user={{DB_USER}}
  &password={{DB_PASSWORD}}
  &sslmode=require
  &currentSchema=public
  &tcpKeepAlives=true
  &ApplicationName=app-name
```

**パラメータ説明：**

| パラメータ | 説明 | 例 |
| --- | --- | --- |
| `HOST` | データベースサーバーのアドレス | `localhost` / `db-server.local` |
| `PORT` | 既定は 5434 | `5434` |
| `DB_NAME` | データベース名 | `ddmsaa` |
| `DB_USER` | アプリケーション接続ユーザー | `app_user` |
| `DB_PASSWORD` | パスワード | （設定により注入） |
| `sslmode` | SSL 接続モード | `require`（本プロジェクトで統一） |
| `currentSchema` | 既定 Schema | `public` |
| `tcpKeepAlives` | TCP キープアライブ | `true` |

### 4.2 HikariCP コネクションプール設定

#### 4.2.1 標準設定

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    hikari:
      # コネクションプールサイズ
      maximum-pool-size: 30          # 最大接続数（同時実行数に応じて調整）
      minimum-idle: 5                 # 最小アイドル接続数
      # タイムアウトとライフサイクル
      connection-timeout: 10000       # 接続取得タイムアウト（ms）
      idle-timeout: 300000            # アイドル接続の回収時間（ms）= 5min
      max-lifetime: 1800000           # 接続ライフサイクル上限（ms）= 30min
      # 検証とメンテナンス
      connection-test-query: SELECT 1 # 接続有効性チェック
      leak-detection-threshold: 60000 # 接続リーク検知しきい値（ms）
      auto-commit: true               # 自動コミット（MyBatis で明示管理する場合は無効化可）
```

#### 4.2.2 パラメータチューニング指針

| パラメータ | チューニング場面 | 推奨 |
| --- | --- | --- |
| `maximum-pool-size` | 高同時実行 | `(コア数 × 2) + 予備数`；一般に 20-50 |
| `minimum-idle` | ウォームアップ | `1/3 ~ 1/2 * maximum-pool-size` を推奨 |
| `connection-timeout` | タイムアウトに敏感な場合 | 保守的な値は 20000ms；緊急時は 5000ms まで下げる |
| `idle-timeout` | 長時間接続 | 300000ms（5min）が標準；600000ms まで調整可 |
| `max-lifetime` | 接続の再利用 | 1800000ms（30min）；データベース側の timeout と合わせる |

### 4.3 MyBatis 設定

#### 4.3.1 汎用設定

```yaml
mybatis:
  mapper-locations: classpath:mybatis/mapper/*.xml
  type-aliases-package: com.company.project.entity
  configuration:
    # キャメルケースマッピング（DB snake_case ↔ Java camelCase）
    map-underscore-to-camel-case: true
    # 既定の Executor タイプ
    default-executor-type: REUSE
    # キャッシュ方針
    cache-enabled: true
    lazy-loading-enabled: true
    lazy-load-trigger-methods: equals,clone,hashCode,toString
    # ログ
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
```

#### 4.3.2 Mapper インタフェースと XML の分離

- **Mapper インタフェース**：`UserMapper`、`RoleMapper` など。CRUD と複雑なクエリを宣言する
- **Mapper XML**：`UserMapper.xml`、`RoleMapper.xml` など。対応する SQL を実装する
- **汎用 Mapper**（任意）：pagehelper-spring-boot-starter を使用してページングを簡素化する
- **SQL 分割**：複雑な SQL は機能モジュール単位（select / insert / update / delete）で分割する

#### 4.3.3 トランザクション管理

```java
// Service 層でトランザクションを宣言
@Service
public class UserService {
    
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = Exception.class,
        timeout = 30
    )
    public void updateUser(UserDTO dto) {
        // 業務ロジック
    }
}
```

### 4.4 基盤標準 Schema（Platform Schema）

プラットフォーム標準 Schema は、認証・権限・監査・設定などの基盤テーブルを格納する。**採用するか否かはプロジェクトの実際のアーキテクチャ選択による**：

- **選択肢 A：独立プラットフォーム Schema**（複数業務システムに推奨）  
  `sys_user`、`sys_role`、`sys_user_role`、`sys_audit_log`、`system_settings` 等を作成し、権限と監査を統一管理する。

- **選択肢 B：業務 Schema 内包**（単一領域システム向け）  
  権限・認証・監査の列を業務テーブルに直接持たせ（例：`users.role`、`user_contacts.personal_email`）、アプリケーション層で権限制御を行う。

本テンプレートは特定の選択肢を強制しない。具体的な選択は、プロジェクトの次の観点に従う：
- 複数システム共有の要否（要/不要）
- 権限粒度の複雑度（単純/複雑）
- 監査・コンプライアンス要件（低/高）

#### 4.4.1 プラットフォームテーブル例（任意）

**独立プラットフォーム Schema を採用する場合の代表的なテーブル：**

| テーブル名 | 用途 | 備考 |
|------|------|------|
| `sys_user` | システムユーザー（認証） | ユーザー名、パスワードハッシュ、状態など |
| `sys_role` | ロール定義 | ロール名、権限集合 |
| `sys_user_role` | ユーザーとロールの関係 | 多対多の関係 |
| `sys_permission` | 権限一覧 | 権限コード、説明 |
| `sys_audit_log` | 操作監査ログ | 重要な業務操作を記録 |
| `system_settings` | システムパラメータ | キーバリュー形式。例：auth_failure_lock_count |

#### 4.4.2 system_settings テーブル（参考テンプレート）

システムパラメータテーブルが必要な場合は、次の DDL を参考にできる：

```sql
CREATE TABLE system_settings (
    id              VARCHAR(20) PRIMARY KEY,
    setting_key     VARCHAR(100) NOT NULL UNIQUE,
    setting_value   VARCHAR(500) NOT NULL,
    description     VARCHAR(255),
    is_system       BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(20) DEFAULT 'SYSTEM',
    updated_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(20)
);

-- サンプルデータ
INSERT INTO system_settings (id, setting_key, setting_value, description, is_system) VALUES
  ('ST0001', 'auth.max-login-failure-count', '5', 'Login failure threshold', TRUE),
  ('ST0002', 'auth.login-lockout-minutes', '15', 'Account lockout duration (minutes)', TRUE),
  ('ST0003', 'sms.expiry-minutes', '10', 'SMS OTP validity (minutes)', TRUE);
```


### 4.5 業務 Schema 設計

#### 4.5.1 テーブル設計規約

| 項目 | 規約 |
| --- | --- |
| **テーブル名** | 小文字 snake_case；接頭辞は業務領域の略称（例：`usr_`、`app_`） |
| **主キー** | `開発規約/spec-database-rule.md` に従う；業務キーは別途 UNIQUE 制約で定義する |
| **列名** | 小文字 snake_case；Backend camelCase / API camelCase に対応させる |
| **型** | VARCHAR(n) / INT / BIGINT / DECIMAL / BOOLEAN / TIMESTAMP / TEXT；データ辞書を参照 |
| **非 NULL** | 必須項目は NOT NULL + DEFAULT；任意項目は NULL を許容する |
| **インデックス** | FK 列、頻繁に検索する列、ソート列；過剰なインデックスは避ける |
| **制約** | FK 関連、一意性、チェック制約；ON DELETE / ON UPDATE の挙動を明示的に宣言する |
| **監査** | `created_at` / `created_by` / `updated_at` / `updated_by` / `deleted_at` を必須とする |

#### 4.5.2 代表的なテーブル種別

| 種別 | 説明 | 例 |
| --- | --- | --- |
| **エンティティテーブル** | 業務エンティティの主テーブル；論理削除に対応 | `users`、`departments` |
| **属性テーブル** | 1:1 / 0..1 の関係；主テーブルと運命を共にする | `user_contacts`、`user_secrets` |
| **集約テーブル** | 複数の属性を含むワイドテーブル；JOIN を削減 | `user_summary` |
| **イベントテーブル** | 時系列イベントを記録；削除なし | `request_status_histories`、`system_lock_histories` |
| **中間テーブル** | 多対多の関係 | `user_role`、`department_user` |
| **参照テーブル** | 列挙/定数テーブル；初期化データ | `holidays`、`country_codes` |

## 5. データベースのバージョンマイグレーション

### 5.1 マイグレーションツールと方針

- **ツール**：Flyway または Liquibase（Spring Boot が標準でサポート）
- **規約**：ファイル命名・バージョン・配置場所は `開発規約/spec-flyway-migration-rule.md` に従う
- **実行**：Spring Boot 起動時に自動実行；手動実行にも対応
- **ロールバック**：慎重に扱うこと；データ変更を伴うマイグレーションは**自動ロールバック非対応**（Flyway コミュニティ版）

### 5.2 マイグレーションファイル例

```sql
-- V2__create_app_user：アプリユーザーテーブルを新規作成
CREATE TABLE app_user (
    user_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(20) DEFAULT 'SYSTEM',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(20),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_app_user_username ON app_user(username);
```

### 5.3 マイグレーション実行フロー

1. **ローカル開発**：アプリケーション起動時に未実行のマイグレーションが自動実行される
2. **ステージング環境**：`./mvnw flyway:migrate` を実行、または手動で検証する
3. **本番環境**：ロールバック計画を策定のうえ、DBA の承認を得て実行する
4. **記録**：マイグレーションごとに実行ログ・変更内容・実施者を保存する

## 6. 性能と監視

### 6.1 インデックス方針

**インデックスを作成する列：**

- 外部キー列（FK）
- WHERE 条件で頻繁に使用される列
- ORDER BY / GROUP BY で頻繁に使用される列
- 一意性制約の列

**過剰なインデックスを避ける：**

- 更新が頻繁な列へのインデックスは性能に影響する
- 各インデックスはディスク容量と保守コストを消費する
- スロークエリを定期的に分析し、インデックスを最適化する

### 6.2 クエリ最適化

```sql
-- ✓ パラメータ化クエリを使用（MyBatis Mapper）
SELECT * FROM users WHERE department_id = #{departmentId}

-- ✗ 文字列連結を避ける（SQL インジェクションのリスク）
SELECT * FROM users WHERE department_id = '${departmentId}'

-- ✓ JOIN を適切に使用（必要に応じて複数テーブル）
SELECT e.*, d.department_name 
FROM users e 
JOIN departments d ON e.department_id = d.departments_id 
WHERE e.work_status = 'ACTIVE'

-- ✗ 大きな IN サブクエリを避ける
SELECT * FROM users WHERE user_id IN (SELECT ... FROM big_table)  -- 避ける

-- ✓ ページングを使用
SELECT * FROM users LIMIT #{pageSize} OFFSET #{offset}
```

### 6.3 監視指標

| 指標 | アラート条件 | 説明 |
| --- | --- | --- |
| コネクションプール使用率 | > 80% | 増強またはクエリ最適化を検討 |
| クエリ所要時間 | > 1s（P95） | インデックス不足やクエリロジックの問題を確認 |
| デッドロック | あらゆるデッドロックイベント | トランザクション分離レベルまたはクエリ順序を調整 |
| ディスク使用率 | > 85% | 増設または履歴データの整理を計画 |
| バックアップ失敗 | あらゆる失敗 | 直ちに対応 |

## 7. バックアップとリストア

### 7.1 バックアップ方針

```bash
# フルバックアップ（SQL 形式）
pg_dump -U {{DB_USER}} -h {{HOST}} -d {{DB_NAME}} \
  --format=custom --compress=9 > backup_$(date +%Y%m%d_%H%M%S).dump

# 定期バックアップ（Cron）
0 2 * * * pg_dump -U {{DB_USER}} ... > /backups/dump_$(date +\%Y\%m\%d).dump

# 増分バックアップ設定（WAL）
# postgresql.conf に設定：
#   wal_level = replica
#   max_wal_senders = 3
```

### 7.2 リストア方針

```bash
# フルリストア
pg_restore -U {{DB_USER}} -h {{HOST}} -d {{DB_NAME}} \
  --clean --if-exists -j 4 backup.dump

# 特定時点へのリストア（WAL の保持が必要）
# recovery.conf または recovery.signal を設定し、recovery_target_timeline / recovery_target_time を指定
```

## 8. プロジェクトディレクトリテンプレート

```
backend-platform/
  database/
    migrations/              # バージョンマイグレーションスクリプト
      V2__create_app_user.sql
      V3__create_app_role.sql
      V4__create_users.sql
    schema/
      platform-schema.yaml   # 基盤標準 Schema 定義（sys_*）
      business-schema.yaml   # 業務 Schema 定義
      schema-definition.yaml # 完全なテーブル定義と関連
    queries/
      query-templates.yaml   # MyBatis SQL テンプレートライブラリ
    common/
      spec-db-connection-temp.md         # 接続と初期化の規約
      spec-db-connection-ui.md     # DB 接続設計と必須事項
      spec-db-foundation-temp.md                  # 本ファイル
      spec-data-dictionary-mapping-ui.md           # 命名マッピングと型規約
```

## 9. 設定情報テンプレート

### 9.1 アプリケーション層の接続設定

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}  # jdbc:postgresql://...
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    hikari:
      maximum-pool-size: 30
      minimum-idle: 5
      connection-timeout: 10000
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
      
  jpa:
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        
mybatis:
  mapper-locations: classpath:mybatis/mapper/*.xml
  type-aliases-package: com.company.project.entity
  configuration:
    map-underscore-to-camel-case: true
```

### 9.2 環境変数の例

```bash
# .env または docker-compose.env
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5434/ddmsaa?currentSchema=public
SPRING_DATASOURCE_USERNAME=app_user
SPRING_DATASOURCE_PASSWORD=secure-password-here

# PostgreSQL 初期化
DB_NAME=ddmsaa
DB_USER=app_user
DB_PASSWORD=secure-password-here
```

## 10. 設計成果物の要点

- 基本設計には、DB アーキテクチャ階層、Schema 構成、設計原則、接続/初期化の要点を残すこと
- 詳細設計には、テーブル設計規約、インデックス方針、マイグレーション規約、トランザクション管理の実装テンプレートを明記すること
- 具体的な業務テーブル定義は対応する業務サービスのドキュメントに分割し、DB テンプレートには汎用規約と再利用パターンのみを残すこと
- すべてのテーブル名/列名と型は、データ辞書（`spec-data-dictionary-mapping-ui.md`）と schema 定義を優先して正とすること
- 共通設計には業務の詳細を埋め込まず、「業務領域 A / 領域 B」や汎用テーブル種別（エンティティテーブル / 属性テーブル / イベントテーブル等）で表現すること

## 11. 技術スタック標準とバージョンアンカー

| 項目 | 選定 | バージョン | 管理方式 |
| --- | --- | --- | --- |
| データベースエンジン | PostgreSQL | 16 | — |
| ドライバ | org.postgresql:postgresql | （Spring Boot BOM に従う） | BOM |
| コネクションプール | HikariCP | （Spring Boot に従う） | BOM |
| ORM / データアクセス | MyBatis | 3.5.x | mybatis-spring-boot-starter に従う |
| ページングプラグイン | pagehelper-spring-boot-starter | 2.1.1 | 明示的に pin |
| マイグレーションツール | Flyway | （Spring Boot に従う） | BOM |
| データベースツール | DBeaver / pgAdmin | 最新 | 推奨開発ツール |

## 12. 操作ガイド

### 12.1 Windows 環境での PostgreSQL 接続

```powershell
# 1. psql で接続
psql -U postgres -h localhost -p 5434

# 2. データベースとユーザーを作成
CREATE USER app_user WITH PASSWORD 'password';
CREATE DATABASE ddmsaa OWNER app_user ENCODING 'UTF8' LC_COLLATE 'C.UTF-8' LC_CTYPE 'C.UTF-8';

# 3. 権限を付与
GRANT ALL PRIVILEGES ON DATABASE ddmsaa TO app_user;
\c ddmsaa app_user
GRANT ALL PRIVILEGES ON SCHEMA public TO app_user;

# 4. 接続文字列を検証
$connStr = "Server=localhost;Port=5434;Database=ddmsaa;User Id=app_user;Password=password;"
```

### 12.2 Linux 環境での PostgreSQL 接続

```bash
# 1. スーパーユーザーで接続
sudo -u postgres psql

# 2. データベースとユーザーを作成
CREATE USER app_user WITH PASSWORD 'password';
CREATE DATABASE ddmsaa OWNER app_user ENCODING 'UTF8' LC_COLLATE 'C.UTF-8' LC_CTYPE 'C.UTF-8';
GRANT ALL PRIVILEGES ON DATABASE ddmsaa TO app_user;

# 3. 接続を検証
psql -U app_user -h localhost -d ddmsaa -c "SELECT version();"

# 4. マイグレーションスクリプトを実行
psql -U app_user -d ddmsaa -f migrations/V2__create_app_user.sql
```

## 13. 補足

本 DB 構築共通テンプレートはバックエンド基盤構築共通テンプレートと対をなし、データベース層の統一設計フレームワークを提供する。具体的な業務テーブル定義はこれを土台に補完し、本テンプレートの命名規約・列標準・インデックス方針に従うこと。すべてのテーブルは監査列の要件を満たし、バージョンマイグレーションと性能監視に対応すること。

