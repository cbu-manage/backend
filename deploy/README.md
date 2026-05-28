# Backend deployment

This folder contains the Docker Compose deployment used by the backend repository.

## Server-only files

Create these files on the server; they are intentionally ignored by git:

- `deploy/.env.dev`
- `deploy/config/application.properties`

## Deploy command

From the backend repository root on the server:

```bash
cd deploy
docker compose down --remove-orphans
docker compose up --build -d
```
