# Drawingo app overview

Drawingo is being repositioned as a universal creative app centered on one idea: make a drawing, then turn it into a short animation securely on your device.

## Current features

### Draw

- Create freehand drawings on a white canvas.
- Use the pen, marker, brush, watercolor, crayon, eraser, and lasso tools. Watercolor uses translucent pigment washes; crayon uses a denser stroke with visible paper flecks.
- Select colors and adjust stroke or eraser size.
- Pan and zoom with touch gestures.
- Undo, redo, or clear the current canvas.

### Animate a drawing

- Preview the current drawing with a secure, on-device local animation.
- All animation logic is powered by Google ML Kit running strictly on the device.
- Stop the local animation or clear the canvas.

There is no coloring-page catalog, parent gate, screen-time timer, kiosk mode, drawing gallery, or export flow in the current UI.

## Privacy and storage summary

- No account, advertising, analytics, social-sharing, camera, microphone, or location features are present.
- Drawings are held in app state and are not saved to a gallery.
- All AI processing (animation, object detection) occurs 100% locally via Google ML Kit. No drawings are uploaded to the cloud.
- Android backup is disabled.

See [the privacy policy](privacy-policy.md) for the current data flows and choices.

## Technical overview

- Native Android app written in Kotlin with Jetpack Compose and Material 3.
- Android minimum SDK 24; target SDK 36; compile SDK 37.
- Gradle wrapper 9.6, Android Gradle Plugin 9.4, Kotlin 2.4, Compose BOM 2026.08, Lifecycle 2.10, Room 2.8, KSP 2.3, and Google ML Kit.

To build the debug APK from the repository root:

```bash
./gradlew :app:assembleDebug
```
