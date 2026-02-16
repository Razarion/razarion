import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import { gunzipSync, deflateSync } from "node:zlib";

// --- Configuration ---
const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const ADMIN_EMAIL = process.env.RAZARION_ADMIN_EMAIL || "admin@admin.com";
const ADMIN_PASSWORD = process.env.RAZARION_ADMIN_PASSWORD || "admin";

let jwtToken: string | null = null;

// --- Auth ---
async function authenticate(): Promise<string> {
  if (jwtToken) return jwtToken;

  const credentials = Buffer.from(`${ADMIN_EMAIL}:${ADMIN_PASSWORD}`).toString("base64");
  const response = await fetch(`${BASE_URL}/rest/user/auth`, {
    method: "POST",
    headers: { Authorization: `Basic ${credentials}` },
  });

  if (!response.ok) {
    throw new Error(`Authentication failed: ${response.status} ${response.statusText}`);
  }

  jwtToken = await response.text();
  return jwtToken;
}

async function apiGet(path: string): Promise<unknown> {
  const token = await authenticate();
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      Accept: "application/json",
    },
  });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(`GET ${path} failed: ${response.status} ${body}`);
  }
  return response.json();
}

async function apiPost(path: string, body?: unknown): Promise<unknown> {
  const token = await authenticate();
  const response = await fetch(`${BASE_URL}${path}`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(`POST ${path} failed: ${response.status} ${text}`);
  }
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

async function apiDelete(path: string): Promise<void> {
  const token = await authenticate();
  const response = await fetch(`${BASE_URL}${path}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(`DELETE ${path} failed: ${response.status} ${text}`);
  }
}

function ok(data: unknown) {
  return { content: [{ type: "text" as const, text: JSON.stringify(data, null, 2) }] };
}

function msg(text: string) {
  return { content: [{ type: "text" as const, text }] };
}

// --- MCP Server ---
const server = new McpServer({
  name: "razarion-ai-content",
  version: "1.0.0",
});

// ============================================================
// BASE ITEM TYPES
// ============================================================

server.tool(
  "list_base_item_types",
  "List all base item types (units, buildings) with their IDs and names",
  {},
  async () => ok(await apiGet("/rest/editor/base_item_type/objectNameIds"))
);

server.tool(
  "read_base_item_type",
  "Read a specific base item type by ID. Returns full config including weapon, factory, builder, harvester, etc.",
  { id: z.number().describe("Base item type ID") },
  async ({ id }) => ok(await apiGet(`/rest/editor/base_item_type/read/${id}`))
);

server.tool(
  "create_base_item_type",
  "Create a new base item type (returns empty template with new ID). Use update_base_item_type to set fields.",
  {},
  async () => ok(await apiPost("/rest/editor/base_item_type/create"))
);

server.tool(
  "update_base_item_type",
  "Update an existing base item type. Send the complete BaseItemType object.",
  { baseItemType: z.record(z.unknown()).describe("Complete BaseItemType JSON object with all fields") },
  async ({ baseItemType }) => {
    await apiPost("/rest/editor/base_item_type/update", baseItemType);
    return msg(`Base item type ${baseItemType.id} updated.`);
  }
);

server.tool(
  "delete_base_item_type",
  "Delete a base item type by ID",
  { id: z.number().describe("Base item type ID to delete") },
  async ({ id }) => {
    await apiDelete(`/rest/editor/base_item_type/delete/${id}`);
    return msg(`Base item type ${id} deleted.`);
  }
);

// ============================================================
// RESOURCE ITEM TYPES
// ============================================================

server.tool(
  "list_resource_item_types",
  "List all resource item types with their IDs and names",
  {},
  async () => ok(await apiGet("/rest/editor/resource_item_type/objectNameIds"))
);

server.tool(
  "read_resource_item_type",
  "Read a specific resource item type by ID",
  { id: z.number().describe("Resource item type ID") },
  async ({ id }) => ok(await apiGet(`/rest/editor/resource_item_type/read/${id}`))
);

server.tool(
  "create_resource_item_type",
  "Create a new resource item type (returns empty template). Use update_resource_item_type to set fields.",
  {},
  async () => ok(await apiPost("/rest/editor/resource_item_type/create"))
);

server.tool(
  "update_resource_item_type",
  "Update an existing resource item type",
  { resourceItemType: z.record(z.unknown()).describe("Complete ResourceItemType JSON object") },
  async ({ resourceItemType }) => {
    await apiPost("/rest/editor/resource_item_type/update", resourceItemType);
    return msg(`Resource item type ${resourceItemType.id} updated.`);
  }
);

// ============================================================
// LEVELS
// ============================================================

server.tool(
  "list_levels",
  "List all game levels with their IDs and names",
  {},
  async () => ok(await apiGet("/rest/editor/level/objectNameIds"))
);

server.tool(
  "read_level",
  "Read a specific level by ID. Fields: number, xp2LevelUp, itemTypeLimitation, levelUnlockEntities",
  { id: z.number().describe("Level ID") },
  async ({ id }) => ok(await apiGet(`/rest/editor/level/read/${id}`))
);

server.tool(
  "create_level",
  "Create a new level (returns empty template). Use update_level to set fields.",
  {},
  async () => ok(await apiPost("/rest/editor/level/create"))
);

server.tool(
  "update_level",
  "Update an existing level",
  { level: z.record(z.unknown()).describe("Complete LevelEntity JSON object") },
  async ({ level }) => {
    await apiPost("/rest/editor/level/update", level);
    return msg(`Level ${level.id} updated.`);
  }
);

server.tool(
  "delete_level",
  "Delete a level by ID",
  { id: z.number().describe("Level ID to delete") },
  async ({ id }) => {
    await apiDelete(`/rest/editor/level/delete/${id}`);
    return msg(`Level ${id} deleted.`);
  }
);

// ============================================================
// PLANETS
// ============================================================

server.tool(
  "list_planets",
  "List all planets with their IDs and names",
  {},
  async () => ok(await apiGet("/rest/editor/planet/objectNameIds"))
);

server.tool(
  "read_planet",
  "Read a specific planet config. Fields: size, itemTypeLimitation, houseSpace, startRazarion, startBaseItemTypeId, groundConfigId",
  { id: z.number().describe("Planet config ID") },
  async ({ id }) => ok(await apiGet(`/rest/editor/planet/read/${id}`))
);

server.tool(
  "update_planet",
  "Update an existing planet config",
  { planet: z.record(z.unknown()).describe("Complete PlanetConfig JSON object") },
  async ({ planet }) => {
    await apiPost("/rest/editor/planet/update", planet);
    return msg(`Planet ${planet.id} updated.`);
  }
);

// ============================================================
// SERVER GAME ENGINE (Bots, Quests, Resource Regions, etc.)
// ============================================================

server.tool(
  "read_server_game_engine",
  "Read the full server game engine config. Contains botConfigs, serverLevelQuestConfigs, resourceRegionConfigs, startRegionConfigs, boxRegionConfigs, and planetConfig.",
  { id: z.number().describe("Server game engine config ID") },
  async ({ id }) => ok(await apiGet(`/rest/editor/server-game-engine/read/${id}`))
);

server.tool(
  "update_bot_configs",
  `Update ALL bot configs for a server game engine. Replaces the entire list.
BotConfig fields: id, internalName, auxiliaryId, npc, actionDelay, realm (PlaceConfig), name, autoAttack, minInactiveMs, maxInactiveMs, minActiveMs, maxActiveMs, botEnragementStateConfigs (array of {name, botItems, enrageUpKills}).
BotItemConfig fields: baseItemTypeId, count, createDirectly, noSpawn, place, angle, moveRealmIfIdle, idleTtl, noRebuild, rePopTime.`,
  {
    serverGameEngineConfigId: z.number().describe("Server game engine config ID"),
    botConfigs: z.array(z.record(z.unknown())).describe("Array of BotConfig objects"),
  },
  async ({ serverGameEngineConfigId, botConfigs }) => {
    await apiPost(`/rest/editor/server-game-engine/update/botConfig/${serverGameEngineConfigId}`, botConfigs);
    return msg(`Bot configs updated for engine ${serverGameEngineConfigId}. ${botConfigs.length} bot(s) configured.`);
  }
);

server.tool(
  "update_quest_configs",
  `Update ALL quest configs for a server game engine. Replaces the entire list.
ServerLevelQuestConfig fields: id, internalName, minimalLevelId, questConfigs[].
QuestConfig fields: id, internalName, xp, razarion, crystal, conditionConfig, tipConfig.
ConditionConfig: conditionTrigger (SYNC_ITEM_KILLED|HARVEST|SYNC_ITEM_CREATED|BASE_KILLED|SYNC_ITEM_POSITION|BOX_PICKED|INVENTORY_ITEM_PLACED|UNLOCKED|SELL), comparisonConfig.
ComparisonConfig: count, typeCount, includeExisting, timeSeconds, placeConfig, startRegionId, botIds[].`,
  {
    serverGameEngineConfigId: z.number().describe("Server game engine config ID"),
    questConfigs: z.array(z.record(z.unknown())).describe("Array of ServerLevelQuestConfig objects"),
  },
  async ({ serverGameEngineConfigId, questConfigs }) => {
    await apiPost(`/rest/editor/server-game-engine/update/serverLevelQuestConfig/${serverGameEngineConfigId}`, questConfigs);
    return msg(`Quest configs updated for engine ${serverGameEngineConfigId}. ${questConfigs.length} quest group(s) configured.`);
  }
);

server.tool(
  "update_resource_regions",
  `Update ALL resource region configs for a server game engine. Replaces the entire list.
ResourceRegionConfig fields: id, internalName, count, minDistanceToItems, resourceItemTypeId, region (PlaceConfig).`,
  {
    serverGameEngineConfigId: z.number().describe("Server game engine config ID"),
    resourceRegions: z.array(z.record(z.unknown())).describe("Array of ResourceRegionConfig objects"),
  },
  async ({ serverGameEngineConfigId, resourceRegions }) => {
    await apiPost(`/rest/editor/server-game-engine/update/resourceRegionConfig/${serverGameEngineConfigId}`, resourceRegions);
    return msg(`Resource regions updated for engine ${serverGameEngineConfigId}. ${resourceRegions.length} region(s) configured.`);
  }
);

server.tool(
  "update_start_regions",
  `Update ALL start region configs for a server game engine. Replaces the entire list.
StartRegionConfig fields: id, internalName, minimalLevelId, region (PlaceConfig), noBaseViewPosition, findFreePosition, positionRadius, positionMaxItems.`,
  {
    serverGameEngineConfigId: z.number().describe("Server game engine config ID"),
    startRegions: z.array(z.record(z.unknown())).describe("Array of StartRegionConfig objects"),
  },
  async ({ serverGameEngineConfigId, startRegions }) => {
    await apiPost(`/rest/editor/server-game-engine/update/startRegionConfig/${serverGameEngineConfigId}`, startRegions);
    return msg(`Start regions updated for engine ${serverGameEngineConfigId}. ${startRegions.length} region(s) configured.`);
  }
);

server.tool(
  "update_box_regions",
  `Update ALL box region configs for a server game engine. Replaces the entire list.
BoxRegionConfig fields: id, internalName, boxItemTypeId, minInterval, maxInterval, count, minDistanceToItems, region (PlaceConfig).`,
  {
    serverGameEngineConfigId: z.number().describe("Server game engine config ID"),
    boxRegions: z.array(z.record(z.unknown())).describe("Array of BoxRegionConfig objects"),
  },
  async ({ serverGameEngineConfigId, boxRegions }) => {
    await apiPost(`/rest/editor/server-game-engine/update/boxRegionConfig/${serverGameEngineConfigId}`, boxRegions);
    return msg(`Box regions updated for engine ${serverGameEngineConfigId}. ${boxRegions.length} region(s) configured.`);
  }
);

// ============================================================
// BOX ITEM TYPES
// ============================================================

server.tool(
  "list_box_item_types",
  "List all box item types with their IDs and names",
  {},
  async () => ok(await apiGet("/rest/editor/box_item_type/objectNameIds"))
);

server.tool(
  "read_box_item_type",
  "Read a specific box item type by ID",
  { id: z.number().describe("Box item type ID") },
  async ({ id }) => ok(await apiGet(`/rest/editor/box_item_type/read/${id}`))
);

// ============================================================
// GROUND CONFIGS
// ============================================================

server.tool(
  "list_ground_configs",
  "List all ground/terrain configs with their IDs and names",
  {},
  async () => ok(await apiGet("/rest/editor/ground/objectNameIds"))
);

server.tool(
  "read_ground_config",
  "Read a specific ground config by ID",
  { id: z.number().describe("Ground config ID") },
  async ({ id }) => ok(await apiGet(`/rest/editor/ground/read/${id}`))
);

// ============================================================
// TERRAIN OBJECTS
// ============================================================

server.tool(
  "list_terrain_objects",
  "List all terrain object configs with their IDs and names",
  {},
  async () => ok(await apiGet("/rest/editor/terrain-object/objectNameIds"))
);

server.tool(
  "read_terrain_object",
  "Read a specific terrain object config by ID",
  { id: z.number().describe("Terrain object config ID") },
  async ({ id }) => ok(await apiGet(`/rest/editor/terrain-object/read/${id}`))
);

// ============================================================
// PLANET MANAGEMENT (restart bots, reload, etc.)
// ============================================================

server.tool(
  "restart_bots",
  "Restart all bots on the running planet. Use after updating bot configs to apply changes.",
  {},
  async () => {
    await apiPost("/rest/planet-mgmt-controller/restartBots");
    return msg("Bots restarted.");
  }
);

server.tool(
  "reload_static",
  "Reload static configuration on the running server. Use after updating item types, levels, etc.",
  {},
  async () => {
    await apiPost("/rest/planet-mgmt-controller/reloadStatic");
    return msg("Static configuration reloaded.");
  }
);

server.tool(
  "restart_planet_warm",
  "Warm restart of the planet. Reloads config without dropping player bases.",
  {},
  async () => {
    await apiPost("/rest/planet-mgmt-controller/restartPlanetWarm");
    return msg("Planet warm restart initiated.");
  }
);

// ============================================================
// TERRAIN MINIMAP
// ============================================================

// Terrain constants (from TerrainUtil.java / terrain-system.md)
const NODE_X_COUNT = 160;
const NODE_Y_COUNT = 160;
const TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT;
const HEIGHT_PRECISION = 0.1;
const HEIGHT_MIN = -200;
const WATER_LEVEL = 0;

function uint16ToHeight(uint16: number): number {
  return uint16 * HEIGHT_PRECISION + HEIGHT_MIN;
}

async function publicFetchBinary(path: string): Promise<Buffer> {
  const response = await fetch(`${BASE_URL}${path}`);
  if (!response.ok) {
    throw new Error(`GET ${path} failed: ${response.status}`);
  }
  const arrayBuffer = await response.arrayBuffer();
  let buffer = Buffer.from(arrayBuffer);
  // If fetch did not auto-decompress gzip, do it manually
  if (buffer.length >= 2 && buffer[0] === 0x1f && buffer[1] === 0x8b) {
    buffer = gunzipSync(buffer);
  }
  return buffer;
}

async function publicFetchJson(path: string): Promise<unknown> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { Accept: "application/json" },
  });
  if (!response.ok) {
    throw new Error(`GET ${path} failed: ${response.status}`);
  }
  return response.json();
}

server.tool(
  "terrain_minimap",
  `Generate an ASCII minimap of the terrain heightmap for a planet.
Fetches the GZIP-compressed heightmap, decodes uint16 values to heights, and renders a downsampled ASCII visualization.
Characters: ~ water (<0m), . low land (0-3m), : medium (3-10m), + high (10-30m), # mountain (30-80m), ^ peak (>80m).
Also returns height statistics.`,
  {
    planetId: z.number().describe("Planet config ID"),
    width: z.number().optional().describe("Output width in characters (default: 100, max: 200)"),
  },
  async ({ planetId, width }) => {
    const outWidth = Math.min(width ?? 100, 200);

    // 1. Get terrain shape to determine tile dimensions
    const terrainShape = (await publicFetchJson(`/rest/terrainshape/${planetId}`)) as {
      nativeTerrainShapeTiles?: unknown[][];
    };
    if (!terrainShape.nativeTerrainShapeTiles || terrainShape.nativeTerrainShapeTiles.length === 0) {
      throw new Error(`Planet ${planetId} has no terrain shape data.`);
    }
    const tileXCount = terrainShape.nativeTerrainShapeTiles.length;
    const tileYCount = terrainShape.nativeTerrainShapeTiles[0].length;
    const totalXNodes = tileXCount * NODE_X_COUNT;
    const totalYNodes = tileYCount * NODE_Y_COUNT;

    // 2. Fetch heightmap binary (public endpoint, GZIP-compressed)
    const buffer = await publicFetchBinary(`/rest/terrainHeightMap/${planetId}`);
    const bytes = new Uint8Array(buffer);

    // 3. Convert byte pairs to uint16 (Little-Endian)
    const heightMapLength = Math.floor(bytes.length / 2);
    const heightMap = new Uint16Array(heightMapLength);
    for (let i = 0; i < heightMapLength; i++) {
      heightMap[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);
    }

    // 4. Determine sampling
    const step = Math.max(1, Math.ceil(totalXNodes / outWidth));
    const cols = Math.ceil(totalXNodes / step);
    const rows = Math.ceil(totalYNodes / step);

    // 5. Render minimap
    let minH = Infinity, maxH = -Infinity;
    let waterCount = 0, landCount = 0, totalSampled = 0;
    const lines: string[] = [];

    for (let oy = rows - 1; oy >= 0; oy--) {
      let line = "";
      for (let ox = 0; ox < cols; ox++) {
        const nodeX = ox * step;
        const nodeY = oy * step;
        const tileX = Math.floor(nodeX / NODE_X_COUNT);
        const tileY = Math.floor(nodeY / NODE_Y_COUNT);
        const localX = nodeX % NODE_X_COUNT;
        const localY = nodeY % NODE_Y_COUNT;
        const idx = TILE_NODE_SIZE * (tileY * tileXCount + tileX) + localY * NODE_X_COUNT + localX;

        if (idx >= heightMapLength) {
          line += " ";
          continue;
        }

        const h = uint16ToHeight(heightMap[idx]);
        totalSampled++;
        if (h < minH) minH = h;
        if (h > maxH) maxH = h;

        if (h < WATER_LEVEL) {
          line += "~";
          waterCount++;
        } else if (h < 3) {
          line += ".";
          landCount++;
        } else if (h < 10) {
          line += ":";
          landCount++;
        } else if (h < 30) {
          line += "+";
          landCount++;
        } else if (h < 80) {
          line += "#";
          landCount++;
        } else {
          line += "^";
          landCount++;
        }
      }
      lines.push(line);
    }

    // 6. Build output
    let out = `Terrain Minimap - Planet ${planetId}\n`;
    out += `Size: ${totalXNodes} x ${totalYNodes} m  (${tileXCount} x ${tileYCount} tiles)\n`;
    out += `Heightmap: ${heightMapLength} values  |  Sample: 1 char = ${step} m  |  ${cols} x ${rows} chars\n\n`;
    out += lines.join("\n");
    out += "\n\n";
    out += "Legend:  ~ water(<0m)  . low(0-3m)  : med(3-10m)  + high(10-30m)  # mountain(30-80m)  ^ peak(>80m)\n";
    out += `Height range: ${minH.toFixed(1)} m .. ${maxH.toFixed(1)} m\n`;
    const waterPct = totalSampled > 0 ? ((waterCount / totalSampled) * 100).toFixed(1) : "0";
    const landPct = totalSampled > 0 ? ((landCount / totalSampled) * 100).toFixed(1) : "0";
    out += `Coverage: water ${waterPct}%  land ${landPct}%  (${totalSampled} samples)`;

    return msg(out);
  }
);

// ============================================================
// TERRAIN MINIMAP IMAGE (PNG)
// ============================================================

// CRC32 lookup table for PNG chunk checksums
const crcTable: number[] = [];
for (let n = 0; n < 256; n++) {
  let c = n;
  for (let k = 0; k < 8; k++) {
    c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  }
  crcTable[n] = c;
}

function crc32(buf: Uint8Array): number {
  let crc = 0xffffffff;
  for (let i = 0; i < buf.length; i++) {
    crc = crcTable[(crc ^ buf[i]) & 0xff] ^ (crc >>> 8);
  }
  return (crc ^ 0xffffffff) >>> 0;
}

function pngChunk(type: string, data: Uint8Array): Uint8Array {
  const typeBytes = new Uint8Array([
    type.charCodeAt(0), type.charCodeAt(1), type.charCodeAt(2), type.charCodeAt(3),
  ]);
  const lenBuf = new Uint8Array(4);
  new DataView(lenBuf.buffer).setUint32(0, data.length, false);
  const crcInput = new Uint8Array(4 + data.length);
  crcInput.set(typeBytes, 0);
  crcInput.set(data, 4);
  const crcVal = crc32(crcInput);
  const crcBuf = new Uint8Array(4);
  new DataView(crcBuf.buffer).setUint32(0, crcVal, false);
  const chunk = new Uint8Array(4 + 4 + data.length + 4);
  chunk.set(lenBuf, 0);
  chunk.set(typeBytes, 4);
  chunk.set(data, 8);
  chunk.set(crcBuf, 8 + data.length);
  return chunk;
}

function encodePNG(width: number, height: number, rgb: Uint8Array): Uint8Array {
  // IHDR
  const ihdr = new Uint8Array(13);
  const ihdrView = new DataView(ihdr.buffer);
  ihdrView.setUint32(0, width, false);
  ihdrView.setUint32(4, height, false);
  ihdr[8] = 8;  // bit depth
  ihdr[9] = 2;  // color type: RGB
  ihdr[10] = 0; // compression
  ihdr[11] = 0; // filter
  ihdr[12] = 0; // interlace

  // Raw scanlines: filter byte (0) + RGB per row
  const rowSize = 1 + width * 3;
  const raw = new Uint8Array(height * rowSize);
  for (let y = 0; y < height; y++) {
    raw[y * rowSize] = 0; // no filter
    raw.set(rgb.subarray(y * width * 3, (y + 1) * width * 3), y * rowSize + 1);
  }

  const compressed = deflateSync(raw, { level: 6 });

  // Assemble PNG
  const signature = new Uint8Array([137, 80, 78, 71, 13, 10, 26, 10]);
  const ihdrChunk = pngChunk("IHDR", ihdr);
  const idatChunk = pngChunk("IDAT", compressed);
  const iendChunk = pngChunk("IEND", new Uint8Array(0));

  const png = new Uint8Array(
    signature.length + ihdrChunk.length + idatChunk.length + iendChunk.length
  );
  let offset = 0;
  png.set(signature, offset); offset += signature.length;
  png.set(ihdrChunk, offset); offset += ihdrChunk.length;
  png.set(idatChunk, offset); offset += idatChunk.length;
  png.set(iendChunk, offset);
  return png;
}

function heightToColor(h: number): [number, number, number] {
  if (h < -5)  return [10, 30, 100];    // deep water
  if (h < 0)   return [30, 80, 170];    // shallow water
  if (h < 1)   return [160, 200, 100];  // beach/shore
  if (h < 3)   return [80, 170, 50];    // low land
  if (h < 10)  return [100, 150, 40];   // medium land
  if (h < 20)  return [170, 150, 60];   // high land
  if (h < 30)  return [150, 120, 60];   // hills
  if (h < 50)  return [130, 110, 90];   // mountain
  if (h < 80)  return [160, 155, 150];  // high mountain
  return [230, 230, 235];               // peak/snow
}

server.tool(
  "terrain_minimap_image",
  `Generate a PNG minimap image of the terrain heightmap for a planet.
Fetches the GZIP-compressed heightmap, decodes uint16 values to heights, and renders a color-mapped PNG image.
Colors: dark blue (deep water), blue (water), green (land), yellow-brown (hills), gray (mountains), white (peaks).
Returns the image as base64-encoded PNG.`,
  {
    planetId: z.number().describe("Planet config ID"),
    width: z.number().optional().describe("Output image width in pixels (default: 400, max: 1024)"),
  },
  async ({ planetId, width }) => {
    const outWidth = Math.min(Math.max(width ?? 400, 32), 1024);

    // 1. Get terrain shape to determine tile dimensions
    const terrainShape = (await publicFetchJson(`/rest/terrainshape/${planetId}`)) as {
      nativeTerrainShapeTiles?: unknown[][];
    };
    if (!terrainShape.nativeTerrainShapeTiles || terrainShape.nativeTerrainShapeTiles.length === 0) {
      throw new Error(`Planet ${planetId} has no terrain shape data.`);
    }
    const tileXCount = terrainShape.nativeTerrainShapeTiles.length;
    const tileYCount = terrainShape.nativeTerrainShapeTiles[0].length;
    const totalXNodes = tileXCount * NODE_X_COUNT;
    const totalYNodes = tileYCount * NODE_Y_COUNT;

    // 2. Fetch heightmap binary
    const buffer = await publicFetchBinary(`/rest/terrainHeightMap/${planetId}`);
    const bytes = new Uint8Array(buffer);

    // 3. Convert byte pairs to uint16 (Little-Endian)
    const heightMapLength = Math.floor(bytes.length / 2);
    const heightMap = new Uint16Array(heightMapLength);
    for (let i = 0; i < heightMapLength; i++) {
      heightMap[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);
    }

    // 4. Determine sampling
    const step = Math.max(1, Math.ceil(totalXNodes / outWidth));
    const cols = Math.ceil(totalXNodes / step);
    const rows = Math.ceil(totalYNodes / step);

    // 5. Render image (RGB buffer)
    const rgb = new Uint8Array(cols * rows * 3);
    for (let oy = 0; oy < rows; oy++) {
      const srcY = (rows - 1 - oy) * step; // flip Y (top = north)
      for (let ox = 0; ox < cols; ox++) {
        const srcX = ox * step;
        const tileX = Math.floor(srcX / NODE_X_COUNT);
        const tileY = Math.floor(srcY / NODE_Y_COUNT);
        const localX = srcX % NODE_X_COUNT;
        const localY = srcY % NODE_Y_COUNT;
        const idx = TILE_NODE_SIZE * (tileY * tileXCount + tileX) + localY * NODE_X_COUNT + localX;

        let r = 0, g = 0, b = 0;
        if (idx < heightMapLength) {
          const h = uint16ToHeight(heightMap[idx]);
          [r, g, b] = heightToColor(h);
        }
        const px = (oy * cols + ox) * 3;
        rgb[px] = r;
        rgb[px + 1] = g;
        rgb[px + 2] = b;
      }
    }

    // 6. Encode PNG and return as base64 image
    const png = encodePNG(cols, rows, rgb);
    const base64 = Buffer.from(png).toString("base64");

    return {
      content: [
        {
          type: "image" as const,
          data: base64,
          mimeType: "image/png",
        },
        {
          type: "text" as const,
          text: `Terrain Minimap Image - Planet ${planetId}\nSize: ${totalXNodes} x ${totalYNodes} m (${tileXCount} x ${tileYCount} tiles)\nImage: ${cols} x ${rows} px (1 px = ${step} m)`,
        },
      ],
    };
  }
);

// --- Start ---
const transport = new StdioServerTransport();
await server.connect(transport);
console.error("Razarion AI Content MCP server running on stdio");
