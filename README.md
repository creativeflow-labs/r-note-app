# R:Note

A minimal, multilingual Android app for recording daily emotions with emoji-based expression and percentage scoring. Built for global users with English as the default language and Korean support. Designed for the ChatGPT App Store (Mind & Meditation category).

## Project Overview

R:Note is an emotion journaling app that removes decision fatigue from the recording process. Users pick an emoji, adjust a mood percentage, and optionally write a short note. All data is stored locally on the device with no account required.

The internal data model includes fields like `sentimentHint`, `emotionScore`, and `wordCount` that are invisible to the user but structured for AI-powered analysis, weekly/monthly reports, and ChatGPT API integration in future versions.

### App Flow

```
Splash → Onboarding (4 pages) → Permission Sheet → Note Editor → Note List
```

## Features

**Emotion Recording** *(v0.2.1)*
- 11-level emotion scale (0–100%) with emoji + localized label
- Dual-button selector: tap emoji or label to expand all 11 options
- Structured sentiment mapping (negative / neutral / positive)
- Title + body note editor

**Note Management**
- Note list grouped by date (locale-aware formatting)
- Edit mode with multi-select deletion and select all
- Floating action button for quick entry
- Tap to edit existing notes
- Double back press to exit app

**ChatGPT Export & Analysis** *(v0.2)*
- Share notes to ChatGPT app with auto-generated analysis prompts
- 3 prompt types: Emotion Pattern Analysis, Weekly/Monthly Report, Counseling
- Export all notes or selected notes only
- Localized prompts (English default optimized for ChatGPT response quality)

**Internationalization (i18n)** *(v0.3.0)*
- English default + Korean support (~70 localized strings)
- Device language auto-detection (no manual toggle needed)
- Locale-aware date formatting (EN: "Feb 12, 2026" / KO: "2026. 2. 12.")
- Emotion labels: English key stored in DB for export compatibility, localized display in UI
- Extensible structure for adding Japanese, Spanish, and more

**Data Safety**
- Auto-save draft on app background/exit
- "Discard changes?" confirmation on back press
- All data stored locally via Room database

**Design** *(v0.2.2)*
- Cloud Dancer (#F0EDE5) background + Charcoal (#2F2F2F) primary
- Hahmlet custom font (Latin + Korean support)
- Custom icons (OpenAI, pen, edit) + app logo
- Android 12+ splash screen handling

**Onboarding & Permissions**
- 4-page horizontal pager onboarding
- Storage permission bottom sheet (skippable)
- Onboarding shown only on first launch

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2026.01.01) |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Navigation Compose |
| Database | Room + KSP |
| Preferences | DataStore |
| Serialization | Gson |
| Build | Gradle 8.13, AGP 9.0, Version Catalog |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

### Project Structure

```
app/src/main/java/com/rnote/app/
├── RNoteApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── NoteEntity.kt          # Room entity (ChatGPT-ready schema)
│   │   ├── NoteDao.kt             # CRUD + draft queries
│   │   └── RNoteDatabase.kt
│   └── repository/
│       └── NoteRepository.kt
├── export/
│   ├── ExportModels.kt            # Export data models + @StringRes prompts
│   └── ExportHelper.kt            # JSON/text export + Share Intent
├── navigation/
│   └── NavGraph.kt
└── ui/
    ├── theme/                      # Color, Type (AppFont), Theme
    ├── splash/SplashScreen.kt
    ├── onboarding/OnboardingScreen.kt
    ├── components/
    │   ├── PermissionBottomSheet.kt
    │   └── RNoteButton.kt         # Shared button component
    ├── note/                       # Editor screen + ViewModel
    └── notelist/                   # List screen + ViewModel

app/src/main/res/
├── values/strings.xml              # English default (~70 strings)
├── values-ko/strings.xml           # Korean translations
├── font/hahmlet.ttf
├── drawable/                       # Logo, icons, splash
└── values-v31/themes.xml           # Android 12+ splash config
```

## Installation

**Requirements**
- Android Studio Ladybug (2024.2) or later
- JDK 17
- Android SDK 35

**Steps**

```bash
git clone https://github.com/creativeflow-labs/r-note-app.git
```

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Run on an emulator or connected device (min API 26)

## Versioning

This project follows [Semantic Versioning](https://semver.org/).

### v0.3.0 — Internationalization (i18n)

> Tag: [`v0.3.0`](https://github.com/creativeflow-labs/r-note-app/releases/tag/v0.3.0) | Date: 2026.02.12

- English default + Korean support (~70 localized strings)
- All UI strings externalized to `stringResource()` / `context.getString()`
- PromptType enum refactored to `@StringRes` resource IDs
- EmotionLevel dual-label: `labelKey` (DB/export) + `labelRes` (UI display)
- Locale-aware date formatting via `DateFormat.MEDIUM`
- AppFont variable for future locale-specific font support

### v0.2.2 — UI Overhaul

> Tag: [`v0.2.2`](https://github.com/creativeflow-labs/r-note-app/releases/tag/v0.2.2) | Date: 2026.02.12

- Charcoal (#2F2F2F) primary color system
- Hahmlet custom font for title and body text
- Custom vector icons (OpenAI, pen, edit)
- App logo with adaptive icon (safe zone applied)
- RNoteButton shared component
- Streamlined export UX (direct OpenAI icon, no dropdown)
- Android 12+ system splash screen handling

### v0.2.1 — UX Improvements

> Tag: [`v0.2.1`](https://github.com/creativeflow-labs/r-note-app/releases/tag/v0.2.1) | Date: 2026.02.12

- 11-level emotion scale (0–100%) replacing 4-emoji system
- Dual-button emotion selector with expandable grid
- Select all / deselect all in edit mode
- Status bar overlap fix (statusBarsPadding)
- Double back press to exit app

### v0.2.0 — ChatGPT Export & Analysis

> Tag: [`v0.2.0`](https://github.com/creativeflow-labs/r-note-app/releases/tag/v0.2.0) | Date: 2026.02.12

- ChatGPT analysis sharing with 3 prompt types (Emotion Analysis, Weekly Report, Counseling)
- JSON file export with emotion_timeline and summary statistics
- Export all notes or selected notes via edit mode
- Prompt selector bottom sheet UI
- FileProvider-based secure file sharing

### v0.1.0 — MVP Initial Release

> Tag: [`v0.1.0`](https://github.com/creativeflow-labs/r-note-app/releases/tag/v0.1.0) | Date: 2026.02.08

First working version with core emotion recording functionality:

- Splash screen with fade-in animation
- 4-page onboarding with permission request
- Emotion input: emoji selection, percentage control, one-line label
- Note editor with title and body
- Note list with date grouping and edit mode (multi-delete)
- Draft auto-save on background/exit
- Room database with ChatGPT-ready data schema
- Cloud Dancer (#F0EDE5) design theme

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

### Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Release-ready code only |
| `develop` | Integration branch for next version |
| `feature/*` | New features |
| `fix/*` | Bug fixes |

## License

This project is licensed under the Apache License 2.0 — see the [LICENSE](LICENSE) file for details.

```
Copyright 2026 CrabAI

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
