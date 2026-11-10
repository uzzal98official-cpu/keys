# Minimal Key — Android keyboard starter project

This is an Android Studio source project for the selected Minimal Key configuration.

## Included in this starter build
- Android InputMethodService keyboard
- English QWERTY layout with permanent number row
- No toolbar, clipboard, autocorrect, word prediction, emoji suggestions, or grammar correction
- Spacebar horizontal gesture for cursor movement
- Adjustable keyboard height
- Light/dark/system appearance and selectable accent palettes
- Optional custom background image selection (saved preference; background rendering can be refined)
- No INTERNET permission: this project does not send typed text to a server

## Build
1. Install Android Studio (recent stable version).
2. Open this folder as a project.
3. Let Gradle sync.
4. Build > Build Bundle(s) / APK(s) > Build APK(s).
5. Install the generated APK on your Android phone.
6. Open Minimal Key > Open keyboard settings, enable it, then choose Minimal Key.

## Important notes
- This is a source-code starter/MVP, not a precompiled APK.
- Android Studio/Gradle is required to compile it into an APK.
- A custom image picker is present; the current keyboard view keeps its background subdued, and full image compositing is a follow-up refinement.
- The current key-corner radius is set to a sensible default. A radius slider can be added in a later settings pass.
- The selected offline-only design is respected: there is no network permission.
- Test on a real device. Android IME behavior and OEM settings can vary.
