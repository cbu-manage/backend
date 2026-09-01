# Backend deployment

This folder contains the Docker Compose deployment used by the backend repository.

## Server-only files

Create these files on the server; they are intentionally ignored by git:

- `deploy/.env.dev`
- `deploy/config/application.properties`

## Deploy

`main` 푸시 → `.github/workflows/deploy.yml` 이 자동 처리한다.

1. GitHub Actions에서 jar 빌드(테스트 포함) → `deploy/Dockerfile.runtime` 으로 실행 전용 이미지 빌드 → `ghcr.io/cbu-manage/backend:<sha7>` push
2. 서버에 SSH → 이미지 pull → `$APP_DIR/.env` 의 `BACKEND_IMAGE` 갱신 → `docker compose up -d backend`
3. 헬스체크(`/api/v1/onboarding-links`, 최대 60초) 실패 시 직전 이미지로 자동 롤백

**서버에서는 빌드하지 않는다.** 램 2GB라 Gradle 빌드가 OOM으로 죽는다.

수동 롤백은 서버에서 `$APP_DIR/.env` 의 `BACKEND_IMAGE` 를 이전 태그로 되돌리고 `docker compose up -d backend`.

이 폴더의 `docker-compose.yml` 은 구 EC2 시절 파일이라 현재 서버(`$APP_DIR/compose.yml`)와 다르다.
