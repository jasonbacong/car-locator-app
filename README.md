# Car Locator

Auto-saves your parking spot when you disconnect from your car's Bluetooth, unless
you're near home — then hands off navigation to Maps/Waze. Two independent native
builds, one per platform:

- [`android/`](android/README.md) — Kotlin + Jetpack Compose. Fully built and running.
- [`ios/`](ios/README.md) — Swift + SwiftUI. Builds and runs in the Simulator; not yet
  tested on a physical iPhone.

Each folder is a self-contained project with its own README — open whichever one
matches the platform you're working on (Android Studio for `android/`, Xcode for
`ios/`). Nothing is shared between them beyond the product concept; there's no
common code, since Kotlin and Swift can't share source directly.

## License

MIT — see [LICENSE](LICENSE).
