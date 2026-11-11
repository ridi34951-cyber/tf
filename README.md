# SonicShield — Powered by Ridi

Android Studio project and GitHub Actions build workflow.

## IMPORTANT: upload structure

Extract this ZIP. Upload **the contents inside this ZIP directly to the root of your GitHub repository**. Do not upload an extra enclosing folder named `SonicShield`.

The repository root must look like this:

```
.github/workflows/android-build.yml
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/com/ridi/sonicshield/MainActivity.kt
build.gradle.kts
gradle.properties
settings.gradle.kts
README.md
```

The workflow expects `settings.gradle.kts` in the repository root. This fixes the previous error where Gradle was run from a directory that did not contain the project settings file.

## Build using GitHub Actions

1. Extract the ZIP.
2. Upload all its contents directly into the repository root, preserving the `.github` folder.
3. Open the repository's **Actions** tab.
4. Select **SonicShield Android Build** and run **Run workflow**, or push to `main` / `master`.
5. When the run succeeds, open its **Artifacts** section and download `SonicShield-debug-apk`.

## Prototype limitations

This is SonicShield 0.1.0. It includes the ANC / Transparency / Off interface, Powered by Ridi branding, and Bluetooth diagnostics. It does **not** yet send vendor-specific ANC commands to the A9 Pro. The A9 Pro protocol must be identified before those buttons can actually change its hardware mode.
