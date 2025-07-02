# WriteBuddy 프론트엔드 개발 가이드

## 📋 프로젝트 개요

**WriteBuddy**는 AI 기반 영어 문법 교정 서비스입니다.
- **백엔드**: Spring Boot + Kotlin
- **데이터베이스**: H2 (개발용) + JPA/Hibernate
- **API 스타일**: REST API with JSON
- **개발 서버**: `http://localhost:7071`

### 🏗️ 백엔드 아키텍처 (2025-06-27 최신 업데이트)

**🔄 주요 변경사항 (2025-06-27)**:
- **성능 최적화**: 교정 + 예시 생성 통합으로 API 호출 50% 감소 (2회 → 1회)
- **타임아웃 설정**: OpenAI API 연결 5초, 읽기 15초로 무한 대기 방지
- **통합 JSON 응답**: 하나의 OpenAI 요청으로 교정과 예시를 함께 생성
- **응답 시간 개선**: 약 15% 성능 향상 (12초 → 10.6초)
- **로깅 개선**: API 호출 시간 측정 및 상세 로그 추가
- **API 안정성**: 환경변수 설정 및 401 인증 오류 해결
- **신뢰도 향상**: 외부 URL/타임스탬프 제거로 서비스 신뢰성 강화
- **통계 개선**: 10점 만점 문장을 실수 통계에서 제외, '잘한 표현'으로 별도 분류
- **새 API**: `/corrections/users/{userId}/good-expressions` 엔드포인트 추가

**이전 변경사항 (2025-06-26)**:
- **AI 기반 예시 생성**: 하드코딩된 샘플 데이터 → OpenAI 실시간 생성
- **보안 강화**: API 키 하드코딩 제거, 환경변수 기반 설정
- **구조 최적화**: 불필요한 CRUD API 제거, 핵심 기능에 집중

**모듈 구조**:
- **OpenAiClient**: 통합 AI API 통신 (교정 + 예시 동시 생성, 타임아웃 설정, 성능 로깅)
- **OpenAiResponseParser**: 통합 JSON 응답 파싱 (교정 + 예시 데이터 동시 처리)
- **PromptManager**: 통합 프롬프트 관리 (교정 + 예시를 하나의 JSON으로 생성)
- **CorrectionService**: saveWithExamples 메서드로 통합 처리
- **OpenApiRestClientConfig**: HTTP 클라이언트 타임아웃 설정 (5초/15초)
- **OpenAiProperties**: 환경별 설정 관리 (타임아웃, 재시도 포함)

**피드백 스타일**: 자신감 넘치는 기가챠드 멘토 톤 ("형", "자, 봐봐", "이건 기본이지", "완벽하게")

**보안 설정**:
- 모든 API 키는 환경변수로 관리
- `.claudeignore`로 민감 정보 보호
- Google OAuth2 통합 인증

## 🎯 핵심 기능

### ⭐ 주요 특징
1. **AI 문법 교정**: OpenAI 기반 실시간 교정
2. **점수 시스템**: 1-10점 품질 평가
3. **번역 기능**: 원문과 교정문의 한국어 번역 자동 제공
4. **후드 스타일 피드백**: 재미있고 친근한 톤으로 설명 ("야 이거 완전 기본기야!")
5. **영어 학습 채팅**: 문법/표현/문화 질문에 대한 자유로운 AI 채팅
6. **실제 사용 예시**: 영화, 가사, 기사 등에서 교정된 표현의 실제 사용 사례 제공
7. **학습 대시보드**: 통계 및 진도 추적
8. **즐겨찾기**: 중요한 교정 결과 북마크
9. **개인 노트**: 학습 메모 기능
10. **사용자 시스템**: 개인별 진도 관리
11. **🆕 AI 플래시카드**: 단어 추가 시 AI가 자동으로 의미와 태그 생성, 스마트 암기 시스템

## 🔌 API 엔드포인트

### 📝 교정 기능

#### 기본 교정 요청 (최적화된 통합 응답)
```http
POST http://localhost:7071/corrections
Content-Type: application/json

{
  "originSentence": "I want to learn English good"
}
```

