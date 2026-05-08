# Sopdu Release Checklist

Use this before uploading an APK or creating a GitHub release.

## Required

- Run `:app:compileDebugKotlin`
- Run `:app:testDebugUnitTest`
- Run `:app:lintDebug`
- Run `:app:assembleDebug`
- Confirm app launches on a real Android device
- Confirm app icon and intro screen render correctly
- Confirm About page safety wording is acceptable
- Confirm Settings has no editable self display name
- Confirm Tethering is presented as planned/future support, not active transport

## Before A Public APK

- Decide debug APK vs signed release APK
- Decide whether debug/test peers should be visible
- Test on two real Android phones if possible
- Add screenshots to README or release page
- Include known limitations in release notes

## Known Preview Limitations

- Nearby/Bluetooth message delivery still needs two-real-device testing.
- Emulator testing is not reliable for the core offline transport.
- Tethering/bridge/mesh is planned, not active.
- Distress mode needs more device testing.

