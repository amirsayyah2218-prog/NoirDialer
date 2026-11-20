# Noir Dialer (Android / Kotlin)

Starter Android Studio project for the Noir Dialer interface.

## Build APK with GitHub Actions (no computer required)

1. Download this ZIP and extract it on your Android phone using Files / ZArchiver.
2. In GitHub, create a **new public or private repository** named `NoirDialer`.
3. In the repository page choose **Add file → Upload files**.
4. Upload the *contents* of the extracted `NoirDialer` folder (including the hidden `.github` folder). If your file manager hides hidden folders, enable “show hidden files” and make sure `.github/workflows/android.yml` is uploaded.
5. Commit the files to `main`.
6. Open the repository's **Actions** tab. Select **Build Noir Dialer APK**, then tap **Run workflow** if it has not started automatically.
7. Wait for the green successful run. Open that run, scroll to **Artifacts**, and download `NoirDialer-debug-apk`.
8. Extract the downloaded artifact ZIP and install `app-debug.apk`. Android may ask you to allow installation from that browser/file manager.

The workflow uses GitHub-hosted Linux build machines, Java 17, Android SDK and Gradle 8.9. No Gradle wrapper is required.

## Local build

Open this folder in Android Studio and run `gradle :app:assembleDebug` (with Android SDK installed).

## Important status and limitations

This is an early development starter, **not a finished or verified replacement for the phone app**. The app source and Telecom service are not yet fully tested/production-ready. Do not rely on it for emergency calls. Before using as the default dialer, test on a spare device and confirm calling, incoming calls, lock-screen behavior and permissions.

Current build does not yet implement complete call history, full contact create/edit/delete, SMS integration, voice assistant, reminders, call transcription/recording, multilingual UI, keypad sound selection, wallpaper-based live blur, or floating island. The incoming-call UI and Telecom integration need device testing and additional work. Android and phone manufacturers can restrict full-screen incoming-call UI; users must grant permissions and select the app as default dialer where supported.
