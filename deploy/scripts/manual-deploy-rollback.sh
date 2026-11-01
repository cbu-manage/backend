#!/usr/bin/env bash
# 평소엔 main push 로 자동 배포된다. 이 스크립트는 Actions 가 죽었거나 특정 태그로 되돌릴 때만.
# 사용: scripts/manual-deploy-rollback.sh <sha7 또는 cbu-backend:migr1>
set -euo pipefail
TAG=${1:?"이미지 태그(sha7) 또는 cbu-backend:migr1"}
H=${CBU_HOST:-cbu-new}
case "$TAG" in *:*) IMG="$TAG";; *) IMG="ghcr.io/cbu-manage/backend:$TAG";; esac
ssh "$H" "set -e; cd ~/cbu
  docker pull '$IMG' 2>/dev/null || echo '로컬에 있는 이미지 사용'
  sed -i 's#^BACKEND_IMAGE=.*#BACKEND_IMAGE=$IMG#' .env
  docker compose up -d backend
  for i in \$(seq 1 12); do
    docker run --rm --network cbu-net curlimages/curl:8.10.1 -fs -o /dev/null http://backend:8080/api/v1/onboarding-links && { echo 'health OK'; exit 0; }
    sleep 5
  done
  echo 'health FAILED'; docker logs backend --tail 50; exit 1"
