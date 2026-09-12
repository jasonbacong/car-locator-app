# Car Locator

Ever park in a hurry and forget exactly where you left the car? This little app
takes care of that for you. It notices when your phone disconnects from your
car's Bluetooth, and unless you're near home or another safe zone you've saved,
it quietly saves your parking spot and hands off navigation to Google Maps or
Waze whenever you're ready to walk back. Along the way it also picks up notes,
reminders, sharing a spot with someone else, a compass "Locate" screen, and a
home-screen widget.

Head over to [`android/`](android/README.md) for the full source and build
instructions.

## Installing the APK

If you'd rather not build it yourself, a debug build is attached to the
[Releases](../../releases) page.

**A quick note before you install anything from an unknown source, this one
included:**

This is a debug-signed APK rather than something distributed through the Play
Store, so Android will warn you when installing it, and Google Play Protect
will very likely flag it too. That's expected, and it's not a bug we're trying
to sneak past you. Play Protect scans sideloaded apps as well, and this app's
permission profile (background "Always" location access, triggered by
Bluetooth events, running without you opening the app) looks a lot like what
stalkerware does under the hood. That's a fair thing for a scanner to catch,
not a false positive worth engineering around. The best way to earn your trust
here is by being transparent: the entire source is right here in this repo, so
take a look through
[`android/app/src/main/java`](android/app/src/main/java) before installing, or
better yet, build it yourself from source (see the Android README) so you
always know exactly what's running on your phone.

And more generally: please don't install an APK, this one or anyone else's,
from a source you haven't verified yourself just because a README asked
nicely.

## License

MIT. See [LICENSE](LICENSE) for the details.
