#!/usr/bin/env bash
# 운영 DB 셸. 한글 리터럴을 쓰려면 --default-character-set=utf8mb4 가 반드시 필요하다(없으면 WHERE 가 조용히 0건).
# 사용: scripts/db-shell.sh            → 대화형
#       scripts/db-shell.sh < x.sql     → 파일 실행 (mysql -e 다중문은 첫 오류에서 멈추고 앞 문장은 이미 반영됨)
H=${CBU_HOST:-cbu-new}
ssh -t "$H" 'P=$(grep -m1 "^spring.datasource.password=" ~/cbu/config/application.properties | cut -d= -f2-); docker exec -it mysql mysql --default-character-set=utf8mb4 -ucbu_app -p"$P" CbuManage'
