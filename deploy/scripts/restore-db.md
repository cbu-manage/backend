# DB 복원 (백업에서 되돌리기)

백업 위치
- 서버 `~/cbu/backup/CbuManage-YYYYMMDD-0330.sql.gz` (최근 14일)
- S3 `s3://cbumanages3bucket/db-backups/daily/` (전부)
- 구 RDS 최종본 `~/cbu/backup/archive/rds-archive-CbuManage-20260829.sql.gz` (이관 당시 원본, 건드리지 말 것)

절차 (서버에서)
```bash
cd ~/cbu
docker compose stop backend                                   # 앱 먼저 내림
F=backup/CbuManage-20261101-0330.sql.gz                       # 되돌릴 파일
gunzip -t "$F"                                                # 무결성
P=$(docker inspect mysql --format '{{range .Config.Env}}{{println .}}{{end}}' | grep ^MYSQL_ROOT_PASSWORD= | cut -d= -f2-)
docker exec mysql mysqldump --single-transaction -uroot -p"$P" CbuManage | gzip > backup/before-restore-$(date +%Y%m%d-%H%M).sql.gz   # 현재 상태도 남김
gunzip -c "$F" | docker exec -i mysql mysql --default-character-set=utf8mb4 -uroot -p"$P" CbuManage
docker compose up -d backend
```
복원 리허설은 2026-08-29 이관 때 한 번 통과했다. 그 뒤로는 안 해봤으니 처음 할 때 시간 여유를 두고.
