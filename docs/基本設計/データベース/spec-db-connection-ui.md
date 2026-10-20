### 基本 DB 接続設計

#### 1. 目的

本設計書はデータベース接続の基本規約を定義する。既存の詳細設計に従った実施を必須とし、バックエンドサービスと PostgreSQL の接続設定を統一的・制御可能・監査可能にする。

#### 2. 適用範囲

- 本プロジェクトのバックエンドのデータベースアクセスモジュールにのみ適用する
- PostgreSQL のみを使用する
- データアクセス層には MyBatis（mybatis-spring-boot-starter）のみを使用する；**MyBatis Plus は使用しない**
- ORM アクセス層として Spring Data JPA および Hibernate は使用しない

#### 3. 設計原則

- カスタマイズ水準：データベース名、schema 名、接続ユーザー、パスワードは設定可能であること
- 接続規約：PostgreSQL JDBC URL に統一し、既定 schema は `currentSchema` または `search_path` で指定する
- 文字セットと照合順序：エンコーディングは `UTF8` 必須、照合順序 `LC_CTYPE` は `C.UTF-8` に統一する（日本向けプロジェクト指定の locale）
- タイムゾーン：データベース接続とアプリケーションのタイムゾーンを一致させ、システムタイムゾーン（`共通/spec-01-system-architecture-ui.md`）に統一する
- 権限分離：データベースのログインユーザーには現行 schema の標準操作権限のみを付与し、管理権限は業務権限から分離する
- 環境サポート：Windows / Linux の 2 種類の実行環境での接続方式をサポートする

#### 4. 必須事項

##### 4.1 PostgreSQL 初期作成

- データベース名：`DB_NAME`
- schema 名：`DB_SCHEMA`
- 接続ユーザー：`DB_USER`
- パスワード：`DB_PASSWORD`

次の初期化手順を必ず実施する：

1. PostgreSQL の管理者ユーザーでログインする
2. 業務接続ユーザーを作成する
3. データベースを作成し、エンコーディングと照合順序を指定する
4. カスタム schema を作成し、schema の所有者を業務接続ユーザーに指定する
5. 接続ユーザーに schema へのアクセス権限と作成権限を付与する
6. schema 内のオブジェクトに既定権限を設定する
7. 接続ユーザーの既定 `search_path` をカスタム schema に向ける

##### 4.2 エンコーディングと照合順序

- `ENCODING = 'UTF8'`
- `LC_COLLATE = 'C.UTF-8'`
- `LC_CTYPE = 'C.UTF-8'`
- タイムゾーン：システムタイムゾーン（`共通/spec-01-system-architecture-ui.md`）

##### 4.3 ユーザーと権限

次を必ず満たすこと：

- 業務接続ユーザーはカスタム schema のみにアクセスできること
- 業務ユーザーが `USAGE`, `CREATE` 権限を持つこと
- 既定のオブジェクト権限に少なくとも `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `REFERENCES`, `TRIGGER` を含めること
- 追加の読み取り専用ロールまたは管理者ロールは必要な場合にのみ作成する

##### 4.4 接続文字列規約

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?currentSchema=${DB_SCHEMA}&sslmode=require
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

##### 4.5 環境別接続例

###### Windows

```powershell
set PGHOST=${DB_HOST}
set PGPORT=${DB_PORT}
set PGDATABASE=${DB_NAME}
set PGUSER=${DB_USER}
set PGPASSWORD=${DB_PASSWORD}
psql
```

または：

```powershell
psql "host=${DB_HOST} port=${DB_PORT} dbname=${DB_NAME} user=${DB_USER} password=${DB_PASSWORD} options='-c search_path=${DB_SCHEMA},public'"
```

###### Linux

```bash
export PGHOST=${DB_HOST}
export PGPORT=${DB_PORT}
export PGDATABASE=${DB_NAME}
export PGUSER=${DB_USER}
export PGPASSWORD=${DB_PASSWORD}
psql
```

または：

```bash
psql "host=${DB_HOST} port=${DB_PORT} dbname=${DB_NAME} user=${DB_USER} password=${DB_PASSWORD} options='-c search_path=${DB_SCHEMA},public'"
```

#### 5. MyBatis 必須設定（ネイティブ MyBatis、MyBatis Plus ではない）

> 本プロジェクトのデータアクセス層は**ネイティブ MyBatis**（`mybatis-spring-boot-starter`）＋ `PageHelper` によるページングであり、**MyBatis Plus は使用しない**。主キー・論理削除の規約は `開発規約/spec-database-rule.md` と `開発規約/spec-backend-coding-rule.md` §6 に従う。

次の設定方式に従い、Spring Boot の設定ファイルに定義すること：

```properties
mybatis.mapper-locations=classpath:mapper/**/*.xml
mybatis.type-aliases-package=com.example.ddmsaa.**.entity
mybatis.configuration.map-underscore-to-camel-case=true
mybatis.configuration.jdbc-type-for-null=NULL
mybatis.configuration.use-column-label=true
mybatis.configuration.cache-enabled=false
# 主キー・論理削除・ページングの規約は 開発規約/spec-database-rule.md、開発規約/spec-backend-coding-rule.md §6 に従う
```

#### 6. トランザクション分離

`TRANSACTION_READ_COMMITTED` の使用を必須とする：

```properties
spring.datasource.hikari.transaction-isolation=TRANSACTION_READ_COMMITTED
```

#### 7. 備考

- 本設計は実行可能な基礎接続設計であり、いかなる変更もレビューを経ること
- 本プロジェクトでは JPA / Hibernate 関連の設定の使用を認めない
- 設定可能なパラメータはすべて環境変数または設定センターで管理し、平文でコードリポジトリに記載しないこと
