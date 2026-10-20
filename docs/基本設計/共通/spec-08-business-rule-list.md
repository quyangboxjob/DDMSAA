# DDMSAA 業務ルール一覧

<!-- rule: order.tax-calculation -->
<!-- rule: approval.single-admin -->
<!-- rule: attachment.safe-download -->
<!-- rule: report.async-export -->

> すべてのルールは API の Service 層で強制し、画面だけの制御に依存しない。操作結果は §5 に従って監査ログに記録する。
> HTTP ステータスと messageId は `開発規約/spec-backend-coding-rule.md` §4 の固定マッピングに従う（業務エラー 409、入力誤り 400、権限 403、不存在 404、サイズ超過 413）。
> **BR 番号は本書を正とする。** 詳細設計は本書の番号と名称をそのまま引用し、新しいルールは先に本書へ登録する。

## 1. 認証・権限

- **BR-AUTH-001 ログイン可否**: 停用されておらず、ロックされておらず、論理削除されていない利用者だけが、ユーザー ID と Magic Link でログインできる。Magic Link 要求（`loginInit`）の応答は可否に関わらず常に `I0001` とし、存在・停用・ロックを区別して返さない（`基盤/spec-02-auth-service-ui.md` §6.1）。
- **BR-AUTH-002 Magic Link**: Magic Link は 1 回使用で失効し、有効期限は 60 分とする。検証時に利用者の状態を再確認し、不可なら `E0004`（401）を返す。JWT アクセストークンの有効期限は 60 分で、リフレッシュは行わない。
- **BR-AUTH-003 データ範囲**: ADMIN は全件、EMPLOYEE は有効な `customer_assignments` の顧客、CUSTOMER は自身の `customer_id` のみを対象にする。
- **BR-AUTH-004 顧客は閲覧専用**: CUSTOMER は注文作成・更新・取消し・承認・添付操作・CSV・マスタ管理を実行できない。
- **BR-AUTH-005 停用・ロック**: 停用・ロック・ロック解除は ADMIN だけが行える。ロックは `system_locks` で管理する。パスワードは存在しないため、試行回数によるロックアウトは行わない。停用・ロックは新たなログインを止め、発行済みの JWT は有効期限（最長 60 分）まで利用できる。
- **BR-AUTH-006 範囲外データ**: データ範囲（BR-AUTH-003）外の 1 件の参照・操作は、存在を開示しないため 404（`E0005`）とする。一覧・検索・集計では範囲外のデータを自動的に除外し、エラーにしない（範囲外の検索条件を指定した場合は 0 件）。ロールで許可されない操作は 403（`E0002`）とする。

## 2. マスタ・利用者

- **BR-USR-001 管理者限定**: 利用者の登録、更新、停用・停用解除、ロック・ロック解除、ロール変更は ADMIN だけが行える。
- **BR-USR-002 秘密情報の非保存**: パスワードは存在しない。Magic Link トークンと JWT を DB・監査・ログへ保存しない。
- **BR-USR-003 顧客アカウント紐付け**: CUSTOMER は必ず `customer_id` を持ち、その顧客のデータだけを参照する。ADMIN / EMPLOYEE は `customer_id` を持たない（`chk_users_customer_id`）。
- **BR-USR-004 ロール変更**: ロール変更は ADMIN ⇔ EMPLOYEE の間だけとし、CUSTOMER と他ロールの相互変更は 400（`E0008`）とする。ユーザー ID は変更しない。EMPLOYEE を ADMIN に変更する場合、有効な担当割当、又は未完了（DRAFT / RETURNED / PENDING_APPROVAL）注文の担当者（`sales_user_id`）が残っていれば 409（`E0006`）とし、先に担当を付け替える。
- **BR-USR-005 管理者の保護**: 自分自身の停用・ロック・ロール変更は 409（`E0006`）とする。有効（ACTIVE かつ未ロック）な ADMIN が 1 名だけのとき、その ADMIN の停用・ロック・ロール変更は 409（`E0055`）とする。
- **BR-CUS-001 担当割当必須**: EMPLOYEE は有効な担当割当がある顧客だけを注文対象にできる。
- **BR-CUS-002 無効顧客**: 無効（INACTIVE）の顧客には新規注文を作成できない（409 `E0054`）。既存の DRAFT / RETURNED 注文は取消しだけができ、申請はできない（BR-ORD-010）。PENDING_APPROVAL の注文はそのまま承認・差戻しできる。
- **BR-CUS-003 担当割当の解除・再割当**: 担当割当の解除は `is_active=FALSE` とし、論理削除しない。同じ顧客・社員を再び割り当てるときは既存行を `is_active=TRUE`・`assigned_date=当日` に戻す（`uq_customer_assignments_customer_id_user_id` と衝突させない）。担当先は有効な EMPLOYEE に限る（400 `E0008`）。
- **BR-PRD-001 商品管理者限定**: 商品の作成、更新、有効化/無効化は ADMIN だけが行える。
- **BR-PRD-002 単価・明細スナップショット**: 明細の単価は商品の標準単価（`standard_unit_price`）とし、画面から変更できない（値引きは初版の対象外）。DRAFT / RETURNED の保存のたびに、商品コード、商品名、単価、税区分、税率を最新のマスタから明細へ複写して再計算し、申請時の値で確定する。PENDING_APPROVAL 以降は商品マスタを変更しても明細を変えない。
- **BR-PRD-003 無効商品**: 注文の商品選択肢には有効（ACTIVE）な商品だけを出す。無効商品を明細に新しく追加することはできない（409 `E0054`）。既存明細の無効商品は複写済みの名称で表示し、保存は妨げないが、申請はできない（BR-ORD-010）。EMPLOYEE の商品検索はサーバー側で有効商品だけに固定する。

