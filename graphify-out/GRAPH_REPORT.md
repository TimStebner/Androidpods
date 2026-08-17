# Graph Report - .  (2026-08-18)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 608 nodes · 1194 edges · 42 communities (30 shown, 12 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 98 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3b6aa763`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- HomeScreen.kt
- AndroidpodsTheme
- AppSettingsRepository
- AirPodsState
- CallGestureManager.kt
- AirPodsRepositoryProvider.kt
- ChimePlayer
- AutoPause.kt
- AapEvent
- ControlsScreen.kt
- AapSession
- FakeTierProbeCache
- AirPodsRepository
- EarDetectionState
- AirPodsGenerationMorphBadge
- MorphingShape.kt
- BatteryWidget.kt
- AapPacketDecoderTest
- AirPodsTransport
- AirPodsTileService.kt
- StemPressAndHoldAction
- CapabilityResolver.kt
- FakeAirPodsTransport
- CapabilityResolverTest
- AirPodsIllustration
- isReducedMotion
- AirPodsTransport
- gradlew
- SmokeTest
- ByteArray
- Context
- CoroutineScope
- StateFlow
- BluetoothDevice
- AirPodsState
- Color
- GlanceAppWidget

## God Nodes (most connected - your core abstractions)
1. `androidpodsSpatialSpec()` - 28 edges
2. `AirPodsRepository` - 27 edges
3. `EarDetectionState` - 26 edges
4. `FakeAirPodsTransport` - 26 edges
5. `AapSession` - 25 edges
6. `AapEvent` - 20 edges
7. `rememberAppHaptics()` - 19 edges
8. `FakeTierProbeCache` - 16 edges
9. `AirPodsState` - 16 edges
10. `AppSettingsRepository` - 15 edges

## Surprising Connections (you probably didn't know these)
- `HomeScreen()` --calls--> `DataStoreTierProbeCache`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt → app/src/main/kotlin/dev/androidpods/core/data/DataStoreTierProbeCache.kt
- `AirPodsRepository` --calls--> `AapSession`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/core/data/AirPodsRepository.kt → app/src/main/kotlin/dev/androidpods/core/airpods/AapSession.kt
- `HomeScreenConnectedDarkPreview()` --calls--> `EarDetectionState`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt → app/src/main/kotlin/dev/androidpods/core/airpods/AapPacketDecoder.kt
- `observeWidgetUpdates()` --calls--> `BatteryWidget`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/widgets/WidgetUpdates.kt → app/src/main/kotlin/dev/androidpods/feature/widgets/BatteryWidget.kt
- `HomeScreenConnectedDarkPreview()` --calls--> `BatteryState`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt → app/src/main/kotlin/dev/androidpods/core/airpods/AapPacketDecoder.kt

## Import Cycles
- None detected.

## Communities (42 total, 12 thin omitted)

### Community 0 - "HomeScreen.kt"
Cohesion: 0.07
Nodes (54): AudioWaveformVisualizer(), Color, Dp, Modifier, ExpressiveScreenHeader(), Color, Dp, ImageVector (+46 more)

### Community 1 - "AndroidpodsTheme"
Cohesion: 0.07
Nodes (33): ForceTierBUnsupportedTest, MainActivity, AirPodsAssociationManager, CompanionDeviceManager, hasCompanionAssociation(), observePresenceQuietly(), resumeObservingAssociatedDevices(), hasBluetoothPermissions() (+25 more)

### Community 2 - "AppSettingsRepository"
Cohesion: 0.07
Nodes (20): HeadGesturesState, DISABLED, ENABLED, HoldDuration, DEFAULT, LONG, SHORT, PressSpeed (+12 more)

### Community 3 - "AirPodsState"
Cohesion: 0.09
Nodes (21): BatteryChargeStatus, CHARGING, DISCONNECTED, NOT_CHARGING, OPTIMIZED_CHARGING, BatteryComponentState, BatteryState, AirPodsState (+13 more)

### Community 4 - "CallGestureManager.kt"
Cohesion: 0.10
Nodes (17): HeadGesture, NOD, NONE, SHAKE, HeadGestureDetector, MotionSample, CallGestureManager, TelephonyCallback (+9 more)

### Community 5 - "AirPodsRepositoryProvider.kt"
Cohesion: 0.11
Nodes (16): L2capTierBProbeTest, AapTransport, AirPodsTransport, CoroutineScope, Flow, AirPodsPresenceService, BluetoothDevice, AirPodsRepositoryProvider (+8 more)

### Community 6 - "ChimePlayer"
Cohesion: 0.10
Nodes (16): ChimePlayer, ChimeTarget, BOTH, CASE, LEFT, RIGHT, StateFlow, ProtocolLogging (+8 more)

### Community 7 - "AutoPause.kt"
Cohesion: 0.15
Nodes (21): AndroidpodsApp, dispatchPauseKeyEvent(), dispatchPlayKeyEvent(), Context, CoroutineScope, Flow, observeAutoPause(), ensureBatteryNotificationChannel() (+13 more)

### Community 8 - "AapEvent"
Cohesion: 0.22
Nodes (13): AapEvent, AapPacketDecoder, AirPodsInformation, Battery, DeviceInfo, EarDetection, HeadGesturesConfig, HeadMotion (+5 more)

