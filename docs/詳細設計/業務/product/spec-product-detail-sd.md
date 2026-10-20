# 詳細設計 — 商品

> 業務ルールの番号・内容は `基本設計/共通/spec-08-business-rule-list.md` を正とし、本書は実装手順を定める。

## 1. データ

`products` は id（PR+8 桁）、product_code、product_name、product_category、standard_unit_price、tax_category（T07-5）、status（T07-4）と共通列を持つ。`tax_rates` は税区分ごとの税率（STANDARD=10.00、REDUCED=8.00、EXEMPT=0.00）を自然キーで保持する。

## 2. API

| operationId | API | 権限 |
|---|---|---|
| pageProducts | GET /api/v1/products | ADMIN（status で絞り込み可）/ EMPLOYEE（サーバー側で ACTIVE のみに固定。BR-PRD-003） |
| createProduct | POST /api/v1/products | ADMIN |
| getProduct | GET /api/v1/products/{productId} | ADMIN / EMPLOYEE（EMPLOYEE は ACTIVE のみ。無効商品は 404 `E0005`） |
| updateProduct | PUT /api/v1/products/{productId} | ADMIN |
| activateProduct / deactivateProduct | POST /api/v1/products/{productId}/activate・/deactivate | ADMIN |
| listTaxRates | GET /api/v1/tax-rates | ADMIN / EMPLOYEE |

## 3. 処理・検証

- productCode は 1～50 文字で未削除行内一意（409 `E0007`）、productName は 1～200 文字。更新時は productCode を変更しない。
- standardUnitPrice は 0 以上（400 `E0008`）、taxCategory は T07-5 の値。
- 無効商品は注文明細に新しく追加できない（409 `E0054`）。既存明細に残る無効商品は保存できるが、申請はできない（BR-PRD-003 / BR-ORD-010）。
- DRAFT / RETURNED の保存・申請のたびに、商品コード、名称、標準単価、税区分、税率（tax_rates）を `order_items` へ複写する。PENDING_APPROVAL 以降は商品を変更しても注文を更新しない（BR-PRD-002）。
- 更新は `updated_at` で楽観ロックし、不一致は 409 `E0051`。監査は `PRODUCT_CREATE` / `PRODUCT_UPDATE` / `PRODUCT_ACTIVATE` / `PRODUCT_DEACTIVATE`。成功 messageId は登録 `I0010`、更新 `I0011`、有効化 `I0034`、無効化 `I0035`。

## 4. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-PRD-01 | コード重複 | 409 `E0007` |
| UT-PRD-02 | 負の単価 | 400 `E0008` |
| UT-PRD-03 | EMPLOYEE の商品更新 | 403 `E0002` |
| UT-PRD-04 | 無効商品で注文作成 | 409 `E0054` |
| UT-PRD-05 | 商品改定後の申請済み注文 | 明細の複写値は不変 |
| UT-PRD-06 | 商品改定後に DRAFT を再保存 | 明細の単価・名称が改定後の値になる |
| UT-PRD-07 | EMPLOYEE が status=INACTIVE で検索 | ACTIVE だけが返る |

## 5. 適用する業務ルール

BR-PRD-001〜BR-PRD-003、BR-ORD-006、BR-AUD-001（内容は `spec-08-business-rule-list.md`）。
