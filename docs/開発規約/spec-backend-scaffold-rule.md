<!-- devtool:file-region id="document-header" -->
# Backend Scaffold Spec（基盤・業務 共通）

> backend コード生成における「整形」と「定型メンバ」の責務を定義する横断規約。
> **基盤（基本設計/基盤）と業務（詳細設計）の両方**がこの規約に従う。
> 各 spec の粒度を下げ（整形・定型コードを spec から剥がす）、再生成の差分ゼロを担保するための正本。
>
> 関連: [`spec-backend-coding-rule.md`](spec-backend-coding-rule.md)（実装規約・§13 校验门）/ 整形プラグイン設定は
> [`基本設計/基盤/インタフェース仕様/spec-project-structure.yaml`](../基本設計/基盤/インタフェース仕様/spec-project-structure.yaml) の `build_plugins` / `root_files` / `code_generation_pipeline`。

<!-- devtool:end-file-region -->
<!-- devtool:block id="rules.springboot.backend-scaffold-rule.s01" -->
## 0. 責務分離（なぜ spec の粒度を下げられるか）

決定論は 2 層に分けて担保する。spec が抱えるのは layer-1 のみ。

| 層 | 対象 | SSOT | spec に書くか |
|----|------|------|:---:|
| **layer-1 意味** | どのメンバが存在するか / `code_body` のロジック / javadoc の**文面** | **spec** | ✅ 書く（曖昧化禁止） |
| **layer-2 整形** | 行長 / 空行 / 空白 / インデント / import 順 / 引数折り返し / javadoc タグ整列 | **Spotless（formatter）** | ❌ 書かない（formatter が上書き） |

帰結:
- 各 spec から **getter/setter 等の定型メンバ**（後述 §1）と **整形記述**（行長・折り返し等）を削除してよい。
- `spec-backend-coding-rule.md` §13 の **G-换行** は本規約の formatter 適用（`spotless:apply`）で自動担保される。

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-scaffold-rule.s02" -->
## 1. 派生メンバ生成規約（`fields` から機械展開。spec に getter/setter を書かない）

### フィールド宣言の拡張属性

`fields` の各要素が持てるキー:

| キー | 意味 |
|------|------|
| `name` / `modifiers` / `type` / `value` | 従来どおり |
| `label` | `@return` / `@param` に出す名詞。**省略時はアクセサを生成しない**（定数等） |
| `subject` | javadoc 文の主語（省略時 = `label`）。文主語と `@return` ラベルがズレる場合のみ指定 |
| `access` | `[get]` / `[get, set]` / `[]`（省略時 `[]` = アクセサなし） |

### getter（`access` に `get` を含むフィールド）

```
signature: public {Type} get{Pascal(name)}()    # boolean も get を使用（is は使わない）
body     : return {name};
javadoc  :
  /**
   * {subject}を返す。
   *
   * @return {label}
   */
```

### setter（`access` に `set` を含むフィールド）

```
signature: public void set{Pascal(name)}({Type} {name})
body     : this.{name} = {name};
javadoc  :
  /**
   * {subject}を設定する。
   *
   * @param {name} {label}
   */
```

### 文連結ルール（getter/setter javadoc 共通・決定論で必須）

javadoc 本文の `{subject}を返す。` / `{subject}を設定する。` を組み立てる際、
**`subject` 末尾が ASCII 英数字の場合は助詞「を」の前に半角スペースを 1 個挿入する**。
末尾が日本語文字（かな/漢字）なら挿入しない。`@return` / `@param` の `{label}` は逐語（挿入なし）。

| subject 末尾 | 例 | 生成文 |
|------|------|------|
| ASCII 英数字 | `メッセージ ID` | `メッセージ ID を返す。`（ID と を の間に空白） |
| 日本語 | `結果コード` / `メッセージ引数` | `結果コードを返す。` / `メッセージ引数を返す。`（空白なし） |

> この規則を入れないと ID 等を含む既存 javadoc と byte 不一致になる（横展開で実測）。

### record

record の accessor（`field()` 形式）は Java 標準のため**生成しない・spec にも書かない**。

### constructor

- パラメータ名 == フィールド名 のものは宣言順に `this.{name} = {name};` を**自動生成**する。
- それ以外の初期化（`now()` / `MDC.get()` / 引数名がフィールド名と異なる代入）は `extra_body` に逐語で書く。
- 両方ある場合は「自動代入 → `extra_body`」の順で連結する。
- 引数名とフィールド名が食い違い自動代入が成立しないコンストラクタは、従来どおり `code_body` を全文記述してよい（**規約より優先**）。

### 記述例（before → after）

```yaml
# before: getter を 1 個ずつ手書き（javadoc・body・空行まで）
- name: getTimestamp
  visibility: public
  return: OffsetDateTime
  javadoc: |
    生成時刻を返す。

    @return タイムスタンプ
  code_body: "return timestamp;"

# after: fields の 1 行に圧縮（生成 javadoc は上と 1 文字一致）
- { name: timestamp, modifiers: [private, final], type: OffsetDateTime, label: タイムスタンプ, subject: 生成時刻, access: [get] }
```

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-scaffold-rule.s03" -->
## 2. 整形の責務（formatter 側。spec では指定しない）

- **正本**: Spotless（Eclipse formatter profile 方式）。**生成時専用ツール**として生成パイプライン最終段で適用する（`apply`）。**納品物には Spotless プラグインも profile も含めない**（整形済みコードのみ納品）。
- **決定論**: spotless-maven-plugin 2.44.5 / eclipse.jdt 4.33 を pin（生成時ツールの版数。比較は**整形後同士**で行う）。
- **プロファイル正本**: `基本設計/基盤/インタフェース仕様/eclipse-formatter.xml`（profile name=ddmsaa / 行長 100 / 4スペース / 第一引数は開き括弧と同行 / 継続インデント +8）。納品物にはコピーしない。
- **javadoc は formatter で触らない**（`comment.format_javadoc_comments=false`）。javadoc は layer-1（spec SSOT）。`keep_*_on_one_line` の値は `one_line_never`。
- **spec が指定してはならない項目**: 行長・折り返し位置 / 空行・空白・インデント / import の並び順 / javadoc タグの整列。
  - `importOrder` / `removeUnusedImports` により、spec の `imports` リストは**順序非依存**になる。
- `code_body` は「意味が正しければ整形は問わない」。1 行で書いても formatter が規約幅で折り返すため、生成器ごとの整形揺れは差分にならない。
- `signature_raw` は「修飾子順・`throws` 等の**意味**」のためのみ逐語使用。空白整形は formatter が正規化する。

<!-- devtool:endblock -->
<!-- devtool:block id="rules.springboot.backend-scaffold-rule.s04" -->
## 3. 生成パイプライン（決定論の担保順序）

```
1. 生成      : spec（layer-1 = 意味）からコード生成。整形の揺れは許容
2. 派生展開  : 本規約 §1 に従い getter/setter/record/ctor自動代入を展開
3. 整形      : mvn spotless:apply（eclipse.jdt.version pin）で行長・空行・空白・import順を正規化
4. 比較      : 整形後コード同士で diff（決定論検証は必ず整形後で行う）
```
<!-- devtool:endblock -->
