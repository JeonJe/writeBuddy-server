# 🚀 WriteBuddy 배포 및 보안 관리 가이드

## 📋 필수 환경변수 설정

### Railway 배포용 환경변수
```bash
OPENAI_API_KEY=sk-proj-...
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
DATABASE_URL=postgresql://postgres:[password]@[host]:5432/postgres
SPRING_PROFILES_ACTIVE=prod
PORT=7071  # Railway에서 자동 설정됨
```

### 로컬 개발용 (.env 파일)
```bash
OPENAI_API_KEY=your-api-key
GOOGLE_CLIENT_ID=your-google-id
GOOGLE_CLIENT_SECRET=your-google-secret
DATABASE_URL=jdbc:h2:mem:testdb  # 로컬에서는 H2 사용
```

## 🚀 Railway 배포 단계

### 1. Supabase PostgreSQL 설정
1. [Supabase](https://supabase.com) 프로젝트 생성
2. Database URL 확인: `postgresql://postgres:[password]@[host]:5432/postgres`

### 2. Railway 배포
1. [Railway](https://railway.app)에서 GitHub 저장소 연결
2. 환경변수 설정 (위 목록 참조)
3. 자동 배포 확인

### 3. Google OAuth 설정
1. [Google Cloud Console](https://console.cloud.google.com)에서 OAuth 클라이언트 생성
2. 리다이렉션 URI 추가: `https://writebuddy.up.railway.app/login/oauth2/code/google`

### 4. CORS 설정 (프론트엔드 배포 시)
프론트엔드가 Vercel에 배포된 경우 CORS 설정이 자동으로 적용됩니다:
- `https://writebuddy.vercel.app` (메인 도메인)
- `https://writebuddy-*.vercel.app` (브랜치 배포)
- 모든 localhost 포트 (개발용)
- Google OAuth 도메인

## 🔒 보안 관리

### API 키 노출 시 긴급 조치
1. **즉시 API 키 무효화**
   - OpenAI Dashboard에서 노출된 키 삭제
   - 새 API 키 발급 및 환경변수 업데이트

2. **Git 히스토리 정리** (BFG Repo-Cleaner 사용)
```bash
# 백업 생성
cp -r writebuddy writebuddy-backup

# 민감한 정보 제거
echo "sk-proj-*" > passwords.txt
bfg --replace-text passwords.txt writebuddy
cd writebuddy
git reflog expire --expire=now --all && git gc --prune=now --aggressive
git push --force
```

### 보안 설정 파일
- `.env` 파일 사용 (spring-dotenv 라이브러리)
- `.gitignore`에 모든 민감 파일 추가
- 환경변수 기반 설정 (`${VARIABLE_NAME}` 형태)

## 🛠️ 주요 설정 파일

### application.yml (YAML 형태로 통일)
```yaml
server:
  port: ${PORT:7071}
  address: 0.0.0.0

spring:
  datasource:
    url: ${DATABASE_URL}
    driver-class-name: org.postgresql.Driver

openai:
  api:
    key: ${OPENAI_API_KEY}
    base-url: https://api.openai.com/v1
```

### nixpacks.toml (Railway 빌드 설정)
```toml
[phases.setup]
nixPkgs = ["openjdk21"]

[phases.build]
cmds = ["./gradlew build -x test -x check --no-daemon"]

[phases.start]
cmd = "java -Dspring.profiles.active=prod -jar build/libs/WriteBuddy-0.0.1-SNAPSHOT.jar"
```

## 🔧 트러블슈팅

### 배포 실패 시
- **502 에러**: 환경변수 누락 확인 (특히 DATABASE_URL, OPENAI_API_KEY)
- **빌드 실패**: Java 21 toolchain 확인
- **CORS 에러**: 프론트엔드 URL에서 포트 번호 제거

### 주요 API 엔드포인트
```bash
# 문장 교정
POST https://writebuddy.up.railway.app/corrections
{"originSentence": "I goes to school every day"}

# 모든 교정 내역
GET https://writebuddy.up.railway.app/corrections

# 통계
GET https://writebuddy.up.railway.app/corrections/statistics
```

## ⚠️ 보안 체크리스트

- [ ] API 키를 환경변수로 관리
- [ ] .env 파일을 .gitignore에 추가
- [ ] 프로덕션에서 민감한 로그 비활성화
- [ ] CORS 설정으로 허용된 도메인만 접근 가능
- [ ] HTTPS 강제 사용 (Railway 자동 제공)
- [ ] 정기적인 API 키 로테이션

## 💡 핵심 명령어

```bash
# 로컬 개발
./gradlew bootRun

# Railway 배포용 빌드 테스트
./gradlew clean build -x test -x check --no-daemon

# API 테스트 (포트 번호 제외)
curl -X POST "https://writebuddy.up.railway.app/corrections" \
  -H "Content-Type: application/json" \
  -d '{"originSentence": "test sentence"}'
```

---

이 가이드를 통해 다른 AI가 배포 및 보안 관리를 수행할 수 있습니다.
