# Graph Report - Androidpods  (2026-08-14)

## Corpus Check
- 18 files · ~38,786 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 368 nodes · 576 edges · 36 communities (23 shown, 13 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 51 edges (avg confidence: 0.82)
- Token cost: 45,000 input · 11,683 output

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
- ROADMAP: MAC-Case Crash Bug
- Protocol Logging
- Smoke Test
- ROADMAP: PAGE_TIMEOUT Gap
- ROADMAP: Notification Verification
- ROADMAP: Tier B Text Duplication Bug
- ROADMAP: Notification Icon Placeholder
- BluetoothDevice Type Reference
- Compose Modifier Reference
- ADR Template
- Event-Driven Lifecycle Principle
- IDE-Local Config Artifact

## God Nodes (most connected - your core abstractions)
1. `FakeAirPodsTransport` - 22 edges
2. `AirPodsRepository` - 19 edges
3. `FakeTierProbeCache` - 15 edges
4. `EarDetectionState` - 15 edges
5. `AirPodsTransport` - 14 edges
6. `AirPodsRepositoryTest` - 12 edges
7. `AapTransport` - 12 edges
8. `AapEvent` - 11 edges
9. `HomeScreenContent()` - 9 edges
10. `observeBatteryNotifications()` - 9 edges

## Surprising Connections (you probably didn't know these)
- `Material 3 Expressive System Integration` --semantically_similar_to--> `MD3 Principles: Personal, Adaptive, Expressive`  [INFERRED] [semantically similar]
  PROJECT.md → .agents/skills/material-3/SKILL.md
- `AirPods 4 AAP Session Start Capture Fixture` --conceptually_related_to--> `ADR-0001: Hidden L2CAP Socket for Tier B Transport`  [INFERRED]
  app/src/test/resources/fixtures/aap/session-start-capture.txt → docs/adr/0001-tier-b-hidden-l2cap-socket.md
- `ADR-0001: Hidden L2CAP Socket for Tier B Transport` --rationale_for--> `Dual Transport Strategy (Tier A & Tier B)`  [INFERRED]
  docs/adr/0001-tier-b-hidden-l2cap-socket.md → PROJECT.md
- `LibrePods Protocol Adaptation Notice` --conceptually_related_to--> `ADR-0002: GPL-3.0-or-later Licensing`  [INFERRED]
  NOTICE.md → docs/adr/0002-gpl-3.0-licensing.md
- `AirPods 4 AAP Session Start Capture Fixture` --references--> `Androidpods Project Specification`  [EXTRACTED]
  app/src/test/resources/fixtures/aap/session-start-capture.txt → PROJECT.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bugs found and fixed this session** — roadmap_mac_address_case_crash_bug, roadmap_zero_delay_handshake_bug, roadmap_tier_b_unavailable_duplicated_text_bug [EXTRACTED 1.00]
- **Real-hardware confirmation pass (Pixel 9 Pro XL, AirPods 4)** — roadmap_widget_live_session_update, roadmap_widget_cold_process_revival, roadmap_notifications_dumpsys_confirmation, roadmap_aaptransport_connect, roadmap_airpods4_model_id_a3053 [INFERRED 0.85]
- **Deliberately not unit-tested framework types (verified on hardware instead)** — roadmap_datastoretierprobecache, roadmap_aaptransport, roadmap_batterywidget, roadmap_batterywidgetreceiver, roadmap_observewidgetupdates, roadmap_batterynotification, roadmap_observebatterynotifications [EXTRACTED 1.00]
- **AirPods Protocol & Transport Architecture** — project_transport_tiers, docs_adr_0001_tier_b_hidden_l2cap_socket_l2capsocket, app_src_test_resources_fixtures_aap_session_start_capture_session_capture, docs_adr_0001_tier_b_hidden_l2cap_socket_tier_probe_cache [EXTRACTED 0.95]
- **AirPods State & Capability Data Flow** — project_architecture_layers, project_capability_driven_ui, project_airpods_state_model [EXTRACTED 0.95]
- **Material Design 3 Token & Adaptive System** — _agents_skills_material_3_skill_skill, _agents_skills_material_3_references_color_system_color_system, _agents_skills_material_3_references_layout_and_responsive_layout_system, _agents_skills_material_3_references_typography_and_shape_visual_tokens [EXTRACTED 0.95]

## Communities (36 total, 13 thin omitted)

### Community 0 - "AAP Session & Repository Core"
Cohesion: 0.10
Nodes (15): AapEvent, BatteryComponentState, AapSession, Flow, AirPodsRepository, StateFlow, AapSessionTest, FakeAirPodsTransport (+7 more)

### Community 1 - "ROADMAP: Hardware Pass Findings"
Cohesion: 0.06
Nodes (38): AapSession.start(), AapTransport, docs/adr/0001-tier-b-hidden-l2cap-socket.md (Tier B transport ADR), Confirmed AirPods 4 model ID A3053 (via DeviceInfo/INFORMATION packet), AirPods 4 (test device), AirPodsPresenceService.onDevicePresenceEvent, AirPodsPresenceService.scope = CoroutineScope(Dispatchers.IO), AirPodsRepository (+30 more)

### Community 2 - "Device Pairing & Association"
Cohesion: 0.12
Nodes (20): MainActivity, AirPodsAssociationManager, CompanionDeviceManager, hasCompanionAssociation(), Context, observePresenceQuietly(), resumeObservingAssociatedDevices(), hasBluetoothPermissions() (+12 more)

### Community 3 - "AirPodsTransport Interface"
Cohesion: 0.09
Nodes (13): AirPodsTransport, Connected, Connecting, ConnectionState, Disconnected, Failed, ByteArray, Flow (+5 more)

### Community 4 - "Tier B L2CAP Transport Impl"
Cohesion: 0.15
Nodes (10): L2capTierBProbeTest, AapTransport, ByteArray, CoroutineScope, Flow, AirPodsRepositoryProvider, BluetoothDevice, Context (+2 more)

### Community 5 - "AAP Packet Decoding"
Cohesion: 0.17
Nodes (14): AapEvent, AapPacketDecoder, AirPodsInformation, Battery, BatteryChargeStatus, CHARGING, DISCONNECTED, NOT_CHARGING (+6 more)

### Community 6 - "App Startup & Battery Notification Wiring"
Cohesion: 0.18
Nodes (16): AndroidpodsApp, ensureBatteryNotificationChannel(), Context, updateBatteryNotification(), AirPodsState, Context, CoroutineScope, Flow (+8 more)

### Community 7 - "Battery Home-Screen Widget"
Cohesion: 0.17
Nodes (16): BatteryCell(), BatteryContent(), BatteryWidget, BatteryWidgetContent(), BatteryComponentState, Context, GlanceAppWidget, NoDataContent() (+8 more)

### Community 8 - "Auto-Pause & AirPodsState"
Cohesion: 0.19
Nodes (9): EarDetectionState, AirPodsState, dispatchPauseKeyEvent(), Context, CoroutineScope, Flow, observeAutoPause(), AutoPauseDecider (+1 more)

### Community 9 - "Home Screen Compose UI"
Cohesion: 0.26
Nodes (16): BatteryColumn(), ConnectedContent(), ConnectingContent(), DisconnectedContent(), EarDetectionIndicator(), EarDetectionRow(), FailedContent(), HomeScreen() (+8 more)

### Community 10 - "Docs, ADRs & Design Skill References"
Cohesion: 0.17
Nodes (16): MD3 Principles: Personal, Adaptive, Expressive, AirPods 4 AAP Session Start Capture Fixture, Claude Code Guidance Document, ADR-0001: Hidden L2CAP Socket for Tier B Transport, DataStore Tier Probe Cache Strategy, ADR-0002: GPL-3.0-or-later Licensing, Architecture Decision Records Index, LibrePods Protocol Adaptation Notice (+8 more)

### Community 11 - "AapPacketDecoder Tests"
Cohesion: 0.25
Nodes (4): AapPacketDecoderTest, ByteArray, loadFixturePacket(), object@L12

### Community 12 - "Bluetooth Availability Monitoring"
Cohesion: 0.33
Nodes (8): BluetoothAvailability, BroadcastReceiver, Disabled, Enabled, Context, Flow, Unsupported, Intent

### Community 13 - "Capability Resolver & Noise Modes"
Cohesion: 0.25
Nodes (7): AirPodsCapabilities, CapabilityResolver, NoiseControlMode, ADAPTIVE, NOISE_CANCELLATION, OFF, TRANSPARENCY

### Community 15 - "Material 3 Design References"
Cohesion: 0.33
Nodes (7): MD3 Color System & Tonal Palettes, MD3 Component Catalog, MD3 Responsive Layout & Window Size Classes, MD3 Adaptive Navigation Patterns, MD3 Theming & Dynamic Color Guide, MD3 Typography, Shape, Elevation & Motion, Material Design 3 Skill Implementation Guide

### Community 16 - "AirPods Presence Service"
Cohesion: 0.48
Nodes (4): AirPodsPresenceService, BluetoothDevice, CompanionDeviceService, DevicePresenceEvent

### Community 19 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 20 - "ROADMAP: MAC-Case Crash Bug"
Cohesion: 0.50
Nodes (4): AirPodsPresenceService.resolveBluetoothDevice, BluetoothAdapter.getRemoteDevice(String), MAC-address case crash bug (lowercase toString vs uppercase-required getRemoteDevice), android.net.MacAddress

### Community 23 - "ROADMAP: PAGE_TIMEOUT Gap"
Cohesion: 0.67
Nodes (3): AapTransport.connect(), PAGE_TIMEOUT retry path (undocumented/unexercised), Pixel 9 Pro XL (test hardware, Android 17/API 37)

## Knowledge Gaps
- **55 isolated node(s):** `object@L21`, `Connected`, `Connecting`, `Disconnected`, `Failed` (+50 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **13 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AirPodsTransport` connect `AirPodsTransport Interface` to `AAP Session & Repository Core`, `Auto-Pause & AirPodsState`, `Tier B L2CAP Transport Impl`?**
  _High betweenness centrality (0.141) - this node is a cross-community bridge._
- **Why does `HomeScreen()` connect `Home Screen Compose UI` to `Device Pairing & Association`, `App Startup & Battery Notification Wiring`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **Why does `AapTransport` connect `Tier B L2CAP Transport Impl` to `AirPodsTransport Interface`?**
  _High betweenness centrality (0.087) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `AirPodsRepository` (e.g. with `.`a single failed probe does not yet skip the next connect attempt`()` and `.`battery event updates battery state`()`) actually correct?**
  _`AirPodsRepository` has 10 INFERRED edges - model-reasoned connections that need verification._
- **Are the 11 inferred relationships involving `FakeTierProbeCache` (e.g. with `.`a single failed probe does not yet skip the next connect attempt`()` and `.`a successful probe resets a prior single failure`()`) actually correct?**
  _`FakeTierProbeCache` has 11 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `EarDetectionState` (e.g. with `.`decodes ear detection packet from real capture`()` and `.`inbound transport packets are decoded into AAP events`()`) actually correct?**
  _`EarDetectionState` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `object@L21`, `Connected`, `Connecting` to the rest of the system?**
  _55 weakly-connected nodes found - possible documentation gaps or missing edges._