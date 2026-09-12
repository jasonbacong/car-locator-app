# Car Locator

Detects when your phone disconnects from your car's Bluetooth, and, unless
you're near your home point, saves the GPS location as your parking spot.
Then a notification lets you jump straight into Google Maps or Waze to
navigate back to it.

## Why this isn't "inside" Maps or Waze

Maps and Waze are closed-source, with no plugin API for third-party apps to
inject features or hook their internal "parked car" logic. This app is a
small standalone companion: it does the detection and saving itself, then
hands off navigation to whichever of Maps/Waze you tap via their standard
deep links (`google.navigation:` / `waze://`). That's the closest thing to
"seamless" that's achievable without Google's or Waze's own source.

Google Maps does have its own built-in "set/detect parking location" feature
(Settings → Navigation settings in the Maps app) that works independently of
this app, so it's worth comparing against once you've tried this one.

## How it works

1. `BluetoothDisconnectReceiver` listens for `ACL_DISCONNECTED` system-wide
   (registered in the manifest, so it fires even with the app not running).
2. If the disconnected device matches the car you picked in Settings, it
   starts `ParkingSaveService` (a short-lived foreground service).
3. The service gets a GPS fix and checks it against your home zone and any
   other safe zones you've added. If you're outside all of them, it
   reverse-geocodes the location, saves it to a local Room database, updates
   the home-screen widget, and fires a notification with "Open in Maps" /
   "Open in Waze" / "Undo" buttons.

## Features

- **Auto-save on disconnect**: the core flow above.
- **Home-screen widget**: shows the latest spot with one-tap Maps/Waze
  buttons, no need to open the app. Long-press your home screen → Widgets →
  Car Locator.
- **Undo**: on the saved-spot notification, in case of a false positive.
- **Notes**: attach free text to a spot (e.g. "Level 3, Section B") from its
  History row, handy for garages where GPS can't tell floors apart.
- **Reminders**: schedule a notification 30 min to 4 hrs out from a saved
  spot, for metered or timed parking.
- **Share**: send a spot to someone else via the system share sheet.
- **Locate**: a distance readout plus compass arrow pointing at a saved spot,
  using the phone's rotation sensor and live GPS. Handy where Maps/Waze
  themselves lose accuracy, like in parking structures.
- **Other safe zones**: beyond home, add more places (work, gym) where
  auto-save should stay quiet.
- **History retention**: spots older than 60 days are cleared automatically.

Not included yet: a Wear OS complication. That would need its own separate
app module and a watch to test against, so it's really its own project
rather than an add-on to this one.

## Building and running

**Android Studio:** open this `android` folder (not the repo root) as the
project, let it sync, then Run on your device, or Build → Build Bundle/APK →
Build APK(s).

**Command line:** from this `android` folder:

```bash
./gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`. With a device
connected over USB (and USB debugging on), install it straight over ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

`assembleDebug` is what you want for your own device, since it's
unsigned-but-installable, which is exactly what ADB and Android Studio's Run
button both use. A `assembleRelease` build additionally needs a signing
config (keystore) to produce something installable, so skip that unless
you're planning to distribute this beyond your own phone.

## First-time setup on your phone

1. Open the app once (this is required: a fresh install is in a "stopped"
   state and won't receive the Bluetooth broadcast until launched at least
   once).
2. Tap **Grant core permissions**, then **Allow background location**. For
   the background location prompt, Android will likely send you into
   Settings and ask you to pick **"Allow all the time"** explicitly.
3. Tap **Select car device** and pick your car's paired Bluetooth profile
   (head unit / hands-free, whatever it's paired as).
4. Park at home, tap **Use current location as home**, and adjust the radius
   slider (default 150 m) to comfortably cover your driveway/street parking.
5. In Android's system settings for this app: turn off **battery
   optimization** (so the OS doesn't kill the detection) and turn off
   **"Remove permissions if app isn't used"** (auto-revoke) under App info →
   Permissions. Otherwise Android may silently strip permissions after a few
   months of the app sitting idle between drives.
6. Never force-stop the app from Settings, since that disables the
   manifest-registered receiver until you manually reopen it.

Use the **Save current location now** button to test the save/notification
flow without waiting for a real disconnect.

## Upgrading from an earlier build

The database schema changed (added notes, safe zones). There's no migration
path: `fallbackToDestructiveMigration()` just wipes and recreates the local
database on first launch after this update. Any test spots you'd already
saved will be gone; your home zone and car device selection (stored
separately) are unaffected.

## Known limitations

- Only triggers on classic Bluetooth disconnect from the specific paired
  device you select, not on Android Auto session end, wireless AA without
  that BT profile, or USB-only connections that don't establish that
  particular profile.
- Waze's `waze://` deep link only works if Waze is installed; it falls back
  to the `waze.com/ul` web link otherwise (which will prompt an app install
  if needed).
- No cloud sync, history lives in a local on-device database only.
