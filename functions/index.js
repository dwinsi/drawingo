/**
 * Drawingo Premium Google Cloud Backend powered by Application Default Credentials (ADC).
 * Uses @google/genai (Google Gen AI SDK) and @google-cloud/text-to-speech via ADC.
 * GCP Project: project-2154682a-9280-4a32-a72
 */

const { GoogleGenAI } = require('@google/genai');
const textToSpeech = require('@google-cloud/text-to-speech');

const PROJECT_ID = process.env.GCP_PROJECT_ID || 'project-2154682a-9280-4a32-a72';
const LOCATION = process.env.GCP_LOCATION || 'us-central1';

// 1. Initialize Google Gen AI client via ADC (Vertex AI / Enterprise mode)
const ai = new GoogleGenAI({
  enterprise: true,
  project: PROJECT_ID,
  location: LOCATION
});

// 2. Initialize Cloud Text-To-Speech Client via ADC
const ttsClient = new textToSpeech.TextToSpeechClient();

/**
 * Express handler for analyzing child's drawing and classifying animation scene.
 */
async function handleAnalyzeDrawing(req, res) {
  try {
    const { imageBase64, mimeType = 'image/png' } = req.body;
    if (!imageBase64) {
      return res.status(400).json({ error: 'imageBase64 parameter is required.' });
    }

    const cleanBase64 = imageBase64.replace(/^data:image\/\w+;base64,/, '');

    const prompt = `
      You are a warm, magical AI friend for kids aged 1 to 8 years (fans of Like Nastya, Peppa Pig, ChuChu TV, Cocomelon).
      Look at this child's drawing or scribbles and analyze what it is!

      Respond in EXACTLY this format:
      SCENE: [OCEAN_LEAP or SKY_FLIGHT or SPACE_LAUNCH or LAND_SAFARI or MAGIC_DANCE]
      SUBJECT: [Short 1-3 word name, e.g., Dolphin, Bird, Rocket, Car, Lion, Flower, Doodle]
      RHYME: [2 to 4 line super catchy, rhythmic nursery rhyme in English or Hindi/Hinglish with sound effects and emojis!]

      SCENE GUIDELINES:
      - Use OCEAN_LEAP for dolphins, fish, sea turtles, octopuses, boats, water creatures.
      - Use SKY_FLIGHT for birds, butterflies, bees, airplanes, clouds, flying creatures.
      - Use SPACE_LAUNCH for rockets, cars, spaceships, stars, comets, fast vehicles.
      - Use LAND_SAFARI for lions, bears, elephants, dinosaurs, dogs, cats, land animals.
      - Use MAGIC_DANCE for general doodles, scribbles, flowers, shapes, suns.
    `;

    const { responseText, modelName } = await generateGenAiContent(cleanBase64, mimeType, prompt);

    const parsed = parseGeminiResponse(responseText);
    console.log(`✨ Drawing analyzed via Google Gen AI ADC (model: ${modelName}): ${parsed.subjectName} (${parsed.sceneType})`);
    res.status(200).json(parsed);
  } catch (err) {
    console.error('Error in analyzeDrawing endpoint:', err);
    res.status(500).json({
      sceneType: 'MAGIC_DANCE',
      subjectName: 'Magic Drawing',
      rhymeText: '✨ Chanda mama door ke, naye dost aaye door ke!\nYour magic drawing is sparkling with joy! 🎨'
    });
  }
}

async function generateGenAiContent(cleanBase64, mimeType, prompt) {
  const modelsToTry = [
    'gemini-2.5-flash',
    'gemini-2.5-pro'
  ];

  let lastError = null;

  for (const modelName of modelsToTry) {
    try {
      const response = await ai.models.generateContent({
        model: modelName,
        contents: [
          {
            role: 'user',
            parts: [
              { inlineData: { mimeType, data: cleanBase64 } },
              { text: prompt }
            ]
          }
        ]
      });

      const responseText = response.text;
      return { responseText, modelName };
    } catch (err) {
      lastError = err;
      console.warn(`Gemini model ${modelName} notice: ${err.message}, trying next model...`);
      continue;
    }
  }

  throw lastError || new Error('No available Gemini model found.');
}

/**
 * Express handler for synthesizing natural human voice audio via Cloud TTS ADC.
 */
async function handleSynthesizeSpeech(req, res) {
  try {
    const { text } = req.body;
    if (!text) {
      return res.status(400).json({ error: 'text parameter is required.' });
    }

    const cleanText = text.replace(/[\uD83C-\uDBFF\uDC00-\uDFFF\u2600-\u27FF]/g, '').trim();

    const isHindi = /[\u0900-\u097F]/.test(cleanText) ||
      /chanda|titli|pyari|dost|machhli/i.test(cleanText);

    const languageCode = isHindi ? 'hi-IN' : 'en-US';
    const voiceName = isHindi ? 'hi-IN-Neural2-A' : 'en-US-Journey-F';

    const ttsRequest = {
      input: { text: cleanText },
      voice: { languageCode: languageCode, name: voiceName },
      audioConfig: { audioEncoding: 'MP3', speakingRate: 0.92 }
    };

    const [ttsResponse] = await ttsClient.synthesizeSpeech(ttsRequest);
    const audioBase64 = ttsResponse.audioContent.toString('base64');

    console.log(`🔊 Natural voice synthesized via Cloud TTS ADC (${languageCode} / ${voiceName})`);
    res.status(200).json({
      audioBase64,
      languageCode: languageCode,
      mimeType: 'audio/mp3'
    });
  } catch (err) {
    console.error('Error in synthesizeSpeech endpoint:', err);
    res.status(500).json({ error: err.message });
  }
}

function parseGeminiResponse(text) {
  let sceneType = 'MAGIC_DANCE';
  let subjectName = 'Magic Drawing';
  let rhymeText = text;

  const lines = text.split('\n');
  const rhymeLines = [];

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
    } else if (/^RHYME:/i.test(trimmed)) {
      rhymeLines.push(trimmed.split(':')[1].trim());
    } else if (trimmed.length > 0 && !/^SCENE:/i.test(trimmed) && !/^SUBJECT:/i.test(trimmed)) {
      rhymeLines.push(trimmed);
    }
  }

  if (rhymeLines.length > 0) {
    rhymeText = rhymeLines.join('\n');
  }

  return { sceneType, subjectName, rhymeText };
}

module.exports = {
  handleAnalyzeDrawing,
  handleSynthesizeSpeech
};
