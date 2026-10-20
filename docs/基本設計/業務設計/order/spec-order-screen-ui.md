# 画面基本設計 — 注文管理（添付・承認操作を含む）

> 対象画面：SCR-11 注文一覧 / SCR-12 注文登録/編集 / SCR-13 注文詳細（添付パネル・承認操作・取消しダイアログを含む）
> 画面 ID・パス・ロールの正本は `共通/spec-screen-list-ui.md`、共通 UI は `共通/spec-12-screen-common-ui.md`。業務ルールは `spec-order-business-ui.md`（状態・金額）、`../approval/spec-approval-business-ui.md`、`../attachment/spec-attachment-business-ui.md`、権限は `共通/spec-03-auth-ui.md`。

## 1. SCR-11 注文一覧

| 項目 | 内容 |
|---|---|
| パス | `/orders` |
| 利用ロール | ADMIN（全件）、EMPLOYEE（担当顧客）、CUSTOMER（自社） |
| 利用 API | `listOrders` |
| 遷移 | 行選択 → SCR-13 / 新規登録（ADMIN・EMPLOYEE）→ SCR-12 |

```mermaid
flowchart TB
  subgraph P[注文一覧]
    subgraph S[検索条件]
      S1[注文番号]
      S2[顧客 ADMIN・EMPLOYEE のみ]
      S3[状態 複数選択]
      S4[注文日 期間]
      S5[検索 / リセット]
    end
    A[新規登録 ADMIN・EMPLOYEE のみ]
    subgraph T[テーブル]
      TH[注文番号 / 顧客 / 注文日 / 状態 / 合計 / 担当 / 更新日時 / 操作]
    end
    PG[ページング]
  end
  S --> A --> T --> PG
```

| 区分 | 項目 | 内容 |
|---|---|---|
| 検索条件 | 注文番号 | 前方一致 |
| 検索条件 | 顧客 | 選択式（`listCustomers`）。CUSTOMER は非表示（自社固定） |
| 検索条件 | 状態 | DRAFT / PENDING_APPROVAL / RETURNED / APPROVED / CANCELLED の複数選択。表示は日本語名・状態色 |
| 検索条件 | 注文日 | 開始日・終了日 |
| 一覧列 | 注文番号、顧客名、注文日、状態、合計金額（税込。円・桁区切り）、担当者、更新日時 | 既定の並び：更新日時降順 |
| 行操作 | 詳細 | SCR-13 へ |
| 行操作 | 取消し | ADMIN・担当 EMPLOYEE、取消し可能状態のとき SCR-13/DLG-1 を開く |

- 画面からの検索は、データ範囲に関係なく API が範囲を強制する。URL に他社の顧客を指定しても結果は空（または `404`）になる。
- 検索条件は URL クエリに保持し、SCR-13 から戻ったときに復元する。

## 2. SCR-12 注文登録/編集

| 項目 | 内容 |
|---|---|
| パス | `/orders/new`、`/orders/:orderId/edit` |
| 利用ロール | ADMIN、EMPLOYEE（担当顧客のみ選択可）。CUSTOMER は利用不可 |
| 利用 API | `createOrder`、`getOrder`、`updateOrder`、`listCustomers`、`listProducts` |
| 編集可能な状態 | DRAFT、RETURNED のみ（それ以外で開いた場合は SCR-13 へ戻し `E0006`） |

```mermaid
flowchart TB
  subgraph P[注文登録/編集]
    subgraph H[ヘッダ]
      H1[顧客 選択]
      H2[注文日]
      H3[訂正元注文 任意]
    end
    subgraph L[明細テーブル 行追加・削除]
      L1[行 No / 商品 / 数量 / 単価 / 税区分 / 明細金額 / 税額 / 削除]
    end
    subgraph SUM[合計 表示のみ]
      SUM1[小計 / 税額 / 合計]
    end
    BTN[下書き保存 / 保存して申請 / キャンセル]
  end
  H --> L --> SUM --> BTN
```

| 区分 | 項目 | 部品 | 必須 | 制約・備考 |
|---|---|---|:---:|---|
| ヘッダ | 顧客 | 選択 | ○ | 有効な顧客のみ。EMPLOYEE は担当顧客のみ。編集時は変更不可（TBD-O1） |
| ヘッダ | 注文日 | 日付 | ○ | 既定は当日 |
| ヘッダ | 訂正元注文 | 選択/表示 | — | 取消済み注文からの訂正のとき自動設定（`revised_from_order_id`） |
| 明細 | 商品 | 選択 | ○ | 有効商品のみ。選択時に単価・税区分を既定表示 |
| 明細 | 数量 | 数値 | ○ | 0 より大きい。小数 3 桁まで（`quantity`） |
| 明細 | 単価 | 数値 | ○ | 0 以上。標準単価から初期設定、修正可否は TBD-O2 |
| 明細 | 税区分・税率 | 表示 | — | 商品から複写。STANDARD 10% / REDUCED 8% / EXEMPT 0% |
| 明細 | 明細金額・税額 | 表示 | — | 参考表示（数量×単価、HALF_UP）。**サーバーが再計算した値が正**で、保存後に画面へ反映する |
| 合計 | 小計・税額・合計 | 表示 | — | 参考表示。画面の値は送信しない |

