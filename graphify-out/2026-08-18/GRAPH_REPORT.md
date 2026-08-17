# Graph Report - .  (2026-08-18)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 606 nodes · 1194 edges · 35 communities (26 shown, 9 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 98 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3b6aa763`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AirPodsRepository
- HomeScreen.kt
- OnboardingScreen.kt
- AppSettingsRepository
- AirPodsState
- CallGestureManager.kt
- AirPodsRepositoryProvider.kt
- ChimePlayer
- Auto-Pause & AirPodsState
- ControlsScreen.kt
- Docs, ADRs & Design Skill References
- AapPacketDecoder Tests
- EarDetectionState
- AirPodsGenerationMorphBadge
- MorphingShape.kt
- BatteryWidget.kt
- AapPacketDecoderTest
- AirPodsTransport
- AppHaptics
- CapabilityResolver.kt
- CapabilityResolverTest
- AirPodsIllustration
- gradlew
- SmokeTest
- ByteArray
- StateFlow
- BluetoothDevice
- AirPodsState
- Color
- GlanceAppWidget

## God Nodes (most connected - your core abstractions)
1. `androidpodsSpatialSpec()` - 28 edges
2. `AirPodsRepository` - 27 edges
3. `FakeAirPodsTransport` - 26 edges
4. `EarDetectionState` - 26 edges
5. `AapSession` - 25 edges
6. `AapEvent` - 20 edges
7. `rememberAppHaptics()` - 19 edges
8. `FakeTierProbeCache` - 16 edges
9. `AirPodsState` - 16 edges
10. `AppSettingsRepository` - 15 edges

## Surprising Connections (you probably didn't know these)
- `HomeScreenConnectedDarkPreview()` --calls--> `EarDetectionState`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt → app/src/main/kotlin/dev/androidpods/core/airpods/AapPacketDecoder.kt
- `observeWidgetUpdates()` --calls--> `BatteryWidget`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/widgets/WidgetUpdates.kt → app/src/main/kotlin/dev/androidpods/feature/widgets/BatteryWidget.kt
- `HomeScreen()` --calls--> `DataStoreTierProbeCache`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt → app/src/main/kotlin/dev/androidpods/core/data/DataStoreTierProbeCache.kt
- `SettingsScreen()` --calls--> `DataStoreTierProbeCache`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/settings/SettingsScreen.kt → app/src/main/kotlin/dev/androidpods/core/data/DataStoreTierProbeCache.kt
- `HomeScreenConnectedDarkPreview()` --calls--> `BatteryState`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt → app/src/main/kotlin/dev/androidpods/core/airpods/AapPacketDecoder.kt

## Import Cycles
- None detected.

## Communities (35 total, 9 thin omitted)

### Community 0 - "AirPodsRepository"
Cohesion: 0.05
Nodes (21): AapEvent, AirPodsTransport, AapSession, ByteArray, Flow, AirPodsRepository, AirPodsState, Flow (+13 more)

### Community 1 - "HomeScreen.kt"
Cohesion: 0.09
Nodes (52): AudioWaveformVisualizer(), Color, Dp, Modifier, ExpressiveScreenHeader(), Color, Dp, ImageVector (+44 more)

### Community 2 - "OnboardingScreen.kt"
Cohesion: 0.08
Nodes (24): ForceTierBUnsupportedTest, MainActivity, AirPodsAssociationManager, CompanionDeviceManager, hasCompanionAssociation(), Context, CoroutineScope, observePresenceQuietly() (+16 more)

### Community 3 - "AppSettingsRepository"
Cohesion: 0.06
Nodes (24): HeadGesturesState, DISABLED, ENABLED, HoldDuration, DEFAULT, LONG, SHORT, PressSpeed (+16 more)

### Community 4 - "AirPodsState"
Cohesion: 0.09
Nodes (21): BatteryChargeStatus, CHARGING, DISCONNECTED, NOT_CHARGING, OPTIMIZED_CHARGING, BatteryComponentState, BatteryState, AirPodsState (+13 more)

### Community 5 - "CallGestureManager.kt"
Cohesion: 0.10
Nodes (17): HeadGesture, NOD, NONE, SHAKE, HeadGestureDetector, MotionSample, CallGestureManager, TelephonyCallback (+9 more)

### Community 6 - "AirPodsRepositoryProvider.kt"
Cohesion: 0.11
Nodes (16): L2capTierBProbeTest, AapTransport, AirPodsTransport, CoroutineScope, Flow, AirPodsPresenceService, BluetoothDevice, AirPodsRepositoryProvider (+8 more)

### Community 7 - "ChimePlayer"
Cohesion: 0.10
Nodes (16): ChimePlayer, ChimeTarget, BOTH, CASE, LEFT, RIGHT, StateFlow, ProtocolLogging (+8 more)

### Community 8 - "Auto-Pause & AirPodsState"
Cohesion: 0.15
Nodes (21): AndroidpodsApp, dispatchPauseKeyEvent(), dispatchPlayKeyEvent(), Context, CoroutineScope, Flow, observeAutoPause(), ensureBatteryNotificationChannel() (+13 more)

