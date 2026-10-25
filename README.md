# Nova Pool 🎱✨

[![Version](https://img.shields.io/badge/Version-0.1%20Beta-blue.svg)](https://github.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-green.svg)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)

> **Next-generation 8-Ball & 9-Ball Pool gaming experience built with native Jetpack Compose and custom 3D projection physics on Android.**

---

## 📖 Overview

**Nova Pool** brings the classic billiards arcade experience to Android with modern UI design principles and custom real-time 3D rendering. Designed from the ground up using **Jetpack Compose** and **Material Design 3**, Nova Pool combines realistic rigid-body sphere collision physics with dynamic camera views, intuitive touch controls, offline progression, AI opponents, cue customization, and local tournament modes.

---

## ⭐ Feature Highlights

* 🎱 **3D Pool & Real-time Physics Engine**
  * Custom 3D perspective projection engine rendering smooth ball rolling, spin vectors, cushion reflections, and multi-ball collisions.
  * Interactive aim assist guidelines, cue angle elevation, power slider controls, and cue ball english/spin selection.

* 🤖 **Intelligent AI Opponents**
  * Multiple AI difficulty tiers (Beginner, Intermediate, Master) with dynamic shot planning, collision avoidance, and tactical cue ball placement.

* 🏪 **Cue Shop & Customization**
  * Unlockable cues with varying stats (Power, Aim Assist Line, Spin Control, Time Bonus).
  * Custom cloth themes, table felts, and visual effects.

* 🏆 **8-Player Local Tournament**
  * Bracket-style local multiplayer tournament mode supporting custom player names, seeding, and knockout rounds.

* 🎨 **Material 3 UI Overlays**
  * Full edge-to-edge support with dynamic theme adaptation, expressive M3 components, fluid animations, and immersive sound effects.

* 💾 **Offline Progression & Persistence**
  * Local coin economy, statistics tracking (Win/Loss ratios, high scores, shot accuracy), and persistent player inventories using DataStore/SharedPreferences.

---

## 🛠️ Tech Stack & Architecture

| Component | Technology / Library |
| :--- | :--- |
| **Language** | Kotlin 100% |
| **UI Framework** | Jetpack Compose (Material 3 Expressive UI) |
| **Graphics & Rendering** | Custom 3D Projection Canvas & Trigonometric Rendering Pipeline |
| **Asynchronous & State** | Kotlin Coroutines, StateFlow, ViewModel |
| **Storage & Persistence** | Jetpack DataStore / SharedPreferences |
| **Build System** | Gradle (Kotlin DSL, Version Catalogs) |

---

## 📱 Screenshots & UI Overview

| Main Menu | 3D Pool Table | Cue Shop | Tournament |
| :---: | :---: | :---: | :---: |
| *(Screenshot Placeholder)* | *(Screenshot Placeholder)* | *(Screenshot Placeholder)* | *(Screenshot Placeholder)* |

---

## 🚀 Building & Running Instructions

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1) or newer recommended.
* **JDK**: OpenJDK 17 or higher.
* **Android SDK**: API Level 34 (Android 14) or higher (Minimum SDK 24 / Android 7.0).

### Gradle Build Commands

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/novapool.git
   cd novapool
   ```

2. **Build Debug APK**
   ```bash
   ./gradlew assembleDebug
   ```
   *Output APK path:* `app/build/outputs/apk/debug/app-debug.apk`

3. **Build Release APK (0.1 Beta)**
   ```bash
   ./gradlew assembleRelease
   ```
   *Output APK path:* `app/build/outputs/apk/release/app-release.apk`

4. **Run Unit & UI Tests**
   ```bash
   ./gradlew test
   ```

---

## 📜 License & Credits

Nova Pool is an experimental side project developed with **Android Studio** and **Gemini**.

Released under the [MIT License](LICENSE). Feel free to explore, fork, and contribute!
