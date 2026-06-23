# CBU 동아리 공식 홈페이지 백엔드

Spring Boot 기반의 동아리 홈페이지 백엔드. JPA + MySQL, Redis, S3, JWT 인증.

---

## 도메인 구조 개요

### 멤버/유저
- **user 도메인만 사용** — `member` 도메인은 레거시이며 무시한다.
- `User` 엔티티: `userId`, `name`, `generation`(기수), `studentNumber`, `major`, `role`
- 역할: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_MEMBER`

---

## Post 구조 (메인 테이블 + 서브 테이블)

`post` 테이블이 메인이고, 카테고리별 서브 테이블이 연결되는 구조.

| category 값 | 도메인 | 서브 테이블 | 그룹 연결 |
|-------------|--------|------------|----------|
| 1 | STUDY | `study` | O (Group) |
| 2 | PROJECT | `project` | O (Group) |
| 5 | PROBLEM | (없음) | X |
| 6 | RESOURCE | (없음) | X |
| 7 | REPORT | `post_report` | O (Group, groupId로 연결) |
| 8 | FREEBOARD | `post_freeboard` | X |

### Post 엔티티 주요 필드
```
post_id, authorId, title, content, category, viewCount, isDeleted, createdAt, updatedAt
```
- Soft Delete 방식 (`isDeleted`, `deletedAt`)
- 작성자는 `authorId`(FK)로만 연결, User 엔티티 직접 참조 없음

---

## PostReport (보고서) — 핵심 도메인

### 엔티티 관계
```
Post (category=7)
  └── PostReport (post_id FK)
        ├── groupId (cbu_groups 참조)
        └── PostReportGroupType (STUDY / PROJECT / MENTORING)

PostReport
  └── ReportMember (report_id FK, user_id FK)
        : 해당 활동에 참여한 멤버 목록 (별도 테이블로 관리)
```

### PostReport 주요 필드
- `post`: Post 엔티티 연결 (`@ManyToOne`)
- `groupId`: 보고서를 작성한 그룹 ID
- `type`: `PostReportGroupType` (STUDY / PROJECT / MENTORING)
- `date`: 활동 일시
- `location`: 활동 장소
- `reportImage`: 활동 사진 S3 URL
- `reportFile`: 첨부 파일 S3 URL (PDF 한정)
- `reflection`: 활동 후기
- `nextPlan`: 다음 활동 계획
- `isAccepted`: 운영진 승인 여부 (엔티티 필드 기본값 false, create()에서 별도 지정 불필요)

### PostReport DTO 노출 범위
- `reflection`, `nextPlan`, `reportFile`은 **상세보기(`ReportInfoDTO`)에서만 노출**
- 목록/미리보기(`PostReportPreviewDTO`)와 HWP 추출(`PostReportToHWPDTO`)에는 포함하지 않음

### ReportMember
- `report_member` 테이블에서 보고서와 참여 유저를 N:M 대신 별도 엔티티로 관리
- 컬럼: `report_member_id`, `report_id`, `user_id` — `member_id`는 레거시 컬럼이므로 DB에서 제거
- 수정 시 기존 멤버를 전부 삭제하고 재저장하는 방식 (`deleteByReportId` → `saveAll`)

### PostReport API (`/api/v1/report`)
| 메서드 | 경로 | 설명 | 권한 |
|--------|------|------|------|
| POST | `/` | 보고서 생성 (Post + PostReport 동시 생성) | 로그인 |
| GET | `/` | 보고서 목록 페이징 조회 | 누구나 |
| GET | `/{postId}` | 보고서 단건 조회 | 작성자 / 그룹 활성멤버 / 운영진 |
| PATCH | `/{postId}` | 보고서 수정 | 작성자 본인 |
| PATCH | `/{postId}/accept` | 보고서 승인 | ADMIN만 |
| GET | `/{postId}/export` | HWP 파일 추출 | ADMIN / MANAGER |
| GET | `/export/group/{groupId}` | 그룹 전체 보고서 ZIP 추출 | ADMIN / MANAGER |
| GET | `/group/{groupId}` | 그룹별 보고서 목록 조회 | 누구나 |

### 보고서 조회 권한 로직 (중요)
```
목록 조회:
- ADMIN / MANAGER → 전체 보고서 조회
- MEMBER → 본인이 ACTIVE 상태인 그룹의 보고서만 조회 (소속 그룹 없으면 빈 페이지)

단건 조회 시 아래 중 하나를 만족해야 접근 허용:
1. isAdmin (ROLE_ADMIN / ROLE_MANAGER)
2. isActiveMember (groupMember.status == ACTIVE && 해당 groupId)
※ 작성자 본인이라도 해당 그룹 ACTIVE 멤버가 아니면 접근 불가

