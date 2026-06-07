import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import { gunzipSync, deflateSync } from "node:zlib";
import { writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";

// --- Configuration ---
const BASE_URL = process.env.RAZARION_BASE_URL || "http://localhost:8080";
const ADMIN_EMAIL = process.env.RAZARION_ADMIN_EMAIL || "admin@admin.com";
const ADMIN_PASSWORD = process.env.RAZARION_ADMIN_PASSWORD || "1234";

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
BotItemConfig fields: baseItemTypeId, count, createDirectly, noSpawn, place, spreadPlace (optional PlaceConfig; vehicles only - movable units get a one-time move to a random position within this region right after spawn/factory build; null for buildings or no spread), angle, moveRealmIfIdle, idleTtl, noRebuild, rePopTime.`,
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
ResourceRegionConfig fields: id, internalName, count, minDistanceToItems, resourceItemTypeId, region (PlaceConfig), evenlyDistributed (boolean; when true the spots are placed on a hexagonal grid over the region's free land and respawn at the same slot, instead of randomly).`,
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
const HEIGHT_PRECISION = 0.01;
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
  if (h < 20)  return [120, 115, 70];   // hills - earthy olive
  if (h < 30)  return [115, 110, 85];   // mountain - gray-brown
  if (h < 50)  return [125, 120, 105];  // high mountain - rocky gray
  if (h < 80)  return [150, 148, 142];  // very high mountain - light gray
  return [210, 210, 215];               // peak/snow
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

// ============================================================
// REGION MAP (config overlay: resource/start/box/bot/quest regions)
// ============================================================

type RegionFeature = {
  kind: string;
  id: number | string;
  name: string;
  char: string;
  color: [number, number, number];
  stats: string;
  polygons: Array<Array<[number, number]>>;
  points: Array<[number, number]>;
  area: number; // total polygon area in game-units²; 0 for point-only places. Smaller area = higher draw priority (drawn on top).
};

function polygonArea(poly: Array<[number, number]>): number {
  let a = 0;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    a += (poly[j][0] + poly[i][0]) * (poly[j][1] - poly[i][1]);
  }
  return Math.abs(a / 2);
}

// For a point in game space, return the highest-priority (smallest-area) feature containing it, or null.
function topFeatureAt(feats: RegionFeature[], gx: number, gy: number): RegionFeature | null {
  let best: RegionFeature | null = null;
  for (const f of feats) {
    if (best && f.area >= best.area) continue;
    for (const poly of f.polygons) {
      if (pointInPolygon(poly, gx, gy)) { best = f; break; }
    }
  }
  return best;
}

// Point-in-polygon raycast — identical to Polygon2D.isInside (corners only).
function pointInPolygon(poly: Array<[number, number]>, px: number, py: number): boolean {
  let c = false;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const sx = poly[i][0], sy = poly[i][1];
    const ex = poly[j][0], ey = poly[j][1];
    if (((sy > py) !== (ey > py)) && (px < ((ex - sx) * (py - sy)) / (ey - sy) + sx)) c = !c;
  }
  return c;
}

function hslToRgb(h: number, s: number, l: number): [number, number, number] {
  const c = (1 - Math.abs(2 * l - 1)) * s;
  const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
  const m = l - c / 2;
  let r = 0, g = 0, b = 0;
  if (h < 60) { r = c; g = x; }
  else if (h < 120) { r = x; g = c; }
  else if (h < 180) { g = c; b = x; }
  else if (h < 240) { g = x; b = c; }
  else if (h < 300) { r = x; b = c; }
  else { r = c; b = x; }
  return [Math.round((r + m) * 255), Math.round((g + m) * 255), Math.round((b + m) * 255)];
}

function cornersToPoly(corners: any): Array<[number, number]> | null {
  if (!Array.isArray(corners) || corners.length < 3) return null;
  return corners.map((c: any) => [c.x, c.y] as [number, number]);
}

const REGION_CHAR_POOL = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

