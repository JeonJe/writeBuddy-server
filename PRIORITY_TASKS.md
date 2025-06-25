# WriteBuddy 우선순위 작업 리스트

## 🎯 이번 주 집중 작업 (Week 1)

### 1단계: 안정성 확보 (1-2일)
```
우선순위: 🔴 CRITICAL
목표: 서비스의 기본적인 안정성과 신뢰성 확보
```

#### A. 글로벌 예외 처리기 구현
- **시간 예상**: 4-6시간
- **파일 위치**: `src/main/kotlin/com/writebuddy/writebuddy/exception/`
- **작업 내용**:
  - `GlobalExceptionHandler` 클래스 생성
  - `CorrectionException`, `OpenAiException` 등 커스텀 예외 정의
  - HTTP 상태 코드별 에러 응답 표준화
  - 로깅 전략 수립

#### B. 입력값 검증 강화
- **시간 예상**: 2-3시간
- **작업 내용**:
  - `CorrectionRequest`에 검증 어노테이션 추가
  - 문장 길이 제한, 특수문자 필터링
  - 빈 값, null 체크 강화

### 2단계: 품질 보증 (2-3일)
```
우선순위: 🟡 HIGH
목표: 코드 품질과 테스트 커버리지 향상
```

#### C. 도메인 테스트 케이스 완성
- **시간 예상**: 6-8시간
- **작업 내용**:
  - `Correction` 도메인 객체 테스트 추가
  - `ErrorType` enum 테스트 작성
  - 경계값 테스트, 예외 상황 테스트
  - 테스트 데이터 빌더 패턴 적용

#### D. 통합 테스트 추가
- **시간 예상**: 4-5시간
- **작업 내용**:
  - `CorrectionController` 통합 테스트
  - Mock 서버를 이용한 OpenAI API 테스트
  - 데이터베이스 트랜잭션 테스트

### 3단계: 개발 효율성 (1일)
```
우선순위: 🟢 MEDIUM
목표: 개발 및 문서화 효율성 향상
```

#### E. API 문서화 설정
- **시간 예상**: 3-4시간
- **작업 내용**:
  - SpringDoc OpenAPI 의존성 추가
  - Swagger UI 설정 및 접근 경로 설정
  - 각 API 엔드포인트 문서화
  - 에러 응답 문서화

---

## 🚀 다음 주 작업 (Week 2) - 재미있는 기능 우선!

### 1단계: 🎮 재미있는 핵심 기능 (우선순위 1)
```
우선순위: 🔴 CRITICAL & FUN
목표: 사용자가 눈으로 보고 즐길 수 있는 핵심 기능 구현
```

#### A. 피드백 점수 시스템 ⭐
- **시간 예상**: 3-4시간
- **작업 내용**:
  - `Correction` 엔티티에 `score` 필드 추가 (1-10점)
  - OpenAI로부터 교정 품질 점수 받아오기
  - 점수별 색상 구분 (빨강: 1-3, 노랑: 4-6, 초록: 7-10)
  - 평균 점수 계산 API

#### B. 교정 히스토리 & 통계 대시보드 📊
- **시간 예상**: 4-5시간  
- **작업 내용**:
  - 일별/주별/월별 교정 횟수 통계
  - 피드백 타입별 분포 차트
  - 점수 변화 추이 그래프
  - 가장 많이 틀리는 패턴 분석
  - "오늘의 교정 성과" 요약

#### C. 즐겨찾기 & 학습 노트 📝
- **시간 예상**: 2-3시간
- **작업 내용**:
  - 교정 결과 즐겨찾기/북마크 기능
  - 개인 학습 노트 추가 (메모 필드)
  - 자주 틀리는 문장 패턴 모음
  - 성과 뱃지 시스템 (연속 교정, 고득점 등)

### 2단계: 👤 사용자 개인화 (우선순위 2)
```
우선순위: 🟡 HIGH & PERSONAL
목표: 개인화된 경험 제공
```

#### D. 간단한 사용자 시스템
- **시간 예상**: 3-4시간
- **작업 내용**:
  - 사용자 엔티티 (username, email 정도만)
  - 세션 기반 간단 인증 (JWT는 나중에)
  - 사용자별 교정 기록 분리
  - 개인 통계 대시보드

