# Dopamine Box - Native Android

Native Android app built with Kotlin + Jetpack Compose. **3-5x faster** and **80% smaller** than web wrapper.

## Build Locally

```bash
cd android-native
./gradlew assembleDebug  # Linux/Mac
gradlew.bat assembleDebug  # Windows
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Build with GitHub Actions

Push to GitHub and the workflow automatically builds the APK. Download from Actions tab.

## Features

- 🪙 Coin Flip (2× multiplier)
- 🃏 Higher/Lower (up to 64×)
- 🎯 Plinko (0.2×-2.0×)
- 📊 Stats tracking
- 🔥 Daily streaks
- 📱 Native haptics

## Tech Stack

Kotlin • Jetpack Compose • Material 3 • DataStore • MVVM