**응답 예시:**
```json
{
  "id": 1,
  "originSentence": "How Can I enjoy new features in this project?",
  "correctedSentence": "How can I enjoy the new features in this project?",
  "feedback": "야 이거 완전 기본기야! 대문자로 시작하는 건 문장 맨 처음이나 고유명사일 때만이고, 'the'는 특정한 것을 가리킬 때 꼭 써줘야 해. 'new features'라고 하면 어떤 기능들인지 명확하게 해주는 거야!",
  "feedbackType": "GRAMMAR",
  "score": 7,
  "isFavorite": false,
  "memo": null,
  "createdAt": "2025-06-25T21:30:00",
  "originTranslation": "이 프로젝트의 새로운 기능들을 어떻게 즐길 수 있을까요?",
  "correctedTranslation": "이 프로젝트의 새로운 기능들을 어떻게 즐길 수 있을까요?",
  "relatedExamples": [
    {
      "id": 1,
      "phrase": "I speak English well",
      "source": "Cambridge English Course",
      "sourceType": "BOOK",
      "sourceTypeDisplay": "문학/도서",
      "sourceTypeEmoji": "📚",
      "context": "Example sentence demonstrating proper use of adverbs",
      "difficulty": 4,
      "tags": ["adverb", "grammar", "basic"],
      "isVerified": true,
      "createdAt": "2025-06-27T10:55:00"
    },
    {
      "id": 2,
      "phrase": "She sings really well",
      "source": "The Voice (TV Show)",
      "sourceType": "MOVIE",
      "sourceTypeDisplay": "영화/드라마",
      "sourceTypeEmoji": "🎬",
      "context": "Judge complimenting a contestant's performance",
      "difficulty": 5,
      "tags": ["adverb", "performance", "compliment"],
      "isVerified": true,
      "createdAt": "2025-06-27T10:55:00"
    }
  ]
}
```

#### 사용자별 교정 요청
```http
POST /corrections/users/{userId}
Content-Type: application/json

{
  "originSentence": "Your sentence here"
}
```

#### 전체 교정 목록 조회
```http
GET /corrections
```

#### 피드백 타입 통계
```http
GET /corrections/statistics
```

**응답 예시:**
```json
{
  "GRAMMAR": 15,
  "SPELLING": 8,
  "STYLE": 3,
  "PUNCTUATION": 2
}
```

### 📊 대시보드 & 분석

#### 평균 점수
```http
GET /corrections/average-score
```

**응답 예시:**
```json
{
  "averageScore": 7.2
}
```

#### 일별 통계
```http
GET /corrections/dashboard/daily
```

**응답 예시:**
```json
{
  "totalCorrections": 5,
  "averageScore": 8.1,
  "feedbackTypes": {
    "GRAMMAR": 3,
    "SPELLING": 1,
    "STYLE": 1
  }
}
```

#### 점수 변화 추이 (최근 20개)
```http
GET /corrections/dashboard/score-trend
```

**응답 예시:**
```json
{
  "scoreTrend": [
    {
      "order": 1,
      "score": 6,
      "feedbackType": "GRAMMAR",
      "createdAt": "2025-06-25T10:30:00"
    },
    {
      "order": 2,
      "score": 8,
      "feedbackType": "SPELLING",
      "createdAt": "2025-06-25T11:15:00"
    }
  ]
}
```

#### 오류 패턴 분석
```http
GET /corrections/dashboard/error-patterns
```

**응답 예시:**
```json
{
  "errorPatterns": {
    "GRAMMAR": ["i am student", "how can i", "she don't like"],
    "SPELLING": ["recieve", "seperate", "occured"],
    "STYLE": ["very very good", "really really nice"]
  }
}
```

### ⭐ 즐겨찾기 & 노트

#### 즐겨찾기 토글
```http
PUT /corrections/{id}/favorite
```

#### 즐겨찾기 목록 조회
```http
GET /corrections/favorites
```

#### 개인 노트 업데이트
```http
PUT /corrections/{id}/memo
Content-Type: application/json

{
  "memo": "Remember: always use articles with countable nouns"
}
```

#### 사용자별 잘한 표현 조회 (10점 만점 문장들)
```http
GET /corrections/users/{userId}/good-expressions
```

