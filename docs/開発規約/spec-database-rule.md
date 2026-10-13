# DB 命名規約

---

## メタデータ
- version: 1.4.0
- target_db: PostgreSQL
- created: 2026/06/06
- author: nebulasoft Co., Ltd
- status: approved

---

## 利用方法
- AI が DB Schema を生成する際は、本ファイルを Prompt に含めること。
- 本ドキュメントには曖昧な表現を一切含めず、すべての規則を機械的に判定可能な形式で記述する。
- 規則に例外を設ける場合は、`EXCEPTION:` タグで明示すること。

---

## 1. Schema 命名規約(Schema Naming)

> 本節は PostgreSQL にのみ適用する。
> Schema は PostgreSQL における名前空間の単位であり、テーブルやオブジェクトをモジュール単位で分離するために用いる。

### 1.1. デフォルト Schema の使用規約
- **規則**：単純なモノリシックアプリケーションでは `public` Schema を**使用しなければならない**。目的のない追加 Schema の作成は禁止する。
- **規則**：マルチモジュールまたはマルチテナントのシステムでは、モジュール単位またはテナント単位で独立した Schema を作成してよい。

### 1.2. Schema 命名ルール
- **規則**：Schema 名は小文字スネークケース(snake_case)を**使用しなければならない**。使用可能な文字は英小文字・数字・アンダースコアのみとする。
- **規則**：モジュール単位で分割する場合、Schema 名はモジュール名と一致させる。
- **規則**：テナント単位で分割する場合、Schema 名は `tenant_{tenant_id}` 形式とする。
- **規則**：`tenant_id` は短い整数、または桁数固定のゼロ埋めコード(例：`00123`)を**使用しなければならない**。UUID などの長い文字列は禁止とし、Schema 名全体を 30 文字以内に収める(接頭辞 `tenant_` の 7 文字を含み、`tenant_id` 部分は最大 23 文字)。
- **誤った例**：`tenant_550e8400-e29b-41d4-a716-446655440000`(UUID、長さ制限超過)
- **正しい例**：`tenant_00123`(ゼロ埋め整数、計 12 文字)
- **誤った例**：`MySchema`、`order-module`、`Tenant1`
- **正しい例**：`order_management`、`inventory`、`tenant_00123`

### 1.3. Schema をまたぐ参照の規約
- **規則**：Schema をまたいでテーブルを参照する場合は、完全修飾名(`{schema}.{table}`)を**使用しなければならない**。
- **正しい例**：`order_management.orders`、`inventory.products`

---

## 2. 共通規則(General Rules)

### 2.1. 命名言語の規約(Naming Language)
- **規則**：識別子の言語（英語のみ）は `開発規約/spec-naming-rule.md` §1.1 に従う。

### 2.2. 表記形式の規約(Case Style)
- **規則**：識別子の表記形式（snake_case）は `開発規約/spec-naming-rule.md` §1.2 に従う。

### 2.3. 文字セットと先頭文字の規約(Character Set & First Character)
- **規則**：名前には英小文字・数字・アンダースコア**のみ**を使用しなければならない。
- **禁止**：ハイフン `-`、スペース、`!@#$` などの特殊文字は一切使用禁止とする。
- **規則**：先頭文字は英小文字でなければならない。数字またはアンダースコアで始めてはならない。
- **誤った例**：`1_users`(数字始まり)、`_orders`(アンダースコア始まり)、`user-name`(不正な文字を含む)
- **正しい例**：`users`、`order_items`

### 2.4. 長さ制限の規約(Length Limits)
- **規則**：名前の最大長は 30 文字に厳格に制限する。
- **規則**：名前の最小長は 2 文字に厳格に制限する。
- **誤った例**：`u`(最小長未満)、`extremely_long_table_name_that_exceeds_limit`(最大長超過)
- **正しい例**：`users`、`order_items`

### 2.5. 略語使用の規約(Abbreviations)
- **規則**：略語は `開発規約/spec-naming-rule.md` §4 のホワイトリストに従う。

---

## 3. テーブル命名規約(Table Naming)

### 3.1. 基本命名ルール(Basic Rule)
- **規則**：テーブル名の形式（英語名詞の複数形・snake_case）は `開発規約/spec-naming-rule.md` §1.2 / §2.1 に従う。

### 3.2. 複合語テーブル名の規約(Compound Table Names)
- **規則**：複数の単語からなるテーブル名は `開発規約/spec-naming-rule.md` §1.2 に従って連結する。

