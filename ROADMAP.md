# Roadmap / Session Handoff

Living doc, not history — overwrite next session, don't append.

## This session

- Fixed auto-pause reconnect misfire (`AutoPause.kt`): reconnect now resets `previous` state instead of comparing against stale pre-disconnect ear state.
- Built the §13.6 DataStore Tier B probe cache (TDD, RED→GREEN both rounds):
  - `TierProbeCache` interface + `DataStoreTierProbeCache` (real) + `FakeTierProbeCache` (test).
  - Keyed on `deviceAddress@Build.FINGERPRINT` — OS upgrade = free cache miss, no manual invalidation.
  - **Confirm-twice policy**: needs 2 consecutive failed probes before caching "unsupported"; any success resets the streak. A single `IOException` can't be told apart from a transient PAGE_TIMEOUT, so caching after one failure would permanently kill Tier B for a capable device.
  - Wired into `AirPodsRepository.connect()` (skips the live probe once confirmed-unsupported) and `AirPodsRepositoryProvider.repositoryFor(device, context)` (now needs `Context`).
  - Added `AirPodsTransport.deviceAddress` so the repository can key the cache without touching `BluetoothDevice` directly (layering, §11).
- Found and documented (not yet fixed) a real gap in ADR-0001: `AapTransport.connect()`'s `catch (e: IOException)` can't distinguish a `PAGE_TIMEOUT` (transient, radio asleep) from a structural PSM rejection — both are the same exception type with just a message string. The plan requires treating PAGE_TIMEOUT as *inconclusive*, not merely retried-then-negative. 3 consecutive page timeouts today still reach `STATE_CONFIRMED_UNSUPPORTED` same as a real rejection. Closing this needs either a native-log signal surfaced into Kotlin, or an ACL-active precondition — both need the real-hardware pass to verify, so left as a documented gap, not guessed at.
- Commits: `2c21460`, `8b13d4e`, `f479931`. Working tree currently has an uncommitted ADR-0001 edit (the gap note above) — not yet committed.

## How

TDD throughout (`superpowers:test-driven-development`, RED confirmed via actual compile/test failure before GREEN both times). ADRs updated live as decisions changed (`docs/adr/0001-tier-b-hidden-l2cap-socket.md`). Verified via `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:lintDebug` after each round — no regressions, only the 3 pre-existing informational lint findings.

## Plan state (`~/.claude/plans/bitte-lies-dir-einmal-smooth-meerkat.md`)

- **M0–M1**: done.
- **M2**: code-complete except two items that need real hardware: (a) read the model ID / confirm the AirPods 4 variant, (b) the PAGE_TIMEOUT-inconclusive gap above. Both deliberately deferred — standing instruction this session: *"do real hardware verification at the end, continue with the plan meanwhile."*
- **M2b** (Tier A advertisement fallback): deliberately deferred (`zurückgestellt`) in the plan itself; its original trigger is largely unreachable now that `minSdk` effectively requires Android 16+.
- **M3** (write controls: ANC/Transparency/Adaptive, ear-detection toggle, etc.): **hard-gated** — plan says no write command before the M2 session is hardware-confirmed (§2.6/§33). Don't start this without the hardware pass first, regardless of how "continue with the plan" is phrased.
- **M4–M7**: not started. Plan states widgets (M5) and Quick Settings depend only on `AirPodsState`, independent of the Tier question — the one legitimately hardware-independent forward path right now.

## Next steps (proposed, not started)

1. Real-hardware pass (Pixel 9 Pro XL + AirPods 4) — do this first when picked back up:
   - Confirm AirPods 4 variant via model ID.
   - Manually force the DataStore probe result to verify the Tier A UI explanation (plan's own prescribed M2 verification step).
   - Check whether native logcat (`GAP_ConnOpen`) can realistically be piped into `AapTransport` to fix the PAGE_TIMEOUT-inconclusive gap; if not feasible, keep it documented as-is.
2. If continuing without hardware access: M5 battery widget is the only unblocked path.
   - Needs adding `glance-appwidget` to the version catalog — **not there today on purpose** (M0 skipped it: `1.1.1` predates this Compose BOM line and may pull an older `compose-runtime`). Do a resolution check first; plan names `1.3.0-alpha02` as the fallback if `1.1.1` conflicts.
   - Skip Hilt for just one widget — wire straight to `AirPodsRepositoryProvider`. M0 flagged Hilt as due at "first injected entry point," but one widget doesn't justify pulling in a DI framework yet.
   - Widget's most common render state is `AirPodsState.INITIAL` (no presence event fired yet since process start) — design an explicit "no data yet" state for it, not an empty battery row.
   - Do **not** build the noise-control widget or Quick Settings tile — both are write surfaces, blocked same as M3.
3. Do not start M3 under any framing until step 1 is actually done.

## Notes for other AI assistants picking this up

- Read `CLAUDE.md` and `PROJECT.md` (esp. §11 layering, §13.6, §33) before touching anything — they override defaults.
- `DataStoreTierProbeCache` and `AapTransport` are both deliberately *not* unit-tested (framework types); don't add JVM tests for them, verify on hardware instead.
- Real MAC addresses/serials are still unredacted in `app/src/test/resources/fixtures/aap/session-start-capture.txt` — intentional for now, **must be scrubbed before any MVP security pass / public release** (§28). Don't scrub preemptively without being asked; it's tracked, not forgotten.
- `docs/adr/0001-tier-b-hidden-l2cap-socket.md` is the live decision record for the Tier B transport — keep it in sync with `TierProbeCache`/`AapTransport` if either changes, it's been edited 3x this session already as the design evolved.
- `.idea/misc.xml` shows modified in git status — untouched by this session, pre-existing IDE-local diff, not part of any commit here.
