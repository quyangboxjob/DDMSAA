# 詳細設計 — 帳票・集計

## 1. API

| operationId | API | 権限 |
|---|---|---|
| getOrderSummary | GET /api/reports/orders/summary | ADMIN/EMPLOYEE |
| getCustomerReport | GET /api/reports/customers | ADMIN/EMPLOYEE |
| getProductReport | GET /api/reports/products | ADMIN/EMPLOYEE |
| createOrderExport | POST /api/exports/orders | ADMIN/EMPLOYEE |
| getExportJob | GET /api/exports/{exportJobId} | 作成者又は ADMIN |
| downloadExport | GET /api/exports/{exportJobId}/download | 作成者又は ADMIN |
| downloadOrderDocument | GET /api/orders/{orderId}/document | 注文の参照権限 |

CUSTOMER は自社注文の document API だけを使用でき、集計 API と CSV API は使用できない。

## 2. PDF

- A4、日本語、注文番号、顧客、注文日、明細、明細税額、小計、税額、合計、状態、承認日時を表示する。
- PDF は APPROVED 注文だけを対象とし、生成前に注文の参照権限を確認する。顧客には自社注文だけを許可する。
- PDF の保存先キーは利用者に公開せず、API が認可してストリーム配信する。

## 3. CSV 出力

1. createOrderExport は filter（期間、顧客、商品、状態）を受け、呼出者のデータスコープをサーバー側で合成する。
2. 対象件数が 10,000 件以下なら同期で UTF-8 BOM CSV を作成し、READY の export_job を返す。
3. 10,001 件以上なら QUEUED の export_job を返し、Worker が RUNNING → READY 又は FAILED に更新する。
4. file は Object Storage に保管し、expires_at は完了から 24 時間とする。期限後は EXPIRED とし、ダウンロードを 404 とする。
5. export_jobs、監査、完了/失敗通知を記録する。

## 4. 集計整合性

集計条件、状態条件、対象件数、生成時刻、依頼者を出力メタデータへ含める。注文取消しを含めるかは §includeCancelled§ の既定 false で明示し、承認済みだけを対象とする場合は §status=APPROVED§ を明示する。

## 5. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-RPT-01 | CUSTOMER が CSV を作成 | 403 |
| UT-RPT-02 | EMPLOYEE が担当外顧客を filter 指定 | 対象に含まれない |
| UT-RPT-03 | 10,000 件 | 同期 READY |
| UT-RPT-04 | 10,001 件 | QUEUED、Worker で生成 |
| UT-RPT-05 | 期限切れ URL | 404 |
| UT-RPT-06 | CUSTOMER の他社 PDF | 404 |

- **BR-RPT-001 帳票データ範囲**: 集計、PDF、CSV は常に利用者のデータスコープをサーバー側で適用する。
- **BR-RPT-002 顧客の出力制限**: CUSTOMER は自社注文 PDF だけを取得でき、集計と CSV を取得できない。
- **BR-RPT-003 CSV 非同期化**: 10,000 件超は export_jobs で非同期生成し、成果物は 24 時間で失効する。
