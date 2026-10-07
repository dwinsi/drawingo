/**
 * Drawingo Premium Google Cloud Backend powered by Application Default Credentials (ADC).
 * Uses @google/genai (Google Gen AI SDK) via ADC.
 * GCP Project: project-2154682a-9280-4a32-a72
 */

const { GoogleGenAI, GenerateVideosOperation } = require('@google/genai');

const PROJECT_ID = process.env.GCP_PROJECT_ID || 'project-2154682a-9280-4a32-a72';
const LOCATION = process.env.GCP_LOCATION || 'us-central1';
const VEO_MODEL = process.env.VEO_MODEL || 'veo-3.1-lite-generate-001';

// 1. Initialize Google Gen AI client via ADC (Vertex AI / Enterprise mode)
const ai = new GoogleGenAI({
  enterprise: true,
  project: PROJECT_ID,
  location: LOCATION
});

/**
 * Express handler for classifying a drawing into an animation scene.
 */
async function handleAnalyzeDrawing(req, res) {
  let prompt = '';
  let generationAttempts = [];
  try {
    const { imageBase64, mimeType = 'image/png', interactionId = 'unknown' } = req.body;
    if (!imageBase64) {
      return res.status(400).json({ error: 'imageBase64 parameter is required.' });
    }

    const cleanBase64 = imageBase64.replace(/^data:image\/\w+;base64,/, '');

    prompt = `
      Analyze this drawing for an adult creative drawing app. Treat the drawing as ambiguous; choose a broad subject if uncertain.
      Do not infer personal attributes about the artist or describe anything outside the image.

      Respond in EXACTLY this format:
      SCENE: [OCEAN_LEAP or SKY_FLIGHT or SPACE_LAUNCH or LAND_SAFARI or ABSTRACT_FLOW]
      SUBJECT: [Short neutral 1-3 word description, or Doodle if uncertain]

      SCENE GUIDELINES:
      - Use OCEAN_LEAP for dolphins, fish, sea turtles, octopuses, boats, water creatures.
      - Use SKY_FLIGHT for birds, butterflies, bees, airplanes, clouds, flying creatures.
      - Use SPACE_LAUNCH for rockets, cars, spaceships, stars, comets, fast vehicles.
      - Use LAND_SAFARI for lions, bears, elephants, dinosaurs, dogs, cats, land animals.
      - Use ABSTRACT_FLOW for general doodles, scribbles, flowers, shapes, and other ambiguous sketches.
    `;

    const { responseText, modelName, attempts } = await generateGenAiContent(cleanBase64, mimeType, prompt);
    generationAttempts = attempts;

    const parsed = parseGeminiResponse(responseText);
    console.log(JSON.stringify({ event: 'gemini_drawing_complete', interactionId, modelName }));
    res.status(200).json({
      ...parsed,
      interactionId,
      debug: { model: modelName, prompt, responseText, attempts }
    });
  } catch (err) {
    console.error('Error in analyzeDrawing endpoint:', err);
    res.status(500).json({
      interactionId: req.body?.interactionId || 'unknown',
      error: 'Gemini drawing analysis failed.',
      debug: { prompt, attempts: err.modelAttempts || generationAttempts, error: err.message }
    });
  }
}

/** Starts an image-to-video generation. This endpoint is deliberately disabled by default. */
async function handleGenerateVideo(req, res) {
  if (process.env.ENABLE_VEO_GENERATION !== 'true') {
    return res.status(503).json({ error: 'Video generation is not enabled on this backend.' });
  }
  const { imageBase64, mimeType = 'image/png', prompt = '' } = req.body || {};
  if (typeof imageBase64 !== 'string' || !['image/png', 'image/jpeg'].includes(mimeType)) {
    return res.status(400).json({ error: 'A PNG or JPEG image is required.' });
  }
  const cleanBase64 = imageBase64.replace(/^data:image\/(png|jpeg);base64,/, '');
  if (!/^[A-Za-z0-9+/]*={0,2}$/.test(cleanBase64) || cleanBase64.length > 7_000_000) {
    return res.status(413).json({ error: 'Image is invalid or exceeds the 5 MB limit.' });
  }
  if (typeof prompt !== 'string' || prompt.length > 800) {
    return res.status(400).json({ error: 'Prompt must be 800 characters or fewer.' });
  }
  try {
    const operation = await ai.models.generateVideos({
      model: VEO_MODEL,
      source: {
        prompt: [
          'Create a calm, visually coherent short animation based on this original artwork.',
          'Preserve the main subject and hand-drawn character of the artwork.',
          'No text, logos, frightening imagery, or sudden camera movement.',
          prompt.trim()
        ].filter(Boolean).join(' '),
        image: { imageBytes: cleanBase64, mimeType }
      },
      config: {
        numberOfVideos: 1,
        durationSeconds: 4,
        aspectRatio: '16:9',
        resolution: '720p',
        generateAudio: false,
        personGeneration: 'dont_allow',
        negativePrompt: 'text, captions, logos, frightening imagery, violent actions, abrupt camera movement'
      }
    });
    if (!operation.name) throw new Error('Video operation did not return an identifier.');
    console.log(JSON.stringify({ event: 'veo_video_submitted', model: VEO_MODEL, operation: operation.name }));
    return res.status(202).json({ operationId: operation.name, model: VEO_MODEL });
  } catch (err) {
    console.error('Error in generateVideo endpoint:', err.message);
    return res.status(502).json({ error: 'Video generation could not be started.' });
  }
}

