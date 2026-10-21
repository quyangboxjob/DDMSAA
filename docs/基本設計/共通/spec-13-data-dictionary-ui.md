<!-- devtool:file-region id="document-header" -->
# 13 データ辞書（総覧）

> 全項目（列）の定義と、T07 列挙型の位置づけを総覧する。項目の定義は「データ項目 ID（DI-xxx）」で一意に識別する。
> テーブル定義書（`spec-06-db-physical-ui.md` / `.yaml`）は列を物理名で定義し、DI-xxx は持たない。両者は「テーブル名＋物理名」で対応づける（`spec-data-dictionary-mapping-ui.md` も同じ DI-xxx を使う）。
> **本書は項目そのものだけを定義する**。項目がテーブルをどう構成するか（PK/FK/インデックス/制約）は `spec-06-db-physical-ui.md` を正とする。

（文書ヘッダは `spec-00-doc-standard-ui.md` を参照）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.common.13-data-dictionary-ui.s01" -->
## 1. 目的と関連文書

| 文書 | パス | 本書との関係 |
|------|------|------|
| データ辞書規範 | [`spec-data-dictionary-rule.md`](../../開発規約/spec-data-dictionary-rule.md) | 基礎型（T01〜T06）・列定義規則・共通項目の層間対照。規則の唯一の出典 |
| プロジェクトデータ辞書 | [`spec-data-dictionary-project-temp.md`](../../開発規約/spec-data-dictionary-project-temp.md) | **T07 列挙型の値定義**・Principle 3 ホワイトリストの唯一の出典（列定義は本書 §4 を正とする） |
| 命名マッピング | [`spec-data-dictionary-mapping-ui.md`](../データベース/spec-data-dictionary-mapping-ui.md) | 論理名 → DB 物理名 → Backend/API 名 → 型の対応表 |
| テーブル定義書 | [`spec-06-db-physical-ui.md`](spec-06-db-physical-ui.md) | テーブル構成の正本。列は物理名で本書の DI-xxx と対応する |
| 型選定規約 | [`spec-postgresql-column-types-rule.md`](../../開発規約/spec-postgresql-column-types-rule.md) | PostgreSQL の列型選定の根拠 |

- 本書の「項目語彙表」は `spec-06-db-physical.yaml` の列定義と**矛盾してはならない**。差異が生じた場合は 06 yaml を先に更新し、本書と mapping を追従させる。
- 列挙値（コード値）の具体的な定義は本書に重複記載せず、T07 の定義を参照する（§3）。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.13-data-dictionary-ui.s02" -->
## 2. 記載規約

- **データ項目 ID（DI-xxx）**：項目ごとの一意の識別子。一度割り当てたら再利用しない。
- **命名**：DB は snake_case、Backend/API は camelCase（機械的変換。詳細は命名マッピングを参照）。
- **型**：基礎型（データ辞書規範 第1部）の名称、または PostgreSQL の型名と長さで記す。
- **必須/任意**：業務上の必須を「必須」とする。条件付き必須の項目は DB では NULL 可とし、アプリケーション層で検証する旨を備考に記す。
- **共通列**：全テーブルに必須の共通列は、データ辞書規範 第2部「必ず含める共通列」を正とする。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.13-data-dictionary-ui.s03" -->
## 3. T07 列挙型の列挙指針

列挙・ステータス・区分コードは次の手順で扱う。

1. 値の定義（コード・名称・英語名・`category_code`）は `開発規約/spec-data-dictionary-project-temp.md` の T07 にのみ記載する。
2. DB は INT コードで保存し、列挙型ごとのコードテーブルへの外部キーで値域を制約する（`spec-database-rule.md` を参照）。
3. API 値は UPPER_SNAKE_CASE の文字列とし、DB コード → API 値の対応を T07 側に併記する。
4. 本書では、列挙型を使用する項目の「取値範囲」欄に `T07-{列挙型名}` の形式で参照を記すにとどめる。
5. 列挙値を追加・廃止する場合は、T07 → DB（コードテーブル）→ アプリケーション層 Enum の順で更新する（順序はデータ辞書規範を参照）。

### 3.1 T07 列挙型の一覧

