# M-FIL ファイル管理 詳細設計書

## 1. 対象と前提

| 項目 | 内容 |
| --- | --- |
| 対象モジュール | M-FIL ファイル管理 |
| 根拠 | `基本設計/業務設計/attachment/spec-attachment-business-ui.md` |
| 対象画面 | SCR-FIL-01〜03 |
| 関連データ | `order_attachments`、`attachment_access_logs` |
| 版数・作成日 | 0.1（初稿）・2026-10-14 |
| ステータス | レビュー待ち |

添付は必ず 1 件の注文に紐付ける。CUSTOMER は自社注文に紐付く `CUSTOMER` 公開の添付だけをダウンロードでき、登録・置換・削除はできない。

## 2. バックエンド詳細設計

| operationId | API | 権限・対象範囲 | 処理概要 |
| --- | --- | --- | --- |
| listOrderAttachments | `GET /api/orders/{orderId}/attachments` | 注文閲覧権限保有者 | 可視性と注文の行単位権限で一覧を返す。 |
| uploadOrderAttachment | `POST /api/orders/{orderId}/attachments` | ADMIN、注文作成 EMPLOYEE | ファイルと公開区分を検証し、注文へ添付する。 |
| downloadAttachment | `GET /api/attachments/{attachmentId}/download` | 添付閲覧権限保有者 | 認可後にストリームまたは短時間の署名 URL を返す。 |
| deleteAttachment | `DELETE /api/attachments/{attachmentId}` | ADMIN、登録者 | 論理削除する。 |

### 2.1 処理・セキュリティ

- ストレージの実キー `storage_key` はサーバーだけが生成し、画面・API に直接返さない。
- `visibility` は `INTERNAL` または `CUSTOMER`。登録者は ADMIN/EMPLOYEE のみが指定できる。
- ダウンロードは毎回、注文の行単位権限と可視性を検証する。実ファイルの URL を画面へ恒久的に保存しない。
- 許可拡張子、最大サイズ、1 注文あたりの容量、ウイルススキャン方式は運用パラメータとして確定するまで、アップロード API を有効化しない。未確定値をハードコードしない。
- 登録、削除、ダウンロードの成功・拒否を `attachment_access_logs` と監査ログに記録する。

## 3. 画面詳細設計

### SCR-FIL-01 添付ファイル一覧

注文詳細内の領域として表示する。列はファイル名、種別、サイズ、公開区分、登録者、登録日時、操作とする。CUSTOMER には `CUSTOMER` の行だけを表示し、公開区分・登録者は表示しない。

### SCR-FIL-02 添付アップロード

| 入力項目 | 必須 | ルール |
| --- | :---: | --- |
| ファイル | ○ | 運用で許可された拡張子・サイズ・スキャン結果を満たす。 |
| 公開区分 | ○ | `INTERNAL` / `CUSTOMER`。CUSTOMER を選ぶ場合は顧客に見えることを確認する。 |
| 説明 | 任意 | 500 文字以内。 |

アップロード中は進捗を表示し、完了するまで重複送信を禁止する。スキャン失敗・容量超過・拡張子不許可はファイル欄にエラーを表示する。

### SCR-FIL-03 添付ダウンロード

一覧の「ダウンロード」操作から起動する。対象ファイル名とサイズを確認し、`downloadAttachment` 成功後に取得を開始する。403/404 の場合は、存在・権限を詳細に区別しないメッセージを表示する。

## 4. フロントエンド構成と例外表示

`AttachmentList`、`AttachmentUploadDialog`、`AttachmentDownloadButton` を注文画面で利用する。ファイル実体は状態管理・ログ出力に保持せず、ファイル名、サイズ、状態だけを扱う。

## 5. 単体テスト用例

| UT-ID | 対象 | 前提・入力 | 期待結果 |
| --- | --- | --- | --- |
| UT-FIL-001 | uploadOrderAttachment | CUSTOMER が実行 | 403。保存しない。 |
| UT-FIL-002 | uploadOrderAttachment | 許可されない拡張子 | 400。ストレージへ保存しない。 |
| UT-FIL-003 | downloadAttachment | CUSTOMER が INTERNAL 添付を指定 | 403 または 404。ファイルを返さない。 |
| UT-FIL-004 | deleteAttachment | 登録者以外の EMPLOYEE | 403。削除状態を変更しない。 |
| UT-FIL-005 | deleteAttachment | 正当な登録者 | 論理削除され、一覧・ダウンロード対象から除外される。 |
