# ✍️ WriteBuddy

WriteBuddy는 영어 문장을 입력하면 교정 결과와 피드백을 제공하는 Kotlin 기반 웹 서비스입니다.  
GPT 연동을 통해 사용자가 작성한 문장을 자연스럽고 올바르게 개선하는 기능을 목표로 합니다.

## 🔧 기술 스택

- **Backend**: Kotlin + Spring Boot 3.4.4
- **Database**: H2 (로컬), PostgreSQL (운영)
- **ORM**: Spring Data JPA + Hibernate
- **Build**: Gradle (Kotlin DSL)
- **Security**: Spring Security + OAuth2 (Google)
- **AI**: OpenAI GPT-4o-mini
- **Testing**: JUnit 5 + AssertJ + Mockito
- **Deployment**: Railway + Supabase

## ✅ 주요 기능

### 📝 문장 교정 시스템
- GPT 기반 영어 문장 교정
- 문법, 맞춤법, 스타일 피드백
- 교정 이력 관리 및 통계

### 📚 학습 지원
- **플래시카드**: 단어 복습 시스템
- **실생활 예제**: AI 생성 학습 예문
- **약점 분석**: 개인별 취약점 분석
- **학습 통계**: 진도 추적 및 성과 분석

### 🔐 사용자 관리
- Google OAuth2 로그인
- 개인화된 학습 데이터
- 안전한 환경변수 관리

## 🚀 빠른 시작

### 1. 저장소 클론
```bash
git clone https://github.com/JeonJe/writebuddy.git
cd writebuddy
```

### 2. 환경변수 설정
```bash
# .env 파일 생성
cp .env.example .env

# .env 파일 편집 (실제 값으로 변경)
nano .env
```

필수 환경변수:
```bash
# OpenAI API 키 (https://platform.openai.com/api-keys)
OPENAI_API_KEY=sk-proj-your-actual-openai-key

# Google OAuth2 (https://console.cloud.google.com)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# 프로필 설정
SPRING_PROFILES_ACTIVE=local
```

### 3. 애플리케이션 실행
```bash
# 개발 모드 실행
./gradlew bootRun

# 애플리케이션 접속
open http://localhost:7071
```

### 4. H2 데이터베이스 콘솔 (개발용)
```
URL: http://localhost:7071/h2-console
JDBC URL: jdbc:h2:mem:testdb
User: sa
Password: (비워둠)
```

## 🏗️ 패키지 구조

```
com.writebuddy.writebuddy
├── controller/          # REST API 컨트롤러
│   ├── dto/            # Request/Response DTO
│   ├── AuthController   # 인증 관련
│   ├── CorrectionController # 교정 요청
│   ├── FlashcardController  # 플래시카드
│   └── LearningAnalyticsController # 학습 분석
├── service/            # 비즈니스 로직
│   ├── OpenAiClient    # GPT API 연동
│   ├── CorrectionService # 교정 서비스
│   └── FlashcardService  # 학습 서비스
├── domain/             # 도메인 모델
│   ├── User            # 사용자
│   ├── Correction      # 교정 결과
│   ├── Flashcard       # 플래시카드
│   └── WeakAreaAnalysis # 약점 분석
├── repository/         # 데이터 접근
├── config/             # 설정
│   ├── SecurityConfig  # 보안 설정
│   ├── OpenAiConfiguration # AI 설정
│   └── CorsConfig      # CORS 설정
└── exception/          # 예외 처리
```

## 🧪 테스트

```bash
# 전체 테스트 실행 (현재 Gradle 호환성 문제로 일시 비활성화)
# ./gradlew test

# 개별 테스트 실행
./gradlew test --tests "CorrectionTest"
```

## 🚀 배포

### Railway + Supabase 배포
```bash
# 1. Supabase PostgreSQL 설정
# 2. Railway 프로젝트 생성
# 3. 환경변수 설정 (Railway 대시보드)
# 4. 자동 배포

# 자세한 가이드
cat DEPLOYMENT_GUIDE.md
```

### 환경변수 (Railway)
```bash
OPENAI_API_KEY=sk-proj-your-key
GOOGLE_CLIENT_ID=your-google-id
GOOGLE_CLIENT_SECRET=your-google-secret
DATABASE_URL=postgresql://postgres:password@host:5432/db
SPRING_PROFILES_ACTIVE=prod
```

## 📚 API 엔드포인트

### 🔐 인증
- `GET /auth/user` - 현재 사용자 정보
- `POST /auth/logout` - 로그아웃

### ✏️ 교정
- `POST /api/corrections` - 문장 교정 요청
- `GET /api/corrections` - 교정 이력 조회

### 📖 학습
- `GET /api/flashcards` - 플래시카드 목록
- `POST /api/flashcards/{id}/review` - 복습 처리
- `GET /api/analytics/weak-areas` - 약점 분석

## 🔒 보안

### 환경변수 관리
- ✅ `.env` 파일로 로컬 관리
- ✅ Git에서 민감정보 완전 제거
- ✅ Railway 환경변수로 운영 관리
- ✅ spring-dotenv로 자동 로드

### 인증/인가
- ✅ Google OAuth2 통합
- ✅ Spring Security 적용
- ✅ 세션 기반 인증

## 📖 개발 가이드

### 코딩 컨벤션
- [CLAUDE.md](./CLAUDE.md) - 프로젝트 규칙 및 가이드라인
- TDD 방식의 도메인 테스트
- AssertJ 기반 검증
- 함수형 프로그래밍 지향

### 보안 가이드
- [SECURITY_GUIDE.md](./SECURITY_GUIDE.md) - 보안 강화 방안
- [ENVIRONMENT_SETUP.md](./ENVIRONMENT_SETUP.md) - 환경설정 가이드

### 배포 가이드
- [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) - Railway 배포 방법

## 🐛 트러블슈팅

### 일반적인 문제
1. **Gradle 빌드 실패**
   ```bash
   # Java 21 확인
   java --version
   
   # Gradle Wrapper 사용
   ./gradlew clean build
   ```

2. **환경변수 로드 실패**
   ```bash
   # .env 파일 존재 확인
   ls -la .env
   
   # spring-dotenv 의존성 확인
   grep spring-dotenv build.gradle.kts
   ```

3. **OpenAI API 오류**
   ```bash
   # API 키 확인
   echo $OPENAI_API_KEY
   
   # 요금 한도 확인 (OpenAI Dashboard)
   ```

### 로그 확인
```bash
# 애플리케이션 로그
tail -f server.log

# Spring Boot 로그 레벨 조정
# application.properties에서 logging.level.com.writebuddy=DEBUG
```

## 🔗 관련 링크

- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Railway Deployment](https://railway.app)
- [Supabase PostgreSQL](https://supabase.com)

## 📜 라이선스

이 프로젝트는 MIT 라이선스 하에 제공됩니다.

---

📧 **문의**: 이슈나 개선사항이 있으시면 GitHub Issues를 이용해 주세요.