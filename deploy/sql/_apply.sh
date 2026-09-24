#!/usr/bin/env bash
# 아직 적용하지 않은 마이그레이션만 실행한다. 배포 워크플로가 앱 재시작 전에 부른다.
#
# SSH 로 여러 줄 명령을 보내면 전송 과정에서 깨진다(실제로 CREATE TABLE 이 쪼개져 실패했다).
# 그래서 로직을 파일로 두고 워크플로는 이 스크립트를 한 줄로 부르기만 한다.
set -euo pipefail

APP_DIR="${APP_DIR:-/home/ubuntu/cbu}"
INCOMING="$APP_DIR/migrations-incoming"
CONF="$APP_DIR/config/application.properties"

DB_USER=$(grep -m1 '^spring.datasource.username=' "$CONF" | cut -d= -f2-)
DB_PASS=$(grep -m1 '^spring.datasource.password=' "$CONF" | cut -d= -f2-)
DB_NAME=$(grep -m1 '^spring.datasource.url=' "$CONF" | sed -E 's#.*/([A-Za-z0-9_]+)\?.*#\1#')

run_sql() { docker exec -i mysql mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" "$@"; }

run_sql -e 'CREATE TABLE IF NOT EXISTS schema_migration (filename VARCHAR(255) NOT NULL PRIMARY KEY, applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)'

# 기록이 비어 있으면 첫 도입이다. 기존 파일은 이미 손으로 적용된 것으로 보고 표시만 남긴다.
# 여기서 전부 실행하면 이미 반영된 DDL 이 다시 돌아 실패한다.
applied_count=$(run_sql -N -e 'SELECT COUNT(*) FROM schema_migration')

if [ "$applied_count" = "0" ]; then
  echo "첫 도입: 기존 마이그레이션을 적용 완료로 표시만 합니다"
  for f in "$INCOMING"/*.sql; do
    [ -e "$f" ] || continue
    name=$(basename "$f")
    run_sql -e "INSERT IGNORE INTO schema_migration (filename) VALUES ('$name')"
    echo "  baseline  $name"
  done
  exit 0
fi

for f in $(ls "$INCOMING"/*.sql 2>/dev/null | sort); do
  name=$(basename "$f")
  done_count=$(run_sql -N -e "SELECT COUNT(*) FROM schema_migration WHERE filename='$name'")
  if [ "$done_count" != "0" ]; then
    echo "  건너뜀    $name"
    continue
  fi
  echo "  적용      $name"
  run_sql < "$f"
  run_sql -e "INSERT INTO schema_migration (filename) VALUES ('$name')"
done

echo "마이그레이션 완료"
