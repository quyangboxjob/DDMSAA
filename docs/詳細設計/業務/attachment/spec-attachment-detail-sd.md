# 詳細設計 — 注文添付

> 業務ルールの番号・内容は `基本設計/共通/spec-08-business-rule-list.md` を正とし、本書は実装手順を定める。

## 1. データ

`order_attachments` は id（AT+8 桁）、order_id、original_file_name、storage_path、content_type、file_size、visibility（T07-9）、scan_status（T07-10）、storage_purged_at と共通列を持つ。アップロード者・日時は created_by / created_at。ファイル本体は DB に保存しない。

## 2. API

| operationId | API | 権限 |
|---|---|---|
| listOrderAttachments | GET /api/v1/orders/{orderId}/attachments | 注文の参照権限（CUSTOMER は公開かつ SAFE のみ） |
| uploadOrderAttachment | POST /api/v1/orders/{orderId}/attachments（multipart） | ADMIN / 担当 EMPLOYEE。CANCELLED 以外の注文 |
| downloadAttachment | GET /api/v1/attachments/{attachmentId}/download | 毎回再認可 |
| deleteAttachment | DELETE /api/v1/attachments/{attachmentId} | ADMIN / 担当 EMPLOYEE。CANCELLED 以外の注文 |

## 3. アップロード処理

1. 注文の操作権限を確認する（範囲外は 404 `E0005`、CUSTOMER は 403 `E0002`）。
2. 注文を `SELECT … FOR UPDATE` で行ロックする。注文が CANCELLED なら 409 `E0006`（BR-FIL-001）。
3. ファイル名の拡張子と検出 MIME が PDF/DOCX/XLSX/CSV/PNG/JPG/JPEG の許可組合せか検証する（不可は 409 `E0009`）。ファイル名は 255 文字以内（超過は 400 `E0008`）。
4. サイズを検証する（BR-FIL-002）。0 バイトは 400 `E0053`、10 MiB 超は 413 `E0010`。
5. 注文の合計を BR-FIL-005 の条件（`deleted_at IS NULL AND scan_status <> REJECTED` の `file_size` 合計）で求め、今回のファイルを加えて 50 MiB を超えれば 413 `E0052`。413 は `PayloadTooLargeException` で送出する（`開発規約/spec-backend-coding-rule.md` §4。`BusinessException` を使うと 409 になるため使わない）。
6. ランダムな storage_path を発行し、オブジェクトストレージへ保存する。
7. `scan_status=PENDING`、visibility を DB に保存し、マルウェアスキャン依頼と監査（`ATTACHMENT_UPLOAD`）を同一トランザクションで登録する（`I0024`）。
8. スキャン完了の内部処理（BAT-05）だけが `SAFE` 又は `REJECTED` に遷移できる。

## 4. ダウンロード・削除

- ダウンロードは attachment、order、利用者の範囲を再検証する。
- ADMIN は全件、EMPLOYEE は担当注文、CUSTOMER は自顧客かつ `visibility=CUSTOMER` かつ `scan_status=SAFE` のみ取得できる。
- PENDING / REJECTED は 409 `E0006`（ADMIN・EMPLOYEE には一覧に状態を表示する）。CUSTOMER から見えない添付と削除済みは 404。ストレージへの恒久 URL を返さない。CANCELLED 注文の添付もダウンロードできる。
- 削除は論理削除（deleted_at）し、物理ファイル削除は BAT-04 が行い storage_purged_at を記録する（`I0012`）。CANCELLED の注文では 409 `E0006`。監査は `ATTACHMENT_DOWNLOAD` / `ATTACHMENT_DELETE`。
- 保持期限 365 日を過ぎた添付は BAT-04 が論理削除する（BR-FIL-006）。

## 5. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-FIL-01 | exe 又は MIME 不一致 | 409 `E0009`、保存なし |
| UT-FIL-02 | 10 MiB 超 | 413 `E0010`、保存なし |
| UT-FIL-03 | PENDING をダウンロード | 409 `E0006` |
| UT-FIL-04 | CUSTOMER 公開でない SAFE 添付 | CUSTOMER は 404 |
| UT-FIL-05 | 担当外 EMPLOYEE が削除 | 404 |
| UT-FIL-06 | スキャン REJECTED | 配信なし、監査あり |
| UT-FIL-07 | 既存 45 MiB の注文へ 6 MiB を追加 | 413 `E0052` |
| UT-FIL-08 | REJECTED・削除済みを除いた合計が 50 MiB 以下 | 保存できる |
| UT-FIL-09 | 0 バイトのファイル | 400 `E0053` |
| UT-FIL-10 | CANCELLED の注文へアップロード | 409 `E0006` |

## 6. 適用する業務ルール

BR-FIL-001〜BR-FIL-005、BR-AUTH-003、BR-AUTH-006、BR-AUD-001（内容は `spec-08-business-rule-list.md`）。
