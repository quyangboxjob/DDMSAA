# DDMSAA バックエンド開発規約

## 1. 技術と構成

Spring Boot、MyBatis、PostgreSQL 16、Flyway、Bean Validation、JUnit 5 + Mockito を使用する。初版は単一のバックエンドアプリケーションであり、独立した auth-service や API Gateway を作らない。

§§§text
backend/
  src/main/java/.../
    common/          例外、応答、監査、セキュリティ共通
    auth/            パスワード認証、セッション、CSRF
    user/
    customer/
    product/
    order/
    approval/
    attachment/
    report/
    infrastructure/  MyBatis、ストレージ、メール、ジョブ
  src/main/resources/
    db/migration/    Flyway migration
§§§

## 2. API とトランザクション

- API の正本は `docs/基本設計/共通/spec-07-api-list.yaml` とする。
- Controller は HTTP 入出力、Service は認可・業務ルール・トランザクション、Mapper は DB アクセスだけを担当する。
- 注文、明細、状態履歴、承認依頼、監査ログは 1 トランザクションで保存する。
- メール、スキャン、出力作成はコミット後にジョブとして実行する。
- 更新 API は `version` を受け、楽観ロック不一致は 409 を返す。

## 3. 認証・認可

- 認証は `login_id` と BCrypt パスワード、サーバー側セッション Cookie `DDMSAA_SESSION` を使う。
- パスワード、Cookie 生値、DB/SMTP/ストレージの秘密情報をログに出力しない。
- ログイン失敗 5 回で 30 分ロックし、失敗理由は同じ 401 応答にする。
- ADMIN は全件、EMPLOYEE は `employee_customer_assignments` の担当顧客、CUSTOMER は自身の `customer_id` だけを Service/SQL で強制する。
- Cookie 認証の POST/PUT/DELETE は CSRF トークンを必須にする。

## 4. データとファイル

- 物理 DB の正本は `spec-06-db-physical.yaml`。変更は Flyway migration でだけ行う。
- パスワードは `users.password_hash` に BCrypt ハッシュだけを保存し、生セッションは `user_sessions.token_hash` としてハッシュ保存する。
- 添付は拡張子/MIME/サイズを検証し、`SAFE` になるまで配信しない。
- API からストレージの恒久 URL を返さない。

## 5. テスト

- Service ごとに成功、認可拒否、状態不正、競合、入力不正を単体テストする。
- 注文は税計算、version 競合、状態遷移、担当外アクセスを必ずテストする。
- 承認は二重操作、別承認者、差戻し理由、経路スナップショットを必ずテストする。
- 添付は MIME 不一致、容量超過、PENDING/REJECTED 配信拒否、顧客公開範囲を必ずテストする。