## 3. 注文・税・承認

- **BR-ORD-001 作成可能者**: ADMIN 又は担当 EMPLOYEE だけが注文を作成できる。
- **BR-ORD-002 編集可能状態**: 注文を編集・申請できるのは `DRAFT` と `RETURNED` だけである。
- **BR-ORD-003 金額再計算**: サーバーは明細ごとに `quantity * unit_price` を計算し、画面から送られた単価・小計・税額・合計を信用しない。
- **BR-ORD-004 税計算**: 税区分は STANDARD 10%、REDUCED 8%、EXEMPT 0% とし、明細税額は `HALF_UP(line_amount * tax_rate / 100)` で計算する。
- **BR-ORD-005 訂正方法**: APPROVED 注文は直接更新しない。取消し後に新規注文を作成し、訂正なら `revised_from_order_id` を記録する（BR-ORD-009）。
- **BR-ORD-006 楽観ロック**: 更新・申請・取消し・承認・差戻し・マスタ更新は `updated_at` を照合し、不一致時は 409（`E0051`）を返す。`updatedAt` は API でマイクロ秒 6 桁の文字列として返し、クライアントはそのまま返送する（`spec-07-api-list-ui.md` §1）。
- **BR-ORD-007 取消し**: DRAFT / RETURNED の取消しは ADMIN 又は担当 EMPLOYEE、APPROVED の取消しは ADMIN だけが行える（EMPLOYEE は 403 `E0002`）。PENDING_APPROVAL と CANCELLED は取り消せない（409 `E0006`）。取消し理由（1〜500 文字）を必須とする。
- **BR-ORD-008 担当者**: EMPLOYEE が作成した注文の担当者（`sales_user_id`）は作成者本人とする。ADMIN が作成する場合は、注文顧客の有効な担当 EMPLOYEE を指定する（未指定・担当外は 400 `E0008`）。担当者の変更は DRAFT / RETURNED のときに ADMIN だけが行える。EMPLOYEE のデータ範囲は担当者ではなく担当割当（BR-AUTH-003）で判定する。
- **BR-ORD-009 訂正元注文**: `revised_from_order_id` は新規作成時だけ指定でき、更新では変更できない。訂正元は CANCELLED かつ同一顧客の注文とし、1 注文につき訂正は 1 回とする（違反は 409 `E0006`、`uq_orders_revised_from_order_id`）。訂正の作成は新規作成のため、元注文の明細に無効商品が含まれていれば保存時に 409（`E0054`）となる（BR-PRD-003）。画面は無効商品を示し、除外してから保存させる。
- **BR-ORD-010 申請時検証**: 申請時に、顧客が有効、全明細の商品が有効、（EMPLOYEE の場合）担当割当が有効、かつ担当者が顧客の有効な担当であることを確認し、満たさない場合は 409（`E0054`）とする。
- **BR-ORD-011 状態履歴と監査**: 作成・申請・承認・差戻し・取消しのすべての状態遷移を、注文の更新と同一トランザクションで `order_status_histories` と `audit_logs` に記録する。
- **BR-APR-001 初版経路**: 金額を問わず ADMIN による 1 段承認とし、申請時に経路を `approval_requests.rule_snapshot` へスナップショット保存して、後の設定変更を遡及させない。承認ルールの登録・更新では、承認者ロールは ADMIN、承認段階は 1 だけを受け付ける（それ以外は 400 `E0008`）。
- **BR-APR-002 承認者**: 承認者は特定しない。有効な ADMIN のうち、申請者（`approval_requests.created_by`）以外の誰でも承認又は差戻しができ、実際に処理した ADMIN を `approval_requests.approver_user_id` に記録する。申請者本人の承認・差戻しは 409（`E0056`）とする。代理承認・多段承認は初版で扱わない。有効な ADMIN が申請者 1 名だけの場合、その注文は承認できない（運用で別の ADMIN を用意する）。
- **BR-APR-003 差戻し理由**: 差戻しには 1 文字以上 500 文字以下の理由を必須とし、注文状態を `RETURNED` にする。
- **BR-APR-004 承認確定**: 承認で注文状態を `APPROVED` にし、BR-NTF-003 の宛先へ通知を投入する。
- **BR-APR-005 同時処理**: 処理済み（PENDING 以外）の承認依頼への承認・差戻しは 409（`E0006`）とする。先に処理した ADMIN の結果を正とする。
- **BR-APR-006 顧客への履歴公開**: CUSTOMER には承認履歴・状態履歴の操作種別（申請・承認・差戻し・取消し等）と日時だけを返し、操作者と理由（差戻し理由・取消し理由を含む）は返さない（API は null）。

