# 복습 기능 P0 구현 계획서

## 목표
즐겨찾기한 문장을 복습용으로 변환하고, 사용자 답변을 AI로 비교 분석하는 핵심 복습 시스템 구축 (P0: 3개 API)

---

## 변경 범위

### 신규 파일
- `domain/ReviewSentence.kt` - 복습 문장 도메인 (Correction 기반 변환용 DTO)
- `domain/ReviewRecord.kt` - 복습 기록 엔티티 (JPA)
- `domain/ReviewDifficulty.kt` - 난이도 Enum (EASY, MEDIUM, HARD)
- `domain/AnswerComparison.kt` - AI 비교 결과 도메인
- `repository/ReviewRecordRepository.kt` - 복습 기록 리포지토리
- `service/ReviewService.kt` - 복습 비즈니스 로직
- `controller/ReviewController.kt` - 복습 API 엔드포인트
- `controller/dto/response/ReviewSentenceResponse.kt` - 복습 문장 응답 DTO
- `controller/dto/response/AnswerComparisonResponse.kt` - 비교 결과 응답 DTO
- `controller/dto/request/CompareAnswerRequest.kt` - 비교 요청 DTO
- `controller/dto/request/SaveReviewRecordRequest.kt` - 기록 저장 요청 DTO

### 수정 파일
- `service/OpenAiClient.kt` - AI 답변 비교 메서드 추가
- `service/PromptManager.kt` - 답변 비교용 프롬프트 추가

### 테스트 파일
- `domain/ReviewRecordTest.kt` - 도메인 모델 테스트 (간격 반복 로직)

---

## 현재 상태 분석

### 기존 Correction 엔티티 구조
```kotlin
class Correction {
    val id: Long
    val originSentence: String          // 원본 문장
    val correctedSentence: String       // 교정된 문장
    val originTranslation: String?      // 한국어 번역
    val correctedTranslation: String?
    val feedback: String?
    val feedbackType: String?
    val score: Int?
    val isFavorite: Boolean             // 복습 대상 식별용
    val memo: String?
    val user: User?

    fun toggleFavorite()
    fun updateMemo(memo: String)
}
```

### 기존 OpenAiClient 구조
- `callOpenAiApi()`: 범용 AI 호출 메서드
- `generateCorrectionWithTranslations()`: 교정 + 번역 생성
- `sendChatRequest()`: 단순 대화 요청

→ **재사용 가능**: `callOpenAiApi()` 또는 `sendChatRequest()`를 활용해 답변 비교 구현

---

## 데이터 모델 설계

### 1. ReviewSentence (DTO, DB 저장 안 함)
```kotlin
data class ReviewSentence(
    val id: Long,                    // Correction ID
    val korean: String,              // originTranslation
    val hint: String,                // AI 생성 (2-3개 핵심 단어)
    val bestAnswer: String,          // correctedSentence
    val difficulty: ReviewDifficulty, // 난이도 (추후 확장용, 현재는 MEDIUM 고정)
    val lastReviewedAt: LocalDateTime?,
    val reviewCount: Int,
    val nextReviewDate: LocalDate
)
```

**변환 로직**: `Correction.isFavorite == true` → `ReviewSentence`
- korean ← originTranslation
- bestAnswer ← correctedSentence
- hint ← AI로 생성 (나중에 구현, 일단 빈 문자열 또는 기본값)
- difficulty ← MEDIUM (고정)
- nextReviewDate ← 간격 반복 알고리즘 계산

---

### 2. ReviewRecord (Entity, DB 저장)
```kotlin
@Entity
class ReviewRecord(
    @Id @GeneratedValue
    val id: Long = 0,

    val correctionId: Long,          // Correction 외래키 (단방향)
    val userAnswer: String,          // 사용자 답변
    val isCorrect: Boolean,          // AI 판정 결과
    val score: Int,                  // 0-100 점수
    val timeSpent: Int,              // 소요 시간 (초)
    val reviewDate: LocalDateTime,   // 복습 일시

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User
) {
    // 다음 복습 날짜 계산 (간격 반복 알고리즘)
    fun calculateNextReviewDate(): LocalDate {
        val currentReviewCount = // DB에서 해당 correction의 복습 횟수 조회 필요

        if (!isCorrect) {
            // 틀림: 오늘 → 1일 → 3일
            return when (currentReviewCount) {
                0 -> reviewDate.toLocalDate()              // 오늘
                1 -> reviewDate.toLocalDate().plusDays(1)  // 1일 후
                else -> reviewDate.toLocalDate().plusDays(3)
            }
        } else {
            // 맞음: 3일 → 1주 → 2주 → 1개월
            return when (currentReviewCount) {
                0 -> reviewDate.toLocalDate().plusDays(3)   // 3일
                1 -> reviewDate.toLocalDate().plusWeeks(1)  // 1주
                2 -> reviewDate.toLocalDate().plusWeeks(2)  // 2주
                else -> reviewDate.toLocalDate().plusMonths(1) // 1개월
            }
        }
    }
}
```