**설명:** 해당 사용자의 최근 3개월간 10점 만점을 받은 문장들을 조회합니다. 실수 통계에서는 제외되며, 잘한 표현으로 별도 관리됩니다.

**응답 예시:**
```json
[
  {
    "id": 15,
    "originSentence": "I have been studying English for two years.",
    "correctedSentence": "I have been studying English for two years.",
    "feedback": "완벽한 현재완료 진행형이야! 🎉 전혀 고칠 게 없는 완벽한 문장이네!",
    "feedbackType": "GRAMMAR",
    "score": 10,
    "isFavorite": false,
    "memo": null,
    "createdAt": "2025-06-27T12:00:00",
    "originTranslation": "나는 2년 동안 영어를 공부해왔습니다.",
    "correctedTranslation": "나는 2년 동안 영어를 공부해왔습니다."
  }
]
```

### 🎬 실제 사용 예시 API

#### 키워드로 예시 검색
```http
GET /examples/search?keyword=agreement
```

**응답 예시:**
```json
[
  {
    "id": 1,
    "phrase": "I couldn't agree more",
    "source": "Friends (TV Show)",
    "sourceType": "MOVIE",
    "sourceTypeDisplay": "영화/드라마",
    "sourceTypeEmoji": "🎬",
    "context": "Ross agrees enthusiastically with Rachel's opinion",
    "url": "https://www.youtube.com/watch?v=example",
    "timestamp": "05:23",
    "difficulty": 6,
    "tags": ["agreement", "enthusiasm", "conversation"],
    "isVerified": true,
    "createdAt": "2025-06-25T21:30:00"
  }
]
```

#### 특정 구문으로 예시 찾기
```http
GET /examples/phrase?phrase=break a leg
```

#### 랜덤 예시 조회
```http
GET /examples/random?count=3
```

#### 오늘의 추천 예시
```http
GET /examples/daily
```

#### 출처 타입별 예시 조회
```http
GET /examples/source/MOVIE
GET /examples/source/SONG
GET /examples/source/NEWS
```

#### 난이도별 예시 조회
```http
GET /examples/difficulty?minDifficulty=1&maxDifficulty=5
```

#### 새 예시 추가
```http
POST /examples
Content-Type: application/json

{
  "phrase": "It's raining cats and dogs",
  "source": "BBC Weather Report",
  "sourceType": "NEWS",
  "context": "Weather presenter describing heavy rainfall",
  "url": "https://bbc.co.uk/weather",
  "difficulty": 8,
  "tags": "weather, idiom, heavy rain",
  "isVerified": true
}
```

### 💬 영어 학습 채팅

#### 자유 질문 채팅
```http
POST /chat
Content-Type: application/json

{
  "question": "What's the difference between 'see', 'look', and 'watch'?"
}
```

**응답 예시:**
```json
{
  "question": "What's the difference between 'see', 'look', and 'watch'?",
  "answer": "야 이거 진짜 좋은 질문이야! 한국인들이 개 많이 헷갈려하는 부분인데 ㅋㅋ 'See'는 그냥 자연스럽게 시야에 들어오는 거, 'look'은 의도적으로 시선을 확 돌리는 거, 'watch'는 움직이는 걸 쭉~ 지켜보는 거야. 예시로 'I saw a bird'(어? 새다!), 'Look at me'(나 좀 봐봐), 'Watch TV'(TV 정주행 ㄱㄱ) 이런 식으로 쓰는 거지!",
  "createdAt": "2025-06-26T10:30:00"
}
```

**사용 예시:**
- 문법 질문: "When should I use 'a' vs 'an'?"
- 표현 질문: "How to politely decline an invitation?"
- 단어 차이: "What's the difference between 'fun' and 'funny'?"
- 문화적 뉘앙스: "Is 'How are you?' always a genuine question?"

### 🎯 학습 분석 & 개인화

#### 사용자 약점 분석
```http
GET /analytics/users/{userId}/weak-areas
```

