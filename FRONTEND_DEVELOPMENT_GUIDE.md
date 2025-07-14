# WriteBuddy 백엔드 API 가이드

**WriteBuddy**: AI 기반 영어 문법 교정 서비스
- **백엔드**: Spring Boot + Kotlin (`http://localhost:7071`)
- **인증**: Google OAuth2
- **핵심 기능**: 문법 교정, 통계 대시보드

## 🚀 핵심 API

### 1. 교정 API
```http
POST /corrections
Content-Type: application/json

{
  "originSentence": "I want to learn English good"
}
```

**응답:**
```json
{
  "id": 1,
  "originSentence": "원문",
  "correctedSentence": "교정문", 
  "feedback": "친근한 톤의 피드백",
  "feedbackType": "GRAMMAR|SPELLING|STYLE|PUNCTUATION",
  "score": 7,
  "isFavorite": false,
  "memo": null,
  "createdAt": "2025-07-13T10:30:00",
  "originTranslation": "원문 번역",
  "correctedTranslation": "교정문 번역",
  "relatedExamples": [실제_사용_예시_배열]
}
```

### 2. 통계 API
```http
GET /statistics
```

**응답:**
```json
{
  "correctionStatistics": {
    "feedbackTypeStatistics": {"GRAMMAR": 15, "SPELLING": 8},
    "averageScore": 7.2
  },
  "dashboardData": {
    "dailyStatistics": {
      "totalCorrections": 25,
      "averageScore": 7.8,
      "feedbackTypes": {"GRAMMAR": 15, "SPELLING": 6}
    },
    "scoreTrend": [{"order": 1, "score": 6, "feedbackType": "GRAMMAR"}],
    "errorPatterns": {"GRAMMAR": ["i am student", "she don't like"]}
  },
  "generatedAt": "2025-07-13T14:30:00"
}

// TODO: 사용자 ID 기능 추가 시 userStatistics 구현 예정
```

### 3. 사용자 인증 API
```http
GET /auth/status     # 로그인 상태 확인
GET /auth/user       # 현재 사용자 정보
POST /logout         # 로그아웃
GET /oauth2/authorization/google  # Google 로그인
```

### 4. 즐겨찾기 & 노트
```http
PUT /corrections/{id}/favorite    # 즐겨찾기 토글
PUT /corrections/{id}/memo        # 노트 수정
GET /corrections/favorites        # 즐겨찾기 목록
```

## 📊 데이터 모델

### Correction (교정 결과)
```typescript
interface Correction {
  id: number;
  originSentence: string;
  correctedSentence: string;
  feedback: string;
  feedbackType: 'GRAMMAR' | 'SPELLING' | 'STYLE' | 'PUNCTUATION';
  score: number | null;  // 1-10점
  isFavorite: boolean;
  memo: string | null;
  createdAt: string;
  originTranslation: string | null;
  correctedTranslation: string | null;
  relatedExamples: RealExample[];
}
```

### User (사용자)
```typescript
interface User {
  id: number;
  username: string;
  email: string;
  profileImageUrl?: string;
  createdAt: string;
}
```

## 🔧 개발 설정

### 환경변수
```bash
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
```

### HTTP 상태 코드
- `200`: 성공
- `400`: 잘못된 요청
- `404`: 리소스 없음
- `500`: 서버 오류

### 에러 응답 형식
```json
{
  "timestamp": "2025-07-13T10:30:00",
  "status": 400,
  "error": "Bad Request", 
  "message": "문장은 1-1000자여야 합니다",
  "path": "/corrections"
}
```

## 💡 개발 팁

### API 에러 핸들링
```javascript
async function getStatistics() {
  try {
    const response = await fetch('/statistics');
    return await response.json();
  } catch (error) {
    console.warn('통계 API 실패');
    return null;
  }
}
```

---

## 📋 백엔드 준비사항

- [x] ✅ 프로젝트 컴파일 및 빌드 성공
- [x] ✅ 통계 API 구현 (`/statistics`) - **교정 데이터 기반 통계 조회**
- [x] ✅ 데이터베이스 쿼리 최적화 (성능 향상)
- [x] ✅ 연결 풀 최적화 (Supabase 프리티어 대응)

> **핵심**: 이 API 정보로 WriteBuddy 프론트엔드를 구현할 수 있습니다.