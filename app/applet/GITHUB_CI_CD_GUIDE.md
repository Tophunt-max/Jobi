# 🚀 GitHub CI/CD & APK Download Guide

This project includes a complete **GitHub Actions CI/CD pipeline** located at `.github/workflows/android-build-apk.yml`. Every time you push code to GitHub or create a release, GitHub automatically compiles and packages the Android APK, making it directly downloadable.

---

## 📥 How to Download the APK on GitHub

### Method 1: Download from GitHub Actions (On Every Push / Commit)
1. Go to your repository on **GitHub**.
2. Click on the **Actions** tab at the top.
3. Select the latest workflow run: **Build & Export Android APK**.
4. Scroll down to the **Artifacts** section at the bottom of the summary page.
5. Click **`Sarkari-Job-Alert-Debug-APK`** to download the ready-to-install `.apk` zip file!

### Method 2: Download from GitHub Releases (Permanent Download Link)
You can create a permanent release download in two ways:

#### Option A: Manual Trigger via GitHub UI
1. Go to your repository's **Actions** tab.
2. Under "Workflows" on the left, click **Build & Export Android APK**.
3. Click the **Run workflow** button on the right.
4. Check **"Publish as a GitHub Release"** (set to true).
5. Click **Run workflow**.
6. When complete, go to the **Releases** tab on your GitHub repository to download `SarkariJobAlert-v1.0-debug.apk`.

#### Option B: Push a Git Version Tag
Pushing any tag starting with `v` (e.g. `v1.0.0`) automatically publishes a GitHub Release with the APK attached:
```bash
git tag v1.0.0
git push origin v1.0.0
```

---

## ⚙️ What the Workflow Does Automatically
1. Sets up **OpenJDK 21 (Temurin)** & **Gradle 9.3.1**.
2. Restores configuration environment files (`.env` and `debug.keystore`).
3. Runs unit & Robolectric tests (`testDebugUnitTest`).
4. Executes `./gradlew assembleDebug` to build the signed, installable APK.
5. Saves the APK as a GitHub Action artifact named `Sarkari-Job-Alert-Debug-APK` (kept for 30 days).
6. Publishes to GitHub Releases if a release tag or manual release trigger is requested.
