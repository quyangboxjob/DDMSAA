# 詳細設計 — 顧客

## 1. データ

| テーブル | 主な列 | 制約 |
|---|---|---|
| `customers` | customer_id, customer_code, customer_name, email, phone, status | customer_code 一意 |
| `customer_contacts` | customer_contact_id, customer_id, contact_name, email, is_notification_recipient, active | 顧客に従属 |
| `employee_customer_assignments` | assignment_id, employee_user_id, customer_id, active, assigned_at | 社員・顧客組合せ一意 |

## 2. API

| operationId | API | 権限 |
|---|---|---|
| listCustomers | GET /api/customers | ADMIN 全件、EMPLOYEE 担当分 |
| createCustomer | POST /api/customers | ADMIN |
| getCustomer | GET /api/customers/{customerId} | ADMIN/担当 EMPLOYEE/自社 CUSTOMER |
| updateCustomer | PATCH /api/customers/{customerId} | ADMIN |
| updateCustomerStatus | PATCH /api/customers/{customerId}/status | ADMIN |

## 3. 処理・検証

- 顧客コードは 1～30 文字の英数字/ハイフンで一意、顧客名は 1～200 文字必須。
- 無効顧客は新規注文、担当割当、顧客ログインの対象にできない。履歴は物理削除しない。
- 担当割当の作成時に employee_user_id が ACTIVE の EMPLOYEE であることを検証する。
- 通知先は active かつ is_notification_recipient=true の連絡先だけを採用する。
- CUSTOMER の customerId と URL の customerId が異なる要求は 404 とする。

## 4. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-CUS-01 | 顧客コード重複 | 409 |
| UT-CUS-02 | 担当外 EMPLOYEE の参照 | 404 |
| UT-CUS-03 | CUSTOMER の他社参照 | 404 |
| UT-CUS-04 | 無効顧客への注文作成 | 422 |

- **BR-CUS-001 担当割当必須**: EMPLOYEE は有効な employee_customer_assignments を持つ顧客だけを操作できる。
- **BR-CUS-002 顧客データ隔離**: CUSTOMER は自身の customer_id のデータだけを参照できる。
- **BR-CUS-003 無効顧客保護**: 無効顧客を新規注文の対象にしない。