/** Polls an existing Veo long-running operation and returns the completed MP4 as base64. */
async function handleVideoStatus(req, res) {
  if (process.env.ENABLE_VEO_GENERATION !== 'true') {
    return res.status(503).json({ error: 'Video generation is not enabled on this backend.' });
  }
  const { operationId } = req.body || {};
  const expectedPrefix = `projects/${PROJECT_ID}/locations/${LOCATION}/publishers/google/models/${VEO_MODEL}/operations/`;
  if (typeof operationId !== 'string' || !operationId.startsWith(expectedPrefix) || operationId.length > 512) {
    return res.status(400).json({ error: 'Invalid video operation identifier.' });
  }
  try {
    const op = new GenerateVideosOperation();
    op.name = operationId;
    const operation = await ai.operations.getVideosOperation({ operation: op });
    if (!operation.done) return res.status(202).json({ status: 'processing' });
    if (operation.error) return res.status(502).json({ error: 'Video generation failed.' });
    const video = operation.response?.generatedVideos?.[0]?.video;
    if (!video?.videoBytes) return res.status(502).json({ error: 'The video result was unavailable.' });
    return res.status(200).json({ status: 'complete', model: VEO_MODEL, mimeType: 'video/mp4', videoBase64: video.videoBytes });
  } catch (err) {
    console.error('Error polling Veo operation:', err.message);
    return res.status(502).json({ error: 'Video status could not be retrieved.' });
  }
}

async function generateGenAiContent(cleanBase64, mimeType, prompt) {
  const modelsToTry = [
    'gemini-2.5-flash',
    'gemini-2.5-pro'
  ];

  let lastError = null;
  const attempts = [];

  for (const modelName of modelsToTry) {
    try {
      const response = await ai.models.generateContent({
        model: modelName,
        config: {
          maxOutputTokens: 300,
          safetySettings: [
            'HARM_CATEGORY_HARASSMENT',
            'HARM_CATEGORY_HATE_SPEECH',
            'HARM_CATEGORY_SEXUALLY_EXPLICIT',
            'HARM_CATEGORY_DANGEROUS_CONTENT'
          ].map((category) => ({ category, threshold: 'BLOCK_LOW_AND_ABOVE' }))
        },
        contents: [
          {
            role: 'user',
            parts: [
              ...(cleanBase64 ? [{ inlineData: { mimeType, data: cleanBase64 } }] : []),
              { text: prompt }
            ]
          }
        ]
      });

      const responseText = response.text;
      attempts.push({
        model: modelName,
        outcome: 'success',
        request: { prompt, mimeType: mimeType || null, includesImage: Boolean(cleanBase64) },
        responseText
      });
      return { responseText, modelName, attempts };
    } catch (err) {
      lastError = err;
      attempts.push({
        model: modelName,
        outcome: 'error',
        request: { prompt, mimeType: mimeType || null, includesImage: Boolean(cleanBase64) },
        error: err.message
      });
      console.warn(`Gemini model ${modelName} notice: ${err.message}, trying next model...`);
      continue;
    }
  }

  const finalError = lastError || new Error('No available Gemini model found.');
  finalError.modelAttempts = attempts;
  throw finalError;
}

function parseGeminiResponse(text) {
  let sceneType = 'ABSTRACT_FLOW';
  let subjectName = 'Doodle';

  const lines = text.split('\n');

  for (const line of lines) {
    const trimmed = line.trim();
    if (/^SCENE:/i.test(trimmed)) {
      const val = trimmed.split(':')[1].trim().toUpperCase();
      if (val.includes('OCEAN') || val.includes('DOLPHIN') || val.includes('FISH')) sceneType = 'OCEAN_LEAP';
      else if (val.includes('SKY') || val.includes('BIRD') || val.includes('FLIGHT')) sceneType = 'SKY_FLIGHT';
      else if (val.includes('SPACE') || val.includes('ROCKET') || val.includes('CAR')) sceneType = 'SPACE_LAUNCH';
      else if (val.includes('LAND') || val.includes('SAFARI') || val.includes('ANIMAL')) sceneType = 'LAND_SAFARI';
      else sceneType = 'ABSTRACT_FLOW';
    } else if (/^SUBJECT:/i.test(trimmed)) {
      subjectName = trimmed.split(':')[1].trim();
    }
  }

  return { sceneType, subjectName };
}

module.exports = {
  handleAnalyzeDrawing,
  handleGenerateVideo,
  handleVideoStatus
};
