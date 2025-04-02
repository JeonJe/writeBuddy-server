# ✍️ WriteBuddy

WriteBuddy는 영어 문장을 입력하면 교정 결과와 피드백을 제공하는 Kotlin 기반 웹 서비스입니다.  
GPT 연동을 통해 사용자가 작성한 문장을 자연스럽고 올바르게 개선하는 기능을 목표로 합니다.

## 🔧 기술 스택

- Kotlin + Spring Boot
- Spring Data JPA + H2 (테스트용)
- Gradle (Kotlin DSL)
- JUnit 5 + AssertJ

## ✅ 주요 기능

- 문장 교정 요청 저장
- 교정 결과 목록 조회
- 작성/수정 시간 및 사용자 추적 (Auditing)

## 📁 패키지 구조

```
com.writebuddy.writebuddy
├── controller
├── service
├── domain
└── repository
```

## 🧪 테스트 실행

```bash
./gradlew test
```

## 🗓️ TODO

- [ ] GPT를 통한 교정 자동화
- [ ] Spring Security 적용
- [ ] 간단한 프론트엔드 연동
