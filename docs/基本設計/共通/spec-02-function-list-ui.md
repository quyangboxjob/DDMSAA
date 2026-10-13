# DDMSAA 販売システム 機能モジュール一覧

## 1. 文書情報

| 項目 | 内容 |
| --- | --- |
| システム名 | DDMSAA 販売システム |
| 文書種別 | 基本設計・機能モジュール一覧 |
| 版数 | 0.1（初稿） |
| 作成日 | 2026-10-13 |
| 根拠 | `docs/要件定義/requirements.md` |
| ステータス | レビュー待ち |

## 2. モジュール構成

各モジュールの基本設計は、本表のリンク先を正本とする。詳細設計以降のリンクは未作成であり、基本設計のレビュー完了後に作成する。

| ID | モジュール名 | 対象システム | 主な責務 | 主な利用者 | 基本設計 | 詳細設計 |
| --- | --- | --- | --- | --- | --- | --- |
| M-USR | 利用者・権限管理 | DDMSAA | アカウント、役割、所属顧客、有効状態、認可 | システム管理者 | [M-USR](../業務設計/user/spec-user-business-ui.md) | [未作成](../../詳細設計/業務/user/spec-user-detail-sd.md) |
| M-CUS | 顧客管理 | DDMSAA | 顧客マスタ、顧客担当者、利用者との所属関係 | システム管理者、一般社員、顧客 | [M-CUS](../業務設計/customer/spec-customer-business-ui.md) | [未作成](../../詳細設計/業務/customer/spec-customer-detail-sd.md) |
| M-PRD | 商品管理 | DDMSAA | 商品マスタ、単価、税区分、有効状態 | システム管理者、一般社員 | [M-PRD](../業務設計/product/spec-product-business-ui.md) | [未作成](../../詳細設計/業務/product/spec-product-detail-sd.md) |
| M-ORD | 注文管理 | DDMSAA | 注文、明細、金額計算、状態遷移、検索 | システム管理者、一般社員、顧客 | [M-ORD](../業務設計/order/spec-order-business-ui.md) | [未作成](../../詳細設計/業務/order/spec-order-detail-sd.md) |
| M-APR | 承認管理 | DDMSAA | 承認ルール、承認依頼、承認・差戻し履歴、通知起点 | システム管理者、一般社員 | [M-APR](../業務設計/approval/spec-approval-business-ui.md) | [未作成](../../詳細設計/業務/approval/spec-approval-detail-sd.md) |
| M-FIL | ファイル管理 | DDMSAA | 注文添付、認可付きダウンロード、保存・削除履歴 | システム管理者、一般社員、顧客 | [M-FIL](../業務設計/attachment/spec-attachment-business-ui.md) | [未作成](../../詳細設計/業務/attachment/spec-attachment-detail-sd.md) |
| M-RPT | 検索・帳票・報表 | DDMSAA | 注文検索、集計、CSV、注文書出力 | システム管理者、一般社員、顧客 | [M-RPT](../業務設計/report/spec-report-business-ui.md) | [未作成](../../詳細設計/業務/report/spec-report-detail-sd.md) |

## 3. 共通の設計方針

- 画面およびメッセージは日本語を基本とする。
- React + TypeScript + Vite を画面、Spring Boot を API、PostgreSQL を永続化に用いる。
- すべての API は認証済み利用者を前提とし、サーバー側で役割・担当・所属顧客を検証する。
- 更新、承認、添付操作および権限変更は監査ログの対象とする。
- 顧客は自社の注文・閲覧可能添付を参照するだけであり、注文・添付を作成、更新、取消、削除できない。

## 4. 未確定事項

- 承認金額閾値、承認段階、代理承認の可否
- 税計算・端数処理、値引き・送料の扱い
- 帳票の正式レイアウトと PDF 出力の要否
- 添付ファイルの許可拡張子、最大サイズ、保存期間、ウイルススキャン方式
- 一般社員が閲覧できる報表の範囲（担当分のみ / 全社分）