#### E. 학습 레벨 & 진행도
- **시간 예상**: 2-3시간
- **작업 내용**:
  - 교정 횟수/점수 기반 레벨 시스템
  - 진행도 바 (다음 레벨까지 몇 개 남았는지)
  - 주간/월간 목표 설정 기능
  - 학습 스트릭 추적 (연속 며칠 사용했는지)

---

## 🚀 다음 단계 작업 (Week 3) - 서비스 품질 & 다양성 우선!

### 1단계: 🎮 더 재미있는 고급 기능 (우선순위 1)
```
우선순위: 🔴 CRITICAL & FUN
목표: 사용자 몰입도를 높이는 게임화 요소와 고급 기능
```

#### A. 학습 레벨 & 진행도 시스템 🏆
- **시간 예상**: 3-4시간
- **작업 내용**:
  - 교정 횟수/점수 기반 레벨 시스템 (1-50레벨)
  - 경험치(XP) 시스템 구현
  - 진행도 바 (다음 레벨까지 몇 개 남았는지)
  - 레벨업 축하 메시지 및 보상

#### B. 성과 뱃지 & 업적 시스템 🏅
- **시간 예상**: 2-3시간
- **작업 내용**:
  - 연속 교정 뱃지 (3일, 7일, 30일 연속)
  - 완벽 점수 뱃지 (10점 달성 횟수)
  - 교정 마스터 뱃지 (총 교정 수 기반)
  - 타입별 전문가 뱃지 (문법, 철자, 스타일 등)

#### C. 학습 스트릭 & 목표 설정 📅
- **시간 예상**: 2-3시간
- **작업 내용**:
  - 연속 학습 일수 추적 (스트릭)
  - 주간/월간 목표 설정 기능
  - 목표 달성률 시각화
  - 동기부여 메시지 시스템

### 2단계: 🛠️ 서비스 품질 향상 (우선순위 2)
```
우선순위: 🟡 HIGH & QUALITY
목표: 사용자 경험 개선과 서비스 안정성
```

#### D. API 문서화 & 프론트엔드 지원
- **시간 예상**: 3-4시간
- **작업 내용**:
  - SpringDoc OpenAPI 의존성 추가
  - Swagger UI 설정 및 API 자동 문서화
  - CORS 설정 (프론트엔드 연동 대비)
  - 개발용 테스트 데이터 시드 생성

#### E. 고급 교정 기능
- **시간 예상**: 4-5시간
- **작업 내용**:
  - 배치 교정 기능 (여러 문장 한번에)
  - 교정 결과 필터링 (점수별, 타입별)
  - 교정 결과 정렬 기능
  - 교정 히스토리 검색 기능

#### F. 사용자 편의 기능
- **시간 예상**: 2-3시간
- **작업 내용**:
  - 교정 결과 내보내기 (JSON, CSV)
  - 즐겨찾기 태그 시스템
  - 학습 노트 카테고리 분류
  - 개인 설정 관리 (알림, 테마 등)

### 3단계: 🔧 성능 & 최적화 (우선순위 3 - 나중에)
```
우선순위: 🟢 LOW - 기능 완성 후 진행
목표: 서비스가 완전히 작동한 후 성능 개선
```

#### G. 캐싱 & 성능 최적화 (후순위)
- Caffeine 캐시 설정
- 자주 사용되는 교정 결과 캐싱
- 데이터베이스 쿼리 최적화
- 페이징 처리 구현

#### H. 고급 인증 & 보안 (후순위)
- Spring Security + JWT 인증
- 소셜 로그인 (구글, 카카오)
- API 레이트 리미팅
- 데이터 암호화

---

## 📊 작업 진행 방법

### 일일 작업 플로우
```
1. 아침 (9:00-12:00): 핵심 개발 작업
2. 오후 (13:00-17:00): 테스트 작성 및 문서화
3. 저녁 (18:00-20:00): 코드 리뷰 및 리팩토링
```

### 작업별 체크포인트

#### 🔴 글로벌 예외 처리기 구현 ✅ **완료 (2025-06-17)**
- [x] `@ControllerAdvice` 클래스 생성
- [x] 5개 커스텀 예외 클래스 정의 (`CorrectionException`, `OpenAiException`, `ValidationException`)
- [x] HTTP 상태 코드 매핑 완료 (400, 500, 503)
- [x] SLF4J 로깅 전략 구현
- [x] `ErrorResponse` DTO로 에러 응답 표준화
- [x] 모든 예외 상황에 대한 핸들러 구현