수정(PATCH):
- 작성자 본인만 가능 (권한 무관)
```

### HWP 내보내기 구조
- `/templates/HWPTemplate.hwp` 파일을 템플릿으로 사용
- 플레이스홀더(`{authorName}`, `{date}`, `{location}`, `{content}`, `{name1}~{name30}` 등)를 문자열 치환
- S3에서 이미지 다운로드 → JPEG 압축 → HWP 표 안 이미지 교체
- 그룹 ZIP 추출: 개별 HWP 생성 실패 시 해당 항목만 skip

---

## Group (그룹)

Study/Project/Report 카테고리에서 사용.

```
cbu_groups: id, groupName, postId, category, status, recruitmentStatus, isDeleted
GroupMember: group_id FK, user_id, role(LEADER/MEMBER), status(ACTIVE/INACTIVE/PENDING)
```

- `GroupStatus`: PENDING / ACTIVE / REJECTED / RESUBMITTED / INACTIVE
- `GroupRecruitmentStatus`: OPEN / CLOSED
- 그룹 승인은 운영진이 처리 (approve/reject/resubmit)

---

## Comment (댓글)

- `post` 테이블과 연결 (`@ManyToOne`)
- `userId`로만 연결 (User 엔티티 직접 참조 없음)
- 대댓글 구조: `parentComment`로 자기참조 (`@ManyToOne`)
- Soft Delete 방식

### 삭제 권한
- 게시글/댓글 삭제는 **작성자 본인 또는 ADMIN/MANAGER**만 가능
- 권한 없으면 403 반환
- `PostService.softDeletePost`, `CommentService.deleteComment` 둘 다 동일한 로직으로 처리

### 익명 댓글 조회
- `GET /api/v1/post/{postId}/comment/anonymous` 엔드포인트는 **익명 자유게시글(`isAnonymous=true`)에서만 사용**
- 익명 게시글이 아닌 postId로 요청 시 400 반환
- 익명 응답(`CommentAnonymousInfoDTO`)은 작성자 정보(userId, name, generation)를 포함하지 않음
- 일반 게시글 댓글은 `GET /api/v1/post/{postId}/comment` 사용 (작성자 정보 포함)

---

## 신고 기능 (Flag)

자유게시판 게시글/댓글 신고 기능. 두 도메인이 같은 구조로 동작한다.

### FlagPost (게시글 신고)

- `flag_post` 테이블: `authorId`, `postId`, `content`, `isDeleted`
- 신고 생성: `POST /api/v1/post/{postId}/flag` — PostController에 위치 (로그인 유저)

| 메서드 | 경로 | 설명 | 권한 |
|--------|------|------|------|
| POST | `/api/v1/post/{postId}/flag` | 게시글 신고 생성 | 로그인 |
| GET | `/api/v1/flag/post` | 신고 목록 페이징 | ADMIN / MANAGER |
| GET | `/api/v1/flag/post/{flagPostId}` | 신고 단건 조회 | ADMIN / MANAGER |
| PATCH | `/api/v1/flag/post/{postId}/resolve` | 해당 게시글의 신고 일괄 처리(soft delete) | ADMIN / MANAGER |

### FlagComment (댓글 신고)

- `flag_comment` 테이블: `authorId`, `commentId`, `content`, `isDeleted`
- 신고 생성: `POST /api/v1/comment/{commentId}/flag` — CommentController에 위치 (로그인 유저)
- DTO 위치: `flagcomment/eto/CommentDTO` (패키지명이 `eto`임에 주의)

| 메서드 | 경로 | 설명 | 권한 |
|--------|------|------|------|
| POST | `/api/v1/comment/{commentId}/flag` | 댓글 신고 생성 | 로그인 |
| GET | `/api/v1/flag/comment` | 신고 목록 페이징 | ADMIN / MANAGER |
| GET | `/api/v1/flag/comment/{flagCommentId}` | 신고 단건 조회 | ADMIN / MANAGER |
| PATCH | `/api/v1/flag/comment/{commentId}/resolve` | 해당 댓글의 신고 일괄 처리(soft delete) | ADMIN / MANAGER |

### 신고 공통 패턴
- 신고 **생성** 엔드포인트는 각 대상의 컨트롤러에 위치 (PostController, CommentController)
- 신고 **조회/처리** 엔드포인트는 FlagPostController / FlagCommentController에 위치
- resolve는 특정 대상의 모든 신고를 soft delete 처리 (개별 삭제 아님)
- `FlagCommentInfoDTO`는 신고 내용 + 신고 대상 댓글 정보 + 댓글 작성자 정보 + 신고자 정보를 포함

---

## 공통 패턴

### DTO 패턴
- `PostDTO` 내부 클래스로 모든 Post 관련 DTO를 한 파일에서 관리
- 요청 DTO → Service에서 `PostCreateDTO` + 서브 DTO로 분리해 각각 처리
- `PostMapper` 컴포넌트가 엔티티 ↔ DTO 변환 담당

### 엔티티 패턴
- `@Setter` 사용 안 함 — 엔티티 변경은 도메인 메서드로 처리 (`changeXxx`, `delete`, `Accept` 등)
- 생성은 `static create()` 팩토리 메서드 사용
- Soft Delete: `isDeleted` + `deletedAt`

### 에러 처리
- `BaseException(ErrorCode)` / `CustomException` 사용
- `GlobalExceptionHandler`에서 일괄 처리
- 서비스에서 `ResponseStatusException` 던지면 컨트롤러에서 `BaseException`으로 래핑

### 인프라
- JWT 인증: `JwtFilter` → `Authentication`에서 `Long.parseLong(authentication.getName())`으로 userId 추출
- Redis: `RedisUtil` (이메일 인증 등)
- S3 파일 업로드: `image` 도메인 대신 **`file` 도메인**(`FileController`, `FileService`)에서 통합 처리
  - `POST /api/file/image` — 이미지 업로드 (jpeg/png/webp/gif/heic/heif, JPEG 압축 후 S3 저장)
  - `POST /api/file/pdf` — PDF 업로드 (PDF 한정, 최대 10MB, PDFBox 압축 후 S3 저장)
  - 업로드 후 반환된 S3 URL을 보고서 생성/수정 DTO의 `reportImage` / `reportFile`에 포함
- HWP 생성: `hwplib` 라이브러리 사용
