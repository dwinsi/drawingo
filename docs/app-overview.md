# Drawingo app overview

Drawingo is being repositioned as an adult creative app centered on one idea: make a drawing, then turn it into a short animation.

## Current features

### Draw

- Create freehand drawings on a white canvas.
- Use the pen, marker, brush, watercolor, crayon, eraser, and lasso tools. Watercolor uses translucent pigment washes; crayon uses a denser stroke with visible paper flecks.
- Select colors and adjust stroke or eraser size.
- Pan and zoom with touch gestures.
- Undo, redo, or clear the current canvas.

### Animate a drawing

- Preview the current drawing with the existing local animation.
- Optionally send a canvas image to the configured HTTPS backend for Gemini scene and subject recognition. Cloud analysis is off by default and must be enabled in Settings.
- Start an optional Veo image-to-video generation with a short custom motion prompt. This sends the drawing and prompt to the configured backend and Google Cloud after the user confirms. The backend endpoint is disabled by default; enable it only on a secured, rate-limited service.
- Play the generated 4-second video in the app. The MP4 is stored in app-private cache temporarily and deleted when the player is dismissed or a new video is requested.
- Stop the local animation or clear the canvas.

There is no coloring-page catalog, parent gate, parental settings, screen-time timer, kiosk mode, drawing gallery, or export flow in the current UI.

## Privacy and storage summary

- No account, advertising, analytics, social-sharing, camera, microphone, or location features are present.
- Drawings are held in app state and are not saved to a gallery.
- Cloud AI is an opt-in setting. When enabled, user-initiated analysis or video generation sends the drawing to the configured HTTPS backend and Google Cloud.
- Gemini interaction logs are stored in app-private files. They can include the drawing image encoded in a request and remain until app data is cleared or the app is uninstalled. Generated MP4 bytes are not copied into the logs.
- Android backup is disabled. Custom backend URLs must use HTTPS.

See [the privacy policy](privacy-policy.md) for the current data flows and choices.

## Technical overview

- Native Android app written in Kotlin with Jetpack Compose and Material 3.
- Android minimum SDK 24; target SDK 36; compile SDK 37.
- Gradle wrapper 9.6, Android Gradle Plugin 9.4, Kotlin 2.4, Compose BOM 2026.08, Lifecycle 2.10, Room 2.8, and KSP 2.3.
- Optional Node.js/Express backend in `functions/` provides drawing analysis and a feature-flagged Veo video-generation API.
- Veo generation requires `ENABLE_VEO_GENERATION=true` on the backend and is disabled by default. The current public Cloud Run deployment must not enable this endpoint until it has authentication and abuse/cost controls.

To build the debug APK from the repository root:

```bash
./gradlew :app:assembleDebug
```