| 列挙型 | category_code | 使用項目（DI-xxx） | 定義の参照先 |
|------|:------:|------|------|
| ロール（user_role） | 1 | DI-039（users.role）、DI-104（approval_rules.approver_role） | プロジェクトデータ辞書 T07 |
| 利用者状態（user_status） | 2 | DI-040（users.status） | プロジェクトデータ辞書 T07 |
| 顧客状態（customer_status） | 3 | DI-052（customers.status） | プロジェクトデータ辞書 T07 |
| 商品状態（product_status） | 4 | DI-070（products.status） | プロジェクトデータ辞書 T07 |
| 税区分（tax_category） | 5 | DI-069（products.tax_category）、DI-071（tax_rates.tax_category）、DI-091（order_items.tax_category） | プロジェクトデータ辞書 T07 |
| 注文状態（order_status） | 6 | DI-077（orders.status）、DI-097（order_status_histories.from_status）、DI-098（order_status_histories.to_status） | プロジェクトデータ辞書 T07 |
| 承認依頼状態（approval_request_status） | 7 | DI-114（approval_requests.status） | プロジェクトデータ辞書 T07 |
| 承認操作（approval_action） | 8 | DI-118（approval_histories.approval_action） | プロジェクトデータ辞書 T07 |
| 添付公開範囲（attachment_visibility） | 9 | DI-126（order_attachments.visibility） | プロジェクトデータ辞書 T07 |
| スキャン状態（scan_status） | 10 | DI-127（order_attachments.scan_status） | プロジェクトデータ辞書 T07 |
| 通知イベント（notification_event） | 11 | DI-129（notification_logs.notification_event） | プロジェクトデータ辞書 T07 |
| 通知状態（notification_status） | 12 | DI-132（notification_logs.status） | プロジェクトデータ辞書 T07 |
| 出力状態（export_status） | 13 | DI-138（export_jobs.status） | プロジェクトデータ辞書 T07 |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.13-data-dictionary-ui.s04" -->
## 4. 項目語彙表

> テーブル単位で記載する。物理定義（PK/FK/インデックス/制約）は `spec-06-db-physical-ui.md` を正とする。

### 4.1 共通管理項目（全テーブル共通）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-001 | 作成日時 | created_at | timestamptz | 必須 | | 既定 CURRENT_TIMESTAMP。 |
| DI-002 | 作成者 | created_by | varchar(10) | 必須 | | 既定 'SYSTEM'。操作者のユーザ ID。システム処理は SYSTEM |
| DI-003 | 更新日時 | updated_at | timestamptz | 必須 | | 既定 CURRENT_TIMESTAMP。トリガーで更新。楽観ロックの照合に使用 |
| DI-004 | 更新者 | updated_by | varchar(10) | 必須 | | 既定 'SYSTEM'。 |
| DI-005 | 削除日時 | deleted_at | timestamptz | 任意 | | 論理削除（NULL=有効）（履歴・コードテーブルは持たない） |

### 4.2 users（ユーザ）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-037 | ユーザID | user_id | varchar(10) | 必須 |  | 業務キー。ロールに応じた prefix（A=ADMIN / E=EMPLOYEE / C=CUSTOMER）+4桁をアプリケーション層で採番。DEFAULT なし |
| DI-038 | 表示名 | user_name | varchar(100) | 必須 |  |  |
| DI-039 | ロール | role | int | 必須 | `T07-ロール` |  |
| DI-040 | 状態 | status | int | 必須 | `T07-利用者状態` |  |
| DI-041 | 顧客ID | customer_id | varchar(10) | 任意 |  | CUSTOMER は必須、ADMIN / EMPLOYEE は NULL（chk_users_customer_id） |

### 4.3 user_contacts（ユーザ連絡先）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-042 | ユーザID | user_id | varchar(10) | 必須 |  | 共有主キー（§4.1-SHARED） |
| DI-043 | メールアドレス | email | varchar(255) | 必須 |  | 全行で一意（論理削除なし） |

### 4.4 system_locks（システムロック）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-044 | ユーザID | user_id | varchar(10) | 必須 |  | 共有主キー（§4.1-SHARED） |
| DI-045 | ロック開始日時 | lock_started_at | timestamptz | 任意 |  | NULL=ロックなし。ロック操作時の現在時刻を設定する（未来時刻は設定しない） |
| DI-046 | ロック理由 | lock_reason | varchar(500) | 任意 |  |  |

