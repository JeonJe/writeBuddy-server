# 복습 기능 구현 현황

> 작성일: 2026-01-06

## 구현 완료 항목

### 1. 도메인 모델

| 파일 | 설명 | 상태 |
|------|------|------|
| `domain/ReviewRecord.kt` | 복습 기록 엔티티 (JPA) | ✅ 완료 |
| `domain/ReviewDifficulty.kt` | 난이도 Enum (EASY, MEDIUM, HARD) | ✅ 완료 |
| `domain/AnswerComparison.kt` | AI 비교 결과 도메인 | ✅ 완료 |

### 2. Repository

| 파일 | 설명 | 상태 |
|------|------|------|
| `repository/ReviewRecordRepository.kt` | 복습 기록 리포지토리 | ✅ 완료 |
| `repository/CorrectionRepository.kt` | 즐겨찾기 조회 쿼리 추가 | ✅ 완료 |

### 3. Service

| 파일 | 설명 | 상태 |
|------|------|------|
| `service/ReviewService.kt` | 복습 비즈니스 로직 | ✅ 완료 |
| `service/PromptManager.kt` | 답변 비교 프롬프트 추가 | ✅ 완료 |

### 4. Controller & DTO

| 파일 | 설명 | 상태 |
|------|------|------|
| `controller/ReviewController.kt` | 3개 API 엔드포인트 | ✅ 완료 |
| `dto/request/CompareAnswerRequest.kt` | 비교 요청 DTO | ✅ 완료 |
| `dto/request/SaveReviewRecordRequest.kt` | 저장 요청 DTO | ✅ 완료 |
| `dto/response/ReviewSentenceResponse.kt` | 문장 응답 DTO | ✅ 완료 |
| `dto/response/CompareAnswerResponse.kt` | 비교 응답 DTO | ✅ 완료 |
| `dto/response/SaveReviewRecordResponse.kt` | 저장 응답 DTO | ✅ 완료 |

---

## API 엔드포인트

### P0-1: 복습 문장 조회
```
GET /api/review/sentences?limit={개수}
```
- 즐겨찾기한 문장을 복습용으로 변환
- 다음 복습 날짜 순으로 정렬 (오늘 이전 우선)

### P0-2: 내 답변 비교
```
POST /api/review/compare
```
- AI로 사용자 답변과 모범 답안 비교
- 응답 시간: 2-5초 (AI 처리)

### P0-3: 복습 기록 저장
```
POST /api/review/records
```
- 복습 결과 저장
- 다음 복습 날짜 계산 (간격 반복 알고리즘)

---

## 간격 반복 알고리즘

| 정답 여부 | 횟수 | 다음 복습 |
|----------|------|----------|
| ✅ 맞음 | 1회 | +3일 |
| ✅ 맞음 | 2회 | +1주 |
| ✅ 맞음 | 3회 | +2주 |
| ✅ 맞음 | 4회+ | +1개월 |
| ❌ 틀림 | 1회 | 오늘 |
| ❌ 틀림 | 2회 | +1일 |
| ❌ 틀림 | 3회+ | +3일 |

---

## 빌드 상태

```bash
./gradlew compileKotlin  # ✅ 성공
./gradlew build          # ⚠️ 기존 RefreshTokenTest 1개 실패 (복습 기능과 무관)
```

---

## 테스트 방법

### cURL 예시

```bash
# 복습 문장 조회
curl -X GET "http://localhost:7071/api/review/sentences?limit=5"

# 답변 비교
curl -X POST "http://localhost:7071/api/review/compare" \
  -H "Content-Type: application/json" \
  -d '{
    "sentenceId": 1,
    "userAnswer": "I will send you the report tomorrow.",
    "bestAnswer": "I'\''ll send you the report by tomorrow.",
    "korean": "이 보고서를 내일까지 보내드릴게요"
  }'

# 기록 저장
curl -X POST "http://localhost:7071/api/review/records" \
  -H "Content-Type: application/json" \
  -d '{
    "sentenceId": 1,
    "userAnswer": "I will send you the report tomorrow.",
    "isCorrect": true,
    "score": 85,
    "timeSpent": 12,
    "reviewDate": "2026-01-06T14:30:00"
  }'
```

---

## 향후 작업 (Out of Scope)

- [ ] P1-1: hint 필드 AI 자동 생성
- [ ] P1-2: 복습 통계 API
- [ ] P2: AI 기반 난이도 분류
- [ ] 성능 최적화 (N+1 쿼리)

---

## 파일 구조

```
src/main/kotlin/com/writebuddy/writebuddy/
├── domain/
│   ├── ReviewRecord.kt          # NEW
│   ├── ReviewDifficulty.kt      # NEW
│   └── AnswerComparison.kt      # NEW
├── repository/
│   ├── ReviewRecordRepository.kt # NEW
│   └── CorrectionRepository.kt   # MODIFIED
├── service/
│   ├── ReviewService.kt         # NEW
│   └── PromptManager.kt         # MODIFIED
└── controller/
    ├── ReviewController.kt      # NEW
    └── dto/
        ├── request/
        │   ├── CompareAnswerRequest.kt      # NEW
        │   └── SaveReviewRecordRequest.kt   # NEW
        └── response/
            ├── ReviewSentenceResponse.kt    # NEW
            ├── CompareAnswerResponse.kt     # NEW
            └── SaveReviewRecordResponse.kt  # NEW

docs/review-feature/
├── review-api-spec-for-frontend.md      # 프론트엔드용 API 스펙
├── review-api-frontend-checklist.md     # 프론트 체크리스트
├── review-feature-p0-plan.md            # P0 구현 계획서
└── IMPLEMENTATION-STATUS.md             # 이 파일
```
