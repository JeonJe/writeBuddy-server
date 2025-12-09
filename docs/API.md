# WriteBuddy API

**Base URL**: `http://localhost:7071`
**인증**: JWT Bearer Token

## 인증 API

### 회원가입
```http
POST /users/register
Content-Type: application/json

{"username": "johndoe", "email": "user@example.com", "password": "SecurePass123!"}

→ 201 Created
{"id": 1, "username": "johndoe", "email": "user@example.com", "roles": ["USER"]}
```

### 로그인
```http
POST /auth/login
Content-Type: application/json

{"email": "user@example.com", "password": "SecurePass123!"}

→ 200 OK
{"accessToken": "eyJ...", "refreshToken": "eyJ...", "tokenType": "Bearer"}
```

### 토큰 갱신
```http
POST /auth/refresh
Content-Type: application/json

{"refreshToken": "eyJ..."}

→ 200 OK
{"accessToken": "새토큰", "refreshToken": "기존토큰", "tokenType": "Bearer"}
```

### 로그아웃
```http
POST /auth/logout
Authorization: Bearer <accessToken>

→ 204 No Content
```

## 교정 API

### 문장 교정
```http
POST /corrections
Authorization: Bearer <accessToken>
Content-Type: application/json

{"originSentence": "I goes to school yesterday"}

→ 200 OK
{
  "id": 1,
  "originSentence": "I goes to school yesterday",
  "correctedSentence": "I went to school yesterday",
  "feedback": "시제 일치 오류",
  "feedbackType": "GRAMMAR",
  "score": 8,
  "isFavorite": false,
  "createdAt": "2025-09-30T12:00:00"
}
```

### 교정 목록
```http
GET /corrections
Authorization: Bearer <accessToken>

→ 200 OK
[{id, originSentence, correctedSentence, score, feedbackType, createdAt}]
```

### 즐겨찾기 토글
```http
PATCH /corrections/{id}/favorite
Authorization: Bearer <accessToken>

→ 200 OK
{"id": 1, "isFavorite": true}
```

## 통계 API

### 통합 통계
```http
GET /statistics
Authorization: Bearer <accessToken>

→ 200 OK
{
  "correctionStatistics": {"feedbackTypeStatistics": {"GRAMMAR": 15}, "averageScore": 7.2},
  "dashboardData": {...}
}
```

## 권한

| 엔드포인트 | 권한 |
|-----------|------|
| `/auth/**`, `/users/register`, `/health` | Public |
| `/corrections/**`, `/statistics` | USER |
| `/admin/**` | ADMIN |

## 에러 응답

```json
{"status": 401, "error": "Unauthorized", "message": "유효하지 않은 토큰", "path": "/corrections"}
```

| 코드 | 의미 |
|------|------|
| 400 | 잘못된 요청 |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 500 | 서버 오류 |
