# Car Locator (iOS)

Swift/SwiftUI port of the Android app. Same feature set — home zone, other safe
zones, notes, reminders, share, a compass "Locate" screen, a home-screen widget,
60-day history retention — built on SwiftData, WidgetKit, and App Intents.

## Why the trigger works differently here

iOS gives no third-party app a way to detect a classic-Bluetooth disconnect in the
background — there's no equivalent of Android's `ACL_DISCONNECTED` broadcast. So
instead of the app watching for it directly, an **App Intent** ("Save Parking Spot")
does the actual work, and you trigger it via a one-time **Shortcuts automation**:
Apple's own bridge for exactly this kind of background trigger.

## Project structure

- `Shared/` — SwiftData models (`ParkingSpot`, `SafeZone`), the App Group–backed
  persistence/settings layer, nav-link builders. Used by both targets below.
- `CarLocator/` — the main app: services (location, geocoding, notifications), the
  `SaveParkingSpotIntent` App Intent, and all SwiftUI screens.
- `CarLocatorWidget/` — the WidgetKit extension (home-screen widget).
- `project.yml` — the [xcodegen](https://github.com/yonaskolb/XcodeGen) spec that
  generates `CarLocator.xcodeproj`. **This is the source of truth** — after changing
  target settings, entitlements, or Info.plist keys, edit `project.yml` and run
  `xcodegen generate` again rather than hand-editing project settings in Xcode,
  or the next `generate` will overwrite your changes.

## Building and running

Needs Xcode (already installed if you're reading this on the same Mac this was
built on) and, for a real device, an Apple ID signed into Xcode — a free account
works fine for everything here, including App Groups; you'll just need to re-install
from Xcode every 7 days instead of it lasting a year.

```bash
cd ios
xcodegen generate          # only needed after editing project.yml
open CarLocator.xcodeproj
```

Then in Xcode: pick your device or a Simulator from the scheme selector, and Run.
For your own iPhone, set your Apple ID under the CarLocator target's Signing &
Capabilities tab first.

This has been verified in the iOS Simulator (build, permissions flow, save flow
with real reverse-geocoding, notifications, SwiftData persistence, the History
list, and the Locate/compass screen) — but **not yet on a physical iPhone**, so
there's no confirmation yet of the real end-to-end trigger (an actual car
Bluetooth disconnect firing the Shortcuts automation).

## First-time setup on your iPhone

1. Install and open the app once. Grant **location access** (you'll be asked for
   "While Using", then need to separately allow **"Always"** — Settings → Car
   Locator → Location → Always — since the App Intent needs to work with the app
   not open) and **notifications**.
2. Set your **home zone** and, optionally, other **safe zones** (work, gym, etc.).
3. Open the **Shortcuts** app → **Automation** tab → **+** → **New Personal
   Automation** → **Bluetooth** → pick your car's paired device → **Disconnects**
   → **Next** → search for and add **"Save Parking Spot"** → **Next** → turn off
   **"Ask Before Running"** → **Done**.
4. Add the widget: long-press your home screen → **+** → search **Car Locator**.

## Known limitations

- Real end-to-end triggering (a car Bluetooth disconnect firing the automation)
  hasn't been verified on hardware yet.
- No app icon asset yet — Simulator/dev builds run fine with the default
  placeholder; add one to `CarLocator/Assets.xcassets` before distributing further.
- Like the Android version, Waze's `waze://` deep link only fires if Waze is
  installed; it falls back to the `waze.com/ul` web link otherwise.
