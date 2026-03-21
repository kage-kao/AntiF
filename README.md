# AntiF Browser - Anti-Detect Browser for Android

Multi-profile anti-detect browser with fingerprint spoofing, proxy support, and built-in JavaScript console.

## Features

- **Multi-Profile Management** — Create unlimited browser profiles with isolated sessions
- **Multi-Tab Browser** — Open multiple tabs per profile, switch between them, close individually
- **Bookmark Manager** — Save and manage bookmarks (global across all profiles)
- **Multi-Threaded Download Manager** — Fast file downloads with configurable thread count (1-12 threads)
- **Download Management** — Pause/resume downloads, view progress, notifications
- **Fingerprint Spoofing** — UserAgent, Canvas, WebGL, AudioContext, Screen, Timezone, etc.
- **AdBlock Engine** — Network-level ad blocking with 250+ domain rules and URL pattern matching
- **Cosmetic Filtering** — CSS injection to hide ad elements (Google Ads, Taboola, Outbrain, etc.)
- **Anti-AdBlock Bypass** — Bypass adblock detection walls on websites
- **Cookie/Consent Banner Blocking** — Auto-hide GDPR/cookie popups
- **Proxy Support** — HTTP and SOCKS5 per-profile proxy configuration
- **JS Console** — Execute JavaScript commands in the browser context
- **Cookie Manager** — View, add, delete cookies per site
- **WebRTC Leak Protection** — Block WebRTC IP leaks
- **FingerprintJS Blocking** — Blocks known fingerprinting services (fpjs.io, fpcdn.io)
- **Quick Actions** — Pre-built commands: `window.resetAllModals()`, `window.resetFreeStandardPlanModal()`
- **Storage Cleanup** — Clear localStorage, sessionStorage, IndexedDB per profile
- **Dark Theme** — Minimal black UI for comfortable usage

## What Gets Spoofed

| Feature | Spoofed |
|---------|---------|
| navigator.userAgent | Yes |
| navigator.platform | Yes |
| navigator.language/languages | Yes |
| screen.width/height | Yes |
| navigator.hardwareConcurrency | Yes |
| navigator.deviceMemory | Yes |
| Canvas fingerprint | Yes (noise injection) |
| WebGL vendor/renderer | Yes |
| AudioContext | Yes (noise injection) |
| Timezone | Yes |
| WebRTC | Blocked |
| Battery API | Spoofed |
| Connection API | Spoofed |
| Plugins/MimeTypes | Spoofed |
| FingerprintJS requests | Blocked |
| Ads (network level)    | Blocked (250+ domains, URL patterns) |
| Ad elements (CSS)      | Hidden (Google Ads, Taboola, Outbrain, etc.) |
| Anti-adblock walls     | Bypassed |
| Cookie/GDPR banners    | Hidden |
| Multi-tab browsing     | Yes |

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Kotlin 1.9.22

## Build Instructions

1. Open the project folder in Android Studio
2. Wait for Gradle sync to complete (may take a few minutes first time)
3. Connect your Android device (USB debugging enabled) or start an emulator
4. Click **Run** button or execute from terminal:

```bash
./gradlew assembleDebug
```

5. APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

For release build:
```bash
./gradlew assembleRelease
```

## Project Structure

```
antif-android/
├── app/
│   ├── build.gradle.kts          # App dependencies
│   ├── proguard-rules.pro        # ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml   # App manifest
│       ├── java/com/antif/browser/
│       │   ├── AntiFApplication.kt    # Application class
│       │   ├── ui/
│       │   │   ├── MainActivity.kt        # Profile list
│       │   │   ├── BrowserActivity.kt     # WebView browser
│       │   │   ├── ConsoleActivity.kt     # JS Console
│       │   │   ├── ProfileEditActivity.kt # Profile editor
│       │   │   └── CookieManagerActivity.kt # Cookie management
│       │   ├── core/
│       │   │   ├── FingerprintSpoofer.kt  # JS injection for spoofing
│       │   │   ├── AdBlockEngine.kt       # Network-level ad blocking
│       │   │   ├── CosmeticFilter.kt      # CSS ad element hiding
│       │   │   ├── ProxyManager.kt        # Proxy configuration
│       │   │   └── WebViewConfigurator.kt # WebView setup
│       │   ├── data/
│       │   │   ├── BrowserProfile.kt      # Room entity
│       │   │   ├── ProfileDao.kt          # Database operations
│       │   │   └── AppDatabase.kt         # Room database
│       │   └── utils/
│       │       ├── FingerprintGenerator.kt # Random profile gen
│       │       └── UserAgentList.kt       # User agent strings
│       └── res/
│           ├── layout/           # UI layouts
│           ├── values/           # Colors, strings, themes
│           ├── drawable/         # Backgrounds, shapes
│           └── xml/              # Network security config
├── build.gradle.kts              # Root build file
├── settings.gradle.kts           # Project settings
├── gradle.properties             # Gradle config
└── README.md                     # This file
```

## Tech Stack

- **Language**: Kotlin 1.9.22
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Database**: Room (SQLite)
- **WebView**: Android WebView with WebKit extensions
- **Proxy**: ProxyController (AndroidX WebKit) + System Properties
- **UI**: Material Design 3, dark theme
- **Architecture**: Single-activity per feature, coroutines for async

## License

For educational and personal use only.
