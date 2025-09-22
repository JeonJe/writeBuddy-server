# WriteBuddy 기술 개발 일지

**프로젝트**: AI 기반 영어 문법 교정 서비스
**기술 스택**: Spring Boot + Kotlin + OpenAI API
**마지막 업데이트**: 2025-09-22

---

## 🚀 Phase 4: 비동기 처리 시스템 구현

### 문제 인식
기존 교정 API가 동기 방식으로 구현되어 OpenAI API 호출 시 블로킹이 발생했습니다. 교정과 예시 생성을 순차적으로 처리하여 전체 응답 시간이 6초를 초과하는 성능 문제가 있었습니다.

### 해결 방안
Spring Boot의 `@Async` 어노테이션을 활용하여 교정과 예시 생성을 병렬 처리하는 비동기 시스템을 구현했습니다.

### 구현 과정

#### 1. 비동기 설정 구성
```kotlin
@Configuration
@EnableAsync
class AsyncConfig {
    @Bean(name = ["asyncExecutor"])
    fun asyncExecutor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("WriteBuddy-Async-")
        executor.initialize()
        return executor
    }
}
```

#### 2. 비동기 서비스 클래스 생성
```kotlin
@Service
class AsyncCorrectionService {
    @Async("asyncExecutor")
    fun generateCorrectionAsync(text: String): CompletableFuture<CorrectionData> {
        val result = openAiClient.generateCorrectionWithTranslations(text)
        return CompletableFuture.completedFuture(result)
    }

    @Async("asyncExecutor")
    fun generateExamplesAsync(text: String): CompletableFuture<List<String>> {
        val examples = realExampleService.generateExamples(text)
        return CompletableFuture.completedFuture(examples)
    }
}
```

#### 3. 컨트롤러 레벨 적용
```kotlin
fun saveWithExamplesAsync(request: CorrectionRequest, userId: Long?): Pair<Correction, List<String>> {
    val correctionFuture = asyncCorrectionService.generateCorrectionAsync(request.originSentence)
    val examplesFuture = asyncCorrectionService.generateExamplesAsync(request.originSentence)

    CompletableFuture.allOf(correctionFuture, examplesFuture).join()

    val correctionData = correctionFuture.get()
    val examples = examplesFuture.get()

    return Pair(savedCorrection, examples)
}
```

### 성과 측정
- **기존 응답 시간**: 6.056초
- **개선 후 응답 시간**: 4.622초
- **성능 향상**: 23.7% 개선

---

## 🔄 Phase 4.5: Spring AI 마이그레이션

### 문제 인식
기존 OpenAI API 연동이 RestClient를 통한 수동 JSON 파싱 방식으로 구현되어 있어 코드 복잡도가 높고 타입 안전성이 부족했습니다.

### 해결 방안
Spring AI 프레임워크를 도입하여 OpenAI API 연동을 표준화하고 자동 응답 매핑을 통해 코드를 간소화했습니다.

### 구현 과정

#### 1. 의존성 추가
```kotlin
// build.gradle.kts
implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M4")
implementation("org.springframework.ai:spring-ai-retry:1.0.0-M4")
```

#### 2. 설정 마이그레이션
```yaml
# application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.3
      retry:
        max-attempts: 3
        backoff:
          initial-interval: 1s
          multiplier: 2
```

#### 3. Spring AI 서비스 구현
```kotlin
@Service
class SpringAiChatService(private val chatClient: ChatClient) {
    fun generateChatResponse(question: String): String {
        return chatClient.prompt()
            .user(question)
            .call()
            .content()
    }
}
```

### 성과
- **코드량 50% 감소**: 수동 JSON 파싱 로직 제거
- **타입 안전성 향상**: 자동 응답 매핑으로 런타임 에러 방지
- **유지보수성 개선**: Spring 생태계 표준 패턴 적용

---

## 🧹 Phase 5: 코드 정리 및 최적화

### 문제 인식
프로젝트 진행 과정에서 미사용 컨트롤러와 서비스들이 누적되어 코드베이스가 복잡해졌습니다. 또한 무거운 전체 조회 로직이 성능에 부정적 영향을 미쳤습니다.

### 해결 방안
미사용 코드를 제거하고 경량 조회만 사용하도록 API를 단순화했습니다.

### 구현 과정

#### 1. 미사용 컨트롤러 제거
- `LearningAnalyticsController.kt` 삭제
- `StatisticsController.kt` 삭제

#### 2. 관련 서비스 및 DTO 정리
- `LearningAnalyticsService.kt` 삭제
- `StatisticsService.kt` 삭제
- `UnifiedStatisticsResponse.kt` 삭제
- `UserWeakAreasSummaryResponse.kt` 삭제

#### 3. API 단순화
```kotlin
// 기존: lightweight 파라미터로 분기 처리
fun getAll(lightweight: Boolean): Any { ... }

// 개선: 경량 조회만 지원
fun getAll(): List<CorrectionListResponse> {
    val projections = correctionService.getAllLightweight(page, size)
    return projections.map { CorrectionListResponse.from(it) }
}
```

### 성과
- **코드베이스 간소화**: 불필요한 파일 8개 제거
- **API 명확성 향상**: 단일 책임 원칙 적용
- **메모리 사용량 감소**: 무거운 엔티티 로딩 제거

---

## 🎯 현재 진행 중: Phase 6 - 일일 목표 달성 게임화

### 목표
사용자 참여도 향상을 위한 게임화 요소 도입으로 학습 동기를 부여하는 시스템을 구현합니다.

### 계획된 기능
- **일일 목표 설정**: 사용자별 하루 문제 수 목표
- **점수 목표 관리**: 평균 점수 목표 설정
- **진행률 시각화**: 직관적인 UI로 달성 현황 표시
- **성취 피드백**: 목표 달성시 보상 시스템

---

## 🔐 예정 작업: Phase 7 - 인증/인가 시스템

### 목표
Spring Security와 JWT 토큰을 활용한 안전한 사용자 인증 시스템을 구축합니다.

### 학습 포인트
- **Spring Security 생태계** 이해
- **JWT 토큰 생명주기** 관리
- **역할 기반 접근 제어** (RBAC) 구현
- **API 보안** 강화

---

## 📚 기술적 학습 성과

### 비동기 프로그래밍
- `@Async` 어노테이션 활용
- `CompletableFuture`를 통한 병렬 처리
- Thread Pool 설정 및 관리

### Spring AI 프레임워워크
- OpenAI API 통합 자동화
- 타입 안전한 응답 처리
- 재시도 및 에러 처리 표준화

### 성능 최적화
- API 응답 시간 측정 및 개선
- 메모리 사용량 최적화
- 코드 복잡도 관리

### 클린 코드
- 단일 책임 원칙 적용
- 불필요한 코드 제거
- 가독성 및 유지보수성 향상