function collectRegionFeatures(cfg: any, layers: Set<string>): RegionFeature[] {
  const feats: RegionFeature[] = [];
  let ci = 0;
  const nextChar = () => REGION_CHAR_POOL[Math.min(ci++, REGION_CHAR_POOL.length - 1)];

  const addPlace = (kind: string, id: any, name: string, stats: string, place: any) => {
    if (!place) return;
    const polygons: Array<Array<[number, number]>> = [];
    const points: Array<[number, number]> = [];
    const poly = cornersToPoly(place?.polygon2D?.corners);
    if (poly) polygons.push(poly);
    else if (place?.position && typeof place.position.x === "number") points.push([place.position.x, place.position.y]);
    if (!polygons.length && !points.length) return;
    const area = polygons.reduce((sum, p) => sum + polygonArea(p), 0);
    feats.push({ kind, id, name: name || "", char: nextChar(), color: [0, 0, 0], stats, polygons, points, area });
  };

  if (layers.has("resource"))
    for (const r of cfg.resourceRegionConfigs ?? [])
      addPlace("resource", r.id, r.internalName, `count=${r.count} resType=${r.resourceItemTypeId} minDist=${r.minDistanceToItems}`, r.region);
  if (layers.has("start"))
    for (const s of cfg.startRegionConfigs ?? [])
      addPlace("start", s.id, s.internalName, `minLevelId=${s.minimalLevelId}`, s.region);
  if (layers.has("box"))
    for (const b of cfg.boxRegionConfigs ?? [])
      addPlace("box", b.id, b.internalName, `boxItemTypeId=${b.boxItemTypeId} count=${b.count}`, b.region);
  if (layers.has("bot"))
    for (const bot of cfg.botConfigs ?? [])
      addPlace("bot", bot.id, bot.internalName, `npc=${bot.npc}`, bot.realm);
  if (layers.has("quest"))
    for (const grp of cfg.serverLevelQuestConfigs ?? [])
      for (const q of grp.questConfigs ?? []) {
        const pc = q?.conditionConfig?.comparisonConfig?.placeConfig;
        if (pc) addPlace("quest", q.id, q.internalName || `quest ${q.id}`, `trigger=${q?.conditionConfig?.conditionTrigger}`, pc);
      }

  feats.forEach((f, i) => { f.color = hslToRgb((i * 360) / Math.max(1, feats.length), 0.65, 0.55); });
  return feats;
}

function computeRegionBounds(feats: RegionFeature[]): { x1: number; y1: number; x2: number; y2: number } {
  let x1 = Infinity, y1 = Infinity, x2 = -Infinity, y2 = -Infinity;
  const acc = (x: number, y: number) => { x1 = Math.min(x1, x); y1 = Math.min(y1, y); x2 = Math.max(x2, x); y2 = Math.max(y2, y); };
  for (const f of feats) {
    for (const poly of f.polygons) for (const [x, y] of poly) acc(x, y);
    for (const [x, y] of f.points) acc(x, y);
  }
  if (!isFinite(x1)) return { x1: 0, y1: 0, x2: 1, y2: 1 };
  const padX = (x2 - x1) * 0.05 + 1, padY = (y2 - y1) * 0.05 + 1;
  return { x1: x1 - padX, y1: y1 - padY, x2: x2 + padX, y2: y2 + padY };
}

function regionLegend(feats: RegionFeature[], withChar: boolean): string {
  const byKind: Record<string, RegionFeature[]> = {};
  for (const f of feats) (byKind[f.kind] ??= []).push(f);
  let out = "";
  for (const kind of Object.keys(byKind)) {
    out += `  [${kind}]\n`;
    for (const f of byKind[kind]) {
      const tag = withChar ? `${f.char} = ` : `rgb(${f.color.join(",")}) `;
      out += `    ${tag}id=${f.id} '${f.name}' ${f.stats}\n`;
    }
  }
  return out;
}

const REGION_LAYER_ENUM = z.enum(["resource", "start", "box", "bot", "quest"]);
const DEFAULT_REGION_LAYERS = ["resource", "start", "box", "bot"];

