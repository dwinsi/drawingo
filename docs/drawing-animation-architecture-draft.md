# Architecture Draft: Agent-Assisted Drawing Animation for Drawingo

**Status:** Product and technical proposal for validation  
**Scope:** A user-guided flow that turns a written visual idea into a reviewed Blender scene and an optional generated video. This is a proposal, not implemented behavior.

## 1. Product intent

Drawingo is an adult-operated recreational creation app. In this concept, a parent or other adult describes a visual idea and may create an original rhyme or story video for children in their family. The adult is the user and remains in control of the prompt, review, revisions, and generation; children are the intended audience for some outputs, not accounts or AI operators. The feature must not ask children to submit personal data or interact with the agents.

The flow is more ambitious than the current drawing-to-animation feature. Validate it as an optional creation mode that complements drawing; do not replace the fast, local drawing experience with an agent workflow.

## 2. Proposed user journey

1. **Describe an idea.** The user writes a visual prompt, for example: “A paper boat crossing a quiet moonlit pond, with reflections and soft mist.”
2. **Develop a visual brief.** A planning coordinator asks specialist roles to propose subject, composition, environment, lighting, camera, palette, and animation beats. It presents one coherent brief with uncertainties and editable choices.
3. **User reviews the brief.** The user can accept it, edit it, or ask for another version. No Blender job or paid video request starts before approval.
4. **Build a 3D scene.** A scene-planning role turns the approved brief into a constrained scene specification. A Blender worker assembles the character, environment, camera, lights, and simple motion from reviewed templates and approved assets. The user previews a still or low-cost animatic and can request revisions.
5. **Develop theme and words.** After the visual direction is accepted, a writing role proposes a title, theme, and optional original, child-appropriate short story or rhyme for the adult to review. The adult can edit, regenerate, omit, or approve these. Do not request children's names, voices, photos, or other personal data.
6. **Generate the final video.** After a separate confirmation, the approved scene render, selected theme/text, and generation settings are sent through Drawingo’s authenticated backend to the configured Google video-generation service. The user sees progress, can return to the scene, and receives a temporary playable result if generation succeeds.
7. **Continue collaborating.** The user can request revisions to the brief, scene, lighting, motion, theme, or text. Each change is shown for approval before it causes another expensive render or generation request.

The agent roles are specialist stages in one controlled workflow. They need not be separate autonomous services or run concurrently. A small orchestrator can call role-specific prompts/tools in a defined sequence and keep the current approved version of each artifact.

## 3. Agent responsibilities and boundaries

| Role | Input | Output for user review | Must not do |
|---|---|---|---|
| Creative planner | User's initial visual prompt and selected preferences | Structured visual brief: subject, composition, palette, setting, mood, camera, lighting, motion, open questions | Start rendering or infer personal traits about the user |
| Visual specialists | Brief; focused questions for composition, character, environment, lighting, and motion | Specific suggestions merged into one consistent plan, with conflicts surfaced | Create unbounded sub-agent loops or silently overrule approved choices |
| Scene planner | User-approved brief and selected asset/template catalog | Typed scene specification with objects, approved assets, transforms, lights, camera, actions, duration, and render limits | Return executable Python or arbitrary shell commands |
| Blender builder | Validated scene specification and versioned templates | Preview render/animatic, job diagnostics, scene artifact | Access credentials, arbitrary network, or user filesystem |
| Theme and writing role | Approved visual plan/preview and adult's creative preferences | Optional theme, title, and original child-appropriate story or rhyme for adult review | Add lyrics or text into video unless separately approved; imitate named living artists, reproduce unlicensed lyrics, or request children's personal data |
| Video generation step | Approved render/reference image, user-approved prompt/text, bounded settings | Temporary generated video and safe status/error | Run without confirmation, disclose upload, or spending controls |

“Multi-agent” should mean clear expert perspectives and reviewable artifacts, not unrestricted agents calling each other indefinitely. Prefer bounded JSON outputs, schemas, tool allowlists, explicit state transitions, and a maximum revision/job budget.

## 4. Proposed architecture

### Android app

