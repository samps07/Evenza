# Automated GitHub APK Publishing & Direct Phone Installation Guide

This repository is configured with an automated **GitHub Actions CI/CD pipeline** (`.github/workflows/build-and-release-apk.yml`) that compiles the app and publishes an installable APK every time changes are pushed to GitHub.

---

## 🚀 How It Works

1. Whenever code is pushed to your GitHub repository (via **Push to GitHub** in Google AI Studio or regular `git push`), GitHub automatically triggers the workflow.
2. The workflow:
   - Sets up **JDK 21** and **Gradle 9.3.1**.
   - Automatically extracts your app version from `app/build.gradle.kts` (e.g. `1.0`).
   - Compiles the universal debug Android APK (`gradle assembleDebug`).
   - Generates the tag **`Evenza-v<version>`** (e.g. `Evenza-v1.0`) and title **`Evenza v<version>`**.
   - Automatically publishes the compiled **`Evenza-v<version>-debug.apk`** directly to **GitHub Releases**.
   - Also archives the APK under the **Actions** tab artifacts.

---

## 📥 How to Download & Install the APK on Your Phone

### Option A: From GitHub Releases (Easiest)
1. On your phone or computer, open your GitHub repository page.
2. Navigate to **Releases** on the right sidebar (or go to `https://github.com/<your-username>/<your-repo>/releases`).
3. Under the release (e.g. **`Evenza v1.0`** with tag **`Evenza-v1.0`**), tap or click **`Evenza-v<version>-debug.apk`** under the **Assets** section.
4. Open the downloaded file on your phone and tap **Install**.
   *(If prompted, toggle on "Allow from this source" in Android settings).*

---

### Option B: From GitHub Actions Runs
1. Navigate to the **Actions** tab in your GitHub repository.
2. Tap the latest workflow run (named **Build & Release Android APK**).
3. Scroll down to the **Artifacts** section at the bottom.
4. Click **`Evenza-Counter-debug-apk`** to download the zipped APK.

---

### Option C: Manual Trigger (Run Without Committing)
You can build a fresh APK anytime directly from GitHub:
1. Go to the **Actions** tab in your GitHub repository.
2. In the left sidebar, click **Build & Release Android APK**.
3. Click the **Run workflow** dropdown button on the right, select branch `main`, and click **Run workflow**.

---

## 🔒 Notes on GitHub Permissions
Ensure GitHub Actions has permission to write releases:
1. In your GitHub repository, go to **Settings** > **Actions** > **General**.
2. Scroll down to **Workflow permissions**.
3. Choose **Read and write permissions** and click **Save**.
