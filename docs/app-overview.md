# Drawingo app overview

Drawingo is a native Android drawing and animation app. Its current direction is a calm, universal creative tool, with on-device processing and private local storage as project requirements. This page describes the implementation currently in the repository; it also calls out legacy cloud code that still needs removal before the app can accurately be described as strictly local-only.

## Current user-facing features

### Drawing canvas

- Freehand drawing with pen, brush, watercolor, crayon, eraser, lasso selection, and text tools.
- A hue-organized color palette, custom color mixing, and separate stroke and eraser size controls.
- Undo, redo, clear, canvas pan and zoom, and horizontal, vertical, or quad symmetry.
- Canvas background choices: white, grid, ruled, and kraft; imported images can also be used as a background.
- A focus mode that hides drawing controls.

### Animation and effects

- Flipbook animation with frame selection, adding, duplicating and deleting frames, playback, FPS state, and onion skin.
- Quick Magic effects including wiggle, pulse, float, shimmer, spin, wander, flip, translate, random, bounce, wave, swing, jitter, heartbeat, spiral, and color cycle.
- Local stroke effects are rendered in the app. The repository also contains a separate legacy cloud video-generation client; there is no visible video-generation action in the current canvas UI.

### Projects, image input, and output

- A projects gallery backed by a local Room database. Projects store drawing strokes, layer metadata, and an optional background image path.
- Start a blank drawing or create a drawing with a photo selected from the gallery or captured through the system camera app.
- Save drawings as PNG or JPEG to device media storage.
- MP4/time-lapse encoding utilities are present in source, but no current UI path to invoke them was found.

### Other controls

- Sound effects through Android `SoundPool`.
- Settings for a legacy cloud AI opt-in and HTTPS backend URL, plus a local API/cloud log viewer. These settings and logging controls are inconsistent with the current local-only product requirements and should be removed as part of the cloud-code cleanup.

## Data and privacy status

- Projects and imported background images are stored locally by the app. Exported images are written to the device media library when requested.
- The app requests Android Internet permission and disables cleartext traffic. Source still contains a default Cloud Run URL, a settings toggle, and a client method that can upload a drawing image and prompt for video generation. The current visible canvas does not invoke that method, but the dormant code means the implementation is not yet safely described as strictly local-only.
- Local interaction logs can contain request payloads, including encoded drawing images, if the legacy cloud request methods are called. The log viewer can read and clear these logs.
- The gallery uses the system camera and photo picker intents. There is no in-app voice recording or transcription implementation found.

**Required before claiming local-only behavior:** remove the legacy cloud backend/client/settings/logging routes, verify the APK's runtime network behavior and permissions, and align the privacy policy with that verified build.

## Technical overview

- Kotlin, Jetpack Compose, and Material 3 Android application.
- MVVM-style state management with Kotlin Flows.
- Room persistence for projects, layers, and strokes.
- `MainActivity` currently launches directly into the canvas; the gallery is reached from the canvas controls.
- Current build settings are defined in `app/build.gradle.kts`, `gradle/libs.versions.toml`, and the Gradle wrapper. Check those files for exact versions rather than copying version numbers into documentation.

## Related documents

- [Shared AI coworker guide](AI_COWORKER_GUIDE.md)
- [Privacy policy](privacy-policy.md)
- [Legacy backend and infrastructure notes](infrastructure-and-accounts.md)
- [Feature backlog](Drawingo%20Feature%20Plan.md)
