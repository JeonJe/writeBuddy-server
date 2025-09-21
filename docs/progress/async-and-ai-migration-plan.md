# 비동기 처리 확대 및 Spring AI 마이그레이션 계획

## 📋 개요

현재 `CorrectionController`에서 성공적으로 구현된 비동기 처리를 다른 컨트롤러로 확장하고, 기존 RestClient 기반 OpenAI 연동을 Spring AI로 마이그레이션하는 계획서입니다.

## 🎯 목표

### 1. 성능 개선
- **현재 상태**: CorrectionController만 비동기 처리 (60-70% 성능 향상 달성)
- **목표**: 모든 AI 관련 API 엔드포인트에 비동기 처리 적용
- **예상 효과**: 전체 시스템 응답 시간 50-70% 단축

### 2. 기술 스택 모던화
- **현재**: RestClient + 수동 JSON 파싱
- **목표**: Spring AI 활용으로 코드 간소화 및 유지보수성 향상

## 📅 Phase 1: 비동기 처리 확대 (우선순위: 높음)

### 1.1 ChatController 비동기화
**현재 상태**: 동기 처리로 인한 긴 응답 시간
```kotlin
// 현재 (동기)
val answer = openAiClient.generateChatResponse(request.question)

// 목표 (비동기)
val answer = chatService.generateResponseAsync(request.question).get(15, TimeUnit.SECONDS)
```

**작업 계획**:
- [ ] `AsyncChatService` 클래스 생성
- [ ] `generateResponseAsync()` 메서드 구현
- [ ] ChatController에 비동기 호출 적용
- [ ] 타임아웃 및 예외 처리 강화
- [ ] 성능 테스트 스크립트 작성

**예상 효과**: 채팅 응답 시간 3-5초 → 1-2초

### 1.2 LearningAnalyticsController 비동기화
**현재 상태**: 약점 분석 처리에 시간 소요
```kotlin
// 현재 (동기)
val summary = learningAnalyticsService.analyzeUserWeakAreas(userId)

// 목표 (비동기)
val summary = analyticsService.analyzeWeakAreasAsync(userId).get(20, TimeUnit.SECONDS)
```

**작업 계획**:
- [ ] `AsyncLearningAnalyticsService` 클래스 생성
- [ ] 대용량 데이터 분석 비동기 처리
- [ ] 백그라운드 분석 스케줄링 검토
- [ ] 분석 진행 상태 API 추가 고려

**예상 효과**: 분석 API 응답 시간 5-10초 → 2-3초

### 1.3 RealExampleService 최적화
**현재 상태**: 이미 비동기 호출되지만 개선 여지 있음
**작업 계획**:
- [ ] 예시 생성 로직 최적화
- [ ] 캐싱 전략 검토
- [ ] 배치 처리 구현 검토

## 📅 Phase 2: Spring AI 마이그레이션 (우선순위: 중간)

### 2.1 현재 RestClient 사용 현황 분석
**RestClient 사용 파일들**:
- `OpenAiClient.kt`: 핵심 AI 통신 로직
- `OpenApiRestClientConfig.kt`: RestClient 설정
- `RealExampleService.kt`: 예시 생성용 API 호출

### 2.2 Spring AI 도입 계획

#### 2.2.1 의존성 추가
```kotlin
// build.gradle.kts 추가 필요
implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
implementation("org.springframework.ai:spring-ai-retry")
```

#### 2.2.2 설정 마이그레이션
```yaml
# application.yml 마이그레이션
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-4
        temperature: 0.3
      retry:
        max-attempts: 3
        backoff:
          initial-interval: 1s
          multiplier: 2
```

#### 2.2.3 코드 마이그레이션 순서
1. **OpenAiClient 리팩토링**
   - [ ] `ChatClient` 인터페이스 도입
   - [ ] 기존 메서드들을 Spring AI 방식으로 변경
   - [ ] 응답 파싱 로직 간소화

2. **프롬프트 관리 개선**
   - [ ] `PromptTemplate` 활용
   - [ ] 프롬프트 버전 관리
   - [ ] 다국어 프롬프트 지원 기반 마련

3. **타입 안전성 향상**
   - [ ] Response 객체 자동 매핑
   - [ ] 커스텀 컨버터 구현

### 2.3 단계별 마이그레이션 전략
1. **Phase 2.1**: 새로운 기능부터 Spring AI 적용
2. **Phase 2.2**: 기존 기능 점진적 마이그레이션
3. **Phase 2.3**: 레거시 RestClient 코드 제거

## 🧪 테스트 전략

### 성능 테스트
- [ ] 기존 `test_async.sh`, `test_sync.sh` 확장
- [ ] 각 Phase별 성능 벤치마크 수집
- [ ] 부하 테스트 시나리오 작성

### 호환성 테스트
- [ ] 기존 API 응답 형식 유지 확인
- [ ] 에러 응답 형식 일관성 검증
- [ ] 타임아웃 시나리오 테스트

## 📊 성공 지표

### Phase 1 목표
- [ ] ChatController 응답시간 60% 단축
- [ ] LearningAnalyticsController 응답시간 50% 단축
- [ ] 전체 AI 관련 API 평균 응답시간 2초 이하
- [ ] 에러율 0.1% 이하 유지

### Phase 2 목표
- [ ] 코드 복잡도 30% 감소 (순환 복잡도 기준)
- [ ] AI 관련 버그 50% 감소
- [ ] 새로운 AI 기능 개발 시간 40% 단축
- [ ] 프롬프트 관리 효율성 향상

## ⚠️ 리스크 및 고려사항

### 기술적 리스크
- **Spring AI 호환성**: 현재 프롬프트 형식과 호환 여부 확인 필요
- **성능 회귀**: 마이그레이션 과정에서 일시적 성능 저하 가능성
- **의존성 충돌**: 기존 라이브러리와의 충돌 가능성

### 비즈니스 리스크
- **서비스 중단**: 롤백 계획 필수
- **API 변경**: 클라이언트 영향도 최소화
- **비용 증가**: 동시 요청 증가로 인한 OpenAI API 비용 상승

## 🚀 실행 계획

### 우선순위 1 (즉시 시작)
1. ChatController 비동기화 (1주)
2. 성능 테스트 및 검증 (3일)

### 우선순위 2 (1주 후)
1. LearningAnalyticsController 비동기화 (1주)
2. 전체 비동기 시스템 통합 테스트 (3일)

### 우선순위 3 (3주 후)
1. Spring AI 도입 검토 및 POC (1주)
2. 점진적 마이그레이션 시작 (2-3주)

## 📚 참고 자료
- [Spring AI 공식 문서](https://spring.io/projects/spring-ai)
- [OpenAI API 최적화 가이드](https://platform.openai.com/docs/guides/performance)
- 현재 프로젝트 비동기 구현: `AsyncCorrectionService.kt`, `AsyncConfig.kt`