**구현 상세**:
- `GlobalExceptionHandler`: 5가지 예외 타입 처리
- 사용자 친화적 에러 메시지 (OpenAI 에러 숨김)
- 요청 경로, 타임스탬프 포함한 상세 에러 정보
- 파일 위치: `src/main/kotlin/com/writebuddy/writebuddy/exception/`

#### 🟡 도메인 테스트 케이스 완성 ✅ **완료 (2025-06-17)**
- [x] `Correction` 객체 생성 테스트 (정상/예외) - 13개 테스트
- [x] `FeedbackType` 변환 테스트 - 9개 테스트
- [x] 경계값 테스트 (빈 문자열, 긴 문장 등)
- [x] `@Nested` 클래스로 테스트 그룹화
- [x] CLAUDE.md 가이드라인 준수 (AssertJ, DisplayName 등)
- [x] 테스트 실행 시간 5초 이내

**구현 상세**:
- `CorrectionTest`: 도메인 객체 생성, 경계값, BaseEntity 상속 테스트
- `FeedbackTypeTest`: Enum 값 검증, 변환, 비즈니스 로직 테스트
- 파일 위치: `src/test/kotlin/com/writebuddy/writebuddy/domain/`

#### 🟢 입력값 검증 강화 ✅ **완료 (2025-06-17)**
- [x] `@NotBlank` 빈 값 검증 추가
- [x] `@Size(min=1, max=1000)` 문장 길이 제한
- [x] `@Pattern` 영문자, 숫자, 기본 구두점만 허용
- [x] 한국어 에러 메시지 적용
- [x] 검증 실패 시 GlobalExceptionHandler 연동

**구현 상세**:
- 정규식: `^[a-zA-Z0-9\\s.,!?;:\"'()\\-]+$`
- 유효: "hello world", 무효: "hello 한글", ""
- 파일 위치: `src/main/kotlin/com/writebuddy/writebuddy/controller/dto/request/`

#### 🔧 통합 테스트 프레임워크 ⚠️ **부분 완료 (2025-06-17)**
- [x] `CorrectionControllerIntegrationTest` 클래스 생성
- [x] `TestConfiguration`으로 Mock 설정
- [x] 11개 통합 테스트 케이스 작성
- [ ] 의존성 해결 (OpenAI 클라이언트 Mock 개선 필요)
- [ ] 테스트 실행 성공

**구현 상세**:
- Mock을 이용한 OpenAI API 테스트 프레임워크
- 검증 실패, 에러 처리 테스트 케이스
- 파일 위치: `src/test/kotlin/com/writebuddy/writebuddy/controller/`

### 완료 기준
- ✅ **모든 테스트 통과**: `./gradlew test`
- ✅ **빌드 성공**: `./gradlew build`
- ✅ **코드 품질 체크**: 정적 분석 도구 통과
- ✅ **문서 업데이트**: 변경사항 문서화 완료

---

## 🎖️ 성과 측정 지표

### 품질 지표
- **테스트 커버리지**: 현재 ~% → 목표 80%+
- **빌드 시간**: 현재 6초 → 목표 10초 이내 유지
- **API 응답 시간**: 평균 500ms 이내

### 개발 생산성 지표
- **일일 커밋 수**: 3-5개 의미있는 커밋
- **코드 리뷰 시간**: 30분 이내
- **문서 업데이트**: 기능 개발과 동시 진행

### 기능 완성도 지표
- **예외 처리**: 모든 API에서 일관된 에러 응답
- **검증 로직**: 모든 입력값에 대한 적절한 검증
- **문서화**: 개발자가 API를 이해하고 사용할 수 있는 수준

---

## 💡 효율적인 작업을 위한 팁

### 개발 환경 최적화
- IDE 단축키 활용으로 작업 속도 향상
- 코드 템플릿 활용 (테스트 코드, 예외 클래스 등)
- Git commit 메시지 템플릿 사용

### 시간 관리
- 포모도로 기법 활용 (25분 집중 + 5분 휴식)
- 복잡한 작업은 더 작은 단위로 분할
- 일일 목표 설정 및 달성률 체크

