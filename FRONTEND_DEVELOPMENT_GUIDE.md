# WriteBuddy 프론트엔드 개발 가이드

## 📋 프로젝트 개요

**WriteBuddy**는 AI 기반 영어 문법 교정 서비스입니다.
- **백엔드**: Spring Boot + Kotlin
- **데이터베이스**: H2 (개발용) + JPA/Hibernate
- **API 스타일**: REST API with JSON
- **개발 서버**: `http://localhost:7071`

## 🎯 핵심 기능

### ⭐ 주요 특징
1. **AI 문법 교정**: OpenAI 기반 실시간 교정
2. **점수 시스템**: 1-10점 품질 평가
3. **실제 사용 예시**: 영화, 가사, 기사 등에서 교정된 표현의 실제 사용 사례 제공
4. **학습 대시보드**: 통계 및 진도 추적
5. **즐겨찾기**: 중요한 교정 결과 북마크
6. **개인 노트**: 학습 메모 기능
7. **사용자 시스템**: 개인별 진도 관리

## 🔌 API 엔드포인트

### 📝 교정 기능

#### 기본 교정 요청
```http
POST http://localhost:7071/corrections
Content-Type: application/json

{
  "originSentence": "How Can I enjoy new features in this project?"
}
```

**응답 예시:**
```json
{
  "id": 1,
  "originSentence": "How Can I enjoy new features in this project?",
  "correctedSentence": "How can I enjoy the new features in this project?",
  "feedback": "소문자로 시작하고 정관사 'the'를 추가해야 합니다.",
  "feedbackType": "GRAMMAR",
  "score": 7,
  "isFavorite": false,
  "memo": null,
  "createdAt": "2025-06-25T21:30:00",
  "originTranslation": "이 프로젝트의 새로운 기능들을 어떻게 즐길 수 있을까요?",
  "feedbackTranslation": "Start with lowercase and add the definite article 'the'.",
  "relatedExamples": [
    {
      "id": 1,
      "phrase": "I couldn't agree more",
      "source": "Friends (TV Show)",
      "sourceType": "MOVIE",
      "sourceTypeDisplay": "영화/드라마",
      "sourceTypeEmoji": "🎬",
      "context": "Ross agrees enthusiastically with Rachel's opinion about Monica's cooking",
      "url": "https://www.youtube.com/watch?v=example",
      "timestamp": "05:23",
      "difficulty": 6,
      "tags": ["agreement", "enthusiasm", "conversation"],
      "isVerified": true,
      "createdAt": "2025-06-25T21:30:00"
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
  "answer": "'See'는 의도하지 않고 자연스럽게 보는 것, 'look'은 의도적으로 시선을 향하는 것, 'watch'는 움직이는 것을 지속적으로 관찰하는 것을 의미합니다. 예를 들어 'I saw a bird'(새를 봤다), 'Look at me'(나를 봐), 'Watch TV'(TV를 보다)처럼 사용합니다.",
  "createdAt": "2025-06-26T10:30:00"
}
```

**사용 예시:**
- 문법 질문: "When should I use 'a' vs 'an'?"
- 표현 질문: "How to politely decline an invitation?"
- 단어 차이: "What's the difference between 'fun' and 'funny'?"
- 문화적 뉘앙스: "Is 'How are you?' always a genuine question?"

### 👤 사용자 관리

#### 사용자 생성
```http
POST /users
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com"
}
```

**응답 예시:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "createdAt": "2025-06-25T20:00:00"
}
```

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
  feedbackTranslation: string | null;  // 피드백의 영어 번역
  relatedExamples: RealExample[];  // 관련 실제 사용 예시
}
```

### 사용자 (User)
```typescript
interface User {
  id: number;
  username: string;
  email: string;
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
  url?: string;                      // 관련 링크 (YouTube, 기사 등)
  timestamp?: string;                // 영상의 경우 타임스탬프
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
  PODCAST = "PODCAST"     // 팟캐스트 🎧
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
- [ ] **번역 기능 표시** (원문/피드백 번역 제공)
- [ ] **실제 사용 예시 표시** (교정 결과와 함께 자동 제공)
- [ ] 즐겨찾기 토글 기능
- [ ] 교정 목록 페이지
- [ ] **영어 학습 채팅 기능** (자유 질문 및 답변)

### 2단계: 대시보드
- [ ] 일별 통계 카드
- [ ] 점수 트렌드 차트
- [ ] 피드백 타입별 분포 차트
- [ ] 오류 패턴 분석 페이지

### 3단계: 사용자 시스템
- [ ] 사용자 등록/로그인
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

### 인터랙션
- 클릭 시 상세 모달 표시
- URL 있는 경우 "원본 보기" 버튼
- 타임스탬프 있는 경우 직접 재생
- 태그 클릭 시 관련 예시 검색

이 가이드를 참고하여 사용자 친화적이고 효과적인 영어 학습 도구를 개발하세요! 🚀
