# R:Note App (ChatGPT 연동) 버전관리 작업노트

---

## ver.0.1.0 — MVP 초기 구현

**작업일**: 2026.02.08
**Git Tag**: `v0.1.0`
**Commit**: `633fb50`

---

### 구현 완료 항목

#### 1. 프로젝트 기반 설정
- Kotlin 1.9.22 + Jetpack Compose (BOM 2024.02.00)
- Room 2.6.1 (로컬 DB) + Navigation Compose 2.7.7
- KSP 기반 어노테이션 프로세싱
- Version Catalog (`libs.versions.toml`) 적용
- minSdk 26 / targetSdk 34 / Release ProGuard 설정

#### 2. 디자인 시스템
- Cloud Dancer (#F0EDE5) 기반 배경 테마
- Sage Green (#7C9A92) 프라이머리 컬러
- 라이트 상태바/네비게이션바 적용
- 커스텀 Typography (한국어 최적화 줄 높이)

#### 3. 데이터 레이어
- `NoteEntity` — ChatGPT 연동 대비 필드 설계
  - `emotionEmoji`, `emotionScore` (0~100), `emotionLabel`
  - `sentimentHint` (positive/neutral/negative)
  - `wordCount`, `isDraft`, `localUserId`
- `NoteDao` — CRUD + Draft 관리 쿼리
- `NoteRepository` — 비즈니스 로직 캡슐화
- `RNoteDatabase` — Room 싱글톤

#### 4. 화면 구현

| 화면 | 파일 | 상태 |
|------|------|------|
| Splash | `SplashScreen.kt` | 완료 — 1.8초 페이드인, 자동 전환 |
| 온보딩 | `OnboardingScreen.kt` | 완료 — 4페이지 HorizontalPager |
| 권한 요청 | `PermissionBottomSheet.kt` | 완료 — 바텀시트, 거부 시에도 진행 |
| 노트 작성 | `NoteScreen.kt` + `NoteViewModel.kt` | 완료 — 이모지 4종, ±10% 퍼센트, 한줄 감정, 제목+본문 |
| 노트 리스트 | `NoteListScreen.kt` + `NoteListViewModel.kt` | 완료 — 날짜별 그룹핑, 편집 모드(체크+삭제), FAB |

#### 5. 핵심 기능
- 감정 이모지 선택 → 퍼센트 자동 세팅 (😀70%, 🙂50%, 😐40%, 😔30%)
- +/- 버튼으로 10% 단위 조절
- Draft 자동 저장 (앱 백그라운드/종료 시)
- 뒤로가기 경고 바텀시트 ("저장하지 않고 나가시겠습니까?")
- 저장 성공 시 리스트로 자동 이동

#### 6. 네비게이션 플로우
```
Splash → (온보딩 미완료) → 온보딩 → 권한 바텀시트 → 노트 작성 → 노트 리스트
Splash → (온보딩 완료)   → 노트 리스트
노트 리스트 → FAB → 노트 작성
노트 리스트 → 아이템 클릭 → 노트 편집
```

---

### 알려진 이슈 및 해결 내역

| 이슈 | 원인 | 해결 |
|------|------|------|
| `AnimatedVisibility` 컴파일 에러 | `Column` 스코프 내 `ColumnScope.AnimatedVisibility` 충돌 | `if` 조건문으로 대체 |
| `loadNote` 반복 호출 | Composable recomposition마다 호출 | `LaunchedEffect`로 래핑 |

---

### 프로젝트 구조

```
app/src/main/java/com/rnote/app/
├── RNoteApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── NoteEntity.kt
│   │   ├── NoteDao.kt
│   │   └── RNoteDatabase.kt
│   └── repository/
│       └── NoteRepository.kt
├── navigation/
│   └── NavGraph.kt
└── ui/
    ├── theme/ (Color, Theme, Type)
    ├── splash/SplashScreen.kt
    ├── onboarding/OnboardingScreen.kt
    ├── components/PermissionBottomSheet.kt
    ├── note/ (NoteScreen, NoteViewModel)
    └── notelist/ (NoteListScreen, NoteListViewModel)
```

---

### 다음 버전 (v0.2.0) 후보 작업

- [ ] JSON Export 기능 (ChatGPT 연동 준비)
- [ ] 주간/월간 감정 통계 화면
- [ ] 노트 검색 기능
- [ ] 다크 모드 지원
- [ ] Google 로그인 연동 + 데이터 마이그레이션
- [ ] ChatGPT API 연동 (감정 분석 리포트)