### 3.3. 中間テーブルの規約(Join Table)
- **規則**：関連の中間テーブルは `{table_a_singular}_{table_b}` 形式で命名しなければならない。`table_a` は単数形、`table_b` は複数形とする。
- **規則**：アルファベット順の比較基準は、関連付ける 2 つのテーブル名の**単数原形の先頭文字**とし、実際に使用する単数形・複数形ではない。
- **規則**：単数原形の先頭文字の昇順に並べ、先頭文字が同じ場合は以降の文字を順に比較する。
- **誤った例**：`products_orders`(アルファベット順でない)、`order_product_rel`(余分な接尾辞を含む)、`orders_products`(両方が複数形)
- **正しい例**：`order_products`(order は単数形 + products は複数形、単数原形 order < product、アルファベット順 o < p)
- **判定例**：`categories` と `orders` の中間テーブル → 単数原形 category vs order → c < o → `category_orders`

### 3.4. 特殊テーブル種別の命名規約(Special Table Types)
- **規則**：履歴テーブルは `{name}_histories` 形式としなければならない。
- **規則**：一時テーブルは `tmp_{name}` 形式としなければならない。
- **規則**：マスタテーブルおよびルックアップテーブルは通常テーブルと同様に、複数形を**使用しなければならない**。

| 種別 | 命名パターン | 例 |
|---|---|---|
| 通常テーブル regular | `{noun}s` | `users`, `orders` |
| 中間テーブル join | `{a_singular}_{b}s` | `order_products` |
| 履歴テーブル history | `{name}_histories` | `order_histories` |
| 一時テーブル temporary | `tmp_{name}s` | `tmp_imports` |
| マスタテーブル master | `{noun}s` | `categories`, `currencies` |

### 3.5. 使用禁止テーブル名パターン(Forbidden Table Name Patterns)
- **禁止**：SQL の予約語またはキーワードをそのままテーブル名にすることは**禁止**する。
- **FORBIDDEN_LIST**：`order`、`group`、`table`、`select`、`insert`、`update`、`delete`、`from`、`where`、`index`
- **対処方法**：代替案は第 7 節に厳格に従うこと。

---

## 4. カラム命名規約(Column Naming)

### 4.1. 主キーの規約(Primary Key)
- **規則**：テーブルの主キーのカラム名は `id` に**固定しなければならない**。
<!-- rule: db.pk-default-varchar10 -->
- **規則**：本プロジェクトでは、主キー型を**デフォルト**で文字列 `VARCHAR(10)`、`NOT NULL` とし、値は採番関数 `public.next_business_id(prefix)` で生成する(§4.1-SEQ 参照)。`VARCHAR` の主キーに `GENERATED ... AS IDENTITY` を使用することは**禁止**する(IDENTITY は整数系のみ対応)。
- **EXCEPTION**：1:1 / 1:0..1 の子テーブルは `id` を省略してよく、親テーブルへの外部キーカラムを**共有主キー**とする(詳細は §4.1-SHARED)。
- **EXCEPTION**：数値の自動採番主キーを選択するテーブルでは、型は `BIGINT` とし `GENERATED ALWAYS AS IDENTITY` を使用**しなければならない**。`NUMERIC`/`DECIMAL` を IDENTITY カラムに使用することは**禁止**する(PostgreSQL エラー：`identity column type must be smallint, integer, or bigint`)。
- **EXCEPTION**：分散アーキテクチャでは、PostgreSQL ネイティブの `UUID` 型による UUID 主キーを使用してよい。ただし、テーブル定義の DDL コメントで理由を明記し、§4.1-UUID の規定に従うこと。
- **EXCEPTION**：設定・参照系の master テーブル(外部からの FK 参照がなく、行数が少なく、安定した自然な一意キーが存在するもの)は、自然キーを主キーとし、採番 `id` を省略してよい(詳細は §4.1-NATURAL)。
<!-- rule: db.pk-users-user-id -->
- **EXCEPTION**：`users` は `user_id`(ユーザー ID の業務キー、`VARCHAR(10)` 採番)を主キーとし、**汎用の `id` ではない**。その 1:1/0..1 の子テーブルは `user_id` を共有する(§4.1-SHARED 参照)。
- **誤った例**：`user_id`(当該テーブルの主キーとして)、`userId`、`ID`、`pk`、`id NUMERIC GENERATED ALWAYS AS IDENTITY`(NUMERIC は IDENTITY カラムにできない)
- **正しい例**：`id VARCHAR(10) NOT NULL`(文字列採番主キー、デフォルト)、`id BIGINT GENERATED ALWAYS AS IDENTITY`(数値自動採番の代替)

#### §4.1-UUID：分散環境における主キーの規約
- **適用条件**：以下のいずれかの条件を満たす場合に限り、自動採番整数の主キーの代わりに UUID を使用してよい。
  - システムに、複数 DB のデータをマージする要件がある
  - アプリケーション層で主キーを事前生成する必要がある(データベースへの往復を避けるため)
