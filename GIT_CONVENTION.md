# R:Note 형상관리 기준

---

## 1. 브랜치 전략

```
main          ← 릴리즈 전용 (Play Store 배포 가능 상태만)
  └─ develop  ← 개발 통합 브랜치 (다음 릴리즈 준비)
       ├─ feature/xxx   ← 기능 개발
       ├─ fix/xxx       ← 버그 수정
       └─ hotfix/xxx    ← 긴급 수정 (main에서 분기 → main/develop 병합)
```

| 브랜치 | 분기 기준 | 병합 대상 | 용도 |
|--------|----------|----------|------|
| `main` | — | — | 릴리즈 태그가 붙는 안정 브랜치 |
| `develop` | `main` | `main` | 개발 통합, 다음 버전 준비 |
| `feature/*` | `develop` | `develop` | 신규 기능 |
| `fix/*` | `develop` | `develop` | 일반 버그 수정 |
| `hotfix/*` | `main` | `main` + `develop` | 배포 후 긴급 수정 |

### 브랜치 네이밍 규칙

```
feature/감정통계-화면
feature/json-export
fix/노트저장-크래시
hotfix/db-migration-오류
```
- 영문 + 한글 혼용 가능, 공백 대신 하이픈(`-`) 사용
- 짧고 목적이 명확하게

---

## 2. 커밋 메시지 컨벤션

### 형식

```
<type>: <subject>

[body (선택)]

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
```

### Type 종류

| Type | 용도 | 예시 |
|------|------|------|
| `feat` | 새 기능 | `feat: 주간 감정 통계 화면 추가` |
| `fix` | 버그 수정 | `fix: 노트 저장 시 빈 본문 크래시 해결` |
| `refactor` | 리팩토링 (기능 변화 없음) | `refactor: NoteRepository DI 구조 개선` |
| `style` | 코드 포맷, 줄바꿈 등 | `style: 미사용 import 정리` |
| `docs` | 문서 | `docs: v0.2.0 작업노트 추가` |
| `chore` | 빌드, 설정 등 | `chore: Gradle 8.6 업데이트` |
| `test` | 테스트 | `test: NoteViewModel 단위 테스트 추가` |

### 규칙
- Subject는 **한국어** 사용, 50자 이내
- 마침표 없이 종결
- Body는 "왜" 변경했는지 필요 시 작성
- AI 협업 시 `Co-Authored-By` 태그 포함

---

## 3. 태그 & 버전 규칙

### Semantic Versioning

```
v{MAJOR}.{MINOR}.{PATCH}
```

| 구분 | 올리는 시점 | 예시 |
|------|-----------|------|
| MAJOR | 대규모 변경, 호환성 깨짐 | `v1.0.0` (정식 출시) |
| MINOR | 기능 추가, 화면 추가 | `v0.2.0` (통계 화면 추가) |
| PATCH | 버그 수정, 미세 조정 | `v0.1.1` (크래시 수정) |

### 태그 생성 규칙
- `main` 브랜치에서만 태그 생성
- 태그 메시지에 변경 요약 포함

```bash
git tag -a v0.2.0 -m "feat: 주간 감정 통계 및 JSON Export 추가"
```

---

## 4. 릴리즈 플로우

```
1. develop에서 기능 개발 완료
2. develop → main 병합 (PR 또는 merge)
3. main에서 태그 생성 (v0.X.0)
4. CHANGELOG.md에 해당 버전 작업노트 추가
5. 빌드 → Play Store 배포
```

---

## 5. 버전 이력

| 버전 | 태그 | 날짜 | 주요 내용 |
|------|------|------|----------|
| v0.1.0 | `v0.1.0` | 2026.02.08 | MVP 초기 구현 (감정 기록, 리스트, Draft) |

---

## 6. 주의사항

- `main` 브랜치에 직접 커밋 금지 (hotfix 제외)
- `.jks`, `.keystore`, `google-services.json` 등 시크릿 파일 커밋 금지
- 빌드 산출물 (`.apk`, `.aab`) 커밋 금지
- DB 스키마 변경 시 Room Migration 코드 필수 포함
