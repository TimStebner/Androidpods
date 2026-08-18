# Androidpods — Roadmap & Architecture Reference

> **Zweck:** Zentrale Dokumentation des aktuellen Entwicklungsstands, der implementierten Features, der Architektur und der Dateistruktur.  
> **Zielgruppe:** Entwickler und AI-Agenten zur schnellen Orientierung, für zielgerichtete Bugfixes und Weiterentwicklungen.  
> **Autoritative Spezifikation:** [`PROJECT.md`](PROJECT.md) (Architektur- und Plattformvorgaben nach §31).

---

## 1. Projektstatus & Hardware-Verifikation

Alle geplanten **Milestones 0 bis 7**, das **Material 3 Expressive UI/UX-Redesign**, das **Battery Pop-up** sowie die **Performance- & Batterielaufzeit-Optimierung** sind implementiert. Die Release-Härtung umfasst minifizierte Builds, Privacy-/Backup-Regeln, synthetische Test-Fixtures, ehrliches Capability-Gating, Zero-Allocation Render-Pfade, Macrobenchmarks und versionierte Baseline Profiles. Alle offenen Release-Gates aus [`SECURITY_PERFORMANCE_REVIEW.md`](SECURITY_PERFORMANCE_REVIEW.md) wurden behoben; der Release Candidate (`v0.1.0`) ist freigegeben für Internal/Closed Testing auf Google Play.
- **Test-Hardware:** Google Pixel 9 Pro XL (Android 17 / API 37)
- **Kopfhörer:** Apple AirPods 4 (Modell `A3050` / `A3053`, H2-Chip)

---

## 2. Feature- & Architektur-Map (Where to find what)

Der Produktionscode bleibt vollständig im einzelnen `app`-Gradle-Modul und ist unter dem Package `dev.androidpods` streng nach Schichten gegliedert ([`PROJECT.md`](PROJECT.md) §11). Das separate `benchmark`-Modul enthält ausschließlich Macrobenchmark-/Baseline-Profile-Instrumentierung:

```
dev.androidpods
├── app/                  # Application-Klasse, MainActivity, DI/Providers
├── core/
│   ├── airpods/          # AAP-Protokoll, Session-Handshake, Codecs, Capabilities
│   ├── audio/            # Akustische Signale, Chime-Synthese, Audio-Routing
│   ├── bluetooth/        # L2CAP-Transport, CDM Presence Service, Permissions, ACL-Link-Checks
│   ├── data/             # AirPodsRepository, AirPodsState, DataStore-Repositories, Battery-Merging
│   ├── designsystem/     # Material 3 Expressive Theming, Hero-Illustrationen, Zero-Allocation Motion & Paths
│   ├── gestures/         # 6-Achsen-Kopfgestenerkennung (Nicken & Schütteln)
│   ├── media/            # Auto-Pause & Auto-Resume bei Trageerkennung (Flow-gefiltert)
│   └── telecom/          # Anrufsteuerung über Kopfgesten (TelecomManager / KeyEvents)
└── feature/
    ├── controls/         # Steuerungs-Tab (Spatial, Gesten, Chimes, Timing, BringIntoView Navigation)
    ├── findmy/           # Suchton-Assistent (Find My Audio Chime)
    ├── home/             # Hauptansicht (Brand Hero, dynamische Generation-Icons, Akkustand, Quick-Cards)
    ├── navigation/       # Floating Navigation Pill mit Federphysik & jitterfreien Transitions
    ├── notifications/    # Persistente Akku- & Verbindungsbenachrichtigungen inkl. Content-Intents
    ├── onboarding/       # Ersteinrichtung, CDM-Pairing & Morphing Shape Hero
    ├── popup/            # Material 3 Expressive AirPods Battery Pop-up & Dialog Activity
    ├── settings/         # App-Einstellungen (Theme, Dynamic Color, Pop-up Toggle, Logging)
    ├── spatial/          # 3D-Kopforientierungs-Visualizer & Lifecycle-gebundener 50Hz IMU-Stream
    ├── tiles/            # Android Quick Settings Tile mit distinctUntilChanged IPC-Filterung
    └── widgets/          # Android Glance Home-Screen Battery Widget & native Live-Vorschau
```

