# ✍️ WriteBuddy

WriteBuddy는 영어 문장을 입력하면 교정 결과와 피드백을 제공하는 Kotlin 기반 웹 서비스입니다.  
GPT 연동을 통해 사용자가 작성한 문장을 자연스럽고 올바르게 개선하는 기능을 목표로 합니다.

## 🔧 기술 스택

- **Backend**: Kotlin + Spring Boot 3.4.4
- **Database**: PostgreSQL (Supabase)
- **ORM**: Spring Data JPA + Hibernate
- **Build**: Gradle (Kotlin DSL)
- **Security**: Spring Security + OAuth2 (Google)
- **AI**: OpenAI GPT-4o-mini
- **Testing**: JUnit 5 + AssertJ + Mockito

## ✅ 주요 기능

### 📝 문장 교정 시스템
- GPT 기반 영어 문장 교정
- 문법, 맞춤법, 스타일 피드백 제공
- 1-10점 교정 점수 시스템
- 교정 이력 관리 및 통계

### 📚 학습 지원
- **실생활 예제**: AI 생성 실제 사용 예문
- **약점 분석**: 개인별 취약점 분석
- **학습 통계**: 진도 추적 및 성과 분석
- **통합 통계 API**: 단일 호출로 모든 통계 제공

### 🔐 사용자 관리
- Google OAuth2 로그인
- 개인화된 학습 데이터
- 안전한 환경변수 관리

## 🏗️ 아키텍처

```
com.writebuddy.writebuddy
├── controller/          # REST API 컨트롤러
│   ├── dto/            # Request/Response DTO
│   ├── CorrectionController # 교정 요청
│   └── StatisticsController # 통계 API
├── service/            # 비즈니스 로직
│   ├── OpenAiClient    # GPT API 연동
│   ├── CorrectionService # 교정 서비스
│   └── StatisticsService # 통계 서비스
├── domain/             # 도메인 모델
│   ├── User            # 사용자
│   ├── Correction      # 교정 결과
│   └── RealExample     # 실사용 예제
├── repository/         # 데이터 접근
└── config/             # 설정
    ├── SecurityConfig  # 보안 설정
    └── OpenAiConfiguration # AI 설정
```

## 📚 API 엔드포인트

### ✏️ 교정
- `POST /corrections` - 문장 교정 요청
- `GET /corrections` - 교정 이력 조회
- `PUT /corrections/{id}/favorite` - 즐겨찾기 토글
- `PUT /corrections/{id}/memo` - 노트 수정

### 📊 통계 (최적화된 통합 API)
- `GET /statistics` - 모든 통계 데이터 단일 호출

### 🔐 인증
- `GET /auth/user` - 현재 사용자 정보
- `POST /logout` - 로그아웃

## 🚀 성능 최적화

### 통합 통계 시스템
- **API 호출 최적화**: 7-8개 API → 1개 API (87.5% 감소)
- **메모리 사용량**: 90% 감소 (집계 쿼리 사용)
- **연결 풀 최적화**: HikariCP 설정으로 Supabase 프리티어 대응
- **타임아웃 방지**: 연결 누수 감지 및 빠른 해제

## 📚 Documentation

### 📋 작업 진행
- [작업 진행 현황](./docs/progress/WORK_PROGRESS.md) - 프로젝트 전체 진행 상태

### 🏗️ 아키텍처 & 설계
- [비동기 처리 구현](./docs/architecture/ASYNC_IMPLEMENTATION.md) - 동시성 처리 및 성능 최적화

### 💻 개발 가이드
- [프론트엔드 개발 가이드](./docs/development/FRONTEND_GUIDE.md) - React 통합 가이드

### 🚀 운영 & 배포
- [배포 및 보안 가이드](./docs/operations/DEPLOYMENT_SECURITY.md) - 프로덕션 배포 절차

## 🚀 빠른 시작

### 환경 설정
```bash
# 환경변수 설정
export OPENAI_API_KEY=your-key
export GOOGLE_CLIENT_ID=your-id
export GOOGLE_CLIENT_SECRET=your-secret
```

### 빌드 및 실행
```bash
# 테스트 실행
./gradlew test

# 애플리케이션 실행
./gradlew bootRun

# 빌드
./gradlew build
```

서버는 http://localhost:7071 에서 실행됩니다.

## 📜 라이선스

이 프로젝트는 MIT 라이선스 하에 제공됩니다.