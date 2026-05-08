# Sopdu

Sopdu is an offline communication utility for Android. It is designed for situations where internet access, mobile networks, or familiar communication channels are unavailable, overloaded, or unreliable.

The current build focuses on nearby discovery, local chats, stable device identity, manual radar scans, local peer notes, on-demand coordinate sharing, and first distress signaling.

## Status

Sopdu is an experimental public preview, not a production emergency system.

Use official help first whenever it is reachable. Sopdu is a support tool only. It is not an emergency service, medical tool, rescue system, or replacement for official safety instructions.

## Current Features

- Stable Sopdu ID generated on each phone
- Manual Radar scans for battery-friendly nearby discovery
- Nearby chat flow with accept/reject for unknown peers
- Hidden chats for rejected and blocked users
- Local additional names for peers
- Battery indicator mock/share-ready UI
- Last seen display
- Manual coordinate sharing inside a chat
- Export local chats as readable JSON
- Basic delivery labels such as `sent` and `saved locally`
- Distress mode with repeated alert sound and flashlight when available
- Tethering settings shortcut for future bridge/mesh work

## Safety Notes

Sopdu is meant to help nearby people coordinate when ordinary communication is not working.

If you can safely call, text, radio, or otherwise reach help, try local emergency numbers, ambulance, fire response, rescue teams, or trusted local authorities before relying on any app.

Nearby range is approximate. In a clear open area, detection may reach about 100 m. Walls, floors, damaged structures, crowds, vehicles, interference, phone hardware, low battery, and battery saver settings can reduce that distance.

Move only when it is safe, and keep watching the area around you. Do not enter unsafe places or separate from trusted people without a clear reason.

## Privacy Notes

- Sopdu does not require an account, profile photo, or social media connection.
- The Sopdu ID identifies this app installation on this phone.
- Additional names are saved locally on the viewer's phone.
- Coordinates are shared only when the user chooses to send them.
- Exported JSON files may contain sensitive messages, peer IDs, local names, and coordinates. Treat exports carefully.

## Current Limitations

- Message delivery over Nearby/Bluetooth still needs testing with two real Android devices.
- Android emulators do not reliably represent Bluetooth/Nearby behavior.
- Tethering is currently a settings shortcut only. It is not an active Sopdu chat transport yet.
- Bridge/mesh behavior is planned for later work.
- Voice messages, media sharing, encryption, and guaranteed delivery are not implemented yet.
- Distress behavior is an early implementation and needs more device testing.

## Debug/Test Data

Debug builds include seeded test peers and chat requests so the UI can be reviewed without two real phones. These are guarded behind `BuildConfig.DEBUG` and are not intended as real contacts.

Before publishing a production release, debug/test peers should be removed from screenshots and release builds.

## Tech Stack

- Kotlin
- Jetpack Compose
- Android Gradle Plugin
- Room / SQLite
- Google Nearby Connections
- Android Bluetooth/BLE APIs
- Material 3

## Build

Open the project in Android Studio, then run the app on an Android device.

Command-line debug build:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Testing Recommendation

For meaningful communication testing, use two real Android phones:

- Install the same APK on both devices.
- Enable Bluetooth and location services.
- Grant requested permissions.
- Keep both phones nearby and unlocked.
- Start a Radar scan.
- Accept the incoming chat request.
- Send messages in both directions.
- Test block/unblock, rejected chats, and location sharing.

## Roadmap

- Real two-device delivery testing
- Cleaner delivery states and retry behavior
- Bridge/mesh research
- Tethering or Wi-Fi Direct transport investigation
- Distress mode improvements
- Optional voice message support
- Public release packaging

## License

Sopdu is released under the MIT License. See [LICENSE](LICENSE).
