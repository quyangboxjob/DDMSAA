# 詳細設計 — 注文添付

## 1. データ

`order_attachments` は attachment_id、order_id、original_file_name、storage_key、content_type、size_bytes、visibility、scan_status、uploaded_by、uploaded_at、deleted_at を持つ。ファイル本体は DB に保存しない。

## 2. API

| operationId | API | 権限 |
|---|---|---|
| listOrderAttachments | GET /api/orders/{orderId}/attachments | 注文の参照権限 |
| uploadOrderAttachment | POST /api/orders/{orderId}/attachments | ADMIN/担当 EMPLOYEE |
| downloadAttachment | GET /api/attachments/{attachmentId}/download | 毎回再認可 |
| deleteAttachment | DELETE /api/attachments/{attachmentId} | ADMIN/担当 EMPLOYEE |

## 3. アップロード処理

1. 注文の操作権限と、注文が削除されていないことを確認する。
2. ファイル名の拡張子と検出 MIME が PDF/DOCX/XLSX/CSV/PNG/JPG/JPEG の許可組合せか検証する。
3. サイズが 0 より大きく 10 MiB 以下、既存添付と合わせて注文ごと 50 MiB 以下か検証する。
4. ランダムな storage_key を発行し、暗号化対応ストレージへ保存する。
5. `scan_status=PENDING`、visibility、操作者を DB に保存し、マルウェアスキャンジョブと監査を同一トランザクションで登録する。
6. スキャン完了の内部処理だけが `SAFE` 又は `REJECTED` に遷移できる。

## 4. ダウンロード・削除

- ダウンロードは attachment、order、利用者の範囲を再検証する。
- ADMIN は全件、EMPLOYEE は担当注文、CUSTOMER は自顧客かつ `visibility=CUSTOMER` かつ `scan_status=SAFE` のみ取得できる。
- PENDING/REJECTED/削除済みは 404 とする。ストレージへの恒久 URL を返さない。
- 削除は論理削除し、物理ファイル削除は非同期清掃ジョブが行う。削除済みは取得不可。
- 保持期限 365 日を過ぎた添付も同様に論理削除する。

## 5. テスト

| ID | 条件 | 期待結果 |
|---|---|---|
| UT-FIL-01 | exe 又は MIME 不一致 | 422、保存なし |
| UT-FIL-02 | 10 MiB 超 | 422、保存なし |
| UT-FIL-03 | PENDING をダウンロード | 404 |
| UT-FIL-04 | CUSTOMER 公開でない SAFE 添付 | CUSTOMER は 404 |
| UT-FIL-05 | 担当外 EMPLOYEE が削除 | 404 |
| UT-FIL-06 | スキャン REJECTED | 配信なし、監査あり |

- **BR-FIL-001 許可形式と容量**: PDF、DOCX、XLSX、CSV、PNG、JPG/JPEG のみ、1 ファイル 10 MiB、注文合計 50 MiB を許可する。
- **BR-FIL-002 スキャン完了必須**: PENDING 又は REJECTED の添付をダウンロードさせない。
- **BR-FIL-003 顧客公開判定**: CUSTOMER は自顧客の CUSTOMER 公開かつ SAFE の添付だけを取得できる。
