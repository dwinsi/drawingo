/**
 * Local Express server runner for Drawingo ADC Backend.
 * Allows testing Application Default Credentials (ADC) locally after running:
 * `gcloud auth application-default login`
 */

// Configure environment variables before requiring @google/genai to silence startup warning
if (!process.env.GEMINI_API_KEY) {
  process.env.GEMINI_API_KEY = 'unused-adc-mode';
}
process.env.GOOGLE_GENAI_USE_VERTEXAI = 'true';
process.env.GOOGLE_GENAI_USE_ENTERPRISE = 'true';
process.env.GOOGLE_CLOUD_PROJECT = process.env.GCP_PROJECT_ID || 'project-2154682a-9280-4a32-a72';
process.env.GOOGLE_CLOUD_LOCATION = process.env.GCP_LOCATION || 'us-central1';

const express = require('express');
const cors = require('cors');
const { exec } = require('child_process');
const { handleAnalyzeDrawing } = require('./index');

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(express.json({ limit: '20mb' }));

app.post('/analyzeDrawing', handleAnalyzeDrawing);

app.get('/health', (req, res) => {
  res.status(200).json({ status: 'ok', project: 'project-2154682a-9280-4a32-a72', auth: 'ADC' });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Drawingo ADC Backend running on http://0.0.0.0:${PORT}`);
  console.log(`GCP Project: project-2154682a-9280-4a32-a72`);
  console.log(`ADC Auth: Active`);

  if (!process.env.K_SERVICE) {
    // Automatically reverse ADB port 8080 for USB connected mobile devices
    exec('adb reverse tcp:8080 tcp:8080', (err, stdout, stderr) => {
      if (!err) {
        console.log(`📱 ADB Port 8080 successfully mapped over USB to physical mobile device!`);
      }
    });
  }
});
