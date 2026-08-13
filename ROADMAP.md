# Roadmap / Session Handoff

Living doc, not history — overwrite next session, don't append.

## This session

- **M5 widget fix (committed as `371f343`):** a design review before commit caught that `BatteryWidget.provideGlance` read `AirPodsRepositoryProvider.state.first()` *outside* `provideContent`'s composable lambda, which freezes a live widget session on its first render forever (`updateAll()` on an already-alive session recomposes, it doesn't re-run `provideGlance`). Fixed with `AirPodsRepositoryProvider.state.collectAsState()` *inside* the composable. `observeWidgetUpdates`/`updateAll()` stays as the complementary path for reviving a *dead* Glance session (process killed, no host asking) — both paths are needed, not either/or. Two backwards code comments were corrected to match.
- **New: M5 battery notification (`feature/notifications`)**, the other item under "Android integration → notifications" (§19/§32), same hardware-independent shape as the widget:
  - `BatteryNotificationContent.kt`: pure `AirPodsState.toBatteryNotificationUiState()` mapping to `BatteryNotificationUiState(left, right, case)?` — TDD, RED→GREEN. Returns `null` on disconnect (not just "no battery yet"), which the observer must treat as *cancel*, not skip — otherwise a disconnect leaves a stale ongoing notification showing the last-known percentages forever. This is the one part of the notification that's unit-tested.
  - `BatteryNotification.kt`: `ensureBatteryNotificationChannel` (`IMPORTANCE_LOW` + `setOnlyAlertOnce(true)` — battery state changes fire often, `IMPORTANCE_DEFAULT` would buzz the phone on every one) and `updateBatteryNotification(context, state)`, which cancels (not skips) on `null` state *or* on missing `POST_NOTIFICATIONS` grant. Framework type, not unit-tested, same precedent as `BatteryWidget`. **No `setOngoing(true)`:** since Android 14, that flag is only honored for a foreground-service or media/call notification, neither of which this app runs (§14) — first draft included it, a design review before commit caught that it would silently do nothing on a plain `notify()` at `targetSdk 37`, and it was removed; the notification is user-dismissible by design, not by oversight.
  - `NotificationUpdates.kt`: `observeBatteryNotifications(context, states, scope)`, wired from `AndroidpodsApp.onCreate()` alongside `observeAutoPause`/`observeWidgetUpdates`. Lives in `feature/notifications`, not `core/data` (§11). Also exports `refreshBatteryNotification(context)` — see the permission-grant fix below.
  - `core.bluetooth.RequiredPermissions.hasNotificationPermission`: **not** merged into `REQUIRED_BLUETOOTH_PERMISSIONS` — must not gate onboarding completion, and denying it must not block using the app (§23).
  - Permission request wired into `HomeScreen` (not `MainActivity`/onboarding): a runtime permission needs a foreground Activity to request from, and `HomeScreen` is the one screen guaranteed to run once a device is set up, whether or not the notification feature itself is visible there. `AndroidManifest.xml`'s `POST_NOTIFICATIONS` comment updated to point at this instead of the old "M5, not yet built" note. **Permission-grant re-push fix:** `observeBatteryNotifications` only re-posts on a battery *state* change; on first launch the observer's first emission (permission not yet granted) is remembered by `distinctUntilChanged`, so the grant itself doesn't trigger a state change and the notification would otherwise only appear at the next battery reading, possibly minutes later. Caught before commit — `HomeScreen`'s permission-launcher callback now calls `refreshBatteryNotification(context)` on grant, which re-reads current state and pushes immediately.
  - `res/drawable/ic_notification.xml`: a plain circle vector, explicitly marked `ponytail:` placeholder (same precedent as `HomeScreen`'s text glyph) — notification `setSmallIcon` is mandatory and there was no existing icon asset in the project to reuse (no launcher icon exists yet either, see below).
- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` all green after both the widget fix and the notification feature. Lint: same 3 pre-existing informational findings as before (AGP version nag, `allowBackup` deprecated, missing app icon) — nothing new.
- **Not verified (needs hardware, explicitly listed so it isn't silently claimed as done):**
  - Widget: that a battery change re-renders a *live* widget session (pinned to home screen, app/state observed, no process kill) via `collectAsState`, and separately that `observeWidgetUpdates` → `updateAll` revives a widget whose Glance session died with the process. Two different code paths — both need checking, live-session is primary (see Next steps).
  - Widget: real device rendering/sizing (only compiled + lint-checked, never run), and the widget-picker preview (no `providePreview`/`previewImage` — falls back to `glance_default_loading_layout`, likely a blank picker tile; cosmetic, not built now).
  - Notification: that the permission prompt actually appears from `HomeScreen`, that the notification posts/updates/cancels correctly across connect → battery change → disconnect on a real device, and that `IMPORTANCE_LOW` behaves as expected (no HUN/sound, still visible in the shade).
- Commits: M5 widget fix committed as `371f343`; notification feature pending commit — see below.

## How

TDD for the one pure/testable seam in each feature (`superpowers:test-driven-development`, RED confirmed via actual compile failure before GREEN, same as last session). Framework-touching code (`BatteryWidget`, `BatteryWidgetReceiver`, `observeWidgetUpdates`, `BatteryNotification`, `observeBatteryNotifications`) intentionally not unit-tested, per the `DataStoreTierProbeCache`/`AapTransport` precedent. Both bugs caught this session (`provideGlance`'s frozen-render bug, and the initial `BatteryNotificationUiState.Battery` sealed-interface smart-cast compile error) were caught before/during build, not invented scope — the sealed interface was simplified to a single data class once it became clear a one-member sealed type added nothing here (ponytail: no abstraction beyond what's needed).

## Plan state (`~/.claude/plans/bitte-lies-dir-einmal-smooth-meerkat.md`)

- **M0–M2**: code-complete except two items that need real hardware: (a) read the model ID / confirm the AirPods 4 variant, (b) the PAGE_TIMEOUT-inconclusive gap in `AapTransport.connect()` (documented in ADR-0001, not yet fixed — see prior sessions).
- **M2b** (Tier A advertisement fallback): deliberately deferred; unreachable now that `minSdk` effectively requires Android 16+.
- **M3** (write controls: ANC/Transparency/Adaptive, ear-detection toggle, etc.): **hard-gated** — no write command before the M2 session is hardware-confirmed (§2.6/§33). Still not started, still shouldn't be, regardless of framing. User has explicitly excluded M3 from current scope.
- **M5** (Android integration): battery widget and battery notification are code-complete, **hardware-unverified** (see above). Noise-control widget, combined widget, and Quick Settings tile(s) remain explicitly out — all are write surfaces, blocked with M3. "Connection experience" (§19 foreground surface — device illustration, connection animation) not started.
- **M4, M6–M7**: not started. M4 (button/stem settings, press speed, assistant triggering, head gestures) is mostly device-setting writes and is likely M3-adjacent — needs a real look at §16/§32 before picking anything from it, not assumed clear just because it's a different milestone number.

## Next steps (proposed, not started)

1. Real-hardware pass (Pixel 9 Pro XL + AirPods 4) — still the standing next step whenever hardware is available, **user will signal readiness separately, do not start proactively**:
   - Confirm AirPods 4 variant via model ID.
   - Manually force the DataStore probe result to verify the Tier A UI explanation (M2's own prescribed verification step).
   - Check whether native logcat (`GAP_ConnOpen`) can realistically be piped into `AapTransport` to fix the PAGE_TIMEOUT-inconclusive gap; if not feasible, keep it documented as-is.
   - Widget checks, in this order:
     1. **Live-session check (primary):** pin a `BatteryWidget` to the home screen with the app process alive, change a battery reading, confirm the number updates *without* killing anything.
     2. **Cold-process check (secondary):** kill the app process, trigger a presence-service reconnect (not opening the app), confirm the widget updates via `observeWidgetUpdates` → `updateAll`.
   - Notification checks: grant prompt appears from `HomeScreen`; notification posts on connect, updates on battery change, cancels on disconnect; `IMPORTANCE_LOW` behavior is as expected.
2. If continuing without hardware access, excluding M3: the remaining thin slice is `PROJECT.md` §19's "connection experience" foreground surface (device illustration, connection state, short connection animation) — the rest of unblocked M5 (battery widget, battery notification) is now done. Worth confirming scope with the user before starting a UI/motion-heavy piece cold rather than assuming it.
3. Do not start M3 under any framing — user has explicitly excluded it from current scope, independent of the hardware-pass gate.

## Notes for other AI assistants picking this up

- Read `CLAUDE.md` and `PROJECT.md` (esp. §11 layering, §13.6, §33) before touching anything — they override defaults.
- `DataStoreTierProbeCache`, `AapTransport`, `BatteryWidget`, `BatteryWidgetReceiver`, `observeWidgetUpdates`, `BatteryNotification`, `observeBatteryNotifications` are all deliberately *not* unit-tested (framework types); don't add JVM tests for them, verify on hardware instead. `BatteryWidgetContent.kt`/`BatteryNotificationContent.kt`'s pure mapping functions are the tested seams — keep new widget/notification logic on that side where possible.
- Real MAC addresses/serials are still unredacted in `app/src/test/resources/fixtures/aap/session-start-capture.txt` — intentional for now, **must be scrubbed before any MVP security pass / public release** (§28). Don't scrub preemptively without being asked; it's tracked, not forgotten.
- `docs/adr/0001-tier-b-hidden-l2cap-socket.md` is the live decision record for the Tier B transport — keep it in sync with `TierProbeCache`/`AapTransport` if either changes.
- No app launcher icon exists yet (`MissingApplicationIcon` lint finding, pre-existing) — not blocking. `res/drawable/ic_notification.xml` is a deliberate placeholder for the same reason (see above), not a regression.
- `.idea/misc.xml` shows modified in git status in some sessions — pre-existing IDE-local diff, not part of any commit.
