# Graph Report - Androidpods  (2026-08-17)

## Corpus Check
- Corpus is ~43,882 words - fits in a single context window. You may not need a graph.

## Summary
- 303 nodes · 568 edges · 24 communities (20 shown, 4 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 42 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Community 0
- Community 1
- Community 2
- Community 3
- Community 4
- Community 5
- Community 6
- Community 7
- Community 8
- Community 9
- Community 10
- Community 11
- Community 12
- Community 13
- Community 14
- Community 15
- Community 16
- Community 17

## God Nodes (most connected - your core abstractions)
1. `FakeAirPodsTransport` - 22 edges
2. `AirPodsTransport` - 20 edges
3. `AirPodsRepository` - 19 edges
4. `AirPodsState` - 19 edges
5. `EarDetectionState` - 17 edges
6. `BatteryComponentState` - 16 edges
7. `FakeTierProbeCache` - 15 edges
8. `AapEvent` - 12 edges
9. `AapTransport` - 12 edges
10. `AirPodsRepositoryTest` - 12 edges

## Surprising Connections (you probably didn't know these)
- `observeWidgetUpdates()` --calls--> `BatteryWidget`  [INFERRED]
  app/src/main/kotlin/dev/androidpods/feature/widgets/WidgetUpdates.kt → app/src/main/kotlin/dev/androidpods/feature/widgets/BatteryWidget.kt
- `BatteryColumn()` --references--> `BatteryComponentState`  [EXTRACTED]
  app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt → app/src/main/kotlin/dev/androidpods/core/airpods/AapPacketDecoder.kt
- `BatteryCell()` --references--> `BatteryComponentState`  [EXTRACTED]
  app/src/main/kotlin/dev/androidpods/feature/widgets/BatteryWidget.kt → app/src/main/kotlin/dev/androidpods/core/airpods/AapPacketDecoder.kt
- `EarDetectionRow()` --references--> `EarDetectionState`  [EXTRACTED]
  app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt → app/src/main/kotlin/dev/androidpods/core/airpods/AapPacketDecoder.kt
- `AapSession` --references--> `AapEvent`  [EXTRACTED]
  app/src/main/kotlin/dev/androidpods/core/airpods/AapSession.kt → app/src/main/kotlin/dev/androidpods/core/airpods/AapPacketDecoder.kt

## Import Cycles
- None detected.

## Communities (24 total, 4 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.10
Nodes (13): AapSession, Flow, AirPodsRepository, StateFlow, AapSessionTest, FakeAirPodsTransport, ByteArray, Flow (+5 more)

### Community 1 - "Community 1"
Cohesion: 0.14
Nodes (24): AndroidpodsApp, AirPodsState, dispatchPauseKeyEvent(), Context, CoroutineScope, Flow, observeAutoPause(), ensureBatteryNotificationChannel() (+16 more)

### Community 2 - "Community 2"
Cohesion: 0.11
Nodes (14): L2capTierBProbeTest, AapTransport, ByteArray, CoroutineScope, Flow, AirPodsPresenceService, BluetoothDevice, AirPodsRepositoryProvider (+6 more)

### Community 3 - "Community 3"
Cohesion: 0.15
Nodes (23): AirPodsIllustration(), drawAirPod(), Color, Modifier, androidpodsSpatialSpec(), BatteryColumn(), ConnectedContent(), ConnectingContent() (+15 more)

### Community 4 - "Community 4"
Cohesion: 0.14
Nodes (17): MainActivity, AirPodsAssociationManager, CompanionDeviceManager, hasCompanionAssociation(), Context, observePresenceQuietly(), resumeObservingAssociatedDevices(), hasBluetoothPermissions() (+9 more)

### Community 5 - "Community 5"
Cohesion: 0.14
Nodes (11): BatteryChargeStatus, CHARGING, DISCONNECTED, NOT_CHARGING, OPTIMIZED_CHARGING, BatteryComponentState, BatteryState, BatteryNotificationUiState (+3 more)

### Community 6 - "Community 6"
Cohesion: 0.13
Nodes (10): EarDetectionState, AirPodsCapabilities, CapabilityResolver, NoiseControlMode, ADAPTIVE, NOISE_CANCELLATION, OFF, TRANSPARENCY (+2 more)

### Community 7 - "Community 7"
Cohesion: 0.18
Nodes (15): BatteryCell(), BatteryContent(), BatteryWidget, BatteryWidgetContent(), Context, GlanceAppWidget, NoDataContent(), Battery (+7 more)

### Community 8 - "Community 8"
Cohesion: 0.28
Nodes (8): AapEvent, AapPacketDecoder, AirPodsInformation, Battery, DeviceInfo, EarDetection, ByteArray, Unrecognized

### Community 9 - "Community 9"
Cohesion: 0.19
Nodes (3): ForceTierBUnsupportedTest, DataStoreTierProbeCache, TierProbeCache

### Community 10 - "Community 10"
Cohesion: 0.19
Nodes (9): AirPodsTransport, Connected, Connecting, ConnectionState, Disconnected, Failed, ByteArray, Flow (+1 more)

### Community 11 - "Community 11"
Cohesion: 0.25
Nodes (4): AapPacketDecoderTest, ByteArray, loadFixturePacket(), object@L12

### Community 12 - "Community 12"
Cohesion: 0.33
Nodes (8): BluetoothAvailability, BroadcastReceiver, Disabled, Enabled, Context, Flow, Unsupported, Intent

### Community 15 - "Community 15"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **19 isolated node(s):** `CHARGING`, `NOT_CHARGING`, `DISCONNECTED`, `OPTIMIZED_CHARGING`, `Unrecognized` (+14 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AirPodsTransport` connect `Community 10` to `Community 0`, `Community 1`, `Community 2`, `Community 3`, `Community 5`, `Community 6`?**
  _High betweenness centrality (0.188) - this node is a cross-community bridge._
- **Why does `AirPodsRepositoryProvider` connect `Community 2` to `Community 0`, `Community 1`, `Community 3`, `Community 7`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **Why does `AirPodsState` connect `Community 1` to `Community 0`, `Community 2`, `Community 3`, `Community 5`, `Community 6`, `Community 7`, `Community 8`?**
  _High betweenness centrality (0.133) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `AirPodsRepository` (e.g. with `.`a single failed probe does not yet skip the next connect attempt`()` and `.`battery event updates battery state`()`) actually correct?**
  _`AirPodsRepository` has 10 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `EarDetectionState` (e.g. with `.`decodes ear detection packet from real capture`()` and `.`inbound transport packets are decoded into AAP events`()`) actually correct?**
  _`EarDetectionState` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `CHARGING`, `NOT_CHARGING`, `DISCONNECTED` to the rest of the system?**
  _19 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.10384615384615385 - nodes in this community are weakly interconnected._