# Drawingo app overview

Drawingo is a native Android drawing and animation app. Its direction is a calm, universal creative tool with private local storage and on-device drawing effects.

## Current user-facing features

### Drawing canvas

- Freehand drawing with pen, brush, watercolor, crayon, fill for closed loops, eraser, lasso selection, and text tools.
- A hue-organized color palette, custom color mixing with direct hex entry, and separate stroke and eraser size controls.
- A unified bottom tool dock keeps the primary color row visible; its pull-up drawer adds a second horizontally scrollable row with the rest of the palette, plus horizontally scrollable tools and size controls.
- Undo, redo, clear, canvas pan and zoom, and horizontal, vertical, or quad symmetry.
- Canvas background choices: white, grid, ruled, and kraft; imported images can also be used as a background.
- A focus mode that hides drawing controls.

### Animation and effects

- Stroke replay reveals the current frame's strokes in their original order and direction, with play/pause, scrubbing, and restart controls.
- Flipbook animation with frame selection, adding, duplicating and deleting frames, adjustable playback speed (1–24 fps), and onion skin.
- Optional locally rendered motion effects including wiggle, pulse, float, shimmer, spin, wander, flip, translate, random, bounce, wave, swing, jitter, heartbeat, spiral, and color cycle.
- Stroke replay, motion effects, and flipbook playback run locally. The app does not currently use an AI model to recognize or animate a subject.

### Projects, image input, and output

- A projects gallery backed by a local Room database. Projects store drawing strokes, layer metadata, and an optional background image path.
- Start a blank drawing or create a drawing with a photo selected from the gallery or captured through the system camera app.
- Save drawings as PNG or JPEG to device media storage.
- MP4/time-lapse encoding utilities are present in source, but no current UI path to invoke them was found.

### Other controls

- Sound effects through Android `SoundPool`.
- A compact canvas action bar with gallery navigation, undo/redo, clear, focus mode, layers, symmetry, animation controls, paper style, and export actions.

## Data and privacy status

- Projects, imported background images, and generated project thumbnails are stored locally by the app. Exported images are written to the device media library only when requested.
- The Android manifest does not request Internet permission. Cloud-generation controls, the Android network client, and payload logging have been removed from the app source.
- The gallery uses the system camera and photo picker intents. There is no in-app voice recording or transcription implementation found.

The old backend source and its deployment workflow have also been removed from this repository. This does not delete or disable any already deployed Google Cloud resources; only an authorized cloud project owner can verify or remove those resources.

## Technical overview

- Kotlin, Jetpack Compose, and Material 3 Android application.
- MVVM-style state management with Kotlin Flows.
- Room persistence for projects, layers, and strokes.
- `MainActivity` currently launches directly into the canvas; the gallery is reached from the canvas controls.
- Current build settings are defined in `app/build.gradle.kts`, `gradle/libs.versions.toml`, and the Gradle wrapper. Check those files for exact versions rather than copying version numbers into documentation.

## Related documents

- [Shared AI coworker guide](AI_COWORKER_GUIDE.md)
- [Privacy policy](privacy-policy.md)
- [Legacy infrastructure cleanup note](infrastructure-and-accounts.md)
- [Feature backlog](Drawingo%20Feature%20Plan.md)
