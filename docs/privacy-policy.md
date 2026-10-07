---
layout: default
title: Drawingo Privacy Policy
permalink: /
---

# Drawingo Privacy Policy

**Last updated: October 7, 2026**

Drawingo is being developed as a creative drawing and animation app intended for adults aged 18 and older. The app does not verify a user's age. This policy describes information handled by the current Android app and its optional cloud AI features. Veo video generation is implemented in the app but remains disabled on the deployed backend by default.

## Information Drawingo processes

### Drawings and local diagnostics

Drawings are held in app memory while you use the canvas. The current app does not provide a drawing gallery or save your drawings as account content. Android backup is disabled.

When cloud AI is enabled and you explicitly request drawing analysis, the app sends a PNG image of the canvas to the configured HTTPS backend. The backend forwards the image and analysis prompt to Google Cloud's Gemini service and returns a scene type and short subject label.

If Veo video generation is enabled on the configured backend, choosing **Create AI video** sends a JPEG image of the canvas and the motion prompt you entered to that backend. The backend forwards them to Google Cloud's Veo video-generation service. The generated MP4 is returned to the app and stored temporarily in app-private cache. It is deleted when you dismiss the player or request another video; if the app is terminated unexpectedly, Android may retain the cache file until it cleans app cache.

The app stores interaction logs in app-private files. Analysis logs can include the request and response, including the drawing image encoded in the request. Video-generation logs include the prompt, request metadata, operation status, and output size; they do not include the returned MP4 bytes. Logs remain on the device until you clear app data or uninstall the app. The app does not provide log export.

### Optional cloud AI

Cloud AI is off by default and can be enabled in the app's Settings. The app requires a separate user action and disclosure before sending a drawing for analysis or video generation. The deployed backend has Veo generation disabled by default (`ENABLE_VEO_GENERATION` is not enabled). When cloud AI is enabled, the backend URL must use HTTPS. A custom backend URL is used as the sole backend host.

The backend does not intentionally save drawing images or maintain a drawing history. It may emit operational diagnostics. Google Cloud Run may also create service and request logs. The project's log buckets have configured retention periods of 30 days and 400 days for their respective categories. Google Cloud's handling of submitted data is subject to the applicable Google Cloud agreement and [Vertex AI data governance information](https://cloud.google.com/vertex-ai/generative-ai/docs/vertex-ai-zero-data-retention). Google describes in-memory caching for some Gemini models and prompt logging for abuse monitoring in that documentation.

Google Cloud's current [Service Specific Terms](https://cloud.google.com/terms/service-terms) restrict using a Generative AI Service in an app or online service directed to or likely to be accessed by people under 18. Drawingo's product direction is adults 18+, but the app does not enforce an age check. The operator must ensure the service is not directed to or likely to be accessed by under-18 users before enabling cloud AI.

### Other data and permissions

The current app has no account, advertising, analytics, social sharing, camera, microphone, contacts, or location feature. It does not record or transcribe voice. Internet access is used for optional cloud AI. Network calls use HTTPS.

This policy is hosted on GitHub Pages. GitHub may process technical information about visits under [GitHub's Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-privacy-statement). The policy site does not use advertising or analytics cookies.

## How information is used and shared

Drawing data is used locally to render your canvas and animation. If you enable cloud AI and request analysis, the drawing image is shared with the configured backend and Google Cloud Gemini to identify a broad subject and select an animation scene. If Veo is enabled and you request a video, the drawing and motion prompt are shared with the backend and Google Cloud Veo to generate an MP4. The app operator does not sell personal information or use an analytics SDK.

Google may process submitted data and service information under the terms and policies applicable to the Google Cloud account. Review [Google Cloud's Privacy Notice](https://cloud.google.com/terms/cloud-privacy-notice) and the Vertex AI data governance link above.

## Your choices and deletion

- Leave cloud AI disabled in Settings to keep drawing and animation on-device. For Veo, the separate confirmation explains the upload before you submit. Video generation also remains unavailable while the backend feature flag is off.
- Clear the app's storage or uninstall the app to remove local settings and private interaction logs.
- Drawingo has no account-based drawing history or server-side gallery to delete. Clearing the app does not delete Google Cloud service or abuse-monitoring records.
- For privacy questions or requests about data handled by Drawingo, contact us at [winsimash@gmail.com](mailto:winsimash@gmail.com). Please do not email drawings or other sensitive information.

## Security and retention

Custom backend addresses are restricted to HTTPS. Local interaction logs and app settings are stored in app-private storage and are removed when app data is cleared or the app is uninstalled. Backend and Google Cloud data are subject to their configured service and log retention practices described above.

No electronic storage or transmission method is completely secure. We limit app permissions and use encrypted network connections, but cannot guarantee that this eliminates all risk.

## Changes to this policy

We will update this page when the app's data practices change. The date above shows when it was last revised.

## Contact

For privacy questions or requests, contact the Drawingo app operator at [winsimash@gmail.com](mailto:winsimash@gmail.com). We use your email and message to respond and resolve the request, then delete them when no longer needed unless a legal obligation requires retention.
