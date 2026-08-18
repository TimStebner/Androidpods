# AGENTS.md

Guidance for AI coding assistants (Antigravity / Gemini, OpenAI Codex, Claude Code) working in this repository.

## Quick Orientation

- **Authoritative Spec**: [`PROJECT.md`](PROJECT.md) is the absolute source of truth (§31). Always follow its architectural and platform requirements.
- **Living Session Handoff & Feature Map**: [`ROADMAP.md`](ROADMAP.md) tracks the latest progress, architecture map, and component overview.
- **Agent Guidelines**: [`CLAUDE.md`](CLAUDE.md) contains detailed development rules, coding standards, and commands.

## Current Project State

- **Milestones 0–7 + Expressive UX, Pop-up & Performance/Battery Optimization**: Fully implemented and hardware-verified on physical **Pixel 9 Pro XL** with **AirPods 4** (MVP, Settings, 50Hz Spatial IMU stream with lifecycle auto-stop, Head Gestures Call Controls, Find My Audio Chime, Battery Pop-up, Last-Known Case Battery Preservation, Dynamic Generation Icons, Link-Aware Bluetooth Lifecycle, Zero-Allocation Drawing, and Deferred RenderNode State Reads).

## Core Rules

1. **Layered Architecture (§11)**:
   `Bluetooth/CDM APIs → Transport → Protocol/Codec → Capability Resolver → Repository → StateFlow (AirPodsState) → UI/Widgets/QuickSettings/Notifications`
2. **Single State Source (§10)**: All UI surfaces (Compose, Glance widgets, Quick Settings, Notifications) observe `AirPodsRepository.state` (`AirPodsState`). Never create secondary state holders.
3. **Capability-Driven UI (§9, §2.6)**: UI strictly renders capabilities. Unsupported features must be hidden or disabled with an honest explanation; never display fake or untested controls.
4. **Event-Driven Lifecycle (§13.4, §14)**: Rely on `CompanionDeviceService` presence events and AAP packets. No continuous BLE scanning or permanent wake locks. Active ACL link checks (`isDeviceAclConnected`) prevent spurious socket timeouts when AirPods are closed in their case.
5. **Gating Rules**:
   - **M3 (Write Controls / ANC / Transparency)** is strictly gated. Do not implement or send write commands without explicit user instruction and real hardware validation (§33).
   - **Tier B Transport**: Uses classic L2CAP socket reflection via `HiddenApiBypass` on PSM `0x1001` ([ADR-0001](docs/adr/0001-tier-b-hidden-l2cap-socket.md)).
   - **AAP Handshake**: The `delay(200)` between initial packets in `AapSession.start()` is load-bearing.
6. **Efficiency & Simplicity (Ponytail Principle)**: Keep changes minimal, maintain the single `app` module structure, avoid speculative abstractions, and verify with tests and lint.
7. **Performance & Zero-Allocation UI**: Continuous animations MUST defer state reads to `Modifier.graphicsLayer { ... }` lambdas. Draw loops (`Canvas`, `DrawScope`) MUST NOT instantiate `Path`, array, or `Rect` allocations per frame. Background StateFlow observers MUST use `distinctUntilChangedBy` before invoking system IPC.

## Verification Commands

```bash
./gradlew :app:assembleDebug          # Build debug APK
./gradlew :app:testDebugUnitTest      # JVM unit tests (67 tests)
./gradlew :app:lintDebug              # Android Lint (0 errors)
```