**응답 예시:**
```json
{
  "userId": 1,
  "topWeakAreas": [
    {
      "type": "GRAMMAR_ARTICLES",
      "typeDisplay": "관사 (a, an, the)",
      "pattern": "관사 누락 또는 잘못된 사용",
      "frequency": 8,
      "frequencyDisplay": "8회 실수",
      "severity": "HIGH",
      "severityDisplay": "🟠 심각",
      "severityColor": "#f97316",
      "improvementRate": 0.3,
      "improvementRateDisplay": "📈 개선 중",
      "exampleMistakes": [
        "I am student → I am a student",
        "She is teacher → She is a teacher"
      ],
      "recommendation": "관사 사용법을 집중적으로 연습해보세요. 가산명사와 불가산명사 구분이 핵심이에요! ⚠️ 빠른 시일 내에 개선이 필요해요."
    }
  ],
  "overallImprovementRate": 0.45,
  "improvementRateDisplay": "📊 꾸준히 성장 (45%)",
  "recommendedFocus": "GRAMMAR_ARTICLES",
  "recommendedFocusDisplay": "관사 (a, an, the)",
  "totalMistakes": 25,
  "analysisDate": "2025-06-26T15:30:00",
  "summary": {
    "criticalAreas": 0,
    "highPriorityAreas": 2,
    "totalWeakAreas": 5,
    "message": "⚠️ 우선적으로 개선할 영역이 2개 있어요."
  }
}
```

#### 약점 분석 수동 트리거
```http
POST /analytics/users/{userId}/analyze
```

**응답 예시:**
```json
{
  "message": "약점 분석이 완료되었습니다",
  "userId": "1"
}
```

**분석 항목:**
- 🔴 **관사 (a, an, the)**: 가산명사/불가산명사 구분 실수
- 🟠 **전치사 (in, on, at)**: 시간/장소 전치사 혼동  
- 🟡 **시제**: 과거/현재/미래 시제 사용 오류
- 🟢 **동사 형태**: 주어-동사 일치 문제
- 📝 **철자 오류**: 자주 틀리는 단어들
- ✏️ **문체**: 단어 선택 및 문장 구조

### 👤 사용자 관리 및 인증

#### OAuth 로그인 (Google)
```http
GET /oauth2/authorization/google
```
Google OAuth 로그인 페이지로 리다이렉트됩니다.

#### 현재 사용자 정보 조회
```http
GET /auth/user
```

**응답 예시:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@gmail.com",
  "oauthProvider": "google",
  "oauthProviderId": "google_user_id_123",
  "profileImageUrl": "https://lh3.googleusercontent.com/...",
  "createdAt": "2025-06-25T20:00:00"
}
```

#### 인증 상태 확인
```http
GET /auth/status
```

**응답 예시:**
```json
{
  "authenticated": true,
  "user": {
    "name": "John Doe",
    "email": "john@gmail.com",
    "picture": "https://lh3.googleusercontent.com/..."
  }
}
```

#### 로그아웃
```http
POST /logout
```

#### 사용자 생성 (OAuth 자동 등록)
OAuth 로그인 시 사용자가 자동으로 생성됩니다.

#### 전체 사용자 목록
```http
GET /users
```

#### 특정 사용자 조회
```http
GET /users/{username}
```

#### 사용자 개인 통계
```http
GET /users/{userId}/statistics
```

**응답 예시:**
```json
{
  "totalCorrections": 25,
  "averageScore": 7.8,
  "favoriteCount": 8,
  "feedbackTypeDistribution": {
    "GRAMMAR": 15,
    "SPELLING": 6,
    "STYLE": 4
  }
}
```

### 🧠 플래시카드 기능 (NEW!)

#### 플래시카드 생성 (AI 자동 분석)
```http
POST /flashcards
Content-Type: application/json

