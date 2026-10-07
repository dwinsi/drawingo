# Drawingo app overview

Drawingo is being repositioned as an adult creative app centered on one idea: make a drawing, then turn it into a short animation.

## Current features

### Draw

- Create freehand drawings on a white canvas.
- Use the pen, marker, brush, eraser, and lasso tools.
- Select colors and adjust stroke or eraser size.
- Pan and zoom with touch gestures.
- Undo, redo, or clear the current canvas.

### Animate a drawing

- Start an animation from the current drawing.
- Optionally send a canvas image to the configured HTTPS backend for Gemini scene and subject recognition. Cloud analysis is off by default and must be enabled in Settings.
- Use a local animation preview when cloud analysis is disabled or unavailable.
- Stop the animation or clear the canvas.

There is no coloring-page catalog, parent gate, parental settings, screen-time timer, kiosk mode, drawing gallery, or export flow in the current UI.

## Privacy and storage summary

- No account, advertising, analytics, social-sharing, camera, microphone, or location features are present.
- Drawings are held in app state and are not saved to a gallery.
- Cloud drawing analysis is an opt-in setting. When enabled, animation analysis sends the current canvas image to the configured HTTPS backend and then Gemini.
- Detailed Gemini request/response logs are stored in app-private files. They can include the drawing image encoded in the request and remain until app data is cleared or the app is uninstalled.
- Android backup is disabled. Custom backend URLs must use HTTPS.

See [the privacy policy](privacy-policy.md) for the current data flows and choices.

## Technical overview

- Native Android app written in Kotlin with Jetpack Compose and Material 3.
- Android minimum SDK 24; target SDK 36; compile SDK 37.
- Gradle wrapper 9.6, Android Gradle Plugin 9.4, Kotlin 2.4, Compose BOM 2026.08, Lifecycle 2.10, Room 2.8, and KSP 2.3.
- Optional Node.js/Express backend in `functions/` provides drawing analysis for animation.

To build the debug APK from the repository root:

```bash
./gradlew :app:assembleDebug
```
