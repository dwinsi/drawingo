# Drawingo: Feature Plan

A list of features to add to Drawingo, aimed at making it easy, simple, and friendly for both young children and older adults.

**Design principles**

1. One obvious thing to do on each screen
2. Nothing is permanent, so everything can be undone
3. Never punish a wrong tap
4. Progressive disclosure: simple by default, advanced options hidden but findable

## 1. Core Drawing

- A small set of brushes: pencil, marker, crayon, paintbrush, eraser
- Large, chunky color palette (12 to 16 colors) with a "more colors" option tucked away
- Brush size as 3 visual choices (small, medium, large) shown as dots, not a numeric slider
- Fill bucket for coloring areas
- Eraser that is clearly distinct from Undo

## 2. Mistake-Proofing

- Big, always-visible Undo and Redo buttons
- Unlimited undo, with no confirmation popups for harmless actions
- Constant autosave, so nothing is lost if the app closes unexpectedly
- "Start over" that asks once and is easy to reverse

## 3. Accessibility and Ease of Use

- Large touch targets (at least 48 to 56px) with generous spacing
- Icons paired with text labels
- High contrast mode and adjustable UI size
- Palm rejection and stylus support
- Stroke smoothing or stabilization for hand tremors and small-motor control
- Voice prompts or read-aloud for labels
- Left-handed mode that flips the toolbar
- Everything usable with one finger; gestures like pinch-to-zoom are optional extras
- Respect the system "reduce motion" setting and avoid flashing effects

## 4. Fun and Motivation

- Coloring pages (animals, flowers, vehicles, mandalas) at easy and detailed levels
- Stamps and stickers
- Shape guides and "trace the dotted line" mode
- Gentle sound effects with an easy mute
- A simple celebration when a drawing is finished, with no scores or pressure
- Symmetry (mirror) drawing mode

## 5. Kid-Specific

- No ads, no external links, no in-app purchases a child can trigger
- Parent area behind a simple gate for settings
- Offline-first, with no account required
- Friendly guide character or mascot

## 6. Older-Adult-Specific

- Calm, uncluttered interface with no surprise animations
- Clear, jargon-free wording
- Reference photo and tracing mode, for recreating a photo of a grandchild, garden, and so on
- Easy printing and sharing with family

## 7. Animation

Animation has two separate levels so the simple version is never cluttered by the advanced one. The Animate screen offers two big cards: **Quick Magic** and **Flipbook**.

### 7.1 Quick Magic (presets)

After drawing, the person taps "Animate" and chooses one big, friendly motion.

- Whole-drawing or selected-object motions: wiggle, bounce, float, pulse, spin
- Draw-on replay: strokes redraw themselves in order, like a timelapse
- Sparkle or twinkle on selected colors
- Grow-in or pop-in entrance
- Tap an object (sun, fish, flower) and give it its own motion
- Draw a path and have an object follow it
- Background effects: falling snow, rain, floating bubbles, drifting clouds

### 7.2 Flipbook (frame-by-frame)

Optional, entered only by people who choose "Make your own animation".

- Limited frame count to start (6 to 8, up to 12)
- Onion-skin ghost of the previous frame
- "Copy this frame" button so each frame starts from the last one
- Big Play and Stop buttons, adjustable playback speed
- Short first-time tutorial
- Option to apply a Quick Magic preset on top of a flipbook animation

### 7.3 Animation sharing and keeping

- Export as GIF, short video, or looping animation
- Animated thumbnails in the gallery
- Clear Stop button; do not autoplay busy or loud animations
- Keep Draw and Animate as separate tabs to avoid confusion

## 8. Magic Shapes (Shape Perfecting Mode)

Rough strokes snap into clean shapes.

- Explicit "Magic Shapes" mode (wand icon) so normal freehand drawing is never hijacked
- Trigger: pause or hold at the end of a stroke, then it snaps
- Supported shapes: line, circle, ellipse, square, rectangle, triangle, star, heart, arrow, pentagon, hexagon
- One-tap undo of the snap to get the original stroke back
- Gentle snap animation
- Sensitivity setting (Easy / Normal / Strict) for shaky hands
- Drag handles to resize, rotate, and move after snapping
- Snap to level or straight, and to equal sides
- Fill color option right after creating a shape
- Stretch ideas: "Guess what I drew" doodle recognition with clip-art swap, and shape-drawing challenges as a hand-control exercise

**Combining with animation:** once shapes are clean objects they are easy to animate (a circle becomes a bouncing ball, a triangle a spinning sail). Signature idea: "Draw it, perfect it, bring it to life."

## 9. Gallery, Sharing and Keeping

- Gallery of past drawings with large thumbnails
- One-tap share, save, and print
- Optional timelapse replay of a drawing being made

## 10. Technical Notes

- **Storage:** store drawings as vector strokes or layers rather than flat pixels. This makes per-object animation, shape editing, and timelapse replay far easier. Decide this early.
- **Frames from day one:** design the data model so a drawing can hold multiple frames, even if the Flipbook screen ships later.
- **Shape recognition, easiest to hardest:**
  1. Geometric heuristics (line and circle fitting, corner detection with Ramer-Douglas-Peucker simplification)
  2. $1 / $P gesture recognizers for a small shape set
  3. A small on-device ML model for freehand doodle recognition (for example trained on the Quick, Draw! dataset)
- **Testing:** make sure undo, autosave, and the gallery work with both animation systems.

## 11. Suggested Phases

**Phase 1: Foundation (MVP)**

- Core brushes, colors, sizes, fill, eraser
- Undo/redo and autosave
- Large touch targets, labels, high contrast, left-handed mode
- Gallery with share and save

**Phase 2: Delight**

- Magic Shapes mode
- Quick Magic animation presets
- Coloring pages, stamps, stickers, symmetry mode
- Sound with mute, celebration on finish
- Animated export (GIF or video)

**Phase 3: Depth**

- Flipbook frame-by-frame mode
- Tracing and reference photo mode
- Read-aloud, stroke stabilization, sensitivity settings
- Timelapse replay, parent area

**Phase 4: Stretch**

- "Guess what I drew" doodle recognition
- Path-following animation and background effects
- Drawing challenges

## 12. Open Questions

- Target platforms (tablet, phone, web) and how much stylus support matters
- What the current Drawingo already includes, so overlapping features can be trimmed from this list
- Whether the app is split into separate kids and seniors modes or one adaptive interface
