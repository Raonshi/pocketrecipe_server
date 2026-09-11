#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${DATABASE_URL:-}" ]]; then
  echo "DATABASE_URL must be a PostgreSQL connection URI." >&2
  exit 1
fi

for file in "$(dirname "$0")"/../db/schema/*.sql; do
  version="$(basename "$file" | cut -d_ -f1)"
  applied="$(psql "$DATABASE_URL" --tuples-only --no-align --command "SELECT 1 FROM schema_versions WHERE version = '$version'" 2>/dev/null || true)"
  if [[ "$applied" == "1" ]]; then
    continue
  fi
  psql "$DATABASE_URL" --set ON_ERROR_STOP=1 --file "$file"
done