### 품질 관리
- 기능 개발과 테스트 코드 작성을 동시에 진행
- 작은 단위로 자주 커밋
- 매일 전체 테스트 실행으로 회귀 버그 방지

---

## 📅 마일스톤

### Week 1 완료 목표 ✅ **달성 (2025-06-17)**
- [x] 안정적인 예외 처리 시스템 구축
- [x] 도메인 테스트 커버리지 달성 (22개 테스트)  
- [x] 강화된 입력값 검증 시스템
- [x] SLF4J 로깅 시스템 구축

### 🎯 **2025-06-17 작업 완료 현황**

#### ✅ 핵심 성과
1. **안정성 확보 완료**
   - 글로벌 예외 처리 시스템 (5가지 예외 타입)
   - 표준화된 에러 응답 (`ErrorResponse` DTO)
   - SLF4J 로깅 전략

2. **품질 보증 완료**
   - 도메인 테스트 22개 (CorrectionTest 13개 + FeedbackTypeTest 9개)
   - 입력값 검증 3단계 (NotBlank, Size, Pattern)
   - CLAUDE.md 가이드라인 준수

3. **코드 개선**
   - 응답 DTO 필드명 수정 (feedBack → feedback)
   - 전체 로깅을 SLF4J로 통일
   - 커스텀 예외 클래스 3개 추가

#### 📂 생성/수정된 파일들
**새로 생성된 파일들**:
- `src/main/kotlin/com/writebuddy/writebuddy/exception/CorrectionException.kt`
- `src/main/kotlin/com/writebuddy/writebuddy/exception/OpenAiException.kt`
- `src/main/kotlin/com/writebuddy/writebuddy/exception/ValidationException.kt`
- `src/main/kotlin/com/writebuddy/writebuddy/controller/dto/response/ErrorResponse.kt`
- `src/test/kotlin/com/writebuddy/writebuddy/domain/CorrectionTest.kt`
- `src/test/kotlin/com/writebuddy/writebuddy/domain/FeedbackTypeTest.kt`
- `src/test/kotlin/com/writebuddy/writebuddy/controller/CorrectionControllerIntegrationTest.kt`
- `src/test/kotlin/com/writebuddy/writebuddy/config/TestConfiguration.kt`
- `src/test/resources/application-test.properties`

**수정된 파일들**:
- `src/main/kotlin/com/writebuddy/writebuddy/exception/GlobalExceptionHandler.kt` - 확장
- `src/main/kotlin/com/writebuddy/writebuddy/controller/dto/request/CorrectionRequest.kt` - 검증 강화
- `src/main/kotlin/com/writebuddy/writebuddy/controller/dto/response/CorrectionResponse.kt` - 필드명 수정
- `src/main/kotlin/com/writebuddy/writebuddy/service/CorrectionService.kt` - SLF4J 적용
- `src/main/kotlin/com/writebuddy/writebuddy/service/OpenAiClient.kt` - SLF4J 적용

#### 🎯 다음 작업 우선순위 (재미 우선!)
1. **피드백 점수 시스템 구현** (3-4시간) ⭐
   - Correction 엔티티에 score 필드 추가
   - OpenAI로부터 점수 받아오는 로직 구현
   - 점수별 색상/등급 시스템

2. **교정 히스토리 & 통계 대시보드** (4-5시간) 📊
   - 일별/주별 통계 API
   - 피드백 타입별 분포 차트
   - 점수 변화 추이 그래프

3. **간단한 사용자 시스템** (3-4시간) 👤
   - 사용자 엔티티 설계
   - 세션 기반 인증
   - 개인별 통계 분리

### 📋 재미있는 기능 체크포인트

#### ⭐ 피드백 점수 시스템 ✅ **완료 (2025-06-25)**
- [x] `Correction` 엔티티에 `score` 필드 추가 (Integer, 1-10)
- [x] OpenAI 프롬프트 수정하여 점수 포함해서 받기
- [x] 점수 파싱 로직 구현 (Quadruple 데이터 구조 도입)
- [x] 평균 점수 계산 API (`GET /corrections/average-score`)
- [x] 점수별 등급 시스템 (1-5: 빨강, 6-7: 노랑, 8-10: 초록)

