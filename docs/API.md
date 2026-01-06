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

## 단어/문법 검색 API (GPT 기반)

### 단어 검색
GPT-4o-mini를 사용하여 단어/표현의 뜻과 예문을 실시간 생성

```http
GET /words/search?keyword=deploy

→ 200 OK
{
  "word": "deploy",
  "meaning": "코드나 애플리케이션을 서버에 배포하다. 개발 완료된 기능을 실제 환경에 적용하는 것을 의미한다.",
  "examples": [
    {
      "sentence": "We need to deploy the hotfix to production immediately.",
      "translation": "핫픽스를 프로덕션에 즉시 배포해야 합니다."
    },
    {
      "sentence": "The CI/CD pipeline will automatically deploy after tests pass.",
      "translation": "테스트 통과 후 CI/CD 파이프라인이 자동으로 배포합니다."
    }
  ],
  "tips": "deploy to (환경명) 형태로 자주 사용됨",
  "isAiGenerated": true
}
```

### 문법/표현 검색
문법 규칙, 사용법, 흔한 실수, 대체 표현까지 제공

```http
GET /words/grammar/search?keyword=would you like me to

→ 200 OK
{
  "expression": "would you like me to",
  "explanation": "상대방에게 정중하게 도움이나 행동을 제안할 때 사용하는 표현. 'Do you want me to'보다 더 공손한 표현이다.",
  "usage": "코드리뷰, PR 코멘트, 슬랙 대화에서 동료에게 도움을 제안할 때 적합",
  "examples": [
    {
      "sentence": "Would you like me to review your PR?",
      "translation": "제가 PR 리뷰해 드릴까요?"
    },
    {
      "sentence": "Would you like me to set up the dev environment for you?",
      "translation": "개발 환경을 세팅해 드릴까요?"
    }
  ],
  "commonMistakes": "'Would you like I to'는 잘못된 표현. 반드시 'me to'를 사용",
  "alternatives": ["Do you want me to", "Shall I", "Let me know if you'd like me to"],
  "isAiGenerated": true
}
```

## 복습 API

### 복습 문장 조회
즐겨찾기한 문장을 복습용으로 변환하여 조회. 다음 복습 날짜 순으로 정렬.

```http
GET /api/review/sentences?limit=10

→ 200 OK
{
  "sentences": [
    {
      "id": 1,
      "korean": "이 보고서를 내일까지 보내드릴게요",
      "hint": "",
      "bestAnswer": "I'll send you the report by tomorrow.",
      "difficulty": "MEDIUM",
      "lastReviewedAt": "2025-12-20T10:30:00",
      "reviewCount": 2,
      "nextReviewDate": "2025-12-27"
    }
  ],
  "total": 15
}
```

### 답변 비교 (AI)
사용자 답변과 모범 답안을 AI로 비교 분석. **응답 시간 2~5초** 소요.

```http
POST /api/review/compare
Content-Type: application/json

{
  "sentenceId": 1,
  "userAnswer": "I will send you the report tomorrow.",
  "bestAnswer": "I'll send you the report by tomorrow.",
  "korean": "이 보고서를 내일까지 보내드릴게요"
}

→ 200 OK
{
  "isCorrect": true,
  "score": 85,
  "differences": [
    {
      "type": "WORD_CHOICE",
      "userPart": "I will",
      "bestPart": "I'll",
      "explanation": "일상 대화에서는 축약형이 더 자연스러워요",
      "importance": "MEDIUM"
    }
  ],
  "overallFeedback": "의미는 정확하지만, 축약형을 쓰면 더 자연스러워요!",
  "tip": "by tomorrow = ~까지, tomorrow = 내일"
}
```

### 복습 기록 저장
복습 결과를 저장하고 다음 복습 날짜를 계산 (간격 반복 알고리즘).

```http
POST /api/review/records
Content-Type: application/json

{
  "sentenceId": 1,
  "userAnswer": "I will send you the report tomorrow.",
  "isCorrect": true,
  "score": 85,
  "timeSpent": 12,
  "reviewDate": "2025-12-25T14:30:00"
}

→ 200 OK
{
  "success": true,
  "nextReviewDate": "2025-12-28"
}
```

**간격 반복 알고리즘:**
- 정답: 3일 → 1주 → 2주 → 1개월
- 오답: 오늘 → 1일 → 3일

## 권한

| 엔드포인트 | 권한 |
|-----------|------|
| `/auth/**`, `/users/register`, `/health` | Public |
| `/corrections/**`, `/statistics`, `/words/**` | Public (개발용, 추후 USER로 변경) |
| `/api/review/**` | Public (개발용, 추후 USER로 변경) |
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