// Static phase boundaries for planet 117 in game coords (origin bottom-left, Y up), from docs/game-design/progression.md §1.
// Phase logic: inside P1 polygon -> P1; else X<2000 & Y<2000 -> P2; else X>=2000 & Y<2500 -> P3; else P4.
const PHASE_BOUNDARIES: Array<{ name: string; color: [number, number, number]; poly: Array<[number, number]> }> = [
  { name: "Phase 1 (Noob Island)", color: [255, 70, 70], poly: [[0, 0], [810, 0], [804, 162], [630, 350], [402, 589], [117, 740], [0, 756]] },
  { name: "Phase 2 (Frontier)", color: [255, 235, 59], poly: [[0, 0], [2000, 0], [2000, 2000], [0, 2000]] },
  { name: "Phase 3 (Siege)", color: [80, 220, 255], poly: [[2000, 0], [5120, 0], [5120, 2500], [2000, 2500]] },
  { name: "Phase 4 (Warzone)", color: [255, 120, 255], poly: [[0, 2000], [2000, 2000], [2000, 2500], [5120, 2500], [5120, 5120], [0, 5120]] },
];

// Bresenham line into an RGB buffer, with square thickness.
function drawLinePx(rgb: Uint8Array, w: number, h: number, x0: number, y0: number, x1: number, y1: number, color: [number, number, number], thick: number): void {
  const dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
  const sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
  const half = Math.floor(thick / 2);
  let err = dx - dy, x = x0, y = y0;
  for (;;) {
    for (let oy = -half; oy <= half; oy++) for (let ox = -half; ox <= half; ox++) {
      const px = x + ox, py = y + oy;
      if (px >= 0 && px < w && py >= 0 && py < h) { const i = (py * w + px) * 3; rgb[i] = color[0]; rgb[i + 1] = color[1]; rgb[i + 2] = color[2]; }
    }
    if (x === x1 && y === y1) break;
    const e2 = 2 * err;
    if (e2 > -dy) { err -= dy; x += sx; }
    if (e2 < dx) { err += dx; y += sy; }
  }
}

server.tool(
  "region_map",
  `ASCII overview map of all config regions for a server game engine (resource/start/box/bot-realm/quest places) over a game-coordinate grid.
Each region gets a unique character; the legend maps characters to id/name/stats. '*' marks cells covered by 2+ regions, '.' is empty.
Useful to inspect resource-region placement, gaps and overlaps. Terrain is NOT shown (heightmap decode pending). Y axis points north (up).`,
  {
    serverGameEngineConfigId: z.number().describe("Server game engine config ID (e.g. 3)"),
    width: z.number().optional().describe("Output width in characters (default 100, max 200)"),
    layers: z.array(REGION_LAYER_ENUM).optional().describe("Layers to include (default: resource, start, box, bot)"),
    bounds: z.object({ x1: z.number(), y1: z.number(), x2: z.number(), y2: z.number() }).optional().describe("Game-coordinate bounds; default auto-fit to all features"),
  },
  async ({ serverGameEngineConfigId, width, layers, bounds }) => {
    const cfg = (await apiGet(`/rest/editor/server-game-engine/read/${serverGameEngineConfigId}`)) as any;
    const feats = collectRegionFeatures(cfg, new Set(layers ?? DEFAULT_REGION_LAYERS));
    if (!feats.length) return msg("No region features found for the selected layers.");
    const b = bounds ?? computeRegionBounds(feats);
    const cols = Math.min(Math.max(width ?? 100, 20), 200);
    const spanX = b.x2 - b.x1, spanY = b.y2 - b.y1;
    const cellW = spanX / cols;
    const rows = Math.max(1, Math.round(spanY / cellW / 2)); // terminal chars ~2x tall
    const cellH = spanY / rows;

    const grid: string[][] = [];
    for (let ry = 0; ry < rows; ry++) {
      const gy = b.y2 - (ry + 0.5) * cellH; // top row = high Y (north up)
      const row: string[] = [];
      for (let rx = 0; rx < cols; rx++) {
        const gx = b.x1 + (rx + 0.5) * cellW;
        const top = topFeatureAt(feats, gx, gy); // smallest-area region wins → big background layers don't mask small ones
        row.push(top ? top.char : ".");
      }
      grid.push(row);
    }
    for (const f of feats) for (const [x, y] of f.points) {
      const rx = Math.floor((x - b.x1) / cellW), ry = Math.floor((b.y2 - y) / cellH);
      if (rx >= 0 && rx < cols && ry >= 0 && ry < rows) grid[ry][rx] = f.char;
    }

    let out = `Region Map - server-game-engine ${serverGameEngineConfigId}\n`;
    out += `Bounds: X ${b.x1.toFixed(0)}..${b.x2.toFixed(0)}  Y ${b.y1.toFixed(0)}..${b.y2.toFixed(0)}  |  grid ${cols}x${rows}  |  cell ${cellW.toFixed(0)}x${cellH.toFixed(0)} game-units  |  Y up = north\n`;
    out += `Overlapping regions: the smaller-area region is shown on top (e.g. resource/bot over a map-wide box field).\n\n`;
    out += grid.map((r) => r.join("")).join("\n");
    out += "\n\nLegend (char = region, sorted): \n";
    out += regionLegend(feats, true);
    out += `  . = empty   |  ${feats.length} region(s)\n`;
    return msg(out);
  }
);