---

## 3. Detaillierte Komponenten-Übersicht

### 3.1 Bluetooth- & Transport-Schicht (`dev.androidpods.core.bluetooth`)
- **[`AapTransport.kt`](app/src/main/kotlin/dev/androidpods/core/bluetooth/AapTransport.kt):**  
  Implementiert die rootless Classic BR/EDR L2CAP-Socket-Verbindung über `HiddenApiBypass` (`createInsecureL2capSocket` auf PSM `0x1001`, siehe [ADR-0001](docs/adr/0001-tier-b-hidden-l2cap-socket.md)).
  - **ACL-Link-Check (`isDeviceAclConnected`):** Prüft vor Socket-Erstellung, ob das Gerät am Bluetooth-Link aktiv ist. Verhindert 6-Sekunden-Timeouts und falsche `ConnectionState.Failed`-Fehler, wenn die AirPods im geschlossenen Ladecase liegen.
- **[`AirPodsPresenceService.kt`](app/src/main/kotlin/dev/androidpods/core/bluetooth/AirPodsPresenceService.kt):**  
  Android `CompanionDeviceService`. Reagiert auf System-Events (`EVENT_BT_CONNECTED`, `EVENT_BT_DISCONNECTED`) für stromsparendes Tracking ohne permanente WakeLocks oder BLE-Scans.
- **[`AirPodsAssociationManager.kt`](app/src/main/kotlin/dev/androidpods/core/bluetooth/AirPodsAssociationManager.kt):**  
  Verwaltet CDM-Assoziationen, prüft Bluetooth-Verbindungsstatus (`isBluetoothDeviceConnected`) und steuert den automatischen Reconnect bei App-Start und `onResume()`. Bereinigt um redundante Cache-Löschungen zur Vermeidung unnötiger Disk-I/O.
- **[`RequiredPermissions.kt`](app/src/main/kotlin/dev/androidpods/core/bluetooth/RequiredPermissions.kt):**  
  Zentrale Definition und Prüfung von Bluetooth-, Benachrichtigungs- und Telefonie-Berechtigungen.

### 3.2 AAP-Protokoll & Codec (`dev.androidpods.core.airpods`)
- **[`AapSession.kt`](app/src/main/kotlin/dev/androidpods/core/airpods/AapSession.kt):**  
  Zustandsautomat für den Apple Accessory Protocol (AAP) Handshake (`delay(200)` Pacing). Erzeugt und sendet Konfigurations-Pakete:
  - Druckgeschwindigkeit (Config `0x25`)
  - Haltedauer (Config `0x26`)
  - Kopfgesten-Toggle (Config `0x3E`)
  - IMU-Sensorstrom-Aktivierung für H2 (`devmotion` Service `0x10` und `HostLibHID` Service `0x12` über Opcode `0x17`)
- **[`AapPacketDecoder.kt`](app/src/main/kotlin/dev/androidpods/core/airpods/AapPacketDecoder.kt):**  
  Dekodiert eingehende AAP-Pakete:
  - Opcode `0x04`: Akkustand (Left `0x02`, Right `0x04`, Case) & Ladezustand
  - Opcode `0x06`: Trageerkennung (Byte 6 Right, Byte 7 Left)
  - Opcode `0x17`: 81-Byte 50Hz IMU-Sensor-Reports ($o_1, o_2, o_3$ an Offset 43, 45, 47 für Pitch, Yaw, Roll)
