<div align="center">

# 씨부엉 백엔드 API 서버

**한국공학대학교 프로그래밍 동아리 씨부엉(CBU Manage) 홈페이지 백엔드**

[![Java](https://img.shields.io/badge/Java%2017-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)](https://redis.io/)
[![AWS S3](https://img.shields.io/badge/AWS%20S3-FF9900?style=flat-square&logo=amazons3&logoColor=white)](https://aws.amazon.com/s3/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)

</div>

---

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [패키지 구조](#패키지-구조)
- [시작하기](#시작하기)
- [API 명세](#api-명세)
- [인증 방식](#인증-방식)
- [환경변수 설정](#환경변수-설정)

---

## 프로젝트 소개

씨부엉 동아리 홈페이지의 백엔드 API 서버입니다.  
회원 인증, 스터디/프로젝트 모집, 활동 보고서, 코딩테스트 문제 관리, 자료실 등 동아리 운영에 필요한 기능을 제공합니다.

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| ORM | Spring Data JPA (Hibernate) |
| Auth | Spring Security + JWT (jjwt 0.12.6) |
| DB | MySQL (AWS RDS) |
| Cache / Token Store | Redis |
| Storage | AWS S3 |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Gradle |
| Deploy | Docker |

---

## 아키텍처

```
프론트엔드 (Next.js)
        │
        │  HTTP (쿠키 기반 JWT)
        ▼
┌─────────────────────────────┐
│       Spring Boot API       │
│  JwtFilter → Controller     │
│       → Service → JPA       │
└──────┬────────────┬─────────┘
       │            │
   MySQL(RDS)     Redis
                   (RefreshToken)
                   
       └──── AWS S3 (이미지)
```

**인증 흐름**
1. 로그인 시 `accessToken` / `refreshToken` 쿠키 발급 (HttpOnly)
2. 이후 모든 요청에서 `JwtFilter`가 쿠키를 검증해 `SecurityContext`에 사용자 정보 등록
3. `accessToken` 만료 시 `/api/v1/login/refresh`로 재발급

---

## 패키지 구조

```
src/main/java/com/example/cbumanage/
├── global/
│   ├── common/         # JwtFilter, JwtProvider, ApiResponse
│   ├── config/         # SecurityConfig, RedisConfig, S3Config
│   └── error/          # 공통 예외 처리 (BaseException, ErrorCode)
│
├── user/               # 로그인/회원가입/내 정보
├── member/             # 동아리 멤버(CbuMember) 관리
├── candidate/          # 가입 후보자 관리 (구글 시트 연동)
│
├── group/              # 그룹 생성·가입·승인 관리
├── post/               # 게시글 공통 (Post 메인 테이블)
├── study/              # 스터디 모집 게시글
├── project/            # 프로젝트 모집 게시글
├── report/             # 활동 보고서 게시글
├── problem/            # 코딩테스트 문제
├── resource/           # 자료실
│
├── comment/            # 댓글 / 답글
├── image/              # 이미지 업로드 (AWS S3)
├── email/              # 이메일 인증
├── dues/               # 회비 관리
└── log/                # 활동 로그
```

---

## 시작하기

### 사전 요구사항

- Java 17
- Docker & Docker Compose (권장)
- MySQL 8.x
- Redis 7.x

### 로컬 실행

**1. 저장소 클론**

```bash
git clone https://github.com/cbu-manage/backend.git
cd backend
```

**2. 환경변수 설정**

`src/main/resources/application.properties` 또는 환경변수로 아래 항목을 설정합니다.  
(상세 항목은 [환경변수 설정](#환경변수-설정) 참고)

**3. Gradle 빌드 & 실행**

```bash
./gradlew clean build -x test
java -jar build/libs/*.jar
```

**4. Docker로 실행**

```bash
docker build -t cbu-backend .
docker run -p 8080:8080 --env-file .env cbu-backend
```

서버 기동 후 Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## API 명세

전체 API는 Swagger UI에서 확인할 수 있습니다.  
아래는 주요 도메인별 엔드포인트 요약입니다.

### 인증 `/api/v1/login`

| Method | Path | 인증 필요 | 설명 |
|--------|------|:---------:|------|
| POST | `/api/v1/login` | ❌ | 로그인 (쿠키에 토큰 발급) |
| POST | `/api/v1/login/signup` | ❌ | 회원가입 |
| POST | `/api/v1/login/refresh` | ❌ | AccessToken 재발급 |
| GET | `/api/v1/login/me` | ✅ | 내 정보 조회 |
| PATCH | `/api/v1/login/password` | ✅ | 비밀번호 변경 |
| POST | `/api/v1/login/password/reset` | ❌ | 이메일 인증코드 기반 비밀번호 초기화 |
| DELETE | `/api/v1/login` | ✅ | 로그아웃 |
| DELETE | `/api/v1/login/account` | ✅ | 회원 탈퇴 |

### 게시글 공통 `/api/v1/post`

| Method | Path | 인증 필요 | 설명 |
|--------|------|:---------:|------|
| GET | `/api/v1/post` | ❌ | 카테고리별 게시글 목록 (페이징) |
| GET | `/api/v1/post/my` | ✅ | 내가 작성한 게시글 목록 |
| GET | `/api/v1/post/{postId}/post` | ❌ | 게시글 단건 조회 |
| DELETE | `/api/v1/post/{postId}` | ✅ | 게시글 삭제 (soft delete) |

> **category 값**: `1` 스터디 · `2` 프로젝트 · `5` 코딩테스트 · `6` 자료실 · `7` 활동보고서

### 스터디 `/api/v1/post/study`

| Method | Path | 인증 필요 | 설명 |
|--------|------|:---------:|------|
| POST | `/api/v1/post/study` | ✅ | 스터디 게시글 생성 |
| GET | `/api/v1/post/study` | ❌ | 스터디 목록 조회 |
| GET | `/api/v1/post/study/{postId}` | ❌ | 스터디 상세 조회 |
| PATCH | `/api/v1/post/study/{postId}` | ✅ | 스터디 수정 (작성자) |
| DELETE | `/api/v1/post/study/{postId}` | ✅ | 스터디 삭제 (작성자) |
| GET | `/api/v1/post/study/filter` | ❌ | 태그별 스터디 검색 |
| POST | `/api/v1/post/study/{postId}/apply` | ✅ | 스터디 참가 신청 |
| GET | `/api/v1/post/study/{postId}/apply` | ✅ | 신청자 목록 조회 (팀장) |
| PATCH | `/api/v1/post/study/{postId}/apply/{applyId}` | ✅ | 신청 수락/거절 (팀장) |
| POST | `/api/v1/post/study/{postId}/close` | ✅ | 모집 마감 + 그룹 생성 (팀장) |

### 프로젝트 `/api/v1/post/project`

| Method | Path | 인증 필요 | 설명 |
|--------|------|:---------:|------|
| POST | `/api/v1/post/project` | ✅ | 프로젝트 게시글 생성 |
| GET | `/api/v1/post/project` | ❌ | 프로젝트 목록 조회 |
| GET | `/api/v1/post/project/{postId}` | ❌ | 프로젝트 상세 조회 |
| PATCH | `/api/v1/post/project/{postId}` | ✅ | 프로젝트 수정 (작성자) |
| DELETE | `/api/v1/post/project/{postId}` | ✅ | 프로젝트 삭제 (작성자) |
| GET | `/api/v1/post/project/filter` | ❌ | 모집분야별 필터 조회 |
| GET | `/api/v1/post/project/me` | ✅ | 내가 작성한 프로젝트 목록 |

### 그룹 `/api/v1/groups`

| Method | Path | 인증 필요 | 권한 | 설명 |
|--------|------|:---------:|------|------|
| POST | `/api/v1/groups/{groupId}/members` | ✅ | 일반 | 그룹 가입 신청 |
| DELETE | `/api/v1/groups/{groupId}/members/me` | ✅ | 일반 | 가입 신청 취소 |
| GET | `/api/v1/groups/my` | ✅ | 일반 | 내가 가입한 그룹 목록 |
| GET | `/api/v1/groups/my/applications` | ✅ | 일반 | 내 신청 목록 |
| GET | `/api/v1/groups/{groupId}` | ✅ | 일반 | 그룹 상세 조회 |
| GET | `/api/v1/groups/{groupId}/applicants` | ✅ | 팀장 | 가입 대기 목록 |
| PATCH | `/api/v1/groups/members/{groupMemberId}/applicant` | ✅ | 팀장 | 가입 수락/거절 |
| PATCH | `/api/v1/groups/{groupId}/recruitment` | ✅ | 팀장 | 모집 상태 변경 |
| GET | `/api/v1/groups/admin` | ✅ | 관리자 | 전체 그룹 조회 |
| PATCH | `/api/v1/groups/{groupId}/admin/status` | ✅ | 관리자 | 그룹 승인/거절 |

### 활동 보고서 `/api/v1/report`

| Method | Path | 인증 필요 | 설명 |
|--------|------|:---------:|------|
| POST | `/api/v1/report` | ✅ | 보고서 생성 |
| GET | `/api/v1/report` | ✅ | 보고서 목록 조회 (페이징) |
| GET | `/api/v1/report/{postId}` | ✅ | 보고서 단건 조회 |
| PATCH | `/api/v1/report/{postId}` | ✅ | 보고서 수정 (작성자) |
| PATCH | `/api/v1/report/{postId}/accept` | ✅ | 보고서 승인 (관리자) |

### 코딩테스트 문제 `/api/v1/post/problems`

| Method | Path | 인증 필요 | 설명 |
|--------|------|:---------:|------|
| POST | `/api/v1/post/problems` | ✅ | 문제 등록 |
| GET | `/api/v1/post/problems` | ❌ | 문제 목록 조회 |
| GET | `/api/v1/post/problems/{id}` | ❌ | 문제 상세 조회 |
| PATCH | `/api/v1/post/problems/{id}` | ✅ | 문제 수정 (작성자) |
| DELETE | `/api/v1/post/problems/{id}` | ✅ | 문제 삭제 (작성자) |

### 자료실 `/api/v1/resources`

| Method | Path | 인증 필요 | 설명 |
|--------|------|:---------:|------|
| POST | `/api/v1/resources` | ✅ | 자료 등록 |
| GET | `/api/v1/resources` | ❌ | 자료 목록 조회 |
| GET | `/api/v1/resources/my` | ✅ | 내 자료 목록 |
| DELETE | `/api/v1/resources/{id}` | ✅ | 자료 삭제 (작성자) |

### 댓글 `/api/v1/`

| Method | Path | 인증 필요 | 설명 |
|--------|------|:---------:|------|
| POST | `/api/v1/post/{postId}/comment` | ✅ | 댓글 작성 |
| GET | `/api/v1/post/{postId}/comment` | ❌ | 댓글 목록 조회 |
| POST | `/api/v1/comment/{commentId}/reply` | ✅ | 답글 작성 |
| PATCH | `/api/v1/comment/{commentId}` | ✅ | 댓글 수정 |
| DELETE | `/api/v1/comment/{commentId}` | ✅ | 댓글 삭제 |

### 이미지 업로드 `/api/image`

| Method | Path | 인증 필요 | 설명 |
|--------|------|:---------:|------|
| POST | `/api/image/upload` | ✅ | 이미지 업로드 (S3) → URL 반환 |

> 지원 형식: `jpg`, `jpeg`, `png`, `gif`, `webp`, `heif`  
> 최대 파일 크기: 10MB

### 이메일 인증 `/api/v1/mail`

| Method | Path | 인증 필요 | 설명 |
|--------|------|:---------:|------|
| POST | `/api/v1/mail/send` | ❌ | 인증 메일 발송 |
| POST | `/api/v1/mail/verify` | ❌ | 인증 코드 확인 |

---

## 인증 방식

### 토큰 발급

로그인 성공 시 응답 헤더 `Set-Cookie`로 두 개의 쿠키가 내려옵니다.

| 쿠키명 | 설명 | 유효시간 |
|--------|------|---------|
| `accessToken` | API 요청 인증용 JWT | 10분 |
| `refreshToken` | accessToken 재발급용 | 10일 |

두 쿠키 모두 `HttpOnly` 설정으로 JavaScript에서 직접 접근 불가합니다.

### 토큰 갱신

```
POST /api/v1/login/refresh
```

`refreshToken` 쿠키가 유효하면 새 `accessToken`과 `refreshToken`을 재발급합니다.

### 프론트엔드 연동 주의사항

프론트엔드와 백엔드의 도메인/포트가 다른 경우(Cross-Origin):
- 요청 시 반드시 `credentials: 'include'` 옵션을 설정해야 쿠키가 전송됩니다.
- 쿠키 설정은 `SameSite=None` / `Secure=false`(개발) 또는 `Secure=true`(HTTPS 운영) 로 구성됩니다.

```typescript
// fetch 예시
fetch('http://localhost:8080/api/v1/login/me', {
  credentials: 'include',
});

// axios 예시
axios.get('/api/v1/login/me', { withCredentials: true });
```

---

## 환경변수 설정

`src/main/resources/application.properties` 에서 관리합니다.  
운영 배포 시에는 환경변수 또는 외부 설정으로 주입하세요.

| 키 | 설명 | 예시 |
|----|------|------|
| `spring.datasource.url` | MySQL 접속 URL | `jdbc:mysql://host:3306/db` |
| `spring.datasource.username` | DB 사용자명 | `admin` |
| `spring.datasource.password` | DB 비밀번호 | `password` |
| `spring.data.redis.host` | Redis 호스트 | `localhost` |
| `spring.data.redis.port` | Redis 포트 | `6379` |
| `cbu.jwt.secret` | JWT 서명 키 (32자 이상 권장) | `your-secret-key` |
| `cbu.jwt.expireTime` | accessToken 만료시간 (ms) | `600000` (10분) |
| `cbu.jwt.refreshExpireTime` | refreshToken 만료시간 (ms) | `864000000` (10일) |
| `cbu.jwt.secureCookie` | 쿠키 Secure 플래그 | `false`(개발) / `true`(운영) |
| `cbu.login.salt` | 비밀번호 해싱 salt | `your-salt` |
| `aws.access.key.id` | AWS 액세스 키 | - |
| `aws.secret.access.key` | AWS 시크릿 키 | - |
| `aws.region.static` | AWS 리전 | `ap-northeast-2` |
| `aws_bucket` | S3 버킷명 | `your-bucket` |
| `spring.mail.username` | Gmail 발신 계정 | `example@gmail.com` |
| `spring.mail.password` | Gmail 앱 비밀번호 | - |
| `google.spreadSheet.id` | 구글 시트 ID (멤버 관리용) | - |

---

<div align="center">
  <sub>Made with ❤️ by CBU Manage · 한국공학대학교 프로그래밍 동아리 씨부엉</sub>
</div>
