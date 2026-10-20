<!-- devtool:file-region id="document-header" -->
# データベース接続設定テンプレート - DDMSAA

**仕組み:**

- 本テンプレートは、各環境（開発/ステージ/本番）のDB接続設定の基盤を定義。
- 実際の接続情報はコンテナ環境変数または secrets で管理（設定ファイルに直接記載しない）。
- ネイティブ MyBatis（mybatis-spring-boot-starter）を前提とした共通設定ポイントを記載（MyBatis Plus は不使用）。
- SSL/TLS、コネクションプール、タイムアウト設定を含む。

**適用対象:**

- 全業務サービス（auth-service / users-service / applications-service 等）
- common-module の DataSourceConfig で利用

**パラメータ管理:**

- DB_HOST, DB_PORT, DB_USER, DB_PASSWORD, DB_NAME → 環境変数で設定
- DB_SCHEMA → 環境変数またはデプロイ時に指定
- SSL 接続オプション、アイドルタイムアウト → application.yaml で設定

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.database.postgresql.db-connection-temp.s01" -->
## 0. DB 初期作成規約

1. DB 名称、スキーマ名、ユーザー、パスワードはすべてカスタム可能とする。
   - DB 名称: `{{DB_NAME}}`（カスタム項目：データベース名）
   - スキーマ名: `{{DB_SCHEMA}}`（カスタム項目：対象 schema）
   - 接続ユーザー: `{{DB_USER}}`（カスタム項目：データベースのログインユーザー）
   - パスワード: `{{DB_PASSWORD}}`（カスタム項目：データベースユーザーのパスワード）

   > 備考: 上記 4 項目はデプロイ前に実際の値へ置き換えること。DB_HOST / DB_PORT も環境に応じて指定すること。

2. 文字エンコーディングは UTF8 を利用し、タイムゾーンはシステムタイムゾーン（`共通/spec-01-system-architecture-ui.md`）を前提とする。
   - PostgreSQL の場合: ENCODING='UTF8', LC_CTYPE='C.UTF-8', LC_COLLATE='C.UTF-8'
   - システム環境に合わせてロケールを調整する。LC_CTYPE によって文字分類/ソートが決定される。

3. 初期権限管理:
   - 作成ユーザーに対して、カスタムスキーマ内のアクセス権を付与。
   - 必要に応じて読み取り専用ユーザー、運用管理ユーザーを別途定義する。
   - 本テンプレートでは、指定ユーザーに対してスキーマ内の標準操作権限を付与する例を示す。

4. ログインユーザーのデフォルトスキーマを指定し、接続時に明示的な schema 指定を不要にする。

5. 文字セット・照合順序はコードページ utf8 / LC_CTYPE を利用し、アプリ側と DB 側の整合性を確保する。

6. 必要に応じて、スキーマ所有者と接続ユーザーを分離する運用も可能。

### PostgreSQL 初期 DB 作成例

```sql
-- 1. 管理者ユーザーで PostgreSQL にログイン
--    例: psql -h ${DB_HOST} -U postgres

-- 2. カスタムユーザーを作成
CREATE ROLE "${DB_USER}" WITH LOGIN PASSWORD '${DB_PASSWORD}' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;

-- 3. データベースを作成
CREATE DATABASE "${DB_NAME}"
  WITH OWNER = "${DB_USER}"
       ENCODING = 'UTF8'
       LC_COLLATE = 'C.UTF-8'
       LC_CTYPE = 'C.UTF-8'
       TEMPLATE = template0;

-- 4. カスタムスキーマを作成し、ユーザーに所有権を付与
\c "${DB_NAME}"
CREATE SCHEMA IF NOT EXISTS "${DB_SCHEMA}" AUTHORIZATION "${DB_USER}";

-- 5. 標準的なスキーマ権限を付与
GRANT USAGE ON SCHEMA "${DB_SCHEMA}" TO "${DB_USER}";
GRANT CREATE ON SCHEMA "${DB_SCHEMA}" TO "${DB_USER}";

-- 6. スキーマ内のテーブル/シーケンスに対するデフォルト権限
ALTER DEFAULT PRIVILEGES IN SCHEMA "${DB_SCHEMA}"
  GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON TABLES TO "${DB_USER}";
ALTER DEFAULT PRIVILEGES IN SCHEMA "${DB_SCHEMA}"
  GRANT USAGE ON SEQUENCES TO "${DB_USER}";

-- 7. 接続ユーザーのデフォルト検索パスを設定
ALTER ROLE "${DB_USER}" SET search_path = "${DB_SCHEMA}", public;
```