#### 📊 교정 히스토리 & 통계 대시보드 ✅ **완료 (2025-06-25)**
- [x] 일별 교정 횟수 통계 API (`GET /corrections/dashboard/daily`)
- [x] 피드백 타입별 분포 API (daily API에 포함)
- [x] 점수 변화 추이 API (`GET /corrections/dashboard/score-trend`)
- [x] "오늘의 성과" 요약 API (오늘 교정 수, 평균 점수)
- [x] 가장 자주 나오는 오류 패턴 분석 (`GET /corrections/dashboard/error-patterns`)

#### 📝 즐겨찾기 & 학습 노트 ✅ **완료 (2025-06-25)**
- [x] `Correction` 엔티티에 `isFavorite` 필드 추가
- [x] 즐겨찾기 토글 API (`PUT /corrections/{id}/favorite`)
- [x] 즐겨찾기 목록 조회 API (`GET /corrections/favorites`)
- [x] 학습 노트 필드 추가 (`memo`)
- [x] 노트 업데이트 API (`PUT /corrections/{id}/memo`)
- [ ] 성과 뱃지 시스템 설계 (연속 교정, 완벽 점수 등) - 차후 구현

#### 👤 간단한 사용자 시스템 ✅ **완료 (2025-06-25)**
- [x] `User` 엔티티 생성 (username, email, createdAt)
- [x] `Correction`과 `User` 다대일 관계 설정
- [x] 사용자 생성 API (`POST /users`)
- [x] 사용자 조회 API (`GET /users`, `GET /users/{username}`)
- [x] 사용자별 교정 기록 분리 (`POST /corrections/users/{userId}`)
- [x] 개인 통계 대시보드 API (`GET /users/{userId}/statistics`)
- [ ] 세션 기반 로그인/로그아웃 API - 차후 구현

### Week 2 완료 목표 🎮 **재미 우선 버전** ✅ **달성 (2025-06-25)**
- [x] 피드백 점수 시스템으로 교정 품질 시각화
- [x] 개인 학습 진도와 통계를 한눈에 보는 대시보드
- [x] 즐겨찾기와 학습 노트로 개인화된 학습 경험
- [x] 사용자별 맞춤 통계와 성과 추적

### 🎯 **2025-06-25 작업 완료 현황 - Week 2 달성!**

#### ✅ 핵심 성과 (재미있는 기능 우선 완료!)
1. **⭐ 피드백 점수 시스템 완료**
   - OpenAI 응답에서 1-10점 점수 자동 파싱
   - Quadruple 데이터 구조 도입으로 확장성 확보
   - 점수별 색상 코딩 가이드라인 제시
   - 평균 점수 계산 API 구현

2. **📊 통계 대시보드 완료**
   - 일별 성과 요약 API (교정 수, 평균 점수, 타입 분포)
   - 점수 변화 추이 그래프 데이터 API (최근 20개)
   - 오류 패턴 분석 API (타입별 예시 문장)
   - 실시간 학습 진도 추적 가능

3. **📝 개인화 기능 완료**
   - 즐겨찾기 시스템 (북마크 토글, 목록 조회)
   - 개인 학습 노트 (메모 작성/수정)
   - 사용자별 맞춤 통계 대시보드
   - 개인 성과 추적 시스템

4. **👤 사용자 관리 시스템 완료**
   - User 엔티티 및 Correction 관계 설정
   - 사용자 생성/조회 API 완성
   - 사용자별 교정 기록 분리
   - 개인 통계 API (교정 수, 평균 점수, 즐겨찾기 수)

#### 🆕 새롭게 추가된 API 엔드포인트 (총 10개)
**점수 & 통계 API:**
- `GET /corrections/average-score` - 전체 평균 점수
- `GET /corrections/dashboard/daily` - 오늘의 성과 요약
- `GET /corrections/dashboard/score-trend` - 점수 변화 추이
- `GET /corrections/dashboard/error-patterns` - 오류 패턴 분석

**즐겨찾기 & 노트 API:**
- `PUT /corrections/{id}/favorite` - 즐겨찾기 토글
- `GET /corrections/favorites` - 즐겨찾기 목록
- `PUT /corrections/{id}/memo` - 학습 노트 업데이트

**사용자 관리 API:**
- `POST /users` - 사용자 생성
- `GET /users`, `GET /users/{username}` - 사용자 조회
- `GET /users/{userId}/statistics` - 개인 통계
- `POST /corrections/users/{userId}` - 사용자별 교정 생성