server.tool(
  "region_map_image",
  `PNG overview map of all config regions for a server game engine (resource/start/box/bot-realm/quest places): colored filled polygons over a neutral background, Y up = north. The colour->region legend is returned as text. Terrain is NOT shown (heightmap decode pending).`,
  {
    serverGameEngineConfigId: z.number().describe("Server game engine config ID (e.g. 3)"),
    width: z.number().optional().describe("Output image width in pixels (default 700, max 1200)"),
    layers: z.array(REGION_LAYER_ENUM).optional().describe("Layers to include (default: resource, start, box, bot)"),
    bounds: z.object({ x1: z.number(), y1: z.number(), x2: z.number(), y2: z.number() }).optional().describe("Game-coordinate bounds; default auto-fit to all features"),
    phases: z.boolean().optional().describe("Overlay the four phase boundaries (P1 polygon, P2/P3/P4 zones) as coloured outlines. Default false."),
    terrain: z.boolean().optional().describe("Render the terrain (water/land/mountain colours) as the background, with region fills blended on top. Lets you see where land actually is. Default false (neutral dark background). planetId 117 is assumed (the only planet)."),
    outputPath: z.string().optional().describe("Absolute file path to write the PNG to. Defaults to <os-temp>/razarion-region-map-<id>.png. The MCP server runs locally, so this lands on your machine — open it with Invoke-Item."),
  },
  async ({ serverGameEngineConfigId, width, layers, bounds, phases, terrain, outputPath }) => {
    const cfg = (await apiGet(`/rest/editor/server-game-engine/read/${serverGameEngineConfigId}`)) as any;
    const feats = collectRegionFeatures(cfg, new Set(layers ?? DEFAULT_REGION_LAYERS));
    if (!feats.length) return msg("No region features found for the selected layers.");
    const b = bounds ?? computeRegionBounds(feats);
    const spanX = b.x2 - b.x1, spanY = b.y2 - b.y1;
    const outW = Math.min(Math.max(width ?? 700, 128), 1200);
    const outH = Math.max(1, Math.round((outW * spanY) / spanX));

    // Optional terrain background. Game coords == metres == heightmap node index (1 unit = 1 node).
    let heightMap: Uint16Array | null = null;
    let tileXCount = 0, totalXNodes = 0, totalYNodes = 0;
    if (terrain) {
      const planetId = cfg.planetConfigId ?? 117;
      const shape = (await publicFetchJson(`/rest/terrainshape/${planetId}`)) as { nativeTerrainShapeTiles?: unknown[][] };
      if (shape.nativeTerrainShapeTiles?.length) {
        tileXCount = shape.nativeTerrainShapeTiles.length;
        const tileYCount = shape.nativeTerrainShapeTiles[0].length;
        totalXNodes = tileXCount * NODE_X_COUNT;
        totalYNodes = tileYCount * NODE_Y_COUNT;
        const bytes = new Uint8Array(await publicFetchBinary(`/rest/terrainHeightMap/${planetId}`));
        const len = Math.floor(bytes.length / 2);
        heightMap = new Uint16Array(len);
        for (let i = 0; i < len; i++) heightMap[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);
      }
    }
    const baseColorAt = (gx: number, gy: number): [number, number, number] => {
      if (!heightMap) return [22, 24, 28];
      const nodeX = Math.round(gx), nodeY = Math.round(gy);
      if (nodeX < 0 || nodeY < 0 || nodeX >= totalXNodes || nodeY >= totalYNodes) return [12, 14, 18];
      const idx = TILE_NODE_SIZE * (Math.floor(nodeY / NODE_Y_COUNT) * tileXCount + Math.floor(nodeX / NODE_X_COUNT)) + (nodeY % NODE_Y_COUNT) * NODE_X_COUNT + (nodeX % NODE_X_COUNT);
      return idx < heightMap.length ? heightToColor(uint16ToHeight(heightMap[idx])) : [12, 14, 18];
    };

    const rgb = new Uint8Array(outW * outH * 3);
    for (let py = 0; py < outH; py++) {
      const gy = b.y2 - ((py + 0.5) / outH) * spanY; // top = north
      for (let px = 0; px < outW; px++) {
        const gx = b.x1 + ((px + 0.5) / outW) * spanX;
        let [r, g, bl] = baseColorAt(gx, gy);
        const top = topFeatureAt(feats, gx, gy); // smallest-area region wins
        if (top) { const a = heightMap ? 0.5 : 1; r = top.color[0] * a + r * (1 - a); g = top.color[1] * a + g * (1 - a); bl = top.color[2] * a + bl * (1 - a); }
        const i = (py * outW + px) * 3; rgb[i] = r; rgb[i + 1] = g; rgb[i + 2] = bl;
      }
    }
    // point markers (location-type places) as small filled squares
    for (const f of feats) for (const [x, y] of f.points) {
      const cx = Math.round(((x - b.x1) / spanX) * outW), cy = Math.round(((b.y2 - y) / spanY) * outH);
      for (let dy = -3; dy <= 3; dy++) for (let dx = -3; dx <= 3; dx++) {
        const xx = cx + dx, yy = cy + dy;
        if (xx >= 0 && xx < outW && yy >= 0 && yy < outH) { const i = (yy * outW + xx) * 3; rgb[i] = f.color[0]; rgb[i + 1] = f.color[1]; rgb[i + 2] = f.color[2]; }
      }
    }

    // phase boundary outlines (drawn on top so the regions stay readable underneath)
    if (phases) {
      const gx2px = (gx: number) => Math.round(((gx - b.x1) / spanX) * outW);
      const gy2px = (gy: number) => Math.round(((b.y2 - gy) / spanY) * outH);
      const thick = Math.max(2, Math.round(outW / 350));
      for (const ph of PHASE_BOUNDARIES)
        for (let i = 0; i < ph.poly.length; i++) {
          const a = ph.poly[i], c = ph.poly[(i + 1) % ph.poly.length];
          drawLinePx(rgb, outW, outH, gx2px(a[0]), gy2px(a[1]), gx2px(c[0]), gy2px(c[1]), ph.color, thick);
        }
    }

    const png = encodePNG(outW, outH, rgb);
    const base64 = Buffer.from(png).toString("base64");

    const filePath = outputPath ?? join(tmpdir(), `razarion-region-map-${serverGameEngineConfigId}.png`);
    let savedNote = "";
    try {
      writeFileSync(filePath, png);
      savedNote = `Saved PNG to: ${filePath}\n(open it with:  Invoke-Item '${filePath}' )\n\n`;
    } catch (e: any) {
      savedNote = `Could not write PNG to ${filePath}: ${e?.message ?? e}\n\n`;
    }

    let legend = `Region Map Image - server-game-engine ${serverGameEngineConfigId}\n`;
    legend += `Bounds: X ${b.x1.toFixed(0)}..${b.x2.toFixed(0)}  Y ${b.y1.toFixed(0)}..${b.y2.toFixed(0)}  |  ${outW}x${outH}px  |  Y up = north\n\n`;
    legend += savedNote;
    legend += "Legend (colour = region):\n" + regionLegend(feats, false);
    if (phases) {
      legend += "  [phase outlines]\n";
      for (const ph of PHASE_BOUNDARIES) legend += `    rgb(${ph.color.join(",")}) ${ph.name}\n`;
    }
    return { content: [{ type: "image" as const, data: base64, mimeType: "image/png" }, { type: "text" as const, text: legend }] };
  }
);