### 初期権限付与のオプション

- 追加の読み取り専用ユーザーを作成する場合:
  1. ロールを作成: `CREATE ROLE read_only NOLOGIN;`
  2. 権限を付与: `GRANT SELECT ON ALL TABLES IN SCHEMA "${DB_SCHEMA}" TO read_only;`
  3. 将来のテーブル追加に対応: `ALTER DEFAULT PRIVILEGES IN SCHEMA "${DB_SCHEMA}" GRANT SELECT ON TABLES TO read_only;`
- 管理者ユーザーにはスキーマ作成やシーケンス操作を許可する場合: `CREATE ROLE admin_user WITH LOGIN ...;`
- 運用用途では、接続ユーザーに最低限の DML/DDL 権限を付与し、管理操作は別ユーザーで実行する。

### 接続文字列サンプル（PostgreSQL）

> 備考: 下記パラメータはカスタマイズ可能な値であり、実際の環境に合わせて置き換えること。
>
> - `${DB_HOST}`: データベースホスト
> - `${DB_PORT}`: データベースポート
> - `${DB_NAME}`: データベース名
> - `${DB_SCHEMA}`: 既定 schema 名
> - `${DB_USER}`: ログインユーザー
> - `${DB_PASSWORD}`: ログインパスワード

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?currentSchema=${DB_SCHEMA}&sslmode=require
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-connection-temp.s02" -->
## 1. 環境別接続設定

### 1.1 Windows 環境

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?currentSchema=${DB_SCHEMA}&sslmode=require
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

Windows から `psql` で接続する例:

```powershell
set PGHOST=${DB_HOST}
set PGPORT=${DB_PORT}
set PGDATABASE=${DB_NAME}
set PGUSER=${DB_USER}
set PGPASSWORD=${DB_PASSWORD}
psql
```

または直接指定:

```powershell
psql "host=${DB_HOST} port=${DB_PORT} dbname=${DB_NAME} user=${DB_USER} password=${DB_PASSWORD} options='-c search_path=${DB_SCHEMA},public'"
```

### 1.2 Linux 環境

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?currentSchema=${DB_SCHEMA}&sslmode=require
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

Linux から `psql` で接続する例:

```bash
export PGHOST=${DB_HOST}
export PGPORT=${DB_PORT}
export PGDATABASE=${DB_NAME}
export PGUSER=${DB_USER}
export PGPASSWORD=${DB_PASSWORD}
psql
```

または直接指定:

```bash
psql "host=${DB_HOST} port=${DB_PORT} dbname=${DB_NAME} user=${DB_USER} password=${DB_PASSWORD} options='-c search_path=${DB_SCHEMA},public'"
```

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-connection-temp.s03" -->
## 2. コネクションプール設定

```properties
# HikariCP 設定（基盤基本設計 アーキテクチャ §6 設定規約 / spec-db-foundation-temp §4.2.1 を正とする）
spring.datasource.hikari.maximum-pool-size=30
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=10000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.connection-test-query=SELECT 1
```

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-connection-temp.s04" -->
## 3. MyBatis 設定（ネイティブ MyBatis、MyBatis Plus ではない）

```properties
# ネイティブ MyBatis（mybatis-spring-boot-starter）。本プロジェクトでは MyBatis Plus を使用しない。
# 主キー・論理削除・ページングの規約は 開発規約/spec-database-rule.md、開発規約/spec-backend-coding-rule.md §6 に従う。
mybatis.mapper-locations=classpath:mapper/**/*.xml
mybatis.type-aliases-package=com.example.domain.model
mybatis.configuration.map-underscore-to-camel-case=true
mybatis.configuration.jdbc-type-for-null=NULL
mybatis.configuration.use-column-label=true
mybatis.configuration.cache-enabled=false
```

<!-- devtool:endblock -->
<!-- devtool:block id="basic.database.postgresql.db-connection-temp.s05" -->
## 4. トランザクション設定

```properties
# Transaction Isolation Level
spring.datasource.hikari.transaction-isolation=TRANSACTION_READ_COMMITTED
# TRANSACTION_READ_COMMITTED
```
<!-- devtool:endblock -->