- 明細は 1 行以上が必須（`E0030`）。同一商品の重複行は許可する（TBD-O3）。
- 合計の再計算はクライアントでも行うが、送信しない。保存・申請の結果で返るサーバー値で置き換える（`requirements.md` §5.1）。
- 楽観ロック：編集時は `getOrder` の `version` を保持し、`updateOrder` に送る。競合は `409`（`E0006`）で、最新を再読込する旨を表示する。

| 操作 | 処理 | メッセージ |
|---|---|---|
| 下書き保存（新規） | `createOrder` → 成功後 SCR-13 | 成功 `I0010`（注文番号を {0}）。入力誤り `E0008`/`E0030`〜`E0034` |
| 下書き保存（編集） | `updateOrder` | 成功 `I0011` |
| 保存して申請 | 保存後に `submitOrder`。申請前に確認ダイアログ | 確認：`W0020`。成功：`I0020` |
| キャンセル | 入力があれば確認 → SCR-11（新規）/ SCR-13（編集） | `W0011` |
| 競合（409） | 再読込を促す | `E0006` |

## 3. SCR-13 注文詳細

| 項目 | 内容 |
|---|---|
| パス | `/orders/:orderId` |
| 利用ロール | ADMIN、EMPLOYEE（担当）、CUSTOMER（自社。閲覧のみ） |
| 利用 API | `getOrder`、`getApprovalHistory`、`listOrderAttachments`、`submitOrder`、`cancelOrder`、`approveOrder`、`returnOrder`、`uploadOrderAttachment`、`downloadAttachment`、`deleteAttachment`、`downloadOrderDocument` |

```mermaid
flowchart TB
  subgraph P[注文詳細]
    subgraph HD[ヘッダ]
      HD1[注文番号 / 状態バッジ]
      HD2[顧客 / 注文日 / 担当者 / 訂正元注文]
      HD3[操作ボタン群 状態・ロールで出し分け]
    end
    subgraph IT[明細テーブル]
      IT1[行 No / 商品コード / 商品名 / 数量 / 単価 / 税率 / 明細金額 / 税額]
    end
    subgraph TT[合計]
      TT1[小計 / 税額 / 合計]
    end
    subgraph TB[タブ]
      TB1[状態履歴]
      TB2[承認履歴]
      TB3[添付]
    end
  end
  HD --> IT --> TT --> TB
```

### 3.1 表示項目

| 領域 | 項目 | ADMIN | EMPLOYEE | CUSTOMER |
|---|---|:---:|:---:|:---:|
| ヘッダ | 注文番号、状態、顧客、注文日、担当者、訂正元注文、取消し理由（取消済みのとき） | ○ | ○ | ○ |
| 明細 | 商品コード・名称・数量・単価・税率・明細金額・税額（注文時スナップショット） | ○ | ○ | ○ |
| 合計 | 小計・税額・合計 | ○ | ○ | ○ |
| 状態履歴 | 変更日時・変更前後の状態・理由・変更者 | ○ | ○ | ○（承認結果が分かる範囲。表示項目は TBD-O4） |
| 承認履歴 | 承認者・結果（承認/差戻し）・コメント・日時 | ○ | ○ | ○ |
| 添付 | §3.3 | ○ | ○（担当） | ○（公開・SAFE のみ） |

### 3.2 操作ボタン（状態 × ロール）

> 表示制御のみ。最終的な可否は API が判定する（`spec-order-business-ui.md` §3）。

| ボタン | 表示条件（状態） | ADMIN | EMPLOYEE（担当） | CUSTOMER | operationId |
|---|---|:---:|:---:|:---:|---|
| 編集 | DRAFT / RETURNED | ○ | ○ | × | → SCR-12 |
| 申請（再申請） | DRAFT / RETURNED | ○ | ○ | × | `submitOrder` |
| 承認 | PENDING_APPROVAL | ○（割当 ADMIN 本人） | × | × | `approveOrder` |
| 差戻し | PENDING_APPROVAL | ○（割当 ADMIN 本人） | × | × | `returnOrder` |
| 取消し | DRAFT / RETURNED | ○ | ○ | × | `cancelOrder` |
| 取消し | APPROVED | ○ | × | × | `cancelOrder` |
| 訂正して再作成 | CANCELLED | ○ | ○ | × | → SCR-12（訂正元を設定） |
| PDF 出力 | APPROVED | ○ | ○ | ○ | `downloadOrderDocument` |
| 一覧へ戻る | 常時 | ○ | ○ | ○ | → SCR-11 |

