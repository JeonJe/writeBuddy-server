# 비동기 처리 전략

## 왜 비동기가 필요한가?

### 현재 문제점
WriteBuddy는 OpenAI API에 크게 의존하며, 각 요청당 10-15초가 소요됨.
- **문제 1**: 교정 요청시 사용자가 10초 이상 대기
- **문제 2**: 동시 20명만 처리 가능 (스레드 풀 한계)
- **문제 3**: 교정과 예제를 순차 처리하여 시간 2배 소요

### 비즈니스 영향
- 사용자 이탈률 증가 (10초 이상 대기시 50% 이탈)
- 서버 증설 필요 (월 $300 추가 비용)
- 확장성 제한 (트래픽 증가 대응 불가)

## 성능 목표 및 실제 결과

### 실제 측정 결과 (2025-09-21)
| 방식 | 응답 시간 | OpenAI API 시간 | 성능 개선 |
|------|-----------|----------------|-----------|
| **동기 (기존)** | 6.056초 | 5.336초 | - |
| **비동기 (적용후)** | 4.622초 | 4.416초 | **23.7% 개선** |

### 성과 분석
- **실제 개선**: 1.4초 단축 (6.056초 → 4.622초)
- **성능 향상률**: 23.7%
- **비동기 스레드**: WriteBuddy-Async-1에서 정상 처리
- **안정성**: 타임아웃/에러시 동기 방식 자동 폴백

## 적용 전략

### 1단계: OpenAI API 병렬화 ✅ **완료**
**왜**: 교정과 예제를 동시에 요청하면 처리 시간 단축
**어떻게**:
- @Async 기반 비동기 처리
- CompletableFuture로 결과 조합
- **실제 성과**: 6.056초 → 4.622초 (23.7% 개선)

### 2단계: 스마트 캐싱 (차후 적용)
**왜**: 동일한 문장 반복 요청시 불필요한 API 호출
**어떻게**:
- 자주 요청되는 문장 캐싱
- TTL 24시간 설정

## WriteBuddy 특화 적용 영역

### OpenAI API 호출 ✅ **구현 완료**
- **기존**: 동기 방식으로 5.336초 소요
- **개선**: @Async 비동기 처리로 4.416초
- **실제 효과**: 사용자 대기 시간 23.7% 단축 (1.4초 절약)

## 기술 구현 가이드

### @Async 설정
```kotlin
@Configuration
@EnableAsync  // 비동기 활성화 필수
class AsyncConfig : AsyncConfigurer {

    @Bean(name = ["asyncExecutor"])
    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10      // 기본 스레드
        executor.maxPoolSize = 25        // 최대 스레드
        executor.queueCapacity = 100     // 대기 큐
        executor.setThreadNamePrefix("WriteBuddy-Async-")
        executor.initialize()
        return executor
    }
}
```

### @Async 사용 패턴 (OpenAI 병렬 호출)
```kotlin
@Service
class AsyncCorrectionService {
    @Async("asyncExecutor")
    fun generateCorrectionAsync(text: String): CompletableFuture<CorrectionData> {
        val result = openAiClient.generateCorrection(text)
        return CompletableFuture.completedFuture(result)
    }

    @Async("asyncExecutor")
    fun generateExamplesAsync(text: String): CompletableFuture<List<Example>> {
        val result = openAiClient.generateExamples(text)
        return CompletableFuture.completedFuture(result)
    }
}

// 메인 서비스에서 병렬 호출
@Service
class CorrectionService(private val asyncService: AsyncCorrectionService) {
    fun saveWithExamples(text: String) {
        // 교정과 예제를 동시에 시작
        val correctionFuture = asyncService.generateCorrectionAsync(text)
        val examplesFuture = asyncService.generateExamplesAsync(text)

        // 둘 다 완료될 때까지 대기
        CompletableFuture.allOf(correctionFuture, examplesFuture).join()

        // 결과 조합
        val correction = correctionFuture.get()
        val examples = examplesFuture.get()

        // 20초 → 10초로 단축!
        return saveToDatabase(correction, examples)
    }
}
```


### ThreadPool 사이징 가이드
```
최적 스레드 수 = CPU 코어 수 × (1 + 대기시간/처리시간)

예시: 4코어, OpenAI 대기 10초, 처리 0.1초
최적 = 4 × (1 + 10/0.1) = 404 스레드

하지만 DB 연결 풀 고려하여 현실적으로 25-50개 권장
```

## 주의사항

### @Async 함정들
1. **같은 클래스 내부 호출 안됨** - 프록시가 동작하지 않음
2. **private 메서드 안됨** - AOP 프록시 제한
3. **반환 타입** - void 또는 Future/CompletableFuture만 가능
4. **예외 처리** - AsyncUncaughtExceptionHandler 구현 필요

### 트랜잭션
- 비동기 메서드는 새로운 트랜잭션에서 실행
- @Transactional과 @Async 함께 쓸 때 주의
- 트랜잭션 전파 레벨 확인 필수

### 에러 처리
- OpenAI 타임아웃시 동기 방식 폴백
- Circuit Breaker로 연쇄 실패 방지
- CompletableFuture.exceptionally() 활용

### 모니터링
```kotlin
// ThreadPool 상태 확인
@Component
class AsyncMonitor(
    @Qualifier("asyncExecutor")
    private val executor: ThreadPoolTaskExecutor
) {
    fun getStatus() = mapOf(
        "activeCount" to executor.activeCount,
        "poolSize" to executor.poolSize,
        "queueSize" to executor.queueSize,
        "remainingCapacity" to executor.queueCapacity
    )
}
```

## 검증 계획
1. 부하 테스트: 동시 100명 시뮬레이션
2. 응답 시간: 95%가 7초 이내
3. 에러율: 0.1% 미만 유지

---
*Last Updated: 2025-09-21*