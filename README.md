# Androidpods

A native Android companion app for Apple AirPods — the richest AirPods experience technically
possible outside the Apple ecosystem, built as a first-class Android app rather than an iOS
clone. See [`PROJECT.md`](PROJECT.md) for the full project specification; it is the
authoritative source of truth for this project.

## Status

Milestone 0 (bootstrap): a single `app` module with package boundaries per `PROJECT.md` §12,
the `AndroidpodsTheme` design-system layer, and a disconnected-state Home screen. No Bluetooth
or AirPods integration yet.

## Building

Requirements:

- JDK 21 for the Gradle daemon (see `gradle.properties` / `org.gradle.java.home` if your
  system JDK isn't 21)
- Android SDK with `compileSdk 37` platform and `build-tools;37.0.0` installed

```
./gradlew assembleDebug
```

## License

Androidpods is licensed under [GPL-3.0-or-later](LICENSE). See [`NOTICE.md`](NOTICE.md) for
third-party attribution and the SPDX/attribution convention used in this repository.

Androidpods is not affiliated with, endorsed by, or associated with Apple Inc. AirPods and
Apple are trademarks of Apple Inc.
