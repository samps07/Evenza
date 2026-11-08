# Implementation Plan: Automated GitHub Actions APK Publishing

Set up an automated Continuous Integration & Continuous Delivery (CI/CD) workflow using GitHub Actions that automatically builds the Android application and publishes an installable APK to GitHub Releases on every code push.

## User Review Required

> [!IMPORTANT]
> The automated pipeline will be configured to build a universal **Debug APK** (`app-debug.apk`) on every push to the `main` branch. Debug APKs can be directly installed on any Android device without configuring keystore secrets in GitHub. Whenever you push code to GitHub (via AI Studio's "Push to GitHub" feature or git), the workflow triggers automatically, compiles the APK, and publishes it under GitHub Releases.

## Proposed Changes

### GitHub Actions CI/CD Pipeline

#### [NEW] `.github/workflows/build-and-release-apk.yml`
- Trigger on `push` to `main` (and optionally on manual dispatch `workflow_dispatch` so you can trigger builds with a single click in GitHub UI).
- Set up **Ubuntu runner** with **JDK 17/21** via `actions/setup-java@v4`.
- Set up **Gradle 9.3.1** via `gradle/actions/setup-gradle@v4`.
- Provide fallback creation of `.env` from `.env.example` so the Secrets Gradle Plugin builds cleanly in CI.
- Execute build step: `gradle assembleDebug --no-daemon`.
- Rename output to a user-friendly format: `Evenza-Counter-debug.apk`.
- Publish the APK using `softprops/action-gh-release@v2`:
  - Automatically updates/creates a rolling **Latest Release** (tag `v-latest` or dated tag).
  - Attaches `Evenza-Counter-debug.apk` directly to the release page as a clickable download.
- Also upload the APK as an **Actions Artifact** via `actions/upload-artifact@v4` for 90-day retention and build log tracking.

---

#### [NEW] `gradlew` & `gradlew.bat`
- Generate standard portable Gradle wrapper scripts (`gradlew` and `gradlew.bat`) matching `gradle/wrapper/gradle-wrapper.properties` so the repository can be built seamlessly on any machine or CI system.

---

### Documentation & Instructions

#### [NEW] `GITHUB_APK_GUIDE.md`
- Concise instructions explaining:
  1. How to push the codebase from AI Studio to GitHub using the AI Studio export/git integration.
  2. How GitHub Actions automatically triggers on push.
  3. Where to find and download the `.apk` on GitHub (`Releases` section or `Actions` tab).
  4. How to install the downloaded APK on your Android device.

## Verification Plan

### Automated / Local Tests
- Verify Gradle build command locally: `gradle assembleDebug` to confirm clean APK generation.
- Verify GitHub Actions workflow syntax and YAML structure against GitHub Actions schema.
- Run `compile_applet` to confirm overall project integrity remains intact.

### Manual Verification
- Once pushed to GitHub:
  - Check the **Actions** tab on GitHub to see the automated build in progress.
  - Check the **Releases** section on the GitHub repository to find the download link for `Evenza-Counter-debug.apk`.
