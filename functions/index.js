/**
 * Drawingo Premium Google Cloud Backend powered by Application Default Credentials (ADC).
 * Uses @google/genai (Google Gen AI SDK) via ADC.
 * GCP Project: project-2154682a-9280-4a32-a72
 */

const { GoogleGenAI } = require('@google/genai');

const PROJECT_ID = process.env.GCP_PROJECT_ID || 'project-2154682a-9280-4a32-a72';
const LOCATION = process.env.GCP_LOCATION || 'us-central1';

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
      SCENE: [OCEAN_LEAP or SKY_FLIGHT or SPACE_LAUNCH or LAND_SAFARI or MAGIC_DANCE]
      SUBJECT: [Short neutral 1-3 word description, or Doodle if uncertain]

      SCENE GUIDELINES:
      - Use OCEAN_LEAP for dolphins, fish, sea turtles, octopuses, boats, water creatures.
      - Use SKY_FLIGHT for birds, butterflies, bees, airplanes, clouds, flying creatures.
      - Use SPACE_LAUNCH for rockets, cars, spaceships, stars, comets, fast vehicles.
      - Use LAND_SAFARI for lions, bears, elephants, dinosaurs, dogs, cats, land animals.
      - Use MAGIC_DANCE for general doodles, scribbles, flowers, shapes, suns.
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
  let sceneType = 'MAGIC_DANCE';
  let subjectName = 'Magic Drawing';

  const lines = text.split('\n');

  for (const line of lines) {
    const trimmed = line.trim();
    if (/^SCENE:/i.test(trimmed)) {
      const val = trimmed.split(':')[1].trim().toUpperCase();
      if (val.includes('OCEAN') || val.includes('DOLPHIN') || val.includes('FISH')) sceneType = 'OCEAN_LEAP';
      else if (val.includes('SKY') || val.includes('BIRD') || val.includes('FLIGHT')) sceneType = 'SKY_FLIGHT';
      else if (val.includes('SPACE') || val.includes('ROCKET') || val.includes('CAR')) sceneType = 'SPACE_LAUNCH';
      else if (val.includes('LAND') || val.includes('SAFARI') || val.includes('ANIMAL')) sceneType = 'LAND_SAFARI';
      else sceneType = 'MAGIC_DANCE';
    } else if (/^SUBJECT:/i.test(trimmed)) {
      subjectName = trimmed.split(':')[1].trim();
    }
  }

  return { sceneType, subjectName };
}

module.exports = {
  handleAnalyzeDrawing
};
