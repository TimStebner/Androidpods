# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

This repository currently contains only `PROJECT.md` (the project specification) — there is no Android/Gradle project, source code, or build system yet. There are no build/lint/test commands to run until the project is bootstrapped (Milestone 0 in `PROJECT.md` §32).

When bootstrapping or writing any code here, **read `PROJECT.md` in full first** — it is the authoritative source of truth for this project (see its §31 "Source of Truth Rules for AI Coding Agents") and takes precedence over default assumptions. What follows is a condensed pointer to its key sections, not a replacement for it.

## What Androidpods is

A native Android companion app for Apple AirPods (Kotlin, Jetpack Compose, Material 3 Expressive), targeting Android 17/API 37 with `minSdk 29`. Goal: the richest possible AirPods experience on Android, built as a first-class native Android app rather than an iOS clone (`PROJECT.md` §1–2).

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

## Suggested module structure (§12)

Start simple — package boundaries inside few modules are fine initially; only split into a Gradle module when it provides a real build/ownership boundary:

```
app/
core/{common,model,bluetooth,airpods,data,designsystem}/
feature/{onboarding,home,controls,widgets,settings}/
build-logic/
gradle/libs.versions.toml
```

## Toolchain baseline (§4)

Exact dependency versions live in `gradle/libs.versions.toml`. Verified baseline (2026-08-12): AGP 9.3.1, Gradle 9.5.0 (not 9.6.0 — Kotlin 2.4.10 and AGP 9.3 both target 9.5.0 as their fully-supported baseline), Kotlin 2.4.10, Compose UI 1.11.4, Material 3 1.4.0 stable / 1.5.0-alpha25 expressive track. Never use dynamic versions (`1.+`, `latest.release`). Before upgrading any dependency, verify AGP/Kotlin/Gradle/API-37 compatibility and re-run the full test suite (§4 dependency version policy).

## Design system

Material 3 Expressive is a functional requirement, not decoration — expressive theme (`MaterialExpressiveTheme` with `MotionScheme.expressive()`), Dynamic Color with a distinct fallback scheme, spring-based motion tied to real state changes (connection, battery, ear-in/out, noise-mode), and expressive shape morphing, used with hierarchy rather than everywhere (§6). Isolate experimental Material 3 Expressive APIs behind the design-system layer so API churn doesn't spread through the app.

## Key domain models to know before writing protocol/UI code

- `AirPodsCapabilities` — per-device feature flags (§9)
- `AirPodsState` — the single authoritative state (connection, device, capabilities, battery, wear state, noise mode, settings) (§10)
- `NoiseControlMode` enum: `OFF, TRANSPARENCY, ADAPTIVE, NOISE_CANCELLATION` — capability layer decides which are valid per device (§18)
- `AirPodsRepository` — the only layer allowed to expose write operations like `setNoiseControlMode`, `setEarDetectionEnabled` (§11)

## Coding standards (§30)

Kotlin: idiomatic, explicit domain types, sealed interfaces/classes for finite state, immutable data, Coroutines/Flow. Avoid `GlobalScope`, blocking calls on Main, giant manager classes, unnecessary abstraction layers.

Compose: state hoisting, unidirectional data flow, previewable components. Never put business logic or Bluetooth access inside composables.

## Explicit non-goals for v1 (§8.4)

No root/Xposed, no Apple ID/iCloud, no Find My network integration, no AirPods firmware flashing, no unsafe Bluetooth stack modifications.

## Licensing (§25)

Do not copy third-party GPL-licensed implementation code into this project without an explicit licensing decision. Review licenses before using external protocol-research source code.
