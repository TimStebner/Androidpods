# Roadmap / Session Handoff

Living doc, not history — overwrite next session, don't append.

## This session

- Built M5, the battery home-screen widget (`feature/widgets`), the one hardware-independent path identified last session:
  - `BatteryWidgetContent.kt`: pure `AirPodsState.toBatteryWidgetUiState()` mapping (`NoData` / `Battery(left, right, case)`) — TDD, RED→GREEN. This is the only part of the widget that's unit-tested; the Glance composable and the update-push function are framework types, same precedent as `AapTransport`/`observeAutoPause`.
  - `BatteryWidget.kt` (`GlanceAppWidget`) + `BatteryWidgetReceiver.kt`: renders `BatteryWidgetUiState` via `GlanceTheme` (not `MaterialExpressiveTheme` — Glance has its own composition, can't use Compose Material3 there). `provideGlance` uses `AirPodsRepositoryProvider.state.collectAsState()` *inside* `provideContent`'s composable lambda, so a live widget session recomposes on every state change. (First draft read `state.first()` outside `provideContent` and froze on first render forever — caught before commit, see below.)
  - `WidgetUpdates.kt`: `observeWidgetUpdates(context, states, scope)`, wired from `AndroidpodsApp.onCreate()` (same shape as `observeAutoPause`). Pushes `BatteryWidget().updateAll(context)` on distinct widget-relevant state changes — this is the complementary path for when *no* Glance session exists yet (process revived without a host asking); `updateAll()` starts one, which is what makes `provideGlance` run again. Lives in `feature/widgets`, not `core/data`, so `AirPodsRepositoryProvider` doesn't import presentation code (§11).
  - `res/xml/battery_widget_info.xml`: `updatePeriodMillis="0"` — the widget is push-updated, never host-timer-polled (§13.4/§14).
  - Added `glance-appwidget:1.1.1` to the version catalog. Did the resolution check the plan demanded first: `./gradlew :app:dependencies --configuration debugRuntimeClasspath` shows Gradle resolving its transitive `compose-runtime` (1.7.0/1.9.0/1.11.0 requested) up to the BOM's `1.12.0` cleanly — no downgrade, no conflict, so `1.1.1` stays; the `1.3.0-alpha02` fallback wasn't needed.
  - No Hilt (manual `BatteryWidget()`/`BatteryWidgetReceiver()` construction, same precedent as `AirPodsRepositoryProvider`). No noise-control widget or Quick Settings tile — both are write surfaces, still blocked with M3.
- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` all green. Lint: same 3 pre-existing informational findings as before (AGP version nag, `allowBackup` deprecated, missing app icon) — nothing new from the widget.
- **Not verified (needs hardware, explicitly listed so it isn't silently claimed as done):** that a battery change re-renders a *live* widget session (pinned to home screen, app/state observed, no process kill) via `collectAsState`, and separately that the `observeWidgetUpdates` → `updateAll` push revives a widget whose Glance session died with the process. These are two different code paths (see fix above) — both need checking, the live-session one is the common case and is primary. Compiling, the unit test, and lint all pass regardless of whether either works; only a real device run distinguishes a live-updating widget from one frozen at first render. Also unverified: real device rendering/sizing of the widget itself (only compiled + lint-checked, never run), and the `minSdk 36` widget-picker preview (no `providePreview`/`previewImage` — falls back to `glance_default_loading_layout`, likely a blank picker tile; cosmetic, not built now).
- Commits: pending (this session's changes not yet committed — see below).

## How

TDD for the one pure/testable seam (`superpowers:test-driven-development`, RED confirmed via actual compile failure — `Unresolved reference` — before GREEN). Framework-touching code (`BatteryWidget`, `BatteryWidgetReceiver`, `observeWidgetUpdates`) intentionally not unit-tested, per the same precedent `DataStoreTierProbeCache`/`AapTransport` already set last session. Glance API calls (`GlanceModifier`, `TextStyle`, `updateAll`, `GlanceAppWidgetReceiver`) were verified against real androidx source/API-diff docs (context7 `/androidx/androidx`) before writing them, not assumed from memory — first draft had wrong types (a hand-rolled `.dp()` helper, wrapping already-`ColorProvider` values in `ColorProvider(...)` again) and was rewritten after checking. A second, more significant bug (`provideGlance` reading `state.first()` outside `provideContent`, freezing the widget on first render — see above) was caught by a design review before commit, not by the test suite; `collectAsState()` inside the composable lambda was confirmed as the correct pattern via context7 before applying the fix.

## Plan state (`~/.claude/plans/bitte-lies-dir-einmal-smooth-meerkat.md`)

- **M0–M2**: code-complete except two items that need real hardware: (a) read the model ID / confirm the AirPods 4 variant, (b) the PAGE_TIMEOUT-inconclusive gap in `AapTransport.connect()` (documented in ADR-0001, not yet fixed — see prior sessions).
- **M2b** (Tier A advertisement fallback): deliberately deferred; unreachable now that `minSdk` effectively requires Android 16+.
- **M3** (write controls: ANC/Transparency/Adaptive, ear-detection toggle, etc.): **hard-gated** — no write command before the M2 session is hardware-confirmed (§2.6/§33). Still not started, still shouldn't be, regardless of framing.
- **M5** (battery widget): code-complete, **hardware-unverified** (see above). No config activity, no resize-driven layout variants — out of scope for a first pass, add if real-device use shows they're needed.
- **M4, M6–M7**: not started.

## Next steps (proposed, not started)

1. Real-hardware pass (Pixel 9 Pro XL + AirPods 4) — still the standing next step whenever hardware is available:
   - Confirm AirPods 4 variant via model ID.
   - Manually force the DataStore probe result to verify the Tier A UI explanation (M2's own prescribed verification step).
   - Check whether native logcat (`GAP_ConnOpen`) can realistically be piped into `AapTransport` to fix the PAGE_TIMEOUT-inconclusive gap; if not feasible, keep it documented as-is.
   - **New this session:** add two widget checks to this pass, in this order:
     1. **Live-session check (primary):** pin a `BatteryWidget` to the home screen with the app process alive, then change a battery reading (unplug/replug a bud or the case) and confirm the number updates *without* killing anything. This is the common case and the one the `collectAsState` fix targets — the buggy first draft would have failed this exact check while still passing the cold-process one below.
     2. **Cold-process check (secondary):** kill the app process, trigger a presence-service reconnect (not opening the app), and confirm the widget updates via `observeWidgetUpdates` → `updateAll`.
2. If continuing without hardware access: M5 was the only unblocked path and it's now done. What's left that's genuinely hardware-independent is thin:
   - Quick Settings tile is explicitly out (write surface, blocked with M3).
   - M6/M7 not yet scoped in the plan file beyond "per §32" — would need a proper look at §32 before starting either, not just picked up cold.
   - Realistically: nothing large is left to build without hardware. Worth checking with the user before inventing scope.
3. Do not start M3 under any framing until step 1 is actually done.

## Notes for other AI assistants picking this up

- Read `CLAUDE.md` and `PROJECT.md` (esp. §11 layering, §13.6, §33) before touching anything — they override defaults.
- `DataStoreTierProbeCache`, `AapTransport`, `BatteryWidget`, `BatteryWidgetReceiver`, and `observeWidgetUpdates` are all deliberately *not* unit-tested (framework types); don't add JVM tests for them, verify on hardware instead. `BatteryWidgetContent.kt`'s pure mapping function is the one part of the widget that is tested — keep new widget logic on that side of the seam where possible.
- Real MAC addresses/serials are still unredacted in `app/src/test/resources/fixtures/aap/session-start-capture.txt` — intentional for now, **must be scrubbed before any MVP security pass / public release** (§28). Don't scrub preemptively without being asked; it's tracked, not forgotten.
- `docs/adr/0001-tier-b-hidden-l2cap-socket.md` is the live decision record for the Tier B transport — keep it in sync with `TierProbeCache`/`AapTransport` if either changes.
- No app launcher icon exists yet (`MissingApplicationIcon` lint finding, pre-existing, not part of this session) — not blocking, just noted so it isn't mistaken for a widget-related regression.
- `.idea/misc.xml` shows modified in git status in some sessions — pre-existing IDE-local diff, not part of any commit.