### Community 9 - "ControlsScreen.kt"
Cohesion: 0.18
Nodes (19): BluetoothAvailability, BroadcastReceiver, Disabled, Enabled, Context, Flow, Unsupported, ControlsScreen() (+11 more)

### Community 10 - "AapSession"
Cohesion: 0.17
Nodes (3): AapSession, ByteArray, AapSessionTest

### Community 11 - "FakeTierProbeCache"
Cohesion: 0.17
Nodes (5): AirPodsRepositoryTest, ByteArray, loadFixturePacket(), object@L21, FakeTierProbeCache

### Community 12 - "AirPodsRepository"
Cohesion: 0.16
Nodes (8): AapEvent, AirPodsRepository, AirPodsState, Flow, HoldDuration, PressSpeed, HeadOrientation, StateFlow

### Community 13 - "EarDetectionState"
Cohesion: 0.20
Nodes (3): EarDetectionState, AutoPauseDecider, AutoPauseDeciderTest

### Community 14 - "AirPodsGenerationMorphBadge"
Cohesion: 0.21
Nodes (15): AirPodsGeneration, GEN_1_2, GEN_3, GEN_4, MAX, PRO, AirPodsGenerationMorphBadge(), drawAirPodsGen1Silhouette() (+7 more)

### Community 15 - "MorphingShape.kt"
Cohesion: 0.21
Nodes (12): calculateBoundsRect(), drawMorphShape(), Color, Dp, Modifier, MorphingShapeHero(), toComposePath(), MorphingShapeTest (+4 more)

### Community 16 - "BatteryWidget.kt"
Cohesion: 0.21
Nodes (14): BatteryCard(), BatteryContent(), BatteryWidget, BatteryWidgetContent(), BatteryComponentState, Context, NoDataContent(), BatteryWidgetReceiver (+6 more)

### Community 17 - "AapPacketDecoderTest"
Cohesion: 0.20
Nodes (4): AapPacketDecoderTest, ByteArray, loadFixturePacket(), object@L9

### Community 18 - "AirPodsTransport"
Cohesion: 0.19
Nodes (9): AirPodsTransport, Connected, Connecting, ConnectionState, Disconnected, Failed, ByteArray, Flow (+1 more)

### Community 19 - "AirPodsTileService.kt"
Cohesion: 0.30
Nodes (6): AirPodsTileService, Context, CoroutineScope, StateFlow, observeTileUpdates(), TileService

### Community 20 - "StemPressAndHoldAction"
Cohesion: 0.25
Nodes (4): StemPressAndHoldAction, DISABLED, NOISE_CONTROL, VOICE_ASSISTANT

### Community 21 - "CapabilityResolver.kt"
Cohesion: 0.25
Nodes (7): AirPodsCapabilities, CapabilityResolver, NoiseControlMode, ADAPTIVE, NOISE_CANCELLATION, OFF, TRANSPARENCY

### Community 22 - "FakeAirPodsTransport"
Cohesion: 0.31
Nodes (3): FakeAirPodsTransport, ByteArray, Flow

### Community 24 - "AirPodsIllustration"
Cohesion: 0.80
Nodes (4): AirPodsIllustration(), drawAirPod(), Color, Modifier

### Community 27 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **49 isolated node(s):** `object@L21`, `object@L9`, `Connected`, `Connecting`, `Disconnected` (+44 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **12 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `EarDetectionState` connect `EarDetectionState` to `HomeScreen.kt`, `AppSettingsRepository`, `AirPodsState`, `AutoPause.kt`, `AapEvent`, `AapSession`, `FakeTierProbeCache`, `AapPacketDecoderTest`, `AirPodsTransport`?**
  _High betweenness centrality (0.116) - this node is a cross-community bridge._
- **Why does `AirPodsRepositoryProvider` connect `AirPodsRepositoryProvider.kt` to `AirPodsState`, `CallGestureManager.kt`, `AutoPause.kt`, `AapEvent`, `AirPodsRepository`, `AirPodsTileService.kt`?**
  _High betweenness centrality (0.102) - this node is a cross-community bridge._
- **Why does `AapEvent` connect `AapEvent` to `AapSession`, `CallGestureManager.kt`, `AirPodsRepositoryProvider.kt`?**
  _High betweenness centrality (0.096) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `androidpodsSpatialSpec()` (e.g. with `AirPodsGenerationMorphBadge()` and `AirPodsIllustration()`) actually correct?**
  _`androidpodsSpatialSpec()` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 11 inferred relationships involving `AirPodsRepository` (e.g. with `AapSession` and `.`a single failed probe does not yet skip the next connect attempt`()`) actually correct?**
  _`AirPodsRepository` has 11 INFERRED edges - model-reasoned connections that need verification._
- **Are the 3 inferred relationships involving `EarDetectionState` (e.g. with `HomeScreenConnectedDarkPreview()` and `.`decodes ear detection packet from real capture`()`) actually correct?**
  _`EarDetectionState` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 18 inferred relationships involving `FakeAirPodsTransport` (e.g. with `.`inbound transport packets are decoded into AAP events`()` and `.`setAssistantTriggerEnabled dispatches configuration for both earbuds`()`) actually correct?**
  _`FakeAirPodsTransport` has 18 INFERRED edges - model-reasoned connections that need verification._