- **規則**：UUID 主キーの型は PostgreSQL ネイティブの `UUID` 型を**使用しなければならない**。`CHAR(36)` による代用は禁止する。
- **規則**：UUID 値は `gen_random_uuid()` 関数で**生成しなければならない**。形式は UUID v4 に従う。
- **バージョン要件**：`gen_random_uuid()` は PostgreSQL 13 以上で組み込み済みであり、拡張は不要。PostgreSQL 12 以下では、事前に `CREATE EXTENSION IF NOT EXISTS pgcrypto;` を実行する必要がある。
- **正しい例**：
```sql
      -- PostgreSQL 13+(拡張不要)
      id UUID NOT NULL DEFAULT gen_random_uuid()

      -- PostgreSQL 12 以下(pgcrypto 拡張の有効化が必要)
      CREATE EXTENSION IF NOT EXISTS pgcrypto;
      id UUID NOT NULL DEFAULT gen_random_uuid()
```
- **誤った例**：`id CHAR(36) NOT NULL DEFAULT gen_random_uuid()`(型の不一致。gen_random_uuid() は uuid 型を返し、文字列ではない)

<!-- rule: db.pk-numbering-function -->
#### §4.1-SEQ：文字列主キーの採番規約
- **適用**：`VARCHAR(10)` の文字列主キーを採用するすべてのテーブル(本プロジェクトのデフォルト)。
- **値の形式**：`{prefix}{ゼロ埋め連番}`。全体の長さは 10 文字以下で**なければならない**。`prefix` は英大文字(1〜2 文字)とし、連番は DB のシーケンスで管理する。
- **採番の役割分担**：アプリケーション層が `prefix` を決定し(例：ユーザー種別に応じた E / A / C)、DB のシーケンスが連番を担当し、共有関数が結合して完全な主キー値を返す。
- **シーケンス命名**：`prefix` ごとに独立したシーケンスを 1 本用意し、`seq_id_{prefix_lowercase}` と命名する(例：`seq_id_e`、`seq_id_a`、`seq_id_c`)。同一 prefix は 1 本のシーケンスを共用し、1 から開始する。
- **採番関数**(全 DB 共有、初期化時に 1 回作成、`public` Schema に明示的に配置)：
```sql
      CREATE OR REPLACE FUNCTION public.next_business_id(p_prefix TEXT, p_width INT DEFAULT 4)
      RETURNS VARCHAR AS $$
      DECLARE
        v_seq TEXT := 'public.seq_id_' || lower(p_prefix);
        v_num BIGINT;
      BEGIN
        v_num := nextval(v_seq::regclass);
        RETURN p_prefix || lpad(v_num::text, p_width, '0');
      END;
      $$ LANGUAGE plpgsql;
```
- **Flyway 実行順序**：`init_functions.sql`(`next_business_id` 関数の作成) → 各 prefix のシーケンス(`CREATE SEQUENCE`) → テーブル作成スクリプト。

**prefix 割当表**(「自前の主キーを持つ」エンティティのみ。1:1 / 0..1 の子テーブルは §4.1-SHARED を参照、採番しない)：

| テーブル名 | prefix | 連番の桁数 | シーケンス名 |
|---|---|---|---|
| `users` | `E`/`A`/`C` | 4 | `seq_id_e`/`seq_id_a`/`seq_id_c` |
| `departments` | `DP` | 4 | `seq_id_dp` |
| `change_requests` | `CR` | 8 | `seq_id_cr` |
| `request_attachments` | `RA` | 8 | `seq_id_ra` |
| `request_status_histories` | `RH` | 8 | `seq_id_rh` |
| `system_lock_histories` | `LH` | 8 | `seq_id_lh` |
| master×4(request_type_limits / escalation_emails / holidays / system_settings) | 自然キー PK(§4.1-NATURAL 参照、採番しない) | — | — |

> ユーザーの prefix はアプリケーション層がユーザー種別(E/A/C)に応じて渡し、`next_business_id(prefix, 4)` を呼び出す(カラムに DEFAULT なし)。それ以外の prefix 固定のテーブルは、カラムの `DEFAULT public.next_business_id('XX', 桁数)` を用いる。id の全長は一律 `VARCHAR(10)` 以内とする。

<!-- rule: db.pk-shared-child -->
#### §4.1-SHARED：1:1 / 0..1 子テーブルの共有主キー例外
- **適用**：親テーブルと 1:1 または 1:0..1 の関係にあり、独立した業務識別子を持たない子テーブル。
- **規則**：自身の `id` を省略**してよく**、親テーブルへの外部キーカラム `{parent_singular}_id` をそのまま主キーとする(当該カラムは PRIMARY KEY と FOREIGN KEY を兼ねる)。
- **規則**：この種のテーブルには、採番 prefix / シーケンスを割り当て**ない**。
- **DDL コメント**：当該カラムに `-- shared PK: same as {parent}.id` を宣言すること。
- **例**：`user_contacts` / `user_secrets` / `user_bank_accounts` / `system_locks` はいずれも `user_id` を主キーとする。

