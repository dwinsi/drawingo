---
layout: default
title: Drawingo Privacy Policy
permalink: /
---

# Drawingo Privacy Policy

**Effective Date:** November 13, 2026  
**Last Updated:** November 13, 2026

Drawingo is a free, universal recreational drawing and animation app for Android. We believe your creative workspace should be entirely private, calm, and distraction-free.

**Drawingo is completely free to use and collects no personal data.** There are no accounts, no advertisements, no tracking or analytics SDKs, and no cloud uploads. Everything you draw, animate, or import stays strictly on your device.

---

## 1. Zero Data Collection Policy

Drawingo does not collect, transmit, store, sell, or share any personal information or device data. Specifically:
- **No Personal Information:** We do not collect names, email addresses, phone numbers, contacts, or demographic data.
- **No Accounts or Logins:** You do not need to register, log in, or provide credentials to use any feature in the app.
- **No Analytics or Telemetry:** The app contains no third-party analytics SDKs (such as Google Analytics or Firebase) to track user behavior, feature usage, or crash logs.
- **No Advertising:** The app contains no advertising SDKs, ad networks, or tracking libraries. It is 100% ad-free.
- **No In-App Purchases:** The app is completely free with no hidden charges, paywalls, or subscriptions.

---

## 2. No Internet Access or Remote Communications

The Drawingo Android application does not request the `android.permission.INTERNET` permission in its manifest.

Because the app lacks network access:
- Your drawings, strokes, and animations are never transmitted across the internet.
- The app cannot communicate with any remote servers, external APIs, or cloud AI services.
- All drawing rendering, watercolor and crayon physics, flipbook playback, and motion effects execute 100% locally on your device hardware.

---

## 3. Data Stored Locally on Your Device

All content created in Drawingo resides solely within your device's private, sandboxed app storage:

- **Drawings, Layers, and Projects:** Vector strokes, layers, project names, color palettes, and animation frames are saved locally in an on-device database (`Room`).
- **Reference & Background Images:** If you choose to import an image or take a reference photo, the app invokes Android's standard system photo picker or camera flow. The app only accesses the specific image you explicitly choose and copies it into private app storage. Drawingo does not scan, index, or access your photo library.
- **Project Previews:** Small thumbnails are generated on-device and stored locally to display in your project gallery.
- **Exported Images:** When you choose to export your artwork as a PNG or JPEG, the file is written to your device's local media library using Android's standard media store APIs.

Android automatic cloud backup (`android:allowBackup="false"`) is explicitly disabled in the app manifest to prevent your drawings from being automatically copied to cloud backup services.

---

## 4. Your Control and Data Deletion

You have complete ownership and control over your data:

- **Deleting Projects:** You can delete individual projects from within the app gallery at any time, which permanently removes their strokes, layers, and previews from the local database.
- **Complete Erasure:** You can permanently erase all app data by clearing Drawingo's storage in Android **Settings > Apps > Drawingo > Storage & Cache > Clear Storage**, or by uninstalling the application.
- **Exported Files:** Any images you exported to your device's photo gallery can be managed or deleted directly through your device's Gallery or Photos app.

---

## 5. Children's Privacy (COPPA / GDPR-K Compliance)

Drawingo is designed as a calm, accessible creative tool for people of all ages, including children and families.

Because Drawingo does not collect, store, or transmit any personal data from anyone, it complies fully with the Children’s Online Privacy Protection Act (COPPA), the General Data Protection Regulation (GDPR / GDPR-K), and Google Play Families Policy requirements. We do not knowingly or unknowingly collect or solicit information from children under the age of 13 (or applicable age in your jurisdiction).

---

## 6. Third-Party Services and Hosting

The Drawingo mobile application contains no third-party services, APIs, or trackers.

This privacy policy document is published online via GitHub Pages. When you visit this policy website in a web browser, GitHub may collect standard web server logs (such as IP address, browser type, and time of request) in accordance with the [GitHub Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-privacy-statement). The policy website uses no advertising cookies or web analytics scripts.

---

## 7. Changes to This Privacy Policy

If we ever update Drawingo's features in a manner that affects data handling or device permissions, we will update this privacy policy with a revised "Last Updated" date. Any updates will remain accessible both within the project documentation and at the public policy URL.

---

## 8. Contact Us

If you have any questions, feedback, or concerns regarding this Privacy Policy or Drawingo's privacy practices, please contact us at:

**Email:** [winsimash@gmail.com](mailto:winsimash@gmail.com)  
**Application:** Drawingo for Android
