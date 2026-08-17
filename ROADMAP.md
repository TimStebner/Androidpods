# Androidpods — Roadmap & Architecture Reference

> **Zweck:** Zentrale Dokumentation des aktuellen Entwicklungsstands, der implementierten Features, der Architektur und der Dateistruktur.  
> **Zielgruppe:** Entwickler und AI-Agenten zur schnellen Orientierung, für zielgerichtete Bugfixes und Weiterentwicklungen.  
> **Autoritative Spezifikation:** [`PROJECT.md`](PROJECT.md) (Architektur- und Plattformvorgaben nach §31).

---

## 1. Projektstatus & Hardware-Verifikation

Alle geplanten **Milestones 0 bis 7** sowie das **Material 3 Expressive UI/UX-Redesign** sind vollständig implementiert, modular getestet (**64 JVM Unit-Tests, 100% bestanden, 0 Android-Lint-Fehler**) und auf physischer Hardware validiert:
- **Test-Hardware:** Google Pixel 9 Pro XL (Android 17 / API 37)
- **Kopfhörer:** Apple AirPods 4 (Modell `A3050` / `A3053`, H2-Chip)

---

## 2. Feature- & Architektur-Map (Where to find what)

Die Codebasis ist in einem einzigen `app`-Gradle-Modul unter dem Package `dev.androidpods` streng nach Schichten gegliedert ([`PROJECT.md`](PROJECT.md) §11):

```
dev.androidpods
├── app/                  # Application-Klasse, MainActivity, DI/Providers
├── core/
│   ├── airpods/          # AAP-Protokoll, Session-Handshake, Codecs, Capabilities
│   ├── audio/            # Akustische Signale, Chime-Synthese, Audio-Routing
│   ├── bluetooth/        # L2CAP-Transport, CDM Presence Service, Permissions
│   ├── data/             # AirPodsRepository, AirPodsState, DataStore-Repositories
│   ├── designsystem/     # Material 3 Expressive Theming, Hero-Illustrationen, Shapes, Motion, Headers
│   ├── gestures/         # 6-Achsen-Kopfgestenerkennung (Nicken & Schütteln)
│   ├── media/            # Auto-Pause & Auto-Resume bei Trageerkennung
│   └── telecom/          # Anrufsteuerung über Kopfgesten (TelecomManager / KeyEvents)
└── feature/
    ├── controls/         # Steuerungs- & Einstellungs-Tab (Spatial, Gesten, Chimes, Timing)
    ├── findmy/           # Suchton-Assistent (Find My Audio Chime)
    ├── home/             # Hauptansicht (Brand Hero, Akkustand L/R/Case, Status-Visualisierung)
    ├── navigation/       # Floating Navigation Pill mit Federphysik & Tonal Elevation
    ├── notifications/    # Persistente Akku- & Verbindungsbenachrichtigungen
    ├── onboarding/       # Ersteinrichtung, CDM-Pairing & Morphing Shape Hero
    ├── popup/            # Material 3 Expressive AirPods Battery Pop-up & Dialog Activity
    ├── settings/         # App-Einstellungen (Theme, Dynamic Color, Pop-up Toggle, Logging)
    ├── spatial/          # 3D-Kopforientierungs-Visualizer & IMU-Telemetrie
    ├── tiles/            # Android Quick Settings Tile (Schnelleinstellungen)
    └── widgets/          # Android Glance Home-Screen Battery Widget & native Live-Vorschau
```

---

## 3. Detaillierte Komponenten-Übersicht

### 3.1 Bluetooth- & Transport-Schicht (`dev.androidpods.core.bluetooth`)
- **[`AapTransport.kt`](app/src/main/kotlin/dev/androidpods/core/bluetooth/AapTransport.kt):**  
  Implementiert die rootless Classic BR/EDR L2CAP-Socket-Verbindung über `HiddenApiBypass` (`createInsecureL2capSocket` auf PSM `0x1001`, siehe [ADR-0001](docs/adr/0001-tier-b-hidden-l2cap-socket.md)). Sendet und empfängt Roh-Bytes über Kotlin Coroutines Flows.
- **[`AirPodsPresenceService.kt`](app/src/main/kotlin/dev/androidpods/core/bluetooth/AirPodsPresenceService.kt):**  
  Android `CompanionDeviceService`. Reagiert auf System-Events (`onDevicePresenceEvent`, `onDeviceAppeared`, `onDeviceDisappeared`) für stromsparendes Tracking ohne permanente WakeLocks oder BLE-Scans.
- **[`AirPodsAssociationManager.kt`](app/src/main/kotlin/dev/androidpods/core/bluetooth/AirPodsAssociationManager.kt):**  
  Verwaltet CDM-Assoziationen und steuert den automatischen Reconnect bei App-Start und `onResume()`.
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
  Verbindet Transport, Session und Reducer (`reduce`). Verwaltet den Lebenszyklus des Motion-Streams.
