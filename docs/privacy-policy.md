---
layout: default
title: Drawingo Privacy Policy
permalink: /
---

# Drawingo Privacy Policy

**Last updated: October 7, 2026**

Drawingo is being developed as a creative drawing and animation app intended for adults aged 18 and older. The app does not verify a user's age. This policy describes information handled by the current Android app and its optional cloud animation feature.

## Information Drawingo processes

### Drawings and local diagnostics

Drawings are held in app memory while you use the canvas. The current app does not provide a drawing gallery or save your drawings as account content. Android backup is disabled.

When cloud animation is enabled and you request drawing analysis, the app sends a PNG image of the canvas to the configured HTTPS backend. The backend forwards the image and analysis prompt to Google Cloud Vertex AI's Gemini service and returns a scene type and short subject label.

The app stores detailed Gemini interaction logs in app-private files. These logs can include the complete request and response, including the drawing image encoded in the request. They remain on the device until you clear app data or uninstall the app. The app does not provide log export.

### Optional cloud animation

Cloud analysis is off by default and can be enabled in the app's Settings. If it is off or unavailable, the app displays a local animation preview without sending the drawing to the backend. When enabled, the backend URL must use HTTPS. A custom backend URL is used as the sole backend host.

The backend does not intentionally save drawing images or maintain a drawing history. It may emit operational diagnostics. Google Cloud Run may also create service and request logs. The project's log buckets have configured retention periods of 30 days and 400 days for their respective categories. Google Cloud's handling of submitted data is subject to the applicable Google Cloud agreement and [Vertex AI data governance information](https://cloud.google.com/vertex-ai/generative-ai/docs/vertex-ai-zero-data-retention). Google describes in-memory caching for some Gemini models and prompt logging for abuse monitoring in that documentation.

Google Cloud's current [Service Specific Terms](https://cloud.google.com/terms/service-terms) restrict using a Generative AI Service in an app or online service directed to or likely to be accessed by people under 18. Drawingo's product direction is adults 18+, but the app does not enforce an age check. The operator must ensure the service is not directed to or likely to be accessed by under-18 users before enabling cloud AI.

### Other data and permissions

The current app has no account, advertising, analytics, social sharing, camera, microphone, contacts, or location feature. It does not record or transcribe voice. Internet access is used for optional cloud animation. Network calls use HTTPS.

This policy is hosted on GitHub Pages. GitHub may process technical information about visits under [GitHub's Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-privacy-statement). The policy site does not use advertising or analytics cookies.

## How information is used and shared

Drawing data is used locally to render your canvas and animation. If you enable and use cloud animation, the drawing image is shared with the configured backend and Google Cloud Vertex AI to identify a broad subject and select an animation scene. The app operator does not sell personal information or use an analytics SDK.

Google may process submitted data and service information under the terms and policies applicable to the Google Cloud account. Review [Google Cloud's Privacy Notice](https://cloud.google.com/terms/cloud-privacy-notice) and the Vertex AI data governance link above.

## Your choices and deletion

- Leave cloud animation disabled in Settings to keep drawing analysis on-device.
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
