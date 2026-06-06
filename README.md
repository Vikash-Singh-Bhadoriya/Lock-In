# LockIn - Android App

> *"Discipline > Motivation. Systems > Willpower. Environment design beats self-control."*

<img src="app/src/main/res/drawable/feature_graphic.jpg" alt="LockIn Feature Graphic" width="100%"/>

LockIn is an offline-first, strict productivity Android application designed to help users enter deep focus states. It eliminates distractions, enforces time-boxed commitments, and uses non-shaming environmental interventions to guide users back on track when they deviate.

Designed for high-ambition individuals who suffer from dopamine-driven distractions, LockIn acts as a calm but strict manager, enforcing commitment rather than relying on endless motivation.

## 🧠 Core Philosophy
* **Low-Dopamine UI:** No flashy colors, no infinite scrolling, no gamification, no streak fireworks. Purely typography-centric and functional.
* **Offline-First & Privacy-Centric:** No backend, no analytics SDKs, no ads. All historical session data and patterns are stored locally on the device.
* **Single-Task Enforcement:** One committed action is prioritized over ten planned ones.

## ✨ Key Features

- **Strict Focus Sessions:** Plan single-objective tasks with precise time boxes (e.g., 25 / 45 / 90 minutes).
- **Distraction Interventions:** Utilizes `SYSTEM_ALERT_WINDOW` (Overlay) to instantly interrupt the user if they break focus, requiring a mindful, logged reflection before exiting.
- **Relapse Detection & Logging:** If a session is aborted, the app records the deviation and requires the user to log the exact distraction (Mental Fatigue, External Interruption, etc.).
- **Nightly Efficiency Analytics:** Calculates a daily efficiency score with proportional credit, delivered via a local background notification at 10:00 PM.
- **Bulletproof State Persistence:** Active sessions survive app backgrounding, force stops, and device reboots using local DataStore and Room caching.
- **Device Admin Enforcement (Optional):** Prevents the user from uninstalling the app in a moment of weakness during an active focus block.

## 📱 Major Screens

<p align="center">
  <img src="readme/timeline_screen.png" width="24%" />
  <img src="readme/plan_screen.png" width="24%" />
  <img src="readme/reflection_screen.png" width="24%" />
  <img src="readme/analytics_screen.png" width="24%" />
</p>

## 🏗 Architecture

This app enforces **Clean Architecture** patterns utilizing **MVVM (Model-View-ViewModel)**.

State management is strictly unidirectional. All screen states are represented as immutable data classes, and Composables observe `StateFlow` directly from isolated ViewModels.

| Overview | UI Layer | Data Layer |
| :---: | :---: | :---: |
| <img src="https://developer.android.com/topic/libraries/architecture/images/mad-arch-overview.png" width="250"> | <img src="https://developer.android.com/topic/libraries/architecture/images/mad-arch-overview-ui.png" width="250"> | <img src="https://developer.android.com/topic/libraries/architecture/images/mad-arch-overview-data.png" width="250"> |

### Architectural Rules Enforced:
1. **UI Layer:** 100% Jetpack Compose. Stateless where possible. No business logic inside Composables.
2. **ViewModel Layer:** Holds UI state. No direct persistence calls. One ViewModel per screen.
3. **Data Layer:** Repository pattern abstracting Room (historical data) and DataStore (preferences/active session state).

## 🛠 Tech Stack

- **[Kotlin](https://kotlinlang.org/)** - Primary language.
- **[Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) & [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/)** - For asynchronous task execution and reactive data streaming.
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** - Modern, declarative UI toolkit.
- **[Hilt (Dagger)](https://dagger.dev/hilt/)** - Dependency Injection framework for scalable architecture and easy testing.
- **[Room Database](https://developer.android.com/training/data-storage/room)** - SQLite object mapping library for robust local data storage.
- **[Preferences DataStore](https://developer.android.com/topic/libraries/architecture/datastore)** - Asynchronous, transactional key-value storage for app states and user settings.
- **Android System Components:**
    - `Foreground Services` & `AlarmManager` (Exact Alarms) for precise, Doze-mode-resistant session timing.
    - `BroadcastReceivers` for local daily and nightly notification triggers.
    - `DevicePolicyManager` for strict uninstall prevention.

## 🔐 Permissions Explained

LockIn requires aggressive system permissions to fulfill its promise of breaking dopamine loops:
- **`POST_NOTIFICATIONS`**: To display the non-swipeable, ongoing session timer and nightly analytics.
- **`SYSTEM_ALERT_WINDOW` (Display over other apps)**: The core enforcement mechanic. Used to instantly cover the screen if the user breaks a promise and attempts to use other apps.
- **`USE_EXACT_ALARM` / `SCHEDULE_EXACT_ALARM`**: To guarantee the user is alerted the exact second their focus block ends, bypassing Android Doze mode.
- **`BIND_DEVICE_ADMIN`**: To prevent uninstallation of the app during an active session (Opt-in feature).

## 🚫 Anti-Patterns (Explicitly Avoided)
To maintain the psychological intent of restraint, this project explicitly avoids:
- Social feeds or community features
- Streak obsession or fireworks animations
- AI Chatbots
- Cloud sync (ensuring total privacy)

## 🤝 Contributing & Developer Intent

LockIn is a commercial project developed by **Vikash Singh**, CEO & Founder of LifeUpgradeApps.
The codebase is built for maintainability over speed, architected for clarity, and explicitly resistant to feature creep.

If you find a bug or have a suggestion, please open an issue in the tracker. Code-specific contributions are currently closed to maintain architectural integrity.

## 📄 License & Privacy

- **Privacy Policy:** 100% Offline. Your data never leaves your device.
- **Terms & Conditions:** Use responsibly.

*Proudly architected and engineered in India by Vikash Singh.*