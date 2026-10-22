# ddmsaa-backend

Spring Boot 3.5 / Java 21 のマイクロサービス（api-gateway :8080、auth-service :8081、business-service-core :8082）。構成・依存は `docs/基本設計/基盤/インタフェース仕様/spec-project-structure.yaml`、実装規約は `docs/開発規約/spec-backend-coding-rule.md` を正とする。

## 前提

- JDK 21（`JAVA_HOME`）。Maven は同梱の `./mvnw`（Windows は `mvnw.cmd`）。
- ローカル DB・Redis：リポジトリ直下の `docker/`（`docker compose up -d`。使い方は `docker/README.md`）。
- 環境変数：`.env.example` を参考に、各サービスの起動前に設定する（IDE の実行構成か `set -a; source .env; set +a`）。

## ビルドとテスト

```bash
./mvnw -B test       # L1 / L2（Docker 不要）
./mvnw -B verify     # L3（*IT。Testcontainers のため Docker が必要）を含む全テスト
./mvnw -B verify -pl business-service-core -Dit.test=TestFoundationIT -Dtest=NoSuchTest -Dsurefire.failIfNoSpecifiedTests=false   # IT を 1 つだけ
```

テスト基盤（`src/test/.../support/`）の使い方は `docs/開発規約/spec-test-rule.md` §2.1・§11 と `.claude/skills/DDMSAA-PROJECT-CONTEXT.md` を参照。

## 起動

```bash
./mvnw -pl auth-service spring-boot:run
./mvnw -pl business-service-core spring-boot:run
./mvnw -pl api-gateway spring-boot:run
```

Magic Link のメールは SMTP が必要。ローカルでは `docker compose --profile mail up -d`（Mailpit）を使い、`MAIL_HOST=localhost`、`MAIL_PORT=1025` を設定する。

## 未整形の注意

Spotless（`docs/基本設計/基盤/インタフェース仕様/eclipse-formatter.xml`）は Eclipse の更新サイトが使えず未適用。手作業で LF・UTF-8・4 スペース・100 桁以内に揃えている。