<!-- rule: db.pk-natural-master -->
#### §4.1-NATURAL：設定・参照テーブルの自然キー主キー例外

- **適用**：設定/参照系の master テーブルで、次の条件を**すべて**満たすもの。① 他のテーブルから外部キーで参照されない、② 行数が少なく、変動が小さい、③ 安定した自然な一意キーが存在する(業務上変更されない)。
- **規則**：採番 `id` を省略**してよく**、自然キーのカラムを PRIMARY KEY とする。この種のテーブルには、採番 prefix / シーケンスを割り当て**ない**。
- **規則**：自然キーが将来変更される可能性がある場合は、採番 `id` 方式に戻すこと(自然キーは UNIQUE とする)。
- **本プロジェクトの適用テーブル**：

| テーブル名 | 自然キー PK | 型 |
|---|---|---|
| `request_type_limits` | `request_type` | INT |
| `escalation_emails` | `email_address` | VARCHAR(255) |
| `holidays` | `holiday_date` | DATE |
| `system_settings` | `setting_key` | VARCHAR(100) |

<!-- rule: db.enum-code-table -->
#### §4.1-CODE：列挙コードテーブルの規約

- **適用**：T07 の列挙・ステータス・区分コードを保存する列。
- **規則**：列挙型（T07 の `category_code` 単位）ごとに 1 つのコードテーブルを作成し、列挙を保持する列はそのコードテーブルへの外部キーで値域を制約しなければならない。`CHECK` 制約による値域列挙と `ENUM` 型（`CREATE TYPE`）は**使用禁止**。
- **規則**：コードテーブル名は `{enum_snake}_codes`（例：`user_role_codes`）とし、主キーは `{enum_snake}_code`（INT。T07 の属性コードをそのまま用い、採番 prefix / シーケンスを割り当て**ない**）、あわせて `{enum_snake}_name`（VARCHAR(50) NOT NULL UNIQUE。T07 の英語名＝API 値の UPPER_SNAKE_CASE）を持つ。標準監査項目は §4.3 に従う。
- **規則**：列挙を保持する列は業務上の列名（`status`、`role` 等。§4.5 に従う）のまま `INT` 型とし、外部キーカラム名の規則（§4.2）の例外とする。必須項目は NOT NULL、省略可の項目は NULL とする（NULL は外部キーの検証対象外）。
- **規則**：コードテーブルの行は T07 の定義と一致させ、投入・変更は Flyway の反復スクリプト（`R__`、冪等な upsert）で行う。

### 4.2. 外部キーの規約(Foreign Key)
<!-- rule: db.fk-column-naming -->
- **規則**：外部キーのカラム名は `{被参照テーブルの単数形}_id` 形式を**使用しなければならない**。
- **規則**：外部キーのカラム型は `VARCHAR` または `NUMERIC` で**なければならない**（§4.1-CODE のコードテーブルへの外部キーは `INT`）。
- **誤った例**：`userId`、`fk_user`、`user`、`u_id`
- **正しい例**：`user_id`(users テーブルを参照)、`order_id`(orders テーブルを参照)

### 4.3. グローバル必須カラムの規約(標準監査項目) (Required Columns)
<!-- rule: db.common-columns -->
- **規則**：以下の 5 つの標準監査項目は、すべてのデータテーブルに**存在しなければならない**。

| カラム名 | 型 | 制約 | 説明 |
|---|---|---|---|
| `created_at` | TIMESTAMPTZ | NOT NULL DEFAULT CURRENT_TIMESTAMP | 作成日時 |
| `created_by` | VARCHAR(10) | NOT NULL DEFAULT 'SYSTEM' | 作成者のユーザー ID。システム/初期データは `'SYSTEM'` |
| `updated_at` | TIMESTAMPTZ | NOT NULL DEFAULT CURRENT_TIMESTAMP | 更新日時。PostgreSQL は `ON UPDATE` 構文をサポートしないため、トリガー(Trigger)による自動更新を**実装しなければならない**。標準実装は §4.3-Trigger を参照。 |
| `updated_by` | VARCHAR(10) | NOT NULL DEFAULT 'SYSTEM' | 更新者のユーザー ID。システム/初期データは `'SYSTEM'` |
| `deleted_at` | TIMESTAMPTZ | NULL | 論理削除日時 |