// ============================================================
// TERRAIN HEIGHTMAP UPLOAD
// ============================================================

async function apiPostBinary(path: string, data: Uint8Array): Promise<void> {
  const token = await authenticate();
  const response = await fetch(`${BASE_URL}${path}`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/octet-stream",
    },
    body: data as any,
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(`POST ${path} failed: ${response.status} ${text}`);
  }
}

server.tool(
  "upload_heightmap",
  `Upload a complete heightmap for a planet. The heightmap is provided as a flat JSON array of uint16 values (0-65535).
Height conversion: height_meters = uint16 * 0.1 - 200. So uint16=2000 → 0m (water level), uint16=2005 → 0.5m (default land).
Array layout: tile-by-tile in row-major order (tileY * tileXCount + tileX), within each tile node-by-node (localY * 160 + localX).
Tile size: 160x160 nodes. For a 5120x5120m planet: 32x32 tiles, 26,214,400 total values.
WARNING: This replaces the ENTIRE heightmap. Use terrain_minimap first to verify the current state.`,
  {
    planetId: z.number().describe("Planet config ID"),
    heightmap: z.array(z.number()).describe("Flat array of uint16 height values (0-65535), tile-by-tile row-major order"),
  },
  async ({ planetId, heightmap }) => {
    // Convert uint16 array to Little-Endian byte buffer
    const buf = Buffer.alloc(heightmap.length * 2);
    for (let i = 0; i < heightmap.length; i++) {
      const v = Math.max(0, Math.min(65535, Math.round(heightmap[i])));
      buf[i * 2] = v & 0xff;
      buf[i * 2 + 1] = (v >> 8) & 0xff;
    }
    const { gzipSync } = await import("node:zlib");
    const compressed = gzipSync(buf);
    await apiPostBinary(`/rest/editor/planeteditor/updateCompressedHeightMap/${planetId}`, compressed);
    return msg(`Heightmap uploaded for planet ${planetId}. ${heightmap.length} values (${compressed.length} bytes compressed). Use terrain_minimap to verify.`);
  }
);

