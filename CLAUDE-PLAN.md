# Androidpods — Spec-Review + Entwicklungsplan

## Kontext

`PROJECT.md` (1431 Zeilen) ist gelesen. Das Repo enthält aktuell **nur** Doku (`PROJECT.md`, `CLAUDE.md`, `skills-lock.json`) — kein Gradle-Projekt, kein Code, keine `LICENSE`, keine `README.md`.

Die Spec ist inhaltlich stark: Architektur-Layering, Capability-Modell, Battery-Policy und Definition-of-Done sind sauber durchdacht. Beim Gegenprüfen gegen Realität (Maven-Metadaten, lokale Toolchain, Stand der AirPods-Protokoll-Forschung) sind aber **acht konkrete Korrekturen** aufgefallen — eine davon betrifft die technische Machbarkeit des Kern-Feature-Sets. Deshalb: erst Spec-Patches, dann Entwicklung.

> **Plan-Mode-Hinweis:** In Plan Mode darf ich nur diese Datei schreiben. Die `PROJECT.md`-Änderungen sind unten als exakte Patches formuliert (englisch, weil das Dokument englisch ist und §30 englische Identifier/Kommentare fordert). Mit der Plan-Freigabe werden sie als erster Schritt angewendet.

---

## Recherche-Ergebnisse, auf denen der Plan beruht

**Toolchain (heute gegen Google Maven / Maven Central verifiziert):**

| Komponente | PROJECT.md §4 | Tatsächlich aktuell | Bewertung |
|---|---|---|---|
| AGP | 9.3.1 | 9.3.1 (neuestes) | ✅ korrekt |
| Kotlin | 2.4.10 | 2.4.10 stable (2.4.20-RC unterwegs) | ✅ korrekt |
| Gradle | 9.5.0 | 9.6.1 existiert; 9.5.0 = supported Baseline | ✅ Begründung in §4 stimmt |
| Material 3 stable | 1.4.0 | 1.4.0 | ✅ korrekt |
| M3 Expressive Track | 1.5.0-alpha25 | 1.5.0-alpha26 | ⚠️ minimal veraltet |
| **Compose UI stable** | **1.11.4** | **1.12.0** | ❌ **falsch** |

`compose-bom:2026.08.00` pinnt genau `ui 1.12.0` + `material3 1.4.0` — ein Pin statt zwei.

**Entscheidende API-Prüfung (aus den AAR-Artefakten dekompiliert, nicht aus dem Gedächtnis):**

`material3 1.4.0` **stable** enthält bereits alles, was §6 im Kern verlangt:
- `MaterialExpressiveTheme(colorScheme, motionScheme, shapes, typography, content)` — Signatur identisch zum Beispiel in §6.1
- `MotionScheme.expressive()` (`ExpressiveMotionSchemeImpl`)
- `ShortNavigationBar` (§6.5 kompakte Bottom-Nav) + `WideNavigationRail` (Large Screens)

Nur in `1.5.0-alpha26` zusätzlich: `ButtonGroup`, `FloatingToolbar`, `LoadingIndicator`, `SplitButton`, `FloatingActionButtonMenu`.
→ **Kein Alpha-Override nötig.** Start auf stable, konform zu §2.5.

**Das große Thema — AirPods-Steuerung auf Android:**

Es gibt zwei völlig verschiedene Datenkanäle, und `PROJECT.md` behandelt sie als einen:

1. **BLE-Advertisement** (Apple Proximity Pairing, Manufacturer-ID `0x004C`, Type `0x07`): liefert Akku L/R/Case, Ladezustand, In-Ear, Deckelstatus, Modell. Read-only, **funktioniert auf jedem Android-Gerät ohne Root**.
2. **AAP/AACP über L2CAP PSM `0x1001`** (BR/EDR): ANC, Transparenz, Adaptive, Ear-Detection-Toggle, Stem-Actions, Firmware-Infos — also praktisch alle *schreibenden* Features aus §8.1.

Kanal 2 war auf Android bisher blockiert: Ein Bug im AOSP-Bluetooth-Stack (Fluoride, `l2c_fcr_chk_chan_modes` / ERTM-Channel-Mode-Prüfung) verhindert die BR/EDR-L2CAP-Verbindung zu PSM 4097 — deshalb brauchen LibrePods und CAPod dort Root/Xposed/Magisk.

**Dieser Bug ist in Android 17 gefixt** (Google-Issue 371713238). Ohne Root funktioniert Kanal 2 laut LibrePods-Doku bereits auf Pixel/Android 16 QPR3, ColorOS/OxygenOS 16 und realme UI 7.0 — und ab Android 17 geräteübergreifend.

**Das validiert die Reference-Platform-Entscheidung aus §3.1 nachträglich als das strategisch Entscheidende am Projekt** — aber es kollidiert mit `minSdk 29`: Auf Android 10–15 wird das Kern-Feature-Set (§8.1: ANC, Transparenz, Adaptive) niemals ohne Root laufen, während §8.4 Root explizit ausschließt. Die Spec verspricht dort etwas, das sie an anderer Stelle verbietet.

