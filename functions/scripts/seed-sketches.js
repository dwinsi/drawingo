/**
 * Generator for Drawingo 18 high-resolution toddler coloring stock sketches.
 * Renders bold, clear line art (stroke-width 8-12px) on 800x800 canvas.
 * Produces crisp PNGs with transparent background, perfect for toddler coloring!
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const OUTPUT_DIR = path.join(__dirname, '../data/generated-sketches');
if (!fs.existsSync(OUTPUT_DIR)) {
  fs.mkdirSync(OUTPUT_DIR, { recursive: true });
}

const SKETCHES = [
  // ==========================================
  // 🌟 CELESTIAL OBJECTS
  // ==========================================
  {
    id: 'celestial_sun',
    title: 'Smiling Sun',
    category: 'CELESTIAL',
    emoji: '☀️',
    difficulty: 'EASY',
    tags: ['sun', 'space', 'sky', 'warm', 'happy'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Sun Rays -->
        <g stroke="#1A1A24" stroke-width="12" stroke-linecap="round" fill="none">
          <line x1="400" y1="90" x2="400" y2="190" />
          <line x1="400" y1="610" x2="400" y2="710" />
          <line x1="90" y1="400" x2="190" y2="400" />
          <line x1="610" y1="400" x2="710" y2="400" />
          <line x1="180" y1="180" x2="250" y2="250" />
          <line x1="550" y1="550" x2="620" y2="620" />
          <line x1="620" y1="180" x2="550" y2="250" />
          <line x1="180" y1="620" x2="250" y2="550" />
        </g>
        <!-- Center Body -->
        <circle cx="400" cy="400" r="190" stroke="#1A1A24" stroke-width="14" fill="none" />
        <!-- Big Cute Eyes -->
        <circle cx="330" cy="370" r="24" fill="#1A1A24" />
        <circle cx="338" cy="362" r="8" fill="#FFFFFF" />
        <circle cx="470" cy="370" r="24" fill="#1A1A24" />
        <circle cx="478" cy="362" r="8" fill="#FFFFFF" />
        <!-- Rosy Cheeks -->
        <circle cx="290" cy="425" r="26" stroke="#1A1A24" stroke-width="8" stroke-dasharray="6,8" fill="none" />
        <circle cx="510" cy="425" r="26" stroke="#1A1A24" stroke-width="8" stroke-dasharray="6,8" fill="none" />
        <!-- Big Happy Smile -->
        <path d="M 330 430 Q 400 510 470 430" stroke="#1A1A24" stroke-width="12" stroke-linecap="round" fill="none" />
        <path d="M 355 455 Q 400 495 445 455" stroke="#1A1A24" stroke-width="8" fill="none" />
      </svg>
    `
  },
  {
    id: 'celestial_moon',
    title: 'Crescent Moon & Star',
    category: 'CELESTIAL',
    emoji: '🌙',
    difficulty: 'EASY',
    tags: ['moon', 'star', 'night', 'sky', 'dream'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Crescent Moon Body -->
        <path d="M 450 120 C 230 140 160 360 210 540 C 260 700 460 720 540 680 C 370 650 300 500 320 370 C 340 240 450 120 450 120 Z"
              stroke="#1A1A24" stroke-width="14" stroke-linejoin="round" fill="none" />
        <!-- Moon Sleeping Face -->
        <path d="M 280 370 Q 305 390 330 370" stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none" />
        <path d="M 290 440 Q 325 470 355 435" stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none" />
        <!-- Little Star Friend -->
        <g transform="translate(560, 320)">
          <path d="M 0 -70 L 20 -20 L 70 -15 L 30 20 L 45 70 L 0 40 L -45 70 L -30 20 L -70 -15 L -20 -20 Z"
                stroke="#1A1A24" stroke-width="10" stroke-linejoin="round" fill="none" />
          <circle cx="-15" cy="5" r="7" fill="#1A1A24" />
          <circle cx="15" cy="5" r="7" fill="#1A1A24" />
          <path d="M -10 20 Q 0 28 10 20" stroke="#1A1A24" stroke-width="6" stroke-linecap="round" fill="none" />
        </g>
        <!-- Tiny Night Sparkles -->
        <circle cx="580" cy="180" r="12" stroke="#1A1A24" stroke-width="8" fill="none" />
        <circle cx="210" cy="220" r="10" stroke="#1A1A24" stroke-width="8" fill="none" />
        <circle cx="630" cy="520" r="12" stroke="#1A1A24" stroke-width="8" fill="none" />
      </svg>
    `
  },
  {
    id: 'celestial_star',
    title: 'Twinkle Star',
    category: 'CELESTIAL',
    emoji: '⭐',
    difficulty: 'EASY',
    tags: ['star', 'twinkle', 'cute', 'sky'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Big Chubby 5-point Star -->
        <path d="M 400 110 L 470 280 L 650 300 L 510 420 L 555 600 L 400 500 L 245 600 L 290 420 L 150 300 L 330 280 Z"
              stroke="#1A1A24" stroke-width="14" stroke-linejoin="round" stroke-linecap="round" fill="none" />
        <!-- Big Cute Eyes -->
        <circle cx="345" cy="380" r="22" fill="#1A1A24" />
        <circle cx="351" cy="373" r="7" fill="#FFFFFF" />
        <circle cx="455" cy="380" r="22" fill="#1A1A24" />
        <circle cx="461" cy="373" r="7" fill="#FFFFFF" />
        <!-- Cheeks -->
        <circle cx="310" cy="425" r="20" stroke="#1A1A24" stroke-width="7" stroke-dasharray="5,6" fill="none" />
        <circle cx="490" cy="425" r="20" stroke="#1A1A24" stroke-width="7" stroke-dasharray="5,6" fill="none" />
        <!-- Smile -->
        <path d="M 360 425 Q 400 470 440 425" stroke="#1A1A24" stroke-width="11" stroke-linecap="round" fill="none" />
      </svg>
    `
  },
  {
    id: 'celestial_saturn',
    title: 'Planet Saturn',
    category: 'CELESTIAL',
    emoji: '🪐',
    difficulty: 'MEDIUM',
    tags: ['saturn', 'planet', 'space', 'ring', 'galaxy'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Background Stars -->
        <circle cx="180" cy="180" r="14" stroke="#1A1A24" stroke-width="8" fill="none" />
        <circle cx="640" cy="200" r="18" stroke="#1A1A24" stroke-width="8" fill="none" />
        <circle cx="660" cy="620" r="14" stroke="#1A1A24" stroke-width="8" fill="none" />
        <!-- Saturn Sphere -->
        <circle cx="400" cy="400" r="180" stroke="#1A1A24" stroke-width="14" fill="none" />
        <!-- Planetary Rings -->
        <g transform="rotate(-22 400 400)">
          <ellipse cx="400" cy="400" rx="360" ry="90" stroke="#1A1A24" stroke-width="14" fill="none" />
          <ellipse cx="400" cy="400" rx="310" ry="70" stroke="#1A1A24" stroke-width="10" stroke-dasharray="16,12" fill="none" />
        </g>
        <!-- Friendly Planet Face -->
        <circle cx="345" cy="380" r="20" fill="#1A1A24" />
        <circle cx="351" cy="374" r="6" fill="#FFFFFF" />
        <circle cx="455" cy="380" r="20" fill="#1A1A24" />
        <circle cx="461" cy="374" r="6" fill="#FFFFFF" />
        <path d="M 360 425 Q 400 465 440 425" stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none" />
      </svg>
    `
  },
  {
    id: 'celestial_rocket',
    title: 'Cosmic Rocket',
    category: 'CELESTIAL',
    emoji: '🚀',
    difficulty: 'MEDIUM',
    tags: ['rocket', 'spaceship', 'blastoff', 'launch', 'space'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Booster Flame -->
        <path d="M 340 570 Q 400 730 400 730 Q 400 730 460 570 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <path d="M 365 570 Q 400 660 400 660 Q 400 660 435 570 Z" stroke="#1A1A24" stroke-width="8" stroke-linejoin="round" fill="none" />
        <!-- Fins -->
        <path d="M 310 440 L 220 570 L 310 550 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <path d="M 490 440 L 580 570 L 490 550 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <!-- Rocket Body -->
        <path d="M 400 120 C 490 220 490 460 480 570 L 320 570 C 310 460 310 220 400 120 Z"
              stroke="#1A1A24" stroke-width="14" stroke-linejoin="round" fill="none" />
        <!-- Nosecone Divider -->
        <path d="M 340 240 Q 400 270 460 240" stroke="#1A1A24" stroke-width="12" fill="none" />
        <!-- Round Porthole Window -->
        <circle cx="400" cy="380" r="60" stroke="#1A1A24" stroke-width="12" fill="none" />
        <circle cx="400" cy="380" r="45" stroke="#1A1A24" stroke-width="6" fill="none" />
        <!-- Smiling Face inside Porthole -->
        <circle cx="380" cy="375" r="10" fill="#1A1A24" />
        <circle cx="420" cy="375" r="10" fill="#1A1A24" />
        <path d="M 385 395 Q 400 410 415 395" stroke="#1A1A24" stroke-width="6" stroke-linecap="round" fill="none" />
      </svg>
    `
  },
  {
    id: 'celestial_comet',
    title: 'Shooting Comet',
    category: 'CELESTIAL',
    emoji: '☄️',
    difficulty: 'EASY',
    tags: ['comet', 'shooting star', 'space', 'speed', 'tail'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Comet Streaks -->
        <g stroke="#1A1A24" stroke-width="12" stroke-linecap="round" fill="none">
          <path d="M 480 400 Q 280 400 140 330" />
          <path d="M 500 460 Q 300 520 120 500" />
          <path d="M 490 350 Q 320 280 180 200" />
          <path d="M 470 510 Q 340 620 200 660" />
        </g>
        <!-- Comet Head -->
        <circle cx="550" cy="420" r="130" stroke="#1A1A24" stroke-width="14" fill="none" />
        <!-- Big Eyes -->
        <circle cx="510" cy="400" r="20" fill="#1A1A24" />
        <circle cx="515" cy="394" r="6" fill="#FFFFFF" />
        <circle cx="590" cy="400" r="20" fill="#1A1A24" />
        <circle cx="595" cy="394" r="6" fill="#FFFFFF" />
        <!-- Smile -->
        <path d="M 525 445 Q 550 475 575 445" stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none" />
      </svg>
    `
  },

  // ==========================================
  // 🌊 SEA ANIMALS
  // ==========================================
  {
    id: 'sea_whale',
    title: 'Baby Whale',
    category: 'SEA_ANIMALS',
    emoji: '🐋',
    difficulty: 'EASY',
    tags: ['whale', 'ocean', 'sea', 'water', 'spout'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Water Blowhole Spout -->
        <g stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none">
          <path d="M 330 250 Q 330 140 260 110" />
          <path d="M 330 250 Q 330 110 330 80" />
          <path d="M 330 250 Q 330 140 400 110" />
        </g>
        <!-- Whale Body -->
        <path d="M 170 450 C 170 300 450 250 560 380 C 640 450 710 420 740 370 C 740 470 660 520 580 500 C 500 580 270 580 170 450 Z"
              stroke="#1A1A24" stroke-width="14" stroke-linejoin="round" fill="none" />
        <!-- Belly Pleats -->
        <path d="M 230 480 C 310 560 480 550 540 490" stroke="#1A1A24" stroke-width="10" fill="none" />
        <!-- Swimming Flipper -->
        <path d="M 350 470 C 340 550 420 540 430 460 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <!-- Big Cute Eye -->
        <circle cx="270" cy="400" r="22" fill="#1A1A24" />
        <circle cx="276" cy="392" r="7" fill="#FFFFFF" />
        <!-- Cheerful Smile -->
        <path d="M 220 440 Q 260 470 290 435" stroke="#1A1A24" stroke-width="11" stroke-linecap="round" fill="none" />
        <!-- Ocean Waves at bottom -->
        <path d="M 100 670 Q 200 630 300 670 Q 400 710 500 670 Q 600 630 700 670" stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none" />
      </svg>
    `
  },
  {
    id: 'sea_turtle',
    title: 'Sea Turtle',
    category: 'SEA_ANIMALS',
    emoji: '🐢',
    difficulty: 'MEDIUM',
    tags: ['turtle', 'sea', 'ocean', 'shell', 'swim'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Flippers -->
        <path d="M 280 320 C 140 220 110 320 220 380 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <path d="M 520 320 C 660 220 690 320 580 380 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <path d="M 270 520 C 170 560 190 640 270 590 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <path d="M 530 520 C 630 560 610 640 530 590 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <!-- Tail -->
        <path d="M 380 610 L 400 670 L 420 610" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <!-- Turtle Head -->
        <circle cx="400" cy="200" r="75" stroke="#1A1A24" stroke-width="14" fill="none" />
        <circle cx="365" cy="185" r="14" fill="#1A1A24" />
        <circle cx="368" cy="180" r="5" fill="#FFFFFF" />
        <circle cx="435" cy="185" r="14" fill="#1A1A24" />
        <circle cx="438" cy="180" r="5" fill="#FFFFFF" />
        <path d="M 380 230 Q 400 250 420 230" stroke="#1A1A24" stroke-width="8" stroke-linecap="round" fill="none" />
        <!-- Big Patterned Shell -->
        <ellipse cx="400" cy="450" rx="190" ry="170" stroke="#1A1A24" stroke-width="14" fill="none" />
        <!-- Shell Hexagon Scutes -->
        <polygon points="400,360 450,390 450,450 400,480 350,450 350,390" stroke="#1A1A24" stroke-width="10" fill="none" />
        <line x1="400" y1="360" x2="400" y2="280" stroke="#1A1A24" stroke-width="10" />
        <line x1="450" y1="390" x2="550" y2="350" stroke="#1A1A24" stroke-width="10" />
        <line x1="450" y1="450" x2="570" y2="480" stroke="#1A1A24" stroke-width="10" />
        <line x1="400" y1="480" x2="400" y2="620" stroke="#1A1A24" stroke-width="10" />
        <line x1="350" y1="450" x2="230" y2="480" stroke="#1A1A24" stroke-width="10" />
        <line x1="350" y1="390" x2="250" y2="350" stroke="#1A1A24" stroke-width="10" />
      </svg>
    `
  },
  {
    id: 'sea_dolphin',
    title: 'Jumping Dolphin',
    category: 'SEA_ANIMALS',
    emoji: '🐬',
    difficulty: 'MEDIUM',
    tags: ['dolphin', 'sea', 'ocean', 'jump', 'playful'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Dolphin Arched Body -->
        <path d="M 230 450 C 230 250 480 200 620 350 C 680 410 730 480 730 480 C 730 480 660 480 610 440 C 510 500 370 530 280 490 C 210 510 180 500 150 470 C 180 440 210 450 230 450 Z"
              stroke="#1A1A24" stroke-width="14" stroke-linejoin="round" fill="none" />
        <!-- Dorsal Fin -->
        <path d="M 450 240 C 470 160 540 180 530 260" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <!-- Flipper -->
        <path d="M 380 420 C 370 490 440 480 440 410 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <!-- Tail Fluke -->
        <path d="M 680 430 C 740 370 760 450 710 480 C 760 510 740 590 680 530 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <!-- Big Cute Eye -->
        <circle cx="260" cy="380" r="18" fill="#1A1A24" />
        <circle cx="264" cy="374" r="6" fill="#FFFFFF" />
        <!-- Smile -->
        <path d="M 200 460 Q 240 480 270 450" stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none" />
        <!-- Splash Waves -->
        <path d="M 140 640 Q 260 590 380 640 Q 500 690 620 640" stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none" />
      </svg>
    `
  },
  {
    id: 'sea_octopus',
    title: 'Jolly Octopus',
    category: 'SEA_ANIMALS',
    emoji: '🐙',
    difficulty: 'EASY',
    tags: ['octopus', 'tentacles', 'sea', 'ocean', 'bubbles'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Bulbous Head -->
        <path d="M 230 400 C 210 200 590 200 570 400 C 570 450 510 490 400 490 C 290 490 230 450 230 400 Z"
              stroke="#1A1A24" stroke-width="14" stroke-linejoin="round" fill="none" />
        <!-- Tentacles -->
        <g stroke="#1A1A24" stroke-width="12" stroke-linecap="round" fill="none">
          <path d="M 260 480 Q 200 570 170 660 Q 150 720 210 710 Q 250 700 240 640" />
          <path d="M 330 490 Q 290 600 310 680 Q 320 730 360 700" />
          <path d="M 400 490 Q 400 620 400 710" />
          <path d="M 470 490 Q 510 600 490 680 Q 480 730 440 700" />
          <path d="M 540 480 Q 600 570 630 660 Q 650 720 590 710 Q 550 700 560 640" />
        </g>
        <!-- Big Cute Eyes -->
        <circle cx="340" cy="360" r="24" fill="#1A1A24" />
        <circle cx="347" cy="352" r="8" fill="#FFFFFF" />
        <circle cx="460" cy="360" r="24" fill="#1A1A24" />
        <circle cx="467" cy="352" r="8" fill="#FFFFFF" />
        <!-- Rosy Cheeks -->
        <circle cx="290" cy="410" r="22" stroke="#1A1A24" stroke-width="6" stroke-dasharray="4,6" fill="none" />
        <circle cx="510" cy="410" r="22" stroke="#1A1A24" stroke-width="6" stroke-dasharray="4,6" fill="none" />
        <!-- Happy Smile -->
        <path d="M 360 410 Q 400 460 440 410" stroke="#1A1A24" stroke-width="11" stroke-linecap="round" fill="none" />
        <!-- Ocean Bubbles -->
        <circle cx="620" cy="240" r="25" stroke="#1A1A24" stroke-width="8" fill="none" />
        <circle cx="670" cy="160" r="15" stroke="#1A1A24" stroke-width="7" fill="none" />
        <circle cx="160" cy="280" r="20" stroke="#1A1A24" stroke-width="8" fill="none" />
      </svg>
    `
  },
  {
    id: 'sea_fish',
    title: 'Cute Goldfish',
    category: 'SEA_ANIMALS',
    emoji: '🐠',
    difficulty: 'EASY',
    tags: ['fish', 'goldfish', 'sea', 'water', 'bubbles'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Fish Body -->
        <ellipse cx="380" cy="400" rx="180" ry="140" stroke="#1A1A24" stroke-width="14" fill="none" />
        <!-- Head / Gill Curve -->
        <path d="M 330 270 Q 370 400 330 530" stroke="#1A1A24" stroke-width="10" fill="none" />
        <!-- Big Tail Fin -->
        <path d="M 540 370 C 640 250 720 280 670 400 C 720 520 640 550 540 430 Z" stroke="#1A1A24" stroke-width="14" stroke-linejoin="round" fill="none" />
        <!-- Top Dorsal Fin -->
        <path d="M 320 265 C 380 180 480 200 490 275" stroke="#1A1A24" stroke-width="12" stroke-linecap="round" fill="none" />
        <!-- Side Flipper Fin -->
        <path d="M 370 420 C 420 480 450 450 430 400 Z" stroke="#1A1A24" stroke-width="10" stroke-linejoin="round" fill="none" />
        <!-- Big Sparkly Eye -->
        <circle cx="260" cy="360" r="25" fill="#1A1A24" />
        <circle cx="268" cy="350" r="8" fill="#FFFFFF" />
        <!-- Kissy Smile -->
        <path d="M 190 410 Q 220 430 240 410" stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none" />
        <!-- Floating Bubbles -->
        <circle cx="160" cy="320" r="22" stroke="#1A1A24" stroke-width="8" fill="none" />
        <circle cx="130" cy="230" r="16" stroke="#1A1A24" stroke-width="7" fill="none" />
        <circle cx="110" cy="150" r="10" stroke="#1A1A24" stroke-width="6" fill="none" />
      </svg>
    `
  },
  {
    id: 'sea_seahorse',
    title: 'Baby Seahorse',
    category: 'SEA_ANIMALS',
    emoji: '🫧',
    difficulty: 'MEDIUM',
    tags: ['seahorse', 'sea', 'ocean', 'curl', 'magic'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Seahorse Head & Snout -->
        <path d="M 400 160 C 430 130 480 150 480 200 C 480 230 430 250 410 260 L 460 270 C 470 280 460 300 440 300 L 390 290 C 370 330 390 440 430 480 C 480 530 470 650 390 680 C 340 700 310 650 350 620 C 390 590 390 560 360 520 C 310 460 310 330 350 250 C 320 220 340 160 400 160 Z"
              stroke="#1A1A24" stroke-width="14" stroke-linejoin="round" stroke-linecap="round" fill="none" />
        <!-- Crown / Crest -->
        <path d="M 370 170 L 340 140 L 370 130 L 380 100 L 405 130" stroke="#1A1A24" stroke-width="10" stroke-linejoin="round" fill="none" />
        <!-- Back Fin -->
        <path d="M 310 370 C 260 380 250 440 320 460" stroke="#1A1A24" stroke-width="12" fill="none" />
        <!-- Belly Segments to Color -->
        <path d="M 360 350 Q 410 360 420 380" stroke="#1A1A24" stroke-width="9" fill="none" />
        <path d="M 365 400 Q 420 410 430 430" stroke="#1A1A24" stroke-width="9" fill="none" />
        <path d="M 375 450 Q 430 460 435 480" stroke="#1A1A24" stroke-width="9" fill="none" />
        <!-- Big Cute Eye -->
        <circle cx="400" cy="205" r="18" fill="#1A1A24" />
        <circle cx="405" cy="199" r="6" fill="#FFFFFF" />
      </svg>
    `
  },

  // ==========================================
  // 🦁 WILD ANIMALS
  // ==========================================
  {
    id: 'wild_lion',
    title: 'Brave Little Lion',
    category: 'WILD_ANIMALS',
    emoji: '🦁',
    difficulty: 'EASY',
    tags: ['lion', 'safari', 'wild', 'mane', 'king'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Big Fluffy Mane -->
        <g stroke="#1A1A24" stroke-width="14" fill="none">
          <circle cx="400" cy="380" r="230" />
          <path d="M 400 120 C 440 100 480 140 500 170 C 550 150 590 190 600 240 C 640 260 660 320 640 370 C 670 420 640 480 610 520 C 610 570 560 610 500 600 C 470 640 410 650 370 630 C 320 650 260 620 240 570 C 190 560 160 500 180 440 C 150 390 160 330 200 290 C 200 230 250 180 300 190 C 340 140 380 120 400 120 Z" />
        </g>
        <!-- Lion Ears -->
        <circle cx="280" cy="270" r="35" stroke="#1A1A24" stroke-width="12" fill="none" />
        <circle cx="520" cy="270" r="35" stroke="#1A1A24" stroke-width="12" fill="none" />
        <!-- Face Circle -->
        <circle cx="400" cy="400" r="140" stroke="#1A1A24" stroke-width="12" fill="none" />
        <!-- Cute Eyes -->
        <circle cx="345" cy="370" r="20" fill="#1A1A24" />
        <circle cx="351" cy="364" r="6" fill="#FFFFFF" />
        <circle cx="455" cy="370" r="20" fill="#1A1A24" />
        <circle cx="461" cy="364" r="6" fill="#FFFFFF" />
        <!-- Heart Nose -->
        <path d="M 380 410 L 420 410 L 400 435 Z" fill="#1A1A24" />
        <!-- Smile & Muzzle -->
        <path d="M 400 435 L 400 455" stroke="#1A1A24" stroke-width="8" />
        <path d="M 360 455 Q 400 485 400 455 Q 400 485 440 455" stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none" />
        <!-- Whiskers -->
        <line x1="260" y1="430" x2="310" y2="440" stroke="#1A1A24" stroke-width="8" stroke-linecap="round" />
        <line x1="260" y1="460" x2="310" y2="460" stroke="#1A1A24" stroke-width="8" stroke-linecap="round" />
        <line x1="490" y1="440" x2="540" y2="430" stroke="#1A1A24" stroke-width="8" stroke-linecap="round" />
        <line x1="490" y1="460" x2="540" y2="460" stroke="#1A1A24" stroke-width="8" stroke-linecap="round" />
      </svg>
    `
  },
  {
    id: 'wild_panda',
    title: 'Cute Panda Bear',
    category: 'WILD_ANIMALS',
    emoji: '🐼',
    difficulty: 'EASY',
    tags: ['panda', 'bear', 'bamboo', 'cute', 'wild'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Big Round Panda Ears -->
        <circle cx="230" cy="240" r="65" stroke="#1A1A24" stroke-width="14" fill="#1A1A24" />
        <circle cx="570" cy="240" r="65" stroke="#1A1A24" stroke-width="14" fill="#1A1A24" />
        <!-- Head -->
        <ellipse cx="400" cy="400" rx="210" ry="190" stroke="#1A1A24" stroke-width="14" fill="none" />
        <!-- Characteristic Eye Patches -->
        <ellipse cx="320" cy="380" rx="45" ry="55" transform="rotate(-15 320 380)" stroke="#1A1A24" stroke-width="10" fill="#1A1A24" />
        <ellipse cx="480" cy="380" rx="45" ry="55" transform="rotate(15 480 380)" stroke="#1A1A24" stroke-width="10" fill="#1A1A24" />
        <!-- White Twinkle Pupils -->
        <circle cx="328" cy="370" r="15" fill="#FFFFFF" />
        <circle cx="332" cy="366" r="5" fill="#1A1A24" />
        <circle cx="472" cy="370" r="15" fill="#FFFFFF" />
        <circle cx="468" cy="366" r="5" fill="#1A1A24" />
        <!-- Cute Nose -->
        <ellipse cx="400" cy="440" rx="24" ry="16" fill="#1A1A24" />
        <!-- Happy Smile -->
        <path d="M 400 456 L 400 475" stroke="#1A1A24" stroke-width="8" />
        <path d="M 360 475 Q 400 515 400 475 Q 400 515 440 475" stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none" />
        <!-- Pink Cheeks -->
        <circle cx="270" cy="450" r="26" stroke="#1A1A24" stroke-width="6" stroke-dasharray="4,6" fill="none" />
        <circle cx="530" cy="450" r="26" stroke="#1A1A24" stroke-width="6" stroke-dasharray="4,6" fill="none" />
      </svg>
    `
  },
  {
    id: 'wild_elephant',
    title: 'Sweet Baby Elephant',
    category: 'WILD_ANIMALS',
    emoji: '🐘',
    difficulty: 'MEDIUM',
    tags: ['elephant', 'safari', 'wild', 'trunk', 'ears'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Huge Floppy Ears -->
        <path d="M 310 260 C 130 180 80 430 250 510 C 270 470 290 420 310 390" stroke="#1A1A24" stroke-width="14" stroke-linejoin="round" fill="none" />
        <path d="M 490 260 C 670 180 720 430 550 510 C 530 470 510 420 490 390" stroke="#1A1A24" stroke-width="14" stroke-linejoin="round" fill="none" />
        <!-- Elephant Head -->
        <ellipse cx="400" cy="380" rx="140" ry="150" stroke="#1A1A24" stroke-width="14" fill="none" />
        <!-- Upcurled Happy Trunk -->
        <path d="M 370 460 C 370 600 460 670 480 620 C 490 580 440 560 430 520 C 430 500 430 480 430 460"
              stroke="#1A1A24" stroke-width="14" stroke-linecap="round" stroke-linejoin="round" fill="none" />
        <!-- Water Spritz Droplets from Trunk -->
        <circle cx="500" cy="530" r="12" stroke="#1A1A24" stroke-width="7" fill="none" />
        <circle cx="540" cy="500" r="10" stroke="#1A1A24" stroke-width="7" fill="none" />
        <circle cx="520" cy="460" r="8" stroke="#1A1A24" stroke-width="6" fill="none" />
        <!-- Cute Eyes -->
        <circle cx="340" cy="360" r="20" fill="#1A1A24" />
        <circle cx="345" cy="354" r="6" fill="#FFFFFF" />
        <circle cx="460" cy="360" r="20" fill="#1A1A24" />
        <circle cx="465" cy="354" r="6" fill="#FFFFFF" />
      </svg>
    `
  },
  {
    id: 'wild_bear',
    title: 'Chubby Teddy Bear',
    category: 'WILD_ANIMALS',
    emoji: '🐻',
    difficulty: 'EASY',
    tags: ['bear', 'teddy', 'wild', 'forest', 'cuddly'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Bear Ears -->
        <circle cx="260" cy="240" r="60" stroke="#1A1A24" stroke-width="14" fill="none" />
        <circle cx="260" cy="240" r="35" stroke="#1A1A24" stroke-width="10" fill="none" />
        <circle cx="540" cy="240" r="60" stroke="#1A1A24" stroke-width="14" fill="none" />
        <circle cx="540" cy="240" r="35" stroke="#1A1A24" stroke-width="10" fill="none" />
        <!-- Head -->
        <ellipse cx="400" cy="400" rx="200" ry="180" stroke="#1A1A24" stroke-width="14" fill="none" />
        <!-- Snout Patch -->
        <ellipse cx="400" cy="460" rx="90" ry="70" stroke="#1A1A24" stroke-width="12" fill="none" />
        <!-- Cute Nose -->
        <ellipse cx="400" cy="435" rx="30" ry="20" fill="#1A1A24" />
        <!-- Bear Smile -->
        <path d="M 400 455 L 400 480" stroke="#1A1A24" stroke-width="8" />
        <path d="M 360 480 Q 400 520 400 480 Q 400 520 440 480" stroke="#1A1A24" stroke-width="10" stroke-linecap="round" fill="none" />
        <!-- Big Eyes -->
        <circle cx="330" cy="360" r="22" fill="#1A1A24" />
        <circle cx="336" cy="352" r="7" fill="#FFFFFF" />
        <circle cx="470" cy="360" r="22" fill="#1A1A24" />
        <circle cx="476" cy="352" r="7" fill="#FFFFFF" />
      </svg>
    `
  },
  {
    id: 'wild_giraffe',
    title: 'Tall Happy Giraffe',
    category: 'WILD_ANIMALS',
    emoji: '🦒',
    difficulty: 'MEDIUM',
    tags: ['giraffe', 'safari', 'wild', 'tall', 'spots'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Giraffe Ossicone Horns with knobs -->
        <line x1="360" y1="180" x2="350" y2="100" stroke="#1A1A24" stroke-width="12" stroke-linecap="round" />
        <circle cx="350" cy="95" r="20" fill="#1A1A24" />
        <line x1="440" y1="180" x2="450" y2="100" stroke="#1A1A24" stroke-width="12" stroke-linecap="round" />
        <circle cx="450" cy="95" r="20" fill="#1A1A24" />
        <!-- Large Leaf Ears -->
        <path d="M 320 220 C 220 180 200 280 300 270 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <path d="M 480 220 C 580 180 600 280 500 270 Z" stroke="#1A1A24" stroke-width="12" stroke-linejoin="round" fill="none" />
        <!-- Head -->
        <ellipse cx="400" cy="270" rx="100" ry="120" stroke="#1A1A24" stroke-width="14" fill="none" />
        <!-- Cute Eyes -->
        <circle cx="360" cy="240" r="18" fill="#1A1A24" />
        <circle cx="365" cy="235" r="6" fill="#FFFFFF" />
        <circle cx="440" cy="240" r="18" fill="#1A1A24" />
        <circle cx="445" cy="235" r="6" fill="#FFFFFF" />
        <!-- Muzzle & Smile -->
        <ellipse cx="400" cy="330" rx="70" ry="50" stroke="#1A1A24" stroke-width="10" fill="none" />
        <circle cx="380" cy="325" r="8" fill="#1A1A24" />
        <circle cx="420" cy="325" r="8" fill="#1A1A24" />
        <path d="M 380 355 Q 400 375 420 355" stroke="#1A1A24" stroke-width="8" stroke-linecap="round" fill="none" />
        <!-- Long Neck -->
        <path d="M 350 370 L 320 730" stroke="#1A1A24" stroke-width="14" />
        <path d="M 450 370 L 480 730" stroke="#1A1A24" stroke-width="14" />
        <!-- Spots to Color on Neck -->
        <circle cx="400" cy="450" r="30" stroke="#1A1A24" stroke-width="10" fill="none" />
        <ellipse cx="360" cy="560" rx="30" ry="40" stroke="#1A1A24" stroke-width="10" fill="none" />
        <ellipse cx="440" cy="660" rx="35" ry="35" stroke="#1A1A24" stroke-width="10" fill="none" />
      </svg>
    `
  },
  {
    id: 'wild_monkey',
    title: 'Playful Monkey',
    category: 'WILD_ANIMALS',
    emoji: '🐵',
    difficulty: 'EASY',
    tags: ['monkey', 'jungle', 'wild', 'banana', 'playful'],
    svg: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">
        <!-- Big Round Monkey Ears -->
        <circle cx="210" cy="380" r="70" stroke="#1A1A24" stroke-width="14" fill="none" />
        <circle cx="210" cy="380" r="40" stroke="#1A1A24" stroke-width="10" fill="none" />
        <circle cx="590" cy="380" r="70" stroke="#1A1A24" stroke-width="14" fill="none" />
        <circle cx="590" cy="380" r="40" stroke="#1A1A24" stroke-width="10" fill="none" />
        <!-- Head -->
        <circle cx="400" cy="380" r="180" stroke="#1A1A24" stroke-width="14" fill="none" />
        <!-- Heart Face Mask -->
        <path d="M 400 320 C 340 260 260 280 270 370 C 270 460 350 490 400 500 C 450 490 530 460 530 370 C 540 280 460 260 400 320 Z"
              stroke="#1A1A24" stroke-width="12" fill="none" />
        <!-- Big Eyes -->
        <circle cx="340" cy="350" r="22" fill="#1A1A24" />
        <circle cx="346" cy="342" r="7" fill="#FFFFFF" />
        <circle cx="460" cy="350" r="22" fill="#1A1A24" />
        <circle cx="466" cy="342" r="7" fill="#FFFFFF" />
        <!-- Nostrils -->
        <circle cx="385" cy="410" r="7" fill="#1A1A24" />
        <circle cx="415" cy="410" r="7" fill="#1A1A24" />
        <!-- Cheeky Big Monkey Smile -->
        <path d="M 330 435 Q 400 510 470 435" stroke="#1A1A24" stroke-width="11" stroke-linecap="round" fill="none" />
      </svg>
    `
  }
];

async function main() {
  console.log(`🎨 Generating ${SKETCHES.length} Stock Sketches...`);

  const catalog = [];
  const BUCKET_NAME = 'drawingo-sketches-project-2154682a-9280-4a32-a72';
  const BASE_GCS_URL = `https://storage.googleapis.com/${BUCKET_NAME}/sketches`;

  // Path to Android Assets directory for instant offline support
  const ANDROID_ASSETS_DIR = path.join(__dirname, '../../app/src/main/assets/sketches');
  if (!fs.existsSync(ANDROID_ASSETS_DIR)) {
    fs.mkdirSync(ANDROID_ASSETS_DIR, { recursive: true });
  }

  for (const sketch of SKETCHES) {
    const svgPath = path.join(OUTPUT_DIR, `${sketch.id}.svg`);
    const pngPath = path.join(OUTPUT_DIR, `${sketch.id}.png`);

    // 1. Save SVG
    fs.writeFileSync(svgPath, sketch.svg.trim(), 'utf8');

    // 2. Render SVG to PNG using QuickLook thumbnailer (qlmanage) on macOS
    try {
      execSync(`qlmanage -t -s 800 -o "${OUTPUT_DIR}" "${svgPath}" > /dev/null 2>&1`);
      const generatedThumb = path.join(OUTPUT_DIR, `${sketch.id}.svg.png`);
      if (fs.existsSync(generatedThumb)) {
        fs.renameSync(generatedThumb, pngPath);
      }
    } catch (e) {
      console.warn(`Warning converting ${sketch.id}:`, e.message);
    }

    // 3. Copy to Android Assets for offline bundle
    if (fs.existsSync(pngPath)) {
      fs.copyFileSync(pngPath, path.join(ANDROID_ASSETS_DIR, `${sketch.id}.png`));
    }

    catalog.push({
      id: sketch.id,
      title: sketch.title,
      category: sketch.category,
      emoji: sketch.emoji,
      difficulty: sketch.difficulty,
      tags: sketch.tags,
      imageUrl: `${BASE_GCS_URL}/${sketch.id}.png`,
      thumbnailUrl: `${BASE_GCS_URL}/${sketch.id}.png`,
      assetPath: `sketches/${sketch.id}.png`,
      createdAt: new Date().toISOString()
    });

    console.log(`  ✅ Generated: [${sketch.category}] ${sketch.title} (${sketch.id})`);
  }

  // 4. Save Catalog JSON
  const catalogPath = path.join(__dirname, '../data/catalog.json');
  fs.mkdirSync(path.dirname(catalogPath), { recursive: true });
  fs.writeFileSync(catalogPath, JSON.stringify(catalog, null, 2), 'utf8');

  // Copy catalog to Android Assets as well
  fs.writeFileSync(path.join(ANDROID_ASSETS_DIR, 'catalog.json'), JSON.stringify(catalog, null, 2), 'utf8');

  console.log(`\n🎉 Generated all ${SKETCHES.length} sketches! Saved catalog to ${catalogPath}`);
}

main().catch(console.error);