server.tool(
  "modify_heightmap_region",
  `Modify a rectangular region of the heightmap. Downloads the current heightmap, applies changes to the specified region, and uploads the result.
Supports operations: "set" (absolute height), "add" (add to current), "max" (take maximum), "min" (take minimum).
Height is in METERS. Conversion: uint16 = (height_meters + 200) / 0.1. Water level = 0m, default land = 0.5m.
Coordinate system: (0,0) = bottom-left, X increases right, Y increases up. Each node = 1 meter.`,
  {
    planetId: z.number().describe("Planet config ID"),
    x1: z.number().describe("Left edge X coordinate (meters)"),
    y1: z.number().describe("Bottom edge Y coordinate (meters)"),
    x2: z.number().describe("Right edge X coordinate (meters)"),
    y2: z.number().describe("Top edge Y coordinate (meters)"),
    height: z.number().describe("Height value in meters"),
    operation: z.enum(["set", "add", "max", "min"]).optional().describe("Operation to apply (default: set)"),
  },
  async ({ planetId, x1, y1, x2, y2, height, operation }) => {
    const op = operation ?? "set";

    // Fetch current heightmap
    const buffer = await publicFetchBinary(`/rest/terrainHeightMap/${planetId}`);
    const bytes = new Uint8Array(buffer);
    const hmLen = Math.floor(bytes.length / 2);
    const hmap = new Uint16Array(hmLen);
    for (let i = 0; i < hmLen; i++) {
      hmap[i] = bytes[i * 2] + (bytes[i * 2 + 1] << 8);
    }

    // Determine planet tile dimensions
    const terrainShape = (await publicFetchJson(`/rest/terrainshape/${planetId}`)) as {
      nativeTerrainShapeTiles?: unknown[][];
    };
    if (!terrainShape.nativeTerrainShapeTiles) throw new Error("No terrain shape");
    const tileXCount = terrainShape.nativeTerrainShapeTiles.length;

    const hu16 = Math.max(0, Math.min(65535, Math.round((height + 200) / 0.1)));
    const nx1 = Math.max(0, Math.floor(x1));
    const ny1 = Math.max(0, Math.floor(y1));
    const nx2 = Math.min(Math.ceil(x2), tileXCount * NODE_X_COUNT - 1);
    const ny2 = Math.min(Math.ceil(y2), terrainShape.nativeTerrainShapeTiles[0].length * NODE_Y_COUNT - 1);

    let modified = 0;
    for (let ny = ny1; ny <= ny2; ny++) {
      for (let nx = nx1; nx <= nx2; nx++) {
        const tx = Math.floor(nx / NODE_X_COUNT);
        const ty = Math.floor(ny / NODE_Y_COUNT);
        const lx = nx % NODE_X_COUNT;
        const ly = ny % NODE_Y_COUNT;
        const idx = TILE_NODE_SIZE * (ty * tileXCount + tx) + ly * NODE_X_COUNT + lx;
        if (idx >= hmLen) continue;

        if (op === "set") hmap[idx] = hu16;
        else if (op === "add") hmap[idx] = Math.max(0, Math.min(65535, hmap[idx] + hu16 - 2000));
        else if (op === "max") hmap[idx] = Math.max(hmap[idx], hu16);
        else if (op === "min") hmap[idx] = Math.min(hmap[idx], hu16);
        modified++;
      }
    }

    // Upload
    const outBuf = Buffer.alloc(hmLen * 2);
    for (let i = 0; i < hmLen; i++) {
      outBuf[i * 2] = hmap[i] & 0xff;
      outBuf[i * 2 + 1] = (hmap[i] >> 8) & 0xff;
    }
    const { gzipSync } = await import("node:zlib");
    const compressed = gzipSync(outBuf);
    await apiPostBinary(`/rest/editor/planeteditor/updateCompressedHeightMap/${planetId}`, compressed);

    return msg(`Modified ${modified} nodes in region (${nx1},${ny1})-(${nx2},${ny2}), op=${op}, height=${height}m. Use terrain_minimap to verify.`);
  }
);