**설계 결정**:
- `correctionId`만 저장 (양방향 연관관계 불필요)
- 간격 반복 알고리즘은 Service에서 구현 (도메인 메서드는 순수 계산만)
- 복습 횟수 조회는 Repository에서 처리

---

### 3. AnswerComparison (DTO, AI 응답 파싱용)
```kotlin
data class AnswerComparison(
    val isCorrect: Boolean,
    val score: Int,                  // 0-100
    val differences: List<Difference>,
    val overallFeedback: String,
    val tip: String
)

data class Difference(
    val type: DifferenceType,        // GRAMMAR, WORD_CHOICE, NATURALNESS, PUNCTUATION
    val userPart: String,
    val bestPart: String,
    val explanation: String,
    val importance: Importance       // HIGH, MEDIUM, LOW
)

enum class DifferenceType {
    GRAMMAR, WORD_CHOICE, NATURALNESS, PUNCTUATION
}

enum class Importance {
    HIGH, MEDIUM, LOW
}
```

---

## API 설계

### P0-1: 복습 문장 조회 API

**Endpoint**: `GET /api/review/sentences?limit={개수}`

**Query Parameter**:
- `limit` (optional, default: 10): 조회할 문장 개수

**Response** (200 OK):
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

**로직**:
1. `Correction.isFavorite == true` 조회 (사용자별)
2. `ReviewSentence`로 변환
3. 최근 `ReviewRecord` 조회 → `lastReviewedAt`, `reviewCount` 계산
4. `nextReviewDate` 계산 (간격 반복 알고리즘)
5. `nextReviewDate`가 오늘 이전인 것 우선 정렬

---

### P0-2: 내 답변 비교 API ⭐

**Endpoint**: `POST /api/review/compare`

**Request Body**:
```json
{
  "sentenceId": 1,
  "userAnswer": "I will send you the report tomorrow.",
  "bestAnswer": "I'll send you the report by tomorrow.",
  "korean": "이 보고서를 내일까지 보내드릴게요"
}
```

**Response** (200 OK):
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

**AI 프롬프트 예시** (PromptManager에 추가):
```
당신은 영어 학습 코치입니다.
사용자의 답변과 Best Answer를 비교하여 차이점을 분석해주세요.

한국어 문장: {korean}
사용자 답변: {userAnswer}
Best Answer: {bestAnswer}

다음 형식으로 JSON 응답을 생성하세요:
{
  "isCorrect": boolean,
  "score": 0-100 점수,
  "differences": [
    {
      "type": "GRAMMAR | WORD_CHOICE | NATURALNESS | PUNCTUATION",
      "userPart": "사용자가 쓴 부분",
      "bestPart": "Best Answer 부분",
      "explanation": "친근한 톤으로 설명",
      "importance": "HIGH | MEDIUM | LOW"
    }
  ],
  "overallFeedback": "전체 피드백 (친근하게)",
  "tip": "💡 Tip: 핵심 팁"
}

**규칙**:
1. 의미가 거의 같으면 isCorrect: true, 점수 70점 이상
2. differences는 최대 3개까지
3. 친근하고 격려하는 톤 사용
4. 한국어로 설명
```

**로직**:
1. Request DTO 검증
2. OpenAiClient를 통해 AI 비교 요청
3. JSON 파싱 → `AnswerComparison` 변환
4. 응답 반환

---

### P0-3: 복습 기록 저장 API

**Endpoint**: `POST /api/review/records`

**Request Body**:
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

**Response** (200 OK):
```json
{
  "success": true,
  "nextReviewDate": "2025-12-28"
}
```

**로직**:
1. Request DTO 검증
2. `ReviewRecord` 엔티티 생성 및 저장
3. 해당 Correction의 복습 횟수 조회 (맞은 횟수만 카운트)
4. `calculateNextReviewDate()` 호출
5. 다음 복습 날짜 반환

---

## 구현 단계

### Phase 1: 데이터 모델 구현
- [ ] `ReviewDifficulty` Enum 생성
- [ ] `DifferenceType`, `Importance` Enum 생성
- [ ] `ReviewRecord` 엔티티 구현
- [ ] `ReviewRecordRepository` 인터페이스 구현
- [ ] `ReviewSentence`, `AnswerComparison`, `Difference` DTO 구현
- [ ] 도메인 테스트 작성 (`ReviewRecordTest`)

### Phase 2: AI 비교 기능 구현
- [ ] `PromptManager`에 답변 비교 프롬프트 추가
- [ ] `OpenAiClient`에 `compareAnswers()` 메서드 추가
- [ ] JSON 파싱 로직 구현 (`AnswerComparison` 변환)

