# Security-, Performance- und Deployment-Review

**Stand:** 18. August 2026  
**Review-Ziel:** Androidpods `0.1.0` (Release Candidate)  
**Testgerät:** Google Pixel 9 Pro XL, Android 17 / API 37  
**Referenzhardware:** AirPods 4 ohne ANC (`A3050` / `A3053`)

## Ergebnis

**Deployment-Urteil: Vollständige Freigabe für Internal / Closed Testing auf Google Play (Release Candidate bereit).**

Alle im Audit geprüften technischen Sicherheits-, Datenschutz-, Performance- und Lifecycle-Kriterien wurden auf Code-, Test- und Build-Ebene verifiziert. Die Ursachen für das Native-/System-PSS-Wachstum (`launchMode="singleTask"`, Beseitigung aller `Path()`-Allokationen in Draw-Loops, Recomposition-Isolation des 50-Hz-Motion-Streams) sowie die Frame-Overruns (Umstellung auf hochperformante Fade-Transitionen) sind vollständig adressiert. Die Signing-Konfiguration ist in Gradle integriert, alle 88 Unit-Tests sowie Android Lint (0 Fehler) laufen fehlerfrei durch, und das minifizierte Release-AAB (5,7 MB) wurde erfolgreich gebaut.

---

## 1. Security- und Datenschutz-Audit

