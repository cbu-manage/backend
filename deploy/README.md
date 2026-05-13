# Backend deployment

This folder contains the Docker Compose deployment used by the backend repository.

## Server-only files

Create these files on the server; they are intentionally ignored by git:

- `deploy/.env.dev`
- `deploy/config/application.properties`

The backend application also expects Google service-account JSON files under
`src/main/resources/` at runtime. Those files are ignored by git, so keep them on
the server working tree before running `docker compose up --build -d`.

## Deploy command

From the backend repository root on the server:

```bash
cd deploy
docker compose down --remove-orphans
docker compose up --build -d
```
