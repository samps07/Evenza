# Evenza ⏳

> **Smart Event Countdown & Expiry Tracking for Android**  
> *Track deadlines, perishables, warranties, and milestones with on-device OCR, AI label analysis, interactive home-screen widgets, and intelligent multi-tier alerts.*

[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin_2.2-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Persistence-Room_Database-4285F4.svg?style=flat)](https://developer.android.com/training/data-storage/room)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions_Automated_Releases-2088FF.svg?style=flat&logo=githubactions)](https://github.com/features/actions)

---

## 🌟 Overview
<img width="956" height="1280" alt="6310009022228142123" src="https://github.com/user-attachments/assets/ec5aa763-4eda-48df-83ce-824bf724b41e" />


**Evenza** is a modern, high-precision event countdown and expiry management application designed for Android. Combining **Material 3 Bento-style interfaces**, **on-device Google ML Kit OCR**, and **LLM label intelligence**, Evenza eliminates guesswork around perishable goods, medication schedules, warranty expirations, and life milestones.

Whether you're tracking perishable groceries in your pantry, managing medicines with strict expiration timelines, or keeping an eye on critical bills and projects, Evenza keeps you ahead of schedule with custom multi-tier notifications and interactive home-screen widgets.

---

## ✨ Key Features & Utilities

### 🤖 1. AI-Powered Expiry & Product Scanner
- **Two-Stage Analysis Architecture**:
  1. **On-Device Optical Character Recognition (OCR)**: Rapidly extracts text from label photos locally using Google ML Kit. Zero cloud latency and complete image privacy.
  2. **Intelligent LLM Parsing**: Uses OpenRouter / OpenAI / Gemini models to intelligently identify product names, expiration dates, manufacturing dates, net quantity, and categories.
- **Dedicated Extracted Text Field**:
  - Displays the raw OCR text alongside parsed data.
  - Automatically preserves and saves the raw text directly inside the event's **Description** field so no critical batch numbers or packaging details are lost if the AI encounters an unusual format.
  - Real-time **"Sync to Description"** allows one-tap synchronization between extracted text and event notes.
- **OCR Enhancements**: Built-in image pre-processing options including contrast boosting, sharpening, and multi-angle text orientation correction.

### 📱 2. Interactive Home-Screen Widgets
- **Dynamic Scrollable Widget**: Pin your active deadlines directly to your Android home screen with real-time countdown progress.
- **Multi-Category Customization**: Configure multiple widget instances with separate category filters (e.g., a "Food & Pantry" widget on one page and a "Medicine & Health" widget on another).
- **Expandable Previews**: Tap any widget item to expand detailed notes, links, and cached photo thumbnails instantly without leaving your home screen.
- **One-Tap Refresh**: Instant background synchronization with your local database.

### 🔔 3. Multi-Tier Expiry Notifications & Reminders
- **Multiple Custom Reminder Periods**: Configure multiple notification checkpoints per category (e.g. 1 day, 2 days, 3 days, 7 days, or 14 days before expiry).
- **Custom Day Input**: Add custom numeric day alerts tailored to specific perishable life cycles.
- **Persistent Notification Pinning**: Pin any critical countdown directly to your Android notification drawer for glanceable, persistent countdown monitoring.
- **Exact Alarms & Doze Mode Compliance**: Built with Android's `AlarmManager` and `WorkManager` for guaranteed on-time delivery across battery optimizations.

### 🎨 4. Automated Category Default Theme Colors
- **Zero-Friction Color-Coding**: Set default accent colors for categories (Food, Medicine, Dairy, Beverages, Pantry, Cosmetics, General, or custom categories).
- **Automated Event Theming**: Adding an event under any category automatically applies that category's theme color, visually tinting cards and badges across the app.
- **Custom Emoji Icons**: Assign custom emojis from your device keyboard or pick from built-in preset icon badges.

### ⚡ 5. Android System Share Integration
- **Quick-Scan From Any App**: Share photos directly from your Camera, Gallery, Browser, or messaging apps to Evenza via the Android Share sheet.
- **Lightweight Floating Dialog**: `ShareDialogActivity` processes the image, runs OCR/AI analysis, and allows saving to your counters without opening the full application.

### 🎛️ 6. Minimalist Settings & Personalization
- **Streamlined Accordions**: Clean, clutter-free settings organization covering **AI Settings**, **OCR**, **Categories**, **UI**, **Theme**, **Sorting**, and **Widgets**.
- **Display & UI Scaling**: Adjust the app's global UI scale dynamically from 80% to 125% for high-density screens or enhanced readability.
- **Flexible Sorting**: Switch between **Urgency Mode** (most critical deadlines first) and **Custom Drag/Reorder Mode**.
- **Time Formats**: Choose between 12-hour (AM/PM) and 24-hour clocks.

---

## 🏗️ Architecture & Technology Stack

| Layer | Technologies |
|---|---|
| **Architecture** | Clean Architecture / MVVM (Model-View-ViewModel) |
| **Language** | Kotlin 2.2 |
| **UI Framework** | Jetpack Compose, Material 3, Dynamic Theming |
| **Local Database** | Room Database (SQLite with Flow-backed reactive queries) |
| **ML & AI** | Google ML Kit (Vision Text Recognition) + OpenRouter REST Client |
| **Image Loading** | Coil Compose with internal persistent file storage |
| **Widgets** | Android AppWidget framework with `RemoteViewsService` and `LruCache` |
| **CI / CD** | GitHub Actions (`actions/setup-java`, `gradle/actions/setup-gradle`, `action-gh-release`) |

---

## 🚀 Automated CI/CD & GitHub Releases

Evenza includes an automated GitHub Actions pipeline (`.github/workflows/build-and-release-apk.yml`) that builds and publishes installable APKs on every commit:

1. **Continuous Integration**: Whenever code is pushed to `main`, GitHub Actions spins up an Ubuntu runner with JDK 21 and Gradle 9.3.1.
2. **Automated Keystore Creation**: Automatically provisions `debug.keystore` in headless environments to guarantee successful builds.
3. **Automated GitHub Release**:
   - Reads `versionName` dynamically from `app/build.gradle.kts`.
   - Creates a release tag formatted as **`Evenza-v<version>`** (e.g. `Evenza-v1.0`).
   - Attaches **`Evenza-v<version>-debug.apk`** directly to the release page.

---

## 📥 Installation

### Download Installable APK
1. Go to the [Releases](https://github.com/samps07/Evenza/releases) section of this repository.
2. Under the latest release (e.g., `Evenza v1.0`), download **`Evenza-v1.0-debug.apk`**.
3. Open the file on your Android device and tap **Install**.
   *(If prompted, toggle on "Allow installation from this source" in your device settings).*

### Building from Source
```bash
# Clone the repository
git clone https://github.com/samps07/Evenza.git
cd Evenza

# Build debug APK locally
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License & Credits

Built with ❤️ by **Sam**.  
Designed for speed, privacy, and seamless deadline tracking.
