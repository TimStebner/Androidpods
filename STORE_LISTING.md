# Google Play Store Listing

## Release track

- Version: `0.1.0` (`versionCode 1`)
- Initial rollout: Internal testing, then Closed testing
- Promotion: Promote the same reviewed app bundle; do not rebuild between tracks

## Listing

**App name:** Androidpods

**Short description:** A private, native Android companion for supported AirPods.

**Full description:**

Androidpods brings battery status, ear detection, head gestures, widgets, notifications,
motion visualization, and capability-aware controls to supported AirPods on Android.

The app processes Bluetooth and motion data locally. It has no account, advertising,
analytics, telemetry, or Internet permission. Hardware-specific write controls are shown only
for models on which they have been physically validated.

Androidpods is open source under GPL-3.0-or-later.

**Privacy policy:** <https://github.com/TimStebner/Androidpods/blob/main/PRIVACY.md>

## Data safety declaration

- Data collected or shared off-device: **None**
- Accounts: **None**
- Advertising or analytics: **None**
- Bluetooth identifiers and AirPods packets: processed locally for association and features
- Phone state: processed locally only for optional head-gesture call controls
- Raw packet logging: debug builds only, disabled by default, explicit opt-in
- Deletion: clear app storage or uninstall Androidpods

## Permission and policy review notes

- Bluetooth permissions support Companion Device Manager association and local AAP transport.
- Phone permissions support optional answer/decline head gestures; no audio, numbers, contacts,
  or call history are recorded.
- `HiddenApiBypass` is used for the non-SDK Classic L2CAP socket required by AAP. A rejected
  non-SDK or permission review is a hard Play release blocker.
