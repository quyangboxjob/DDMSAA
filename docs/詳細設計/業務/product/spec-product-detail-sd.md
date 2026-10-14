# 詳細設計 — 商品

## 1. データ

`products` は product_id、product_code、product_name、category、standard_unit_price、tax_category_code、status、created_at、updated_at、deleted_at を持つ。`tax_categories` の初期値は STANDARD=10%、REDUCED=8%、EXEMPT=0% である。

## 2. API

| operationId | API | 権限 |
|---|---|---|
| listProducts | GET /api/products | ADMIN/EMPLOYEE（有効商品のみ） |
| createProduct | POST /api/products | ADMIN |
| getProduct | GET /api/products/{productId} | ADMIN/EMPLOYEE |
| updateProduct | PATCH /api/products/{productId} | ADMIN |
| updateProductStatus | PATCH /api/products/{productId}/status | ADMIN |

## 3. 処理・検証

- productCode は 1～50 文字で一意、productName は 1～200 文字。
- standardUnitPrice は 0 以上、taxCategoryCode は有効な税区分。
- 無効商品は新規注文の明細に追加できない。
- 注文作成時に商品コード、名称、単価、税区分、税率を `order_items` へコピーする。後の変更で過去の注文を更新しない。

## 4. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-PRD-01 | コード重複 | 409 |
| UT-PRD-02 | 負の単価 | 422 |
| UT-PRD-03 | EMPLOYEE の商品更新 | 403 |
| UT-PRD-04 | 無効商品で注文作成 | 422 |
| UT-PRD-05 | 商品改定後の既存注文 | 明細スナップショット不変 |

- **BR-PRD-001 商品管理者限定**: 商品の作成、更新、有効状態変更は ADMIN だけが実行する。
- **BR-PRD-002 注文明細スナップショット**: 商品の変更後も既存注文のコード、名称、単価、税区分、税率を変えない。
