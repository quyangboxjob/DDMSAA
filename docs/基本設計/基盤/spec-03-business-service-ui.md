# 基盤基本設計 — 業務サービス

## 1. サービス責務

| モジュール | サービスが保証すること |
|---|---|
| User/Customer/Product | 管理者だけがマスタを変更し、一意性・有効状態を検証する |
| Order | 担当割当、注文状態、明細、税額、楽観ロックを検証する |
| Approval | 申請時の承認経路をスナップショット化し、管理者だけが操作する |
| Attachment | 所有者、拡張子/MIME/サイズ、スキャン状態、公開範囲を検証する |
| Report | データ範囲を強制し、同期/非同期出力を選択する |

## 2. データ範囲の強制

`ADMIN` は全件、`EMPLOYEE` は `employee_customer_assignments` で有効な顧客、`CUSTOMER` は自身の `customer_id` のみを対象にする。検索条件を画面から信頼せず、`UserContext` からスコープを作り、Repository 呼出しに必ず渡す。

```java
// 擬似コード: 画面の customerId をそのまま信用しない
scope = accessScope.resolve(userContext);
order = orderRepository.findByIdAndScope(orderId, scope)
    .orElseThrow(NotFoundException::new);
```

## 3. トランザクション

- 注文と明細、状態履歴、承認依頼、監査ログは同一トランザクションで保存する。
- メール、スキャン、CSV/PDF 生成はコミット後にジョブ化する。
- `version` を使い、古い画面からの更新は `409` とする。
- 承認済み注文は更新不可。取消し、必要なら元注文を `revised_from_order_id` に持つ新規作成で訂正する。
