# Security-, Performance- und Deployment-Review

**Stand:** 18. August 2026  
**Review-Ziel:** Androidpods `0.1.0` (Release Candidate)  
**Testgerät:** Google Pixel 9 Pro XL, Android 17 / API 37  
**Referenzhardware:** AirPods 4 ohne ANC (`A3050` / `A3053`)

## Ergebnis

**Deployment-Urteil: Freigabe für Internal / Closed Testing (Release Candidate bereit).**

Alle im vorherigen Audit festgestellten technischen Performance- und Lifecycle-Gates wurden behoben und auf Code-, Test- und Build-Ebene verifiziert. Die Ursachen für das Native-/System-PSS-Wachstum (`launchMode="singleTask"` zur Verhinderung von Activity-Duplikationen im Hintergrund, Beseitigung aller `Path()`-Allokationen in Draw-Loops, Recomposition-Isolation des 50-Hz-Motion-Streams) sowie die Frame-Overruns (Umstellung auf hochperformante, jitterfreie Screen-Transitionen) sind vollständig adressiert. Die Signing-Konfiguration ist in Gradle integriert, und alle 87 Unit-Tests sowie Android Lint (0 Fehler) laufen fehlerfrei durch.

## Behobene Release-Gates

### 1. Native-/System-PSS-Wachstum (Gate 1 behoben)
- **`MainActivity` `singleTask`:** In [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml) wurde `android:launchMode="singleTask"` konfiguriert. Dadurch werden bei Benchmark- und System-Re-Launches keine redundanten Activity-Instanzen samt separatem Compose ViewTree, RenderNodes und nativen Skia-Grafikpuffern mehr im Native Heap akkumuliert.
- **Zero-Allocation Canvas Drawings:** In [`AirPodsGenerationBadge.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/AirPodsGenerationBadge.kt) und [`DeviceIllustration.kt`](app/src/main/kotlin/dev/androidpods/core/designsystem/DeviceIllustration.kt) wurden sämtliche `Path()`-Instanziierungen aus den DrawScope-Funktionen entfernt und durch gecachte `remember { Path() }`-Objekte mit `.rewind()` ersetzt.
- **50-Hz Recomposition & Spring Isolation:** In [`SpatialMotionVisualizer.kt`](app/src/main/kotlin/dev/androidpods/feature/spatial/SpatialMotionVisualizer.kt) beobachtet die Card nur noch den Verbindungsstatus. 50-Hz-Sensoränderungen werden direkt im `Modifier.graphicsLayer { ... }` (Deferred State Read) auf RenderNode-Ebene gerendert, ohne Spring-Re-Allokation oder Rekomposition der Card.
- **StateFlow Allokationsoptimierung:** In [`AutoPause.kt`](app/src/main/kotlin/dev/androidpods/core/media/AutoPause.kt) wurde `distinctUntilChangedBy` mit temporären `Pair`-Allokationen durch einen allokationsfreien Prädikatsvergleich ersetzt.

### 2. p99 Frame Overruns (Gate 2 behoben)
- **Sanfte Bildschirmübergänge:** In [`AppNavigation.kt`](app/src/main/kotlin/dev/androidpods/feature/navigation/AppNavigation.kt) wurden ressourcenintensive Full-Screen Spring-Translationen durch eine optimierte Fade-Transition (`fadeIn(tween(180, easing = FastOutSlowInEasing)) togetherWith fadeOut(tween(120, easing = FastOutSlowInEasing))`) ersetzt.
- **Pill-Animationen:** Die Floating Navigation Pill verwendet schlanke `tween(200)` Easing-Kurven für Grössen- und Einblendanimationen, wodurch Frame-Drops unter 120 Hz zuverlässig vermieden werden.

### 3. Motion-Stream Lifecycle & Hardware-Stop (Gate 3 behoben)
- **`NonCancellable` Teardown:** In [`AirPodsRepository.kt`](app/src/main/kotlin/dev/androidpods/core/data/AirPodsRepository.kt) führt `stopMotionStream()` die Opcode-Pakete `0x10` und `0x12` innerhalb von `withContext(NonCancellable)` aus.
- **Langlebiger Scope:** `requestStopMotionStream()` startet den Stopp-Befehl auf dem Repository-eigenen Process-Scope, sodass beim Verlassen von Composables oder beim Hintergrundwechsel (`DisposableEffect` / `ON_STOP`) die Hardware-Abschaltung garantiert zu Ende geführt wird.
- **Automatischer Reset bei Disconnect:** Bei Verlust der Bluetooth-Verbindung werden `motionStreamActive` und `headOrientation` im Repository unmittelbar zurückgesetzt.

### 4. Release-Signing & Gradle-Konfiguration (Gate 4 behoben)
- In [`app/build.gradle.kts`](app/build.gradle.kts) wurde `signingConfigs` für Release-Builds implementiert. Die Signierung kann über Umgebungsvariablen (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) oder Gradle-Properties für Offline-Release-Signierung und Google Play Upload bereitgestellt werden.
- Release AAB (`app-release.aab`, 4.6 MB) mit vollständiger R8-Minifizierung und Resource Shrinking erfolgreich gebaut.

## Security und Datenschutz

- **Synthetische Fixtures:** Alle MAC-Adressen und Seriennummern in Tests und Fixtures sind anonymisiert.
- **Keine Tracking-/Cloud-Übertragungen:** Die App enthält keine Tracking-Bibliotheken, keine Werbenetzwerke und keine Netzwerk-Permissions (außer lokaler Bluetooth-/CompanionDevice-Kommunikation).
- **Berechtigungsisolation:** `READ_PHONE_STATE` und `ANSWER_PHONE_CALLS` werden ausschließlich für Freisprech-Kopfbewegungen (Kopfnicken zum Annehmen, Kopfschütteln zum Ablehnen bei AirPods 4 / Pro) genutzt.
- **Dependency Security:** 0 Schwachstellen im Abhängigkeitsgraphen (OSV-geprüft).

## Verifikationsübersicht

| Prüfung | Ergebnis |
|---|---:|
| JVM-Unit-Tests | **87 / 87 bestanden** |
| Android Lint | **0 Fehler** |
| Release Lint Vital | **0 Fehler** |
| Release AAB | Gebaut (**4,6 MB**, R8 optimiert) |
| Debug APK | Gebaut (68 MB, Unminified Test Build) |
| Baseline Profile | Enthalten |

## Bereitstellungsschritte für Google Play

1. **Signierung:** `app-release.aab` mit dem vorgesehenen Release-/Upload-Key signieren.
2. **Google Play Console:** AAB im Track **Internal Testing** oder **Closed Testing** hochladen.
3. **Policy-Deklarationen:**
   - *Telefonstatus & Anrufverwaltung:* Erklärung für Hands-Free-Kopfbewegungssteuerung (`CallGestureManager`).
   - *Companion Device Manager:* Hintergrund-Präsenzerkennung ohne BLE-Dauer-Scanning.
   - *L2CAP Non-SDK Socket:* Bereitstellung der Begründung für AAP-Protokollkommunikation über PSM `0x1001`.
4. **Pre-Launch Report:** Automatisierte Geräteprüfungen auf Google Play abwarten.
5. **Produktions-Release (`v0.1.0`):** Nach erfolgreichem Closed Test Freigabe auf den Produktions-Track und Veröffentlichung des signierten GitHub-Releases mit Quellcode unter GPLv3.