// ============================================================
// PROMPTS
// ============================================================

server.prompt(
  "generate_phase2_terrain",
  `Generate Phase 2 ("Semi-Noob Frontier") terrain for a Razarion planet. Provides complete design specifications, terrain feature parameters, and a step-by-step generation procedure.`,
  { planetId: z.string().describe("Planet config ID (e.g. '117')") },
  async ({ planetId }) => ({
    messages: [
      {
        role: "user" as const,
        content: {
          type: "text" as const,
          text: `# Generate Phase 2 Terrain for Planet ${planetId}

## 1. Phase 2 Overview

Phase 2 ("Semi-Noob Frontier") is the second map phase in Razarion.
- **Region:** X: 0–2000, Y: 0–2000 (minus Phase 1 at X: 0–820, Y: 0–800)
- **Area:** ~3.34 km² wrapping around Phase 1 on top and right
- **Theme:** Exploration & unlock — players find Crystal boxes guarded by defensive bot outposts
- **Terrain character:** More varied than Phase 1 — chokepoints, elevated areas, open plains, border ridges

## 2. Terrain Features to Generate

### 2.1 Rolling Base Terrain (entire Phase 2)
- Multi-octave sine noise for gentle undulation
- Height range: 0.3–3.5m (always above water level 0m)
- Algorithm:
  \`\`\`
  noise(x,y,seed) = sin(x*0.0131+seed+1.7)*cos(y*0.0173+seed*1.3)*0.5
                   + sin(x*0.0293+y*0.0311+seed*2.1)*0.25
                   + cos(x*0.0531-y*0.0471+seed*3.7)*0.125
                   + sin(x*0.0971+y*0.0893+seed*5.3)*0.0625
  base_height = max(0.3, 1.5 + noise(x,y,42)*1.2 + noise(x,y,123)*0.6 + fineNoise(x,y,77)*0.2)
  \`\`\`

### 2.2 Ring Hill Formation
- **Center:** (1350, 1650) — matches design doc range X: 900–1800, Y: 1200–2100
- **Inner radius:** 200m, **Outer radius:** 420m
- **Peak height:** ~22m with ±6m noise variation
- **Profile:** Smooth bell curve across ring wall width
- **4 Gaps (passes):** East (0°, width 0.38 rad), NNE (99°, 0.30), West (180°, 0.35), SSW (-72°, 0.28)
- **Interior plateau:** 3–5m elevated, suitable for base building
- Gaps create natural chokepoints where bot outposts can guard

### 2.3 Eastern Border Ridge (Phase 2 → Phase 3)
- **Position:** X: 1870–2070, ridge peak at X≈1960
- **Height:** 16m base + ±4m noise
- **3 Passes at:** Y≈400, Y≈1150, Y≈1750 (pass width: 90m with smoothstep)
- Separates Phase 2 from Phase 3 territory

### 2.4 Northern Border Ridge (Phase 2 → Phase 4)
- **Position:** Y: 1870–2070, ridge peak at Y≈1960
- **Height:** 14m base + ±4m noise
- **3 Passes at:** X≈450, X≈1050, X≈1650 (pass width: 90m)
- Separates Phase 2 from Phase 4 territory

### 2.5 Scattered Hills (tactical cover)
| Center X | Center Y | Radius | Height |
|----------|----------|--------|--------|
| 550      | 1100     | 85m    | 9m     |
| 1700     | 400      | 65m    | 7m     |
| 280      | 1550     | 75m    | 11m    |
| 1100     | 500      | 95m    | 8m     |
| 1680     | 1050     | 55m    | 6m     |
| 850      | 350      | 70m    | 7m     |
| 400      | 900      | 60m    | 5m     |

### 2.6 Ponds (water features)
| Center X | Center Y | Radius | Depth  |
|----------|----------|--------|--------|
| 1300     | 1680     | 30m    | -3.0m  | (inside ring)
| 1400     | 1600     | 22m    | -2.0m  | (inside ring)
| 750      | 1250     | 35m    | -2.5m  | (west of ring)

### 2.7 Phase 1 Transition Zone
- **Preserve:** All nodes at X<810, Y<790 (Phase 1 core)
- **Blend zone:** 60m wide at Phase 1 boundary using smoothstep
- **Edge fade:** Beyond X=2000 or Y=2000, fade to existing terrain over 100m

## 3. Technical Reference

### Heightmap Format
- uint16 values, Little-Endian, GZIP-compressed
- Conversion: \`height_m = uint16 * 0.1 - 200\`, \`uint16 = (height_m + 200) / 0.1\`
- Key values: 0m (water) = uint16 2000, 0.5m (default) = uint16 2005
- Tile layout: 160×160 nodes per tile, row-major order (tileY * tileXCount + tileX)

### REST Endpoints
- Download: \`GET /rest/terrainHeightMap/{planetId}\` (GZIP binary, public)
- Upload: \`POST /rest/editor/planeteditor/updateCompressedHeightMap/{planetId}\` (GZIP binary, requires JWT)
- Auth: \`POST /rest/user/auth\` with HTTP Basic → returns JWT token

## 4. Procedure

1. Use \`terrain_minimap\` tool to view current terrain state for planet ${planetId}
2. Write a Node.js script that:
   a. Authenticates via \`POST /rest/user/auth\` (Basic auth admin@admin.com:1234 → JWT)
   b. Downloads heightmap via \`GET /rest/terrainHeightMap/${planetId}\`
   c. Decompresses GZIP, parses uint16 Little-Endian array
   d. Iterates Phase 2 nodes (X: 0–2100, Y: 0–2100, skipping Phase 1 core)
   e. Computes height = sum of all feature generators (base + ring + ridges + hills + ponds)
   f. Applies Phase 1 transition blending
   g. Packs back to uint16 LE, GZIP compresses
   h. Uploads via \`POST /rest/editor/planeteditor/updateCompressedHeightMap/${planetId}\` with Bearer JWT
3. Run the script
4. Verify with \`terrain_minimap_image\` tool
5. Apply changes with \`restart_planet_warm\` tool

## 5. Expected Result

The Phase 2 terrain should show:
- A distinct ring-shaped hill formation in the center-west with 4 passable gaps
- Brown/elevated border ridges along X≈2000 and Y≈2000 with passable gaps
- Gently rolling green terrain throughout (no flat areas)
- A few scattered hills providing tactical variety
- 3 small ponds (2 inside ring, 1 outside)
- Smooth transitions to Phase 1 (preserved) and to flat Phase 3/4 areas
- Water coverage ~1.4%, height range roughly -10m to 30m`,
        },
      },
    ],
  })
);

// --- Start ---
const transport = new StdioServerTransport();
await server.connect(transport);
console.error("Razarion AI Content MCP server running on stdio");
