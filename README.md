# R:Note

A minimal Android app for recording daily emotions with emoji-based expression and percentage scoring. Designed for effortless journaling with a data structure ready for future ChatGPT integration.

## Project Overview

R:Note is an emotion journaling app that removes decision fatigue from the recording process. Users pick an emoji, adjust a mood percentage, and optionally write a short note. All data is stored locally on the device with no account required.

The internal data model includes fields like `sentimentHint`, `emotionScore`, and `wordCount` that are invisible to the user but structured for AI-powered analysis, weekly/monthly reports, and ChatGPT API integration in future versions.

### App Flow

```
Splash → Onboarding (4 pages) → Permission Sheet → Note Editor → Note List
```

## Features

**Emotion Recording**
- 4 emotion emojis with auto-mapped percentage scores
- +/- 10% manual adjustment
- One-line emotion label (optional free text)
- Title + body note editor

**Note Management**
- Note list grouped by date (descending)
- Edit mode with multi-select deletion
- Floating action button for quick entry
- Tap to edit existing notes

**ChatGPT Export & Analysis** *(v0.2)*
- Share notes to ChatGPT app with auto-generated analysis prompts
- 3 prompt types: Emotion Pattern Analysis, Weekly/Monthly Report, Counseling
- Export all notes or selected notes only
- JSON file export with structured data (emotion_timeline, sentiment distribution)
- FileProvider-based secure file sharing

**Data Safety**
- Auto-save draft on app background/exit
- "Discard changes?" confirmation on back press
- All data stored locally via Room database

**Onboarding & Permissions**
- 4-page horizontal pager onboarding
- Storage permission bottom sheet (skippable)
- Onboarding shown only on first launch

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 1.9.22 |
| UI | Jetpack Compose (BOM 2024.02.00) |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Navigation Compose 2.7.7 |
| Database | Room 2.6.1 + KSP |
| Preferences | DataStore |
| Serialization | Gson |
| Build | Gradle 8.13, AGP 8.13.2, Version Catalog |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

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
│   ├── ExportModels.kt            # Export data models + prompt templates
│   └── ExportHelper.kt            # JSON/text export + Share Intent
├── navigation/
│   └── NavGraph.kt
└── ui/
    ├── theme/                      # Cloud Dancer color system
    ├── splash/SplashScreen.kt
    ├── onboarding/OnboardingScreen.kt
    ├── components/PermissionBottomSheet.kt
    ├── note/                       # Editor screen + ViewModel
    └── notelist/                   # List screen + ViewModel
```

## Installation

**Requirements**
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

**Steps**

```bash
git clone https://github.com/CrabAI/r-note-app.git
```

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Run on an emulator or connected device (min API 26)

## Versioning

This project follows [Semantic Versioning](https://semver.org/).

### v0.2.0 — ChatGPT Export & Analysis

> Tag: [`v0.2.0`](https://github.com/CrabAI/r-note-app/releases/tag/v0.2.0) | Date: 2026.02.12

- ChatGPT analysis sharing with 3 prompt types (Emotion Analysis, Weekly Report, Counseling)
- JSON file export with emotion_timeline and summary statistics
- Export all notes or selected notes via edit mode
- Prompt selector bottom sheet UI
- FileProvider-based secure file sharing

### v0.1.0 — MVP Initial Release

> Tag: [`v0.1.0`](https://github.com/CrabAI/r-note-app/releases/tag/v0.1.0) | Date: 2026.02.08

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
