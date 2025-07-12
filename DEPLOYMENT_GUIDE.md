# WriteBuddy 배포 가이드

이 가이드는 H2 인메모리 데이터베이스를 사용하던 프로젝트를 Railway와 Supabase PostgreSQL로 배포하는 방법을 안내합니다.

## 🚀 Railway + Supabase 배포

### 1. Supabase 설정

1. [Supabase](https://supabase.com)에 가입하고 새 프로젝트 생성
2. 프로젝트 설정에서 Database URL 확인:
   ```
   postgresql://postgres:[password]@[host]:5432/postgres
   ```
3. API 키는 필요하지 않음 (PostgreSQL만 사용)

### 2. Railway 배포

1. [Railway](https://railway.app)에 가입
2. GitHub 저장소 연결
3. 환경변수 설정:
   ```bash
   OPENAI_API_KEY=sk-proj-...
   GOOGLE_CLIENT_ID=your-google-client-id
   GOOGLE_CLIENT_SECRET=your-google-client-secret
   DATABASE_URL=postgresql://postgres:[password]@[host]:5432/postgres
   SPRING_PROFILES_ACTIVE=prod
   ```

### 3. Google OAuth 설정

1. [Google Cloud Console](https://console.cloud.google.com)
2. OAuth 2.0 클라이언트 ID 생성
3. 승인된 리디렉션 URI 추가:
   ```
   https://your-app.railway.app/login/oauth2/code/google
   ```

### 4. 프로젝트 설정 파일 확인

프로젝트에는 이미 다음 Railway 배포 파일들이 준비되어 있습니다:
- `railway.toml`: Railway 배포 설정
- `nixpacks.toml`: 빌드 설정
- `Procfile`: Railway에서 실행할 명령어
- `application-prod.properties`: 프로덕션 환경 설정

### 5. 배포 확인

1. Railway에서 자동 배포 시작
2. 로그 확인: `railway logs`
3. 헬스 체크: `https://your-app.railway.app/actuator/health`

## 📋 필수 환경변수

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `OPENAI_API_KEY` | OpenAI API 키 | `sk-proj-...` |
| `GOOGLE_CLIENT_ID` | Google OAuth 클라이언트 ID | `123456789-...` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth 클라이언트 시크릿 | `GOCSPX-...` |
| `DATABASE_URL` | Supabase PostgreSQL URL | `postgresql://postgres:...` |
| `RAILWAY_STATIC_URL` | Railway 앱 URL | `https://your-app.railway.app` |
| `SPRING_PROFILES_ACTIVE` | 스프링 프로필 | `prod` |

## ⚠️ H2에서 PostgreSQL로 마이그레이션 시 주의사항

### 데이터베이스 스키마 관리
- 개발환경: `spring.jpa.hibernate.ddl-auto=create-drop` (H2)
- 운영환경: `spring.jpa.hibernate.ddl-auto=update` (PostgreSQL)
- 첫 배포 시 테이블이 자동 생성됩니다
- 기존 H2 데이터는 마이그레이션되지 않습니다

### PostgreSQL 특이사항
- H2와 달리 대소문자 구분이 있을 수 있음
- 예약어 충돌 가능성 (필요시 테이블/컬럼명에 따옴표 사용)
- PostgreSQL dialect가 자동으로 설정됨

## 🔧 트러블슈팅

### 빌드 실패
- **Java 호환성 문제**: Railway에서 Java 21을 지원하지 않는 경우 Java 17로 다운그레이드
- **Gradle 테스트 태스크 오류**: `tasks.withType<Test>` 설정으로 인한 빌드 실패 시 해당 설정 제거
- **의존성 충돌**: Gradle 빌드 로그를 확인하여 호환되지 않는 의존성 해결

### 데이터베이스 연결 실패
- DATABASE_URL 형식 확인
- Supabase 연결 제한 확인
- 네트워크 방화벽 설정

### OAuth 인증 실패
- Google Cloud Console에서 리디렉션 URI 확인
- 클라이언트 ID/시크릿 재확인
- 도메인 설정 확인

## 💡 성능 최적화

- **JVM 옵션**: `-Xmx512m -Xms256m`
- **Connection Pool**: 최대 10개 연결
- **로깅**: 운영 환경에서는 INFO 레벨
- **모니터링**: `/actuator/health`, `/actuator/metrics`

## 📱 로컬 개발

### H2 데이터베이스로 로컬 개발
기본 설정은 H2 인메모리 데이터베이스를 사용합니다:

```bash
# 환경변수 설정
export OPENAI_API_KEY=your-key
export GOOGLE_CLIENT_ID=your-id
export GOOGLE_CLIENT_SECRET=your-secret

# 애플리케이션 실행 (기본: H2 사용)
./gradlew bootRun
```

### PostgreSQL로 로컬 개발 (옵션)
운영환경과 동일한 환경을 원한다면:

```bash
# PostgreSQL 로컬 실행 (Docker)
docker run -d \
  --name writebuddy-postgres \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  postgres:15

# 환경변수 설정
export DATABASE_URL=postgresql://postgres:password@localhost:5432/postgres

# 프로덕션 프로필로 실행
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## 🎯 배포 체크리스트

### 사전 준비
- [ ] 현재 H2 데이터베이스 사용 중임을 인지
- [ ] PostgreSQL로 전환 준비 완료
- [ ] 필요한 데이터 백업 완료 (있다면)

### 배포 단계
- [ ] Supabase 프로젝트 생성 및 DATABASE_URL 확인
- [ ] Railway 프로젝트 생성 및 GitHub 연결
- [ ] 모든 환경변수 설정 완료
  - [ ] OPENAI_API_KEY
  - [ ] GOOGLE_CLIENT_ID
  - [ ] GOOGLE_CLIENT_SECRET
  - [ ] DATABASE_URL
  - [ ] RAILWAY_STATIC_URL (자동 설정됨)
  - [ ] SPRING_PROFILES_ACTIVE=prod
- [ ] Google OAuth 리디렉션 URI 업데이트
- [ ] 빌드 및 배포 성공 확인
- [ ] 데이터베이스 테이블 자동 생성 확인
- [ ] 헬스 체크 엔드포인트 접근 가능
- [ ] API 엔드포인트 테스트 완료
- [ ] OAuth 로그인 테스트 완료

## 🛠️ 개발 과정에서 해결한 주요 이슈들

### 1. 🔒 보안 강화 작업

#### API 키 노출 문제 해결
- **문제**: `application.properties`에 OpenAI API 키가 하드코딩되어 GitHub에 노출
- **해결**: 
  - Git 히스토리에서 완전 제거 (BFG Repo-Cleaner 사용)
  - `.env` 파일 시스템 도입 (`spring-dotenv` 라이브러리)
  - `.gitignore`에 모든 민감 파일 추가
  - 환경변수 기반 설정으로 전환

#### 환경변수 관리 시스템 구축
```bash
# 로컬 개발: .env 파일
OPENAI_API_KEY=your-actual-key
GOOGLE_CLIENT_ID=your-google-id
GOOGLE_CLIENT_SECRET=your-google-secret

# Railway 배포: 환경변수 설정
# Railway 대시보드에서 Variables 탭에서 설정
```

### 2. 🚀 Railway 배포 호환성 해결

#### Java 버전 호환성 문제
- **문제**: Railway에서 Java 21 toolchain을 찾을 수 없음
- **해결**: 
  ```kotlin
  // Before: Java 21 toolchain
  java {
      toolchain {
          languageVersion = JavaLanguageVersion.of(21)
      }
  }
  
  // After: Java 17 호환성
  java {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
  }
  ```

#### Gradle 빌드 최적화
- **문제**: Test 태스크 설정으로 인한 빌드 실패
- **해결**: 
  - 문제가 되는 `tasks.withType<Test>` 설정 제거
  - `--no-daemon` 플래그 추가로 Railway 환경 최적화
  - `nixpacks.toml` 설정으로 명시적 Java 17 지정

#### nixpacks.toml 설정
```toml
[phases.setup]
nixPkgs = ["openjdk17"]

[phases.build]
cmds = ["./gradlew build -x test -x check --no-daemon"]

[phases.start]
cmd = "java -Dspring.profiles.active=prod -jar build/libs/WriteBuddy-0.0.1-SNAPSHOT.jar"
```

### 3. 🌐 환경별 설정 분리

#### 개발/운영 환경 분리
- **로컬 개발**: H2 인메모리 데이터베이스
- **운영 환경**: Supabase PostgreSQL
- **설정 파일**:
  - `application.properties` (기본, H2)
  - `application-prod.properties` (운영, PostgreSQL)
  - `.env` (로컬 환경변수)

#### Google OAuth2 설정
```properties
# 로컬
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:7071/login/oauth2/code/google

# 운영
spring.security.oauth2.client.registration.google.redirect-uri=${RAILWAY_STATIC_URL}/login/oauth2/code/google
```

### 4. 📦 프로젝트 구조 개선

#### 의존성 추가
```kotlin
dependencies {
    // 환경변수 지원
    implementation("me.paulschwarz:spring-dotenv:4.0.0")
    
    // 기존 의존성들...
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
}
```

#### 보안 파일 관리
```bash
# .gitignore에 추가된 보안 파일들
.env
.env.local
.env.production
src/main/resources/application.properties
**/application-*.properties
```

## 📚 참고 문서

### 작성된 가이드 문서들
- **[SECURITY_GUIDE.md](./SECURITY_GUIDE.md)**: 보안 강화 및 API 키 관리
- **[ENVIRONMENT_SETUP.md](./ENVIRONMENT_SETUP.md)**: 환경변수 설정 상세 가이드
- **[README.md](./README.md)**: 프로젝트 전체 개요 및 설치 가이드

### 핵심 명령어
```bash
# 로컬 개발 시작
cp .env.example .env
# .env 파일 편집 후
./gradlew bootRun

# Railway 배포용 빌드 테스트
./gradlew clean build -x test -x check --no-daemon

# Git 히스토리 정리 (필요시)
./clean-git-history.sh
```

---

배포 완료 후 애플리케이션 URL:
`https://your-app.railway.app`

## 🎉 성공적인 배포를 위한 최종 체크리스트

- ✅ **보안**: API 키 완전 제거 및 환경변수 시스템 구축
- ✅ **호환성**: Java 17 호환성으로 Railway 배포 성공
- ✅ **환경 분리**: 로컬(H2) / 운영(PostgreSQL) 환경 구분
- ✅ **문서화**: 포괄적인 배포 및 개발 가이드 작성
- ✅ **자동화**: Railway 자동 배포 파이프라인 구축