### 4.5 customers（顧客）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-047 | 顧客ID | id | varchar(10) | 必須 |  | CS+8桁を採番 |
| DI-048 | 顧客コード | customer_code | varchar(30) | 必須 |  | 未削除行で一意 |
| DI-049 | 顧客名 | customer_name | varchar(200) | 必須 |  |  |
| DI-050 | メールアドレス | email | varchar(255) | 任意 |  |  |
| DI-051 | 電話番号 | phone_number | varchar(50) | 任意 |  |  |
| DI-052 | 状態 | status | int | 必須 | `T07-顧客状態` |  |

### 4.6 customer_contacts（顧客連絡先）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-053 | 顧客連絡先ID | id | varchar(10) | 必須 |  | CC+8桁を採番 |
| DI-054 | 顧客ID | customer_id | varchar(10) | 必須 |  |  |
| DI-055 | 連絡先名 | contact_name | varchar(100) | 必須 |  |  |
| DI-056 | メールアドレス | email | varchar(255) | 必須 |  |  |
| DI-057 | 通知先フラグ | is_notification_recipient | boolean | 必須 |  |  |
| DI-058 | 有効フラグ | is_active | boolean | 必須 |  |  |

### 4.7 customer_assignments（担当割当）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-059 | 担当割当ID | id | varchar(10) | 必須 |  | AS+8桁を採番 |
| DI-060 | 顧客ID | customer_id | varchar(10) | 必須 |  |  |
| DI-061 | ユーザID | user_id | varchar(10) | 必須 |  | 担当 EMPLOYEE |
| DI-062 | 有効フラグ | is_active | boolean | 必須 |  | 解除は FALSE にする（論理削除しない）。同じ顧客・社員を再割当するときは同じ行を TRUE に戻し、割当日を当日にする |
| DI-063 | 割当日 | assigned_date | date | 必須 |  |  |

### 4.8 products（商品）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-064 | 商品ID | id | varchar(10) | 必須 |  | PR+8桁を採番 |
| DI-065 | 商品コード | product_code | varchar(50) | 必須 |  | 未削除行で一意 |
| DI-066 | 商品名 | product_name | varchar(200) | 必須 |  |  |
| DI-067 | 商品カテゴリ | product_category | varchar(100) | 任意 |  |  |
| DI-068 | 標準単価 | standard_unit_price | numeric(15,2) | 必須 |  | 0 以上（chk_products_std_unit_price） |
| DI-069 | 税区分 | tax_category | int | 必須 | `T07-税区分` |  |
| DI-070 | 状態 | status | int | 必須 | `T07-商品状態` |  |

### 4.9 tax_rates（税率）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-071 | 税区分 | tax_category | int | 必須 | `T07-税区分` | 自然キー主キー |
| DI-072 | 税率 | tax_rate | numeric(5,2) | 必須 |  | 0.00〜100.00（chk_tax_rates_tax_rate） |

### 4.10 orders（注文）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-073 | 注文ID（注文番号） | id | varchar(10) | 必須 |  | OD+8桁を採番 |
| DI-074 | 顧客ID | customer_id | varchar(10) | 必須 |  |  |
| DI-075 | 担当者ユーザID | sales_user_id | varchar(10) | 必須 |  | 注文顧客の担当 EMPLOYEE（BR-ORD-008） |
| DI-076 | 注文日 | order_date | date | 必須 |  |  |
| DI-077 | 状態 | status | int | 必須 | `T07-注文状態` |  |
| DI-078 | 小計 | subtotal_amount | numeric(15,2) | 必須 |  | サーバー再計算値 |
| DI-079 | 税額 | tax_amount | numeric(15,2) | 必須 |  | サーバー再計算値 |
| DI-080 | 合計 | total_amount | numeric(15,2) | 必須 |  | サーバー再計算値 |
| DI-081 | 訂正元注文ID | revised_from_order_id | varchar(10) | 任意 |  | 取消済み（CANCELLED）・同一顧客の元注文。新規作成時のみ設定し、訂正は 1 回（BR-ORD-009） |
| DI-082 | 取消し理由 | cancel_reason | varchar(500) | 任意 |  |  |

