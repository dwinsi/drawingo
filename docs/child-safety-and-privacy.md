# Drawingo child safety and privacy notes

This document describes the current app behavior and the release work still needed. It is an engineering inventory, not a legal compliance certification.

## Data and permissions

- The app requests only `INTERNET`. It does not request camera, microphone, location, contacts, phone, advertising ID, or storage permissions.
- Drawing strokes and selected templates are held in app state. The current UI does not save drawings to the Room database or upload them automatically.
- Optional drawing analysis is off by default. A parent can enable it in Parent Settings; only then can the child-triggered animation action send a PNG of the current drawing to the configured HTTPS backend. The production backend forwards it to Google's Gemini model. The app does not embed or store a Gemini API key.
- When cloud analysis is enabled, generated rhyme text can also be sent to the same backend for Google Cloud Text-to-Speech. Otherwise, speech uses Android's on-device text-to-speech.
- The stock sketch catalogue is fetched from the backend independently of the AI sharing setting. Remote template images are fetched only over HTTPS from `storage.googleapis.com`; no drawing is included in those requests.
- App backup is disabled. There are no ads, analytics, accounts, social sharing, or third-party ad SDKs in the current Android app.

## Parent controls

- A parent gate protects settings. The gate currently uses a randomly generated addition question. It is a child-resistant UI gate only; it is **not** a verifiable parental consent mechanism.
- The cloud analysis switch is off unless a parent turns it on and saves settings.
- The daily screen-time setting now tracks foreground use by local calendar day and blocks the canvas when the limit is reached. A parent can change the limit through the gate. This local control can be bypassed by changing device time or clearing app data, so it is not a managed-device enforcement guarantee.
- Optional kiosk mode uses Android lock task mode where device-owner policy permits it. Device-owner provisioning is a separate deployment step.
- A custom backend URL must be HTTPS and cannot contain credentials, a path, query, or fragment. Cleartext traffic is disabled in the manifest.

## Release requirements still open

Before distributing this as a child-directed service, the operator must verify the applicable Google Play Families and privacy-law requirements, publish an accurate privacy policy and in-app notice, confirm Gemini/Cloud TTS data-processing and retention terms for child-directed use, define request/log retention and deletion processes, and implement a legally sufficient parental consent flow where required. The current arithmetic gate and cloud opt-in are useful safeguards but do not establish legal consent by themselves. Play Console audience, Data safety, and content-rating declarations also need to match the final release behavior.

References: [Google Play Families Policy](https://support.google.com/googleplay/android-developer/answer/9893335) and [FTC COPPA Rule](https://www.ftc.gov/legal-library/browse/rules/childrens-online-privacy-protection-rule-coppa).