- **[`AirPodsRepositoryProvider.kt`](app/src/main/kotlin/dev/androidpods/core/data/AirPodsRepositoryProvider.kt):**  
  Prozessweiter Singleton-Provider für `AirPodsState` und den gepufferten `events`-Flow (`extraBufferCapacity = 64`).
- **[`AppSettingsRepository.kt`](app/src/main/kotlin/dev/androidpods/core/data/AppSettingsRepository.kt):**  
  DataStore Preferences für Theme, Dynamic Color, Auto-Pause/Resume, Gesten und Benachrichtigungen.
- **[`DataStoreTierProbeCache.kt`](app/src/main/kotlin/dev/androidpods/core/data/DataStoreTierProbeCache.kt):**  
  Persistenter Cache für erfolgreiche Tier-B-Verbindungen mit Self-Healing-Funktion.

### 3.4 Interaktion, Audio & Gesten
- **[`AutoPauseManager.kt`](app/src/main/kotlin/dev/androidpods/core/media/AutoPauseManager.kt):**  
  Pausiert und setzt Medien-Wiedergabe (`AudioManager.dispatchMediaKeyEvent`) anhand der Trageerkennung fort.
- **[`HeadGestureDetector.kt`](app/src/main/kotlin/dev/androidpods/core/gestures/HeadGestureDetector.kt):**  
  Erkennt Nicken (**NOD**) und Kopfschütteln (**SHAKE**) über bidirektionale Oszillationsprüfung auf 50Hz-IMU-Daten. Verhindert Fehl-Auslösungen bei statischer Kopfneigung.
- **[`CallGestureManager.kt`](app/src/main/kotlin/dev/androidpods/core/telecom/CallGestureManager.kt):**  
  Lauscht auf eingehende Anrufe (`TelephonyCallback`), startet den Motion-Stream und steuert die Annahme (Nicken) bzw. Ablehnung (Schütteln) über `TelecomManager` und `KEYCODE_HEADSETHOOK`.
- **[`ChimePlayer.kt`](app/src/main/kotlin/dev/androidpods/core/audio/ChimePlayer.kt):**  
  Synthetisiert das Apple Find-My Suchton-Signal (2.5kHz–5.5kHz Frequenz-Sweeps in 16-Bit 44.1kHz Stereo PCM) mit Kanaltrennung (Links, Beide, Rechts) und Bluetooth-Audio-Routing via `preferredDevice`.

### 3.5 Designsystem & Material 3 Expressive (`dev.androidpods.core.designsystem`)
- **[`Color.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/Color.kt) & [`Theme.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/Theme.kt):**  
  - **Obsidian Dark:** `#0A0D14` Canvas/Surface, `#161B28` Container, `#1E2536` High Container, `#F8FAFC` reines Weiß für Typografie (WCAG AAA).
  - **High-Contrast Light:** `#F1F5F9` Slate-Canvas, `#FFFFFF` reine weiße Karten mit plastischer Elevation, `#0F172A` Deep Navy Text, `#475569` Slate-600 Subtitel.
  - **Electric Cyan & Teal:** `#38BDF8` und `#0284C7` für lebendige Akzente.
- **[`ExpressiveScreenHeader.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/ExpressiveScreenHeader.kt):**  
  Einheitlicher Header für alle Tabs mit bolde Typografie (`headlineLarge`, `FontWeight.Black`), schwungvoller Spring-Eingangsanimation (`androidpodsSpatialSpec()`) und interaktiven Badges.
- **[`AirPodsGenerationBadge.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/AirPodsGenerationBadge.kt):**  
  Interaktives Badge mit Vektorsilhouetten aller 5 AirPods-Generationen (AirPods 1/2, AirPods 3, AirPods 4, AirPods Pro, AirPods Max).
- **[`MorphingShape.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/MorphingShape.kt):**  
  Material 3 Expressive Polygon-Morphing (`MaterialShapes.Sunny`, `Cookie4Sided`, `Clover4Leaf`, `Pill`, `Burst`, `Arch`, `Boom`, `Heart`, `ClamShell`) für Onboarding- und Lade-Hero-Einheiten.
- **[`AirPodsHeroIllustration.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/AirPodsHeroIllustration.kt):**  
  Material 3 Expressive Vektor-Illustrationen und animierte Hero-Composables für **`AnimatedLeftAirPod`**, **`AnimatedRightAirPod`** (harmonische Schwebung mit Phasenverschiebung) und **`AnimatedAirPodsCase`** (mit aktiver, pulsierender **LED-Statusleuchte** in Grün/Gelb/Cyan, Lade-Aura und Feder-Touchkompression).
- **[`SpatialMotionSpec.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/SpatialMotionSpec.kt) & [`AppHaptics.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/AppHaptics.kt):**  
  Zentrale Definition für `androidpodsSpatialSpec()` Federphysik (`DampingRatioMediumBouncy`) und taktiles haptisches Feedback.