### Community 9 - "ControlsScreen.kt"
Cohesion: 0.15
Nodes (22): AndroidpodsTheme(), isReducedMotion(), ControlsScreen(), ControlsScreenContent(), ControlsScreenPreview(), InfoRow(), AirPodsState, HoldDuration (+14 more)

### Community 10 - "Docs, ADRs & Design Skill References"
Cohesion: 0.22
Nodes (13): AapEvent, AapPacketDecoder, AirPodsInformation, Battery, DeviceInfo, EarDetection, HeadGesturesConfig, HeadMotion (+5 more)

### Community 11 - "AapPacketDecoder Tests"
Cohesion: 0.16
Nodes (14): BluetoothAvailability, BroadcastReceiver, Disabled, Enabled, Context, Flow, Unsupported, AirPodsTileService (+6 more)

### Community 12 - "EarDetectionState"
Cohesion: 0.20
Nodes (3): EarDetectionState, AutoPauseDecider, AutoPauseDeciderTest

### Community 13 - "AirPodsGenerationMorphBadge"
Cohesion: 0.21
Nodes (15): AirPodsGeneration, GEN_1_2, GEN_3, GEN_4, MAX, PRO, AirPodsGenerationMorphBadge(), drawAirPodsGen1Silhouette() (+7 more)

### Community 14 - "MorphingShape.kt"
Cohesion: 0.21
Nodes (12): calculateBoundsRect(), drawMorphShape(), Color, Dp, Modifier, MorphingShapeHero(), toComposePath(), MorphingShapeTest (+4 more)

### Community 15 - "BatteryWidget.kt"
Cohesion: 0.21
Nodes (14): BatteryCard(), BatteryContent(), BatteryWidget, BatteryWidgetContent(), BatteryComponentState, Context, NoDataContent(), BatteryWidgetReceiver (+6 more)

### Community 16 - "AapPacketDecoderTest"
Cohesion: 0.20
Nodes (4): AapPacketDecoderTest, ByteArray, loadFixturePacket(), object@L9

### Community 17 - "AirPodsTransport"
Cohesion: 0.19
Nodes (9): AirPodsTransport, Connected, Connecting, ConnectionState, Disconnected, Failed, ByteArray, Flow (+1 more)

### Community 19 - "CapabilityResolver.kt"
Cohesion: 0.25
Nodes (7): AirPodsCapabilities, CapabilityResolver, NoiseControlMode, ADAPTIVE, NOISE_CANCELLATION, OFF, TRANSPARENCY

### Community 21 - "AirPodsIllustration"
Cohesion: 0.80
Nodes (4): AirPodsIllustration(), drawAirPod(), Color, Modifier

### Community 22 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **49 isolated node(s):** `object@L21`, `Unrecognized`, `Disabled`, `Enabled`, `Unsupported` (+44 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **9 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `EarDetectionState` connect `EarDetectionState` to `AirPodsRepository`, `HomeScreen.kt`, `AppSettingsRepository`, `AirPodsState`, `Auto-Pause & AirPodsState`, `Docs, ADRs & Design Skill References`, `AapPacketDecoderTest`?**
  _High betweenness centrality (0.117) - this node is a cross-community bridge._
- **Why does `AirPodsRepositoryProvider` connect `AirPodsRepositoryProvider.kt` to `AirPodsRepository`, `AirPodsState`, `CallGestureManager.kt`, `Auto-Pause & AirPodsState`, `Docs, ADRs & Design Skill References`, `AapPacketDecoder Tests`?**
  _High betweenness centrality (0.102) - this node is a cross-community bridge._
- **Why does `AapEvent` connect `Docs, ADRs & Design Skill References` to `AirPodsRepository`, `CallGestureManager.kt`, `AirPodsRepositoryProvider.kt`?**
  _High betweenness centrality (0.097) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `androidpodsSpatialSpec()` (e.g. with `AirPodsGenerationMorphBadge()` and `AirPodsIllustration()`) actually correct?**
  _`androidpodsSpatialSpec()` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 11 inferred relationships involving `AirPodsRepository` (e.g. with `AapSession` and `.`a single failed probe does not yet skip the next connect attempt`()`) actually correct?**
  _`AirPodsRepository` has 11 INFERRED edges - model-reasoned connections that need verification._
- **Are the 18 inferred relationships involving `FakeAirPodsTransport` (e.g. with `.`inbound transport packets are decoded into AAP events`()` and `.`setAssistantTriggerEnabled dispatches configuration for both earbuds`()`) actually correct?**
  _`FakeAirPodsTransport` has 18 INFERRED edges - model-reasoned connections that need verification._
- **Are the 3 inferred relationships involving `EarDetectionState` (e.g. with `HomeScreenConnectedDarkPreview()` and `.`decodes ear detection packet from real capture`()`) actually correct?**
  _`EarDetectionState` has 3 INFERRED edges - model-reasoned connections that need verification._