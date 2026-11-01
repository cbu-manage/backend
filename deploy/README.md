# 서버 운영 가이드 (Lightsail, 2026-11-02 기준)

코드 밖에 있는 것 — 서버가 어디에 어떻게 떠 있는지, 비밀값은 어디에 있는지, 배포·백업·복구는 어떻게 하는지.

2026-08-29에 EC2+RDS에서 **Lightsail 한 대 + 같은 서버의 MySQL 컨테이너**로 옮겼다.
이 폴더의 `docker-compose.yml`은 서버 `~/cbu/compose.yml`의 사본이다(2026-11-02 동기화).

```
개발자 PC ──push main──▶ GitHub Actions ──이미지 push──▶ GHCR (ghcr.io/cbu-manage/backend:<sha7>)
                              │ ssh                                      │ pull
                              ▼                                          ▼
                     Lightsail cbu-manage (54.116.11.192, Ubuntu 24.04, 2GB)
                       nginx(443) ─▶ backend(8080) ─▶ mysql:8.4 (standalone 컨테이너)
                                                  └─▶ redis:7 (refresh token)
FE(Vercel, tukcbu.com) ──▶ https://dev.tukcbu.com/api/v1/… ──▶ 위 nginx
```

## 1. 접속

| 항목 | 값 |
|---|---|
| 서버 | Lightsail `cbu-manage`, 서울 2a, `small_3_0`($12/월, 2vCPU/2GB/60GB), 고정 IP **54.116.11.192** |
| SSH | `ssh ubuntu@54.116.11.192 -i lightsail-cbu.pem` → `deploy/scripts/ssh-config.snippet`을 `~/.ssh/config`에 넣으면 `ssh cbu-new` |
| 키 | `lightsail-cbu.pem` (Lightsail 기본 키페어). 저장소에 없음. 본인 공개키를 서버 `~/.ssh/authorized_keys`에 등록받는 방식 권장 |
| 방화벽 | Lightsail 네트워킹 탭: 22 / 80 / 443 |
| 도메인 | `dev.tukcbu.com` A → 54.116.11.192 (가비아, 민주 계정) |
| 인증서 | Let's Encrypt, 호스트의 certbot. 만료 2027-01-26, `/etc/cron.d/certbot-renew`가 매일 03:00 자동 갱신 + nginx 리로드 |
| AWS 콘솔 | 계정 064160141919, IAM `cbumanage1` (ReadOnly + Lightsail 관리). **자격증명은 따로 전달** |
| GitHub | `cbu-manage/backend`. 작업은 `dev` 브랜치에서 PR, **main 머지는 민주가 한다**(main push = 즉시 운영 배포). Actions 시크릿(`SERVER_IP/USER/SSH_KEY/APP_DIR`)은 admin만 보이는데, 평소 운영엔 건드릴 일 없음 |

헬스: `curl -s https://dev.tukcbu.com/api/v1/onboarding-links` → 200. 스웨거: `https://dev.tukcbu.com/swagger-ui/index.html`

## 2. 서버 구성 (`/home/ubuntu/cbu`)

```
~/cbu
├── compose.yml                 backend / redis / nginx  (deploy/docker-compose.yml 과 동일)
├── .env                        BACKEND_IMAGE=ghcr.io/cbu-manage/backend:<sha7>  ← 배포가 갱신
├── config/application.properties   ★ 비밀값 전부 여기. git 에 없음. 컨테이너에 ro 마운트
├── nginx/default.conf          dev.tukcbu.com 443 → backend:8080 (/api/v1, /swagger-ui, /v3/api-docs 만 통과)
├── migrations-incoming/        배포 때 deploy/sql/* 이 복사되는 곳 + _apply.sh
├── backup/                     backup.sh, 야간 덤프 14일분, backup.log, archive/(구 RDS 최종 덤프)
├── mysql-conf/low-mem.cnf      buffer pool 256M, max_connections 60
└── mysql-data/                 MySQL 데이터 (uid 999). 이 폴더가 곧 DB다.
```

컨테이너 4개. **mysql 은 compose 밖에서 따로 띄운 standalone 컨테이너**라 `docker compose down` 해도 안 죽고, `docker compose ps`에도 안 보인다. 네트워크 `cbu-net`(external)로 셋이 연결된다.

```
backend  ghcr.io/cbu-manage/backend:<sha7>   mem_limit 900m, JVM -Xmx640m (deploy/Dockerfile.runtime)
redis    redis:7-alpine                      mem_limit 60m
nginx    nginx:stable-alpine                 80/443
mysql    mysql:8.4                           ~/cbu/mysql-data → /var/lib/mysql, ~/cbu/mysql-conf → /etc/mysql/conf.d
```

