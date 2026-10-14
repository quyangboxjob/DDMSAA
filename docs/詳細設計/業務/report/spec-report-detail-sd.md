# M-RPT 検索・帳票・報表 詳細設計書

## 1. 対象と前提

| 項目 | 内容 |
| --- | --- |
| 対象モジュール | M-RPT 検索・帳票・報表 |
| 根拠 | `基本設計/業務設計/report/spec-report-business-ui.md` |
| 対象画面 | SCR-RPT-01〜04 |
| 関連データ | `orders`、`order_items`、`products`、`customers`、`audit_logs` |
| 版数・作成日 | 0.1（初稿）・2026-10-14 |
| ステータス | レビュー待ち |

ADMIN は全注文を検索・集計・CSV 出力できる。EMPLOYEE は自分が `sales_user_id` の注文だけ、CUSTOMER は自社注文の検索・帳票出力だけを行える。CUSTOMER に集計・CSV は提供しない。

## 2. バックエンド詳細設計

| operationId | API | 権限・対象範囲 | 処理概要 |
| --- | --- | --- | --- |
| listOrders | `GET /api/orders` | 認証済み、行単位認可 | 注文検索の共通 API。 |
| getOrderSummary | `GET /api/reports/orders/summary` | ADMIN、EMPLOYEE | 注文件数、金額、承認済み件数を集計する。 |
| getCustomerReport | `GET /api/reports/customers` | ADMIN、EMPLOYEE | 顧客別の件数・金額を集計する。 |
| getProductReport | `GET /api/reports/products` | ADMIN、EMPLOYEE | 商品別の数量・金額を集計する。 |
| exportOrdersCsv | `GET /api/orders/export.csv` | ADMIN、EMPLOYEE | 権限範囲の検索結果を CSV として出力する。 |
| downloadOrderDocument | `GET /api/orders/{orderId}/document` | 承認済み注文の閲覧権限保有者 | 注文書または注文確認書を出力する。 |

### 2.1 検索・出力ルール

- 共通検索条件は注文番号、顧客、注文日範囲、状態、担当社員、金額範囲。`from` が `to` より後の場合は 400 とする。
- API の WHERE 句に必ず行単位条件を追加する。ADMIN は制限なし、EMPLOYEE は `sales_user_id = currentUserId`、CUSTOMER は `customer_id = currentCustomerId` とする。
- CSV は画面に表示可能な検索結果だけを出力する。出力操作には実行者、検索条件、件数を監査ログへ記録する。
- 注文書出力は `APPROVED` だけを対象とし、画面上の金額ではなく保存済み注文・明細のスナップショットを使用する。
- PDF レイアウト、CSV 文字コード、最大件数、非同期化の要否は帳票運用設計で確定する。未確定のため、初版での出力形式は実装前に選定する。

## 3. 画面詳細設計

### SCR-RPT-01 注文検索

| 区分 | 設計 |
| --- | --- |
| 検索条件 | 注文番号、顧客、注文日範囲、状態、担当社員、金額範囲。CUSTOMER は顧客・担当社員を操作できない。 |
| 表示列 | 注文番号、顧客、担当社員、注文日、状態、合計金額、更新日時。 |
| 操作 | 検索、条件クリア、詳細遷移。ADMIN/EMPLOYEE は CSV と集計への導線を表示する。 |

### SCR-RPT-02 受注集計

期間、顧客、商品を条件に、受注件数、合計金額、承認済み件数を表示する。EMPLOYEE は自分の注文だけに自動制限する。集計根拠となる検索条件を画面上部に表示する。

### SCR-RPT-03 CSV 出力

検索条件と対象件数を確認してから `exportOrdersCsv` を実行する。CUSTOMER には操作を表示しない。出力対象が 0 件の場合は空ファイルを作らず、画面に 0 件メッセージを表示する。

### SCR-RPT-04 注文書出力

承認済み注文だけに「帳票出力」を表示する。利用者が選択した注文番号、顧客、明細、金額、承認情報を元に出力する。CUSTOMER への出力は自社注文に限る。

## 4. フロントエンド構成と例外表示

`OrderSearchPage`、`OrderSummaryPage`、`ReportFilter`、`CsvExportButton`、`OrderDocumentButton` を配置する。出力処理中はボタンを無効化し、同一条件の二重出力を防止する。403/404 は対象が存在しない、または権限がない旨の共通表示とする。

## 5. 単体テスト用例

| UT-ID | 対象 | 前提・入力 | 期待結果 |
| --- | --- | --- | --- |
| UT-RPT-001 | listOrders | CUSTOMER が他社注文番号を指定 | 0 件または 404。情報を返さない。 |
| UT-RPT-002 | getOrderSummary | EMPLOYEE の集計 | 自分の注文だけを含む。 |
| UT-RPT-003 | exportOrdersCsv | EMPLOYEE が検索条件を指定 | CSV は自分の注文だけ。監査ログを追加する。 |
| UT-RPT-004 | exportOrdersCsv | CUSTOMER が実行 | 403。ファイルを返さない。 |
| UT-RPT-005 | downloadOrderDocument | DRAFT 注文を指定 | 409。帳票を出力しない。 |
