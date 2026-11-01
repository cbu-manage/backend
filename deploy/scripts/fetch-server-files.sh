#!/usr/bin/env bash
# 서버의 실제 설정 파일(비밀값 포함)을 로컬 secrets/ 로 받는다. SSH 접속이 먼저 돼야 한다.
# deploy/secrets/ 는 절대 git 에 올리지 말 것.
set -euo pipefail
H=${1:-cbu-new}
cd "$(dirname "$0")/../.."
mkdir -p deploy/secrets && chmod 700 secrets
for f in compose.yml .env config/application.properties nginx/default.conf backup/backup.sh mysql-conf/low-mem.cnf; do
  mkdir -p "deploy/secrets/$(dirname "$f")"
  ssh "$H" "cat ~/cbu/$f" > "deploy/secrets/$f"
done
# mysql root 비밀번호는 컨테이너 환경변수에만 있다 (backup.sh 가 이걸 쓴다)
ssh "$H" 'docker inspect mysql --format "{{range .Config.Env}}{{println .}}{{end}}" | grep ^MYSQL_' > deploy/secrets/mysql-container.env
chmod -R go-rwx deploy/secrets
echo "받은 파일:"; find deploy/secrets -type f
