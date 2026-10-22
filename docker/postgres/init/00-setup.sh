#!/bin/bash
# 機構A：docker 初回起動（データボリュームが空のとき）に 1 回だけ実行される初期化スクリプト。
# setup/*.sql は /setup に読み取り専用でマウントされる（docker-compose.yml）。
# 手順の仕様：docs/基本設計/データベース/spec-db-migration-ui.md §2、spec-db-connection-ui.md §4.1。
set -euo pipefail

: "${DB_NAME:?DB_NAME is required}"
: "${DB_USER:?DB_USER is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"
SEED_SAMPLE_DATA="${SEED_SAMPLE_DATA:-true}"
SETUP_DIR="${SETUP_DIR:-/setup}"

# スーパーユーザー（POSTGRES_USER）で接続するための共通オプション。
SU=(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER")
VARS=(-v "db_name=$DB_NAME" -v "db_user=$DB_USER" -v "db_password=$DB_PASSWORD")

echo "[setup] 00_bootstrap: role and database"
"${SU[@]}" --dbname postgres "${VARS[@]}" -f "$SETUP_DIR/00_bootstrap.sql"

echo "[setup] 01_schema_grants: schema privileges"
"${SU[@]}" --dbname "$DB_NAME" "${VARS[@]}" -f "$SETUP_DIR/01_schema_grants.sql"

# 以降は業務接続ユーザーで実行する（オブジェクトの所有者を DB_USER にする）。
export PGPASSWORD="$DB_PASSWORD"
APP=(psql -v ON_ERROR_STOP=1 --username "$DB_USER" --dbname "$DB_NAME")

echo "[setup] 10_schema: 31 tables, functions, sequences, triggers"
"${APP[@]}" --single-transaction -f "$SETUP_DIR/10_schema.sql"

echo "[setup] 20_seed: reference masters (T07 code tables, tax_rates)"
"${APP[@]}" --single-transaction -f "$SETUP_DIR/20_seed.sql"

if [ "$SEED_SAMPLE_DATA" = "true" ]; then
  echo "[setup] 21_seed_sample: development sample data"
  "${APP[@]}" --single-transaction -f "$SETUP_DIR/21_seed_sample.sql"
else
  echo "[setup] 21_seed_sample: skipped (SEED_SAMPLE_DATA=$SEED_SAMPLE_DATA)"
fi

echo "[setup] done"