### 3.6 UI-Schicht & Android-Integration (`dev.androidpods.feature.*`)
- **[`AirPodsBatteryPopup.kt`](app/src/main/kotlin/dev/androidpods/feature/popup/AirPodsBatteryPopup.kt) & [`BatteryPopupActivity.kt`](app/src/main/kotlin/dev/androidpods/feature/popup/BatteryPopupActivity.kt):**  
  Material 3 Expressive **AirPods Battery Pop-up** Bottom Sheet mit Hero-Trio (`AirPodsExpressiveTrio`), Live-Akkustands-Pillars, Lade-Blitzen (`⚡`), Trageerkennungs-Indikatoren, Wischgesten-Dismissal und transluzentem Activity-Dialog für automatischen Connect-Trigger.
- **[`HomeScreen.kt`](app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt):**  
  Brand-Hero-Header mit App-Logo, animierter Centerpiece-AirPods-Grafik mit Live-Waveform-Equalizer, und **`UnifiedBatteryPillar`**-Trio (harmonische Oberflächen ohne Graustufen-Mischmasch) mit Berührungs-Federkompression (`scale = 0.94f`).
- **[`ControlsScreen.kt`](app/src/main/kotlin/dev/androidpods/feature/controls/ControlsScreen.kt):**  
  Expressiver Steuerungs-Tab für Spatial Audio, Kopfgesten, Find My Suchton und Druck-/Haltezeiten.
- **[`SettingsScreen.kt`](app/src/main/kotlin/dev/androidpods/feature/settings/SettingsScreen.kt):**  
  App-Einstellungen mit Theme-Auswahl, Dynamic Color, Pop-up-Schalter inkl. „Vorschau / Pop-up testen“-Button und L2CAP-Cache-Reset.
- **[`WidgetsScreen.kt`](app/src/main/kotlin/dev/androidpods/feature/widgets/WidgetsScreen.kt):**  
  Interaktive In-App-Live-Vorschau der Akku-Pill mit Spring-Touch-Reaktion (`scale = 0.96f`), animierten Zählern und Pin-Widget-Action.
- **[`BatteryWidget.kt`](app/src/main/kotlin/dev/androidpods/feature/widgets/BatteryWidget.kt):**  
  Android Glance Home-Screen Widget mit M3 Expressive Tonal-Cards (`cornerRadius(32.dp)`), klaren Labels ("Left", "Case", "Right") und Lade-Indikator (`⚡`).
- **[`battery_widget_info.xml`](app/src/main/res/xml/battery_widget_info.xml) & [`widget_battery_preview_layout.xml`](app/src/main/res/layout/widget_battery_preview_layout.xml):**  
  Native 1:1 Vorschau im Android Widget-Picker via echtem Widget-Screenshot (`widget_battery_preview.png`).
- **[`AppNavigation.kt`](app/src/main/kotlin/dev/androidpods/feature/navigation/AppNavigation.kt):**  
  Schwebende **Floating Navigation Pill** (`surfaceContainerLowest`, `shadowElevation = 6.dp`, ohne künstlichen grauen Rand) mit lebendigem Electric-Blue-Aktiv-Zustand.

---

## 4. Wichtige Gating- & Entwicklungsregeln für Bugfixes

1. **Single Source of Truth ([`PROJECT.md`](PROJECT.md) §10):**  
   Alle UI-Elemente, Widgets, Tiles und Benachrichtigungen beobachten ausschließlich `AirPodsRepositoryProvider.state`. Niemals sekundäre StateFlows oder parallele Zustandsautomaten anlegen.
2. **Capability-Gated UI (§9):**  
   Nicht unterstützte Features müssen ausgeblendet oder ehrlich erklärt werden (z. B. kein Noise Control bei Standard-AirPods 4).
3. **Pacing im AAP-Handshake:**  
   Das `delay(200)` zwischen Paketen in `AapSession.start()` ist hardware-bedingt notwendig. Nicht entfernen.
4. **Verifikations-Befehle:**
   ```bash
   ./gradlew :app:assembleDebug          # Debug-APK bauen
   ./gradlew :app:testDebugUnitTest      # JVM Unit-Tests
   ./gradlew :app:lintDebug              # Android Lint
   ```

---

## 5. Zukünftige Erweiterungen (sobald weitere Hardware vorliegt)

- **Active Noise Cancellation (ANC) / Transparency Writes (M3):**  
  Gated für AirPods Pro 1/2 und AirPods 4 mit ANC. Paket-Hex-Definitionen sind im Code dokumentiert, Schreibbefehle erfordern physische Hardware-Validierung vor Freigabe.
- **Case Speaker Chimes (M7):**  
  Aktivierung des integrierten Case-Lautsprechers auf AirPods 4 mit ANC und AirPods Pro 2.