<!-- rule: db.soft-delete-column -->
- **EXCEPTION**：以下の種別のテーブルは `deleted_at` を省略してよいが、テーブル定義の DDL コメントで `-- no soft delete: {理由}` の形式により**必ず宣言すること**。
  1. 一時テーブル(テーブル名が `tmp_` で始まるもの)
  2. 履歴テーブル(テーブル名が `_histories` で終わるもの)
  3. 純粋な関連中間テーブル(独立した業務エンティティとしての意味を持たず、2 テーブル間の関連のみを保持するもの)
  4. コードテーブル(§4.1-CODE)

<!-- rule: db.updated-at-trigger -->
#### §4.3-Trigger：`updated_at` 標準トリガーの実装

- **規則**：以下のトリガー関数は全 DB 共有の関数であり、**データベース初期化時に 1 回だけ作成すればよい**。
- **規則**：`updated_at` を含む各テーブルには、対応するトリガーを**登録しなければならない**。トリガー名は `trg_set_updated_at_{table_name}` 形式に従う。
- **長さの免除**：トリガー名は PostgreSQL ネイティブの 63 文字制限に従い、§2.4 の 30 文字ルールは**適用しない**。接頭辞 `trg_set_updated_at_` が 19 文字を占めるため、`table_name` 部分は最大 44 文字まで使用でき、すべての正当なテーブル名を網羅できる。

```sql
      -- ステップ 1：全 DB 共有のトリガー関数を作成(1 回のみ実行)
      CREATE OR REPLACE FUNCTION public.set_updated_at()
      RETURNS TRIGGER AS $$
      BEGIN
        NEW.updated_at = CURRENT_TIMESTAMP;
        RETURN NEW;
      END;
      $$ LANGUAGE plpgsql;

      -- ステップ 2：各テーブルにトリガーを登録(テーブルごとに 1 回実行、{table_name} を置換)
      CREATE TRIGGER trg_set_updated_at_{table_name}
      BEFORE UPDATE ON {table_name}
      FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
```

- **正しい例**(`users` テーブルの場合)：
```sql
      CREATE TRIGGER trg_set_updated_at_users
      BEFORE UPDATE ON users
      FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
```

#### §4.3-Trigger-Ops：トリガーの運用とマイグレーションに関する説明

- **自動クリーンアップ**：トリガーは所属テーブルの削除に伴い**自動的に削除**されるため、手動でのクリーンアップは不要。
- **テーブルのリネーム**：PostgreSQL ではトリガー名はテーブルのリネームに伴って自動更新**されない**。テーブルをリネームする際は、旧トリガーを手動で削除して再作成しなければならない。**以下のスクリプトは `ALTER TABLE RENAME` の実行完了後に実行しなければならない**：
```sql
      -- 前提：ALTER TABLE {old_table_name} RENAME TO {new_table_name}; が実行完了していること

      -- ステップ 1：旧トリガーを削除(テーブルは既に新名称となっているため、ON 句には新テーブル名を使用)
      DROP TRIGGER IF EXISTS trg_set_updated_at_{old_table_name} ON {new_table_name};

      -- ステップ 2：トリガーを再作成(新テーブル名を使用)
      CREATE TRIGGER trg_set_updated_at_{new_table_name}
      BEFORE UPDATE ON {new_table_name}
      FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
```

> **注意**：共有関数 `public.set_updated_at()` は `public` Schema 配下に明示的に作成する。これにより、マルチ Schema 構成のすべての Schema から呼び出せることを保証し、`search_path` に `public` が含まれないことによる関数利用不可の問題を回避する。

- **マイグレーションツール連携**(Flyway / Liquibase)：
  - `public.set_updated_at()` 共有関数は、初期化マイグレーションスクリプト(例：`init_functions.sql`)で**作成しなければならない**。すべてのテーブル作成スクリプトより先に実行されることを保証する。
  - 各テーブルのトリガーは、対応するテーブルのテーブル作成マイグレーションスクリプト内で、`CREATE TABLE` 文の直後に**作成しなければならない**。
  - **正しい例**(Flyway スクリプトの順序)：
```
    init_functions.sql   -- public.set_updated_at() 関数を作成
    create_users.sql     -- CREATE TABLE users + CREATE TRIGGER
    trg_set_updated_at_users
    create_orders.sql    -- CREATE TABLE orders + CREATE TRIGGER
    trg_set_updated_at_orders
```

### 4.4. 真偽値フラグカラムの規約(Boolean Flag Columns)
- **規則**：真偽値(Boolean)型のフラグ項目は `is_{name}` 形式で命名しなければならない。
- **誤った例**：`active`、`enabled`、`activeFlag`、`flg_active`
- **正しい例**：`is_active`、`is_deleted`、`is_verified`

### 4.5. ステータスカラムの規約(Status Columns)
- **規則**：単一の業務ステータス項目は `status` に**固定して命名しなければならない**。
- **規則**：1 つのテーブル内に複数の観点のステータス項目が存在する場合は、`{domain}_status` 形式を**使用しなければならない**。
- **誤った例**：`st`、`state`、`orderStatus`、`order_state`
- **正しい例**：`status`、`payment_status`、`shipping_status`

