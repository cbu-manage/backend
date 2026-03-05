# 스터디 모집 API 명세서
---

## 목차

1. [인증 방식](#인증-방식)
2. [공통 응답 형식](#공통-응답-형식)
3. [에러 코드](#에러-코드)
4. [스터디 게시글 API](#스터디-게시글-api)
5. [그룹·신청 API](#그룹신청-api)
6. [버튼 분기 로직](#버튼-분기-로직)
7. [태그 시스템](#태그-시스템)
8. [제약사항·유효성 검사](#제약사항유효성-검사)
9. [전체 플로우 요약](#전체-플로우-요약)

---

## 인증 방식

- **쿠키 기반 JWT** — 로그인 후 발급된 `ACCESS_TOKEN` 쿠키가 요청에 자동 첨부됨
- 별도의 Authorization 헤더 불필요

| 구분 | 설명 |
|------|------|
| 로그인 필수 | 게시글 생성·수정·삭제, 모집 마감, 가입 신청·취소, 신청자 조회, 수락·거절 |
| 비로그인 허용 | 스터디 목록 조회, 상세 조회, 태그 필터 조회 |

---

## 공통 응답 형식

```json
{
  "code": "S-COMMON-SUCCESS",
  "message": "요청 성공",
  "data": { ... }
}
```

| code | HTTP | 의미 |
|------|------|------|
| `S-COMMON-SUCCESS` | 200 | 조회 성공 |
| `S-COMMON-CREATED` | 201 | 생성 성공 |
| `S-COMMON-UPDATED` | 200 | 수정·변경 성공 |
| `S-COMMON-DELETED` | 200 | 삭제 성공 |

---

## 에러 코드

| code | HTTP | 의미 | 발생 상황 |
|------|------|------|---------|
| `E-COMMON-0001` | 400 | 잘못된 요청 | 유효성 검사 실패, 마감된 스터디 재마감, ACTIVE 멤버 없이 마감 시도, 마감 후 studyName·maxMembers 수정 시도 |
| `E-AUTH-0001` | 401 | 인증 필요 | 쿠키 없음, 만료된 토큰 |
| `E-AUTH-0002` | 403 | 권한 없음 | 작성자가 아닌 사람이 수정·삭제·마감 시도, 팀장이 아닌 사람이 신청자 조회·수락·거절 시도 |
| `E-COMMON-0002` | 404 | 리소스 없음 | 존재하지 않는 postId·groupId, 삭제된 게시글 접근 |
| `E-COMMON-0005` | 409 | 이미 가입된 멤버 | PENDING 또는 ACTIVE 상태에서 중복 신청 |

---

## 스터디 게시글 API

---

### 1. 게시글 생성

```
POST /api/v1/post/study
```

**인증**: 로그인 필수
**설명**: 게시글 생성과 동시에 그룹이 자동 생성됩니다. 작성자가 팀장(LEADER/ACTIVE)으로 자동 등록됩니다.

#### Request Body

```json
{
  "title": "알고리즘 스터디 모집합니다",
  "content": "주 2회 알고리즘 문제풀이를 함께할 팀원을 모집합니다.",
  "studyTags": ["알고리즘", "코딩테스트", "Python"],
  "studyName": "알고리즘 마스터",
  "recruiting": true,
  "maxMembers": 4,
  "category": 1
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | String | ✅ | 게시글 제목 (최대 200자) |
| `content` | String | ✅ | 게시글 내용 |
| `studyTags` | String[] | ❌ | 자유 입력 태그 목록 (최대 10개) |
| `studyName` | String | ✅ | 스터디 이름 (최대 50자) |
| `recruiting` | boolean | ✅ | 모집 중 여부 — 생성 시 `true` 로 보내세요 |
| `maxMembers` | int | ✅ | 최대 모집 인원, 팀장 포함 (2 이상) |
| `category` | int | ✅ | **스터디: 반드시 `1`** |

#### Response `201`

```json
{
  "code": "S-COMMON-CREATED",
  "data": {
    "postId": 101,
    "groupId": 50,
    "authorId": 15,
    "authorGeneration": 34,
    "authorName": "홍길동",
    "title": "알고리즘 스터디 모집합니다",
    "content": "주 2회 알고리즘 문제풀이를 함께할 팀원을 모집합니다.",
    "studyTags": ["알고리즘", "코딩테스트", "Python"],
    "studyName": "알고리즘 마스터",
    "recruiting": true,
    "maxMembers": 4,
    "createdAt": "2026-03-02T10:00:00",
    "category": 1
  }
}
```

> ⚠️ **`postId`와 `groupId`를 반드시 저장하세요.** 수정·삭제·마감·가입 신청 등 모든 후속 API에서 사용됩니다.

---

### 2. 목록 조회

```
GET /api/v1/post/study?page=0&size=10&category=1
```

**인증**: 비로그인 허용
**정렬**: 최신순 (`createdAt` 내림차순)

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `page` | int | ✅ | 페이지 번호 (0부터 시작) |
| `size` | int | ✅ | 한 페이지 당 개수 |
| `category` | int | ✅ | **스터디: `1`** |

#### Response `200`

```json
{
  "code": "S-COMMON-SUCCESS",
  "data": {
    "content": [
      {
        "postId": 101,
        "title": "알고리즘 스터디 모집합니다",
        "studyTags": ["알고리즘", "코딩테스트", "Python"],
        "studyName": "알고리즘 마스터",
        "authorId": 15,
        "authorGeneration": 34,
        "authorName": "홍길동",
        "createdAt": "2026-03-02T10:00:00",
        "recruiting": true,
        "maxMembers": 4
      }
    ],
    "totalElements": 42,
    "totalPages": 5,
    "number": 0,
    "size": 10
  }
}
```

> Soft delete된 게시글은 자동 제외됩니다.

---

### 3. 내 게시글 목록

```
GET /api/v1/post/study/me?page=0&size=10&category=1
```

**인증**: 로그인 필수
**설명**: 로그인한 사용자가 작성한 게시글만 조회합니다.

Query Parameters·Response 구조는 [목록 조회](#2-목록-조회)와 동일합니다.

---

### 4. 상세 조회

```
GET /api/v1/post/study/{postId}
```

**인증**: 비로그인 허용
**설명**: 조회할 때마다 `viewCount`가 1씩 증가합니다.

#### Path Parameter

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `postId` | Long | 게시글 ID |

#### Response `200`

```json
{
  "code": "S-COMMON-SUCCESS",
  "data": {
    "postId": 101,
    "title": "알고리즘 스터디 모집합니다",
    "content": "주 2회 알고리즘 문제풀이를 함께할 팀원을 모집합니다.",
    "studyTags": ["알고리즘", "코딩테스트", "Python"],
    "studyName": "알고리즘 마스터",
    "authorId": 15,
    "authorGeneration": 34,
    "authorName": "홍길동",
    "createdAt": "2026-03-02T10:00:00",
    "recruiting": true,
    "maxMembers": 4,
    "groupId": 50,
    "isLeader": false,
    "hasApplied": false,
    "viewCount": 42
  }
}
```

> `isLeader`, `hasApplied` 값으로 버튼을 분기합니다 → [버튼 분기 로직](#버튼-분기-로직) 참고

---

### 5. 게시글 수정

```
PATCH /api/v1/post/study/{postId}
```

**인증**: 로그인 필수, **작성자 본인만 가능**
**설명**: 보내지 않은 필드는 기존 값을 유지합니다 (부분 수정 지원).

#### Request Body

```json
{
  "title": "[수정] 알고리즘 스터디",
  "content": "내용 수정",
  "studyTags": ["알고리즘", "Java"],
  "studyName": "알고리즘 고수반",
  "maxMembers": 6
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | String | ❌ | 생략 시 기존 값 유지 |
| `content` | String | ❌ | 생략 시 기존 값 유지 |
| `studyTags` | String[] | ❌ | 생략 시 기존 값 유지 (전체 교체됨, 빈 배열 `[]` 전송 시 태그 전체 삭제) |
| `studyName` | String | ❌ | 생략 시 기존 값 유지. **모집 마감 후 수정 불가** |
| `maxMembers` | Integer | ❌ | 생략 시 기존 값 유지. **모집 마감 후 수정 불가** |

**제약**:
- `recruiting`은 이 API로 변경 불가 → 모집 마감은 [마감 API](#8-모집-마감) 사용
- 모집 마감 후 `studyName`·`maxMembers` 수정 요청 시 **400**
- `maxMembers`를 현재 ACTIVE 멤버 수 이하로 변경 시 **400**

#### Response `200`

```json
{ "code": "S-COMMON-UPDATED", "data": null }
```

---

### 6. 게시글 삭제

```
DELETE /api/v1/post/study/{postId}
```

**인증**: 로그인 필수, **작성자 본인만 가능**
**설명**: Soft Delete 처리됩니다. 게시글 + 연결 그룹이 함께 삭제되며 목록·상세 조회에서 제외됩니다.

| 에러 상황 | HTTP | code |
|---------|------|------|
| 이미 삭제된 게시글 재삭제 | 404 | `E-COMMON-0002` |
| 작성자가 아닌 사람 요청 | 403 | `E-AUTH-0002` |

#### Response `200`

```json
{ "code": "S-COMMON-DELETED", "data": null }
```

---

### 7. 태그 필터 조회

```
GET /api/v1/post/study/filter?page=0&size=10&tag=알고리즘
```

**인증**: 비로그인 허용
**설명**: 태그명이 **정확히 일치**하는 게시글만 반환합니다. 부분 일치는 지원하지 않습니다.

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `page` | int | ✅ | 페이지 번호 |
| `size` | int | ✅ | 페이지 당 개수 |
| `tag` | String | ✅ | 검색할 태그 (완전 일치) |

Response 구조는 [목록 조회](#2-목록-조회)와 동일합니다.

---

### 8. 모집 마감

```
POST /api/v1/post/study/{postId}/close
```

**인증**: 로그인 필수, **팀장(작성자)만 가능**

마감 시 자동으로 수행되는 동작:
1. PENDING 상태 신청자 전원 **자동 거절 (REJECTED)**
2. 그룹 모집 상태 → **CLOSED**
3. 스터디 `recruiting` → **false**

**마감 가능 조건**: 팀장 외 ACTIVE 멤버가 **1명 이상** 있어야 합니다.

| 에러 상황 | HTTP | code |
|---------|------|------|
| 이미 마감된 스터디 재마감 | 400 | `E-COMMON-0001` |
| ACTIVE 멤버가 팀장뿐 (0명) | 400 | `E-COMMON-0001` |
| 팀장이 아닌 사람 요청 | 403 | `E-AUTH-0002` |

#### Response `200`

```json
{ "code": "S-COMMON-UPDATED", "data": null }
```

---

## 그룹·신청 API

> 스터디 게시글 생성 시 그룹이 **자동 생성**됩니다. 별도로 그룹을 만들 필요 없습니다.
> 상세 조회 응답의 `groupId` 필드를 사용하세요.

---

### 9. 가입 신청

```
POST /api/v1/groups/{groupId}/members
```

**인증**: 로그인 필수

| 상황 | 결과 |
|------|------|
| 신규 신청 | PENDING 상태로 생성 |
| REJECTED 후 재신청 | 기존 레코드를 PENDING으로 복원 |
| PENDING·ACTIVE 상태에서 재신청 | **409** `E-COMMON-0005` |
| 마감(CLOSED) 그룹에 신청 | **400** `E-COMMON-0001` |

#### Response `201`

```json
{
  "code": "S-COMMON-CREATED",
  "data": {
    "groupMemberId": 200,
    "userId": 20,
    "userName": "김철수",
    "grade": 3,
    "major": "컴퓨터공학과",
    "groupMemberRole": "MEMBER",
    "groupMemberStatus": "PENDING",
    "createdAt": "2026-03-02T11:00:00"
  }
}
```

> ⚠️ **`groupMemberId`를 저장하세요.** 수락·거절 API에서 사용됩니다.

---

### 10. 가입 신청 취소

```
DELETE /api/v1/groups/{groupId}/members/me
```

**인증**: 로그인 필수
**설명**: 본인의 PENDING 상태 신청을 취소합니다.

| 에러 상황 | HTTP | code |
|---------|------|------|
| ACTIVE 상태에서 취소 시도 | 400 | `E-COMMON-0001` |

#### Response `200`

```json
{ "code": "S-COMMON-UPDATED", "data": null }
```

---

### 11. 신청자 목록 조회 (팀장 전용)

```
GET /api/v1/groups/{groupId}/applicants
```

**인증**: 로그인 필수, **팀장만 가능**
**설명**: PENDING 상태 신청자 목록을 조회합니다.

| 에러 상황 | HTTP | code |
|---------|------|------|
| 팀장이 아닌 사람 요청 | 403 | `E-AUTH-0002` |

#### Response `200`

```json
{
  "code": "S-COMMON-SUCCESS",
  "data": [
    {
      "groupMemberId": 200,
      "userId": 20,
      "userName": "김철수",
      "grade": 3,
      "major": "컴퓨터공학과",
      "groupMemberRole": "MEMBER",
      "groupMemberStatus": "PENDING",
      "createdAt": "2026-03-02T11:00:00"
    }
  ]
}
```

---

### 12. 신청 수락 / 거절 (팀장 전용)

```
PATCH /api/v1/groups/members/{groupMemberId}/applicant
```

**인증**: 로그인 필수, **팀장만 가능**

#### Request Body

```json
{ "action": "ACCEPT" }
```

| `action` 값 | 상태 변화 | 결과 |
|------------|---------|------|
| `ACCEPT` | PENDING → **ACTIVE** | 멤버로 승인 |
| `REJECT` | PENDING → **REJECTED** | 거절 (재신청 가능) |

> ACTIVE 인원이 `maxMembers`에 도달하면 자동으로 모집이 마감됩니다.

#### Response `200`

```json
{ "code": "S-COMMON-UPDATED", "data": null }
```

---

### 13. 그룹 상세 조회

```
GET /api/v1/groups/{groupId}
```

**인증**: 로그인 필수

#### Response `200`

```json
{
  "code": "S-COMMON-SUCCESS",
  "data": {
    "groupId": 50,
    "groupName": "알고리즘 마스터 #101",
    "createdAt": "2026-03-02T10:00:00",
    "updatedAt": "2026-03-02T11:00:00",
    "activeMemberCount": 2,
    "maxActiveMembers": 4,
    "minActiveMembers": 1,
    "groupRecruitmentStatus": "OPEN",
    "groupStatus": "INACTIVE",
    "members": [
      {
        "groupMemberId": 100,
        "userId": 15,
        "userName": "홍길동",
        "grade": 4,
        "major": "소프트웨어학과",
        "groupMemberRole": "LEADER",
        "groupMemberStatus": "ACTIVE",
        "createdAt": "2026-03-02T10:00:00"
      }
    ]
  }
}
```

| 필드 | 설명 |
|------|------|
| `activeMemberCount` | ACTIVE 상태 멤버 수만 카운트 (PENDING 제외) |
| `groupRecruitmentStatus` | `OPEN`: 모집 중 / `CLOSED`: 마감 |
| `groupStatus` | `ACTIVE`: 활동 중 / `INACTIVE`: 비활동 (관리자가 변경) |
| `groupMemberRole` | `LEADER`: 팀장 / `MEMBER`: 일반 멤버 |
| `groupMemberStatus` | `ACTIVE`: 승인 / `PENDING`: 대기 / `REJECTED`: 거절 / `INACTIVE`: 비활동 |

---

## 버튼 분기 로직

스터디 상세 조회 응답의 `isLeader`, `hasApplied` 두 값으로 버튼을 결정합니다.

```
isLeader = true
└── "신청 인원 확인" 버튼 표시
    └── 클릭 시 GET /api/v1/groups/{groupId}/applicants 호출

isLeader = false
├── hasApplied = true   → PENDING 상태 (신청 대기 중)
│   └── "신청 취소하기" 버튼 표시
│       └── 클릭 시 DELETE /api/v1/groups/{groupId}/members/me 호출
│
├── hasApplied = false  → 미신청 상태 (비로그인 포함, REJECTED 후 재신청 가능)
│   └── "신청하기" 버튼 표시
│       └── 클릭 시 POST /api/v1/groups/{groupId}/members 호출
│           (비로그인 상태면 로그인 페이지로 유도)
│
└── hasApplied = null   → 이미 ACTIVE 멤버 (승인 완료)
    └── "가입 완료" 표시 (버튼 비활성화)
```

---

## 태그 시스템

스터디 태그는 **자유 입력 문자열**입니다. 서버에 고정 목록이 없습니다.

| 기능 | API |
|------|-----|
| 태그 포함 목록 조회 (항상 포함) | `GET /post/study?category=1` |
| 특정 태그로 필터링 | `GET /post/study/filter?tag=알고리즘` |

> 태그 필터는 **완전 일치** 검색입니다. `"알고"` 로 검색하면 `"알고리즘"` 은 나오지 않습니다.

---

## 제약사항·유효성 검사

| 필드 | 제약 조건 |
|------|---------|
| `title` | 필수, 1자 이상, 최대 200자 |
| `content` | 필수, 1자 이상 |
| `studyName` | 필수, 1자 이상, 최대 50자 |
| `maxMembers` | 필수, 2 이상 (팀장 포함) |
| `studyTags` | 선택, 최대 10개 |
| `category` | 스터디 생성 시 반드시 `1` |

유효성 검사 실패 응답:
```json
{
  "code": "E-COMMON-0001",
  "message": "잘못된 요청",
  "data": null
}
```

---

## 전체 플로우 요약

```
[팀장 플로우]
1. POST  /api/v1/login                              → 로그인
2. POST  /api/v1/post/study                         → 게시글·그룹 생성 (postId, groupId 저장)
3. GET   /api/v1/post/study/{postId}                → 게시글 확인
4. GET   /api/v1/groups/{groupId}/applicants        → 신청자 목록 확인 (PENDING 목록)
5. PATCH /api/v1/groups/members/{groupMemberId}/applicant  → 수락(ACCEPT) 또는 거절(REJECT)
6. POST  /api/v1/post/study/{postId}/close          → 모집 마감

[참가자 플로우]
1. GET   /api/v1/post/study?category=1              → 목록 조회 (비로그인 가능)
2. GET   /api/v1/post/study/{postId}                → 상세 확인 (비로그인 가능, hasApplied=false)
3. POST  /api/v1/login                              → 로그인
4. POST  /api/v1/groups/{groupId}/members           → 가입 신청 (groupMemberId 저장)
   └── 취소하려면: DELETE /api/v1/groups/{groupId}/members/me
5. 팀장이 수락하면 → 다음 상세 조회 시 hasApplied=null (가입 완료)
```
