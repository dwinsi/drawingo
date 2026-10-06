# Drawingo — Cloud Infrastructure & Accounts Documentation

This document records the Google Cloud Platform (GCP) configurations, service accounts, Workload Identity Federation (WIF) setup, and GitHub Actions CI/CD pipeline powering the Drawingo backend.

---

## 1. Project Information

| Property | Value |
| :--- | :--- |
| **GCP Project ID** | `project-2154682a-9280-4a32-a72` |
| **GCP Project Number** | `357002186662` |
| **Primary Region** | `us-central1` |
| **GitHub Repository** | `dwinsi/drawingo` (Branch: `main`) |

---

## 2. Cloud Run Service (Production Backend)

The Drawingo backend runs on **Google Cloud Run** as a fully managed serverless container.

- **Service Name:** `drawingo-backend`
- **Live URL:** `https://drawingo-backend-357002186662.us-central1.run.app`
- **Runtime Environment:** Node.js 20 (Express)
- **Authentication:** ADC (Application Default Credentials) via Compute Service Account
- **Memory / CPU:** 512 MiB / 1 vCPU
- **Concurrency / Scaling:** Auto-scales from 0 instances (serverless cost optimization)

### Production Endpoints

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/health` | `GET` | Service liveness and ADC status check |
| `/analyzeDrawing` | `POST` | Toddler drawing classification + nursery rhyme generation via Gemini 2.5 Flash |
| `/synthesizeSpeech` | `POST` | High-quality text-to-speech audio synthesis (hi-IN & en-US) |

### AI Models & SDK
- **SDK:** Google Gen AI SDK (`@google/genai@2.27.0`)
- **Primary Model:** `gemini-2.5-flash` (Vertex AI / Enterprise mode)
- **Fallback Model:** `gemini-2.5-pro`
- **Voice Synthesis:** Google Cloud Text-to-Speech (`hi-IN-Neural2-A`, `en-US-Journey-F`)

---

## 3. Service Accounts & IAM Roles

### A. Cloud Run Runtime Service Account
Runs the backend container in production and calls Vertex AI / Cloud TTS:
- **Email:** `357002186662-compute@developer.gserviceaccount.com`
- **Key IAM Roles:**
  - `roles/aiplatform.user` — Access to Gemini 2.5 models on Vertex AI
  - `roles/logging.logWriter` — Cloud Logging access
  - `roles/storage.admin` — Cloud Storage access

### B. CI/CD Deployment Service Account
Used by GitHub Actions to build and deploy updates:
- **Email:** `drawingo-cicd@project-2154682a-9280-4a32-a72.iam.gserviceaccount.com`
- **Display Name:** `Drawingo GitHub Actions CI/CD`
- **Key IAM Roles:**
  - `roles/run.admin` — Deploy and manage Cloud Run revisions
  - `roles/artifactregistry.writer` — Push build artifacts
  - `roles/cloudbuild.builds.editor` — Execute Cloud Build for containerization
  - `roles/storage.objectAdmin` — Stage source packages in Cloud Storage
  - `roles/iam.serviceAccountUser` — Deploy service acting as the compute runtime SA
  - `roles/iam.workloadIdentityUser` — Impersonated by GitHub Actions via OIDC token

---

## 4. Workload Identity Federation (Keyless GitHub Actions Auth)

To ensure maximum security and avoid long-lived JSON service account keys, GitHub Actions authenticates using **Workload Identity Federation (OIDC)**:

- **Workload Identity Pool:**
  `projects/357002186662/locations/global/workloadIdentityPools/github-pool`
- **Identity Provider:**
  `projects/357002186662/locations/global/workloadIdentityPools/github-pool/providers/github-provider`
- **OIDC Issuer:** `https://token.actions.githubusercontent.com`
- **Attribute Condition:**
  `assertion.repository == 'dwinsi/Agentic-Cinema' || assertion.repository == 'dwinsi/drawingo'`
- **Principal Set Bound:**
  `principalSet://iam.googleapis.com/projects/357002186662/locations/global/workloadIdentityPools/github-pool/attribute.repository/dwinsi/drawingo`

> **Note:** Because WIF is bound directly to the `dwinsi/drawingo` repository, developers do **not** need to manually generate or paste any secret keys into GitHub Secrets.

---

## 5. GitHub Actions Pipeline

- **Workflow File:** `.github/workflows/backend-ci-cd.yml`
- **Trigger Conditions:**
  - Push to `main` modifying files in `functions/**`
  - Pull request targeting `main` modifying `functions/**`
  - Manual trigger via GitHub Actions UI (`workflow_dispatch`)

### Workflow Stages:
1. **`test-and-build`:** Sets up Node.js 20, installs dependencies with `npm ci`, and validates code syntax with `npm test`.
2. **`deploy-cloud-run`:** Authenticates to Google Cloud via WIF, deploys to Cloud Run with `gcloud run deploy`, and pings `/health` to verify live status.

---

## 6. Local Development & Useful Commands

### Local Server
Run backend locally on `http://localhost:8080`:
```bash
cd functions
npm install
npm start
```

### ADB Reverse for Android Testing (Physical Device over USB)
```bash
adb reverse tcp:8080 tcp:8080
```

### Manual Cloud Deployment
```bash
cd functions
./deploy.sh
```

### View Live Logs in Cloud Run
```bash
gcloud beta run services logs read drawingo-backend \
  --project=project-2154682a-9280-4a32-a72 \
  --region=us-central1 \
  --limit=50
```

### Quick Verification via cURL
```bash
# Health Check
curl -s https://drawingo-backend-357002186662.us-central1.run.app/health

# Drawing Analysis
curl -s -X POST https://drawingo-backend-357002186662.us-central1.run.app/analyzeDrawing \
  -H "Content-Type: application/json" \
  -d '{"imageBase64": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==", "mimeType": "image/png"}'
```

---

## 7. How the Android App Connects to the Cloud Backend

The Android app communicates with the Cloud Run backend automatically via `AdcBackendClient.kt`:

1. **Automatic Discovery & Priority Order:**
   - When the user draws and taps the magic wand button, `DrawingViewModel` calls `AdcBackendClient.analyzeDrawing(canvasBitmap)`.
   - `AdcBackendClient` checks the following endpoints in priority order:
     1. **Custom URL** (if configured in Parent Gate settings)
     2. **Cloud Run Production Endpoint:** `https://drawingo-backend-357002186662.us-central1.run.app`
     3. **Local Dev Fallbacks:** `http://10.0.2.2:8080`, `http://127.0.0.1:8080`, `http://localhost:8080`
   - If Cloud Run is unreachable or offline, the app automatically falls back to client-side direct Gemini API (`GeminiMagicManager`) and offline text-to-speech.

2. **Network Security & Timeouts:**
   - Requests are sent over secure HTTPS (`android.permission.INTERNET`).
   - Timeouts are set to **10s connect / 15s read** to gracefully handle serverless container cold starts and multimodal AI inference.

3. **Parent Gate Override:**
   - Parents can open the Parent Gate settings in the app to inspect or override the backend URL if testing a local dev environment.

