# Backend deployment

This folder contains the Docker Compose deployment used by the backend repository.

## Server-only files

Create these files on the server; they are intentionally ignored by git:

- `deploy/.env.dev`
- `deploy/config/application.properties`

## Required settings

`deploy/config/application.properties`

- `cbu.proxy.secret` — 프론트 BFF와 나눠 갖는 비밀값. 프론트(Vercel)의 `PROXY_SECRET`과 같은 값을 넣는다.
  이 값이 있어야 BFF가 넘긴 브라우저 IP를 신뢰하고 인증메일 발송을 요청자 단위로 제한한다.
  비워두면 IP 기준 제한을 걸지 않는다(주소 단위 제한은 그대로 동작).

## Deploy command

From the backend repository root on the server:

```bash
cd deploy
docker compose down --remove-orphans
docker compose up --build -d
```