- **[`CapabilityResolver.kt`](app/src/main/kotlin/dev/androidpods/core/airpods/CapabilityResolver.kt):**  
  Ermittelt anhand der Apple-Modellnummer (z. B. `A3050`/`A3053` für AirPods 4) die genauen Fähigkeiten (`AirPodsCapabilities`) nach dem Prinzip: *„UI renders capabilities. It does not infer them.“* (§9).

### 3.3 State Management & Datenfluss (`dev.androidpods.core.data`)
- **[`AirPodsState.kt`](app/src/main/kotlin/dev/androidpods/core/data/AirPodsState.kt):**  
  Unveränderliches Zustandsmodell der gesamten App (`connection`, `battery`, `earDetection`, `headOrientation`, `capabilities`, `motionStreamActive`).
- **[`AirPodsRepository.kt`](app/src/main/kotlin/dev/androidpods/core/data/AirPodsRepository.kt):**  
  Verbindet Transport, Session und Reducer (`reduce`).
  - **Smart Battery Merging (`mergeBatteryEvent`):** Behält den letzten bekannten Akkustand des Ladecases (z. B. 85%) mit Status `DISCONNECTED` bei, wenn das Case geschlossen wird und das AAP-Paket `0% / DISCONNECTED` sendet.
  - **Probe-Cache-Schutz:** Normales Trennen beim Einlegen ins Case wird nicht mehr fälschlicherweise als Platform-Fehler im `TierProbeCache` registriert.
- **[`AirPodsRepositoryProvider.kt`](app/src/main/kotlin/dev/androidpods/core/data/AirPodsRepositoryProvider.kt):**  
  Prozessweiter Singleton-Provider für `AirPodsState` und den gepufferten `events`-Flow (`extraBufferCapacity = 64`).
- **[`AppSettingsRepository.kt`](app/src/main/kotlin/dev/androidpods/core/data/AppSettingsRepository.kt):**  
  DataStore Preferences für Theme, Dynamic Color, Auto-Pause/Resume, Gesten, Pop-up und Benachrichtigungen.
- **[`DataStoreTierProbeCache.kt`](app/src/main/kotlin/dev/androidpods/core/data/DataStoreTierProbeCache.kt):**  
  Persistenter Cache für erfolgreiche Tier-B-Verbindungen mit Self-Healing-Funktion.

### 3.4 Interaktion, Audio & Gesten
- **[`AutoPauseManager.kt`](app/src/main/kotlin/dev/androidpods/core/media/AutoPauseManager.kt) & [`AutoPause.kt`](app/src/main/kotlin/dev/androidpods/core/media/AutoPause.kt):**  
  Pausiert und setzt Medien-Wiedergabe (`AudioManager.dispatchMediaKeyEvent`) anhand der Trageerkennung fort. Stream wird via `distinctUntilChangedBy` gefiltert, um redundante Auswertungen bei Telemetrie-Änderungen zu verhindern.
- **[`HeadGestureDetector.kt`](app/src/main/kotlin/dev/androidpods/core/gestures/HeadGestureDetector.kt):**  
  Erkennt Nicken (**NOD**) und Kopfschütteln (**SHAKE**) über bidirektionale Oszillationsprüfung auf 50Hz-IMU-Daten. Verhindert Fehl-Auslösungen bei statischer Kopfneigung.
- **[`CallGestureManager.kt`](app/src/main/kotlin/dev/androidpods/core/telecom/CallGestureManager.kt):**  
  Lauscht auf eingehende Anrufe (`TelephonyCallback`), startet den Motion-Stream und steuert die Annahme (Nicken) bzw. Ablehnung (Schütteln) über `TelecomManager` und `KEYCODE_HEADSETHOOK`.
- **[`ChimePlayer.kt`](app/src/main/kotlin/dev/androidpods/core/audio/ChimePlayer.kt):**  
  Synthetisiert das Apple Find-My Suchton-Signal (2.5kHz–5.5kHz Frequenz-Sweeps in 16-Bit 44.1kHz Stereo PCM) mit Kanaltrennung (Links, Beide, Rechts) und Bluetooth-Audio-Routing via `preferredDevice`.

