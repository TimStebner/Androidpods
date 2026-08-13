# ADR-0001: Tier B AAP transport via hidden L2CAP socket API

**Date**: 2026-08-13
**Status**: accepted
**Deciders**: Tim Stebner, Claude (development session)

## Context

AirPods control features — noise control, ear-detection toggle, stem actions, firmware/model
metadata (`PROJECT.md` §8.1) — require Apple's AAP/AACP protocol over a classic BR/EDR L2CAP
channel at PSM `0x1001` (§13.5 Tier B). The plan assumed this channel would be reachable via
Android's public `BluetoothDevice.createL2capChannel`/`createInsecureL2capChannel` (public since
API 29) on Android 17, based on a documented AOSP Bluetooth stack fix (Fluoride ERTM
channel-mode bug, Google issue 371713238).

A real-hardware probe (`L2capTierBProbeTest`, Pixel 9 Pro XL, Android 17, bonded AirPods 4)
showed both public methods fail immediately for PSM `0x1001` and a control PSM `0x1003` alike,
secure and insecure alike, with the native log reporting
`GAP_ConnOpen: Failure registering PSM 0x...., is_le: true`. The public API implements LE
Connection-oriented Channels only, not classic BR/EDR — a structural mismatch independent of the
stack fix, not an Android-version problem.

LibrePods reaches this channel via a root-requiring Magisk module (`btl2capfix.zip`) that patches
`libbluetooth_jni.so` via `dlsym` offset lookup. A CAPod-adjacent approach instead reflects on the
non-public `BluetoothDevice.createInsecureL2capSocket(int)` — a denied hidden API (note "Socket",
not "Channel": a different, older method than the two public ones above).

## Decision

Reach the AAP L2CAP channel via
`HiddenApiBypass.invoke(BluetoothDevice::class.java, device, "createInsecureL2capSocket", 0x1001)`
(`org.lsposed.hiddenapibypass`), which exempts this app's own process from Android's non-SDK-
interface enforcement via a JNI call into `libart.so`.

Confirmed on real hardware: the native log shows correct classic BR/EDR registration
(`L2CA_Register: L2CAP Registered service classic PSM: 0x1001`) and a successful connection
(`notify_app_connected: ... is_le: false`) once the AirPods have an active BR/EDR ACL.
Implemented in `AapTransport` (`core.bluetooth`).

## Alternatives Considered

### Public createL2capChannel / createInsecureL2capChannel

- **Pros**: Fully public SDK API, no non-SDK-interface risk.
- **Cons**: LE Connection-oriented Channels only; structurally cannot reach a classic BR/EDR
  accessory channel.
- **Why not**: Confirmed failing on real hardware regardless of Android version or the stack fix
  landing.

### Root/Xposed + native libbluetooth_jni.so patch (LibrePods' btl2capfix Magisk module)

- **Pros**: Doesn't depend on a specific hidden method continuing to exist; patches the stack
  directly.
- **Cons**: Requires root/Xposed.
- **Why not**: `PROJECT.md` §8.4 explicitly excludes root/Xposed as a v1 non-goal.

### Wait for or require a future public BR/EDR L2CAP API

- **Pros**: No hidden-API risk at all.
- **Cons**: No such API exists or is announced.
- **Why not**: Not actionable within this project's timeframe; would block the entire Tier B
  feature set indefinitely.

## Consequences

### Positive

- Unlocks the entire Tier B feature set (§8.1 write operations, firmware/model metadata) without
  root, matching the project's core differentiator (§3.1 reference-platform bet).
- No system or cross-process modification — the exemption is scoped to this app's own reflection
  calls only, not root and not a stack modification (§8.4).

### Negative

- Depends on a specific hidden method (`createInsecureL2capSocket(int)`) continuing to exist and
  behave the same way across Android versions and OEM builds, with no compatibility guarantee
  from Google.
- `HiddenApiBypass` itself could stop working on a future Android version that tightens
  non-SDK-interface enforcement further.

### Risks

- **OEM/version fragmentation**: §13.6 calls for treating Tier B availability as a runtime probe
  result, cached per device address and OS build fingerprint, never inferred from `Build.VERSION`.
  That cache is **not yet implemented** — today's probe result lives only for the process/session
  lifetime (`AapTransport`'s connect-time retry, surfaced as `ConnectionState.Failed` and rendered
  honestly per §2.6, but re-probed from scratch on every connection attempt). The DataStore-backed
  cache is still open work, tracked as an M2 item.
- **PAGE_TIMEOUT false negatives**: an idle/asleep classic ACL causes a page timeout on the first
  connect attempt that looks identical to a structural rejection. Mitigated with a bounded retry
  (3 attempts, 2s backoff) in `AapTransport.connect()` — a one-shot probe would permanently
  mis-cache a capable device as unsupported.
