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

### 다음 버전 후보 작업

- [ ] 주간/월간 감정 통계 화면
- [ ] 노트 검색 기능
- [ ] 다크 모드 지원
- [ ] Google 로그인 연동 + 데이터 마이그레이션
- [ ] ChatGPT API 직접 연동 (감정 분석 리포트)

---
---

## ver.0.2.0 — ChatGPT 내보내기 & 분석 요청

**작업일**: 2026.02.12
**Git Tag**: `v0.2.0`

---

### 구현 완료 항목

#### 1. 내보내기 데이터 모델 (`export/ExportModels.kt`)
- `ExportPackage` — 전체 내보내기 패키지 구조
  - `export_info`: 내보내기 메타정보 (기간, 총 개수, 평균 감정점수, 감정분포)
  - `emotion_timeline`: 시계열 감정 데이터 (날짜별 emoji + score + sentiment)
  - `notes`: 개별 노트 상세 데이터
- `ExportMapper` — NoteEntity → ExportPackage 변환 로직
  - 평균 감정점수 자동 계산
  - sentiment 분포 (positive/neutral/negative) 집계
  - 날짜 포맷팅 (yyyy-MM-dd HH:mm)

#### 2. ChatGPT 프롬프트 시스템 (`export/ExportModels.kt`)
- `PromptType` enum — 3종 분석 프롬프트
  - **감정 패턴 분석**: 감정 흐름, 트리거, 변화 추이 분석 요청
  - **주간/월간 리포트**: 요약 통계 리포트 생성 요청
  - **종합 심리 상담**: 따뜻한 상담 관점의 조언 요청
- 텍스트 내보내기 시 프롬프트 자동 삽입
- 감정 타임라인 + 개별 노트 데이터 구조화 텍스트 생성

#### 3. 내보내기 유틸 (`export/ExportHelper.kt`)
- **JSON 파일 내보내기**: Gson PrettyPrinting → 캐시 파일 → FileProvider URI 공유
- **ChatGPT 텍스트 공유**: text/plain Share Intent로 ChatGPT 앱에 직접 전달
- FileProvider 설정 (`xml/file_paths.xml`, AndroidManifest 등록)

#### 4. UI 변경 (`notelist/`)

| 기능 | 위치 | 설명 |
|------|------|------|
| ⋮ 메뉴 | NoteListTopBar | MoreVert 아이콘 → DropdownMenu |
| ChatGPT 분석 요청 | DropdownMenu | 전체 노트 대상 프롬프트 선택 → 공유 |
| JSON 내보내기 | DropdownMenu | 전체 노트 JSON 파일 공유 |
| 선택 내보내기 | 편집모드 TopBar | Share 아이콘 → 선택 노트 ChatGPT 공유 |
| 프롬프트 선택 | BottomSheet | 3종 분석 유형 선택 UI |

#### 5. ViewModel 확장 (`NoteListViewModel.kt`)
- `ExportTarget` enum (ALL / SELECTED)
- `showExportMenu` / `showPromptSelector` 상태 관리
- `getNotesForExport()` — 대상에 따라 전체/선택 노트 반환

---

### 프로젝트 구조 변경

```
app/src/main/java/com/rnote/app/
├── export/                          # [NEW] 내보내기 모듈
│   ├── ExportModels.kt              # 데이터 모델, 매퍼, 프롬프트
│   └── ExportHelper.kt              # JSON/텍스트 생성, Share Intent
├── ui/notelist/
│   ├── NoteListScreen.kt            # [MODIFIED] 메뉴, 프롬프트 시트 추가
│   └── NoteListViewModel.kt         # [MODIFIED] 내보내기 상태 관리

app/src/main/res/
└── xml/file_paths.xml               # [NEW] FileProvider 경로 설정
```

---

### 다음 버전 후보 작업

