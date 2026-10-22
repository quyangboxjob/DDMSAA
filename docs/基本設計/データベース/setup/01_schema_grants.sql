-- 01_schema_grants.sql：schema 権限と既定権限（機構A。スーパーユーザーで業務 DB に接続して実行）。
-- 変数：db_user（00-setup.sh が psql -v で渡す）。
-- 仕様：データベース/spec-db-connection-ui.md §4.1（手順 4〜7）・§4.3。

-- psql 変数（:"var" / :'var'）は SQLFluff が解析できないため、psql 専用スクリプトとして PRS を免除する。
-- noqa: disable=PRS

ALTER SCHEMA public OWNER TO :"db_user";
REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT USAGE, CREATE ON SCHEMA public TO :"db_user";

ALTER DEFAULT PRIVILEGES FOR ROLE :"db_user" IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES, TRIGGER ON TABLES TO :"db_user";

ALTER DEFAULT PRIVILEGES FOR ROLE :"db_user" IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO :"db_user";

-- noqa: enable=PRS