### 4.11 order_items（注文明細）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-083 | 注文明細ID | id | varchar(10) | 必須 |  | OI+8桁を採番 |
| DI-084 | 注文ID | order_id | varchar(10) | 必須 |  |  |
| DI-085 | 行番号 | line_number | numeric | 必須 |  |  |
| DI-086 | 商品ID | product_id | varchar(10) | 必須 |  |  |
| DI-087 | 商品コード（複写） | product_code | varchar(50) | 必須 |  |  |
| DI-088 | 商品名（複写） | product_name | varchar(200) | 必須 |  |  |
| DI-089 | 数量 | quantity | numeric(15,3) | 必須 |  | 0 より大きい（chk_order_items_quantity） |
| DI-090 | 単価 | unit_price | numeric(15,2) | 必須 |  | 商品の standard_unit_price の複写（保存・申請時。BR-PRD-002）。0 以上 |
| DI-091 | 税区分（複写） | tax_category | int | 必須 | `T07-税区分` |  |
| DI-092 | 税率（複写） | tax_rate | numeric(5,2) | 必須 |  |  |
| DI-093 | 明細金額 | line_amount | numeric(15,2) | 必須 |  | quantity × unit_price |
| DI-094 | 明細税額 | tax_amount | numeric(15,2) | 必須 |  | HALF_UP(line_amount × tax_rate / 100) |

### 4.12 order_status_histories（注文状態履歴）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-095 | 注文状態履歴ID | id | varchar(10) | 必須 |  | OH+8桁を採番 |
| DI-096 | 注文ID | order_id | varchar(10) | 必須 |  |  |
| DI-097 | 変更前状態 | from_status | int | 任意 | `T07-注文状態` | 新規作成時は NULL |
| DI-098 | 変更後状態 | to_status | int | 必須 | `T07-注文状態` |  |
| DI-099 | 理由 | reason | varchar(500) | 任意 |  | 取消し・差戻しの理由 |

### 4.13 approval_rules（承認ルール）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-100 | 承認ルールID | id | varchar(10) | 必須 |  | AR+8桁を採番 |
| DI-101 | 金額下限 | min_amount | numeric(15,2) | 必須 |  |  |
| DI-102 | 金額上限 | max_amount | numeric(15,2) | 任意 |  | NULL=上限なし |
| DI-103 | 承認段階 | approval_step | numeric | 必須 |  |  |
| DI-104 | 承認者ロール | approver_role | int | 必須 | `T07-ロール` | 初版は ADMIN 固定 |
| DI-106 | 有効フラグ | is_active | boolean | 必須 |  |  |
| DI-107 | 適用開始日 | effective_from_date | date | 必須 |  |  |
| DI-108 | 適用終了日 | effective_to_date | date | 任意 |  |  |

> DI-105（承認ルールの承認者ユーザID）は欠番。承認者は特定しない（BR-APR-002）。

### 4.14 approval_requests（承認依頼）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-109 | 承認依頼ID | id | varchar(10) | 必須 |  | AQ+8桁を採番 |
| DI-110 | 注文ID | order_id | varchar(10) | 必須 |  |  |
| DI-111 | 承認段階 | approval_step | numeric | 必須 |  |  |
| DI-112 | 承認者ユーザID | approver_user_id | varchar(10) | 任意 |  | 実際に承認・差戻しした ADMIN。申請時は NULL。申請者（created_by）とは異なる |
| DI-113 | ルールスナップショット | rule_snapshot | jsonb | 必須 |  | 申請時点の approval_rules の複写。申請者は created_by |
| DI-114 | 状態 | status | int | 必須 | `T07-承認依頼状態` |  |
| DI-115 | 処理日時 | acted_at | timestamptz | 任意 |  |  |

### 4.15 approval_histories（承認履歴）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-116 | 承認履歴ID | id | varchar(10) | 必須 |  | AH+8桁を採番 |
| DI-117 | 承認依頼ID | approval_request_id | varchar(10) | 必須 |  |  |
| DI-118 | 操作 | approval_action | int | 必須 | `T07-承認操作` |  |
| DI-119 | 理由 | reason | varchar(500) | 任意 |  | 差戻し時は必須（1〜500 文字） |

### 4.16 order_attachments（注文添付）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-120 | 添付ID | id | varchar(10) | 必須 |  | AT+8桁を採番 |
| DI-121 | 注文ID | order_id | varchar(10) | 必須 |  |  |
| DI-122 | 元ファイル名 | original_file_name | varchar(255) | 必須 |  |  |
| DI-123 | 保存パス | storage_path | varchar(512) | 必須 |  | オブジェクトストレージのキー。利用者へ返さない |
| DI-124 | コンテンツタイプ | content_type | varchar(100) | 必須 |  |  |
| DI-125 | ファイルサイズ | file_size | numeric | 必須 |  | バイト。1〜10485760（chk_ord_atts_file_size） |
| DI-126 | 公開範囲 | visibility | int | 必須 | `T07-添付公開範囲` |  |
| DI-127 | スキャン状態 | scan_status | int | 必須 | `T07-スキャン状態` |  |
| DI-154 | ファイル削除日時 | storage_purged_at | timestamptz | 任意 |  | ストレージ上のファイルを削除した日時（BAT-04）。NULL=未削除 |