- [ ] 주간/월간 감정 통계 화면
- [ ] 노트 검색 기능
- [ ] 다크 모드 지원
- [ ] ChatGPT API 직접 연동 (앱 내 분석 결과 표시)
- [ ] 한국어 키워드 자동 추출 (형태소 분석)

---
---

## ver.0.2.1 — UX 개선 (감정 스케일, 전체선택, 시스템 UI)

**작업일**: 2026.02.12
**Git Tag**: `v0.2.1`

---

### 구현 완료 항목

#### 1. 11단계 감정 스케일 (`NoteViewModel.kt`)
- 기존 4단계 → 11단계 (0~100%, 10% 단위)
- `EmotionLevel` 데이터 모델: emoji + score + English label + sentiment
- 각 레벨에 확정된 sentiment 매핑 (negative/neutral/positive)
- DB 스키마 변경 없음 (기존 필드 재활용)

| Score | Emoji | Label | Sentiment |
|-------|-------|-------|-----------|
| 0% | 😭 | Worst | negative |
| 10% | 😢 | Terrible | negative |
| 20% | 😞 | Very Bad | negative |
| 30% | 😕 | Bad | negative |
| 40% | 🙁 | A Bit Down | neutral |
| 50% | 😐 | Neutral | neutral |
| 60% | 🙂 | A Bit Good | positive |
| 70% | 😊 | Good | positive |
| 80% | 😄 | Very Good | positive |
| 90% | 😆 | Great | positive |
| 100% | 🤩 | Amazing | positive |

#### 2. 감정 선택 UI 재설계 (`NoteScreen.kt`)
- 듀얼 버튼: [이모지] — 퍼센트 — [English Label]
- 클릭 시 11개 옵션 FlowRow 그리드 확장 (애니메이션)
- 옵션 선택 → emoji + score + label 동시 연동 → 자동 접힘
- ±10% 수동 조절 버튼 제거
- 한줄 감정 자유 텍스트 → 구조화된 English label 대체

#### 3. 편집모드 전체선택 (`NoteListViewModel.kt` + `NoteListScreen.kt`)
- 전체선택 / 전체해제 토글 TextButton
- TopBar 좌측 "N개 선택" 옆에 배치
- `selectAll()` / `deselectAll()` ViewModel 함수 추가

#### 4. 시스템 상태바 영역 해결
- `statusBarsPadding()` 적용: NoteScreen, NoteListScreen, OnboardingScreen
- `navigationBarsPadding()` 적용: OnboardingScreen
- SplashScreen: 전체 화면 센터 정렬이므로 별도 처리 불필요

#### 5. 뒤로가기 2회 앱 종료 (`NoteListScreen.kt`)
- `BackHandler` + `System.currentTimeMillis()` 비교
- 첫 번째: Toast "한 번 더 누르면 앱이 종료됩니다"
- 2초 이내 두 번째: `Activity.finish()` 호출
- NoteListScreen에서만 동작 (NoteScreen은 기존 저장 확인 유지)

---
---

## ver.0.2.2 — UI 전면 개선 (컬러, 폰트, 아이콘, 로고, 컴포넌트)

**작업일**: 2026.02.12
**Git Tag**: `v0.2.2`

---

### 구현 완료 항목