- **Creation flow UI:** prompt entry, brief review, scene preview, optional theme/text review, final-generation confirmation, progress, result playback, and return-to-edit controls.
- **ViewModel/state:** holds the flow state and approved artifact versions; coordinates cancellation and user actions without blocking Compose rendering.
- **Drawingo canvas integration:** lets users enter this flow from their creative work and return without losing strokes, layers, or undo/redo state. A user-created drawing may be an optional reference, not silently replaced by generated content.
- **Local preview:** show brief cards, storyboards, and low-cost previews before full rendering. Respect reduced-motion preferences and keep audio off unless the user chooses it.
- **Temporary media:** keep generated files in app-private temporary storage with a clear delete action and retention behavior.

### Backend orchestration

Use the existing Node/Express backend as the integration point only after hardening it. Model the workflow as explicit jobs and review checkpoints, for example:

`DRAFT_BRIEF -> AWAITING_BRIEF_APPROVAL -> BUILDING_PREVIEW -> AWAITING_SCENE_APPROVAL -> DRAFTING_THEME -> AWAITING_TEXT_APPROVAL -> AWAITING_VIDEO_CONFIRMATION -> GENERATING_VIDEO -> COMPLETE/FAILED/CANCELLED`

Persist only the minimum job state needed to resume and enforce limits. Version all approved artifacts so a revision to the prompt or scene invalidates downstream approvals where appropriate. Return structured, validated JSON, not agent chain-of-thought or raw provider traces.

### Blender worker

Blender runs as an isolated background job, not inside an Android client and not inside the public API process. The scene planner returns data matching a strict schema. A trusted, version-controlled Python builder maps that schema to Blender API calls and reviewed templates. Do not execute LLM-authored `bpy` scripts.

The worker should be disposable and have no application secrets, no unrestricted network, a restricted filesystem, approved assets only, and strict CPU, memory, disk, frame-count, resolution, and wall-clock limits. Record Blender version, template version, and a bounded diagnostic result for reproducibility. Start with a small catalog of simple stylized characters and environments; do not promise arbitrary rigging from text.

### Video generation provider

The current repository uses Google Gen AI through Vertex AI for Gemini drawing analysis and has a feature-flagged Veo video path. Treat final video generation as a separate backend job using a video-generation model available through the configured Google service; Gemini text/model reasoning and video generation are distinct capabilities. Confirm the supported model, input format, output limits, pricing, and terms before implementation. Keep the provider credentials on the server.

Define whether the provider receives a Blender-rendered clip, selected frames, or a still/reference image. Prefer the minimum inputs that produce the desired result. Do not send the user's entire project or private interaction history.

## 5. Approval, revision, and cost controls

- Approval is required after the visual brief, before a costly Blender preview if it exceeds a configured budget, before final scene render, and before cloud video generation.
- Give revisions a clear target (“change the lighting”, “make the boat smaller”) and show the proposed change before applying it.
- Provide retry/regenerate controls that disclose when they may trigger another paid job.
- Apply per-user/IP rate limits, concurrent-job limits, daily spending budgets, maximum revisions, timeouts, output-size caps, and abuse monitoring on the backend.
- Keep preview rendering low-resolution and short. Render at final quality only after scene approval.
- Do not enable paid endpoints on a public unauthenticated deployment. The existing project guide explicitly requires authentication and spending controls before enabling Veo publicly.

## 6. Data flow and privacy

### Planning and scene preview

`User prompt -> explicit create action -> HTTPS backend -> specialist planning calls -> brief returned to app -> user approval -> validated scene specification -> isolated Blender worker -> preview returned to app`

If a drawing is used as a reference, disclose that and send only the selected image. Keep any locally feasible brief/scene preview available without cloud video.

### Theme and final video

`User accepts visual scene -> optional theme/verse draft -> user edits/approves -> separate Create video confirmation -> backend submits approved prompt and minimum visual reference to video provider -> temporary result returned -> app-private cache -> user plays/deletes`

Before confirmation, name the prompt/image/text being sent, the configured backend and provider, the purpose, and the possibility of cost. Cloud AI stays off by default. Provide clear errors and return to the last approved scene when a job fails.

Review and update `docs/privacy-policy.md`, `site/`, and `docs/app-overview.md` before shipping. In particular, review current interaction logs: the existing analysis logger can include a base64 drawing image. Do not store generated text, images, prompts, or videos longer than necessary; never log image payloads or credentials. Add deletion and abandoned-job cleanup behavior.

## 7. Product fit and audience gate

