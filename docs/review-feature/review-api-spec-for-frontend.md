# 복습 기능 API 스펙 (프론트엔드용)

## 개요
즐겨찾기한 문장을 복습용으로 변환하고, 사용자 답변을 AI로 비교 분석하는 3개 API

**Base URL**: `http://localhost:7071/api/review`

**인증**: 모든 API는 JWT Bearer Token 필요

---

## 1. 복습 문장 조회 API

### Endpoint
```
GET /api/review/sentences?limit={개수}
```

### Query Parameters
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| limit | number | No | 10 | 조회할 문장 개수 (최대 50) |

### Response (200 OK)
```json
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

### TypeScript 타입
```typescript
interface ReviewSentencesResponse {
  sentences: ReviewSentence[];
  total: number;
}

interface ReviewSentence {
  id: number;                      // 원본 Correction ID
  korean: string;                  // 한국어 문장
  hint: string;                    // 힌트 (현재 빈 문자열, 추후 확장)
  bestAnswer: string;              // 모범 답안
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';  // 난이도 (현재 MEDIUM 고정)
  lastReviewedAt: string | null;   // ISO 8601 날짜 (복습 안 했으면 null)
  reviewCount: number;             // 총 복습 횟수
  nextReviewDate: string;          // 다음 복습 날짜 (YYYY-MM-DD)
}
```

### 사용 예시
```typescript
// React Query 예시
const { data } = useQuery({
  queryKey: ['reviewSentences', limit],
  queryFn: async () => {
    const response = await fetch(
      `${API_BASE}/api/review/sentences?limit=${limit}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    if (!response.ok) throw new Error('Failed to fetch');
    return response.json() as ReviewSentencesResponse;
  },
});
```

### 정렬 순서
- `nextReviewDate`가 오늘 이전인 것 우선 (복습할 문장)
- 그 다음 오래된 것부터

---

## 2. 내 답변 비교 API ⭐ (가장 중요)

### Endpoint
```
POST /api/review/compare
```

### Request Body
```json
{
  "sentenceId": 1,
  "userAnswer": "I will send you the report tomorrow.",
  "bestAnswer": "I'll send you the report by tomorrow.",
  "korean": "이 보고서를 내일까지 보내드릴게요"
}
```

### Response (200 OK)
```json
{
  "isCorrect": true,
  "score": 85,
  "differences": [
    {
      "type": "WORD_CHOICE",
      "userPart": "I will",
      "bestPart": "I'll",
      "explanation": "일상 대화에서는 축약형(I'll)이 더 자연스러워요",
      "importance": "MEDIUM"
    },
    {
      "type": "NATURALNESS",
      "userPart": "tomorrow",
      "bestPart": "by tomorrow",
      "explanation": "'by tomorrow'는 마감 기한을 명확히 표현해요",
      "importance": "HIGH"
    }
  ],
  "overallFeedback": "의미는 정확하지만, 축약형을 쓰면 더 자연스러워요!",
  "tip": "💡 Tip: by tomorrow = ~까지, tomorrow = 내일"
}
```

### TypeScript 타입
```typescript
interface CompareAnswerRequest {
  sentenceId: number;
  userAnswer: string;
  bestAnswer: string;
  korean: string;
}

interface CompareAnswerResponse {
  isCorrect: boolean;             // 의미상 맞는지
  score: number;                  // 0-100 점수
  differences: Difference[];       // 차이점 배열 (최대 3개)
  overallFeedback: string;        // 전체 피드백
  tip: string;                    // 핵심 팁
}

interface Difference {
  type: 'GRAMMAR' | 'WORD_CHOICE' | 'NATURALNESS' | 'PUNCTUATION';
  userPart: string;               // 사용자가 쓴 부분
  bestPart: string;               // 모범 답안 부분
  explanation: string;            // 설명 (한국어)
  importance: 'HIGH' | 'MEDIUM' | 'LOW';  // 중요도
}
```

### 사용 예시
```typescript
const { mutate: compareAnswer, isPending } = useMutation({
  mutationFn: async (request: CompareAnswerRequest) => {
    const response = await fetch(`${API_BASE}/api/review/compare`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(request),
    });
    if (!response.ok) throw new Error('Failed to compare');
    return response.json() as CompareAnswerResponse;
  },
  onSuccess: (data) => {
    console.log('점수:', data.score);
    console.log('피드백:', data.overallFeedback);
  },
});

// 사용
compareAnswer({
  sentenceId: 1,
  userAnswer: myAnswer,
  bestAnswer: sentence.bestAnswer,
  korean: sentence.korean,
});
```

### ⚠️ 주의사항
- **응답 시간**: AI 처리로 인해 **2-5초** 소요
- **로딩 UI 필수**: `isPending` 상태로 로딩 표시 필요
- **에러 핸들링**: AI 장애 시 500 에러 가능 → 재시도 버튼 권장

---

## 3. 복습 기록 저장 API

### Endpoint
```
POST /api/review/records
```

### Request Body
```json
{
  "sentenceId": 1,
  "userAnswer": "I will send you the report tomorrow.",
  "isCorrect": true,
  "score": 85,
  "timeSpent": 12,
  "reviewDate": "2025-12-25T14:30:00"
}
```

### Response (200 OK)
```json
{
  "success": true,
  "nextReviewDate": "2025-12-28"
}
```

### TypeScript 타입
```typescript
interface SaveReviewRecordRequest {
  sentenceId: number;
  userAnswer: string;
  isCorrect: boolean;              // API 2에서 받은 결과
  score: number;                   // API 2에서 받은 점수
  timeSpent: number;               // 소요 시간 (초)
  reviewDate: string;              // ISO 8601 (보통 new Date().toISOString())
}

interface SaveReviewRecordResponse {
  success: boolean;
  nextReviewDate: string;          // 다음 복습 날짜 (YYYY-MM-DD)
}
```

### 사용 예시
```typescript
// API 2 (비교) 완료 후 자동으로 API 3 (저장) 호출
const { mutate: saveRecord } = useMutation({
  mutationFn: async (request: SaveReviewRecordRequest) => {
    const response = await fetch(`${API_BASE}/api/review/records`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(request),
    });
    if (!response.ok) throw new Error('Failed to save');
    return response.json() as SaveReviewRecordResponse;
  },
});

// 비교 API 성공 후 자동 저장
compareAnswer(request, {
  onSuccess: (compareResult) => {
    saveRecord({
      sentenceId: request.sentenceId,
      userAnswer: request.userAnswer,
      isCorrect: compareResult.isCorrect,
      score: compareResult.score,
      timeSpent: elapsedSeconds,
      reviewDate: new Date().toISOString(),
    });
  },
});
```

---

## 전체 Flow 예시

### 복습 화면 진입 시
```typescript
// 1. 복습할 문장 조회
const { data: sentences } = useQuery(['reviewSentences'], fetchSentences);

// 2. 첫 번째 문장 표시
const [currentIndex, setCurrentIndex] = useState(0);
const currentSentence = sentences?.sentences[currentIndex];
```

### 사용자가 답변 제출 시
```typescript
const [startTime] = useState(Date.now());

// 1. 답변 비교 API 호출
compareAnswer({
  sentenceId: currentSentence.id,
  userAnswer: userInput,
  bestAnswer: currentSentence.bestAnswer,
  korean: currentSentence.korean,
}, {
  onSuccess: (result) => {
    // 2. 결과 표시
    showFeedback(result);

    // 3. 복습 기록 저장
    const timeSpent = Math.floor((Date.now() - startTime) / 1000);
    saveRecord({
      sentenceId: currentSentence.id,
      userAnswer: userInput,
      isCorrect: result.isCorrect,
      score: result.score,
      timeSpent,
      reviewDate: new Date().toISOString(),
    });
  },
});
```

---

## 에러 응답

### 공통 에러 형식
```json
{
  "timestamp": "2025-12-25T14:30:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "잘못된 요청입니다",
  "path": "/api/review/compare"
}
```

### 주요 에러 코드
| Status | Error | 설명 | 대응 |
|--------|-------|------|------|
| 400 | Bad Request | 필수 필드 누락, 잘못된 형식 | 입력값 검증 |
| 401 | Unauthorized | JWT 토큰 없음/만료 | 로그인 페이지로 이동 |
| 404 | Not Found | 문장 ID 없음 | 에러 메시지 표시 |
| 500 | Internal Server Error | AI 장애, DB 오류 | 재시도 버튼 제공 |

---

## 주의사항 & FAQ

### Q1. `hint` 필드가 항상 빈 문자열인가요?
**A**: 네, 현재 P0에서는 빈 문자열입니다. P1 이후에 AI로 핵심 단어 2-3개 생성 예정입니다.

### Q2. `difficulty`가 항상 `MEDIUM`인가요?
**A**: 네, 현재는 고정값입니다. P2에서 AI 기반 난이도 분류 구현 예정입니다.

### Q3. API 2 (비교) 응답이 느린데 어떻게 하나요?
**A**: AI 처리로 2-5초 소요됩니다. 로딩 UI를 꼭 넣어주세요. 예:
```tsx
{isPending && <div>AI가 분석 중입니다... 🤖</div>}
```

### Q4. 복습 기록을 저장하지 않고 비교만 할 수 있나요?
**A**: 네, API 2만 호출하면 됩니다. API 3은 선택 사항입니다.

### Q5. `nextReviewDate`는 어떻게 계산되나요?
**A**: 간격 반복 알고리즘:
- **맞음**: 3일 → 1주 → 2주 → 1개월
- **틀림**: 오늘 → 1일 → 3일

### Q6. 즐겨찾기하지 않은 문장도 복습할 수 있나요?
**A**: 아니요, API 1은 `isFavorite == true`인 문장만 반환합니다.

### Q7. `differences` 배열이 비어있을 수 있나요?
**A**: 완벽한 답변이면 빈 배열일 수 있습니다. 최대 3개까지 반환됩니다.

---

## 구현 우선순위 권장

### Phase 1 (기본 동작)
1. API 1로 문장 조회 → 화면에 표시
2. 사용자 입력 → API 2로 비교 → 결과 표시
3. API 3으로 기록 저장

### Phase 2 (UX 개선)
4. 로딩 상태 UI (API 2는 2-5초 소요)
5. 에러 핸들링 (재시도 버튼)
6. 복습 완료 후 다음 문장으로 자동 이동

### Phase 3 (선택 사항)
7. 진도 표시 (3/10 완료)
8. 점수 히스토리 그래프
9. 오늘 복습할 문장 개수 표시

---

## 테스트용 cURL 예시

### API 1 (문장 조회)
```bash
curl -X GET "http://localhost:7071/api/review/sentences?limit=5" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### API 2 (답변 비교)
```bash
curl -X POST "http://localhost:7071/api/review/compare" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "sentenceId": 1,
    "userAnswer": "I will go there",
    "bestAnswer": "I'\''ll go there",
    "korean": "나는 거기 갈게요"
  }'
```

### API 3 (기록 저장)
```bash
curl -X POST "http://localhost:7071/api/review/records" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "sentenceId": 1,
    "userAnswer": "I will go there",
    "isCorrect": true,
    "score": 85,
    "timeSpent": 12,
    "reviewDate": "2025-12-25T14:30:00"
  }'
```

---

## 변경 예정 사항 (참고용)

추후 P1/P2에서 추가될 기능:
- `hint` 필드: AI로 핵심 단어 자동 생성
- `difficulty`: AI 기반 난이도 분류
- 통계 API: 오늘/이번 주 복습 통계
- 설정 API: 하루 목표 개수, 난이도 필터

---

**문의사항이나 수정 요청은 언제든지 연락주세요!**
