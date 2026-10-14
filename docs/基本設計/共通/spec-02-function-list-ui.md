# DDMSAA 機能モジュール一覧

| ID | モジュール | 主な利用者 | 主な責務 | 主な API | 基本設計 | 詳細設計 |
|---|---|---|---|---|---|---|
| M-user | ユーザー・認証 | 全ロール/ADMIN | パスワードログイン、セッション、利用者管理 | /auth, /users | 業務設計/user | 詳細設計/業務/user |
| M-customer | 顧客 | ADMIN/EMPLOYEE | 顧客、連絡先、担当割当 | /customers | 業務設計/customer | 詳細設計/業務/customer |
| M-product | 商品 | ADMIN | 商品、税区分、有効状態 | /products | 業務設計/product | 詳細設計/業務/product |
| M-order | 注文 | ADMIN/EMPLOYEE/CUSTOMER | 注文、明細、税額、状態履歴 | /orders | 業務設計/order | 詳細設計/業務/order |
| M-approval | 承認 | ADMIN | 承認経路、承認、差戻し、履歴 | /approval-requests | 業務設計/approval | 詳細設計/業務/approval |
| M-attachment | 添付 | ADMIN/EMPLOYEE/CUSTOMER | 検証、スキャン、公開範囲、配信 | /attachments | 業務設計/attachment | 詳細設計/業務/attachment |
| M-report | 帳票・集計 | ADMIN/EMPLOYEE/CUSTOMER | PDF、集計、CSV、出力ジョブ | /reports, /exports | 業務設計/report | 詳細設計/業務/report |

## 横断ルール

- ロールは ADMIN / EMPLOYEE / CUSTOMER。
- EMPLOYEE は担当顧客、CUSTOMER は自社だけをサーバー側で対象にする。
- 注文金額・税額・承認・添付・出力は共通業務ルール一覧に従う。
- 未決事項を残さず、初版の決定は requirements.md と spec-08-business-rule-list.md に集約する。
