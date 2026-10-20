# Drawingo AI coworker guide

This is the shared product and engineering reference for Codex and Gemini in Android Studio. `AGENTS.md` is the short always-on entrypoint; this document contains the fuller context. If the implementation and this guide disagree, inspect the implementation, identify the mismatch, and update the relevant documentation as part of the task when appropriate.

## Product direction

Drawingo is being repositioned as a **universal recreational drawing app** for all ages with a calm, elegant, and minimal feel. The product should make it enjoyable to draw and experiment with materials. It can support relaxation as an experience, but must not claim to diagnose, treat, or prevent stress or any health condition.

The current creative core is a freehand drawing canvas with multiple media and an optional drawing-to-animation experience. Keep the act of drawing central. New features should deepen the creative flow rather than turn the app into a productivity tracker, social network, or noisy game.

### Product principles

- Make the first mark easy: open to a usable canvas with clear drawing controls.
- Make tools feel distinct through their appearance and behavior, not only their names or icons.
- Keep choices inviting and reversible. Avoid scores, streak pressure, competitive leaderboards, guilt-based reminders, and forced sharing.
- Make sound, motion, prompts, and guided sessions optional, with accessible controls and reduced-motion support where relevant.
- Ensure 100% on-device, secure experiences. Do not add network-dependent AI or upload drawings. Use Google ML Kit only when a concrete on-device AI feature needs it; the current local drawing effects do not require AI.
- Keep the UI universally appealing. Ensure icons, typography, and colors are polished and elegant, rather than overly playful or emoji-heavy.

## Current implementation

Read the source before relying on this list; the app is changing quickly and `docs/app-overview.md` may lag behind a recent feature change.

- Native Android app written in Kotlin with Jetpack Compose and Material 3.
- Drawing canvas with pen, brush, eraser, lasso, fill for closed loops, watercolor, crayon, and text tools; color palette and mixer; size selection; undo/redo; pan and zoom.
- The expanded bottom dock keeps the primary colors visible, places all remaining colors in a second horizontally scrollable row, and scrolls the tool row horizontally. A vertical, draggable edge caddy offers the three most recently selected tools, including in Zen mode. It stays tucked behind a small edge tab until opened, then tucks away after a tool is selected. More actions changes its left/right placement, and its position is saved locally.
- Drawing controls support System, Light, and Dark appearance modes, selected from the canvas More actions menu and saved locally. Use fine neutral outlines, readable icons, and a restrained purple selection accent. Keep the default canvas white, honor the user's selected background style independently of the UI theme, and keep dock expansion immediate.
- Watercolor and crayon use dedicated stroke rendering, and their live previews show the material marks. Watercolor uses one renderer for the live canvas and exported bitmap so the appearance stays consistent.
- Stroke replay, flipbook playback, and motion effects are implemented locally. Do not describe these as ML Kit processing unless the current source actually invokes an on-device ML Kit API.
- Cloud-generation controls, the Android client and payload logger, the backend source, and its deployment workflow have been removed from the repository. The app manifest no longer requests Internet access. This removes the app's known network path; do not reintroduce remote generation or drawing uploads.
- The privacy policy is in `docs/privacy-policy.md` and is published from `site/`. Keep product behavior, data-flow documentation, and policy aligned.

The overview in `docs/app-overview.md` currently describes the older tool set; update it when editing product documentation or when a task changes user-visible behavior.

## Collaboration workflow

Treat the other coding tool as a coworker using the same repository, not as an authority whose uncommitted work can be discarded.

1. **Orient:** inspect `git status`, recent changes, docs, and the relevant implementation before proposing edits.
2. **Frame:** restate the user outcome and constraints. For feature planning, distinguish shipped behavior from proposed behavior and identify a small first milestone.
3. **Plan:** for a broad or cross-cutting change, outline affected areas, data flow, security/privacy implications, and how the user-visible behavior will work. Keep small fixes direct.
4. **Implement:** make focused changes that fit the current architecture. Preserve unrelated work and avoid speculative extras.
5. **Check:** use appropriate static checks and project validation. If a build or tests are not run, say so; never imply they passed.
6. **Hand off:** summarize files and behavior changed, any decisions or limitations, and the checks actually performed. Update docs when behavior, dependencies, setup, or privacy practices change.

If tools or earlier instructions conflict, follow the current user request and repository state. If the conflict affects safety, data handling, or product direction and cannot be resolved from context, explain the conflict and ask one concise question.

## Android engineering guidance

- Follow existing MVVM and Compose patterns: state belongs in the current ViewModel/state flows where appropriate; composables render state and report user actions.
- Keep drawing input and rendering responsive. Avoid expensive per-frame work for static content; bound texture/detail work for long strokes and many saved strokes.
- Preserve undo/redo semantics when changing stroke data or interactions. When adding a tool, check the canvas renderer, bitmap/export renderer, animation flow, and accessibility descriptions so behavior remains consistent.
- Reuse the version catalog in `gradle/libs.versions.toml`. Before changing a library, Gradle plugin, SDK, or Android API usage, check the project's current version and authoritative documentation.
- Handle lifecycle, cancellation, and errors intentionally. Avoid blocking the UI thread and avoid swallowing errors that matter to the user.
- Make UI controls accessible with meaningful labels, adequate touch targets, contrast, and support for system font/display settings.

## Privacy and security rules

- The app is local-first and the current Android source contains no network-generation client or Internet permission. Do not embed external credentials or add network connections for generation.
- The app currently launches the system camera and photo picker for user-selected drawing backgrounds. Do not add in-app camera capture, microphone, location, account, analytics, ads, or social collection. Any change to system-mediated image selection still requires privacy-policy review.
- Treat user drawings as private content.
- Keep `docs/privacy-policy.md` accurate when data collection, network transfer, logging, or retention changes.

## Planning new features

For a proposed feature, answer these before implementation:

- How does it help users of all ages enjoy drawing or settle into a creative flow?
- What is the smallest complete version a user can understand and use?
- Can it work locally using on-device frameworks like ML Kit?
- How does it affect accessibility, motion, sound, battery, and canvas performance?
- What existing behavior, documentation, and privacy disclosures need to change?

Prefer a small end-to-end feature that can be evaluated on-device over a large speculative platform.
