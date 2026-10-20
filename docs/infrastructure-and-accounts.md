# Legacy cloud infrastructure note

Drawingo's Android app is intended to run locally. The Android network client, request logger, cloud settings, backend source, and backend deployment workflow have been removed from this repository.

This repository change does **not** disable, delete, or verify any Google Cloud service, service account, IAM grant, GitHub identity provider, or stored cloud log that may have been created previously. Their current status is unknown from the source tree. An authorized project owner must check Google Cloud and GitHub directly before deciding whether to disable or remove remote resources.

Do not restore the old cloud deployment workflow, add service-account keys, or send drawing content to a remote service. If a future product decision changes the local-only requirement, review security, consent, data retention, and the privacy policy before designing a new data flow.
