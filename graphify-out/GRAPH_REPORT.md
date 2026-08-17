# Graph Report - .  (2026-08-17)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 531 nodes · 1041 edges · 36 communities (24 shown, 12 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 96 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `789db3ea`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AAP Session & Repository Core
- ROADMAP: Hardware Pass Findings
- Device Pairing & Association
- AirPodsTransport Interface
- Tier B L2CAP Transport Impl
- AAP Packet Decoding
- App Startup & Battery Notification Wiring
- Battery Home-Screen Widget
- Auto-Pause & AirPodsState
- Home Screen Compose UI
- Docs, ADRs & Design Skill References
- AapPacketDecoder Tests
- Bluetooth Availability Monitoring
- Capability Resolver & Noise Modes
- Tier B Probe Cache
- Material 3 Design References
- AirPods Presence Service
- CapabilityResolver Tests
- Tier B Cache Manual Test Tool
- Gradle Wrapper Script
- Protocol Logging
- ROADMAP: Tier B Text Duplication Bug
- ROADMAP: Notification Icon Placeholder
- App Gradle Build Script
- BluetoothDevice Type Reference
- Design System Color
- Compose Modifier Reference
- Root Gradle Build Script
- ADR Template
- Gradle Settings Script

## God Nodes (most connected - your core abstractions)
1. `AirPodsState` - 29 edges
2. `AirPodsRepository` - 27 edges
3. `EarDetectionState` - 27 edges
4. `AapSession` - 26 edges
5. `FakeAirPodsTransport` - 26 edges
6. `AapEvent` - 23 edges
7. `BatteryComponentState` - 22 edges
8. `AirPodsRepositoryProvider` - 18 edges
9. `FakeTierProbeCache` - 16 edges
10. `HoldDuration` - 15 edges

## Surprising Connections (you probably didn't know these)
- `resumeObservingAssociatedDevices()` --calls--> `DataStoreTierProbeCache`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/core/bluetooth/AirPodsAssociationManager.kt → app/src/main/kotlin/dev/androidpods/core/data/DataStoreTierProbeCache.kt
- `HomeScreen()` --calls--> `DataStoreTierProbeCache`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt → app/src/main/kotlin/dev/androidpods/core/data/DataStoreTierProbeCache.kt
- `ControlsScreenPreview()` --calls--> `AndroidpodsTheme()`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/controls/ControlsScreen.kt → app/src/main/kotlin/dev/androidpods/core/designsystem/Theme.kt
- `SegmentOption()` --calls--> `androidpodsSpatialSpec()`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/controls/ControlsScreen.kt → app/src/main/kotlin/dev/androidpods/core/designsystem/Theme.kt
- `SensorStatusPill()` --calls--> `androidpodsSpatialSpec()`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/controls/ControlsScreen.kt → app/src/main/kotlin/dev/androidpods/core/designsystem/Theme.kt

## Import Cycles
- None detected.

## Communities (36 total, 12 thin omitted)

### Community 0 - "AAP Session & Repository Core"
Cohesion: 0.06
Nodes (16): AapSession, ByteArray, StemPressAndHoldAction, DISABLED, NOISE_CONTROL, VOICE_ASSISTANT, AirPodsRepository, AapSessionTest (+8 more)

### Community 1 - "ROADMAP: Hardware Pass Findings"
Cohesion: 0.06
Nodes (31): AirPodsTransport, HeadGesturesState, DISABLED, ENABLED, HoldDuration, DEFAULT, LONG, SHORT (+23 more)

### Community 2 - "Device Pairing & Association"
Cohesion: 0.10
Nodes (23): BatteryChargeStatus, CHARGING, DISCONNECTED, NOT_CHARGING, OPTIMIZED_CHARGING, BatteryComponentState, BatteryState, AirPodsState (+15 more)

### Community 3 - "AirPodsTransport Interface"
Cohesion: 0.11
Nodes (33): AirPodsIllustration(), drawAirPod(), Color, Modifier, androidpodsSpatialSpec(), BatteryCard(), ConnectedContent(), ConnectingContent() (+25 more)

### Community 4 - "Tier B L2CAP Transport Impl"
Cohesion: 0.10
Nodes (16): HeadGesture, NOD, NONE, SHAKE, HeadGestureDetector, MotionSample, CallGestureManager, TelephonyCallback (+8 more)

### Community 5 - "AAP Packet Decoding"
Cohesion: 0.09
Nodes (19): AppSettings, ThemeMode, DARK, LIGHT, SYSTEM, AppSettingsRepository, AppSettingsRepositoryProvider, Context (+11 more)

### Community 6 - "App Startup & Battery Notification Wiring"
Cohesion: 0.11
Nodes (16): L2capTierBProbeTest, AapTransport, AirPodsTransport, ByteArray, CoroutineScope, Flow, AirPodsPresenceService, BluetoothDevice (+8 more)