만약 mysql 컨테이너를 다시 만들어야 하면 (데이터는 `mysql-data`에 있으니 보존됨):
```bash
P=$(docker inspect mysql --format '{{range .Config.Env}}{{println .}}{{end}}' | grep ^MYSQL_ROOT_PASSWORD= | cut -d= -f2-)   # 지우기 전에 확보
docker run -d --name mysql --restart unless-stopped --network cbu-net \
  -v /home/ubuntu/cbu/mysql-data:/var/lib/mysql -v /home/ubuntu/cbu/mysql-conf:/etc/mysql/conf.d \
  -e MYSQL_ROOT_PASSWORD="$P" mysql:8.4
```

램이 2GB라 여유가 거의 없다(스왑 2G 있음). `free -m`에서 available 이 200MB 아래로 내려가면 backend 를 먼저 의심.
**서버에서 gradle 빌드 금지** — 구 EC2가 OOM 으로 죽던 원인이 그거다.

## 3. 비밀값 — 어디에 있고 어떻게 받나

전부 서버 `~/cbu/config/application.properties` 한 파일에 있다. 키 목록과 의미는 `deploy/config/application.properties.example` 참고.
SSH 가 되면 `deploy/scripts/fetch-server-files.sh` 를 돌리면 `secrets/` 폴더로 실물이 내려온다. **secrets/ 는 git 에 올리지 말 것.**

| 묶음 | 키 | 비고 |
|---|---|---|
| DB | `spring.datasource.*` | 호스트 `mysql`, DB `CbuManage`, 계정 `cbu_app`. root 비번은 mysql 컨테이너 env(`MYSQL_ROOT_PASSWORD`)에만 있음 |
| JWT | `cbu.jwt.secret`, `cbu.login.salt` | 바꾸면 전원 재로그인. 과거 채팅에 노출된 적 있어 교체 검토 대상 |
| 메일 | `spring.mail.*` | 인증메일 발신 Gmail 앱 비밀번호 |
| S3 | `aws.access.key.id/secret`, `aws_bucket=cbumanages3bucket` | 업로드 이미지·HWP 서명·DB 백업이 이 버킷 |
| 구글 시트 | `google.*` | 지원서 연동 |
| FE 연동 | `cbu.proxy.secret` | **Vercel 의 `PROXY_SECRET` 과 같은 값**이어야 BFF 가 넘긴 IP 를 믿는다. 한쪽만 바꾸면 인증메일 제한이 풀린다 |
| 기타 | `default.login.password`, `cbu.cors.allowed-origins` | 관리자 생성 회원 초기 비번 / FE 도메인 바뀌면 origin 추가 |

properties 를 고쳤으면 `docker compose up -d backend` (재시작) 해야 반영된다. 백업본 `application.properties.bak-20260902` 가 옆에 있다.

## 4. 배포

**`main` 에 push 하면 끝.** `.github/workflows/deploy.yml` 이 순서대로:
1. Actions 에서 jar 빌드(테스트 포함, 깨지면 여기서 중단) → `deploy/Dockerfile.runtime` 로 실행 전용 이미지 → GHCR push
2. `deploy/sql/*.sql` 을 서버 `migrations-incoming/` 로 복사 → `_apply.sh` 가 **미적용분만** 실행 (아래 5절)
3. 서버에서 이미지 pull → `.env` 의 `BACKEND_IMAGE` 갱신 → `docker compose up -d backend`
4. 60초 안에 `/api/v1/onboarding-links` 가 200 이 안 나오면 **직전 이미지로 자동 롤백**하고 워크플로 실패

실전에서 수십 번 돌았다. 보통 push 후 3~4분.

수동 롤백/특정 태그 배포: `deploy/scripts/manual-deploy-rollback.sh <sha7>` (서버 `.env` 고치고 `compose up` 하는 것을 묶어둔 것). 최후의 수동 롤백본 `cbu-backend:migr1` 이미지가 서버에 남아 있다.

함정
- **워크플로에 `environment:` 를 붙이지 말 것.** GitHub 환경 `SERVER CI/CD` 에 구 EC2 시절 시크릿이 그대로 남아 있고, 환경 시크릿이 레포 시크릿보다 우선한다. 예전에 이걸로 구 서버에 배포가 나갔다. 현재 워크플로는 레포 레벨 시크릿만 쓴다.
- **`docker image prune -af` 금지.** 실행 중이 아닌 `cbu-backend:migr1` 까지 지운다. 워크플로가 하는 정리는 직전·latest·migr1 을 남긴다.
- 서버의 `~/cbu/compose.yml` 을 바꾸면 `deploy/docker-compose.yml` 도 같이 맞출 것(배포가 이 파일을 쓰진 않는다).
- 로그: `ssh cbu-new 'docker logs backend --tail 200 -f'`

## 5. DB 스키마 변경 (마이그레이션)

`spring.jpa.hibernate.ddl-auto=validate` 라 **JPA 가 스키마를 만들지 않는다.** 엔티티를 바꾸면 SQL 을 직접 써야 한다.

