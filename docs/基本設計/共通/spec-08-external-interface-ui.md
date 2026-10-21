<!-- devtool:file-region id="document-header" -->
# 08 外部インタフェース設計

> 外部システム・サービスとの連携：メール（認証 Magic Link・業務通知）、オブジェクトストレージ、ファイル出力（PDF・CSV）、ウイルススキャン。
> 業務ルールは `spec-08-business-rule-list.md`、バッチは `spec-15-batch-design-ui.md` を正とする。

（文書ヘッダは `spec-00-doc-standard-ui.md` を参照）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.common.08-external-interface-ui.s01" -->
## 1. 外部インタフェース一覧

| IF ID | 名称 | 方向 | 連携先 | トリガー | 関連機能 |
|--------|------|------|---------|------|---------|
| IF-01 | メール送信 | 出力 | SMTP/メールサービス | Magic Link（auth-service）、業務通知（BAT-01） | F-AUTH-01、F-NTF-01、F-ORD-04、F-ORD-05、F-APR-02、F-RPT-03 |
| IF-02 | オブジェクトストレージ | 双方向 | オブジェクトストレージ | 添付の保存・読込・削除、CSV の保存・読込・削除 | F-ATT-02〜04、F-RPT-03 |
| IF-03 | 帳票・ファイル出力 | 出力 | ファイルダウンロード（API 応答） | 注文 PDF、CSV のダウンロード | F-RPT-01、F-RPT-03 |
| IF-04 | ウイルススキャン | 双方向 | ClamAV（clamd。`app.scan.type=clamd`） | BAT-05 が PENDING の添付をスキャンに渡す | F-ATT-02 |

> 方向：出力（システム→外部）/ 入力（外部→システム）/ 双方向。

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.08-external-interface-ui.s02" -->
## 2. インタフェース定義

### IF-01 メール送信

| 項目 | 内容 |
|------|------|
| 連携先・プロトコル | SMTP（`JavaMailSender`） |
| トリガー条件 | Magic Link：`loginInit` / `resendMagicLink`（auth-service が直接送信）。業務通知：`notification_logs` の QUEUED 行を BAT-01 が送信 |
| 同期/非同期 | Magic Link は要求内で送信。業務通知は非同期（コミット後に BAT-01） |
| 失敗時の処理 | Magic Link：warn ログのみ（応答は常に `I0001`。token はログに出さない）。業務通知：初回＋再試行 3 回（1 分・5 分・15 分後）、最終失敗は FAILED とし ADMIN が SCR-18 で再送（BR-NTF-002） |
| 宛先 | BR-NTF-003 の表に従う。同一メールアドレスへは 1 通 |

### IF-02 オブジェクトストレージ

| 項目 | 内容 |
|------|------|
| 連携先・プロトコル | オブジェクトストレージ（S3 互換 API） |
| 保存キー | ランダムに発行したキー（`order_attachments.storage_path` / `export_jobs.storage_path`）。利用者に URL を返さない |
| 読込 | ダウンロード API が毎回再認可したうえでストリーム配信する |
| 削除 | BAT-03（CSV、失効後）・BAT-04（添付、論理削除後）が削除し `storage_purged_at` を記録 |
| 失敗時の処理 | 保存失敗は 500（`E0012`）としてトランザクションを戻す。削除失敗は次回バッチで再試行する |

### IF-03 帳票・ファイル出力

| 項目 | 内容 |
|------|------|
| 注文 PDF | `downloadOrderPdf`。APPROVED のみ（BR-RPT-001）。ファイル名 `order_<注文番号>.pdf` |
| CSV | `downloadExportJob`。UTF-8 BOM、1 行＝1 注文（BR-RPT-002）。ファイル名 `orders_YYYYMMDD.csv` |
| 形式 | Result で包まないバイナリ応答（`Content-Disposition` 付き） |

### IF-04 ウイルススキャン

| 項目 | 内容 |
|------|------|
| 連携先・プロトコル | ClamAV の clamd へ TCP（INSTREAM）。`infrastructure.scan.VirusScanService`（`spec-07-infrastructure.yaml`）。開発・テストは `app.scan.type=none`（スキャンせず SAFE） |
| トリガー条件 | BAT-05（1 分ごと）が `scan_status=PENDING` の添付を取り出してスキャンに渡す。アップロード API は依頼しない（外部の結果通知 API・アウトボックス表は不要） |
| 同期/非同期 | 非同期（アップロードとは別のバッチ） |
| 失敗時の処理 | スキャナ不達・タイムアウトは PENDING のまま次回に再試行。24 時間を超えた PENDING は WARN ログで検知し、運用者が確認する |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.08-external-interface-ui.s03" -->
## 3. メールテンプレート一覧（メールを使用する場合、文面を集中管理する）

| テンプレートID | 送信タイミング | 宛先 | 件名 | 本文の要点 | 関連機能 |
|--------|---------|-------|------|---------|---------|
| MAIL-01 | Magic Link 要求・再送 | 本人（`user_contacts.email`） | ログイン用リンク | ログイン用 URL（1 回限り・60 分有効）。token は URL にのみ含める | F-AUTH-01 |
| MAIL-02 | 申請・再申請（ORDER_SUBMITTED） | 有効な ADMIN（申請者を除く） | 承認依頼 | 注文番号・顧客名・合計・申請者 | F-ORD-04 |
| MAIL-03 | 承認（ORDER_APPROVED） | 申請者・担当者・顧客通知先 | 注文が承認されました | 注文番号・顧客名・合計 | F-APR-02 |
| MAIL-04 | 差戻し（ORDER_RETURNED） | 申請者・担当者 | 注文が差し戻されました | 注文番号・差戻し理由 | F-APR-02 |
| MAIL-05 | 取消し（ORDER_CANCELLED） | 申請者・担当者（APPROVED からの取消しは顧客通知先も） | 注文が取り消されました | 注文番号。顧客宛には取消し理由を載せない（BR-APR-006） | F-ORD-05 |
| MAIL-06 | 出力完了（EXPORT_COMPLETED） | 出力ジョブの依頼者 | CSV の作成が完了しました | ジョブ ID・件数・有効期限（ファイルは添付しない） | F-RPT-03 |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.08-external-interface-ui.s04" -->
## 4. ファイル出力規約（帳票/エクスポートがある場合）

> **帳票・エクスポートの一覧・形式・レイアウトの正本は `spec-14-report-design-ui.md`**（帳票 ID は `RPT-`）。ファイル名規則は `spec-00-doc-standard-ui.md` §2。本書では帳票を再定義せず、外部連携としての扱い（IF-03）のみを定める。

| IF ID | 内容 | 帳票の正本 |
|--------|------|------|
| IF-03 | PDF/CSV 出力（ファイルダウンロード） | `spec-14-report-design-ui.md` §1 帳票一覧（RPT-xx） |

### 記入要領

- 外部依存には必ず**失敗時の処理**（リトライ・アラート・補償）を定義し、外部が常に成功すると仮定しない。
- メール内容に個人情報が含まれる場合は、マスキングとコンプライアンスに留意する。
- 帳票のファイル名規則は `spec-00-doc-standard-ui.md` §2「出力ファイル」を正本とし、`spec-07-api-list-ui.md` と整合させる。
<!-- devtool:endblock -->