### 4.17 notification_logs（通知ログ）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-128 | 通知ID | id | varchar(10) | 必須 |  | NL+8桁を採番 |
| DI-129 | 通知イベント | notification_event | int | 必須 | `T07-通知イベント` |  |
| DI-130 | 注文ID | order_id | varchar(10) | 任意 |  |  |
| DI-131 | 宛先メール | recipient_email | varchar(255) | 必須 |  |  |
| DI-132 | 状態 | status | int | 必須 | `T07-通知状態` |  |
| DI-153 | 出力ジョブID | export_job_id | varchar(10) | 任意 |  | 出力完了通知（EXPORT_COMPLETED）の対象ジョブ。それ以外は NULL |
| DI-133 | 試行回数 | attempt_count | numeric | 必須 |  | 送信を試みた回数（初回を含む。最大 4 回＝初回＋再試行 3 回。手動再送で 0 に戻す。BR-NTF-002） |
| DI-152 | 次回送信日時 | next_attempt_at | timestamptz | 任意 |  | 次回送信予定日時。QUEUED のとき送信ワーカーが `next_attempt_at <= 現在` の行を処理する（BR-NTF-002） |
| DI-134 | エラー内容 | error_message | text | 任意 |  |  |
| DI-135 | 送信日時 | sent_at | timestamptz | 任意 |  |  |

### 4.18 export_jobs（CSV 出力ジョブ）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-136 | 出力ジョブID | id | varchar(10) | 必須 |  | EJ+8桁を採番 |
| DI-137 | 抽出条件 | filter_condition | jsonb | 必須 |  |  |
| DI-138 | 状態 | status | int | 必須 | `T07-出力状態` |  |
| DI-139 | 保存パス | storage_path | varchar(512) | 任意 |  |  |
| DI-140 | 件数 | record_count | numeric | 必須 |  |  |
| DI-141 | 有効期限 | expires_at | timestamptz | 任意 |  | 完了から 24 時間。経過後は BAT-03 が EXPIRED にする |
| DI-142 | エラー内容 | error_message | text | 任意 |  |  |
| DI-143 | 完了日時 | completed_at | timestamptz | 任意 |  |  |
| DI-155 | ファイル削除日時 | storage_purged_at | timestamptz | 任意 |  | ストレージ上のファイルを削除した日時（BAT-03）。NULL=未削除 |

### 4.19 audit_logs（監査ログ）

| データ項目 ID | 論理名 | 物理名 | 型(長さ) | 必須 | 取値範囲 | 備考 |
|------|------|------|------|:---:|------|------|
| DI-144 | 監査ログID | id | varchar(10) | 必須 |  | AL+8桁を採番 |
| DI-145 | 操作者ユーザID | actor_user_id | varchar(10) | 任意 |  | 未認証の操作は NULL |
| DI-146 | 操作 | audit_action | varchar(100) | 必須 |  |  |
| DI-147 | 対象種別 | target_type | varchar(100) | 必須 |  |  |
| DI-148 | 対象ID | target_id | varchar(100) | 任意 |  |  |
| DI-149 | トレースID | trace_id | varchar(100) | 任意 |  |  |
| DI-150 | IP アドレス | ip_address | varchar(45) | 任意 |  |  |
| DI-151 | 監査詳細 | audit_detail | jsonb | 任意 |  | メール本文・ファイル本体・Magic Link token を含めない |

### 4.20 列挙コードテーブル

`*_codes` の各列（`{enum}_code` / `{enum}_name`）は T07 の値をそのまま保持する。項目 ID は採番しない。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.13-data-dictionary-ui.s05" -->
## 5. 未決事項

| # | 事項 | 状態 | 備考 |
|---|------|------|------|
| — | なし（DB 関連の未決は `spec-06-db-physical-ui.md` §8 を参照） | — | — |
<!-- devtool:endblock -->
