#!/usr/bin/env bash
# Deploy Drawingo Premium ADC Backend to Cloud Run on GCP Project project-2154682a-9280-4a32-a72

set -e

PROJECT_ID="project-2154682a-9280-4a32-a72"
REGION="us-central1"
SERVICE_NAME="drawingo-backend"

echo "🚀 Deploying Drawingo Backend to Cloud Run in project ${PROJECT_ID}..."

gcloud run deploy ${SERVICE_NAME} \
  --project="${PROJECT_ID}" \
  --region="${REGION}" \
  --source=. \
  --platform=managed \
  --allow-unauthenticated \
  --memory=512Mi \
  --cpu=1 \
  --set-env-vars="GCP_PROJECT_ID=${PROJECT_ID},GCP_LOCATION=${REGION},GOOGLE_GENAI_USE_VERTEXAI=true,GOOGLE_GENAI_USE_ENTERPRISE=true"

echo "✨ Deployment to Google Cloud Run finished successfully!"
