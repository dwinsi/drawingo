# Drawingo app overview

Drawingo is a child-focused Android drawing and coloring app. It combines a free-draw canvas with a toddler-friendly magic mode, coloring templates, and an optional drawing-to-animation experience.

## What children can do

### Draw on a touch canvas

- Choose Drawingo mode to make and edit freehand drawings.
- Pick a pen, highlighter, brush, eraser, or wand tool.
- Choose from the color palette and seven stroke-width presets.
- Change the paper between white, blue grid, cosmic night, and warm cream.
- Pan and zoom the canvas with two fingers.
- Undo and redo drawing strokes, or clear the current strokes and magic characters.

The wand tool is currently a thin colored drawing stroke; it does not select or move existing artwork. The current canvas does not offer a save, export, or gallery screen.

### Play in Toddler Magic mode

- Tap or draw a short line to place a large animated character sticker. The characters cycle through celestial objects, sea animals, wild animals, and classic friendly characters.
- Use the neon palette automatically applied in this mode.
- Hear a short sound effect when a character appears.

### Color stock sketches

Choose a template from the sketch picker and draw over it. The app includes 18 bundled templates in three groups:

- **Celestial:** smiling sun, crescent moon and star, twinkle star, Saturn, rocket, comet
- **Sea animals:** whale, turtle, dolphin, octopus, goldfish, seahorse
- **Wild animals:** lion, panda, elephant, bear, giraffe, monkey

Templates include difficulty labels and can be browsed by category. The app starts with bundled assets, then can merge in a refreshed backend catalogue and cache it for later use. Template browsing can use the network independently of the cloud AI setting.

### Animate a drawing

Tap the magic-wand animation button after drawing. The app creates a short scene in one of five styles: ocean, sky, space, land safari, or general magic. It shows a short rhyme, plays speech, and offers controls to replay the voice or stop the animation.

By default, scene selection uses curated on-device results and speech uses Android text-to-speech. A parent can enable cloud drawing analysis in Parent Settings. If enabled, the current drawing image is sent to the configured HTTPS backend, which forwards it to Google's Gemini service. The generated rhyme may also be sent to that backend for cloud speech synthesis. If the backend is unavailable, the app falls back to the local scene and on-device speech.

## Parent and device controls

- **Parent Settings gate:** a multiplication question must be answered to open settings.
- **Cloud AI drawing analysis:** off by default; a parent can opt in or turn it off again.
- **Backend URL:** parents can configure an HTTPS backend endpoint. If a custom endpoint is configured, the app only uses that host for backend requests.
- **Daily screen-time limit:** configurable from 15 to 120 minutes (30 minutes by default). Foreground use is accumulated by local day; after the limit, the canvas is blocked until a parent changes the setting.
- **Kiosk lock mode:** optionally uses Android lock task mode. Where device-owner policy permits kiosk mode, a four-finger hold for three seconds exits the lock.
- **Idle clear:** after one minute without drawing activity, a wipe animation clears canvas content.

The screen-time counter is local to the device and can be reset by clearing app data or changing the device clock. Kiosk enforcement depends on device configuration and owner privileges.

## Privacy and storage summary

- No account, ads, analytics, social sharing, camera, microphone, or location features are present in the Android app.
- The app requests internet access for the stock sketch catalogue and optional cloud features. Cleartext traffic is disabled and custom backend addresses must use HTTPS.
- Drawings are held in app state; the current UI does not persist them as a personal gallery. The Room database classes are present but are not wired to drawing save/load actions.
- App backup is disabled. Sketch catalogue/cache files and generated audio are kept in app-private storage/cache.
- The cloud AI control governs drawing analysis and cloud speech; it does not disable stock sketch catalogue refreshes.

See [Child safety and privacy notes](child-safety-and-privacy.md) for current data flows and remaining release requirements. The parent gate and cloud opt-in are product safeguards, not a legal certification or verifiable parental consent flow.

## Technical overview

- Native Android app written in Kotlin with Jetpack Compose and Material 3.
- Android minimum SDK 24; target SDK 36; compile SDK 37.
- Gradle wrapper 9.6, Android Gradle Plugin 9.4, Kotlin 2.4, Compose BOM 2026.08, Lifecycle 2.10, Room 2.8, and KSP 2.3.
- Optional Node.js/Express backend in `functions/` provides drawing analysis, cloud speech synthesis, sketch catalogue, and sketch-ingestion endpoints.

To build the debug APK from the repository root:

```bash
./gradlew :app:assembleDebug
```