{
  "userId": 1,
  "word": "sophisticated"
}
```

**응답 예시:**
```json
{
  "id": 1,
  "userId": 1,
  "word": {
    "id": 1,
    "word": "sophisticated",
    "meaning": "정교한, 세련된",
    "difficulty": 7,
    "tags": ["형용사", "복잡성", "학술용어"],
    "category": "ACADEMIC",
    "isAiGenerated": true
  },
  "memoryStatus": "NEW",
  "reviewCount": 0,
  "correctCount": 0,
  "incorrectCount": 0,
  "accuracy": 0.0,
  "lastReviewedAt": null,
  "nextReviewAt": "2025-07-02T23:00:00",
  "personalNote": null,
  "isFavorite": false,
  "isReadyForReview": true,
  "createdAt": "2025-07-02T22:00:00"
}
```

#### 플래시카드 목록 조회 (암기 상태별 필터링)
```http
GET /flashcards/users/{userId}?memoryStatus=LEARNING&page=0&size=20
```

**가능한 memoryStatus 값:**
- `NEW`: 새로운 단어
- `STRUGGLING`: 어려워하는 단어  
- `LEARNING`: 학습 중인 단어
- `REVIEWING`: 복습 중인 단어
- `MASTERED`: 숙달된 단어

#### 복습 대기 플래시카드 조회
```http
GET /flashcards/users/{userId}/review?size=10
```

**설명:** 복습 시간이 된 플래시카드들을 우선순위대로 조회

#### 즐겨찾기 플래시카드 조회
```http
GET /flashcards/users/{userId}/favorites
```

#### 플래시카드 학습 결과 기록
```http
POST /flashcards/{flashcardId}/review
Content-Type: application/json

{
  "isCorrect": true
}
```

**기능:** 정답/오답에 따라 자동으로 암기 상태와 다음 복습 시간이 조정됩니다.

#### 플래시카드 즐겨찾기 토글
```http
PUT /flashcards/{flashcardId}/favorite
```

#### 플래시카드 개인 노트 수정
```http
PUT /flashcards/{flashcardId}/note
Content-Type: application/json

{
  "note": "이 단어는 학술 논문에서 자주 사용됨"
}
```

#### 플래시카드 삭제
```http
DELETE /flashcards/{flashcardId}
```

#### 플래시카드 학습 통계
```http
GET /flashcards/users/{userId}/statistics
```

**응답 예시:**
```json
{
  "totalCount": 50,
  "masteredCount": 12,
  "reviewingCount": 18,
  "learningCount": 15,
  "strugglingCount": 3,
  "newCount": 2,
  "readyForReviewCount": 8
}
```

## 📊 데이터 모델

### 교정 결과 (Correction)
```typescript
interface Correction {
  id: number;
  originSentence: string;
  correctedSentence: string;
  feedback: string;
  feedbackType: 'GRAMMAR' | 'SPELLING' | 'STYLE' | 'PUNCTUATION' | 'SYSTEM';
  score: number | null;  // 1-10 점수
  isFavorite: boolean;
  memo: string | null;
  createdAt: string;     // ISO 8601 format
  originTranslation: string | null;    // 원문의 한국어 번역
  correctedTranslation: string | null; // 교정문의 한국어 번역
  relatedExamples: RealExample[];  // 관련 실제 사용 예시
}
```

### 사용자 (User)
```typescript
interface User {
  id: number;
  username: string;
  email: string;
  oauthProvider?: string;     // OAuth 제공자 (google 등)
  oauthProviderId?: string;   // OAuth 제공자의 사용자 ID
  profileImageUrl?: string;   // 프로필 이미지 URL
  createdAt: string;
}
```

### 채팅 응답 (ChatResponse)
```typescript
interface ChatResponse {
  question: string;        // 사용자 질문
  answer: string;         // AI 답변
  createdAt: string;      // 응답 생성 시간
}
```

### 실제 사용 예시 (RealExample)
```typescript
interface RealExample {
  id: number;
  phrase: string;                    // 실제 사용된 표현
  source: string;                    // 출처 (영화명, 노래명 등)
  sourceType: ExampleSourceType;     // 출처 타입
  sourceTypeDisplay: string;         // 출처 타입 표시명
  sourceTypeEmoji: string;           // 출처 타입 이모지
  context: string;                   // 사용된 맥락/상황 설명
  url?: string | null;               // 관련 링크 (신뢰도 향상을 위해 null 권장)
  timestamp?: string | null;         // 영상 타임스탬프 (신뢰도 향상을 위해 null 권장)
  difficulty: number;                // 1-10 난이도
  tags: string[];                    // 검색용 태그 배열
  isVerified: boolean;               // 검증된 예시인지 여부
  createdAt: string;
  updatedAt?: string;
}