### 4.6. 金額・価格カラムの規約(Amount / Price Columns)
- **規則**：金額または価格に関わるすべての項目には、それぞれ `{name}_amount` または `{name}_price` の接尾辞を**付与しなければならない**。
- **誤った例**：`price`、`total`、`amt`、`cost`
- **正しい例**：`unit_price`、`total_amount`、`discount_amount`

### 4.7. 日付・時刻カラムの規約(Datetime Columns)
- **規則**：時刻の要素を含む日時項目には `_at` 接尾辞を**付与しなければならない**。
- **規則**：日付のみを保持する項目(時刻の精度を持たないもの)には `_date` 接尾辞を**付与しなければならない**。
- **誤った例**：`created`、`createDate`、`order_time`
- **正しい例**：`created_at`、`ordered_at`、`birth_date`、`due_date`

### 4.8. カウントカラムの規約(Count Columns)
- **規則**：件数・回数・集計数量を表す項目には、`{name}_count` 接尾辞を**付与しなければならない**。
- **誤った例**：`views`、`loginNum`、`cnt`
- **正しい例**：`view_count`、`login_count`、`retry_count`

### 4.9. テキストカラムの規約(Text Columns)
- **規則**：短い説明や備考には `note` を、長文または詳細な説明テキストには `description` を**使用しなければならない**。
- **誤った例**：`memo`、`txt`、`info`、`detail`
- **正しい例**：`note`、`description`、`body`

---

## 5. データ型マッピング規約(Data Type Mapping)

<!-- rule: db.type-mapping -->
- **規則**：データ型の定義は下表のマッピングに**厳格に従わなければならない**。任意の指定は禁止する。

| 業務概念 | DB 型 | 制約 | 説明 |
|---|---|---|---|
| 主キー(文字列/採番)| VARCHAR(10) | NOT NULL | 本プロジェクトの**デフォルト** PK。値は `public.next_business_id()` で生成する(§4.1-SEQ 参照)。IDENTITY は禁止 |
| 主キー(数値自動採番·代替)| BIGINT | NOT NULL GENERATED ALWAYS AS IDENTITY | 代替 PK。IDENTITY は整数系のみ対応のため、NUMERIC/DECIMAL は禁止 |
| 主キー(UUID)| UUID | NOT NULL DEFAULT gen_random_uuid() | 分散環境の PK |
| 氏名·タイトル | VARCHAR(100) | NOT NULL | 実際の業務に応じて長さを調整してよい |
| メールアドレス | VARCHAR(255) | NOT NULL | RFC 5321 標準に厳格に準拠 |
| URL | VARCHAR(2048) | NULL 許容 | |
| 短文テキスト·備考 | VARCHAR(500) | NULL 許容 | |
| 長文テキスト·説明 | TEXT | NULL 許容 | 格納上限なし |
| 金額·価格 | NUMERIC(15,2) | NOT NULL DEFAULT 0 | 浮動小数点型の使用は禁止 |
| 数量·件数 | NUMERIC | NOT NULL DEFAULT 0 | |
| ステータス·列挙型(T07) | INT | NOT NULL(カラムを NULL 可とすることも可) | 列挙/ステータスコードは**整数で格納**し、列挙型ごとのコードテーブルへの外部キーで値域を制約する（§4.1-CODE）。`CHECK` による値域列挙と `ENUM` 型は使用しない。値の定義は spec-data-dictionary-rule.md T07 を唯一の情報源とする。API 層では Java Enum 経由で UPPER_SNAKE_CASE の文字列として出力する。詳細は DBQ-01 |
| 真偽値フラグ | BOOLEAN | NOT NULL DEFAULT FALSE | TRUE / FALSE / NULL の三値論理によるエラーを避けるため、NOT NULL の宣言が必須 |
| 日時 datetime | TIMESTAMPTZ | NOT NULL DEFAULT CURRENT_TIMESTAMP | DB セッションのタイムゾーンはシステムタイムゾーン（Asia/Tokyo、`spec-01-system-architecture-ui.md`）に一致させる |
| 日付のみ | DATE | NOT NULL | 時刻の精度が不要な場合に使用 |
| 表示順 | NUMERIC | NOT NULL DEFAULT 0 | 昇順表示の制御に使用 |
| JSON·メタデータ | JSONB | NULL 許容 | GIN インデックスをサポートし、内部項目に対する効率的な検索が可能。元のテキスト形式を厳密に保持する必要がある場合は `JSON` 型に変更し、DDL コメントで理由を記載する |
| ファイルパス | VARCHAR(512) | NULL 許容 | |
| IP アドレス | VARCHAR(45) | NULL 許容 | IPv6 をネイティブにサポートすること |
| パーセンテージ | NUMERIC(5,2) | NOT NULL DEFAULT 0 | 有効な数値範囲：0.00 〜 100.00 |

