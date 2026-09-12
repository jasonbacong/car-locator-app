# Car Locator

Auto-saves your parking spot when your phone disconnects from your car's
Bluetooth, unless you're near home or another saved safe zone — then hands off
navigation to Google Maps or Waze. Also does notes, reminders, share, a compass
"Locate" screen, and a home-screen widget.

Android only for now — see [`android/`](android/README.md) for the full source
and build instructions. (An iOS version exists but isn't published here yet.)

## Installing the APK

A debug build is attached to the [Releases](../../releases) page for anyone who
wants to try it without building from source.

**Read this before installing anything from an unknown source, this one included:**

- This is a **debug-signed APK**, not distributed through the Play Store. Android
  will warn you when installing it, and **Google Play Protect will very likely
  flag it** — that's expected, not a bug. Play Protect scans sideloaded apps too,
  and this app's permission profile (background "Always" location access,
  triggered by Bluetooth events, running without you opening the app) is
  structurally identical to what consumer stalkerware looks like. That's not a
  false positive to work around — it's the scanner doing its job. The only honest
  way to earn trust here is transparency, not evasion: **the entire source is in
  this repo** — read [`android/app/src/main/java`](android/app/src/main/java) before
  you install, or better, build it yourself from source (see the Android README)
  so you know exactly what's running on your phone.
- Never install an APK — this one or anyone else's — from a source you haven't
  verified, just because a README told you to.

## License

MIT — see [LICENSE](LICENSE).