**Lizenz:** `kavishdevar/librepods` und `d4rken-org/capod` sind beide **GPL-3.0**. Androidpods hat noch keine Lizenz — §25 antizipiert die Entscheidung, trifft sie aber nicht. Sie ist jetzt getroffen (siehe A7).

**Lokale Umgebung:** JDK 25.0.4, Gradle 9.6.1 (System), SDK Platform `android-37.0` ✅, Build-Tools **nur 36.0.0** (37.x fehlt für `compileSdk 37`), AVD `Pixel_10_Pro_XL` vorhanden, **kein physisches Gerät per adb verbunden**. Der Emulator hat kein Bluetooth-Backend für AirPods → alles Hardware-Nahe braucht das echte Telefon.

## Getroffene Entscheidungen

| Frage | Entscheidung | Konsequenz |
|---|---|---|
| Testhardware | **AirPods 4** | Es gibt zwei Varianten (mit/ohne ANC). Der `CapabilityResolver` unterscheidet sie über die Modell-ID — nichts wird angenommen (§9). |
| Testgerät | **Pixel mit Android 17** | **Tier B ist ohne Root erreichbar.** M2b und M3 sind nicht blockiert. Bestmögliches Szenario. |
| minSdk | ~~31~~ **36** *(korrigiert nach M1, s.u.)* | Legacy-Permission-Pfad entfällt (A2); `startObservingDevicePresence(ObservingDevicePresenceRequest)` erzwingt zusätzlich API 36. |
| Lizenz | **GPL-3.0** | LibrePods/CAPod dürfen adaptiert werden, mit Attribution und `NOTICE.md` (A7). Spart in M2b/M3 die meiste Reverse-Engineering-Arbeit. |

---

# Teil A — Änderungen an `PROJECT.md`

> **Korrektur nach Ausführung (M0-Build):** Die These "kein Alpha-Override nötig" unten war
> falsch. Der `assembleDebug`-Build hat gezeigt, dass `MotionScheme`, `MotionScheme.expressive()`
> und `ExperimentalMaterial3ExpressiveApi` in `material3 1.4.0` stable Kotlin-`internal` sind
> (Bytecode-Beweis: `expressive$material3()`, name-mangled) — der frühere Decompile-Check hat
> nur die Bytecode-Signatur gesehen, nicht die Kotlin-Sichtbarkeit. `material3` wird auf
> `1.5.0-alpha26` überschrieben, isoliert hinter `core/designsystem`. `PROJECT.md` §4 ist
> entsprechend korrigiert.

