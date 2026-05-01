# CoinRoutine — Kotlin Multiplatform Crypto Wallet

_A cross-platform cryptocurrency wallet simulation built with Kotlin Multiplatform, sharing UI and business logic between Android and iOS._

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.3-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Android](https://img.shields.io/badge/Android-minSdk%2024%20%7C%20targetSdk%2036-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![iOS](https://img.shields.io/badge/iOS-supported-000000?logo=apple&logoColor=white)](https://developer.apple.com)
[![CI Android](https://github.com/gugabrilhante/CoinRoutine/actions/workflows/android-ci.yml/badge.svg)](https://github.com/gugabrilhante/CoinRoutine/actions/workflows/android-ci.yml)
[![CI iOS](https://github.com/gugabrilhante/CoinRoutine/actions/workflows/ios-ci.yml/badge.svg)](https://github.com/gugabrilhante/CoinRoutine/actions/workflows/ios-ci.yml)
[![KMP Tests](https://github.com/gugabrilhante/CoinRoutine/actions/workflows/kmp-tests.yml/badge.svg)](https://github.com/gugabrilhante/CoinRoutine/actions/workflows/kmp-tests.yml)
[![Android Unit Tests](https://github.com/gugabrilhante/CoinRoutine/actions/workflows/android-unit-tests.yml/badge.svg)](https://github.com/gugabrilhante/CoinRoutine/actions/workflows/android-unit-tests.yml)
[![Android UI Tests](https://github.com/gugabrilhante/CoinRoutine/actions/workflows/android-ui-tests.yml/badge.svg)](https://github.com/gugabrilhante/CoinRoutine/actions/workflows/android-ui-tests.yml)
[![Codecov](https://codecov.io/gh/gugabrilhante/CoinRoutine/branch/master/graph/badge.svg)](https://codecov.io/gh/gugabrilhante/CoinRoutine)

---

## 📱 Demo

> 🎥 [Watch the full demonstration](https://github.com/gugabrilhante/CoinRoutine/issues/1#issue-4129287259)

<p align="center">
  <img src="docs/demonstration_coin_routine.gif" alt="CoinRoutine Android demo" width="320"/>
</p>

---

## ✨ Highlights

- 🌐 **Kotlin Multiplatform** — shared business logic and UI between Android and iOS from a single codebase
- 🎨 **Compose Multiplatform** — native-feeling UI on both platforms with animations, dark/light themes, and custom components
- 🏛️ **Clean Architecture** — strict separation of Domain, Data, and Presentation layers per feature
- 🔄 **MVVM + MVI-like state** — unidirectional data flow with `StateFlow` and sealed state/event classes
- 🔌 **Ktor 3** — async REST API integration fetching live cryptocurrency data
- 💾 **Room 2.8 + SQLite Bundled** — local persistence shared across platforms
- 💉 **Koin 4** — compile-safe dependency injection with multiplatform modules
- 🔐 **Biometric Authentication** — fingerprint/Face ID via `expect/actual` for Android and iOS
- 🧪 **Automated Testing** — unit and UI tests with Kotlin Test, Turbine, and AssertK
- ⚙️ **CI/CD with GitHub Actions** — automated Android build, iOS build, unit tests, and Kover coverage reports

---

## 🛠️ Tech Stack

| Category | Technology | Version |
|---|---|---|
| Language | Kotlin | 2.3.20 |
| UI Framework | Compose Multiplatform | 1.10.3 |
| Networking | Ktor | 3.4.2 |
| Dependency Injection | Koin | 4.2.0 |
| Local Database | Room + SQLite Bundled | 2.8.4 / 2.6.2 |
| Image Loading | Coil | 3.4.0 |
| Async / Reactive | Kotlin Coroutines + Flow | 1.10.2 |
| Serialization | kotlinx.serialization | 1.10.0 |
| Testing | Kotlin Test + Turbine + AssertK | — |
| Coverage | Kover | 0.9.8 |
| CI/CD | GitHub Actions | — |

---

## 🏛️ Architecture

The project applies **Clean Architecture** across all features. Dependencies always point inward toward the **Domain layer**, which is the core of the application and depends on nothing else. Both the Presentation and Data layers depend on Domain — never the other way around.

```text
        Presentation → Domain ← Data
```

---

## 🧪 Quality Engineering & Test Strategy

This project follows a rigorous testing strategy adapted for Kotlin Multiplatform to ensure stability and maintainability.

### 🏛️ Multiplatform Test Pyramid

1.  **Shared Module (commonTest)**
    - **Unit Tests**: Focus on business logic, use cases, and domain models using `kotlin.test`, `Turbine` for Flow testing, and `AssertK`.
    - Fast, deterministic, and platform-independent.

2.  **Android Module (androidUnitTest)**
    - **UI Tests (Compose)**: Uses Robolectric + Compose UI Test framework to validate UI components without a physical device.
    - **Integration Tests**: Tests the data layer (Room DAOs and Repositories) using in-memory databases.

3.  **UI Testing (primary)**
    - Prefer **Compose UI Test** APIs over Espresso for better synchronization and idiomatic Compose testing.
    - Extensive use of `Modifier.testTag()` for robust element locating.

### ⚙️ CI / CD & Coverage

- **GitHub Actions**: Modular pipelines for KMP tests, Android Unit tests, and Android UI tests. Pull Requests are automatically validated.
- **Kover + Codecov**: Automated code coverage reports integrated into the CI pipeline.

---

## 📂 Project Structure

```text
CoinRoutine/
├── composeApp/
│   └── src/
│       ├── commonMain/kotlin/.../coinroutine/
│       │   ├── coins/          # Coins list & detail feature
│       │   ├── portfolio/      # Portfolio management feature
│       │   ├── trade/          # Buy & sell feature
│       │   ├── core/           # Network, DB, error handling, navigation
│       │   ├── di/             # Shared Koin module
│       │   └── theme/          # Design system (colors, typography)
│       ├── androidMain/        # Android entry point & platform implementations
│       └── iosMain/            # iOS entry point & platform implementations
├── iosApp/                     # Xcode project & SwiftUI wrapper
├── .github/workflows/          # CI/CD pipelines
└── gradle/libs.versions.toml   # Version catalog
```

---

## ⚙️ CI / CD

Automated pipelines run on every push and pull request to `master`:

| Pipeline | Trigger | What it does |
|---|---|---|
| `android-ci.yml` | push / PR | Assembles Android debug APK |
| `ios-ci.yml` | push / PR | Compiles Kotlin/Native for iOS · Builds via Xcode · Runs iOS unit tests |
| `kmp-tests.yml` | push / PR | Runs common unit tests · Generates Kover XML coverage report |
| `android-unit-tests.yml` | push / PR | Runs Android JVM unit tests (including Robolectric UI tests) |

---

## 🚀 Getting Started

### Prerequisites

Follow the official setup guide for your environment:
👉 [Kotlin Multiplatform — Get Started](https://kotlinlang.org/docs/multiplatform/get-started.html)

- Android Studio with KMP plugin
- Xcode (for iOS builds)
- JDK 17+

### API Configuration

This project uses the [CoinRanking API](https://coinranking.com). Create an account and generate your API key, then configure credentials for each platform:

**Android** — create `secrets.properties` in the project root:
```properties
API_KEY="your_api_key_here"
API_BASE_URL="your_base_url_here"
```

**iOS** — create `Secrets.plist` inside `iosApp/iosApp/` in Xcode with:
```text
apiKey  → String → your_api_key_here
apiUrl  → String → your_base_url_here
```

### Build & Run

**Android**
```shell
./gradlew :composeApp:assembleDebug
```

**iOS** — open `iosApp/` in Xcode and run, or compile Kotlin/Native directly:
```shell
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

---

## 📄 About

Project developed while taking the Udemy course **"Ultimate Compose Multiplatform: Android/iOS + Testing"**, then extended with automated testing (Kotlin Test, Turbine, AssertK), Kover coverage, and full CI/CD pipelines with GitHub Actions.

---

> Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
