---
layout: default
title: Drawingo Privacy Policy
permalink: /
---

# Drawingo Privacy Policy

**Last updated: October 7, 2026**

Drawingo is a drawing and coloring app designed for children. This policy explains what the Android app and its optional online services process, how they use it, and what parents can do.

## Information Drawingo processes

### Drawing and app data on the device

Drawing strokes and the selected coloring page are held in the app while drawing. The app does not currently provide a personal drawing gallery or upload drawings automatically. Parent settings and cached sketch catalogue data are stored in app-private storage. Generated speech audio and downloaded sketch images may be kept in the app cache.

The app also writes private diagnostic logs for cloud AI interactions. These logs can include the complete request and response, including a base64 copy of a drawing sent for analysis, generated story text, and selected story choices. They remain in the app's private files until the app's data is cleared or the app is uninstalled. The app has no account or log-export feature. Device backup is disabled for the app.

### Optional cloud drawing and story features

Cloud drawing analysis is off unless enabled in Parent Settings. When enabled, and when someone uses the animation feature, Drawingo sends a PNG of the current canvas to the configured HTTPS Drawingo backend. The backend sends the drawing to Google Cloud Vertex AI's Gemini service to identify a broad subject and generate a short rhyme. If a story choice is submitted, the choice and short story context are also sent to the backend for a continuation. Generated text may be sent to Google Cloud Text-to-Speech to create audio. If cloud analysis is off or unavailable, the app uses its on-device animation and Android text-to-speech instead.

The backend does not intentionally save drawing images or story text as an app history. It writes operational logs, such as interaction IDs, model names, and errors. Google Cloud Run may also create request and service logs, including network and diagnostic metadata. The production project's log buckets currently have 30-day and 400-day retention settings for their respective log categories.

Google Cloud processes data under its own terms and policies. Google states that Vertex AI may keep in-memory prompt/response caches for up to 24 hours and may log prompts for abuse monitoring in certain cases. Google's current [Service Specific Terms](https://cloud.google.com/terms/service-terms) prohibit use of its Generative AI Services in applications directed to or likely to be accessed by people under 18. Drawingo is designed for children, so its current Gemini integration is not appropriate for the intended audience under those published terms. **The app operator must not make cloud AI available to children unless Google provides written authorization or the integration is replaced with a service whose terms permit this use.** A Parent Settings toggle does not override provider terms.

### Voice

The current app does not request microphone permission and does not record, upload, or transcribe a child's voice. Speech is generated from text using Android's on-device text-to-speech, or—if cloud speech is used—from Google Cloud Text-to-Speech. If a voice recording or transcription feature is added later, this policy must be updated before that feature is made available.

### Sketch catalogue and this policy website

The app may contact the Drawingo backend to refresh the stock sketch catalogue and downloads stock images from Google Cloud Storage. These requests do not include the child's drawing. The privacy policy is hosted on GitHub Pages. GitHub may process technical information about visits to this site under [GitHub's Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-privacy-statement). This site does not use advertising or analytics cookies.

## How information is used and shared

Drawingo uses the information described above to provide drawing, coloring, optional AI animation and speech, refresh stock sketches, and diagnose failures. The app operator does not sell children's personal information, serve ads, or use an analytics SDK. There are no user accounts, social sharing features, or public child profiles.

When the optional cloud features are used, the drawing or story text is shared with Google Cloud services as described above to produce the requested result. Google may process network and service data as part of providing and protecting those services. Review [Google Cloud's data governance information for Vertex AI](https://cloud.google.com/vertex-ai/generative-ai/docs/vertex-ai-zero-data-retention) and [Google Cloud's Privacy Notice](https://cloud.google.com/terms/cloud-privacy-notice) for Google's practices.

## Parent choices and deletion

- A parent can leave cloud drawing analysis turned off in Parent Settings. Stock sketch catalogue refreshes are separate from this setting.
- A parent can clear the app's storage or uninstall it to remove local settings, cached files, and local Gemini interaction logs.
- Drawingo has no account-based child profile or server-side drawing history to delete. Google Cloud may retain service or abuse-monitoring data under its own terms and retention practices; clearing the app does not delete Google's records.
- Parents may contact us to request access to, correction of, or deletion of information handled by Drawingo. We will respond to requests for data within our control. Please do not email a child's drawing, voice recording, or other sensitive details.

The multiplication question protecting Parent Settings is a child-resistant UI gate. It is not a verifiable parental consent process. If applicable law requires verifiable parental consent for a feature, that feature should remain disabled until the required consent process is in place.

## Security and retention

Network requests use HTTPS. The app does not request camera, microphone, location, contacts, or external-storage permissions. Local drawings are not kept as a personal gallery; the current drawing exists in app state, while cloud interaction logs and cached content follow the local retention described above. Backend operational logs follow the configured Google Cloud log retention. Google Cloud's service-level processing is governed by Google's terms and retention practices.

No method of electronic storage or transmission is completely secure. We limit the app's permissions and use encrypted network connections, but do not claim that this eliminates all risk.

## Changes to this policy

We will update this page when the app's data practices change. The date at the top shows when it was last revised. Parents should review it before enabling optional cloud features.

## Contact

For privacy questions or requests, contact the Drawingo app operator at [winsimash@gmail.com](mailto:winsimash@gmail.com). We use the parent's email and message to respond and resolve the request, then delete them when no longer needed unless a legal obligation requires retention.
