# Shared project instructions

These instructions are the shared starting point for Codex and Gemini in Android Studio. Treat the repository as the source of truth; do not rely on an earlier chat summary when the code or docs say otherwise.

## Before changing the project

- Read `docs/AI_COWORKER_GUIDE.md`, the relevant source files, and the applicable feature documentation.
- Check `git status` and inspect local diffs before editing. Preserve user changes and work from other tools; do not reset, discard, or overwrite them.
- For product planning or a multi-file feature, load `.agents/skills/drawingo-coworker/SKILL.md` and follow its workflow.
- If current behavior or product direction is unclear, inspect the app and docs first. Ask a focused question only when a decision cannot reasonably be inferred.

## Shared principles

- Drawingo is an adult-oriented, recreational drawing app designed to feel calm, playful, and low pressure. Do not add child-directed content, parental controls, or claims that the app treats or prevents a health condition.
- Keep drawing responsive and enjoyable. Prefer understandable, accessible UI and native Android patterns already used in the app.
- Keep the app local-first. Any network transfer of a drawing must remain explicit, clearly described, and consistent with Settings and the privacy policy. Never add analytics, accounts, advertising, or cloud uploads implicitly.
- Keep secrets out of source, resources, and client builds. Use HTTPS, validate inputs, and handle network failures without exposing credentials or private payloads.
- Match existing Kotlin, Jetpack Compose, Gradle version-catalog, and architecture conventions. Avoid new dependencies unless they solve a concrete need.
- Make focused, reviewable changes. Do not commit, deploy, publish, or perform destructive actions unless the user asks.
- Report what changed, important tradeoffs, and exactly what validation was run. Never claim a build or test passed unless it did.

## Project map

- Product and data-flow overview: `docs/app-overview.md`
- Shared product, privacy, architecture, and collaboration guidance: `docs/AI_COWORKER_GUIDE.md`
- Android app: `app/src/main/java/com/example/drawingo/`
- Optional Gemini backend: `functions/`
- Privacy policy: `docs/privacy-policy.md` and `site/`