### Community 7 - "Battery Home-Screen Widget"
Cohesion: 0.11
Nodes (15): ChimePlayer, ChimeTarget, BOTH, CASE, LEFT, RIGHT, StateFlow, ProtocolLogging (+7 more)

### Community 8 - "Auto-Pause & AirPodsState"
Cohesion: 0.15
Nodes (21): AndroidpodsApp, dispatchPauseKeyEvent(), dispatchPlayKeyEvent(), Context, CoroutineScope, Flow, observeAutoPause(), ensureBatteryNotificationChannel() (+13 more)

### Community 9 - "Home Screen Compose UI"
Cohesion: 0.14
Nodes (18): MainActivity, AirPodsAssociationManager, CompanionDeviceManager, hasCompanionAssociation(), Context, CoroutineScope, observePresenceQuietly(), resumeObservingAssociatedDevices() (+10 more)

### Community 10 - "Docs, ADRs & Design Skill References"
Cohesion: 0.22
Nodes (13): AapEvent, AapPacketDecoder, AirPodsInformation, Battery, DeviceInfo, EarDetection, HeadGesturesConfig, HeadMotion (+5 more)

### Community 11 - "AapPacketDecoder Tests"
Cohesion: 0.16
Nodes (14): BluetoothAvailability, BroadcastReceiver, Disabled, Enabled, Context, Flow, Unsupported, AirPodsTileService (+6 more)

### Community 12 - "Bluetooth Availability Monitoring"
Cohesion: 0.16
Nodes (17): BatteryCard(), BatteryContent(), BatteryWidget, BatteryWidgetContent(), Context, NoDataContent(), BatteryWidgetReceiver, GlanceAppWidget (+9 more)

### Community 13 - "Capability Resolver & Noise Modes"
Cohesion: 0.20
Nodes (3): EarDetectionState, AutoPauseDecider, AutoPauseDeciderTest

### Community 14 - "Tier B Probe Cache"
Cohesion: 0.20
Nodes (4): AapPacketDecoderTest, ByteArray, loadFixturePacket(), object@L9

### Community 15 - "Material 3 Design References"
Cohesion: 0.16
Nodes (3): ForceTierBUnsupportedTest, DataStoreTierProbeCache, TierProbeCache

### Community 16 - "AirPods Presence Service"
Cohesion: 0.19
Nodes (9): AirPodsTransport, Connected, Connecting, ConnectionState, Disconnected, Failed, ByteArray, Flow (+1 more)

### Community 18 - "Tier B Cache Manual Test Tool"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **44 isolated node(s):** `object@L21`, `Unrecognized`, `Disabled`, `Enabled`, `Unsupported` (+39 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **12 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AirPodsState` connect `Device Pairing & Association` to `AAP Session & Repository Core`, `ROADMAP: Hardware Pass Findings`, `AirPodsTransport Interface`, `Tier B L2CAP Transport Impl`, `App Startup & Battery Notification Wiring`, `Battery Home-Screen Widget`, `Auto-Pause & AirPodsState`, `AapPacketDecoder Tests`, `Bluetooth Availability Monitoring`?**
  _High betweenness centrality (0.195) - this node is a cross-community bridge._
- **Why does `AirPodsRepositoryProvider` connect `App Startup & Battery Notification Wiring` to `AAP Session & Repository Core`, `ROADMAP: Hardware Pass Findings`, `Device Pairing & Association`, `AirPodsTransport Interface`, `Tier B L2CAP Transport Impl`, `Auto-Pause & AirPodsState`, `Home Screen Compose UI`, `Docs, ADRs & Design Skill References`, `AapPacketDecoder Tests`, `Bluetooth Availability Monitoring`?**
  _High betweenness centrality (0.159) - this node is a cross-community bridge._
- **Why does `AirPodsRepository` connect `AAP Session & Repository Core` to `ROADMAP: Hardware Pass Findings`, `Docs, ADRs & Design Skill References`, `Device Pairing & Association`, `App Startup & Battery Notification Wiring`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `AirPodsRepository` (e.g. with `.`a single failed probe does not yet skip the next connect attempt`()` and `.`battery event updates battery state`()`) actually correct?**
  _`AirPodsRepository` has 10 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `EarDetectionState` (e.g. with `.`decodes ear detection packet from real capture`()` and `.`inbound transport packets are decoded into AAP events`()`) actually correct?**
  _`EarDetectionState` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 8 inferred relationships involving `AapSession` (e.g. with `.`inbound transport packets are decoded into AAP events`()` and `.`setAssistantTriggerEnabled dispatches configuration for both earbuds`()`) actually correct?**
  _`AapSession` has 8 INFERRED edges - model-reasoned connections that need verification._
- **Are the 18 inferred relationships involving `FakeAirPodsTransport` (e.g. with `.`inbound transport packets are decoded into AAP events`()` and `.`setAssistantTriggerEnabled dispatches configuration for both earbuds`()`) actually correct?**
  _`FakeAirPodsTransport` has 18 INFERRED edges - model-reasoned connections that need verification._