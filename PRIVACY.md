# Androidpods Privacy Policy

Last updated: 2026-08-18

Androidpods processes AirPods and Android device information locally on the user's device. The app has no user accounts, advertising, analytics, telemetry service, or `INTERNET` permission, and it does not collect, sell, share, or upload personal data.

## Data processed on the device

- Bluetooth identifiers and AirPods model information are used to associate the selected accessory and cache whether the local Android Bluetooth stack supports the required connection method.
- Battery, wear-state, configuration, and motion packets are decoded in memory to provide app features.
- App preferences are stored in Android DataStore. The Bluetooth tier-probe cache is excluded from Android cloud backup and device transfer.
- Phone state is used only when the optional head-gesture call-control feature is enabled. Androidpods does not record call audio, phone numbers, contacts, or call history.
- Raw protocol packet logging is available only in debug builds, disabled by default, and requires explicit opt-in. It is not present as a user control in release builds.

Androidpods does not use Bluetooth scanning to derive location and does not request location permission.

## Permissions

Androidpods requests Bluetooth permissions for device association and communication. Notification and phone permissions are optional and are requested only for their corresponding features.

## Data deletion

App preferences and local caches can be removed by clearing Androidpods storage or uninstalling the app. The tier-probe cache can also be reset from the app's diagnostics settings.

## Open-source verification

The complete source code is available at <https://github.com/TimStebner/Androidpods>. Privacy or security concerns can be reported through the repository's issue tracker.

This policy may be updated when the app's behavior changes. Material changes will be reflected in this document and the app's release notes.