## 4. 添付・帳票・通知

- **BR-FIL-001 添付可能者・可能状態**: ADMIN 又は担当 EMPLOYEE だけが注文添付のアップロード・削除を行える。CANCELLED 以外の注文で行え、CANCELLED の注文では 409（`E0006`）とする（ダウンロードは可）。
- **BR-FIL-002 添付検証**: PDF、DOCX、XLSX、CSV、PNG、JPG/JPEG だけを受け付ける（許可外は 409 `E0009`）。空ファイルは 400（`E0053`）、1 ファイル 10 MiB 超は 413（`E0010`）、注文合計 50 MiB 超は 413（`E0052`）とする。
- **BR-FIL-003 スキャン完了前は非公開**: 添付は `PENDING` で保存し、`SAFE` だけをダウンロード可能にする。`REJECTED` は配信しない。
- **BR-FIL-004 顧客公開範囲**: CUSTOMER は自顧客の注文に属し、`visibility=CUSTOMER` かつ `scan_status=SAFE` の添付だけを参照できる。
- **BR-FIL-005 合計容量の算定**: 注文合計は、未削除（`deleted_at IS NULL`）かつ `scan_status <> REJECTED` の添付の `file_size` 合計とする。アップロード時は注文行を `SELECT … FOR UPDATE` でロックしてから合計を確認し、同時アップロードによる超過を防ぐ。
- **BR-FIL-006 保持期限**: 添付はアップロードから 365 日で論理削除し、ストレージ上のファイルは後続の清掃で削除して `storage_purged_at` を記録する（BAT-04）。
- **BR-RPT-001 帳票範囲**: PDF・集計は利用者のデータ範囲に限定する。CUSTOMER は自社注文の PDF だけを取得でき、集計と CSV は ADMIN / EMPLOYEE だけに提供する。PDF は APPROVED の注文だけを出力し、それ以外は 409（`E0006`）とする。
- **BR-RPT-002 CSV 出力**: CSV は UTF-8 BOM とし、すべての出力を `export_jobs` に記録する。10,000 件以下は同期生成して `READY` で返し（`I0028`）、10,001 件以上は `QUEUED` で登録して非同期生成する（`I0027`）。成果物は完了から 24 時間で失効（`EXPIRED`）し、ストレージ上のファイルを削除する（BAT-03）。CSV は注文単位（1 行＝1 注文）で、列は注文番号、注文日、顧客コード、顧客名、状態、担当者、小計、税額、合計。ファイル名は `orders_YYYYMMDD.csv`（作成日。日本時間）。状態条件を指定しない場合は APPROVED のみを対象とする。
- **BR-RPT-003 出力ジョブの参照範囲**: ADMIN はすべての利用者の出力ジョブを参照・ダウンロードできる（依頼者で絞り込み可）。EMPLOYEE は自分が依頼したジョブだけを参照・ダウンロードできる（他人のジョブは 404 `E0005`）。
- **BR-NTF-001 通知イベント**: 承認申請、承認、差戻し、取消し、出力完了は通知ジョブを作成し、送信結果を `notification_logs` に記録する。Magic Link は auth-service が送信し、`notification_logs` には記録しない。
- **BR-NTF-002 再試行**: 送信は初回＋再試行 3 回の最大 4 回とする（`attempt_count` は初回を含む試行回数）。失敗時は `attempt_count` を加算し、4 回未満なら `QUEUED` のまま `next_attempt_at` を 1 分・5 分・15 分後（指数バックオフ）に設定する。4 回目も失敗したら `FAILED` とし、管理者が通知履歴で追跡できるようにする。手動再送（`retryNotification`）は `FAILED` だけを対象に、`attempt_count=0`・`next_attempt_at=現在` で `QUEUED` に戻す。送信処理は BAT-01（`spec-15-batch-design-ui.md`）。
- **BR-NTF-003 宛先**: 宛先は次のとおりとし、同一メールアドレスへは 1 通だけ送る。申請者は `approval_requests.created_by`、担当者は `orders.sales_user_id`、顧客通知先は `customer_contacts` の `is_notification_recipient = TRUE` かつ有効な連絡先とする。

  | イベント | 宛先 |
  |---|---|
  | ORDER_SUBMITTED（申請・再申請） | 有効な ADMIN 全員（申請者を除く） |
  | ORDER_APPROVED | 申請者、担当者、顧客通知先 |
  | ORDER_RETURNED | 申請者、担当者（顧客には通知しない） |
  | ORDER_CANCELLED | 申請者、担当者（未申請の DRAFT は担当者のみ）。APPROVED からの取消しは顧客通知先を加える |
  | EXPORT_COMPLETED | 出力ジョブの依頼者（非同期生成のときだけ） |