The user clarified that **adults will create original rhymes or story videos for their children**. The creator-facing flow remains adult-operated; generated content can be family-friendly and intended for children. This is a more specific product direction than the current shared guide, which says not to add children's content. Before implementation, update that guide and other product documentation to state the distinction between adult users and child audiences. Do not add child accounts, child-operated AI features, or collection of children's personal data.

The current Google Cloud Service Specific Terms prohibit use of a Generative AI Service as part of an application or online service directed toward or likely to be accessed by under-18s. That clause is about the service's direction/access, and this draft does not assume that a parent-created video audience alone settles how it applies. Before any Google AI feature ships, confirm with Google Cloud whether an adult-operated app that creates videos for children and may play them for children in-app is permitted; design the app so children cannot operate the AI workflow, and follow the provider's written guidance. [Google Cloud Service Specific Terms, Generative AI Services § Age Restrictions](https://cloud.google.com/terms/service-terms)

## 8. Main risks and mitigations

- **Overbuilt workflow:** many agents and approvals can make a drawing app feel like a production studio. Keep the flow optional, fast to exit, and test a single visual idea end-to-end before expanding roles.
- **Inconsistent agent outputs:** use typed artifacts, shared approved context, validation, and explicit user sign-off; avoid passing unstructured transcripts between every role.
- **Blender reliability:** constrain scenes to tested templates and assets; test a representative set of prompts and report unsupported requests clearly.
- **Untrusted generated code:** never run model-authored Python. Use a schema-to-template builder inside an isolated worker.
- **Cost and abuse:** require authentication, quotas, budget ceilings, job limits, and explicit user confirmation before paid work.
- **Artwork privacy:** treat prompts and drawings as private creative content; disclose each transfer; minimize retention and diagnostics.
- **Model reinterpretation:** final generated video may change scene details. Label it as an AI-generated interpretation, preserve the approved scene, and let the user return to it.
- **Child audience and safe output:** adult review is required before a story or video is generated or shown. Keep output age-appropriate, do not create child profiles or collect child data, and do not position the service as a child-operated AI app.
- **Rights and originality:** do not generate or distribute copyrighted rhyme lyrics or imitate specific artists. Check asset/model/provider licenses and output terms before release.
- **Latency and failure:** show job stages, support cancellation where possible, preserve the last approved artifact, and make retries optional.

## 9. Validation plan and decision gates

1. **Test the flow with static mock outputs.** Validate whether users understand the sequence and find the review checkpoints useful before building agents or Blender integration.
2. **Build a narrow vertical prototype.** One visual brief, one stylized character/environment template, one lighting/motion option, one low-resolution Blender preview, and one revision path.
3. **Evaluate output quality.** Measure whether the approved brief and scene preview match user intent, how often a revision is needed, and whether Blender succeeds within a defined time/resource budget.
4. **Test the optional text stage separately.** Confirm that adults value creating original family-friendly stories or rhymes and can easily edit or omit them.
5. **Only then test final video generation.** Compare video fidelity, latency, failure rate, and cost against the approved Blender preview; verify provider terms, model availability, and backend security first.
6. **Decide whether to proceed.** Continue only if users prefer this guided creation flow as a complement to drawing and quality/cost/privacy tradeoffs are acceptable.

Do not add production analytics merely to measure the prototype. Use moderated sessions or an explicitly consented research method that does not collect artwork unless required and disclosed.

## 10. First implementation milestone (if approved)

Create a non-production vertical slice with mocked planning and text-agent responses, one constrained Blender scene template in an isolated worker, and a review screen for the brief and preview. Include adult-reviewed, family-friendly story/rhyme drafts; defer production video generation and audio/voice generation. Exclude arbitrary Blender scripting and new analytics. Use this prototype to validate the interaction and rendering loop before selecting an orchestration framework or expanding the agent roster.

## 11. Decisions to resolve

- Should a user-created Drawingo sketch be a required input, an optional reference, or neither?
- Can the Google AI service be used under its current age restriction for an adult-operated app whose generated videos may be viewed by children?
- Which two or three visual specialties provide distinct value for the first prototype?
- What asset/template scope is acceptable for the first character and environment?
- Should the final video be based on Blender-rendered frames or use the Blender scene as a reference for a video model?
- What is the acceptable per-result cost and maximum wait time?
- What temporary project and video retention/deletion behavior should users receive?
