# 🔒 WriteBuddy 보안 가이드

## 🚨 긴급 조치 사항

### 1. 노출된 API 키 즉시 무효화
1. [OpenAI Dashboard](https://platform.openai.com/api-keys)에 접속
2. 노출된 키 `sk-proj-jW47iQO...` 찾아서 **즉시 삭제**
3. 새 API 키 발급

### 2. Git 히스토리에서 민감한 정보 제거

#### 옵션 1: BFG Repo-Cleaner 사용 (권장)
```bash
# BFG 다운로드
brew install bfg  # Mac
# 또는 https://rtyley.github.io/bfg-repo-cleaner/ 에서 다운로드

# 백업 생성
cp -r writebuddy writebuddy-backup

# 민감한 텍스트 제거
echo "sk-proj-jW47iQO*" > passwords.txt
bfg --replace-text passwords.txt writebuddy
cd writebuddy
git reflog expire --expire=now --all && git gc --prune=now --aggressive

# 강제 푸시
git push --force
```

#### 옵션 2: git filter-branch 사용
```bash
# 백업 생성
cp -r writebuddy writebuddy-backup

# 히스토리에서 파일 제거
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch src/main/resources/application.properties" \
  --prune-empty --tag-name-filter cat -- --all

# 강제 푸시
git push origin --force --all
git push origin --force --tags
```

## 🛡️ 안전한 설정 방법

### 1. 환경변수 사용

#### Mac/Linux
```bash
# .bashrc 또는 .zshrc에 추가
export OPENAI_API_KEY="your-new-api-key"
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"

# 적용
source ~/.bashrc  # 또는 source ~/.zshrc
```

#### Windows
```cmd
# 시스템 환경변수에 추가
setx OPENAI_API_KEY "your-new-api-key"
setx GOOGLE_CLIENT_ID "your-google-client-id"
setx GOOGLE_CLIENT_SECRET "your-google-client-secret"
```

### 2. IntelliJ IDEA 설정
1. Run → Edit Configurations
2. Environment variables 클릭
3. 다음 추가:
   ```
   OPENAI_API_KEY=your-new-api-key
   GOOGLE_CLIENT_ID=your-google-client-id
   GOOGLE_CLIENT_SECRET=your-google-client-secret
   ```

### 3. application.properties 템플릿 생성
```bash
# application.properties.template 파일 생성
cp src/main/resources/application.properties src/main/resources/application.properties.template

# 실제 사용 시
cp src/main/resources/application.properties.template src/main/resources/application.properties
# 그 다음 실제 값으로 수정
```

## 📋 보안 체크리스트

- [ ] OpenAI API 키 무효화 및 재발급
- [ ] Git 히스토리에서 민감한 정보 제거
- [ ] .gitignore에 application.properties 추가
- [ ] 환경변수로 모든 민감한 정보 이동
- [ ] 팀원들에게 보안 사고 알림
- [ ] 새로운 클론 필요함을 팀원들에게 알림

## ⚠️ 주의사항

1. **Private 레포도 안전하지 않음**
   - 팀원들에게 노출
   - 나중에 Public으로 전환 시 문제
   - GitHub 해킹 시 노출 위험

2. **Git 히스토리는 영구적**
   - 한 번 커밋된 정보는 삭제해도 히스토리에 남음
   - force push 후에도 다른 사람의 로컬에는 남아있을 수 있음

3. **팀 공지 필요**
   - 모든 팀원이 새로 클론해야 함
   - 기존 로컬 저장소는 삭제 권장

## 🔐 향후 보안 강화 방안

1. **GitHub Secrets 사용** (GitHub Actions용)
2. **Pre-commit hooks 설정**으로 API 키 커밋 방지
3. **정기적인 보안 감사**
4. **API 키 로테이션 정책** 수립

---

⚡ **즉시 행동**: 노출된 API 키는 이미 봇들이 수집했을 가능성이 높습니다. **지금 바로** 무효화하세요!