<!-- devtool:file-region id="document-header" -->
# プロジェクトデータ辞書：DDMSAA

> 本ファイルはプロジェクト固有のデータ辞書であり、T07列挙型定義、列定義、Principle 3 ホワイトリストを記載する。
> すべての規則は強制的な取り決めであり、例外はない。

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="rules.data-dictionary-project-temp.s01" -->
## 目次

- [プロジェクトデータ辞書：DDMSAA](#プロジェクトデータ辞書ddmsaa)
  - [目次](#目次)
  - [文書関係表](#文書関係表)
- [第1部：Principle 3 ホワイトリスト](#第1部principle-3-ホワイトリスト)
  - [タイプA：意味境界の絞り込み](#タイプa意味境界の絞り込み)
  - [タイプB：技術的競合による強制置換](#タイプb技術的競合による強制置換)
- [第2部：T07 列挙型定義](#第2部t07-列挙型定義)
    - T07-1〜T07-13（ロール〜出力状態）
- [第3部：列定義](#第3部列定義)
  - [改訂履歴](#改訂履歴)

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.data-dictionary-project-temp.s02" -->
## 文書関係表

> 本文書内では具体的なファイル名ではなく「文書名」で統一して参照する。ファイル名を変更する場合は、本表のみ更新すればよい。

| 文書名 | ファイル名 | 役割 | 本文書との関係 |
|---|---|---|---|
| プロジェクトデータ辞書（本文書） | `data-dictionary-{project}.md` | T07列挙型定義、列定義、Principle 3 ホワイトリスト | プロジェクト固有データの唯一の情報源 |
| データ辞書規範 | `spec-data-dictionary-rule.md` | 基礎型定義、列定義規則、共通項目の層間対照規範 | 本文書は同規範に従って記入する |
| 命名規約 | `spec-naming-rule.md` | 層をまたぐ命名規則と変換規則 | Principle 3 ホワイトリストの登録規則は同文書を正とする |
| DB開発規約 | `spec-database-rule.md` | DB命名規約、インデックス方針、トリガー規範 | SQL予約語の完全なリストは同文書を正とする |

---

# 第1部：Principle 3 ホワイトリスト

> AI がコードを生成する際は、まず本節を参照し、対象項目に明示的な変換登録が存在するかを確認すること。
> 登録規則は命名規約 §4 を参照する。
> 本節のホワイトリストは、全層のコード生成における唯一の検索窓口である。

<!-- devtool:endblock -->
<!-- devtool:block id="rules.data-dictionary-project-temp.s03" -->
## タイプA：意味境界の絞り込み

> 広い語が特定の文脈で十分に意味が明確となる場合に、限定語の省略を許容する変換である。
> 汎用的な変換はここに事前記入済みであり、プロジェクト固有の事情がなければ変更不要。

| 変換 | 変換方向 | 理由 |
|---|---|---|
| （DDMSAA は登録なし） | — | DDMSAA は業務キー（`VARCHAR(10)`）を主キー `id` としてそのまま API に出すため、`externalId` → `id` のような変換は不要（二重 ID 方式は使用しない。DBQ-08） |

<!-- プロジェクトでタイプAの変換を追加する場合は、上記の形式で追記する -->

<!-- devtool:endblock -->
<!-- devtool:block id="rules.data-dictionary-project-temp.s04" -->
## タイプB：技術的競合による強制置換

> 概念名が DB 層で SQL 予約語と競合する場合の強制置換の登録である。
> SQL 予約語の完全なリストは DB開発規約 を唯一の参照元とする。

| Backend / Frontend 概念名 | DB 代替名 | 競合理由 |
|---|---|---|

> 本プロジェクトには現在、タイプBの変換登録はない。技術的競合が発生した場合は、上記の形式で追記する。

---

<!-- rule: enum.t07-values -->
# 第2部：T07 列挙型定義

> 取りうる値の範囲は本定義を唯一の定義元とする。
> 値域の**拡大**（列挙値の追加）時の実施順序：本辞書 → コードテーブル（先に行を追加）→ アプリケーション層の Enum クラスをデプロイ
> 値域の**縮小**（列挙値の廃止）時の実施順序：本辞書 → アプリケーション層の Enum クラスをデプロイ（書き込み停止）→ 履歴データのクリーンアップ → コードテーブル（廃止値の行を削除）
> `category_code` は本ファイル内で一意でなければならず、追加時は現在の最大番号+1とする。廃止済みの番号は再利用不可とし、廃止時は番号を保持したまま定義箇所に「廃止済み」と明記する。
> コードテーブル（`{enum_type_name_snake_case}_codes`）の行は本辞書の定義と一致させる。

<!-- category_code の昇順に並べる -->

### ロール (user_role)

- **対応 category_code**: 1
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `user_role_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 管理者 | ADMIN |
| 2 | 営業担当 | EMPLOYEE |
| 3 | 顧客 | CUSTOMER |

### 利用者状態 (user_status)

- **対応 category_code**: 2
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `user_status_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 有効 | ACTIVE |
| 2 | 停用 | DISABLED |

### 顧客状態 (customer_status)

- **対応 category_code**: 3
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `customer_status_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 有効 | ACTIVE |
| 2 | 無効 | INACTIVE |

### 商品状態 (product_status)

- **対応 category_code**: 4
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `product_status_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 有効 | ACTIVE |
| 2 | 無効 | INACTIVE |

### 税区分 (tax_category)

- **対応 category_code**: 5
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `tax_category_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 標準税率 | STANDARD |
| 2 | 軽減税率 | REDUCED |
| 3 | 非課税 | EXEMPT |

### 注文状態 (order_status)

- **対応 category_code**: 6
- **DB実装**: `INT NOT NULL`（`order_status_histories.from_status` のみ `INT NULL`）
- **コードテーブル**: `order_status_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 下書き | DRAFT |
| 2 | 承認待ち | PENDING_APPROVAL |
| 3 | 差戻し | RETURNED |
| 4 | 承認済み | APPROVED |
| 5 | 取消済み | CANCELLED |

### 承認依頼状態 (approval_request_status)

- **対応 category_code**: 7
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `approval_request_status_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 承認待ち | PENDING |
| 2 | 承認 | APPROVED |
| 3 | 差戻し | RETURNED |

### 承認操作 (approval_action)

- **対応 category_code**: 8
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `approval_action_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 承認 | APPROVE |
| 2 | 差戻し | RETURN |

### 添付公開範囲 (attachment_visibility)

- **対応 category_code**: 9
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `attachment_visibility_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 社内 | INTERNAL |
| 2 | 顧客公開 | CUSTOMER |

### スキャン状態 (scan_status)

- **対応 category_code**: 10
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `scan_status_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 確認中 | PENDING |
| 2 | 安全 | SAFE |
| 3 | 拒否 | REJECTED |

### 通知イベント (notification_event)

- **対応 category_code**: 11
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `notification_event_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 承認依頼 | ORDER_SUBMITTED |
| 2 | 承認 | ORDER_APPROVED |
| 3 | 差戻し | ORDER_RETURNED |
| 4 | 取消し | ORDER_CANCELLED |
| 5 | CSV 出力完了 | EXPORT_COMPLETED |

### 通知状態 (notification_status)

- **対応 category_code**: 12
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `notification_status_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 待機中 | QUEUED |
| 2 | 送信中 | SENDING |
| 3 | 送信済み | SENT |
| 4 | 失敗 | FAILED |

### 出力状態 (export_status)

- **対応 category_code**: 13
- **DB実装**: `INT NOT NULL`
- **コードテーブル**: `export_status_codes`（下表の行を保持する）

| 属性コード | 属性名 | 英語名 |
|---|---|---|
| 1 | 待機中 | QUEUED |
| 2 | 生成中 | RUNNING |
| 3 | ダウンロード可 | READY |
| 4 | 失敗 | FAILED |
| 5 | 期限切れ | EXPIRED |

---

<!-- 以下、必要に応じて列挙型定義を追記する。category_code は連続して加算する -->

---

# 第3部：列定義

> すべての列は、データ辞書規範 第1部の基礎型を参照する。
> 英語列名のアルファベット順で統一管理する。列を追加する際は順序を維持すること。
> T07列挙型を参照する列は、説明欄に「参照: {型名}」の形式で明記すること。
> 共通列（主キー・監査列・論理削除日時）の定義はデータ辞書規範（`開発規約/spec-data-dictionary-rule.md`）を正とし、ここでの重複登録は不要。（`external_id`／二重 ID は使用しない。DBQ-08 解決済み）
> 「必須/任意」列は、基礎型名のプレフィックスと一致させる：`必須` → 必須、`任意` → 任意。

> **DDMSAA の列定義は `基本設計/共通/spec-13-data-dictionary-ui.md` §4 の項目語彙表（DI-xxx）を正とし、本部には重複登録しない。** 列の追加・変更は `spec-06-db-physical.yaml` → spec-13 の順に反映する。

---

<!-- devtool:endblock -->
<!-- devtool:block id="rules.data-dictionary-project-temp.s05" -->
## 改訂履歴

| バージョン | 日付 | 作成者 | 変更種別 | 変更内容 |
|---|---|---|---|---|
| 1.0.0 | 2026-10-20 | — | T07追加 | DDMSAA の T07 列挙型（13 種）を定義 |
| 1.0.1 | 2026-10-20 | — | 説明修正 | タイプA の汎用例（externalId → id）を削除。第3部の列定義は spec-13 の項目語彙表を正とする旨に変更 |

<!-- 変更種別：初版 / T07追加 / T07修正 / 列定義追加 / 列定義修正 / ホワイトリスト追加 / 説明修正 -->
<!-- T07追加/T07修正 は DB マイグレーション（コードテーブルの更新）が必要。説明修正 は DB マイグレーション不要 -->
<!-- devtool:endblock -->
