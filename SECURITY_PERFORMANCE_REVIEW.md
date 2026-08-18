# Security-, Performance- und Deployment-Review

**Stand:** 18. August 2026  
**Review-Ziel:** Androidpods `0.1.0`  
**Testgerät:** Google Pixel 9 Pro XL, Android 17 / API 37  
**Referenzhardware:** AirPods 4 ohne ANC (`A3050` / `A3053`)

## Ergebnis

**Deployment-Urteil: No-Go.**

Die Codebasis ist deutlich gehärtet, der optimierte Build ist klein und der gemessene Cold Start erfüllt das festgelegte Ziel. Eine Veröffentlichung ist trotzdem noch nicht vertretbar: Das Frame-p99-Gate und beide PSS-Gates schlagen fehl, der abschließende Motion-Lifecycle-Assert wurde deshalb nicht erreicht, die Release-Artefakte sind unsigniert und Play Pre-Launch sowie die Policy-Prüfung für Telefonberechtigungen und `HiddenApiBypass` stehen aus.

## Security und Datenschutz

### Behoben

- Persönliche Hardwarekennungen in Tests und Paket-Fixtures wurden durch synthetische Werte gleicher Struktur ersetzt.
- Alle erreichbaren Commits und der Arbeitsbaum wurden gegen die bekannte private Kennungsliste geprüft. Der abschließende Scan enthält keine Treffer.
- Die Git-Historie wurde vollständig neu geschrieben. Eine verifizierte, nicht veröffentlichte Bundle-Sicherung und der alte Remote-SHA wurden vor dem Rewrite außerhalb des Repositories mit Dateimodus `0600` abgelegt.
- Rohpakete laufen ausschließlich über `ProtocolLogging`. Byte-Dumps werden nur bei `BuildConfig.DEBUG && rawPacketLoggingEnabled` erzeugt; der Schalter ist threadsicher und nur in Debug-Builds sichtbar.
- Cloud-Backup und Gerätetransfer sind per `dataExtractionRules` auf `files/datastore/app_settings.preferences_pb` begrenzt. `tier_probe_cache.preferences_pb` ist ausgeschlossen.
- [`PRIVACY.md`](PRIVACY.md) dokumentiert lokale Verarbeitung, fehlende Konten/Analytics/Internetübertragung, Bluetooth-Zuordnung, Telefonstatus und das opt-in Debug-Logging.
- Der Gradle-Wrapper prüft Gradle 9.5.0 gegen die offizielle SHA-256. Strikte Dependency Verification deckt den aufgelösten Build-Graph ab.
- Der aufgelöste `releaseRuntimeClasspath` wurde mit OSV geprüft: **0 Treffer in 142 Koordinaten**.
- AgentShield bewertet die lokale Claude-Konfiguration mit **A / 100**, **0 High**, **0 Medium** und einem bewussten Low-Hinweis wegen des nicht eingeführten Stop-Hooks.
- Eine SHA-gepinnte GitHub-CI baut Unit-Tests, Lint und das unsignierte Release-Bundle. Dependabot überwacht Gradle und GitHub Actions monatlich.
- Keystores und lokale Signing-Properties sind ignoriert; Signierschlüssel bleiben außerhalb von Git und CI.

### Verbleibende Grenzen

- Ein History-Rewrite bereinigt keine Forks, alten Klone, bereits heruntergeladenen Artefakte oder externe Caches. Besitzer solcher Kopien müssen sie separat löschen oder neu klonen.
- Das Release-Bundle und die Release-APK sind noch **unsigniert**. Es wurde deshalb keine Zertifikatskette verifiziert und kein veröffentlichbares Artefakt erzeugt.
- Play Pre-Launch, Permission Review und die Prüfung der verwendeten Non-SDK-API sind noch offen. Eine Ablehnung von `HiddenApiBypass` ist ein harter Play-Release-Blocker.

## Ehrliche Fähigkeiten und Schreiboperationen

- Die rein lokale Noise-Control-Oberfläche und die zugehörigen Domain-Fähigkeiten wurden entfernt.
- `ChimeTarget.CASE`, Case-Speaker-Capabilities und die irreführende Case-Auswahl wurden entfernt. Der verbleibende Chime steuert ausschließlich die Ohrhörerkanäle.
- Schreibfähigkeiten sind nur für die physisch validierte AirPods-4-Familie ohne ANC freigeschaltet. Andere Modelle behalten ausschließlich belegte Lesefunktionen.
- `AirPodsTransport.send(ByteArray)` signalisiert fehlende Verbindungen und Schreibfehler als `IOException`, schließt defekte Sockets und aktualisiert den Connection-State.
- Geräteeinstellungen werden erst nach erfolgreichem Transport-Write persistiert. Fehler erreichen die Compose-UI und bestätigte AAP-Werte bleiben die bevorzugte Darstellungsquelle.
- Der 50-Hz-Stream für Anrufgesten ist an Nutzeroption, Capability, Anrufzustand und Lifecycle gebunden.
- Überholte SDK-Zweige unterhalb von `minSdk = 36` wurden entfernt. Die deprecated Telecom-Aufrufe bleiben für v1 bewusst bestehen und benötigen eine Play-Policy-Begründung.

## Performance und Codequalität