## 5. 監査

- **BR-AUD-001 監査対象**: 次の操作を `audit_logs` に記録する。業務操作は業務データの更新と同一トランザクションで記録し、ログイン系は auth-service が `audit_logs` へ追記する（auth-service の `audit_logs` 権限は INSERT のみ）。Magic Link トークン・JWT・ファイル本体・メール本文は記録しない。

  | 区分 | `audit_action` | `target_type` / `target_id` |
  |---|---|---|
  | ログイン | `LOGIN_REQUESTED`（loginInit）、`LOGIN_SUCCEEDED`（verify 成功）、`LOGIN_FAILED`（Magic Link 無効・期限切れ・ログイン不可） | `USER` / 入力されたユーザー ID（10 文字に切り詰め。存在しない ID も記録し、`actor_user_id` は NULL） |
  | 権限拒否 | `PERMISSION_DENIED`（403、及び BR-AUTH-006 の範囲外 1 件アクセスによる 404） | 対象リソースの種別 / ID |
  | 利用者 | `USER_CREATE`、`USER_UPDATE`、`USER_DISABLE`、`USER_ENABLE`、`USER_LOCK`、`USER_UNLOCK` | `USER` / ユーザー ID |
  | 顧客・商品・承認ルール | `CUSTOMER_CREATE`、`CUSTOMER_UPDATE`、`CUSTOMER_ACTIVATE`、`CUSTOMER_DEACTIVATE`、`PRODUCT_CREATE`、`PRODUCT_UPDATE`、`PRODUCT_ACTIVATE`、`PRODUCT_DEACTIVATE`、`APPROVAL_RULE_CREATE`、`APPROVAL_RULE_UPDATE` | `CUSTOMER` / `PRODUCT` / `APPROVAL_RULE` と各 ID |
  | 注文・承認 | `ORDER_CREATE`、`ORDER_UPDATE`、`ORDER_SUBMIT`、`ORDER_APPROVE`、`ORDER_RETURN`、`ORDER_CANCEL` | `ORDER` / 注文 ID |
  | 添付・帳票・出力・通知 | `ATTACHMENT_UPLOAD`、`ATTACHMENT_DOWNLOAD`、`ATTACHMENT_DELETE`、`ORDER_PDF_DOWNLOAD`、`EXPORT_CREATE`、`EXPORT_DOWNLOAD`、`NOTIFICATION_RETRY` | `ATTACHMENT` / `ORDER` / `EXPORT_JOB` / `NOTIFICATION` と各 ID |

  ログアウトはクライアント側のトークン破棄だけで API がないため記録しない。一覧の範囲外自動除外（BR-AUTH-006）は権限拒否として記録しない。
