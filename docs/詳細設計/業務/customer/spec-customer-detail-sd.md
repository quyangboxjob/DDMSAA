# 詳細設計 — 顧客

> 業務ルールの番号・内容は `基本設計/共通/spec-08-business-rule-list.md` を正とし、本書は実装手順を定める。

## 1. データ

| テーブル | 主な列 | 制約 |
|---|---|---|
| `customers` | id（CS+8 桁）, customer_code, customer_name, email, phone_number, status | customer_code は未削除行で一意。status は T07-3 |
| `customer_contacts` | id（CC+8 桁）, customer_id, contact_name, email, is_notification_recipient, is_active | 顧客に従属 |
| `customer_assignments` | id（AS+8 桁）, customer_id, user_id, is_active, assigned_date | 顧客・社員の組合せは未削除行で一意 |

## 2. API

| operationId | API | 権限 |
|---|---|---|
| pageCustomers | GET /api/v1/customers | ADMIN 全件、EMPLOYEE 担当分 |
| createCustomer | POST /api/v1/customers | ADMIN |
| getCustomer | GET /api/v1/customers/{customerId} | ADMIN / 担当 EMPLOYEE / 自社 CUSTOMER（CUSTOMER には担当社員を返さない） |
| updateCustomer | PUT /api/v1/customers/{customerId} | ADMIN（連絡先・担当社員は全件置換） |
| activateCustomer / deactivateCustomer | POST /api/v1/customers/{customerId}/activate・/deactivate | ADMIN |

## 3. 処理・検証

- 顧客コードは 1～30 文字の英数字/ハイフンで未削除行内一意（重複は 409 `E0007`）、顧客名は 1～200 文字必須。更新時は顧客コードを変更しない。
- 無効顧客は新規注文の対象にできない（409 `E0054`。BR-CUS-002）。担当割当・顧客利用者の登録の対象にもできない（409 `E0006`）。既存の DRAFT / RETURNED 注文は取消しだけができ、申請は 409 `E0054`。PENDING_APPROVAL の注文はそのまま承認・差戻しできる。履歴は物理削除しない。
- 担当割当の user_id が有効な EMPLOYEE であることを検証する。
- updateCustomer の連絡先・担当社員は全件置換とする。要求に含まれない既存の連絡先は論理削除する。担当割当は BR-CUS-003 に従い、要求に含まれない行を is_active=false にし、要求に含まれる社員で既存行があれば is_active=true・assigned_date=当日に戻し、なければ新規作成する（部分ユニーク索引と衝突させない）。
- 通知先は is_active かつ is_notification_recipient=true の連絡先だけを採用する（宛先の対象イベントは BR-NTF-003）。
- CUSTOMER の customer_id と URL の customerId が異なる要求、担当外 EMPLOYEE の要求は 404 `E0005` とする（BR-AUTH-006）。
- 更新は `updated_at` で楽観ロックし、不一致は 409 `E0051`。監査は `CUSTOMER_CREATE` / `CUSTOMER_UPDATE` / `CUSTOMER_ACTIVATE` / `CUSTOMER_DEACTIVATE`。成功 messageId は登録 `I0010`、更新 `I0011`、有効化 `I0034`、無効化 `I0035`。

## 4. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-CUS-01 | 顧客コード重複 | 409 `E0007` |
| UT-CUS-02 | 担当外 EMPLOYEE の参照 | 404 `E0005` |
| UT-CUS-03 | CUSTOMER の他社参照 | 404 `E0005` |
| UT-CUS-04 | 無効顧客への注文作成 | 409 `E0054` |
| UT-CUS-05 | CUSTOMER が自社を参照 | 200、assignedEmployees を含まない |
| UT-CUS-06 | 解除済みの担当社員を再割当 | 既存行が is_active=true・assigned_date=当日になり、行は増えない |

## 5. 適用する業務ルール

BR-AUTH-003、BR-AUTH-006、BR-CUS-001〜BR-CUS-003、BR-ORD-006、BR-NTF-003、BR-AUD-001（内容は `spec-08-business-rule-list.md`）。
