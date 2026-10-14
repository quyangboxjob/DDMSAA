# M-ORD 注文管理 詳細設計書

## 1. 対象と前提

| 項目 | 内容 |
| --- | --- |
| 対象モジュール | M-ORD 注文管理 |
| 根拠 | `基本設計/業務設計/order/spec-order-business-ui.md`、`要件定義/requirements.md` |
| 対象画面 | SCR-ORD-01〜04 |
| 関連データ | `orders`、`order_items`、`order_status_histories` |
| 版数・作成日 | 0.1（初稿）・2026-10-14 |
| ステータス | レビュー待ち |

顧客は注文を作成・更新・取消できない。EMPLOYEE は自分が `sales_user_id` である注文だけを処理でき、登録時に担当割当済み顧客だけを選択できる。ADMIN は全注文を処理できる。

## 2. バックエンド詳細設計

| operationId | API | 権限・対象範囲 | 処理概要 |
| --- | --- | --- | --- |
| listOrders | `GET /api/orders` | 認証済み、行単位認可 | 注文番号、顧客、期間、状態、担当社員、金額で検索する。 |
| createOrder | `POST /api/orders` | ADMIN、EMPLOYEE | 下書き注文と 1 件以上の明細を登録する。 |
| getOrder | `GET /api/orders/{orderId}` | 行単位認可 | 注文、明細、状態履歴、権限内添付を返す。 |
| updateOrder | `PATCH /api/orders/{orderId}` | ADMIN、作成 EMPLOYEE | 下書き・差戻し注文を更新する。 |
| submitOrder | `POST /api/orders/{orderId}/submit` | ADMIN、作成 EMPLOYEE | 妥当性を確認して承認依頼へ状態遷移する。 |
| cancelOrder | `POST /api/orders/{orderId}/cancel` | ADMIN、作成 EMPLOYEE | 下書き・差戻し注文を理由付きで取り消す。 |

### 2.1 状態遷移とトランザクション

```text
DRAFT / RETURNED --submitOrder--> PENDING_APPROVAL
PENDING_APPROVAL --承認完了--> APPROVED
PENDING_APPROVAL --差戻し--> RETURNED
DRAFT / RETURNED --cancelOrder--> CANCELLED
```

- `APPROVED` と `CANCELLED` はこのモジュールから直接更新できない。
- 更新・承認依頼・取消は `orders.version` を用いた楽観ロックで競合を検出する。競合時は `409 VERSION_CONFLICT` とし、画面は再読込を促す。
- 注文、明細、状態履歴、監査ログは 1 トランザクションで更新する。承認依頼後のメール送信はトランザクション外の通知キューに登録する。
- 金額は `quantity * unitPrice` の明細合計とする。税計算・端数・値引き・送料は未確定のため、初版では明細に保存した税区分を保持し、最終計算規則が確定するまで自動税額確定を行わない。

### 2.2 入力検証

| 項目 | ルール |
| --- | --- |
| 顧客 | 有効顧客のみ。EMPLOYEE は担当割当済み顧客だけ。 |
| 明細 | 1 件以上。商品は有効商品、数量は 1 以上の整数、単価は 0 以上の整数。 |
| 状態 | 更新・提出・取消の前に許可状態であることを検証する。 |
| 取消理由 | `cancelOrder` 時に必須、1〜500 文字。 |

## 3. 画面詳細設計

### SCR-ORD-01 注文一覧

| 区分 | 設計 |
| --- | --- |
| 検索条件 | 注文番号、顧客、注文日範囲、状態、担当社員、金額範囲。CUSTOMER は顧客・担当社員条件を変更不可。 |
| 表示列 | 注文番号、顧客、担当社員、注文日、状態、合計金額、更新日時。 |
| 操作 | 検索、条件クリア、ページ移動、詳細遷移。ADMIN と EMPLOYEE だけ新規登録を表示する。 |
| 範囲 | API が範囲外レコードを返さない。画面側のフィルタだけで制御しない。 |

### SCR-ORD-02 注文詳細

ヘッダ、明細、金額、状態履歴、承認履歴、添付一覧を表示する。`DRAFT` と `RETURNED` の作成者には編集・承認依頼・取消を表示する。CUSTOMER は自社の注文を参照専用で表示し、内部用添付・社内コメント・操作ボタンを表示しない。

### SCR-ORD-03 注文登録・編集

| 入力項目 | 必須 | ルール |
| --- | :---: | --- |
| 顧客 | ○ | 有効顧客。EMPLOYEE は担当割当済み顧客のみ。 |
| 注文日 | ○ | 当日を初期値とする。将来日の許容可否は業務運用で確定。 |
| 明細 | ○ | 1 件以上。商品、数量、単価、税区分を保持する。 |
| 備考 | 任意 | 500 文字以内。顧客に表示するかは帳票設計で決定する。 |

「下書き保存」は `createOrder` または `updateOrder`、「承認依頼」は保存成功後に `submitOrder` を実行する。送信中は二重クリックを防止する。

### SCR-ORD-04 取消確認

対象の注文番号・顧客・状態を表示し、取消理由入力欄を必須とする。確認後に `cancelOrder` を 1 回だけ実行し、成功時は SCR-ORD-02 へ戻る。

## 4. フロントエンド構成と監査

`OrderListPage`、`OrderDetailPage`、`OrderFormPage`、`OrderItemEditor`、`OrderCancelDialog` を配置する。数量・単価変更時はクライアント側で小計を再計算するが、保存時の金額計算は必ずサーバーを正とする。登録、更新、承認依頼、取消を監査ログに記録する。

## 5. 単体テスト用例

| UT-ID | 対象 | 前提・入力 | 期待結果 |
| --- | --- | --- | --- |
| UT-ORD-001 | createOrder | CUSTOMER が実行 | 403。注文を作成しない。 |
| UT-ORD-002 | createOrder | EMPLOYEE が未担当顧客を指定 | 403。注文を作成しない。 |
| UT-ORD-003 | submitOrder | 明細なし、または無効商品 | 400。状態は DRAFT のまま。 |
| UT-ORD-004 | updateOrder | 古い version で更新 | 409。最新注文を変更しない。 |
| UT-ORD-005 | cancelOrder | APPROVED 注文を指定 | 409。取消履歴を作成しない。 |


## 6. 業務ルール

- **BR-ORD-001 注文作成権限**: 注文の作成、更新、提出、取消は ADMIN または作成 EMPLOYEE だけが実行でき、CUSTOMER は参照専用とする。
- **BR-ORD-002 担当注文制限**: EMPLOYEE は自分が `sales_user_id` である注文だけを処理できる。
- **BR-ORD-003 金額再計算**: 注文金額はサーバーが明細から再計算し、クライアント送信の税額・合計額を正値として保存しない。
- **BR-ORD-004 状態遷移制限**: `DRAFT` と `RETURNED` だけを更新、提出、取消でき、`APPROVED` と `CANCELLED` は直接更新できない。
- **BR-ORD-005 取消通知**: 注文取消時は取消理由を必須とし、取消履歴・監査ログを残して、DEC-NTF-001 で確定した通知先へ通知をキュー登録する。