enum ExampleSourceType {
  MOVIE = "MOVIE",        // 영화/드라마 🎬
  SONG = "SONG",          // 음악/가사 🎵
  NEWS = "NEWS",          // 뉴스/기사 📰
  BOOK = "BOOK",          // 문학/도서 📚
  INTERVIEW = "INTERVIEW", // 인터뷰 🎤
  SOCIAL = "SOCIAL",      // 소셜미디어 📱
  SPEECH = "SPEECH",      // 연설/강연 🎙️
  PODCAST = "PODCAST",    // 팟캐스트 🎧
  OTHER = "OTHER"         // 기타 📄
}
```

### 플래시카드 (Flashcard)
```typescript
interface Flashcard {
  id: number;
  userId: number;
  word: Word;
  memoryStatus: MemoryStatus;
  reviewCount: number;
  correctCount: number;
  incorrectCount: number;
  accuracy: number;               // 정답률 (0.0 ~ 1.0)
  lastReviewedAt: string | null;
  nextReviewAt: string | null;
  personalNote: string | null;
  isFavorite: boolean;
  isReadyForReview: boolean;
  createdAt: string;
}

interface Word {
  id: number;
  word: string;
  meaning: string;
  difficulty: number;             // 1-10 난이도
  tags: string[];                 // 검색용 태그
  category: WordCategory;
  isAiGenerated: boolean;
}

enum MemoryStatus {
  NEW = "NEW",           // 새로운 단어
  STRUGGLING = "STRUGGLING", // 어려워하는 단어
  LEARNING = "LEARNING",     // 학습 중인 단어
  REVIEWING = "REVIEWING",   // 복습 중인 단어
  MASTERED = "MASTERED"      // 숙달된 단어
}

enum WordCategory {
  GRAMMAR = "GRAMMAR",       // 문법
  BUSINESS = "BUSINESS",     // 비즈니스
  ACADEMIC = "ACADEMIC",     // 학술
  DAILY = "DAILY",           // 일상
  TRAVEL = "TRAVEL",         // 여행
  TECHNOLOGY = "TECHNOLOGY", // 기술
  GENERAL = "GENERAL"        // 일반
}

interface FlashcardStatistics {
  totalCount: number;
  masteredCount: number;
  reviewingCount: number;
  learningCount: number;
  strugglingCount: number;
  newCount: number;
  readyForReviewCount: number;
}
```

### 피드백 타입
- `GRAMMAR`: 문법 교정
- `SPELLING`: 철자 교정
- `STYLE`: 스타일 개선
- `PUNCTUATION`: 구두점 교정
- `SYSTEM`: 시스템 피드백 (fallback)

## 🎨 UI/UX 가이드라인

### 점수 시각화
```css
/* 점수별 색상 코드 */
.score-excellent { 
  color: #22c55e;      /* 초록색: 8-10점 */
  background: #dcfce7; 
}

.score-good { 
  color: #f59e0b;      /* 노란색: 6-7점 */
  background: #fef3c7; 
}

.score-needs-work { 
  color: #ef4444;      /* 빨간색: 1-5점 */
  background: #fee2e2; 
}
```

### 추천 컴포넌트

#### 1. 메인 교정 인터페이스
```
┌─────────────────────────────────────┐
│        영어 문장을 입력하세요         │
│ ┌─────────────────────────────────┐ │
│ │ How Can I enjoy new features?   │ │
│ └─────────────────────────────────┘ │
│         [교정하기] 버튼              │
└─────────────────────────────────────┘