### 5.1. 重要な型の強制規則(Critical Type Rules)
- **規則**：金額および価格の項目には `FLOAT` や `DOUBLE` を**使用禁止**とし、浮動小数点計算の誤差を排除するため、`NUMERIC(15,2)` に**統一しなければならない**。
- **規則**：真偽値のステータスにはネイティブの `BOOLEAN` 型を**使用しなければならない**。PostgreSQL は `BOOLEAN` をネイティブにサポートしているため、整数型による代用は不要。
<!-- rule: time.db-timestamptz -->
- **規則**：すべての `TIMESTAMPTZ` 項目は、DB セッションのタイムゾーンをシステムタイムゾーン（Asia/Tokyo、`spec-01-system-architecture-ui.md` を参照）に**一致させなければならない**。

---

## 6. 制約・インデックス命名規約(Constraint and Index Naming)

### 6.1. 命名パターン定義(Naming Patterns)

<!-- rule: db.constraint-index-naming -->
| 制約種別 | 命名パターン | 例 |
|---|---|---|
| 主キー primary key | `pk_{table}` | `pk_users` |
| 外部キー foreign key | `fk_{table}_{ref_table}` | `fk_orders_users` |
| 一意制約 unique | `uq_{table}_{column}` | `uq_users_email` |
| インデックス index | `idx_{table}_{column}` | `idx_orders_created_at` |
| 複合インデックス composite index | `idx_{table}_{col1}_{col2}` | `idx_orders_user_id_status` |
| チェック制約 check | `chk_{table}_{column}` | `chk_products_unit_price` |
| トリガー trigger | `trg_set_updated_at_{table}` | `trg_set_updated_at_users` |

### 6.2. インデックス設計ルール(Index Rules)
- **規則**：複合インデックスを構築する際は、選択度が高いカラムほどインデックスのカラム順の先頭に**配置しなければならない**。
- **規則**：すべての外部キーカラムに、独立したインデックスを明示的に**作成しなければならない**。
- **規則**：`created_at` 項目のみを対象とした単一カラムインデックスは作成不要とする(複合インデックスへの組み込みを推奨)。

---

## 7. 使用禁止語・予約語の管理(Reserved and Banned Words)

- **規則**：下表に挙げる語は、テーブル名またはカラム名として単独で使用することを**禁止**する。

| 使用禁止語 | 禁止理由 | 代替案 |
|---|---|---|
| `code` | 単独では概念が不明確 | `{domain}_code` を使用。例：`product_code` |
| `data` | 意味が曖昧 | `payload`, `content`, `body` |
| `delete` | SQL 標準キーワード | `removal` |
| `detail` | 意味が曖昧 | `description` または `note` を使用 |
| `flag` | 記述性が不足 | `is_{name}` 形式を使用。例：`is_active` |
| `group` | SQL 標準キーワード | `team`, `category` |
| `info` | 非標準の曖昧な略語 | `detail`, `summary`, `profile` |
| `insert` | SQL 標準キーワード | `entry`, `addition` |
| `list` | カラム名として不適切 | 複数形の名詞を使用。例：`tags`、`categories` |
| `name` | 単独では概念が不明確 | `{domain}_name` を使用。例：`product_name` |
| `number` | 単独では概念が不明確 | `{domain}_number` を使用。例：`invoice_number` |
| `order` | SQL 標準キーワード | `purchase`, `booking` |
| `result` | 意味が曖昧 | `{domain}_result` を使用。例：`payment_result` |
| `select` | SQL 標準キーワード | `choice`, `option` |
| `table` | SQL 標準キーワード | `entity`, `record` |
| `text` | 意味が曖昧 | `body`、`content`、`description` を使用 |
| `type` | 単独では概念が不明確 | `{domain}_type` を使用。例：`payment_type` |
| `update` | SQL 標準キーワード | `modification`, `revision` |
| `value` | 意味が曖昧 | `amount`, `score`, `rating` |

- **注意**：`status` は本規約が推奨するカラム名であるが、一部の SQL 方言ではコンテキストキーワードに該当する。PostgreSQL では `status` をカラム名として使用しても安全である。**ネイティブ SQL またはビュー(View)**を記述する際は、複数 DB 間のマイグレーションにおける構文上の曖昧さを排除するため、`status` をダブルクォート(`"status"`)で囲むことを強く推奨する。ORM(Hibernate、MyBatis など)を使用する場合は、ORM フレームワークが自動的にクォートを処理するため、手動での追加は不要。