- PENDING_APPROVAL の注文には編集・取消しを表示しない（`../approval/spec-approval-business-ui.md` §3）。
- CANCELLED では編集・申請・取消しを出さない。

| 操作 | ダイアログ | 処理と結果 |
|---|---|---|
| 申請 | 確認ダイアログ（`W0020`） | `submitOrder` → 状態が PENDING_APPROVAL に。成功 `I0020` |
| 取消し（DLG-1） | 理由入力（必須・最大 500）＋確認 | `cancelOrder` → 成功 `I0021`。取消し後にメール通知が送られる旨を注記 |
| 承認（DLG-2） | 確認のみ | `approveOrder` → 成功 `I0022` |
| 差戻し（DLG-3） | 理由入力（必須・1〜500 文字） | `returnOrder` → 成功 `I0023`。理由未入力は `E0030`、500 超は `E0032` |
| 状態競合 | — | `409`：`E0006`。画面を再読込して最新状態を表示する |

### 3.3 添付パネル（M-attachment）

```mermaid
flowchart TB
  subgraph A[添付タブ]
    U[アップロード ADMIN・担当 EMPLOYEE のみ]
    subgraph T[添付テーブル]
      TH[ファイル名 / サイズ / 公開範囲 / スキャン状態 / アップロード者 / 日時 / 操作]
    end
    N[注記 許可形式・上限サイズ]
  end
  U --> T --> N
```

| 項目 | 内容 |
|---|---|
| 一覧列 | ファイル名、サイズ（KB/MB）、公開範囲（社内 / 顧客公開）、スキャン状態（確認中 / 安全 / 拒否）、アップロード者、日時 |
| ダウンロード | スキャン状態が SAFE のときのみ有効（PENDING は「確認中」で無効、REJECTED は無効）。API を経由し、ストレージ URL を画面へ埋め込まない |
| 削除 | ADMIN・担当 EMPLOYEE。確認ダイアログ（`W0010`）→ `deleteAttachment` → `I0012` |
| CUSTOMER | 公開範囲が CUSTOMER かつ SAFE の添付だけが一覧に出る。アップロード・削除ボタンは出さない |
| 注記 | 許可形式：PDF、DOCX、XLSX、CSV、PNG、JPG/JPEG。1 ファイル 10 MiB、注文合計 50 MiB まで |

**添付アップロード（DLG-4）**

| 項目 | 部品 | 必須 | 制約 |
|---|---|:---:|---|
| ファイル | ファイル選択 | ○ | 拡張子・サイズをクライアントで事前確認（形式 `E0009`、サイズ `E0010`） |
| 公開範囲 | 選択 | ○ | INTERNAL（社内）/ CUSTOMER（顧客公開）。既定は INTERNAL |

- アップロード成功は受付完了（`202`）であり、直後は「確認中」でダウンロードできない旨を表示する（`I0024`）。スキャン完了は画面の再読込（または手動更新）で反映する。
- 更新方法（自動ポーリングの有無）は TBD-O5。

## 4. 画面状態（spec-12 §5 に従う追加条件）

| 状態 | 表示 |
|---|---|
| 読込中 | 明細・履歴・添付をそれぞれスケルトン表示 |
| データなし | 明細 0 件は異常（`E0012` 相当）。履歴・添付 0 件は「なし」を表示 |
| 範囲外・未存在 | API が `404` を返し SCR-20 |

## 5. 確認事項（TBD）

| # | 事項 |
|---|---|
| TBD-O1 | 編集時に顧客を変更可能にするか（本書は不可と仮定。変更が必要なら取消し→新規） |
| TBD-O2 | 単価をユーザーが標準単価から変更できるか（要件は商品の単価を複写としか書いていない。本書は ADMIN・EMPLOYEE が変更可と仮定） |
| TBD-O3 | 同一商品の明細重複を許可するか（本書は許可と仮定） |
| TBD-O4 | CUSTOMER に状態履歴の「理由・変更者」を見せてよいか（取消し理由・差戻し理由の公開範囲） |
| TBD-O5 | 添付のスキャン状態の更新を、画面の手動更新だけとするか、一定間隔の自動更新を行うか |