교정 결과:
┌─────────────────────────────────────┐
│ 원문: How Can I enjoy new features? │
│ 교정: How can I enjoy the new...    │
│ 피드백: 소문자로 시작하고...         │
│ 점수: [7] ⭐ 즐겨찾기 📝 노트       │
└─────────────────────────────────────┘
```

#### 2. 대시보드 카드
```
┌─────────────────────────────────────┐
│        📅 오늘의 학습 성과           │
│                                     │
│  ✅ 교정 횟수: 5회                  │
│  ⭐ 평균 점수: 8.1점                │
│  📈 문법: 3, 철자: 1, 스타일: 1      │
└─────────────────────────────────────┘
```

#### 3. 점수 트렌드 차트
- **라이브러리 추천**: Chart.js, Recharts, D3.js
- **차트 타입**: 선 그래프 (Line Chart)
- **X축**: 시간 순서 (order)
- **Y축**: 점수 (1-10)

#### 4. 즐겨찾기 카드
```
┌─────────────────────────────────────┐
│ ⭐ "I am student" → "I am a student" │
│                                     │
│ 📝 노트: 가산명사 앞에는 관사 필요    │
│ 📅 2025-06-25  🏷️ GRAMMAR          │
└─────────────────────────────────────┘
```

#### 5. 플래시카드 인터페이스 (NEW!)
```
┌─────────────────────────────────────┐
│           📚 플래시카드              │
│                                     │
│         sophisticated               │
│                                     │
│    [뜻 보기] [정답] [오답] [⭐]      │
└─────────────────────────────────────┘

플립 후:
┌─────────────────────────────────────┐
│        정교한, 세련된                │
│                                     │
│ 🏷️ 형용사, 복잡성, 학술용어          │
│ 📈 난이도: 7/10  📊 정답률: 85%      │
│ 📝 노트: 학술 논문에서 자주 사용됨    │
└─────────────────────────────────────┘
```

#### 6. 플래시카드 대시보드
```
┌─────────────────────────────────────┐
│        🧠 단어 학습 현황             │
│                                     │
│ 🆕 새 단어: 5개     🎯 복습 대기: 8개 │
│ 📚 학습 중: 15개    ⭐ 숙달: 12개    │
│ 💪 정답률: 78%      🔥 연속: 3일     │
└─────────────────────────────────────┘
```

### 암기 상태별 색상 코드
```css
/* 암기 상태별 색상 */
.memory-new { 
  color: #6b7280;      /* 회색: 새 단어 */
  background: #f3f4f6; 
}

.memory-struggling { 
  color: #ef4444;      /* 빨간색: 어려운 단어 */
  background: #fee2e2; 
}

.memory-learning { 
  color: #f59e0b;      /* 주황색: 학습 중 */
  background: #fef3c7; 
}

.memory-reviewing { 
  color: #3b82f6;      /* 파란색: 복습 중 */
  background: #dbeafe; 
}

.memory-mastered { 
  color: #22c55e;      /* 초록색: 숙달 */
  background: #dcfce7; 
}
```

## 📱 반응형 디자인

### 모바일 (< 768px)
- 원문/교정문 세로 배치
- 카드 형태의 단일 컬럼 레이아웃
- 터치 친화적인 버튼 크기

### 태블릿 (768px - 1024px)
- 2컬럼 그리드 레이아웃
- 대시보드 카드 2x2 배치

### 데스크톱 (> 1024px)
- 원문/교정문 좌우 배치
- 3컬럼 그리드 레이아웃
- 사이드바 네비게이션

## 🚀 개발 단계별 우선순위

### 1단계: MVP (핵심 기능)
- [ ] 기본 교정 입력/출력 화면
- [ ] 점수 표시 (색상 코딩)
- [ ] **번역 기능 표시** (원문/교정문 번역 제공)
- [ ] **실제 사용 예시 표시** (교정 결과와 함께 자동 제공)
- [ ] 즐겨찾기 토글 기능
- [ ] 교정 목록 페이지
- [ ] **영어 학습 채팅 기능** (자유 질문 및 답변)
- [ ] **개인화된 약점 분석** (사용자별 실수 패턴 분석)

### 2단계: 대시보드
- [ ] 일별 통계 카드
- [ ] 점수 트렌드 차트
- [ ] 피드백 타입별 분포 차트
- [ ] 오류 패턴 분석 페이지
- [ ] **약점 분석 대시보드** (심각도별 색상 구분, 개선율 표시)
- [ ] **맞춤형 학습 추천** (약점 기반 학습 가이드)

### 3단계: 사용자 시스템 ✅ (OAuth 구현 완료)
- [x] **Google OAuth 로그인** 
- [x] 사용자 인증 및 세션 관리
- [x] OAuth 사용자 정보 자동 등록
- [ ] 개인 통계 대시보드
- [ ] 사용자별 교정 기록 관리

### 4단계: 고급 기능
- [ ] 개인 노트 편집기
- [ ] 실제 사용 예시 고급 검색 (키워드, 출처별, 난이도별)
- [ ] 실제 사용 예시 즐겨찾기 및 학습 노트
- [ ] 고급 필터링/검색
- [ ] 학습 스트릭 표시
- [ ] 성취 뱃지 시스템

## 🔧 기술적 고려사항

### OAuth 설정
Google Cloud Console에서 OAuth 클라이언트 설정 필요:
1. **Authorized redirect URIs**: `http://localhost:7071/login/oauth2/code/google`
2. **환경변수 설정**:
   ```bash
   export GOOGLE_CLIENT_ID="your-google-client-id"
   export GOOGLE_CLIENT_SECRET="your-google-client-secret"
   ```

