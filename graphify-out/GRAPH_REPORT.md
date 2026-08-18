# Graph Report - Androidpods  (2026-08-18)

## Corpus Check
- 115 files · ~95,392 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 665 nodes · 1437 edges · 33 communities (26 shown, 7 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 79 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- AAP Session & Config Protocols
- Tier B Probe & App UI Orchestration
- Audio Visualizer & Equalizer
- Battery State & Charging Engine
- Interaction Settings & Head Gestures State
- App Lifecycle, State & Auto-Pause
- Head Gesture Detector & Telecom
- Tier B L2CAP Reachability Probes
- Find My Chime Audio Synthesizer
- AirPods Generation Vectors & Badges
- Theme, Settings & Battery Popup
- AAP Packet Decoder & Events
- Android Glance Battery Widget
- Ear Detection & Media Deciders
- Material 3 Morphing Shapes
- AAP Packet Decoder Unit Tests
- Expressive AirPods Hero Trio
- ADRs & AAP Capture Fixtures
- Material 3 Design System Guides
- Roadmap, Agents & Architecture Context
- Capability Resolver Unit Tests
- Gradle Wrapper Scripts
- Testing Smoke Verification
- ADR Architecture Templates
- AirPodsState StateFlow Model
- Layered Architecture Spec
- AirPods Capabilities Spec
- Event-Driven Lifecycle Spec

## God Nodes (most connected - your core abstractions)
1. `AirPodsState` - 40 edges
2. `androidpodsSpatialSpec()` - 33 edges
3. `AirPodsTransport` - 30 edges
4. `AirPodsRepository` - 30 edges
5. `FakeAirPodsTransport` - 29 edges
6. `BatteryComponentState` - 28 edges
7. `EarDetectionState` - 27 edges
8. `AapSession` - 26 edges
9. `AapEvent` - 24 edges
10. `rememberAppHaptics()` - 24 edges

## Surprising Connections (you probably didn't know these)
- `Material 3 Expressive System Integration` --semantically_similar_to--> `MD3 Principles: Personal, Adaptive, Expressive`  [INFERRED] [semantically similar]
  PROJECT.md → .agents/skills/material-3/SKILL.md
- `ADR-0001: Hidden L2CAP Socket for Tier B Transport` --rationale_for--> `Dual Transport Strategy (Tier A & Tier B)`  [INFERRED]
  docs/adr/0001-tier-b-hidden-l2cap-socket.md → PROJECT.md
- `AirPods 4 AAP Session Start Capture Fixture` --conceptually_related_to--> `ADR-0001: Hidden L2CAP Socket for Tier B Transport`  [INFERRED]
  app/src/test/resources/fixtures/aap/session-start-capture.txt → docs/adr/0001-tier-b-hidden-l2cap-socket.md
- `LibrePods Protocol Adaptation Notice` --conceptually_related_to--> `ADR-0002: GPL-3.0-or-later Licensing`  [INFERRED]
  NOTICE.md → docs/adr/0002-gpl-3.0-licensing.md
- `AirPods 4 AAP Session Start Capture Fixture` --references--> `Androidpods Project Specification`  [EXTRACTED]
  app/src/test/resources/fixtures/aap/session-start-capture.txt → PROJECT.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Material Design 3 Token & Adaptive System** — _agents_skills_material_3_skill_skill, _agents_skills_material_3_references_color_system_color_system, _agents_skills_material_3_references_layout_and_responsive_layout_system, _agents_skills_material_3_references_typography_and_shape_visual_tokens [EXTRACTED 0.95]
- **AirPods Protocol & Transport Architecture** — project_transport_tiers, docs_adr_0001_tier_b_hidden_l2cap_socket_l2capsocket, app_src_test_resources_fixtures_aap_session_start_capture_session_capture, docs_adr_0001_tier_b_hidden_l2cap_socket_tier_probe_cache [EXTRACTED 0.95]
- **AirPods State & Capability Data Flow** — project_architecture_layers, project_capability_driven_ui, project_airpods_state_model [EXTRACTED 0.95]
- **Androidpods State, Lifecycle & Battery Architecture** — roadmap_architecture_reference, project_smart_battery_merging, project_acl_aware_transport_lifecycle, project_m3_expressive_battery_popup [EXTRACTED 0.95]

## Communities (33 total, 7 thin omitted)

### Community 0 - "AAP Session & Config Protocols"
Cohesion: 0.06
Nodes (17): AapSession, ByteArray, Flow, StemPressAndHoldAction, DISABLED, NOISE_CONTROL, VOICE_ASSISTANT, AirPodsRepository (+9 more)

### Community 1 - "Tier B Probe & App UI Orchestration"
Cohesion: 0.06
Nodes (41): ForceTierBUnsupportedTest, Bundle, ComponentActivity, MainActivity, AirPodsAssociationManager, CompanionDeviceManager, hasCompanionAssociation(), isBluetoothDeviceConnected() (+33 more)

### Community 2 - "Audio Visualizer & Equalizer"
Cohesion: 0.07
Nodes (45): AudioWaveformVisualizer(), Color, Dp, Modifier, ExpressiveScreenHeader(), Color, Dp, ImageVector (+37 more)

### Community 3 - "Battery State & Charging Engine"
Cohesion: 0.05
Nodes (37): BatteryChargeStatus, CHARGING, DISCONNECTED, NOT_CHARGING, OPTIMIZED_CHARGING, BatteryComponentState, BatteryState, AirPodsCapabilities (+29 more)

### Community 4 - "Interaction Settings & Head Gestures State"
Cohesion: 0.05
Nodes (33): HeadGesturesState, DISABLED, ENABLED, HoldDuration, DEFAULT, LONG, SHORT, PressSpeed (+25 more)

### Community 5 - "App Lifecycle, State & Auto-Pause"
Cohesion: 0.10
Nodes (32): AndroidpodsApp, AirPodsState, dispatchPauseKeyEvent(), dispatchPlayKeyEvent(), Context, CoroutineScope, Flow, observeAutoPause() (+24 more)

### Community 6 - "Head Gesture Detector & Telecom"
Cohesion: 0.10
Nodes (16): HeadGesture, NOD, NONE, SHAKE, HeadGestureDetector, MotionSample, CallGestureManager, TelephonyCallback (+8 more)

### Community 7 - "Tier B L2CAP Reachability Probes"
Cohesion: 0.11
Nodes (15): L2capTierBProbeTest, AapTransport, ByteArray, CoroutineScope, Flow, AirPodsPresenceService, BluetoothDevice, AirPodsRepositoryProvider (+7 more)

### Community 8 - "Find My Chime Audio Synthesizer"
Cohesion: 0.11
Nodes (15): ChimePlayer, ChimeTarget, BOTH, CASE, LEFT, RIGHT, StateFlow, ProtocolLogging (+7 more)

### Community 9 - "AirPods Generation Vectors & Badges"
Cohesion: 0.15
Nodes (26): AirPodsGeneration, GEN_1_2, GEN_3, GEN_4, MAX, PRO, AirPodsGenerationMorphBadge(), drawAirPodsGen1Silhouette() (+18 more)

### Community 10 - "Theme, Settings & Battery Popup"
Cohesion: 0.15
Nodes (15): AppSettings, AndroidpodsTheme(), isReducedMotion(), BatteryPopupActivity, Bundle, ComponentActivity, Context, Modifier (+7 more)

### Community 11 - "AAP Packet Decoder & Events"
Cohesion: 0.22
Nodes (13): AapEvent, AapPacketDecoder, AirPodsInformation, Battery, DeviceInfo, EarDetection, HeadGesturesConfig, HeadMotion (+5 more)

### Community 12 - "Android Glance Battery Widget"
Cohesion: 0.17
Nodes (16): BatteryCard(), BatteryContent(), BatteryWidget, BatteryWidgetContent(), Context, GlanceAppWidget, NoDataContent(), Battery (+8 more)

### Community 13 - "Ear Detection & Media Deciders"
Cohesion: 0.20
Nodes (3): EarDetectionState, AutoPauseDecider, AutoPauseDeciderTest

### Community 14 - "Material 3 Morphing Shapes"
Cohesion: 0.21
Nodes (12): calculateBoundsRect(), drawMorphShape(), Color, Dp, Modifier, MorphingShapeHero(), toComposePath(), MorphingShapeTest (+4 more)

### Community 15 - "AAP Packet Decoder Unit Tests"
Cohesion: 0.20
Nodes (4): AapPacketDecoderTest, ByteArray, loadFixturePacket(), object@L9

### Community 16 - "Expressive AirPods Hero Trio"
Cohesion: 0.53
Nodes (11): AirPodsExpressiveTrio(), AnimatedAirPodExpressiveItem(), AnimatedAirPodsCase(), AnimatedLeftAirPod(), AnimatedRightAirPod(), drawExpressiveM3AirPod(), drawExpressiveM3Case(), Color (+3 more)

### Community 17 - "ADRs & AAP Capture Fixtures"
Cohesion: 0.25
Nodes (11): MD3 Principles: Personal, Adaptive, Expressive, AirPods 4 AAP Session Start Capture Fixture, ADR-0001: Hidden L2CAP Socket for Tier B Transport, DataStore Tier Probe Cache Strategy, ADR-0002: GPL-3.0-or-later Licensing, Architecture Decision Records Index, LibrePods Protocol Adaptation Notice, Third-Party Notices Index (+3 more)

### Community 18 - "Material 3 Design System Guides"
Cohesion: 0.33
Nodes (7): MD3 Color System & Tonal Palettes, MD3 Component Catalog, MD3 Responsive Layout & Window Size Classes, MD3 Adaptive Navigation Patterns, MD3 Theming & Dynamic Color Guide, MD3 Typography, Shape, Elevation & Motion, Material Design 3 Skill Implementation Guide

### Community 19 - "Roadmap, Agents & Architecture Context"
Cohesion: 0.29
Nodes (7): AI Agents Orientation & Rules, Claude Code Standards & Rules, ACL-Link Aware Bluetooth Transport Lifecycle, Material 3 Expressive Battery Popup Architecture, Smart Battery Merging & Cache Retention, Androidpods Overview & Feature Summary, Roadmap & Architecture Reference

### Community 21 - "Gradle Wrapper Scripts"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **71 isolated node(s):** `DEFAULT`, `SLOW`, `SLOWEST`, `DEFAULT`, `SHORT` (+66 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AirPodsTransport` connect `Battery State & Charging Engine` to `AAP Session & Config Protocols`, `Tier B Probe & App UI Orchestration`, `Audio Visualizer & Equalizer`, `Interaction Settings & Head Gestures State`, `App Lifecycle, State & Auto-Pause`, `Head Gesture Detector & Telecom`, `Tier B L2CAP Reachability Probes`, `Find My Chime Audio Synthesizer`?**
  _High betweenness centrality (0.135) - this node is a cross-community bridge._
- **Why does `AirPodsState` connect `App Lifecycle, State & Auto-Pause` to `AAP Session & Config Protocols`, `Audio Visualizer & Equalizer`, `Battery State & Charging Engine`, `Interaction Settings & Head Gestures State`, `Head Gesture Detector & Telecom`, `Tier B L2CAP Reachability Probes`, `Find My Chime Audio Synthesizer`, `Android Glance Battery Widget`?**
  _High betweenness centrality (0.129) - this node is a cross-community bridge._
- **Why does `AirPodsRepositoryProvider` connect `Tier B L2CAP Reachability Probes` to `AAP Session & Config Protocols`, `Tier B Probe & App UI Orchestration`, `Audio Visualizer & Equalizer`, `Battery State & Charging Engine`, `Interaction Settings & Head Gestures State`, `App Lifecycle, State & Auto-Pause`, `Head Gesture Detector & Telecom`, `Theme, Settings & Battery Popup`, `AAP Packet Decoder & Events`, `Android Glance Battery Widget`?**
  _High betweenness centrality (0.098) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `androidpodsSpatialSpec()` (e.g. with `AirPodsGenerationMorphBadge()` and `AnimatedAirPodExpressiveItem()`) actually correct?**
  _`androidpodsSpatialSpec()` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Are the 11 inferred relationships involving `AirPodsRepository` (e.g. with `.`a single failed probe does not yet skip the next connect attempt`()` and `.`battery event updates battery state`()`) actually correct?**
  _`AirPodsRepository` has 11 INFERRED edges - model-reasoned connections that need verification._
- **What connects `DEFAULT`, `SLOW`, `SLOWEST` to the rest of the system?**
  _71 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `AAP Session & Config Protocols` be split into smaller, more focused modules?**
  _Cohesion score 0.057971014492753624 - nodes in this community are weakly interconnected._