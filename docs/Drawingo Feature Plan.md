# Drawingo feature backlog

This is an idea backlog, not a release commitment. The current product is a calm, universal drawing app. Keep features optional, local-first, accessible, and focused on creative flow. Do not make claims about treating health conditions. See the [app overview](app-overview.md) for features already in the repository.

## Current foundation

The app already has a drawing canvas with multiple tools and colors, layers, a flipbook timeline, stroke replay, local motion effects, a local projects gallery with generated thumbnails, image backgrounds, and PNG/JPEG export. These existing features are summarized in the app overview; do not list them as planned work.

## Candidate improvements

Ideas below are not implemented unless the app overview says otherwise. Prioritize based on user feedback and implement one small complete slice at a time.

### Drawing feel

- Improve stroke stabilization and pressure behavior, with responsive stylus support.
- Refine brush materials so watercolor, crayon, marker, and brush have clearly distinct visual results across canvas rendering and export.
- Add optional shape guides or shape snapping, with immediate undo and a way to preserve the original freehand stroke.
- Improve accessibility labels, touch target sizing, contrast, and left-handed layout options.

### Relaxed creative sessions

- Optional, user-controlled drawing prompts or short guided sessions that work offline.
- Optional sound and motion controls, including a reduced-motion setting and a quick mute control.
- A clearer animation workflow that distinguishes frame-by-frame flipbook playback, stroke replay, and motion effects.

### Projects and output

- Clearer project rename/delete controls.
- Evaluate animated export only if it can be implemented reliably on-device and with clear storage controls.

## Product and engineering constraints

- Drawing content stays on-device. Do not add cloud AI, video/image generation services, analytics, accounts, advertising, or social collection.
- Consider ML Kit only for a defined on-device use case; ordinary drawing effects should remain deterministic local rendering.
- Avoid streaks, scores, competitive leaderboards, guilt-based reminders, and forced sharing.
- Keep undo and recovery clear. Respect system font sizing, accessibility services, battery, and reduced-motion preferences.
- Update the app overview and privacy policy when shipped behavior or data flow changes. Keep this backlog separate from implemented features.
