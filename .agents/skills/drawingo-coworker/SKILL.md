---
name: drawingo-coworker
description: Use when planning, implementing, reviewing, or documenting a Drawingo product feature or a cross-cutting Android change. Align the work with Drawingo's adult recreational drawing direction, privacy principles, and shared Codex/Gemini workflow.
---

# Drawingo coworker workflow

Read the repository-root `AGENTS.md` and `docs/AI_COWORKER_GUIDE.md` first. They are the shared source of truth for project principles and must be followed by both Codex and Gemini in Android Studio.

## For feature planning

1. Inspect the current app, `git status`, relevant code, and `docs/app-overview.md`.
2. Separate features that exist today from ideas that are only planned.
3. State the user problem, target experience, smallest complete milestone, and key tradeoffs.
4. For networked or AI features, describe the exact data flow, opt-in point, failure behavior, and privacy-policy impact.
5. Do not implement while the user is asking only to brainstorm or approve a direction.

## For implementation

1. Map the user-visible behavior to existing models, ViewModels, composables, services, and renderers before editing.
2. Preserve user changes and work left by the other coding tool. Never use reset/checkout/clean to make the tree convenient.
3. Implement a focused end-to-end slice. For drawing media, keep tool state, canvas rendering, bitmap rendering, animation input, and accessibility descriptions aligned.
4. Review the diff for accidental scope changes, privacy exposure, security regressions, and stale documentation.
5. Run only validation appropriate to the request and environment; report exact commands and results. Do not claim unrun checks passed.
6. Finish with a concise handoff: behavior changed, key files, validation, and unresolved limitations.

## Review priorities

Review first for user-data flow, credential handling, input validation, lifecycle/performance, and regressions in undo/redo or drawing fidelity. Then check UI clarity, accessibility, and documentation alignment. Raise concrete findings with file/line evidence rather than vague style preferences.