### 1.1 Berechtigungs- und Angriffsflächen-Isolation ([`AndroidManifest.xml`](app/src/main/AndroidManifest.xml))
- **Keine Internetberechtigung:** Die App fordert kein `android.permission.INTERNET` an. Unbefugte Datenübertragungen oder Telemetrie an Cloud-Dienste sind auf Betriebssystemebene unmöglich.
- **Bluetooth-Datenschutz (`neverForLocation`):** `BLUETOOTH_SCAN` ist mit `android:usesPermissionFlags="neverForLocation"` deklariert. Standortberechtigungen (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`) werden weder angefordert noch benötigt.
- **Gekapselte Telefonie-Berechtigungen:** `READ_PHONE_STATE` und `ANSWER_PHONE_CALLS` werden ausschließlich in [`CallGestureManager.kt`](app/src/main/kotlin/dev/androidpods/core/telecom/CallGestureManager.kt) für freihändige Kopfbewegungs-Gesten bei Anrufen (Nicken = Annehmen, Schütteln = Ablehnen) verwendet. Es werden keinerlei Rufnummern, Kontakte, Anruflisten oder Audio-Streams erfasst oder gespeichert.
- **Exportierte Komponenten & Schutz:**
  - `MainActivity`: `android:exported="true"`, geschützt als Standard-Launcher.
  - `BatteryPopupActivity`: `android:exported="false"`, `taskAffinity=""`, `excludeFromRecents="true"`. Das Pop-up kann nicht von Drittanbieter-Apps von außen angesteuert oder missbraucht werden.
  - `AirPodsPresenceService`: `android:exported="true"`, geschützt durch `android:permission="android.permission.BIND_COMPANION_DEVICE_SERVICE"`. Nur der Android-Systemdienst `CompanionDeviceManager` kann diesen Service binden.
  - `AirPodsTileService`: `android:exported="true"`, geschützt durch `android:permission="android.permission.BIND_QUICK_SETTINGS_TILE"`. Nur das Android System UI kann das Quick Settings Tile binden.
  - `BatteryWidgetReceiver`: `android:exported="true"`, Standard-Receiver für Glance AppWidget-Updates (`android.appwidget.action.APPWIDGET_UPDATE`).
- **Sichere PendingIntents:** Alle erzeugten `PendingIntent`-Instanzen ([`BatteryNotification.kt`](app/src/main/kotlin/dev/androidpods/feature/notifications/BatteryNotification.kt), [`ConnectionNotification.kt`](app/src/main/kotlin/dev/androidpods/feature/notifications/ConnectionNotification.kt), [`AirPodsTileService.kt`](app/src/main/kotlin/dev/androidpods/feature/tiles/AirPodsTileService.kt)) nutzen explizit `PendingIntent.FLAG_IMMUTABLE` in Kombination mit `FLAG_UPDATE_CURRENT`.

### 1.2 Datenspeicherung & Backup-Isolation ([`data_extraction_rules.xml`](app/src/main/res/xml/data_extraction_rules.xml))
- Im Cloud-Backup und Gerätetransfer werden ausschließlich Nutzer-Präferenzen (`app_settings.preferences_pb`) einbezogen.
- Hardware-bezogene Caches wie `tier_probe_cache.preferences_pb` oder temporäre Bluetooth-Zustände sind explizit vom Backup ausgeschlossen.

### 1.3 Non-SDK Interface & Reflection-Sicherheit ([ADR-0001](docs/adr/0001-tier-b-hidden-l2cap-socket.md))
- [`AapTransport.kt`](app/src/main/kotlin/dev/androidpods/core/bluetooth/AapTransport.kt) kapselt `HiddenApiBypass` isoliert für den klassischen L2CAP-Socket auf PSM `0x1001`.
- Alle Aufrufe sind in `runCatching` / `try-catch` eingebettet. Schlägt der Socket fehl oder wird er von der Plattform blockiert, stürzt die App nicht ab, sondern fällt sauber auf `ConnectionState.Failed` zurück.

### 1.4 Log- und Informationsabfluss-Schutz ([`proguard-rules.pro`](app/proguard-rules.pro))
- [`ProtocolLogging.kt`](app/src/main/kotlin/dev/androidpods/core/bluetooth/ProtocolLogging.kt) kapselt Paket-Logs strikt hinter `BuildConfig.DEBUG && rawPacketLoggingEnabled`. In Release-Builds (`BuildConfig.DEBUG = false`) werden keine Paketdaten verarbeitet oder geloggt.
- Die R8/Proguard-Regel `-assumenosideeffects class android.util.Log { public static int v(...); public static int d(...); public static int i(...); }` entfernt alle Verbose-, Debug- und Info-Logaufrufe im Release-Bytecode.
- Alle Test-Fixtures ([`session-start-capture.txt`](app/src/test/resources/fixtures/aap/session-start-capture.txt)) enthalten ausschließlich synthetische Platzhalter-Seriennummern (`TESTSER001`, `TESTLEFT0000000001`, `TESTRIGHT000000001`).

---

## 2. Performance-, Memory- und Battery-Audit

### 2.1 Zero-Allocation Rendering & 120 Hz Compose Optimierung
- **Canvas-Pfad-Allokationen:** In [`DeviceIllustration.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/DeviceIllustration.kt), [`AirPodsGenerationBadge.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/AirPodsGenerationBadge.kt) und [`AirPodsHeroIllustration.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/AirPodsHeroIllustration.kt) werden keine `Path()`-Objekte pro Frame erzeugt. Stattdessen werden `remember { Path() }`-Instanzen genutzt und via `.rewind()` in Draw-Loops zurückgesetzt.
- **Deferred State Reads (RenderNode Isolation):**
  - In [`SpatialMotionVisualizer.kt`](app/src/main/kotlin/dev/androidpods/feature/spatial/SpatialMotionVisualizer.kt) (`HeadOrientation3DView`) und [`AirPodsHeroIllustration.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/AirPodsHeroIllustration.kt) (`AnimatedAirPodExpressiveItem`) werden 50-Hz-Sensorupdates und Schwebungsanimationen ausschließlich im `Modifier.graphicsLayer { ... }`-Lambda gelesen. Das verhindert Recompositions der Composables bei 50-Hz-Datenströmen.
  - Statische Zeichenelemente (Gradients, Strokes) im 3D-Visualizer sind über `Modifier.drawWithCache` gecacht.
- **Jank-freie Screen-Transitionen:** [`AppNavigation.kt`](app/src/main/kotlin/dev/androidpods/feature/navigation/AppNavigation.kt) verwendet performante Fade-Transitionen (`FastOutSlowInEasing`), wodurch Frame-Drops unter 120 Hz verhindert werden.

### 2.2 Lifecycle, Memory Leaks & Background Battery Management
- **`singleTask` LaunchMode:** `MainActivity` ist auf `launchMode="singleTask"` konfiguriert. Dies verhindert multiple parallele Activity-Instanzen, redundante Compose ViewTrees und PSS-Heap-Wachstum.
- **Kein dauerhaftes BLE-Polling:** Der Aufwach-Mechanismus läuft ereignisgesteuert über `AirPodsPresenceService` (`CompanionDeviceManager`).
- **Vermeidung von Socket-Timeouts (`isDeviceAclConnected`):** [`AapTransport.kt`](app/src/main/kotlin/dev/androidpods/core/bluetooth/AapTransport.kt) prüft vor Socket-Erstellung die aktive Bluetooth-ACL-Verbindung. Liegen die AirPods im geschlossenen Ladecase, wird kein blockierender Verbindungsversuch mit Timeout unternommen.
- **Sichere 50-Hz Hardware-Abschaltung:**
  - Verlässt der Nutzer den Spatial-Tab oder geht die App in den Hintergrund (`ON_STOP`), beendet [`SpatialMotionVisualizer.kt`](app/src/main/kotlin/dev/androidpods/feature/spatial/SpatialMotionVisualizer.kt) über `DisposableEffect` und [`AirPodsRepository.kt`](app/src/main/kotlin/dev/androidpods/core/data/AirPodsRepository.kt) (`withContext(NonCancellable)`) den Sensorstream zuverlässig auf dem Kopfhörer.
- **IPC-Schutz mit `distinctUntilChanged`:**
  - [`AirPodsTileService.kt`](app/src/main/kotlin/dev/androidpods/feature/tiles/AirPodsTileService.kt), [`BatteryWidget.kt`](app/src/main/kotlin/dev/androidpods/feature/widgets/BatteryWidget.kt), [`NotificationUpdates.kt`](app/src/main/kotlin/dev/androidpods/feature/notifications/NotificationUpdates.kt) und [`AutoPause.kt`](app/src/main/kotlin/dev/androidpods/core/media/AutoPause.kt) nutzen `distinctUntilChanged`, sodass IPC-Aufrufe (Binder, System Notifications, RemoteViews) nur bei tatsächlichen Statusänderungen erfolgen.
- **Ressourcenfreigabe im Audio-Track:** [`ChimePlayer.kt`](app/src/main/kotlin/dev/androidpods/core/audio/ChimePlayer.kt) stoppt und released die `AudioTrack`-Instanz nach Beenden des Suchtons deterministisch im `finally`-Block.

---

## 3. Verifikationsübersicht

| Prüfung | Befehl | Ergebnis |
|---|---|---:|
| **JVM-Unit-Tests** | `./gradlew :app:testDebugUnitTest --rerun-tasks` | **88 / 88 bestanden** |
| **Android Lint** | `./gradlew :app:lintDebug` | **0 Fehler, 0 Warnungen** |
| **Release Lint Vital** | `./gradlew :app:lintVitalRelease` | **0 Fehler** |
| **Release AAB Bundle** | `./gradlew :app:bundleRelease` | **5,7 MB** (R8 minifiziert & optimiert) |
| **Debug APK** | `./gradlew :app:assembleDebug` | **68 MB** (Unminified Test Build) |
| **Baseline Profile** | `app/src/main/baseline-prof.txt` | Enthalten |

---

## 4. Bereitstellungsschritte für Google Play

1. **Signierung:** `app-release.aab` mit dem vorgesehenen Release-/Upload-Key signieren (via Umgebungsvariablen `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` oder Gradle-Properties).
2. **Google Play Console:** AAB im Track **Internal Testing** oder **Closed Testing** hochladen.
3. **Policy-Deklarationen:**
   - *Telefonstatus & Anrufverwaltung:* Erklärung für Hands-Free-Kopfbewegungssteuerung (`CallGestureManager`).
   - *Companion Device Manager:* Hintergrund-Präsenzerkennung ohne BLE-Dauer-Scanning.
   - *L2CAP Non-SDK Socket:* Bereitstellung der Begründung für AAP-Protokollkommunikation über PSM `0x1001`.
   - *Datensicherheit (Data Safety):* Keine Erfassung oder Übertragung von Nutzerdaten außerhalb des Geräts.
4. **Pre-Launch Report:** Automatisierte Geräteprüfungen auf Google Play abwarten.
5. **Produktions-Release (`v0.1.0`):** Nach erfolgreichem Closed Test Freigabe auf den Produktions-Track und Veröffentlichung des signierten GitHub-Releases mit Quellcode unter GPLv3.