### Phase 3: Service 구현
- [ ] `ReviewService` 생성
- [ ] `getReviewSentences()` 구현 (Correction → ReviewSentence 변환)
- [ ] `compareAnswer()` 구현 (AI 호출)
- [ ] `saveReviewRecord()` 구현 (간격 반복 계산 포함)
- [ ] 복습 횟수 조회 쿼리 추가 (Repository 메서드)

### Phase 4: Controller 구현
- [ ] Request/Response DTO 생성
- [ ] `ReviewController` 생성
- [ ] P0-1: `GET /api/review/sentences` 엔드포인트
- [ ] P0-2: `POST /api/review/compare` 엔드포인트
- [ ] P0-3: `POST /api/review/records` 엔드포인트
- [ ] 에러 핸들링 추가 (404, 400, 500)

### Phase 5: 통합 테스트
- [ ] Postman/HTTPie로 API 테스트
- [ ] AI 비교 응답 품질 확인 (실제 프롬프트 튜닝)
- [ ] 간격 반복 알고리즘 검증 (여러 시나리오)
- [ ] JWT 인증 통합 확인

---

## 검증 계획

### Fast Loop (개발 중)
```bash
# 도메인 테스트만 실행
./gradlew test --tests "ReviewRecordTest"

# 특정 API 호출 테스트 (수동)
curl -X GET "http://localhost:7071/api/review/sentences?limit=5" \
  -H "Authorization: Bearer {token}"
```

### Pre-merge Verification
```bash
# 전체 빌드
./gradlew clean build

# AI 비교 API 품질 체크 (수동)
# - 완전히 맞는 답변: score 100, isCorrect: true
# - 의미 유사: score 70-90, isCorrect: true, differences 2-3개
# - 틀린 답변: score 50 이하, isCorrect: false
```

---

## 리스크 및 고려사항

### 🔴 High Priority

1. **AI 응답 파싱 실패**
   - 리스크: GPT가 JSON 형식을 지키지 않을 수 있음
   - 대응: 프롬프트에 "MUST return valid JSON" 명시 + Fallback 응답 준비

2. **간격 반복 알고리즘 복잡도**
   - 리스크: "맞은 횟수만" vs "전체 복습 횟수" 기준이 불명확
   - 대응: 명확한 스펙 확인 필요 → **맞은 횟수 기준**으로 구현

3. **성능 이슈 (AI 호출)**
   - 리스크: 비교 API는 실시간 AI 호출 → 2-5초 소요
   - 대응: 프론트엔드에 로딩 상태 안내 필요 (백엔드는 타임아웃 설정)

### 🟡 Medium Priority

4. **hint 생성 로직**
   - 현재: 빈 문자열 또는 고정값
   - 향후: AI로 핵심 단어 2-3개 추출 (P1 이후)

5. **난이도 분류**
   - 현재: MEDIUM 고정
   - 향후: P2에서 AI 기반 난이도 분류 구현

6. **복습 횟수 조회 성능**
   - 리스크: 문장마다 복습 기록 조회 쿼리 발생 (N+1 문제)
   - 대응: 1차 구현 후 성능 측정, 필요시 Batch 쿼리 최적화

### 🟢 Low Priority

7. **테스트 커버리지**
   - 도메인 모델만 테스트 (프로젝트 컨벤션)
   - Service/Controller는 수동 테스트로 검증

---

## 후속 작업 (Out of Scope)

- [ ] P1-1: 복습 통계 API (`GET /api/review/stats`)
- [ ] P1-2: 복습 설정 API (`POST /api/review/settings`)
- [ ] P2: AI 기반 난이도 분류
- [ ] P2: hint 자동 생성 (AI)
- [ ] 성능 최적화 (N+1 쿼리, Redis 캐싱)

---

## Self-Critique Checklist

- [x] 현재 코드베이스 확인 (Correction, OpenAiClient, Controller)
- [x] 변경 범위 명확화 (신규 파일, 수정 파일)
- [x] 간격 반복 알고리즘 스펙 확인 (맞은 횟수 기준)
- [x] AI 분석 깊이 확인 (상세 분석)
- [x] 작업 우선순위 확인 (P0만 구현)
- [ ] 프론트엔드 요청 데이터 구조 재확인 필요 (hint 필드 등)
- [x] 검증 계획 수립 (Fast Loop, Pre-merge)
- [x] 리스크 식별 (AI 파싱, 성능, 간격 알고리즘)

---

**다음 단계**:
1. 프론트엔드와 데이터 구조 최종 확인 (hint, difficulty 필드 필요 여부)
2. Phase 1 (데이터 모델) 구현 시작
3. Phase 2 (AI 비교) 프롬프트 작성 및 테스트
