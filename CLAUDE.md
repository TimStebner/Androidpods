# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

**Read `ROADMAP.md` first, every session** — it is the living session-handoff doc ("overwrite next session, don't append") and has the actual current state: what's hardware-confirmed, what's mid-flight, what's explicitly out of scope right now. This file (`CLAUDE.md`) only covers the stable architectural rules that don't change session to session.

The project has completed **Milestones 0–7**, fully hardware-validated on a physical **Pixel 9 Pro XL** (Android 17) paired with **AirPods 4**:
- **M0–M2**: Rootless Classic L2CAP Socket (PSM `0x1001` via `HiddenApiBypass`), AAP protocol decoder, calibrated wear-state ear detection, battery levels (L/R/Case), capability resolver.
- **M3–M4**: Auto-Pause/Resume, Press Speed (`0x25`), Press & Hold Duration (`0x26`), Head Gestures AAP toggle (`0x3E`), Capability-gated Assistant controls.
- **M5**: 1:1 Pixel-Twin Expressive Battery Widget (Glance), Quick Settings Tile (`AirPodsTileService`), persistent battery notification & connection banners.
- **M6**: Settings & Diagnostics (DataStore Preferences, ThemeMode, Dynamic Color, Tier-B Probe Cache reset, telemetry log).
- **M7**: 50Hz Live IMU Sensor-Streaming (Opcode `0x17`), 3D Spatial Motion Visualizer, Head Gestures Call Answering (Nod/Shake), Find My Audio Chime with channel isolation.

Build/lint/test commands (single `app` Gradle module):

```bash
./gradlew :app:assembleDebug          # build debug APK
./gradlew :app:testDebugUnitTest      # JVM unit tests
./gradlew :app:lintDebug              # lint
```

When writing any code here, **read `PROJECT.md` in full first** — it is the authoritative source of truth for this project (see its §31 "Source of Truth Rules for AI Coding Agents") and takes precedence over default assumptions.

## What Androidpods is

A native Android companion app for Apple AirPods (Kotlin, Jetpack Compose, Material 3 Expressive), targeting Android 17/API 37 with `minSdk 36` (Android 16 QPR3+). Goal: the richest possible AirPods experience on Android, built as a first-class native Android app rather than an iOS clone (`PROJECT.md` §1–2). The feature and architecture map is in `ROADMAP.md`.

## Core architectural rules (non-negotiable, see §11)

Layered architecture, strictly separated:

```
Bluetooth/Companion Device APIs → Transport → Protocol/Codec → Capability Resolver → Repository → State (StateFlow) → UI/Widgets/QuickSettings/Notifications/Assistant
```

- **Transport layer**: Bluetooth socket/channel lifecycle only. No UI logic.
- **Protocol layer**: AirPods packet parsing/encoding, constants, model identifiers. No Compose.
- **Repository layer**: high-level operations, state coordination, capability enforcement, persistence.
- **Presentation layer**: rendering + interaction only. Never constructs raw protocol packets.

Everything (Compose UI, Glance widgets, Quick Settings, notifications) derives from **one shared `AirPodsState` `StateFlow`** — do not build separate duplicated state machines per feature (§10).

### Capability-driven UI (§9)

`UI renders capabilities. It does not infer them.` Every AirPods feature is gated by an `AirPodsCapabilities` model derived from model/generation/firmware/protocol/Bluetooth-stack support. Unknown capability = not supported. Never show a control for a feature the connected model doesn't support, and never present something as working before it's actually implemented and hardware-validated (§2.6, §33).

### Event-driven, not polling-driven (§2.3, §13.4)

Battery efficiency is a first-class requirement. Priority order: AirPods protocol event → Bluetooth system event → cached state → conservative documented fallback polling only when unavoidable. No continuous BLE scanning, no permanent wake locks/foreground services, no high-frequency timers (§14).

## Module structure (§12)

Single `app` Gradle module with clean package boundaries under `dev.androidpods`:

```
app/                                   (Application, MainActivity)
core/{airpods,bluetooth,data,designsystem,gestures,media,telecom}/
feature/{controls,home,notifications,onboarding,settings,tiles,widgets}/
```

## Toolchain baseline (§4)

Exact dependency versions live in `gradle/libs.versions.toml`. Baseline: AGP 9.3.1, Gradle 9.5.0, Kotlin 2.4.10, Compose BOM 2026.08.00 (Compose UI 1.12.0), Material 3 Expressive 1.5.0-alpha26.

## Design system

Material 3 Expressive is a functional requirement: expressive theme (`MaterialExpressiveTheme` with `MotionScheme.expressive()`), Dynamic Color with a distinct fallback scheme, spring-based motion (`androidpodsSpatialSpec()`), and expressive shapes.

## Explicit non-goals for v1 (§8.4)

No root/Xposed, no Apple ID/iCloud, no Find My network reverse-engineering, no AirPods firmware flashing, no unsafe Bluetooth stack modifications.

## Licensing (§25)

Do not copy third-party GPL-licensed implementation code into this project without an explicit licensing decision. Review licenses before using external protocol-research source code.