#### 📂 새로 생성된 파일들 (총 8개)
**도메인 계층:**
- `src/main/kotlin/com/writebuddy/writebuddy/domain/User.kt`
- `src/main/kotlin/com/writebuddy/writebuddy/repository/UserRepository.kt`

**서비스 계층:**
- `src/main/kotlin/com/writebuddy/writebuddy/service/UserService.kt`
- `src/main/kotlin/com/writebuddy/writebuddy/service/Quadruple.kt` (OpenAiClient.kt 내부)

**컨트롤러 계층:**
- `src/main/kotlin/com/writebuddy/writebuddy/controller/UserController.kt`

**DTO 계층:**
- `src/main/kotlin/com/writebuddy/writebuddy/controller/dto/request/CreateUserRequest.kt`
- `src/main/kotlin/com/writebuddy/writebuddy/controller/dto/request/UpdateMemoRequest.kt`
- `src/main/kotlin/com/writebuddy/writebuddy/controller/dto/response/UserResponse.kt`

**문서화:**
- `FRONTEND_DEVELOPMENT_GUIDE.md` - 프론트엔드 개발 가이드

#### 🔧 수정된 기존 파일들 (총 6개)
- `Correction.kt` - score, isFavorite, memo, user 필드 추가
- `CorrectionResponse.kt` - 새 필드들 포함
- `CorrectionController.kt` - 10개 새 엔드포인트 추가
- `CorrectionService.kt` - 통계, 즐겨찾기, 사용자 연동 메서드 추가
- `OpenAiClient.kt` - 점수 파싱 로직 및 Quadruple 구조 추가
- `TestConfiguration.kt` - 새 Mock 설정 업데이트

---

## 📋 Week 3 재미있는 기능 체크포인트

#### 🏆 학습 레벨 & 진행도 시스템
- [ ] 레벨 계산 로직 구현 (교정 수 + 평균 점수 기반)
- [ ] 경험치(XP) 시스템 설계 및 구현
- [ ] 레벨별 필요 XP 정의 (1레벨: 0XP, 2레벨: 100XP...)
- [ ] 진행도 계산 API (`GET /users/{userId}/progress`)
- [ ] 레벨업 이벤트 감지 및 알림 시스템

#### 🏅 성과 뱃지 & 업적 시스템
- [ ] Badge 엔티티 생성 (타입, 조건, 획득일자)
- [ ] 사용자-뱃지 관계 설정 (UserBadge)
- [ ] 뱃지 조건 체크 로직 구현
- [ ] 뱃지 획득 API (`GET /users/{userId}/badges`)
- [ ] 뱃지 시스템 자동 감지 (교정 후 체크)

#### 📅 학습 스트릭 & 목표 설정
- [ ] 일별 활동 추적 테이블 (DailyActivity)
- [ ] 스트릭 계산 로직 (연속 활동 일수)
- [ ] 목표 설정 엔티티 (Goal: 주간/월간)
- [ ] 목표 달성률 계산 API
- [ ] 동기부여 메시지 시스템

#### 🛠️ API 문서화 & 프론트엔드 지원
- [ ] SpringDoc OpenAPI 의존성 추가
- [ ] Swagger UI 설정 (`/swagger-ui.html`)
- [ ] 모든 API 엔드포인트 문서화 완료
- [ ] CORS 설정 (localhost:3000 허용)
- [ ] 개발용 데이터 시드 스크립트 작성

#### 🔍 고급 교정 기능
- [ ] 배치 교정 API (`POST /corrections/batch`)
- [ ] 교정 결과 필터링 API (점수, 타입, 날짜별)
- [ ] 교정 결과 정렬 API (최신순, 점수순, 알파벳순)
- [ ] 전체 텍스트 검색 기능 (교정 내용, 피드백)
- [ ] 즐겨찾기 고급 검색 및 태그 시스템

### Week 3 완료 목표 🎮 **게임화 & 품질 향상**
- [ ] 게임화 요소로 사용자 몰입도 극대화
- [ ] API 문서화로 프론트엔드 개발 지원
- [ ] 고급 교정 기능으로 사용자 편의성 향상
- [ ] 다양한 기능으로 서비스 차별화 달성