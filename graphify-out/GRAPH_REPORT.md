# Graph Report - Androidpods  (2026-08-18)

## Corpus Check
- 50 files · ~95,575 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 692 nodes · 1298 edges · 41 communities (31 shown, 10 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 52 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Cluster 0 (58 nodes)
- Cluster 1 (55 nodes)
- Cluster 2 (41 nodes)
- Cluster 3 (37 nodes)
- Cluster 4 (36 nodes)
- Cluster 5 (33 nodes)
- Cluster 6 (33 nodes)
- Cluster 7 (32 nodes)
- Cluster 8 (32 nodes)
- Cluster 9 (32 nodes)
- Cluster 10 (30 nodes)
- Cluster 11 (29 nodes)
- Cluster 12 (29 nodes)
- Cluster 13 (22 nodes)
- Cluster 14 (22 nodes)
- Cluster 15 (20 nodes)
- Cluster 16 (18 nodes)
- Cluster 17 (18 nodes)
- Cluster 18 (18 nodes)
- Cluster 19 (18 nodes)
- Cluster 20 (16 nodes)
- Cluster 21 (12 nodes)
- Cluster 22 (11 nodes)
- Cluster 23 (7 nodes)
- Cluster 24 (7 nodes)
- Cluster 25 (6 nodes)
- Cluster 26 (4 nodes)
- Cluster 27 (3 nodes)
- Cluster 29 (1 nodes)
- Cluster 32 (1 nodes)
- Cluster 33 (1 nodes)
- Cluster 35 (1 nodes)
- Cluster 36 (1 nodes)
- Cluster 37 (1 nodes)
- Cluster 38 (1 nodes)
- Cluster 39 (1 nodes)

## God Nodes (most connected - your core abstractions)
1. `AirPodsRepository` - 29 edges
2. `AapSession` - 24 edges
3. `AapEvent` - 20 edges
4. `EarDetectionState` - 20 edges
5. `FakeAirPodsTransport` - 17 edges
6. `AirPodsTransport` - 16 edges
7. `AirPodsState` - 16 edges
8. `AppSettingsRepository` - 16 edges
9. `BatteryComponentState` - 15 edges
10. `ControlsScreenContent()` - 14 edges

## Surprising Connections (you probably didn't know these)
- `Material 3 Expressive System Integration` --semantically_similar_to--> `MD3 Principles: Personal, Adaptive, Expressive`  [INFERRED] [semantically similar]
  PROJECT.md → .agents/skills/material-3/SKILL.md
- `AirPods 4 AAP Session Start Capture Fixture` --conceptually_related_to--> `ADR-0001: Hidden L2CAP Socket for Tier B Transport`  [INFERRED]
  app/src/test/resources/fixtures/aap/session-start-capture.txt → docs/adr/0001-tier-b-hidden-l2cap-socket.md
- `ADR-0001: Hidden L2CAP Socket for Tier B Transport` --rationale_for--> `Dual Transport Strategy (Tier A & Tier B)`  [INFERRED]
  docs/adr/0001-tier-b-hidden-l2cap-socket.md → PROJECT.md
- `AirPods 4 AAP Session Start Capture Fixture` --references--> `Androidpods Project Specification`  [EXTRACTED]
  app/src/test/resources/fixtures/aap/session-start-capture.txt → PROJECT.md
- `LibrePods Protocol Adaptation Notice` --conceptually_related_to--> `ADR-0002: GPL-3.0-or-later Licensing`  [INFERRED]
  NOTICE.md → docs/adr/0002-gpl-3.0-licensing.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **AirPods Protocol & Transport Architecture** — project_transport_tiers, docs_adr_0001_tier_b_hidden_l2cap_socket_l2capsocket, app_src_test_resources_fixtures_aap_session_start_capture_session_capture, docs_adr_0001_tier_b_hidden_l2cap_socket_tier_probe_cache [EXTRACTED 0.95]
- **AirPods State & Capability Data Flow** — project_architecture_layers, project_capability_driven_ui, project_airpods_state_model [EXTRACTED 0.95]
- **Androidpods State, Lifecycle & Battery Architecture** — roadmap_architecture_reference, project_smart_battery_merging, project_acl_aware_transport_lifecycle, project_m3_expressive_battery_popup [EXTRACTED 0.95]
- **Material Design 3 Token & Adaptive System** — _agents_skills_material_3_skill_skill, _agents_skills_material_3_references_color_system_color_system, _agents_skills_material_3_references_layout_and_responsive_layout_system, _agents_skills_material_3_references_typography_and_shape_visual_tokens [EXTRACTED 0.95]

## Communities (41 total, 10 thin omitted)

### Community 0 - "Cluster 0 (58 nodes)"
Cohesion: 0.06
Nodes (23): HeadGesturesState, DISABLED, ENABLED, HoldDuration, DEFAULT, LONG, SHORT, PressSpeed (+15 more)

### Community 1 - "Cluster 1 (55 nodes)"
Cohesion: 0.06
Nodes (27): BatteryChargeStatus, CHARGING, DISCONNECTED, NOT_CHARGING, OPTIMIZED_CHARGING, BatteryComponentState, BatteryState, AppHaptics (+19 more)

### Community 2 - "Cluster 2 (41 nodes)"
Cohesion: 0.08
Nodes (21): AppSettings, ThemeMode, DARK, LIGHT, SYSTEM, AppSettingsRepository, AppSettingsRepositoryProvider, Context (+13 more)

### Community 3 - "Cluster 3 (37 nodes)"
Cohesion: 0.10
Nodes (17): HeadGesture, NOD, NONE, SHAKE, HeadGestureDetector, MotionSample, CallGestureManager, TelephonyCallback (+9 more)

### Community 4 - "Cluster 4 (36 nodes)"
Cohesion: 0.09
Nodes (12): AapEvent, AirPodsRepository, AirPodsState, BatteryComponentState, Flow, HoldDuration, PressSpeed, StateFlow (+4 more)

### Community 5 - "Cluster 5 (33 nodes)"
Cohesion: 0.08
Nodes (19): AirPodsCapabilities, CapabilityResolver, NoiseControlMode, ADAPTIVE, NOISE_CANCELLATION, OFF, TRANSPARENCY, AirPodsTransport (+11 more)

### Community 6 - "Cluster 6 (33 nodes)"
Cohesion: 0.14
Nodes (29): AudioWaveformVisualizer(), Color, Dp, Modifier, ExpressiveScreenHeader(), Color, Dp, ImageVector (+21 more)

### Community 7 - "Cluster 7 (32 nodes)"
Cohesion: 0.10
Nodes (16): L2capTierBProbeTest, AapTransport, AirPodsTransport, ByteArray, CoroutineScope, Flow, AirPodsPresenceService, BluetoothDevice (+8 more)

### Community 8 - "Cluster 8 (32 nodes)"
Cohesion: 0.12
Nodes (23): Bundle, ComponentActivity, MainActivity, AirPodsAssociationManager, CompanionDeviceManager, hasCompanionAssociation(), isBluetoothDeviceConnected(), Context (+15 more)

### Community 9 - "Cluster 9 (32 nodes)"
Cohesion: 0.12
Nodes (29): Color, Dp, Modifier, StatusBarScrim(), ControlsScreen(), ControlsScreenContent(), ControlsScreenPreview(), ControlsSection (+21 more)

### Community 10 - "Cluster 10 (30 nodes)"
Cohesion: 0.11
Nodes (24): BluetoothAvailability, BroadcastReceiver, Disabled, Enabled, Context, Flow, Unsupported, BatteryCard() (+16 more)

### Community 11 - "Cluster 11 (29 nodes)"
Cohesion: 0.11
Nodes (15): ChimePlayer, ChimeTarget, BOTH, CASE, LEFT, RIGHT, StateFlow, ProtocolLogging (+7 more)

### Community 12 - "Cluster 12 (29 nodes)"
Cohesion: 0.15
Nodes (26): AirPodsGeneration, GEN_1_2, GEN_3, GEN_4, MAX, PRO, AirPodsGenerationMorphBadge(), drawAirPodsGen1Silhouette() (+18 more)

### Community 13 - "Cluster 13 (22 nodes)"
Cohesion: 0.22
Nodes (13): AapEvent, AapPacketDecoder, AirPodsInformation, Battery, DeviceInfo, EarDetection, HeadGesturesConfig, HeadMotion (+5 more)

### Community 14 - "Cluster 14 (22 nodes)"
Cohesion: 0.18
Nodes (18): AirPodsState, ensureBatteryNotificationChannel(), Context, updateBatteryNotification(), ensureConnectionNotificationChannel(), Context, postConnectionNotification(), Context (+10 more)

### Community 15 - "Cluster 15 (20 nodes)"
Cohesion: 0.12
Nodes (4): ForceTierBUnsupportedTest, DataStoreTierProbeCache, TierProbeCache, FakeTierProbeCache

### Community 16 - "Cluster 16 (18 nodes)"
Cohesion: 0.20
Nodes (14): AndroidpodsApp, dispatchPauseKeyEvent(), dispatchPlayKeyEvent(), AirPodsState, Context, CoroutineScope, Flow, observeAutoPause() (+6 more)

### Community 17 - "Cluster 17 (18 nodes)"
Cohesion: 0.20
Nodes (3): EarDetectionState, AutoPauseDecider, AutoPauseDeciderTest

### Community 18 - "Cluster 18 (18 nodes)"
Cohesion: 0.31
Nodes (16): AirPodsExpressiveTrio(), AnimatedAirPodExpressiveItem(), AnimatedAirPodsCase(), AnimatedLeftAirPod(), AnimatedRightAirPod(), drawExpressiveM3AirPod(), drawExpressiveM3Case(), Color (+8 more)

### Community 19 - "Cluster 19 (18 nodes)"
Cohesion: 0.20
Nodes (13): calculateBoundsRect(), drawMorphShape(), Color, Dp, Modifier, MorphingShapeHero(), toComposePath(), MorphingShapeTest (+5 more)

### Community 20 - "Cluster 20 (16 nodes)"
Cohesion: 0.20
Nodes (4): AapPacketDecoderTest, ByteArray, loadFixturePacket(), object@L9

### Community 21 - "Cluster 21 (12 nodes)"
Cohesion: 0.33
Nodes (7): AirPodsTileService, AirPodsState, Context, CoroutineScope, StateFlow, observeTileUpdates(), TileService

### Community 22 - "Cluster 22 (11 nodes)"
Cohesion: 0.25
Nodes (11): MD3 Principles: Personal, Adaptive, Expressive, AirPods 4 AAP Session Start Capture Fixture, ADR-0001: Hidden L2CAP Socket for Tier B Transport, DataStore Tier Probe Cache Strategy, ADR-0002: GPL-3.0-or-later Licensing, Architecture Decision Records Index, LibrePods Protocol Adaptation Notice, Third-Party Notices Index (+3 more)

### Community 23 - "Cluster 23 (7 nodes)"
Cohesion: 0.33
Nodes (7): MD3 Color System & Tonal Palettes, MD3 Component Catalog, MD3 Responsive Layout & Window Size Classes, MD3 Adaptive Navigation Patterns, MD3 Theming & Dynamic Color Guide, MD3 Typography, Shape, Elevation & Motion, Material Design 3 Skill Implementation Guide

### Community 24 - "Cluster 24 (7 nodes)"
Cohesion: 0.29
Nodes (7): AI Agents Orientation & Rules, Claude Code Standards & Rules, ACL-Link Aware Bluetooth Transport Lifecycle, Material 3 Expressive Battery Popup Architecture, Smart Battery Merging & Cache Retention, Androidpods Overview & Feature Summary, Roadmap & Architecture Reference

### Community 26 - "Cluster 26 (4 nodes)"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **72 isolated node(s):** `Disabled`, `Enabled`, `Unsupported`, `Unrecognized`, `NoData` (+67 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **10 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AirPodsRepositoryProvider` connect `Cluster 7 (32 nodes)` to `Cluster 1 (55 nodes)`, `Cluster 3 (37 nodes)`, `Cluster 4 (36 nodes)`, `Cluster 8 (32 nodes)`, `Cluster 10 (30 nodes)`, `Cluster 13 (22 nodes)`, `Cluster 14 (22 nodes)`?**
  _High betweenness centrality (0.153) - this node is a cross-community bridge._
- **Why does `AapEvent` connect `Cluster 13 (22 nodes)` to `Cluster 0 (58 nodes)`, `Cluster 3 (37 nodes)`, `Cluster 7 (32 nodes)`?**
  _High betweenness centrality (0.097) - this node is a cross-community bridge._
- **Why does `AirPodsTransport` connect `Cluster 5 (33 nodes)` to `Cluster 0 (58 nodes)`, `Cluster 1 (55 nodes)`, `Cluster 8 (32 nodes)`, `Cluster 11 (29 nodes)`, `Cluster 14 (22 nodes)`?**
  _High betweenness centrality (0.094) - this node is a cross-community bridge._
- **Are the 11 inferred relationships involving `AirPodsRepository` (e.g. with `.`a single failed probe does not yet skip the next connect attempt`()` and `.`battery event updates battery state`()`) actually correct?**
  _`AirPodsRepository` has 11 INFERRED edges - model-reasoned connections that need verification._
- **Are the 8 inferred relationships involving `AapSession` (e.g. with `.`inbound transport packets are decoded into AAP events`()` and `.`setAssistantTriggerEnabled dispatches configuration for both earbuds`()`) actually correct?**
  _`AapSession` has 8 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `EarDetectionState` (e.g. with `.`decodes ear detection packet from real capture`()` and `.`inbound transport packets are decoded into AAP events`()`) actually correct?**
  _`EarDetectionState` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Disabled`, `Enabled`, `Unsupported` to the rest of the system?**
  _72 weakly-connected nodes found - possible documentation gaps or missing edges._