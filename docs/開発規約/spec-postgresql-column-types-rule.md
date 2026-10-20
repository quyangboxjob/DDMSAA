<!-- devtool:file-region id="document-header" -->
# PostgreSQL Column Types Spec（カラムで使用可能な型一覧）

> PostgreSQL で 1 つのカラム（列）に宣言できるすべての型。
> PostgreSQL のバージョンは `基本設計/共通/spec-01-system-architecture-ui.md` §2.3 に従う（Aurora PostgreSQL 互換）。

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s01" -->
## 一、数値型

| 型 | 別名 | バイト数 | 説明 |
|------|------|------|------|
| `smallint` | `int2` | 2 | 小整数 |
| `integer` | `int`, `int4` | 4 | 通常の整数 |
| `bigint` | `int8` | 8 | 大整数 |
| `numeric(p,s)` | `decimal(p,s)` | 可変長 | 正確な小数。金額の第一選択 |
| `real` | `float4` | 4 | 単精度浮動小数点 |
| `double precision` | `float8` | 8 | 倍精度浮動小数点 |
| `smallserial` | `serial2` | 2 | 自動採番の小整数 |
| `serial` | `serial4` | 4 | 自動採番の整数 |
| `bigserial` | `serial8` | 8 | 自動採番の大整数 |

> 自動採番カラムは `integer GENERATED ALWAYS AS IDENTITY` と記述することもできる（推奨、SQL 標準に準拠）。
> 注：`smallserial` / `serial` / `bigserial` は**実際のデータ型ではなく**、「`smallint`/`integer`/`bigint` + シーケンス（sequence）の自動作成 + デフォルト値」の省略記法である。新規プロジェクトでは `GENERATED ALWAYS AS IDENTITY` を優先して使用することを推奨する。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s02" -->
## 二、文字（テキスト）型

| 型 | 別名 | 説明 |
|------|------|------|
| `varchar(n)` | `character varying(n)` | 可変長、最大長 n |
| `varchar` | `character varying` | 可変長、長さ制限なし |
| `char(n)` | `character(n)` | 固定長、空白で埋める |
| `text` | — | 可変長、上限なし |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s03" -->
## 三、日付 / 時刻型

| 型 | 別名 | 説明 |
|------|------|------|
| `date` | — | 日付のみ |
| `time` | `time without time zone` | 時刻のみ |
| `timetz` | `time with time zone` | タイムゾーン付きの時刻 |
| `timestamp` | `timestamp without time zone` | 日付+時刻（タイムゾーンなし） |
| `timestamptz` | `timestamp with time zone` | 日付+時刻（タイムゾーンあり） |
| `interval` | — | 時間間隔 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s04" -->
## 四、真偽値型

| 型 | 別名 | 説明 |
|------|------|------|
| `boolean` | `bool` | TRUE / FALSE / NULL |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s05" -->
## 五、バイナリ型

| 型 | 説明 |
|------|------|
| `bytea` | 可変長のバイナリデータ（ファイル、画像など） |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s06" -->
## 六、JSON 型

| 型 | 説明 |
|------|------|
| `json` | テキスト形式で格納し、入力をそのまま保持 |
| `jsonb` | バイナリ形式で格納し、インデックス作成が可能で検索が高速（第一選択） |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s07" -->
## 七、UUID 型

| 型 | 説明 |
|------|------|
| `uuid` | 128 ビットの一意識別子 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s08" -->
## 八、ネットワークアドレス型

| 型 | 説明 |
|------|------|
| `inet` | IPv4 / IPv6 ホストアドレス |
| `cidr` | IPv4 / IPv6 ネットワークセグメント |
| `macaddr` | MAC アドレス（6 バイト） |
| `macaddr8` | MAC アドレス（8 バイト / EUI-64） |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s09" -->
## 九、ビット列型

| 型 | 説明 |
|------|------|
| `bit(n)` | 固定長ビット列 |
| `bit varying(n)` | 可変長ビット列 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s10" -->
## 十、列挙型

| 型 | 説明 |
|------|------|
| `ENUM`（事前に `CREATE TYPE` が必要） | 固定された順序付きの値の集合。ステータスコードなど |

```sql
CREATE TYPE status AS ENUM ('draft', 'submitted', 'approved');
```

> 本プロジェクトでは `ENUM` 型を使用しない（`spec-database-rule.md` §4.1-CODE のコードテーブルを用いる）。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s11" -->
## 十一、配列型

| 記法 | 説明 |
|------|------|
| `型[]` | 任意の型の後ろに `[]` を付ける。例：`integer[]`、`text[]` |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s12" -->
## 十二、範囲型

| 型 | 説明 |
|------|------|
| `int4range` | 整数の範囲 |
| `int8range` | 大整数の範囲 |
| `numrange` | 数値の範囲 |
| `tsrange` | タイムスタンプの範囲（タイムゾーンなし） |
| `tstzrange` | タイムスタンプの範囲（タイムゾーンあり） |
| `daterange` | 日付の範囲 |

**多重範囲型（multirange、PG14+）**：各範囲型には対応する多重範囲型があり、1 つの値で連続しない複数の区間を表現できる。

| 型 | 説明 |
|------|------|
| `int4multirange` | 複数の整数範囲 |
| `int8multirange` | 複数の大整数範囲 |
| `nummultirange` | 複数の数値範囲 |
| `tsmultirange` | 複数のタイムスタンプ範囲（タイムゾーンなし） |
| `tstzmultirange` | 複数のタイムスタンプ範囲（タイムゾーンあり） |
| `datemultirange` | 複数の日付範囲 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s13" -->
## 十三、全文検索型

| 型 | 説明 |
|------|------|
| `tsvector` | 単語分割済みの文書ベクトル |
| `tsquery` | 全文検索のクエリ条件 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s14" -->
## 十四、幾何型

| 型 | 説明 |
|------|------|
| `point` | 点 (x, y) |
| `line` | 直線 |
| `lseg` | 線分 |
| `box` | 矩形 |
| `path` | パス |
| `polygon` | 多角形 |
| `circle` | 円 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s15" -->
## 十五、その他の型

| 型 | 説明 |
|------|------|
| `money` | 8 バイト、通貨（ロケール設定に依存するため、`numeric` への置き換えを推奨） |
| `xml` | XML ドキュメント |
| `pg_lsn` | WAL ログシーケンス番号 |
| `tsvector` / `tsquery` | 全文検索（上記参照） |
| `oid` およびシステム型 | システム内部で使用され、業務では通常使用しない |

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.postgresql.postgresql-column-types-rule.s16" -->
## 業務向け型選定の早見表

> 本プロジェクトの実際の型選定は `開発規約/spec-database-rule.md` §5 / `開発規約/spec-data-dictionary-rule.md` に従う。下表は PostgreSQL 全般の参考情報である。

| 場面 | 推奨型 |
|------|---------|
| 主キー（単一 DB の自動採番） | `bigint GENERATED ALWAYS AS IDENTITY` |
| 主キー（分散/複数 DB） | `uuid` |
| 金額、数量（正確） | `numeric(p,s)` |
| 名称、備考、テキスト | `text` または `varchar(n)` |
| フラグ | `boolean` |
| 作成/更新日時（タイムゾーンをまたぐ） | `timestamptz` |
| 日付のみ | `date` |
| 半構造化データ | `jsonb` |
| ファイル/画像 | `bytea`（大きなファイルは S3 に外部化してパスを保存することを推奨） |
| ステータスコード | コードテーブル + 外部キー（`ENUM` より優れ、コメントの保守が容易） |
<!-- devtool:endblock -->
