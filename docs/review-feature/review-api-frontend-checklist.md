# 복습 API 프론트엔드 체크리스트 및 합의안

> 스펙 문서: `review-api-spec-for-frontend.md` 기반 검토 결과

---

## 1. hint가 빈 문자열("")인 건 괜찮은가?

### 스펙 확인
- **현재**: `hint: string` (빈 문자열 `""`)
- **향후**: P1 이후에 AI로 핵심 단어 2-3개 생성 예정

### 권장 대응
```typescript
// 프론트에서 빈 힌트 처리
const displayHint = hint === "" ? null : hint;

// JSX
{displayHint && <HintBadge>{displayHint}</HintBadge>}
```

### 결론
- API 계약 변경 불필요 (현재 `string` 유지)
- 프론트: 빈 문자열이면 힌트 영역 숨김

---

## 2. difficulty가 MEDIUM 고정인 건 괜찮은가?

### 스펙 확인
- **현재**: `'EASY' | 'MEDIUM' | 'HARD'` 타입이지만 **MEDIUM 고정**
- **향후**: P2에서 AI 기반 난이도 분류 구현 예정

### 권장 대응
| 옵션 | 설명 |
|------|------|
| A. 노출 안 함 | 난이도 뱃지 자체를 숨김 (혼란 방지) |
| B. "기본" 표시 | MEDIUM일 때 "기본 난이도"로만 표시 |

```typescript
// 옵션 A: 숨김
// 난이도 뱃지 컴포넌트 자체를 렌더링하지 않음

// 옵션 B: 조건부 표시
const difficultyLabel = {
  EASY: '쉬움',
  MEDIUM: '기본',  // 또는 노출 안 함
  HARD: '어려움'
};
```

### 결론
- 타입은 현재대로 유지 (확장 대비)
- 프론트: Week1에서는 난이도 뱃지 **노출하지 않음** 권장

---

## 3. API 응답 구조 검증 체크리스트

### 3.1 GET /api/review/sentences 응답

| 항목 | 확인 결과 |
|------|----------|
| 응답 형태 | `{ sentences: [...], total: number }` 래퍼 |
| 배열 직접 반환? | **아니오**, 래퍼 객체 사용 |

```json
{
  "sentences": [...],
  "total": 15
}
```

### 3.2 날짜 필드 포맷

| 필드 | 포맷 | 예시 |
|------|------|------|
| `lastReviewedAt` | ISO 8601 (nullable) | `"2025-12-20T10:30:00"` 또는 `null` |
| `nextReviewDate` | YYYY-MM-DD | `"2025-12-27"` |
| `reviewDate` (요청) | ISO 8601 | `"2025-12-25T14:30:00"` |

```typescript
// 프론트 파싱 예시
const lastReviewed = lastReviewedAt 
  ? new Date(lastReviewedAt).toLocaleDateString() 
  : '복습 기록 없음';
```

### 3.3 POST /api/review/compare 응답

| 필드 | 설명 | 프론트 렌더링 |
|------|------|--------------|
| `overallFeedback` | 요약 1줄 | 결과 카드 상단에 표시 |
| `differences[]` | 항목별 피드백 (최대 3개) | 리스트로 펼쳐서 표시 |
| `tip` | 핵심 팁 | 하단 박스로 강조 |

```json
{
  "isCorrect": true,
  "score": 85,
  "differences": [
    {
      "type": "WORD_CHOICE",
      "userPart": "I will",
      "bestPart": "I'll",
      "explanation": "축약형이 더 자연스러워요",
      "importance": "MEDIUM"
    }
  ],
  "overallFeedback": "의미는 정확하지만, 축약형을 쓰면 더 자연스러워요!",
  "tip": "💡 Tip: by tomorrow = ~까지"
}
```

### 3.4 에러 포맷

```typescript
interface ErrorResponse {
  timestamp: string;    // "2025-12-25T14:30:00.123"
  status: number;       // 400, 401, 404, 500
  error: string;        // "Bad Request"
  message: string;      // "잘못된 요청입니다"
  path: string;         // "/api/review/compare"
}
```

| Status | 대응 |
|--------|------|
| 400 | 입력값 검증 메시지 표시 |
| 401 | 로그인 페이지로 리다이렉트 |
| 404 | "문장을 찾을 수 없습니다" 표시 |
| 500 | 재시도 버튼 제공 |

---

## 4. 프론트 플로우 관점 중요 계약

### 4.1 POST /api/review/compare (2~5초 소요)

#### 스펙 명시 사항
- 응답 시간: AI 처리로 인해 **2-5초** 소요
- 로딩 UI 필수
- 에러 핸들링: AI 장애 시 500 에러 가능

#### 권장 UX 패턴

```typescript
const { mutate, isPending, isError, reset } = useMutation({...});

// 제출 버튼 클릭 시
const handleSubmit = () => {
  setInputLocked(true);  // 입력 잠금
  mutate(request);
};
```