- `HeadGestureDetector` verarbeitet nur Pitch, Yaw und Zeit. Minima und Maxima werden ohne Listen oder Boxing in einem Durchlauf berechnet.
- Der Paketdecoder vergleicht Headerbytes direkt und allokiert kein `copyOfRange` pro Paket.
- Der Spatial-Motion-Bereich ist eine eigene Recomposition-Grenze. Animationswerte werden in `graphicsLayer` gelesen; unveränderliche Zeichenobjekte werden mit `drawWithCache` wiederverwendet.
- Activity-/Compose-Flows verwenden `collectAsStateWithLifecycle`; `AirPodsRepository.state` bleibt die einzige Zustandsquelle.
- Endlosanimationen werden nur für sichtbare und aktive Elemente erzeugt und bei Reduced Motion vollständig angehalten.
- Tile- und Widget-Vergleiche vermeiden temporäre `Triple`-Objekte.
- Generierte `graphify-out`-Dateien, ungenutzte Ressourcen und obsolete SDK-Zweige wurden entfernt. UI- und Accessibility-Texte liegen in `strings.xml`.
- Launcher-, Widget- und Logo-Bitmaps liegen passend skaliert in Density-Verzeichnissen; Adaptive Icons enthalten eine monochrome Ebene.
- Der Release-Build nutzt R8, Resource Shrinking und `proguard-android-optimize.txt`. Nur Verbose-, Debug- und Info-Logs werden entfernt; Warnungen und Fehler bleiben erhalten.

## Verifikation

### Build und statische Prüfungen

| Prüfung | Ergebnis |
|---|---:|
| JVM-Unit-Tests | 84 / 84 bestanden |
| Android Lint | 0 Fehler; 1 bewusster Gradle-Upgrade-Hinweis |
| Strikte Dependency Verification | bestanden |
| Release AAB | gebaut, 4.792.812 Bytes, unsigniert |
| Release APK | gebaut, 3.633.734 Bytes, unsigniert |
| Benchmark-Modul | kompiliert |
| Versioniertes Baseline Profile | 19.546 Zeilen, 2.017.177 Bytes |
| Baseline-Profile-SHA-256 | `5a806321edf1f8adfb0d1a846cc979b442ad41020c5b72d4f806bfbf72595505` |
| Baseline Profile im AAB/APK | enthalten |

Der einzelne Test `generateBaselineProfile` ist erfolgreich. Der Aggregat-Task `connectedBaselineProfileAndroidTest` endet dennoch mit 3/5 bestandenen Tests, weil die beiden PSS-Assertions im selben Instrumentierungslauf fehlschlagen. Ein Task `:app:generateBaselineProfile` wird bewusst nicht vorgetäuscht: Das AndroidX Baseline Profile Gradle Plugin 1.4.1 ist binär inkompatibel mit AGP 9.3.1. Das Profil wird stattdessen durch das Benchmark-Modul erzeugt und im App-Quellpfad versioniert.

### Pixel-9-Pro-XL-Benchmarks

Cold Start und Hardware-Motion müssen in getrennten Gerätezuständen laufen. Bei aktiv präsenten AirPods startet der Companion Device Manager den Prozess unmittelbar neu; AndroidX kann dann keinen echten Cold State herstellen.

| Gate | Messwert | Ziel | Ergebnis |
|---|---:|---:|:---:|
| Cold-Start-TTID, 10 Läufe | min 81,4 ms; Median 263,4 ms; p95/max 284,3 ms | p95 ≤ 500 ms | bestanden |
| `frameOverrunMs`, 20 Läufe | p95 −1,1 ms; p99 **12,0 ms** | p95 ≤ 0 ms; p99 ≤ 8,33 ms | **fehlgeschlagen** |
| Navigation-PSS, 20 Messzyklen | 127.502 KB → 158.635 KB; **+24,4 %** | ≤ +10 % | **fehlgeschlagen** |
| Hardware-Motion-PSS, 20 Zyklen | 158.567 KB → 193.167 KB; **+21,8 %** | ≤ +10 % | **fehlgeschlagen** |
| Motion-Stream nach Lifecycle-Stopp | nicht erreicht, da PSS-Assert vorher fehlschlug | gestoppt | **offen** |

Das reproduzierbare Wachstum liegt überwiegend im Native Heap beziehungsweise System PSS. Eine Freigabe erfordert einen neuen Profiling-Durchlauf, eine belegte Ursache und anschließend bestandene 20-Zyklen-Gates. Perfetto meldete dabei `EXITCODE=2`; AndroidX toleriert diesen Wert, warnt aber, dass Datenquellen eventuell noch nicht vollständig bereit waren.

## Offene Release-Gates

Vor einem Deployment müssen alle folgenden Punkte erfüllt sein:

1. Ursache des Native-/System-PSS-Wachstums beheben und beide 10-%-Gates auf dem Pixel 9 Pro XL bestehen.
2. Die p99-Frame-Overruns auf höchstens 8,33 ms senken und den 20-Läufe-Navigationsbenchmark wiederholen.
3. Motion-Stream-Abschaltung bei explizitem Stop, Lifecycle-Stopp, deaktivierter Option und fehlender Capability auf Hardware vollständig nachweisen.
4. Release-AAB offline mit dem vorgesehenen App-Signing-Key signieren; GitHub-APK mit demselben App-Key signieren und beide Signaturen verifizieren. Der Play-Upload-Key bleibt separat.
5. Dasselbe geprüfte AAB zunächst in Internal/Closed Testing ausrollen und den Play Pre-Launch Report ohne Blocker abschließen.
6. Telefonberechtigungen, deprecated Telecom-Aufrufe und `HiddenApiBypass` im Play-Policy-Review akzeptieren lassen.
7. Erst danach `v0.1.0` mit signierter APK, SHA-256-Prüfsummen, GPL-Quellcode und Third-Party-Notices veröffentlichen.

Bis diese Gates bestanden sind, darf weder das aktuelle unsignierte Artefakt veröffentlicht noch der interne Teststand in Produktion promotet werden.