### 3.5 Designsystem & Performance-Rendering (`dev.androidpods.core.designsystem`)
- **[`StatusBarScrim.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/StatusBarScrim.kt):**  
  Material 3 Expressive Status Bar Scrim mit sanftem vertikalem Farbverlauf über `WindowInsets.statusBars` + 16dp Bleed. Schützt die System-Statusleiste bei Edge-to-Edge Scroll-Content.
- **[`DeviceIllustration.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/DeviceIllustration.kt):**  
  Dynamische Centerpiece-Vektor-Illustrationen (`AirPodsIllustration`) mit wiederverwendbaren `Path()`-Instanzen (`rewind()`) und RenderNode-gebundenem `graphicsLayer`-Pulsieren ohne Composable-Rekomposition.
- **[`Color.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/Color.kt) & [`Theme.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/Theme.kt):**  
  - **Obsidian Dark:** `#0A0D14` Canvas/Surface, `#161B28` Container, `#1E2536` High Container, `#F8FAFC` reines Weiß für Typografie (WCAG AAA).
  - **High-Contrast Light:** `#F1F5F9` Slate-Canvas, `#FFFFFF` reine weiße Karten mit plastischer Elevation, `#0F172A` Deep Navy Text.
- **[`ExpressiveScreenHeader.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/ExpressiveScreenHeader.kt):**  
  Einheitlicher Header für alle Tabs mit bolde Typografie (`headlineLarge`, `FontWeight.Black`) ohne 1-Frame-Verzögerung für flüssige Screen-Transitions.
- **[`AirPodsGenerationBadge.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/AirPodsGenerationBadge.kt):**  
  Interaktives Badge mit Vektorsilhouetten aller 5 AirPods-Generationen.
- **[`AirPodsHeroIllustration.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/AirPodsHeroIllustration.kt):**  
  Material 3 Expressive Vektor-Illustrationen für Pop-up und Detailansichten: **`AnimatedLeftAirPod`**, **`AnimatedRightAirPod`** und **`AnimatedAirPodsCase`** mit 0 Rekompositionen während der 3D-Schwebe-Animation durch Deferred Reads in `graphicsLayer`.
- **[`AudioWaveformVisualizer.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/AudioWaveformVisualizer.kt):**  
  Zero-Allocation-Wellenform-Visualizer mit `remember(barCount)` Hüllkurven-Cache.
