# Legacy cloud infrastructure notes

> **Status: legacy / not part of the intended product architecture.** Drawingo's repository-level requirements prohibit cloud uploads and require local-first behavior. These notes describe infrastructure and source that remain in the repository; they do not confirm that any service, account, permission, or deployment is currently active. Do not deploy, enable, or grant access based on this document.

## What remains in the repository

- `functions/` contains a Node.js HTTP backend with drawing-analysis and Veo video-generation routes.
- Android source contains `AdcBackendClient`, a default Cloud Run URL, settings for an optional cloud backend, and an app-private interaction logger.
- `functions/index.js` gates video generation behind `ENABLE_VEO_GENERATION`; that source-level flag does not prove the deployed service is configured the same way.
- The Android canvas currently exposes settings and API/cloud logs. No visible canvas action invoking the video-generation method was found during the source review.

These components conflict with the current product requirements and should be removed before a release is described as strictly local-only. This document is retained only as a migration/removal aid.

## Historical deployment record

Older versions of this document recorded a Google Cloud project, a Cloud Run service, service accounts, Workload Identity Federation, and a GitHub Actions deployment workflow. Those values are intentionally not treated here as current operational facts. Before any account cleanup, access review, or resource deletion, an authorized project owner must inspect the Google Cloud Console, IAM policy, GitHub repository settings, and deployment history directly.

No service-account credentials, API keys, or other secrets should be placed in the repository. Do not restore long-lived service-account keys to GitHub Actions; if a separately authorized infrastructure project is ever approved, use narrowly scoped federation and review IAM permissions first.

## Local source inspection

The legacy backend source is under `functions/`. The Android client and settings are under `app/src/main/java/com/example/drawingo/net/`, `AppPreferences.kt`, and `ui/AppSettingsDialog.kt`. This section is a repository map, not instructions to start or deploy the backend.

## Removal checklist

1. Remove the Android network client, cloud-AI preferences and settings UI, cloud logging screen/logger, and any manifest network permission that is no longer needed.
2. Remove backend routes and deployment automation if no approved product use remains.
3. Search the full repository for backend URLs, Gemini/Veo references, service-account identifiers, and cloud deployment workflows.
4. Verify the built app has no unexpected network behavior and update the privacy policy only after reviewing the resulting release build.
5. Have the project owner independently verify and clean up any remote cloud resources or IAM grants. Repository changes do not remove remote resources.
