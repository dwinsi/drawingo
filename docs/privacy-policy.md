---
layout: default
title: Drawingo Privacy Policy
permalink: /
---

# Drawingo Privacy Policy

**Last updated: October 20, 2026**

Drawingo is an Android drawing app. The product is intended to work locally and does not require an account. This policy describes the data handling visible in the current source, including legacy cloud code that has not yet been removed. The app's implementation and its local-only product requirements are not fully aligned; until the legacy cloud pathways are removed and verified, do not assume that every build is offline-only.

## Information stored on your device

- **Drawings and projects:** Drawing projects, strokes, layers, frame data, and project names are stored in a local Room database in app-private storage.
- **Reference images:** If you choose an image or take a photo using the Android system camera flow, the app copies the selected image into app-private storage and uses it as a drawing background.
- **Exports:** If you choose PNG or JPEG export, the image is saved to the device's media library. Source also contains an MP4/time-lapse encoder, though no current user interface path to invoke it was found.
- **Preferences and diagnostics:** App preferences are stored locally. The source includes a local interaction logger for legacy cloud requests; entries may include request and response details and an encoded drawing image. The app includes a screen that can display and clear these logs.

Local app data is generally removed when you clear the app's storage or uninstall it. Exported images in the device media library are separate and must be deleted there. The Android manifest disables automatic app backup.

## Network and cloud processing

The product requirement is that drawings and AI features remain on-device. However, the current Android source still includes an Internet permission, an optional cloud-AI setting, a default Cloud Run backend address, and a client method capable of sending a JPEG drawing and a prompt to that backend for video generation. The video endpoint can then forward the content to Google Cloud's Veo service. The settings screen describes cloud AI, but no visible action in the current canvas UI was found that invokes the video-generation client method.

Because this legacy code remains, this policy cannot promise that all builds are strictly local-only. If a build invokes the legacy cloud method, the drawing image and prompt are transmitted to the configured HTTPS backend and may be processed by that backend and Google Cloud. The client also records request and response details in app-private logs. A custom backend address is required to use HTTPS; the client has a default Cloud Run URL when no custom address is set. The app disables cleartext network traffic.

The repository's intended direction is to remove these cloud pathways. Once that removal is complete, the shipped binary has been checked for network behavior, and the data-flow review confirms local-only operation, this section and the rest of this policy must be revised to describe the verified release accurately.

## Permissions and third-party services

The app uses Android's system photo selection and camera capture activities so you can explicitly choose or take a background image. The inspected source does not contain in-app voice recording, voice transcription, location collection, analytics, advertising, accounts, or social features.

This privacy policy is hosted on GitHub Pages. GitHub may process technical information about visits under [GitHub's Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-privacy-statement). The policy site does not intentionally use advertising or analytics cookies.

## Your choices

- Choose whether to import a photo; the app does not need an image until you select or capture one.
- Use the app's settings to leave the legacy cloud-AI option off. This toggle is not a substitute for removing and verifying the dormant cloud code.
- Clear local app storage or uninstall the app to remove app-private projects, preferences, and logs. Delete exported media separately from the device's gallery.
- For privacy questions or requests, contact [winsimash@gmail.com](mailto:winsimash@gmail.com). Please do not email drawings or other sensitive information.

## Security and changes

Drawingo stores project data in app-private storage and rejects cleartext network traffic. No electronic storage or transmission method is completely secure. We will update this policy when the app's data practices change, and will review it against the actual release build.

## Contact

For privacy questions or requests, contact the Drawingo app operator at [winsimash@gmail.com](mailto:winsimash@gmail.com). We use your email and message to respond and resolve the request, then delete them when no longer needed unless a legal obligation requires retention.
