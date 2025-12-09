# WriteBuddy 개발 가이드

**Spring Boot + Kotlin** | **OpenAI GPT-4** | **JWT 인증**

## 아키텍처

### JWT 인증
- Access Token: 1시간 / Refresh Token: 7일
- BCrypt 비밀번호 암호화
- USER, ADMIN 역할 (EAGER + @BatchSize로 N+1 해결)

### 비동기 처리
- @Async 기반 OpenAI API 병렬 호출
- 성과: 6.056초 → 4.622초 (23.7% 개선)

```kotlin
@Async("asyncExecutor")
fun generateCorrectionAsync(text: String): CompletableFuture<CorrectionData>
```

## 환경 설정

### 필수 환경변수
```bash
OPENAI_API_KEY=sk-proj-...
JWT_SECRET=your-256-bit-secret
DATABASE_URL=postgresql://...
```

### 로컬 실행
```bash
./gradlew bootRun
```

### 빌드
```bash
./gradlew build -x test
```

## 배포 (Railway)

### 환경변수
```
OPENAI_API_KEY, JWT_SECRET, DATABASE_URL, SPRING_PROFILES_ACTIVE=prod
```

### DB 연결 풀 (Supabase)
```yaml
hikari:
  maximum-pool-size: 3
  minimum-idle: 1
  connection-timeout: 15000
```

## 프로젝트 현황

### 완료
- 문법 교정 시스템
- JWT 인증/인가
- 비동기 처리 (23.7% 성능 개선)
- 통계 대시보드

### 예정
- 일일 목표 게임화
- 캐싱 시스템

## 보안 체크리스트

- [ ] API 키 환경변수 관리
- [ ] .env 파일 .gitignore 추가
- [ ] HTTPS 강제 (Railway 자동)
- [ ] 정기적 API 키 로테이션
