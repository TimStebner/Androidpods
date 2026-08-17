# Androidpods

A native Android companion app for Apple AirPods — the richest AirPods experience technically
possible outside the Apple ecosystem, built as a first-class Android app rather than an iOS
clone. See [`PROJECT.md`](PROJECT.md) for the full project specification (the authoritative source of truth).

## Current Status

The core functional MVP for **AirPods 4** is implemented:

- **Transport & Protocol (Tier B)**: Rootless BR/EDR classic L2CAP socket connection to PSM `0x1001` via `HiddenApiBypass`, AAP handshake, packet decoding for battery (Left/Right/Case), charging flags, and ear-detection wear state.
- **Device Lifecycle**: Companion Device Manager (`CompanionDeviceManager`) pairing filter and `CompanionDeviceService` presence tracking.
- **State & Architecture**: Single reactive `AirPodsState` Flow managed by `AirPodsRepository`, model capability resolution (`CapabilityResolver`), and DataStore tier-probe caching.
- **UI & Design System**: Jetpack Compose + Material 3 Expressive (`AndroidpodsTheme`), edge-to-edge, dynamic color with fallback, responsive device illustration (`DeviceIllustration`), and system reduced-motion support.
- **Android Integration**: Glance battery home-screen widget (`BatteryWidget`), persistent low-priority battery status notification (`BatteryNotification`), high-priority auto-canceling connection banner notification (`ConnectionNotification`), adaptive launcher icons, and media auto-pause on ear removal (`AutoPause`).
- **Gated**: Write controls (M3: ANC / Transparency / Adaptive toggles) are explicitly gated until verified. Tier A (BLE advertisement fallback) is deferred.

Session progress and next steps are tracked in [`ROADMAP.md`](ROADMAP.md), and the active milestone plan lives in [`PLAN-CURRENT.md`](PLAN-CURRENT.md).

## Requirements & Building

- **JDK 21** for Gradle daemon and Java toolchain
- **Android SDK**: `compileSdk 37`, `minSdk 36` (Android 16 QPR3+ / Android 17), `build-tools;37.0.0` (or `36.0.0`)

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest

# Run lint checks
./gradlew :app:lintDebug
```

## Architecture & Decisions

- Architecture and requirements: [`PROJECT.md`](PROJECT.md)
- Architectural Decision Records: [`docs/adr/`](docs/adr/)
  - [ADR-0001: Hidden L2CAP Socket for Tier B Transport](docs/adr/0001-tier-b-hidden-l2cap-socket.md)
  - [ADR-0002: GPL-3.0-or-later Licensing](docs/adr/0002-gpl-3.0-licensing.md)

## License

Androidpods is licensed under [GPL-3.0-or-later](LICENSE). See [`NOTICE.md`](NOTICE.md) for third-party attribution.

*Androidpods is not affiliated with, endorsed by, or associated with Apple Inc. AirPods and Apple are trademarks of Apple Inc.*