- **[`MorphingShape.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/MorphingShape.kt):**  
  Allokationsfreie AndroidX-Polygon-Morphing-Komponente mit wiederverwendbaren FloatArray-Bounds.
- **[`SpatialMotionSpec.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/SpatialMotionSpec.kt) & [`AppHaptics.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/AppHaptics.kt):**  
  Zentrale Definition für `androidpodsSpatialSpec()` Federphysik und taktiles haptisches Feedback.

### 3.6 UI-Schicht & Android-Integration (`dev.androidpods.feature.*`)
- **[`AirPodsBatteryPopup.kt`](app/src/main/kotlin/dev/androidpods/feature/popup/AirPodsBatteryPopup.kt) & [`BatteryPopupActivity.kt`](app/src/main/kotlin/dev/androidpods/feature/popup/BatteryPopupActivity.kt):**  
  Material 3 Expressive **AirPods Battery Pop-up** Bottom Card mit Hero-Trio (`AirPodsExpressiveTrio`), Live-Akkustands-Pillars, Lade-Blitzen (`⚡`), Trageerkennungs-Indikatoren und 100% transparentem Hintergrund ohne künstliche Abdunklung.
- **[`HomeScreen.kt`](app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt):**  
  Brand-Hero mit modellabhängiger Grafik, Live-Waveform-Equalizer, `UnifiedBatteryPillar`-Trio und `ConnectionBadge` (RenderNode Alpha).
- **[`ControlsScreen.kt`](app/src/main/kotlin/dev/androidpods/feature/controls/ControlsScreen.kt):**  
  Expressiver Steuerungs-Tab für Spatial Audio, Kopfgesten, Find My Suchton und Druck-/Haltezeiten. Bereinigt um synchrone Binder-IPCs in der Komposition.
- **[`SpatialMotionVisualizer.kt`](app/src/main/kotlin/dev/androidpods/feature/spatial/SpatialMotionVisualizer.kt):**  
  3D-Kopforientierungs-Visualizer mit automatischer `Lifecycle`-Abschaltung des 50Hz-Sensorstreams bei `ON_STOP` im Hintergrund.
- **[`AirPodsTileService.kt`](app/src/main/kotlin/dev/androidpods/feature/tiles/AirPodsTileService.kt):**  
  Quick Settings Tile mit `distinctUntilChangedBy`-Filterung gegen IPC-Überlastung bei IMU-Streams.
- **[`AppNavigation.kt`](app/src/main/kotlin/dev/androidpods/feature/navigation/AppNavigation.kt):**  
  Schwebende **Floating Navigation Pill** mit optimierter Layout-Messung ohne verschachtelte `animateContentSize`.
- **[`Notifications`](app/src/main/kotlin/dev/androidpods/feature/notifications/):**  
  Lautlose Akku-Benachrichtigung und Heads-Up-Verbindungsbanner mit `PendingIntent` zum direkten Zurückkehren in die App.
- **[`BatteryWidget.kt`](app/src/main/kotlin/dev/androidpods/feature/widgets/BatteryWidget.kt) & [`WidgetsScreen.kt`](app/src/main/kotlin/dev/androidpods/feature/widgets/WidgetsScreen.kt):**  
  Android Glance Home-Screen Widget und In-App-Live-Vorschau.

---

## 4. Wichtige Gating- & Entwicklungsregeln für Bugfixes

1. **Single Source of Truth ([`PROJECT.md`](PROJECT.md) §10):**  
   Alle UI-Elemente, Widgets, Tiles und Benachrichtigungen beobachten ausschließlich `AirPodsRepositoryProvider.state`. Niemals sekundäre StateFlows oder parallele Zustandsautomaten anlegen.
2. **Capability-Gated UI (§9):**  
   Nicht unterstützte Features müssen ausgeblendet oder ehrlich erklärt werden.
3. **Pacing im AAP-Handshake:**  
   Das `delay(200)` zwischen Paketen in `AapSession.start()` ist hardware-bedingt notwendig. Nicht entfernen.
4. **Performance & Zero-Allocation Rule:**  
   Animationen müssen kontinuierliche State-Reads in `graphicsLayer { ... }`-Lambdas kapseln. Zeichen-Funktionen (`Canvas`, `DrawScope`) dürfen keine `Path`-, Array- oder `Rect`-Objekte pro Frame allokieren.
5. **Verifikations-Befehle:**
   ```bash
   ./gradlew :app:assembleDebug          # Debug-APK bauen
   ./gradlew :app:testDebugUnitTest      # JVM Unit-Tests (84 Tests)
   ./gradlew :app:lintDebug              # Android Lint (0 Fehler)
   ./gradlew :app:bundleRelease          # Optimiertes, noch unsigniertes Release-AAB
   ./gradlew :benchmark:connectedBenchmarkAndroidTest
   ```

---

## 5. Zukünftige Erweiterungen (sobald weitere Hardware vorliegt)

- **Active Noise Cancellation (ANC) / Transparency Writes (M3):**  
  Gated für AirPods Pro 1/2 und AirPods 4 mit ANC. Paket-Hex-Definitionen sind im Code dokumentiert, Schreibbefehle erfordern physische Hardware-Validierung vor Freigabe.
- **Case Speaker Chimes (M7):**  
  Aktivierung des integrierten Case-Lautsprechers auf AirPods 4 mit ANC und AirPods Pro 2.