### 환경별 설정 관리
프로젝트는 환경별로 다른 설정을 사용합니다:

**로컬 개발 환경** (`spring.profiles.active=local`):
```properties
# 개발용 빠른 설정
openai.retry.max-attempts=2
openai.retry.delay=500
logging.level.com.writebuddy=DEBUG
```

**운영 환경** (`spring.profiles.active=prod`):
```properties
# 운영용 안정적 설정
openai.retry.max-attempts=5
openai.retry.delay=2000
logging.level.com.writebuddy=INFO
```

### HTTP 상태 코드
- `200`: 성공
- `400`: 잘못된 요청 (validation 실패)
- `404`: 리소스 없음
- `500`: 서버 오류

### 에러 응답 형식
```json
{
  "timestamp": "2025-06-25T21:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "문장은 1-1000자여야 합니다",
  "path": "/corrections"
}
```

### 입력 validation
- **문장 길이**: 1-1000자
- **허용 문자**: 영문자, 숫자, 기본 구두점
- **금지 문자**: 한글, 특수기호

### 성능 최적화
- API 응답 캐싱 (단기)
- 이미지/아이콘 지연 로딩
- 무한 스크롤 (교정 목록)
- 디바운싱 (검색 입력)

## 💡 UX 개선 아이디어

### 마이크로 인터랙션
- 점수 애니메이션 (숫자 카운트업)
- 즐겨찾기 하트 효과
- 로딩 스피너 (교정 중)
- 성공 토스트 메시지

### 접근성 (A11Y)
- 키보드 네비게이션
- 스크린 리더 지원
- 색상 대비 준수
- 포커스 인디케이터

### 학습 동기부여
- 연속 학습 일수 표시
- 주간/월간 목표 설정
- 점수 향상 축하 메시지
- 레벨업 시스템

## 🎬 실제 사용 예시 UX 가이드

### 예시 카드 디자인
```
┌─────────────────────────────────────┐
│ 🎬 "I couldn't agree more"          │
│ 출처: Friends (TV Show)             │
│                                     │
│ 📝 Ross가 Rachel의 의견에 열정적으로 │
│    동의하며 말하는 장면              │
│                                     │
│ 🔗 YouTube 05:23  📈 난이도: 6/10   │
│ 🏷️ #동의 #열정 #대화               │
└─────────────────────────────────────┘
```

### 출처 타입별 아이콘
- 🎬 영화/드라마: 빨간색 배경
- 🎵 음악/가사: 보라색 배경  
- 📰 뉴스/기사: 파란색 배경
- 📚 문학/도서: 갈색 배경
- 🎤 인터뷰: 주황색 배경
- 📱 소셜미디어: 핑크색 배경
- 🎙️ 연설/강연: 회색 배경
- 🎧 팟캐스트: 초록색 배경

### 난이도 표시
- 1-3: 🟢 초급 (Beginner)
- 4-6: 🟡 중급 (Intermediate)  
- 7-8: 🟠 중상급 (Upper-Intermediate)
- 9-10: 🔴 고급 (Advanced)

### 인터랙션 (2025-06-27 업데이트)
- 클릭 시 상세 모달 표시
- 태그 클릭 시 관련 예시 검색
- 출처와 맥락 정보 중심의 학습 경험 제공
- **신뢰도 개선**: 외부 URL 제거로 안정적인 예시 제공

이 가이드를 참고하여 사용자 친화적이고 효과적인 영어 학습 도구를 개발하세요! 🚀