1. `deploy/sql/YYYY-MM-DD-설명.sql` 추가 (기존 파일 참고, 멱등하게 쓰면 안전)
2. main 에 push → 배포가 앱 재시작 **전에** `_apply.sh` 로 실행하고 `schema_migration` 테이블에 파일명을 기록. 이미 기록된 파일은 건너뜀
3. 적용 여부 확인: `deploy/scripts/db-shell.sh` → `SELECT * FROM schema_migration ORDER BY applied_at DESC LIMIT 5;`

함정
- 엔티티에서 필드만 지우고 컬럼을 안 지우면 **조회는 되고 저장만 죽는다.** validate 가 못 잡는다. 컬럼 삭제 SQL 을 같이 넣을 것.
- SQL 파일 하나가 중간에 실패하면 앞 문장은 이미 반영됐고 파일은 미기록으로 남는다. 고친 뒤 재배포하면 처음부터 다시 돈다 → 멱등하게 쓰는 이유.
- 손으로 DB 를 만질 땐 `--default-character-set=utf8mb4` 필수(`db-shell.sh` 에 들어 있음). 없으면 한글 조건이 조용히 0건.
- `post` PK 는 `post_id`, `post_report` PK 는 `post_report_id`. 자료방이 post 를 참조하고 댓글은 자기참조라 대량 삭제는 순서가 있다.

## 6. 백업·복구

- 매일 03:30 `backup.sh`(ubuntu crontab) → `mysqldump` → gzip → 무결성 검사 → **S3 `cbumanages3bucket/db-backups/daily/`** 업로드, 서버엔 14일분. 로그 `~/cbu/backup/backup.log` 끝줄이 `backup ok` 면 정상.
- 복원 절차는 `deploy/scripts/restore-db.md`. 리허설은 이관 때 한 번만 했다.
- Lightsail 자동 스냅샷: 매일 03:00 KST, 7일 보관(2026-11-02 켬). 디스크째 복구는 이걸로, 데이터만 되돌리는 건 S3 덤프로.
- 이관 당시 구 RDS 최종 덤프 2개가 `~/cbu/backup/archive/` 에 있다. 지우지 말 것.

## 7. 아직 안 끝난 것 (2026-11-02 실측)

| # | 항목 | 상태 | 왜 중요한가 |
|---|---|---|---|
| 1 | **구 EC2 `cbuManage`(t2.micro, 15.165.190.216)·RDS `database-1`·EIP 가 아직 살아 있음** | running / available | 매달 비용이 나가고 있다. **11/2 내부 확인 완료: 구 EC2엔 CBU만 있다**(`~/CBU_Manage` + backend/nginx/redis 컨테이너, 뉴로 흔적 없음, 최근 트래픽은 스캐너뿐). 구 RDS 최종 덤프는 Lightsail `~/cbu/backup/archive/`에 이미 있고 RDS 자동 스냅샷도 매일 돈다 → **지워도 된다.** 순서: RDS 최종 스냅샷 생성 → RDS 삭제 → EC2 중지·삭제 → EIP 해제 → 볼륨 잔존 확인. IAM `cbumanage1`은 ReadOnly라 **삭제는 루트 계정(민주)으로** |
| 2 | RDS 보안그룹이 0.0.0.0/0 | 열려 있음 | 1번 삭제 전까지 노출 상태 |
| 3 | JWT secret / salt 교체 | 미정 | 과거 채팅 노출. 바꾸면 전원 로그아웃이라 모집 기간 피해서 |
| 4 | GitHub 환경 `SERVER CI/CD` 낡은 시크릿 | 남아 있음 | 4절 함정. 환경 자체를 지우는 게 깔끔 |
| 5 | Lightsail 자동 스냅샷 | 11/2 켬(매일 03:00 KST, 7일 보관) | 6절 |
| 6 | 서버에 `mysql:8.0` 이미지 1.1GB 미사용 | 남아 있음 | `docker rmi mysql:8.0` 해도 됨 (8.4 가 실제) |

## 8. 저장소 운영

- 브랜치: `main` = 배포(push 즉시 운영 반영), `dev` = 통합. **작업 → `dev`로 PR → 민주가 main 머지.** PR 룰셋이 비활성이라 main 직접 push도 기술적으로는 되지만 하지 않는다.
- 테스트: `./gradlew test`. 배포 빌드가 테스트를 다시 돌리므로 **테스트가 깨지면 배포가 안 나간다**(앱은 그대로 떠 있음).
- 로컬 실행은 저장소 README "시작하기". `src/main/resources/application.properties` 는 gitignore 라 서버 것을 받아 DB 주소만 로컬로 바꾸면 된다.
- FE 와 맞물리는 것: 쿠키 JWT(`accessToken` 10분 / `refreshToken` 10일), CORS origin 목록, `cbu.proxy.secret`. FE 저장소 `cbu-manage/frontend` 의 BFF(`app/api/...`)가 `dev.tukcbu.com` 을 부른다.