> **Korrektur nach M0-Review:** Der ursprüngliche M0-Verifikationsschritt ("Theme-Screenshot
> zeigt Expressive-Farben + Dynamic-Color-Fallback") war ein Fehlschluss. `AndroidpodsTheme`
> hatte `ExperimentalMaterial3ExpressiveApi` in `HomeScreen.kt` (Feature-Layer) durchgesickert
> statt hinter `core/designsystem` isoliert zu bleiben, `.gitignore`s `/build/`-Zeile hat
> `app/build/` nicht erfasst, und `AndroidpodsFallbackLightColorScheme` setzte nur
> primary/secondary/tertiary — jede nicht gesetzte Rolle (`background`, `secondaryContainer`, …)
> fiel auf Materials Baseline-Lila zurück, wodurch der Fallback-Screenshot optisch identisch zum
> Dynamic-Color-Screenshot aussah, ohne dass das je auffiel. Alle vier behoben: Motion-Accessor
> zurück in `Theme.kt`, `.gitignore` auf `build/` korrigiert, vollständige Farbrollen für Light/
> Dark-Fallback gesetzt, `ShortNavigationBar`/`WideNavigationRail`-Kompilierprobe und
> `src/test/kotlin`-Smoke-Test ergänzt (beide grün, Probe-Datei wieder gelöscht). Siehe
> `[[material3-lightcolorscheme-unset-slots-default-purple]]`.

## A1 — §4: Compose-Baseline korrigieren, BOM einführen

Tabellenzeilen ersetzen:

```markdown
| Compose BOM | 2026.08.00 |
| Compose UI stable line | 1.12.0 (via BOM) |
| Material 3 stable | 1.4.0 (via BOM) |
| Material 3 Expressive track | 1.5.0-alpha26 (opt-in, siehe unten) |
```

Neuer Absatz unter der Tabelle:

```markdown
### Compose BOM as the single pin

Compose artifacts are pinned through `androidx.compose:compose-bom:2026.08.00`, which
resolves `compose-ui 1.12.0` and `compose-material3 1.4.0` as a tested pair. Individual
Compose artifact versions must not be pinned alongside the BOM.

### Material 3 Expressive: stable first

Verified against the published artifacts: `material3 1.4.0` (stable) already provides
`MaterialExpressiveTheme`, `MotionScheme.expressive()`, `ShortNavigationBar` and
`WideNavigationRail`. Everything §6 requires at its core is available on the stable track.

The `1.5.0-alpha` track additionally provides `ButtonGroup`, `FloatingToolbar`,
`LoadingIndicator`, `SplitButton` and `FloatingActionButtonMenu`.

Policy: build on the stable track. Overriding `material3` to the alpha track is allowed
only when a named §6 requirement cannot be met on stable, must be recorded as an ADR, and
must be isolated behind `core/designsystem`.
```

## A2 — §3.1: `minSdk 31` statt 29 *(freigegeben)*

Begründung, die in die Spec gehört: Geräte auf API 29/30 erreichen ohnehin nur die Read-only-Stufe. BLE-Scanning verlangt dort `ACCESS_FINE_LOCATION` **plus aktivierte Standortdienste** — genau das, was §23 ausdrücklich nicht will. `minSdk 31` löscht einen kompletten Legacy-Permission-Pfad (`BLUETOOTH`/`BLUETOOTH_ADMIN`/Location-Kopplung) und kostet nur Geräte, die die Kernfeatures nie bekommen hätten.

```markdown
minSdk = 31
```

```markdown
`minSdk 31` is the compatibility floor because API 31 introduced the `BLUETOOTH_SCAN` /
`BLUETOOTH_CONNECT` runtime permission model with `usesPermissionFlags="neverForLocation"`.
Supporting API 29/30 would require requesting `ACCESS_FINE_LOCATION` and enabled location
services purely to perform a BLE scan, which §23 explicitly rejects — and those devices can
only ever reach the read-only transport tier described in §13.5 anyway.
```

## A3 — §13: neuer Abschnitt "Transport Tiers" *(die wichtigste Ergänzung)*

```markdown
### 13.5 Transport tiers

AirPods expose two independent data channels. Androidpods must treat them separately.

**Tier A — BLE proximity advertisement (read-only, every device)**

Apple manufacturer data (company ID `0x004C`, type `0x07`) carries battery for left/right/case,
charging flags, in-ear state, lid state and a model identifier. No pairing-level access and no
special Android build is required. Tier A alone supports the Home dashboard, widgets, the
battery notification and ear-detection-driven auto-pause.

**Tier B — AAP/AACP over L2CAP PSM 0x1001 (read/write, restricted)**

Apple's accessory protocol runs over a BR/EDR L2CAP channel on PSM `0x1001`. Every write
operation in §8.1 (noise control, ear-detection toggle, stem actions, press behaviour) and all
firmware/capability metadata require this channel.

A defect in the AOSP Bluetooth stack (Fluoride channel-mode negotiation, Google issue
371713238) previously made this channel unreachable for unprivileged apps. The defect is fixed
in Android 17. Tier B is additionally reported to work unrooted on Pixel/Android 16 QPR3,
ColorOS/OxygenOS 16 and realme UI 7.0. On other builds it fails and Androidpods must degrade to
Tier A.

**Tier C — privileged/Apple-spoofing (out of scope)**

Spoofing the Bluetooth Device ID to Apple's vendor ID `0x004C` unlocks further accessory
features but requires root/Xposed. Per §8.4 this is not part of Androidpods.

### 13.6 Tier detection is a probe, not a version check

Tier B availability must never be inferred from `Build.VERSION`, because vendor builds below
Android 17 also support it and vendor builds at the same API level may not.

The capability resolver performs one guarded connection attempt to PSM `0x1001` per known
device and caches the outcome in DataStore, keyed by device address and OS build fingerprint.
The cache is invalidated when the build fingerprint changes.

A failed probe results in Tier A operation plus an honest explanation in the UI
("this Android build does not allow the AirPods control channel"). It must never result in a
control that appears functional and silently does nothing (§2.6).
```

## A4 — §13.3: Scanning-Regel operationalisieren

§13.3 verbietet Dauer-Scanning, sagt aber nicht, wie Tier A dann an Daten kommt. Ergänzen:

```markdown
Tier A requires BLE scanning by nature. It is made event-gated rather than continuous:

1. `CompanionDeviceManager.startObservingDevicePresence()` for the associated device, or the
   A2DP/HFP ACL-connected broadcast, acts as the trigger.
2. The scan uses a `ScanFilter` on manufacturer ID `0x004C` and `SCAN_MODE_LOW_POWER`.
3. The scan stops on audio-device disconnect, on screen-off without playback, and on any
   Androidpods component teardown.

While no associated AirPods device is connected to the audio profile, Androidpods performs no
scanning at all.
```

## A5 — §23: fehlende Permissions ergänzen

```markdown
Concretely required:

- `BLUETOOTH_SCAN` with `android:usesPermissionFlags="neverForLocation"` — Tier A advertisement
  decoding.
- `BLUETOOTH_CONNECT` — device metadata and the Tier B L2CAP channel.
- `POST_NOTIFICATIONS` — connection and battery notifications.
- Companion Device association — presence observation without polling.

Media auto-pause uses `AudioManager.dispatchMediaKeyEvent()`, which requires no permission.
`MediaSessionManager.getActiveSessions()` would require notification-listener access and is
therefore not used for auto-pause; it may only be considered later, opt-in, if auto-resume
demands session-level knowledge.
```

## A6 — §32: Meilenstein 2 aufteilen

Meilenstein 2 setzt implizit Tier B voraus. Akku/In-Ear/Case kommen aber ohne L2CAP aus.

```markdown
### Milestone 2 — Read-only state via AAP session (Tier B)

- L2CAP transport to PSM 0x1001
- tier probe with cached result (§13.6)
- AAP handshake, packet decoding, firmware/model metadata
- battery L/R/case, charging state, wear detection, capability resolution
- Home dashboard
- read-only only: the session must be proven before any configuration command is sent

### Milestone 2b — Advertisement fallback (Tier A)

- BLE advertisement parser feeding the same `AirPodsState`
- event-gated scan lifecycle per §13.4
- serves devices where the Tier B probe fails

Tier B is implemented first because it is the tier the reference hardware supports and because
it carries the project's only genuine technical unknown. Tier A is a documented requirement of
§13.5 but is scheduled once a device that needs it exists.
```

## A7 — §25 / Repo-Root: Lizenz = GPL-3.0 *(entschieden)*

`LICENSE` fehlt bisher. Mit GPL-3.0 dürfen LibrePods und CAPod nicht nur als Referenz gelesen, sondern **adaptiert** werden — das verkürzt M2b/M3 erheblich. Dafür gelten Pflichten, die in die Spec gehören:

```markdown
### Project licence

Androidpods is licensed under GPL-3.0-or-later. `LICENSE` at the repository root is
authoritative.

This makes the existing GPL-3.0 AirPods protocol implementations (LibrePods, CAPod) licence-
compatible sources. Adaptation is therefore permitted, subject to:

- every file containing adapted code carries an SPDX header and an attribution comment naming
  the upstream project, the file, and the commit it was taken from,
- upstream copyright notices are preserved,
- `NOTICE.md` lists every upstream project, its licence and its scope of use,
- adapted code is still reviewed against §11 layering and §30 coding standards rather than
  merged verbatim,
- the resulting binary is distributed under GPL-3.0 including a written offer for source.

Protocol facts — PSM numbers, opcodes, packet field layouts, advertisement byte offsets — are
factual interface information and remain usable regardless of source. Every protocol constant
carries a comment naming the origin of the observation (own capture vs. upstream documentation).
```

§8.4 bleibt davon unberührt: Der VendorID-Spoof-Hook aus LibrePods ist root-basiert und wird **nicht** übernommen.

## A8 — §5: Navigation-3-Erwartung korrigieren

§5 nennt Navigation 3 als "preferred when production-ready". `androidx.navigation3` steht bei `1.2.0-alpha07` — also nicht production-ready.

```markdown
- Navigation: with four top-level destinations, `ShortNavigationBar` /
  `NavigationSuiteScaffold` driven by a sealed destination type is sufficient. Navigation 3
  (`androidx.navigation3`, currently 1.2.0-alpha07) is not production-ready and is not adopted
  in v1. Navigation Compose 2.9.8 is the fallback if a real back-stack requirement appears.
```

### Nicht geändert

§9–§12 (Capability-/State-Modell, Layering), §14–§22, §26–§31, §33–§34 sind tragfähig und bleiben unverändert.

---

# Teil B — Entwicklungsplan

Reihenfolge nach einem Prinzip: **Das echte Produkt auf der echten Hardware zuerst, das größte Unbekannte so früh wie möglich.** Weil Testgerät und Kopfhörer vorhanden sind, ist das Tier B — nicht der Tier-A-Fallback für Geräte, die es aktuell nicht gibt.

## M0 — Bootstrap (bewusst kleiner als §32 vorschlägt)

§12 erlaubt das ausdrücklich: *"These may initially be package boundaries inside a smaller number of Gradle modules."*

- **Ein** `app`-Modul, Package-Grenzen `core.model` / `core.bluetooth` / `core.airpods` / `core.data` / `core.designsystem` / `feature.*` gemäß §12
- `gradle/libs.versions.toml`: AGP 9.3.1, Kotlin 2.4.10, compose-bom 2026.08.00, lifecycle 2.11.0, activity-compose 1.13.0, datastore-preferences 1.2.1, core-ktx 1.19.0. **Kein Glance** — nichts in M0 nutzt es, und `glance-appwidget 1.1.1` ist älter als diese Compose-Linie und kann eine ältere `compose-runtime` hereinziehen. Kommt in M5 dazu, dann mit Auflösungs-Check (`1.3.0-alpha02` als Ausweichkandidat)
- Gradle Wrapper 9.5.0 (überschreibt lokal installiertes 9.6.1) + **Java-Toolchain 21** in der Catalog/Build-Config, damit das lokale JDK 25 irrelevant ist
- Build-Tools 37.x über `sdkmanager` nachinstallieren (lokal nur 36.0.0)
- `core/designsystem`: `AndroidpodsTheme` kapselt `MaterialExpressiveTheme` + `MotionScheme.expressive()` + Dynamic Color mit Fallback-Schema (§6.2); **kein** M3-Import außerhalb dieser Schicht für experimentelle APIs
- Eine Compose-Screen: Home im Zustand *disconnected* — beweist Theme, Motion, Edge-to-Edge, Predictive Back
- `LICENSE` (GPL-3.0), `NOTICE.md` (Upstream-Attribution) und `README.md` anlegen; SPDX-Header-Konvention festlegen
- **Verzicht (Trigger zum Nachholen):** build-logic-Convention-Plugins (→ ab dem 2. Gradle-Modul), 6-Modul-Split (→ wenn Build-Zeit oder Ownership es fordert), Hilt (→ beim ersten injizierten Entry-Point: Widget, Tile, Receiver oder Service), CI (→ sobald es Tests gibt, die grün bleiben sollen)

## M1 — Bluetooth-Fundament

- Permission-Flow für `BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT`/`POST_NOTIFICATIONS` mit Nutzen-Begründung (§23)
- Bluetooth-Verfügbarkeitszustand als `StateFlow`
- Onboarding: `CompanionDeviceManager.associate()` mit AirPods-Filter — ersetzt freie Discovery (§13.3)
- `startObservingDevicePresence()` + ACL-Broadcast als Aufwach-Trigger
- Transport-Abstraktion mit zwei Implementierungen hinter einem Interface: `AdvertisementSource` (Tier A, erst M2b) und `AapTransport` (Tier B, erst M2)
- Debug-Logging opt-in (§27), Raw-Pakete standardmäßig aus

> **Korrektur nach M1-Umsetzung:** `CompanionDeviceService.onDevicePresenceEvent(DevicePresenceEvent)`
> (API 36, nicht deprecated — per `api-versions.xml` verifiziert, nicht 37 wie hier ursprünglich
> notiert) liefert `EVENT_BLE_APPEARED`/`EVENT_BLE_DISAPPEARED` (Tier-A-BLE-Nähe)
> und `EVENT_BT_CONNECTED`/`EVENT_BT_DISCONNECTED` (Tier-B-ACL-Signal) bereits in einem Event-Stream
> — ein separater `BroadcastReceiver` auf ein ACL-Connected-Intent ist überflüssig, `M1` nutzt nur
> diese eine Callback-Methode. Die ursprünglich hier angelegte `AirPodsPresence`-Singleton-State
> (`object` mit `MutableStateFlow<Boolean>`) wurde nach Review wieder entfernt: sie war eine zweite,
> unsynchronisierte State-Machine neben der in §10 verlangten `AirPodsState` und hätte bei totem
> App-Prozess (Service läuft, Activity nicht) unbemerkt auf `false` zurückgesetzt. `M2`s
> `AirPodsRepository` übernimmt `onDevicePresenceEvent` direkt und wird die einzige Quelle für
> Nearby/Connected-State. `startObservingDevicePresence()`-Aufrufe sind in try/catch gekapselt
> (`observePresenceQuietly`), damit ein Registrierungsfehler weder den Association-Callback noch
> `Application.onCreate` crasht (§2.6).
>
> Nur auf dem Emulator verifiziert (kein physisches Pixel per `adb` verbunden): Permission-Flow,
> CDM-Namefilter (`AirPods.*` im Logcat bestätigt), Cancel-Pfad. **Nicht verifiziert:**
> `onAssociationCreated` → `startObservingDevicePresence()` (der Erfolgspfad, da die Association nie
> abgeschlossen wurde) und alles Tier-B-Nahe. Das bleibt offen bis zum echten Pixel + AirPods 4.
>
> **Korrektur nach `./gradlew lint`:** Der erste Lint-Lauf dieser Session (bisher lief nur
> `compileDebugKotlin`/`assembleDebug`/`test`, keiner davon prüft API-Level gegen `minSdk`) fand
> 10 `NewApi`-Fehler in `AirPodsAssociationManager.kt`: `associate(request, executor, callback)`
> und `AssociationInfo#getId()`/`CompanionDeviceManager#getMyAssociations()` verlangen API 33,
> `startObservingDevicePresence(ObservingDevicePresenceRequest)` verlangt API 36 — alle unguarded
> gegen `minSdk 31`. Experiment statt Vermutung: `minSdk 33` gesetzt → 4 Fehler bleiben (nur die
> Presence-Observation-Kette); `minSdk 36` gesetzt → 0 Fehler. Da Tier B ohnehin erst ab Android 16
> QPR3 erreichbar ist (§13.5) und für API 31-35 kein Testgerät/Emulator existiert, wäre jede
> Versions-Gate-Fallback-Kette für diesen Bereich ungetesteter Code ohne einen einzigen
> funktionierenden Feature-Gewinn. Nutzer hat `minSdk 36` freigegeben; `PROJECT.md` §3.1/A2
> entsprechend korrigiert. Keine Version-Gates in `AirPodsAssociationManager.kt` nötig — der Code
> bleibt wie geschrieben.
>
> Nebenbei behoben (gleicher Lint-Lauf): `OnboardingScreen.kt` rief `context.getString(...)` direkt
> im `associationLauncher`-Callback auf (`LocalContextGetResourceValueCall`, Error-Severity) —
> jetzt `stringResource(...)` einmal oben im Composable gehoben.

## M2 — AAP-Session, nur lesend (Tier B)

- Tier-Probe (§13.6 neu) mit DataStore-Cache — auf dem Zielgerät voraussichtlich immer erfolgreich, für jedes andere Gerät der einzige ehrliche Weg
- L2CAP-Transport zu PSM `0x1001`, AAP-Handshake
- **Zuerst echte Bytes aufzeichnen, dann parsen.** Ein Capture von den eigenen AirPods 4 ist die Fixture. Synthetische Pakete beweisen nur, dass der Parser meiner Annahme über das Byte-Layout entspricht — sie bestehen genau dann, wenn die Annahme falsch ist. Synthetisch bleiben nur die Fälle, wo Synthese der Punkt ist: Malformed-Input, unbekannte Felder, abgeschnittene Pakete (§28)
- Referenzquelle: LibrePods (`AAP Definitions.md` + Android-Implementierung), GPL-3.0-konform adaptiert mit Attribution je Datei — Feldbedeutungen von dort, Fixtures aus eigenem Capture
- Decoder → Akku L/R/Case, Charging, Wear-State, Firmware-/Modell-Metadaten
- `CapabilityResolver`: Modell-ID → `AirPodsCapabilities`, Unbekannt ⇒ nicht unterstützt (§9)
- `AirPodsRepository` + `AirPodsState`-`StateFlow` (§10)
- Home-Dashboard mit echter Anzeige, expressive Motion bei Akku-/In-Ear-Wechsel
- Auto-Pause via `AudioManager.dispatchMediaKeyEvent()` bei Wear-State-Wechsel, Auto-Resume optional (§16)
- **Kein Schreibbefehl in M2.** Erst wenn die Session gegen die echten AirPods 4 bestätigt ist (§2.6, §33)
- UI zeigt bei fehlgeschlagener Probe eine ehrliche Erklärung statt toter Controls
- ADR: Tier-Modell und Probe-Strategie (`architecture-decision-records`)

> **Korrektur nach Tier-B-Hardware-Probe (echtes Pixel 9 Pro XL, Android 17):** M1 wurde
> nachträglich auf dem echten Gerät verifiziert — `dumpsys companiondevice` zeigt eine aktive,
> nicht-pending, nicht-revoked Association (`mId=3`) mit laufendem Presence-Tracking für die
> AirPods. Der oben offen gelassene M1-Erfolgspfad ist damit geschlossen.
>
> Die zentrale Tier-B-These dieses Plans — "ohne Root erreichbar ab Android 17" — ist dagegen
> nach einer echten Probe **so nicht haltbar**. `L2capTierBProbeTest.probeAapChannel()` hat
> `BluetoothDevice#createL2capChannel(0x1001)` und `#createInsecureL2capChannel(0x1001)` gegen
> die gebondeten AirPods 4 aufgerufen. Beide scheitern sofort mit `IOException: read failed,
> socket might closed or timeout, read ret: -1`; der native Bluetooth-Stack-Log zeigt in jedem
> Fall `GAP_ConnOpen: Failure registering PSM 0x...., is_le: true` — für PSM `0x1001` genauso wie
> für eine PSM-unabhängige Kontrollmessung mit `0x1003` (schließt eine PSM-spezifische Kollision,
> z.B. mit AVRCP/BIP, aus), für secure und insecure gleichermaßen. `is_le: true` ist der
> entscheidende Befund: Androids öffentliche `createL2capChannel`/`createInsecureL2capChannel`-API
> (seit API 29) implementiert **LE L2CAP Connection-oriented Channels**, nicht klassische BR/EDR-
> Fixed-Channel-Verbindungen. Das ist strukturell die falsche API für einen BR/EDR-Zubehör-Kanal
> wie AAP — unabhängig vom AOSP-Fluoride-Bugfix in Android 17, den dieser Plan als Begründung für
> "root-frei" angenommen hatte.
>
> Recherche gegen LibrePods' tatsächliche Implementierung (GPL-3.0, zulässig per A7) bestätigt:
> Ihr Android-Pfad zum AAP-Kanal läuft über ein **root-voraussetzendes Magisk-Modul**
> (`btl2capfix.zip`), das `libbluetooth_jni.so` nativ per `dlsym`-Offset-Suche patcht (PR #449,
> "Auto-detect L2CAP function offsets via dlsym for Android") — nicht über einen einfachen
> öffentlichen oder reflektierten Java-Aufruf. Ein damit verwandter Ansatz aus dem CAPod-Umfeld
> reflektiert stattdessen auf die **nicht-öffentliche** Methode
> `BluetoothDevice#createInsecureL2capSocket(int)` (Name "Socket", nicht "Channel" — eine andere,
> ältere Methode als die zwei oben getesteten öffentlichen), die laut Androids Hidden-API-Liste
> als *denied* markiert ist und laut CAPod-Issue-Thread `AndroidHiddenApiBypass` braucht, um
> überhaupt auszuführen.
>
> **Update nach `probeHiddenL2capSocket`-Lauf:** `HiddenApiBypass.invoke(BluetoothDevice::class,
> device, "createInsecureL2capSocket", 0x1001)` läuft ohne Root durch und liefert ein reales
> `BluetoothSocket`. Der native Log zeigt `L2CA_Register: L2CAP Registered service classic PSM:
> 0x1001` — die klassische BR/EDR-Registrierung gelingt, ganz anders als die sofortige
> API-Ablehnung (`is_le: true`) der öffentlichen Methode oben. Das ist der entscheidende
> Unterschied: die Hidden-API landet strukturell auf dem richtigen Transport.
>
> Der eine abgeschlossene Testlauf scheiterte trotzdem an `connect()`
> (`BluetoothSocketException: ACL connection failed`, nativ `PAGE_TIMEOUT(0x04)`,
> `is_le: false`). `dumpsys bluetooth_manager` erklärt das: Die AirPods hatten zu dem Zeitpunkt
> gar keinen aktiven BR/EDR-ACL (`[ACL BR/EDR:N LE:N]`, letzter Disconnect ~1.5s vor Testbeginn).
> Ein Page-Timeout bei inaktivem Link ist ein Radio-Level-Timing-Effekt, kein API-Fehler — die
> AirPods schlafen ihren klassischen Funk ein, wenn kein Audio läuft, und müssen erst neu gepaged
> werden.
>
> Der Probe-Test hat jetzt einen bounded Retry (3 Versuche, 2s Abstand je PSM) bekommen, damit ein
> einzelner Page-Timeout nicht als hartes Scheitern fehlinterpretiert wird. **Das ist zugleich ein
> echter Spec-Defekt, den diese Probe aufgedeckt hat:** §13.6s Ein-Schuss-Probe mit Cache würde für
> ein durchaus fähiges Gerät dauerhaft "Tier B nicht unterstützt" cachen, nur weil die AirPods im
> Moment der Probe gerade keinen aktiven Link hatten. Die Probe-Strategie muss (a) einen aktiven
> ACL als Vorbedingung prüfen/erzwingen (z.B. über eine kurze A2DP-Verbindung) und (b)
> `PAGE_TIMEOUT` als *inconclusive* behandeln (später erneut versuchen), nicht als negatives
> Ergebnis. Wird bei der `CapabilityResolver`-Implementierung in M2 berücksichtigt, noch nicht
> umgesetzt.
>
> **Zur Nutzer-Entscheidung (§8.4):** Hidden-API-Reflection über `HiddenApiBypass` ist weder Root
> noch eine Stack-Modifikation — §8.4 verbietet explizit nur "root/Xposed" und "unsafe Bluetooth
> stack modifications". Der Mechanismus exemptet nur den eigenen Prozess von Androids
> Non-SDK-Interface-Enforcement per JNI-Aufruf in `libart.so`, ohne System- oder Fremdprozess-
> Modifikation. Das einzige reale Risiko ist Haltbarkeit über Android-Versionen/OEMs hinweg — genau
> das Problem, für das §13.6s Laufzeit-Probe mit Cache bereits existiert. Ein
> `Unsupported class loader: CoreOjClassLoader`-Warning trat zweimal auf, war aber nicht fatal:
> `invoke()` lief trotzdem durch.
>
> **Konklusiver Retry (echter AirPods-Link, `[ACL BR/EDR:Y]` per `dumpsys bluetooth_manager`
> bestätigt vor dem Testlauf):** `probeHiddenL2capSocket` verbindet. Nativer Log lückenlos:
> `L2CA_Register: L2CAP Registered service classic PSM: 0x1001` →
> `notify_app_connected: Connected to L2CAP connection for device: xx:xx:xx:xx:84:88, channel:
> 4097, is_le: false, rx_mtu: 8087`, App-seitig `CONNECTED isConnected=true`. **Tier B ist ohne
> Root erreichbar, bestätigt auf echter Hardware (Pixel 9 Pro XL, Android 17, AirPods 4).** Der
> vorherige Fehlschlag war ausschließlich die fehlende ACL-Vorbedingung, kein API- oder
> Berechtigungsproblem. §13.5/§13.6 brauchen trotzdem die oben notierte Korrektur: der Mechanismus
> ist `createInsecureL2capSocket(int)` per `HiddenApiBypass`-Reflection, nicht "kein besonderer
> Mechanismus", und die Probe-Strategie muss die ACL-Vorbedingung + Inconclusive-statt-Negativ-
> Behandlung von `PAGE_TIMEOUT` berücksichtigen (s. oben). M2 kann mit L2CAP-Transport + AAP-
> Handshake fortgesetzt werden.

**Checkpoint am Ende von M2:** Modell-ID auslesen und die AirPods-4-Variante bestimmen. Ist es die Variante *ohne* ANC, gibt es kein Noise-Control-Write, das nach §33 gegen Hardware validiert werden könnte — dann ist M3s Validierungsziel Ear-Detection und Stem-Actions statt Noise Control. Vorher blockiert das nichts.

## M2b — Advertisement-Fallback (Tier A) · zurückgestellt

Parser für `0x004C`/Type `0x07` → dieselben Felder, in denselben `AirPodsState`, plus die event-gated Scan-Lifecycle aus A4.

**Trigger zum Nachholen** *(korrigiert nach `minSdk 36`: ein Gerät mit Android ≤ 15 kann die App gar nicht mehr installieren, damit entfällt dieser Trigger ersatzlos)*: ein API-36+-Vendor-Build, dessen Tier-B-Probe fehlschlägt, oder der erste externe Nutzer in dieser Lage. Vorher hat Tier A null Konsumenten, kostet aber Scan-Lifecycle, CDM-Presence-Observation, Scan-Filter und BLE-Permission-UX. §13.5 dokumentiert die Stufe trotzdem — die Spec beschreibt den ehrlichen Abbau, der Code holt ihn nach, wenn es jemanden gibt, der ihn braucht.

## M3 — Schreibende Controls

Noise Control (Off/Transparenz/Adaptive/ANC — welche davon gültig sind, entscheidet der Capability-Resolver anhand der in M2 bestimmten AirPods-4-Variante) und Ear-Detection-Toggle. Jeder Write mit Read-back-Bestätigung, Timeout- und Reject-Behandlung. Freigabe erst nach Verifikation gegen die echten AirPods (§33).

## M4–M7

Wie in §32. Widgets (M5) und Quick Settings hängen nur am `AirPodsState` und sind unabhängig von der Tier-Frage.

---

## Skills-Roster

| Phase | Skill | Wozu |
|---|---|---|
| jetzt | `material-3` (repo-lokal) | Expressive-Theming, Nav-Patterns, Farb-/Shape-System |
| A (Spec-Patch) | `architecture-decision-records` | zwei echte ADRs: Transport-Tier-Modell, Lizenzentscheidung |
| M0–M1 | `android-clean-architecture`, `kotlin-patterns` | Layering nach §11, idiomatisches Kotlin nach §30 |
| M1–M2 | `kotlin-coroutines-flows` | `StateFlow`-Store, Transport-Lifecycle, kein `GlobalScope` |
| M2 | `superpowers:test-driven-development`, `kotlin-testing` | Protokoll-Decoder gegen aufgezeichnete Fixtures — der Ort, an dem TDD hier real bezahlt |
| M0+ | `gradle-build` / `kotlin-build` | Version-Catalog, Toolchain, Build-Fehler |
| Abschluss je Meilenstein | `kotlin-review`, `superpowers:verification-before-completion` | §33 Definition of Done |

---

## Verifikation

- **M0:** `./gradlew assembleDebug` grün; App startet im Emulator `Pixel_10_Pro_XL`; Theme-Screenshot zeigt Expressive-Farben + Dynamic-Color-Fallback
  - Die Java-Toolchain 21 steuert nur die Kompilierung, nicht die Daemon-JVM — der Wrapper startet auf dem lokalen JDK 25. Lehnt AGP 9.3.1 diesen Daemon ab, schlägt `assembleDebug` trotz korrekter Toolchain fehl. Dann `org.gradle.java.home` in `gradle.properties` auf ein JDK 21 setzen. Das ist der wahrscheinlichste Weg, wie M0 verwirrend scheitert.
- **M2:** Tier-Probe auf dem Pixel protokolliert Erfolg; `./gradlew test` — Decoder gegen die aufgezeichneten Fixtures grün, inkl. Malformed-Input; Akkuwerte auf dem Pixel gegen die Anzeige eines Apple-Geräts bzw. gegen CAPod gegenprüfen; Fehlschlag-Pfad künstlich erzwingen (Probe-Ergebnis im DataStore manipulieren), um die Tier-A-Erklärung in der UI zu verifizieren
- **M3:** jeder Write mit anschließendem Read-back gegen die echten AirPods 4; ohne diesen Nachweis bleibt das Feature hinter einem Experimental-Flag
- **Batterie (§14):** `dumpsys batterystats` bzw. Battery Historian über 12 h mit getrennten AirPods → Ziel: kein messbarer Androidpods-Anteil

Testmatrix-Startzeile (§28): AirPods 4 (Variante per Modell-ID zu bestimmen) · Pixel · Android 17 · Tier B erwartet.

Ein API-36+-Gerät mit fehlschlagender Tier-B-Probe ist der Trigger für M2b (korrigiert nach `minSdk 36`, s. M2b-Abschnitt oben) — erst damit lässt sich der Tier-A-Fallback echt verifizieren statt nur simuliert. Kein Blocker — sag Bescheid, falls ein solcher Fall auftritt, dann wandert M2b nach vorn.
