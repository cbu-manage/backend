#!/bin/bash
# 서버 실물: /home/ubuntu/cbu/backup/backup.sh — ubuntu crontab 03:30 매일
# CBU DB 야간 백업: mysqldump → gzip → S3 업로드, 로컬 14일 보관
set -euo pipefail
DIR=/home/ubuntu/cbu/backup
STAMP=$(date +%Y%m%d-%H%M)
FILE=$DIR/CbuManage-$STAMP.sql.gz

docker exec mysql sh -c 'exec mysqldump --single-transaction --triggers --routines -uroot -p"$MYSQL_ROOT_PASSWORD" CbuManage' | gzip > "$FILE"

# 무결성: gzip 열리고 마지막 줄에 Dump completed 있어야
gunzip -t "$FILE"
gunzip -c "$FILE" | tail -1 | grep -q "Dump completed" || { echo "dump incomplete"; exit 1; }

aws s3 cp "$FILE" s3://cbumanages3bucket/db-backups/daily/ --only-show-errors

# 로컬 14일 초과분 삭제
find "$DIR" -maxdepth 1 -name "CbuManage-*.sql.gz" -mtime +14 -delete
echo "backup ok: $FILE ($(stat -c%s "$FILE") bytes)"
