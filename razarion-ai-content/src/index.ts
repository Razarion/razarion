import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";

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

// --- Start ---
const transport = new StdioServerTransport();
await server.connect(transport);
console.error("Razarion AI Content MCP server running on stdio");
