# Implementation Plan: Comprehensive Evenza README.md

Create a rich, professional, and visually engaging `README.md` for Evenza that documents all features, capabilities, architecture, and CI/CD integration for the GitHub repository.

## User Review Required

> [!NOTE]
> The README will detail all user-facing features (AI OCR expiry scanning, interactive home-screen widgets, custom category notification timelines, automatic color-coding, persistent status bar alerts) and technical specifications (Modern Jetpack Compose, Room DB, Material 3 Bento UI, GitHub Actions release automation).

## Proposed README Structure

1. **Header & Badges**: App title, tagline, platform badges (Android 14+, Jetpack Compose, Material 3, Room, GitHub Actions).
2. **Overview**: Introduction to Evenza as an intelligent countdown & product expiry tracking companion.
3. **Key Features & Capabilities**:
   - 🤖 **AI-Powered Label Scanner**: On-device Google ML Kit OCR + OpenRouter/Gemini LLM integration with raw text capture and auto-description sync.
   - 📱 **Interactive Home-Screen Widgets**: Multi-category filtered widgets, auto-refresh, thumbnail previews, and instant tap-to-expand.
   - 🔔 **Multi-Tier Expiry Notifications**: Multiple custom reminder periods per category (e.g. 1d, 3d, 7d before expiry) and persistent notification drawer pinning.
   - 🎨 **Automated Category Theme Colors**: Category-level color-coding that automatically colors events and visual badges.
   - 🚀 **Sharing & Quick Capture**: System Share intent handling (`ShareDialogActivity`) allowing direct scanning from any app or gallery.
   - ⚙️ **Minimalist Settings & Customization**: UI scaling, 12h/24h time formats, sorting modes (Urgency vs Custom order), and OCR preprocessing options.
4. **CI/CD & Automated Releases**:
   - Explanation of the `.github/workflows/build-and-release-apk.yml` pipeline.
   - Automatic generation of `Evenza-v<version>` release tags and downloadable APKs on push.
5. **Tech Stack & Architecture**:
   - Kotlin, Jetpack Compose, Coroutines/Flow, Room Database, Material 3.
6. **Installation & Getting Started**:
   - How to download the APK from GitHub Releases and install on Android.

## Verification Plan

- Verify formatting, links, and markdown syntax.
- Confirm `compile_applet` succeeds without regression.
