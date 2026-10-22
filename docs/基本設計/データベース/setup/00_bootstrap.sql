-- 00_bootstrap.sql：業務接続ユーザー・データベースの作成（機構A。スーパーユーザーで postgres DB に接続して実行）。
-- 変数は docker/postgres/init/00-setup.sh が psql -v で渡す：db_name / db_user / db_password（値をこのファイルに書かない）。
-- 仕様：データベース/spec-db-connection-ui.md §4.1〜§4.3、spec-db-foundation-temp.md §4.1。

-- psql 変数（:"var" / :'var'）は SQLFluff が解析できないため、psql 専用スクリプトとして PRS を免除する。
-- noqa: disable=PRS

CREATE ROLE :"db_user" WITH LOGIN PASSWORD :'db_password';

CREATE DATABASE :"db_name"
WITH OWNER = :"db_user"
ENCODING = 'UTF8'
LC_COLLATE = 'C.UTF-8'
LC_CTYPE = 'C.UTF-8'
TEMPLATE = template0;

REVOKE ALL ON DATABASE :"db_name" FROM PUBLIC;
GRANT ALL PRIVILEGES ON DATABASE :"db_name" TO :"db_user";

-- システムタイムゾーン（spec-01 §2）に揃える。
ALTER DATABASE :"db_name" SET timezone TO 'Asia/Tokyo';
ALTER ROLE :"db_user" SET search_path TO public;

-- noqa: enable=PRS