---

## 8. 正誤事例対照表(Good VS Bad Examples)

### 8.1. テーブル名の事例対照(Table Names)

| 項目 | 誤った例 | 正しい例 | 説明 |
|---|---|---|---|
| 単数形 | `user` | `users` | テーブル名は複数形としなければならない |
| キャメルケース | `orderItems` | `order_items` | 小文字スネークケースに厳格に統一しなければならない |
| SQL キーワード | `order` | `purchases` | SQL キーワードをそのまま使用することは禁止 |
| 略語・接尾辞 | `usr_mst` | `users` | 略語および `mst` 接尾辞の使用は禁止 |
| 関連中間テーブル | `products_orders` | `order_products` | 構成するテーブル名はアルファベット順の昇順としなければならない |

### 8.2. カラム名の事例対照(Column Names)

| 項目 | 誤った例 | 正しい例 | 説明 |
|---|---|---|---|
| 主キー定義 | `user_id`, `userId`, `ID` | `id` | 主キー名は `id` に固定しなければならない |
| 外部キー定義 | `userId`, `fk_user`, `user` | `user_id` | `{table_singular}_id` 形式としなければならない |
| 監査項目 | `createDate`, `created`, `InsertDate` | `created_at` | `_at` 接尾辞を付与して統一しなければならない |
| 真偽値フラグ | `active`, `activeFlag`, `enabled` | `is_active` | `is_` 接頭辞で統一しなければならない |
| 数値の金額 | `price`, `total`, `amt` | `total_amount` | `_amount` 接尾辞を使用しなければならない |
| ステータス定義 | `st`, `orderStatus`, `State` | `status` | 単一のステータスは `status` に固定しなければならない |
| カウント集計 | `views`, `loginNum`, `cnt` | `view_count` | `_count` 接尾辞を使用しなければならない |

### 8.3. 制約名の事例対照(Constraint Names)

| 項目 | 誤った例 | 正しい例 | 説明 |
|---|---|---|---|
| 主キー制約 | `PRIMARY`, `pk1`, `users_pk` | `pk_users` | `pk_{table}` 形式に適合しなければならない |
| 外部キー制約 | `fk1`, `user_fk`, `orders_user` | `fk_orders_users` | `fk_{table}_{ref}` 形式に適合しなければならない |
| 一意制約 | `unique1`, `users_email_unique` | `uq_users_email` | `uq_{table}_{col}` 形式に適合しなければならない |
| 基本インデックス | `index1`, `users_idx` | `idx_users_email` | `idx_{table}_{col}` 形式に適合しなければならない |
| トリガー | `updated_at_trigger`, `trig_users` | `trg_set_updated_at_users` | `trg_set_updated_at_{table}` 形式に適合しなければならない |

---

## 9. AI Prompt テンプレート(AI Prompt Template)

本節では、AI が DB Schema を生成する際に、本規約ドキュメントを Prompt に埋め込んで利用する方法を示す。

```
## 前提条件
以下のファイルに定義されたルールを読み込み、厳格に遵守してください：
- DB 命名規約：./spec-database-rule.md
- データ辞書：../基本設計/共通/spec-13-data-dictionary-ui.md

## タスク (Task)
以下の要件に基づいて DB Schema を生成してください：
[ここに要件を記入]
```
---

## 改訂履歴

| バージョン | 日付 | 作成者 | 変更内容 |
|---|---|---|---|
| 1.0.0 | 2026/06/06 | nebulasoft Co., Ltd | 初版作成 |
| 1.1.0 | 2026/06/08 | nebulasoft Co., Ltd | TIMESTAMPT→TIMESTAMPTZ　DECIMAL→NUMERIC |
| 1.2.0 | 2026/06/08 | nebulasoft Co., Ltd | 主キー型の修正 |
| 1.3.0 | 2026/06/09 | nebulasoft Co., Ltd | 主キー型の矛盾を修正(NUMERIC は IDENTITY にできない、例を同期)。§5 の主キー行を「文字列採番」「数値自動採番の代替」の 2 種に分割。§4.1-SEQ の文字列採番関数 `next_business_id` とシーケンス命名規約を追加(prefix 割当表に未確定項目を含む)。created_by/updated_by に `DEFAULT 'SYSTEM'` を追加 |
| 1.4.0 | 2026/06/10 | nebulasoft Co., Ltd | §4.1-SHARED(1:1 / 0..1 子テーブルの共有主キー例外、自身の id を省略)を追加。§4.1-SEQ の prefix 割当表を確定(users E/A/C 4 桁、departments DP 4 桁、change_requests/request_attachments/request_status_histories/system_lock_histories 8 桁。4 つの master の主キー＝自然キー §4.1-NATURAL、DBQ-02 解決済み) |
