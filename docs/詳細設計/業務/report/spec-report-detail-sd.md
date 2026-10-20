# 詳細設計 — 帳票・集計

> 業務ルールの番号・内容は `基本設計/共通/spec-08-business-rule-list.md` を正とし、本書は実装手順を定める。

## 1. API

| operationId | API | 権限 |
|---|---|---|
| getOrderSummary | GET /api/v1/reports/order-summary | ADMIN / EMPLOYEE |
| getCustomerSummary | GET /api/v1/reports/customer-summary | ADMIN / EMPLOYEE |
| getProductSummary | GET /api/v1/reports/product-summary | ADMIN / EMPLOYEE |
| createExportJob | POST /api/v1/export-jobs | ADMIN / EMPLOYEE |
| pageExportJobs | GET /api/v1/export-jobs | ADMIN は全員分（requestedBy で絞り込み可）、EMPLOYEE は自分の依頼分のみ（BR-RPT-003） |
| getExportJob | GET /api/v1/export-jobs/{exportJobId} | ADMIN、又は依頼者本人（他人のジョブは 404 `E0005`） |
| downloadExportJob | GET /api/v1/export-jobs/{exportJobId}/download | ADMIN、又は依頼者本人（他人のジョブは 404 `E0005`） |
| downloadOrderPdf | GET /api/v1/orders/{orderId}/pdf | 注文の参照権限 |

CUSTOMER は自社注文の PDF だけを使用でき、集計 API と CSV API は使用できない（403 `E0002`）。

## 2. PDF

- A4、日本語、注文番号、顧客、注文日、明細、明細税額、小計、税額、合計、状態、承認日時を表示する。
- PDF は APPROVED 注文だけを対象とし（それ以外は 409 `E0006`。BR-RPT-001）、生成前に注文の参照権限を確認する（範囲外は 404 `E0005`）。顧客には自社注文だけを許可する。監査は `ORDER_PDF_DOWNLOAD`。
- ファイル名は `order_<注文番号>.pdf`。API が認可してストリーム配信し、保存先を公開しない。

## 3. CSV 出力

すべての CSV 出力を export_jobs に記録する（BR-RPT-002）。

1. createExportJob は抽出条件（期間、顧客、状態）を受け、呼出者のデータスコープをサーバー側で合成して `filter_condition` に保存する。
2. 対象件数が 10,000 件以下なら依頼時に UTF-8 BOM CSV を作成し、READY の export_job（completed_at・expires_at を設定）を返す（`I0028`）。この場合は完了通知を送らない。
3. 10,001 件以上なら QUEUED の export_job を返し（`I0027`）、BAT-02 が RUNNING → READY 又は FAILED に更新する（`基本設計/共通/spec-15-batch-design-ui.md`）。
4. ファイルはオブジェクトストレージに保管し、expires_at は完了から 24 時間とする。期限後は BAT-03 が EXPIRED にしてファイルを削除する（storage_purged_at）。期限後のダウンロードは、BAT-03 の実行前でも expires_at で判定して 409 `E0006` とする。
5. export_jobs と監査（`EXPORT_CREATE` / `EXPORT_DOWNLOAD`）を記録する。非同期生成の完了時だけ完了通知（EXPORT_COMPLETED、宛先は依頼者、notification_logs.export_job_id にジョブ ID）を投入する。
6. CSV は注文単位（1 行＝1 注文）。列は注文番号、注文日、顧客コード、顧客名、状態、担当者名、小計、税額、合計。ファイル名は `orders_YYYYMMDD.csv`。statuses 未指定は APPROVED のみ。

## 4. 集計整合性

集計条件、状態条件、対象件数、生成時刻、依頼者を出力メタデータへ含める。状態条件は `statuses` で明示し、既定は APPROVED のみとする。

## 5. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-RPT-01 | CUSTOMER が CSV を依頼 | 403 `E0002` |
| UT-RPT-02 | EMPLOYEE が担当外顧客を条件に指定 | 対象に含まれない |
| UT-RPT-03 | 10,000 件 | 同期生成、READY |
| UT-RPT-04 | 10,001 件 | QUEUED、ワーカーで生成 |
| UT-RPT-05 | 期限切れのジョブをダウンロード | 409 `E0006` |
| UT-RPT-06 | CUSTOMER の他社 PDF | 404 `E0005` |
| UT-RPT-07 | DRAFT 注文の PDF | 409 `E0006` |
| UT-RPT-08 | 10,000 件以下の出力 | export_jobs に READY で 1 行記録される |
| UT-RPT-09 | EMPLOYEE が他人のジョブを取得 | 404 `E0005` |
| UT-RPT-10 | ADMIN が他人のジョブを一覧・ダウンロード | 取得できる |

## 6. 適用する業務ルール

BR-RPT-001〜BR-RPT-003、BR-AUTH-003、BR-AUTH-004、BR-AUTH-006、BR-NTF-003、BR-AUD-001（内容は `spec-08-business-rule-list.md`）。
