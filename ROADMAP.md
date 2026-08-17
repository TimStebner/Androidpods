# Androidpods — Roadmap & Architecture Reference

> **Zweck:** Zentrale Dokumentation des aktuellen Entwicklungsstands, der implementierten Features, der Architektur und der Dateistruktur.  
> **Zielgruppe:** Entwickler und AI-Agenten zur schnellen Orientierung, für zielgerichtete Bugfixes und Weiterentwicklungen.  
> **Autoritative Spezifikation:** [`PROJECT.md`](PROJECT.md) (Architektur- und Plattformvorgaben nach §31).

---

## 1. Projektstatus & Hardware-Verifikation

Alle geplanten **Milestones 0 bis 7** sind vollständig implementiert, modular getestet (**64 Unit-Tests, 100% bestanden, 0 Android-Lint-Fehler**) und auf physischer Hardware validiert:
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
│   ├── designsystem/     # Material 3 Expressive Theming, Dynamic Color, Shapes, Motion
│   ├── gestures/         # 6-Achsen-Kopfgestenerkennung (Nicken & Schütteln)
│   ├── media/            # Auto-Pause & Auto-Resume bei Trageerkennung
│   └── telecom/          # Anrufsteuerung über Kopfgesten (TelecomManager / KeyEvents)
└── feature/
    ├── controls/         # Steuerungs- & Einstellungs-Tab (Spatial, Gesten, Chimes, Timing)
    ├── findmy/           # Suchton-Assistent (Find My Audio Chime)
    ├── home/             # Hauptansicht (Akkustand L/R/Case, Status-Visualisierung)
    ├── notifications/    # Persistente Akku- & Verbindungsbenachrichtigungen
    ├── onboarding/       # Ersteinrichtung & Pairing via CompanionDeviceManager
    ├── settings/         # App-Einstellungen (Theme, Dynamic Color, Logging, Cache-Reset)
    ├── spatial/          # 3D-Kopforientierungs-Visualizer & IMU-Telemetrie
    ├── tiles/            # Android Quick Settings Tile (Schnelleinstellungen)
    └── widgets/          # Android Glance Home-Screen Battery Widget
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

### 3.5 UI-Schicht & Android-Integration (`dev.androidpods.feature.*`)
- **[`HomeScreen.kt`](app/src/main/kotlin/dev/androidpods/feature/home/HomeScreen.kt):**  
  Expressive Hauptansicht mit Akkustandsanzeigen, AirPods-Grafik und Trageerkennungs-Pills.
- **[`ControlsScreen.kt`](app/src/main/kotlin/dev/androidpods/feature/controls/ControlsScreen.kt):**  
  Zentraler Steuerungs-Tab für Spatial Audio, Kopfgesten (inkl. Permission-Flow), Find My Suchton, Druckgeschwindigkeit und Haltedauer.
- **[`SpatialMotionVisualizer.kt`](app/src/main/kotlin/dev/androidpods/feature/spatial/SpatialMotionVisualizer.kt):**  
  3D-Canvas-Visualizer mit perspektivischer Drehung (`graphicsLayer`), Orbit-Ringen, Live-Winkelanzeige und Gesten-Badge.
- **[`FindMyCard.kt`](app/src/main/kotlin/dev/androidpods/feature/findmy/FindMyCard.kt):**  
  Suchton-Karte mit Zielauswahl (Left, Both, Right), animiertem Sonar-Effekt, Warnhinweis und Play/Stop-Steuerung.
- **[`AirPodsTileService.kt`](app/src/main/kotlin/dev/androidpods/feature/tiles/AirPodsTileService.kt):**  
  Android Quick Settings Tile mit dynamischem Akku-Untertitel und Schnellstart-Funktion.
- **[`BatteryWidget.kt`](app/src/main/kotlin/dev/androidpods/feature/widgets/BatteryWidget.kt):**  
  Glance Home-Screen Widget als 1:1 Pixel-Twin des Android-Designs.
- **[`BatteryNotification.kt`](app/src/main/kotlin/dev/androidpods/feature/notifications/BatteryNotification.kt) & [`ConnectionNotification.kt`](app/src/main/kotlin/dev/androidpods/feature/notifications/ConnectionNotification.kt):**  
  Systembenachrichtigungen für Akkustand und Verbindungsstatus.

---

## 4. Wichtige Gating- & Entwicklungsregeln für Bugfixes

1. **Single Source of Truth ([`PROJECT.md`](PROJECT.md) §10):**  
   Alle UI-Elemente, Widgets, Tiles und Benachrichtigungen beobachten ausschließlich `AirPodsRepositoryProvider.state`. Niemals sekundäre StateFlows oder parallele Zustandsautomaten anlegen.
2. **Capability-Gated UI (§9):**  
   Nicht unterstützte Features müssen ausgeblendet oder ehrlich erklärt werden (z. B. kein Case-Lautsprecher bei Standard-AirPods 4).
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
