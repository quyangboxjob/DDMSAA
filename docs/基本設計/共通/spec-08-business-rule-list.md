# DDMSAA 業務ルール一覧

<!-- rule: order.tax-calculation -->
<!-- rule: approval.single-admin -->
<!-- rule: attachment.safe-download -->
<!-- rule: report.async-export -->

> すべてのルールは API の Service 層で強制し、画面だけの制御に依存しない。操作結果は必要に応じて監査ログに記録する。

## 1. 認証・権限

- **BR-AUTH-001 有効利用者のみログイン**: `ACTIVE` の利用者だけが `login_id` と BCrypt パスワードでログインできる。失敗理由は利用者に区別して返さない。
- **BR-AUTH-002 ロックアウト**: 同一利用者の連続失敗が 5 回に達したら 30 分ロックし、成功ログインで失敗回数を 0 に戻す。
- **BR-AUTH-003 データ範囲**: ADMIN は全件、EMPLOYEE は有効な `employee_customer_assignments` の顧客、CUSTOMER は自身の `customer_id` のみを対象にする。
- **BR-AUTH-004 顧客は閲覧専用**: CUSTOMER は注文作成・更新・取消し・承認・添付操作・CSV・マスタ管理を実行できない。

## 2. マスタ

- **BR-USR-001 ユーザー管理者限定**: ユーザー作成、ロール変更、無効化は ADMIN だけが行える。
- **BR-CUS-001 担当割当必須**: EMPLOYEE は有効な担当割当がある顧客だけを注文対象にできる。
- **BR-PRD-001 商品管理者限定**: 商品の作成、更新、有効化/無効化は ADMIN だけが行える。
- **BR-PRD-002 注文時点スナップショット**: 商品コード、商品名、単価、税区分、税率は注文明細に複写し、後の商品変更で過去注文を変えない。

## 3. 注文・税・承認

- **BR-ORD-001 作成可能者**: ADMIN 又は担当 EMPLOYEE だけが注文を作成できる。
- **BR-ORD-002 編集可能状態**: 注文を編集できるのは `DRAFT` と `RETURNED` だけである。
- **BR-ORD-003 金額再計算**: サーバーは明細ごとに `quantity * unit_price` を計算し、画面から送られた小計・税額・合計を信用しない。
- **BR-ORD-004 税計算**: 税区分は STANDARD 10%、REDUCED 8%、EXEMPT 0% とし、明細税額は `HALF_UP(line_amount * tax_rate / 100)` で計算する。
- **BR-ORD-005 訂正方法**: APPROVED 注文は直接更新しない。取消し後に新規注文を作成し、訂正なら `revised_from_order_id` を記録する。
- **BR-ORD-006 楽観ロック**: 注文更新、申請、取消しは `orders.version` を照合し、不一致時は 409 を返す。
- **BR-APR-001 初版経路**: 金額を問わず ADMIN 1 段承認とし、申請時に経路を `approval_requests` へスナップショット保存する。
- **BR-APR-002 承認者本人性**: 承認又は差戻しは割り当てられた ADMIN 自身だけが実行でき、代理承認は初版で扱わない。
- **BR-APR-003 差戻し理由**: 差戻しには 1 文字以上 500 文字以下の理由を必須とし、注文状態を `RETURNED` にする。
- **BR-APR-004 最終承認**: 最終段階の承認で注文状態を `APPROVED` にし、作成者と担当者へ通知を投入する。

## 4. 添付・帳票・通知

- **BR-FIL-001 添付可能者**: ADMIN 又は担当 EMPLOYEE だけが注文添付をアップロード又は削除できる。
- **BR-FIL-002 添付検証**: PDF、DOCX、XLSX、CSV、PNG、JPG/JPEG だけを受け付け、1 ファイル 10 MiB、注文合計 50 MiB を超える場合は 422 を返す。
- **BR-FIL-003 スキャン完了前は非公開**: 添付は `PENDING` で保存し、`SAFE` だけをダウンロード可能にする。`REJECTED` は配信しない。
- **BR-FIL-004 顧客公開範囲**: CUSTOMER は自顧客の注文に属し、`visibility=CUSTOMER` かつ `scan_status=SAFE` の添付だけを参照できる。
- **BR-RPT-001 帳票範囲**: PDF/集計は利用者のデータ範囲に限定し、CUSTOMER は自社注文 PDF のみ、CSV は ADMIN/EMPLOYEE だけに提供する。
- **BR-RPT-002 CSV 出力**: CSV は UTF-8 BOM とし、10,000 件超は `export_jobs` で非同期生成して 24 時間で失効させる。
- **BR-NTF-001 通知イベント**: 承認申請、承認、差戻し、取消し、出力完了は通知ジョブを作成し、送信結果を `notification_logs` に記録する。
- **BR-NTF-002 再試行**: 通知失敗は指数バックオフで最大 3 回再試行し、最終失敗を管理者が追跡可能にする。
