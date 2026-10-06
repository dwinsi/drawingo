#!/usr/bin/env node
/**
 * CLI tool to add new stock sketches to Drawingo database & Cloud Storage.
 * Usage:
 *   node scripts/add-sketch.js --title="Baby Dino" --category="WILD_ANIMALS" --file="path/to/img.png" --emoji="🦕"
 *   node scripts/add-sketch.js --title="Spaceship" --category="CELESTIAL" --url="https://..." --emoji="🛸"
 */

const fs = require('fs');
const path = require('path');
const { addSketch } = require('../sketchManager');

const args = process.argv.slice(2);
const params = {};

for (const arg of args) {
  if (arg.startsWith('--')) {
    const [key, ...vals] = arg.slice(2).split('=');
    params[key] = vals.join('=');
  }
}

if (!params.title || (!params.file && !params.url)) {
  console.log(`
Drawingo Stock Sketch Importer
--------------------------------
Usage:
  node scripts/add-sketch.js --title="<Title>" --category="<Category>" [--file="<Path>" | --url="<URL>"] [--emoji="<Emoji>"] [--tags="tag1,tag2"]

Categories:
  CELESTIAL, SEA_ANIMALS, WILD_ANIMALS, OTHER

Options:
  --title     (Required) Human friendly name e.g. "Baby Dinosaur"
  --file      Local path to PNG/JPEG/SVG line art
  --url       Remote public HTTPS image URL
  --category  CELESTIAL | SEA_ANIMALS | WILD_ANIMALS | OTHER (Default: OTHER)
  --emoji     Emoji icon for UI display (Default: 🎨)
  --difficulty EASY | MEDIUM | HARD (Default: EASY)
  --tags      Comma separated tags e.g. "dino,safari,cute"
  `);
  process.exit(1);
}

async function run() {
  console.log(`🚀 Adding new stock sketch: "${params.title}" (${params.category || 'OTHER'})...`);

  let imageBase64 = null;
  let imageUrl = params.url || null;

  if (params.file) {
    const fullPath = path.resolve(params.file);
    if (!fs.existsSync(fullPath)) {
      console.error(`❌ File not found: ${fullPath}`);
      process.exit(1);
    }
    const buffer = fs.readFileSync(fullPath);
    imageBase64 = buffer.toString('base64');
  }

  const tags = params.tags ? params.tags.split(',').map(t => t.trim()) : [];

  const created = await addSketch({
    title: params.title,
    category: params.category || 'OTHER',
    emoji: params.emoji || '🎨',
    difficulty: params.difficulty || 'EASY',
    tags,
    imageBase64,
    imageUrl
  });

  console.log(`\n🎉 Successfully added stock sketch!`);
  console.log(`  ID:          ${created.id}`);
  console.log(`  Title:       ${created.title}`);
  console.log(`  Category:    ${created.category}`);
  console.log(`  Emoji:       ${created.emoji}`);
  console.log(`  Image URL:   ${created.imageUrl}`);
  console.log(`  Created:     ${created.createdAt}`);
  console.log(`\nThe new sketch is live in the database and immediately available in the Drawingo app!\n`);
}

run().catch(err => {
  console.error('❌ Error adding sketch:', err.message);
  process.exit(1);
});