#### 1. 컬러 시스템 변경 (`ui/theme/Color.kt`)
- `SagePrimary`: Sage Green (#7C9A92) → 차콜 (#2F2F2F)
- `TextSecondary`: #888888 → #8C8A85
- `CardBackground`: #F7F5F0 → #FFFFFF (화이트)
- 파생 컬러(`SagePrimaryLight`, `SagePrimaryDark`) 동시 업데이트

#### 2. Hahmlet 폰트 적용 (`ui/theme/Type.kt`)
- `HahmletFont` 폰트 패밀리 등록 (`res/font/hahmlet.ttf`, Medium weight)
- `HahmletStyle` 공통 스타일: Medium / letterSpacing -2%
- 적용 영역: 노트 제목, 노트 본문, 노트 리스트 아이템 제목

#### 3. 커스텀 아이콘 추가 (`res/drawable/`)
- `ic_openai.xml` — OpenAI 로고 (ChatGPT 내보내기 버튼)
- `ic_pencil_square.xml` — 편집 모드 진입 아이콘
- `ic_pen_fill.xml` — FAB (새 노트 작성) 아이콘
- Bootstrap Icons SVG → Android Vector Drawable 변환

#### 4. 로고 이미지 통합
- `res/drawable/logo.png` — 512×512 커스텀 로고
- 스플래시 화면: 120dp + RoundedCornerShape(24.dp) 클리핑
- 노트 리스트 헤더: 36dp + RoundedCornerShape(8.dp) 클리핑
- 앱 아이콘: Adaptive Icon foreground (61% safe zone 적용, 다중 해상도 mipmap)
- 앱 아이콘 배경: #2F2F2F (차콜)

#### 5. 내보내기 UX 간소화 (`notelist/`)
- DropdownMenu (⋮) 제거 → OpenAI 아이콘 직접 노출
- 일반 모드: OpenAI 아이콘 → 전체 노트 ChatGPT 분석 요청
- 편집 모드: OpenAI 아이콘 → 선택 노트 ChatGPT 분석 요청
- JSON 내보내기 메뉴 항목 제거 (ChatGPT 공유에 집중)
- `NoteListViewModel`에서 `showExportMenu` 상태 제거

#### 6. RNoteButton 공통 컴포넌트 (`ui/components/RNoteButton.kt`)
- 배경: SagePrimary (차콜) / 텍스트: CloudDancer (크림)
- RoundedCornerShape(12.dp), height 52.dp
- Typography의 `labelLarge` 색상 오버라이드 이슈 해결
  - 원인: Typography에 명시적 `color = TextPrimary` 설정 → Button의 `contentColor` 무시
  - 해결: 컴포넌트 내부에서 `color = CloudDancer` 명시 지정
- 적용: OnboardingScreen, PermissionBottomSheet

#### 7. Android 12+ 시스템 스플래시 처리
- `res/values-v31/themes.xml` — API 31+ 전용 테마
- `windowSplashScreenBackground`: #F0EDE5 (CloudDancer)
- `windowSplashScreenAnimatedIcon`: 투명 drawable → 아이콘 숨김
- `res/drawable/splash_transparent.xml` — 1dp 투명 shape
- 효과: 시스템 스플래시에서 앱 아이콘 중복 노출 방지

#### 8. 기타 변경
- 앱 이름 `R:note` → `R:Note` 통일 (`strings.xml`)
- 노트 작성 제목 플레이스홀더: "제목 (선택)" → "제목"
- 노트 작성 본문 플레이스홀더: "오늘의 감정을 자유롭게 적어보세요..." → "오늘의 감정과 생각을 자유롭게 적어보세요."
- `.gitignore`: 소스 에셋 폴더 (`/font/`, `/icon/`, `/logo.png`) 제외 추가

---

### 프로젝트 구조 변경

```
app/src/main/java/com/rnote/app/
├── ui/
│   ├── components/
│   │   ├── PermissionBottomSheet.kt  # [MODIFIED] RNoteButton 적용
│   │   └── RNoteButton.kt           # [NEW] 공통 버튼 컴포넌트
│   ├── theme/
│   │   ├── Color.kt                 # [MODIFIED] 차콜 컬러 시스템
│   │   └── Type.kt                  # [MODIFIED] Hahmlet 폰트 추가
│   ├── splash/SplashScreen.kt       # [MODIFIED] 로고 이미지 적용
│   ├── onboarding/OnboardingScreen.kt # [MODIFIED] RNoteButton 적용
│   ├── note/NoteScreen.kt           # [MODIFIED] Hahmlet 폰트, 플레이스홀더
│   └── notelist/
│       ├── NoteListScreen.kt        # [MODIFIED] 로고, 아이콘, UX 간소화
│       └── NoteListViewModel.kt     # [MODIFIED] 상태 정리

app/src/main/res/
├── drawable/
│   ├── logo.png                     # [NEW] 앱 로고
│   ├── ic_openai.xml                # [NEW] OpenAI 아이콘
│   ├── ic_pen_fill.xml              # [NEW] 펜 아이콘 (FAB)
│   ├── ic_pencil_square.xml         # [NEW] 편집 아이콘
│   ├── ic_launcher_background.xml   # [MODIFIED] 배경 #2F2F2F
│   └── splash_transparent.xml       # [NEW] 투명 스플래시 아이콘
├── font/hahmlet.ttf                 # [NEW] Hahmlet 가변 폰트
├── mipmap-{mdpi~xxxhdpi}/
│   └── ic_launcher_foreground.png   # [NEW] Safe zone 적용 로고
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml              # [MODIFIED] mipmap foreground 참조
│   └── ic_launcher_round.xml        # [MODIFIED] mipmap foreground 참조
└── values-v31/themes.xml            # [NEW] Android 12+ 스플래시 설정
```

---

### 해결된 이슈

| 이슈 | 원인 | 해결 |
|------|------|------|
| 온보딩/권한 버튼 텍스트 안 보임 | Typography `labelLarge`에 `color = TextPrimary` 명시 → Button contentColor 무시 | `RNoteButton` 컴포넌트에서 `color = CloudDancer` 명시 |
| 앱 아이콘 safe zone 미적용 | foreground PNG가 108dp 캔버스 전체 차지 | PIL로 61% safe zone 비율 재생성 |
| 시스템 스플래시 로고 중복 | Android 12+ 자동 스플래시 + Compose 스플래시 이중 표시 | values-v31 테마에서 시스템 스플래시 아이콘 투명 처리 |

---
---

## ver.0.3.0 — 다국어 지원 (i18n): 영어 기본 + 한국어 지원

**작업일**: 2026.02.12
**Git Tag**: `v0.3.0`
**Branch**: `feature/i18n-support`

---

### 구현 완료 항목

#### 1. String Resource 분리 (`res/values/`, `res/values-ko/`)
- 영어(default) `strings.xml` — ~70개 문자열
- 한국어 `values-ko/strings.xml` — ~70개 번역
- 카테고리별 체계 구성: Onboarding, Permission, Note, NoteList, Prompt, Export, Emotion
- Format string 파라미터 정합성 유지 (`%d`, `%1$s ~ %2$s`, `%d%%`)
- 기기 언어 설정에 따라 자동 전환

#### 2. PromptType enum 리팩토링 (`export/ExportModels.kt`)
- `PromptType(label: String, prompt: String)` → `PromptType(@StringRes labelRes, @StringRes promptRes, @StringRes descRes, emoji: String)`
- emoji/description을 enum 필드로 통합 (기존 NoteListScreen의 PromptOption when 블록 제거)
- ChatGPT 프롬프트 영문 default → 글로벌 AI 응답 품질 최적화
- Export version: "0.2.0" → "0.3.0"

#### 3. Export 레이어 Context 전달 (`export/`)
- `ExportMapper.toShareText()` — Context 파라미터 추가
- `ExportHelper.createChatGptShareIntent()` — Context 첫 번째 파라미터 추가
- 내보내기 텍스트 내 데이터 레이블 전체 리소스화 (기간, 총 기록, 평균 점수, 감정 분포 등)

#### 4. EmotionLevel 이중 레이블 구조 (`ui/note/NoteViewModel.kt`)
- `EmotionLevel.label` → `labelKey` (영문, DB 저장용) + `labelRes` (@StringRes, UI 표시용)
- DB에는 영어 레이블 유지 → ChatGPT 내보내기 호환성 보장
- UI에서는 `stringResource(level.labelRes)` → 기기 locale에 따라 번역 표시

#### 5. 전 화면 stringResource() 교체

| 화면 | 파일 | 교체 문자열 수 |
|------|------|---------------|
| NoteListScreen | `NoteListScreen.kt` | 20개 (Toast, 버튼, content description, 빈 상태, 프롬프트 셀렉터) |
| NoteScreen | `NoteScreen.kt` | 8개 (저장 버튼, placeholder, 종료 확인 다이얼로그) |
| OnboardingScreen | `OnboardingScreen.kt` | 12개 (4페이지 title/desc, 건너뛰기/다음/시작하기) |
| PermissionBottomSheet | `PermissionBottomSheet.kt` | 4개 (제목, 설명, 허용, 나중에) |

#### 6. 날짜 포맷 locale 자동 대응 (`ui/notelist/NoteListViewModel.kt`)
- `SimpleDateFormat("yyyy.MM.dd")` → `DateFormat.getDateInstance(DateFormat.MEDIUM)`
- EN: "Feb 12, 2026" / KO: "2026. 2. 12."
- 시스템 locale에 따라 자동 포맷 전환

#### 7. 폰트 구조 확장 준비 (`ui/theme/Type.kt`)
- `AppFont` 변수 분리 (현재 HahmletFont 할당)
- 향후 일본어/스페인어 등 locale별 폰트 교체 진입점 마련

---

### 수정 파일 목록 (11개)

```
res/values/strings.xml              # [EXPANDED] English default ~70개
res/values-ko/strings.xml           # [NEW] Korean translations ~70개
export/ExportModels.kt              # [MODIFIED] PromptType @StringRes, toShareText Context
export/ExportHelper.kt              # [MODIFIED] createChatGptShareIntent Context
ui/note/NoteViewModel.kt            # [MODIFIED] EmotionLevel labelKey + labelRes
ui/note/NoteScreen.kt               # [MODIFIED] stringResource() 교체
ui/notelist/NoteListScreen.kt       # [MODIFIED] stringResource() 교체
ui/notelist/NoteListViewModel.kt    # [MODIFIED] DateFormat.MEDIUM locale 대응
ui/onboarding/OnboardingScreen.kt   # [MODIFIED] OnboardingPage @StringRes
ui/components/PermissionBottomSheet.kt # [MODIFIED] stringResource() 교체
ui/theme/Type.kt                    # [MODIFIED] AppFont 변수 분리
```

---

### 설계 결정 사항

| 항목 | 결정 | 근거 |
|------|------|------|
| DB 감정 레이블 | 영문 유지 (labelKey) | ChatGPT 내보내기 호환성, 언어 독립 데이터 |
| ChatGPT 프롬프트 | 영문 default | AI 응답 품질 최적화, GPT Store 글로벌 타겟 |
| 폰트 | Hahmlet 유지 (Latin+한글 지원) | 추가 폰트 없이 영어/한국어 모두 대응 |
| 날짜 포맷 | `DateFormat.MEDIUM` | Context 의존 없이 ViewModel에서 locale 대응 |
| 향후 언어 확장 | `values-{locale}/strings.xml` 추가만으로 가능 | Android 네이티브 i18n 구조 활용 |

---

### 다음 버전 후보 작업

- [ ] 주간/월간 감정 통계 화면
- [ ] 노트 검색 기능
- [ ] 다크 모드 지원
- [ ] ChatGPT API 직접 연동 (앱 내 분석 결과 표시)
- [ ] 일본어/스페인어 추가 지원

---
---

## ver.1.0.0 — Google AdMob 하단 배너 광고

**작업일**: 2026.02.17
**Git Tag**: `v1.0.0`
**Branch**: `feature/admob-banner`

---

### 구현 완료 항목

#### 1. 의존성 추가 (`gradle/libs.versions.toml`, `app/build.gradle.kts`)
- `play-services-ads:24.3.0` 버전 카탈로그 등록 및 app 모듈 implementation 추가

#### 2. AndroidManifest 설정 (`AndroidManifest.xml`)
- `com.google.android.gms.ads.APPLICATION_ID` meta-data 추가
- AdMob App ID: `ca-app-pub-6816394622305612~5023364294`
- INTERNET 권한은 SDK 자동 병합으로 별도 추가 불필요

#### 3. MobileAds SDK 초기화 (`RNoteApplication.kt`)
- `onCreate()`에서 `MobileAds.initialize(this)` 비동기 호출
- 메인 스레드 블로킹 없음

#### 4. 광고 ID 리소스 관리 (`res/values/strings.xml`)
- `admob_app_id`, `admob_banner_unit_id` — `translatable="false"`로 다국어 제외
- 프로덕션 배너 단위 ID: `ca-app-pub-6816394622305612/7295573330`

#### 5. BannerAd 컴포저블 (`ui/components/BannerAd.kt`) — [NEW]
- `AndroidView`로 `AdView` 래핑 (Compose에서 AdMob 사용 표준 방식)
- `AdSize.BANNER` (320×50) — 가장 작은 비침해적 포맷
- `fillMaxWidth()` + string resource에서 광고 단위 ID 로드
- `navigationBarsPadding()` 적용 — edge-to-edge 환경 대응

#### 6. NoteListScreen 배너 삽입 (`ui/notelist/NoteListScreen.kt`)
- Scaffold `bottomBar = { BannerAd() }` 추가
- 노트 리스트(메인 화면)에만 광고 노출, 노트 작성/편집 화면은 광고 없음
- Scaffold가 자동으로 paddingValues에 배너 높이 포함 → 콘텐츠 겹침 없음

---

### 수정 파일 목록 (7개)

```
gradle/libs.versions.toml              # [MODIFIED] play-services-ads 추가
app/build.gradle.kts                   # [MODIFIED] implementation 추가
app/src/main/AndroidManifest.xml       # [MODIFIED] AdMob App ID meta-data
app/src/main/res/values/strings.xml    # [MODIFIED] 광고 ID 문자열 추가
RNoteApplication.kt                    # [MODIFIED] MobileAds.initialize()
ui/components/BannerAd.kt             # [NEW] 재사용 가능 배너 컴포저블
ui/notelist/NoteListScreen.kt         # [MODIFIED] Scaffold bottomBar 추가
```

---

### 해결된 이슈

| 이슈 | 원인 | 해결 |
|------|------|------|
| FAB이 네비게이션 바 뒤로 밀림 | `enableEdgeToEdge()` + Scaffold `bottomBar` → 네비게이션 바 인셋 처리가 bottomBar에 위임됨 | BannerAd에 `navigationBarsPadding()` 적용 |
| 광고 영역 미표시 | 신규 AdMob 계정은 광고 인벤토리 준비까지 시간 필요 (최대 24시간) | Google 테스트 배너 ID로 통합 검증 완료 후 프로덕션 ID 복원 |

---

### 설계 결정 사항

| 항목 | 결정 | 근거 |
|------|------|------|
| 광고 위치 | 노트 리스트만 | 노트 작성 시 집중 방해 방지 |
| 광고 포맷 | BANNER (320×50) | 가장 작은 비침해적 포맷, UX 영향 최소화 |
| 유료 플랜 | 없음 | 무료 앱 단일 모델 |
| 광고 ID 관리 | strings.xml (translatable=false) | 빌드 variant별 관리 용이, 다국어 제외 |

---

### 다음 버전 후보 작업

- [ ] 주간/월간 감정 통계 화면
- [ ] 노트 검색 기능
- [ ] 다크 모드 지원
- [ ] ChatGPT API 직접 연동 (앱 내 분석 결과 표시)
- [ ] 일본어/스페인어 추가 지원
