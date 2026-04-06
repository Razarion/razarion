import { writeFileSync } from "node:fs";

const BASE_URL = "http://localhost:8080";

const PLANT_MODELS = [
  { name: "Palm bush", weight: 3 },
  { name: "Tropical Plant", weight: 3 },
  { name: "Fern", weight: 3 },
  { name: "Banana plant", weight: 2 },
  { name: "Fern small", weight: 2 },
  { name: "Palm plant", weight: 2 },
  { name: "Palm tree1", weight: 1 },
  { name: "Palm tree", weight: 1 },
];

// Build weighted array for random selection (more small plants, fewer palms)
const WEIGHTED = [];
for (const m of PLANT_MODELS) for (let i = 0; i < m.weight; i++) WEIGHTED.push(m.name);

function seededRandom(seed) { let s = seed; return () => { s = (s * 16807) % 2147483647; return (s - 1) / 2147483646; }; }

// Asphalt decals from ServerTerrainShapeService.generateDecals()
// Horizontal: 6 decals at y=2, x=25+i*35, size 25x25
// Vertical: 6 decals at x=7, y=35+i*35, size 25x25
const ASPHALT_AREAS = [];
for (let i = 0; i < 6; i++) {
  ASPHALT_AREAS.push({ id: `h${i}`, minX: 25 + i * 35, maxX: 25 + i * 35 + 25, minY: 2, maxY: 2 + 25 });
}
for (let i = 0; i < 6; i++) {
  ASPHALT_AREAS.push({ id: `v${i}`, minX: 7, maxX: 7 + 25, minY: 35 + i * 35, maxY: 35 + i * 35 + 25 });
}

const EDGE_OFFSET = 6;   // distance from edge
const SPACING = 8;       // along-edge spacing
const rand = seededRandom(555);

const positions = [];

for (const area of ASPHALT_AREAS) {
  const { minX, maxX, minY, maxY } = area;
  const edgePoints = [];

  // Top edge (y = minY - offset)
  for (let x = minX; x <= maxX; x += SPACING) {
    edgePoints.push({ x: x + rand() * 4 - 2, y: minY - EDGE_OFFSET + rand() * 3 - 1 });
  }
  // Bottom edge (y = maxY + offset)
  for (let x = minX; x <= maxX; x += SPACING) {
    edgePoints.push({ x: x + rand() * 4 - 2, y: maxY + EDGE_OFFSET + rand() * 3 - 1 });
  }
  // Left edge (x = minX - offset)
  for (let y = minY; y <= maxY; y += SPACING) {
    edgePoints.push({ x: minX - EDGE_OFFSET + rand() * 3 - 1, y: y + rand() * 4 - 2 });
  }
  // Right edge (x = maxX + offset)
  for (let y = minY; y <= maxY; y += SPACING) {
    edgePoints.push({ x: maxX + EDGE_OFFSET + rand() * 3 - 1, y: y + rand() * 4 - 2 });
  }

  // Corners get an extra plant or two
  const corners = [
    { x: minX - 4, y: minY - 4 },
    { x: maxX + 4, y: minY - 4 },
    { x: minX - 4, y: maxY + 4 },
    { x: maxX + 4, y: maxY + 4 },
  ];
  for (const c of corners) {
    edgePoints.push({ x: c.x + rand() * 2, y: c.y + rand() * 2 });
  }

  for (const pt of edgePoints) {
    const modelName = WEIGHTED[Math.floor(rand() * WEIGHTED.length)];
    const rotZ = rand() * Math.PI * 2;
    const scale = 0.7 + rand() * 0.6;
    positions.push({ x: Math.round(pt.x), y: Math.round(pt.y), modelName, rotZ, scale });
  }

  console.log(`Area ${area.id}: ${edgePoints.length} plants around edges`);
}

console.log(`Total: ${positions.length} plants`);

// Use exact config IDs from DB
const CONFIG_IDS = {
  "Palm bush": 316, "Tropical Plant": 315, "Fern": 317,
  "Banana plant": 314, "Fern small": 323, "Palm plant": 324,
  "Palm tree1": 310, "Palm tree": 311,
};

const configSql = [];

const posSql = positions.map(({ x, y, modelName, rotZ, scale }) => {
  const configId = CONFIG_IDS[modelName];
  return `INSERT INTO TERRAIN_OBJECT_POSITION (internalName, x, y, terrainObjectEntity_id, planet, rotationX, rotationY, rotationZ, scaleX, scaleY, scaleZ) VALUES ('asphalt-edge@${x},${y}', ${x}, ${y}, ${configId}, 117, 0, 0, ${rotZ.toFixed(3)}, ${scale.toFixed(2)}, ${scale.toFixed(2)}, ${scale.toFixed(2)});`;
});

writeFileSync("place-asphalt-plants.sql", [...configSql, "", ...posSql].join("\n"));
console.log("SQL written to place-asphalt-plants.sql");
