const fs = require('fs');
const path = require('path');
const { Storage } = require('@google-cloud/storage');

const PROJECT_ID = process.env.GCP_PROJECT_ID || 'project-2154682a-9280-4a32-a72';
const BUCKET_NAME = process.env.SKETCHES_BUCKET || 'drawingo-sketches-project-2154682a-9280-4a32-a72';
const LOCAL_CATALOG_PATH = path.join(__dirname, 'data/catalog.json');

const storage = new Storage({ projectId: PROJECT_ID });
const bucket = storage.bucket(BUCKET_NAME);

let memoryCatalog = null;
let lastFetchTime = 0;
const CACHE_TTL_MS = 60 * 1000; // 1 minute memory cache

/**
 * Loads the sketches catalog from GCS with fallback to local file.
 */
async function getCatalog() {
  const now = Date.now();
  if (memoryCatalog && (now - lastFetchTime < CACHE_TTL_MS)) {
    return memoryCatalog;
  }

  try {
    const file = bucket.file('catalog.json');
    const [exists] = await file.exists();
    if (exists) {
      const [contents] = await file.download();
      memoryCatalog = JSON.parse(contents.toString('utf8'));
      lastFetchTime = now;
      return memoryCatalog;
    }
  } catch (err) {
    console.warn('⚠️ Could not load catalog from GCS, using local fallback:', err.message);
  }

  if (fs.existsSync(LOCAL_CATALOG_PATH)) {
    try {
      const data = fs.readFileSync(LOCAL_CATALOG_PATH, 'utf8');
      memoryCatalog = JSON.parse(data);
      lastFetchTime = now;
      return memoryCatalog;
    } catch (e) {
      console.error('Error reading local catalog:', e);
    }
  }

  memoryCatalog = [];
  lastFetchTime = now;
  return memoryCatalog;
}

/**
 * Saves updated catalog to both GCS and local disk.
 */
async function saveCatalog(catalog) {
  memoryCatalog = catalog;
  lastFetchTime = Date.now();

  const jsonStr = JSON.stringify(catalog, null, 2);

  // 1. Save locally
  try {
    fs.mkdirSync(path.dirname(LOCAL_CATALOG_PATH), { recursive: true });
    fs.writeFileSync(LOCAL_CATALOG_PATH, jsonStr, 'utf8');
  } catch (err) {
    console.warn('Could not save catalog locally:', err.message);
  }

  // 2. Save to GCS
  try {
    const file = bucket.file('catalog.json');
    await file.save(jsonStr, {
      contentType: 'application/json',
      metadata: {
        cacheControl: 'public, max-age=60'
      }
    });
    console.log(`☁️ Synced updated catalog (${catalog.length} items) to gs://${BUCKET_NAME}/catalog.json`);
  } catch (err) {
    console.error('Failed to sync catalog to GCS:', err.message);
  }
}

/**
 * Returns all sketches, optionally filtered by category.
 */
async function getSketches(category) {
  const catalog = await getCatalog();
  if (!category || category.toUpperCase() === 'ALL') {
    return catalog;
  }
  return catalog.filter(s => s.category.toUpperCase() === category.toUpperCase());
}

/**
 * Adds a new stock sketch to the database & Cloud Storage.
 */
async function addSketch({ title, category = 'OTHER', emoji = '🎨', difficulty = 'EASY', tags = [], imageBase64, imageUrl }) {
  if (!title) {
    throw new Error('title is required');
  }

  const catalog = await getCatalog();
  const safeTitle = title.toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_+|_+$/g, '');
  const id = `${category.toLowerCase()}_${safeTitle}_${Date.now().toString().slice(-4)}`;

  let finalImageUrl = imageUrl;

  if (imageBase64) {
    const cleanBase64 = imageBase64.replace(/^data:image\/\w+;base64,/, '');
    const buffer = Buffer.from(cleanBase64, 'base64');
    const filename = `sketches/${id}.png`;
    const file = bucket.file(filename);

    await file.save(buffer, {
      contentType: 'image/png',
      metadata: {
        cacheControl: 'public, max-age=86400'
      }
    });

    finalImageUrl = `https://storage.googleapis.com/${BUCKET_NAME}/${filename}`;
    console.log(`📤 Uploaded new sketch image to ${finalImageUrl}`);
  }

  if (!finalImageUrl) {
    throw new Error('Either imageBase64 or imageUrl must be provided');
  }

  const newSketch = {
    id,
    title,
    category: category.toUpperCase(),
    emoji,
    difficulty: difficulty.toUpperCase(),
    tags: Array.isArray(tags) ? tags : [tags].filter(Boolean),
    imageUrl: finalImageUrl,
    thumbnailUrl: finalImageUrl,
    createdAt: new Date().toISOString()
  };

  catalog.push(newSketch);
  await saveCatalog(catalog);

  return newSketch;
}

module.exports = {
  getSketches,
  addSketch,
  getCatalog
};
