# M-CUS 顧客管理 詳細設計書

## 1. 対象と前提

| 項目 | 内容 |
| --- | --- |
| 対象モジュール | M-CUS 顧客管理 |
| 根拠 | `基本設計/業務設計/customer/spec-customer-business-ui.md` |
| 対象画面 | SCR-CUS-01〜03 |
| 関連データ | `customers`、`customer_contacts`、`employee_customer_assignments` |
| 版数・作成日 | 0.1（初稿）・2026-10-14 |
| ステータス | レビュー待ち |

顧客マスタの登録・編集・有効化は ADMIN だけが行う。EMPLOYEE は担当割当済み顧客だけ、CUSTOMER は自社顧客だけを参照できる。

## 2. バックエンド詳細設計

| operationId | API | 権限・対象範囲 | 処理概要 |
| --- | --- | --- | --- |
| listCustomers | `GET /api/customers` | ADMIN は全件、EMPLOYEE は担当顧客 | コード、名称、有効状態でページング検索する。 |
| createCustomer | `POST /api/customers` | ADMIN | 顧客、連絡先、担当社員を登録する。 |
| getCustomer | `GET /api/customers/{customerId}` | ADMIN、担当社員、当該 CUSTOMER | 顧客、連絡先、担当社員、権限内の関連注文を返す。 |
| updateCustomer | `PATCH /api/customers/{customerId}` | ADMIN | 顧客基本情報、連絡先、担当社員を更新する。 |
| updateCustomerStatus | `PATCH /api/customers/{customerId}/status` | ADMIN | 有効・無効を変更する。 |

### 処理・整合性

- 顧客コードは大文字英数字とハイフンを許容し、未削除レコード内で一意とする。登録後は変更不可。
- 顧客と担当社員の更新は、顧客情報と `employee_customer_assignments` を同一トランザクションで置換する。
- 無効顧客は新規注文の顧客候補から除外する。既存注文・添付・履歴は参照可能とする。
- 顧客の住所、連絡先、担当社員、有効状態の変更を監査ログへ記録する。

## 3. 画面詳細設計

### SCR-CUS-01 顧客一覧

| 区分 | 設計 |
| --- | --- |
| 検索条件 | 顧客コード、顧客名称、有効状態。 |
| 表示列 | 顧客コード、顧客名称、メール、電話、有効状態、担当社員、更新日時。 |
| 操作 | 検索、条件クリア、ページ移動、詳細遷移。ADMIN だけ「新規登録」を表示する。 |
| 範囲 | EMPLOYEE の API 結果は担当割当済み顧客だけ。CUSTOMER は本画面を利用しない。 |

### SCR-CUS-02 顧客詳細

基本情報、連絡先、担当社員、関連注文の要約を表示する。CUSTOMER には担当社員情報と内部メモを表示しない。EMPLOYEE は担当割当が失われた場合、次回取得から 403 または 404 とする。

### SCR-CUS-03 顧客登録・編集

| 入力項目 | 必須 | ルール |
| --- | :---: | --- |
| 顧客コード | ○（新規のみ） | 1〜30 文字、一意、登録後変更不可。 |
| 顧客名称 | ○ | 1〜200 文字。 |
| 住所 | 任意 | 500 文字以内。 |
| 電話番号 | 任意 | 30 文字以内。 |
| メール | 任意 | メール形式、255 文字以内。 |
| 連絡先 | 任意 | 氏名は必須。メールは入力時に形式検証する。複数行可。 |
| 担当社員 | 任意 | 有効な EMPLOYEE だけを複数選択できる。 |
| 有効状態 | ○ | 無効化時は将来の注文登録ができないことを確認表示する。 |

保存成功後は SCR-CUS-02 へ遷移する。CUSTOMER の画面には登録・編集ボタンを表示しない。

## 4. フロントエンド構成と例外表示

`CustomerListPage`、`CustomerDetailPage`、`CustomerFormPage`、`CustomerContactEditor`、`EmployeeAssignmentSelector` を構成する。409（顧客コード重複）はコード欄、400 は該当する入力欄、403/404 は権限不足画面に対応付ける。

## 5. 単体テスト用例

| UT-ID | 対象 | 前提・入力 | 期待結果 |
| --- | --- | --- | --- |
| UT-CUS-001 | createCustomer | 既存と同じ顧客コード | 409。顧客・担当割当は作成されない。 |
| UT-CUS-002 | listCustomers | EMPLOYEE に顧客 A だけ割当 | 顧客 A だけを返す。 |
| UT-CUS-003 | getCustomer | CUSTOMER が他社 ID を指定 | 403 または 404。情報を返さない。 |
| UT-CUS-004 | updateCustomer | 担当社員と連絡先を同時変更 | すべて更新、または全て未更新となる。 |
| UT-CUS-005 | updateCustomerStatus | 無効顧客を指定 | 後続の新規注文候補に含まれない。 |


## 6. 業務ルール

- **BR-CUS-001 顧客データ範囲**: CUSTOMER は自社に紐付く顧客、注文、公開添付、帳票だけを参照できる。
- **BR-CUS-002 担当顧客制限**: EMPLOYEE は有効な担当割当を持つ顧客だけを検索し、注文作成時の顧客として選択できる。
- **BR-CUS-003 利用停止顧客**: 利用停止済み顧客には新規注文を登録できないが、既存履歴の参照は保持する。