```tsx
// 로딩 UI
{isPending && (
  <LoadingOverlay>
    <Spinner />
    <p>AI가 분석 중입니다...</p>
    {/* 선택: 취소 버튼 */}
    <button onClick={() => { reset(); setInputLocked(false); }}>
      취소
    </button>
  </LoadingOverlay>
)}

// 에러 UI (폴백 제공)
{isError && (
  <ErrorCard>
    <p>비교에 실패했어요.</p>
    <button onClick={retry}>재시도</button>
    <button onClick={showBestAnswerOnly}>모범답안만 보기</button>
  </ErrorCard>
)}
```

#### 연타 방지
```typescript
<button 
  onClick={handleSubmit} 
  disabled={isPending || !userInput.trim()}
>
  {isPending ? '분석 중...' : '정답 확인'}
</button>
```

### 4.2 POST /api/review/records (비동기 저장)

#### 권장 패턴: Optimistic UI

```typescript
// compare 성공 후
onSuccess: (result) => {
  // 1. 결과 즉시 표시 (체감 속도 향상)
  showFeedback(result);
  
  // 2. 다음 문장으로 이동 준비
  // (사용자가 "다음" 버튼 누르면 바로 이동)
  
  // 3. 저장은 뒤에서 비동기로 (fire-and-forget)
  saveRecord({
    sentenceId,
    userAnswer,
    isCorrect: result.isCorrect,
    score: result.score,
    timeSpent,
    reviewDate: new Date().toISOString(),
  }, {
    onError: (error) => {
      // 실패 시 로컬 큐에 저장 후 나중에 재시도
      addToRetryQueue({ sentenceId, userAnswer, result, timeSpent });
      console.warn('기록 저장 실패, 재시도 예정:', error);
    }
  });
}
```

#### 로컬 큐 재시도 (선택 사항)
```typescript
// 앱 시작 시 또는 네트워크 복구 시
const retryQueue = getRetryQueue();
retryQueue.forEach(item => saveRecord(item));
```

---

## 5. 확정된 TypeScript 타입

```typescript
// ========== 요청 타입 ==========

interface CompareAnswerRequest {
  sentenceId: number;
  userAnswer: string;
  bestAnswer: string;
  korean: string;
}

interface SaveReviewRecordRequest {
  sentenceId: number;
  userAnswer: string;
  isCorrect: boolean;
  score: number;
  timeSpent: number;        // 초 단위
  reviewDate: string;       // ISO 8601
}

// ========== 응답 타입 ==========

interface ReviewSentencesResponse {
  sentences: ReviewSentence[];
  total: number;
}

interface ReviewSentence {
  id: number;
  korean: string;
  hint: string;                              // "" 가능
  bestAnswer: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';    // 현재 MEDIUM 고정
  lastReviewedAt: string | null;             // ISO 8601 또는 null
  reviewCount: number;
  nextReviewDate: string;                    // YYYY-MM-DD
}

interface CompareAnswerResponse {
  isCorrect: boolean;
  score: number;                             // 0-100
  differences: Difference[];                 // 최대 3개, 완벽하면 []
  overallFeedback: string;
  tip: string;
}

interface Difference {
  type: 'GRAMMAR' | 'WORD_CHOICE' | 'NATURALNESS' | 'PUNCTUATION';
  userPart: string;
  bestPart: string;
  explanation: string;
  importance: 'HIGH' | 'MEDIUM' | 'LOW';
}

interface SaveReviewRecordResponse {
  success: boolean;
  nextReviewDate: string;                    // YYYY-MM-DD
}

// ========== 에러 타입 ==========

interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
```

---

## 6. 구현 체크리스트

### Phase 1: 기본 동작
- [ ] API 1로 문장 조회 및 화면 표시
- [ ] 사용자 입력 폼 구현
- [ ] API 2로 비교 요청 및 결과 표시
- [ ] API 3으로 기록 저장

### Phase 2: UX 개선
- [ ] 로딩 상태 UI (2~5초 대응)
- [ ] 입력 잠금 및 연타 방지
- [ ] 에러 핸들링 (재시도 버튼, 폴백)
- [ ] 복습 완료 후 다음 문장 자동 이동

### Phase 3: 고급 기능 (선택)
- [ ] 진도 표시 (3/10 완료)
- [ ] 점수 히스토리
- [ ] 오늘 복습할 문장 개수 배지
- [ ] 실패 시 로컬 큐 재시도

---

## 7. 결론 및 합의 사항

| 항목 | 합의 내용 |
|------|----------|
| `hint` | 빈 문자열 허용, 프론트에서 빈 값이면 숨김 처리 |
| `difficulty` | MEDIUM 고정, 프론트에서 난이도 뱃지 숨김 권장 |
| 응답 래퍼 | `{ sentences: [], total }` 형태 확정 |
| 날짜 포맷 | `lastReviewedAt`: ISO, `nextReviewDate`: YYYY-MM-DD |
| 에러 포맷 | `{ timestamp, status, error, message, path }` |
| compare 로딩 | 2~5초 대응 필수, 로딩 UI + 연타 방지 |
| records 저장 | 비동기 fire-and-forget, 실패 시 로컬 큐 |

---

*작성일: 2026-01-06*
*기준 스펙: review-api-spec-for-frontend.md*
