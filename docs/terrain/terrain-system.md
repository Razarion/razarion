# Terrain System

This document describes the Razarion terrain system: heightmap data format, height calculation, terrain classification, BotGround/slope system, and REST endpoints. It serves as reference for the **razarion-ai-content MCP Server** to correctly generate and manipulate terrain.

## 1. Overview

A planet is divided into **tiles**. Each tile is a grid of **160 x 160 nodes**, where each node represents **1 meter**. The terrain height is stored as a **Uint16Array** (one 16-bit unsigned integer per node), GZIP-compressed on the server.

Key source files:
- `razarion-share/.../terrain/TerrainUtil.java` -- constants, coordinate conversion
- `razarion-share/.../terrain/container/TerrainAnalyzer.java` -- terrain classification, height lookup
- `razarion-frontend/.../babylon-terrain-tile.impl.ts` -- client-side rendering and height conversion

## 2. Heightmap Data Format

| Property | Value |
|----------|-------|
| Storage per node | `uint16` (16-bit unsigned integer) |
| Compression | GZIP (server to client) |
| Byte order | Little-Endian: `value = lowerByte + (upperByte << 8)` |
| Tile dimensions | 160 x 160 = **25,600** values per tile |

### Byte-to-Uint16 Conversion

The server stores the heightmap as a GZIP-compressed byte array. After decompression, each pair of bytes is combined into a uint16 value (see `CollectionUtils.convertToUnsignedIntArray`):

```java
int lower = byteArray[i * 2];
if (lower < 0) lower = 256 + lower;  // signed byte -> unsigned
int upper = byteArray[i * 2 + 1];
if (upper < 0) upper = 256 + upper;
unsignedIntArray[i] = lower + (upper << 8);  // Little-Endian
```

Source: `razarion-share/.../utils/CollectionUtils.java`

### Array-Layout (1D to 2D Mapping)

The heightmap is stored as a **single flat 1D array** covering the entire planet. The planet is divided into tiles, and tiles are stored **tile-by-tile** in the array.

#### Planet Dimensions

```
tileXCount = ceil(planetSize.x / NODE_X_COUNT)    // e.g. ceil(480 / 160) = 3
tileYCount = ceil(planetSize.y / NODE_Y_COUNT)
TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT = 160 * 160 = 25,600
totalArrayLength = tileXCount * tileYCount * TILE_NODE_SIZE
```

#### Tile Order in the Array

Tiles are stored in **row-major order** (Y first, then X):

```
tileStartIndex = TILE_NODE_SIZE * (tileY * tileXCount + tileX)
```

For a 3x2 planet (3 tiles wide, 2 tiles tall), the tile order is:
```
Array: [Tile(0,0)][Tile(1,0)][Tile(2,0)][Tile(0,1)][Tile(1,1)][Tile(2,1)]
Index:  0..25599   25600..    51200..    76800..    102400..   128000..
```

#### Node Order within a Tile

Within each tile, nodes are stored in **row-major order** (Y row by row, X within each row):

```
nodeOffset = localY * NODE_X_COUNT + localX
```

So for a tile, the first 160 values are row Y=0 (X=0..159), the next 160 values are row Y=1, etc.

#### Global Index Formula

To find the height value for an absolute node position `(nodeX, nodeY)`:

```
tileX = floor(nodeX / 160)
tileY = floor(nodeY / 160)
localX = nodeX % 160
localY = nodeY % 160

globalIndex = TILE_NODE_SIZE * (tileY * tileXCount + tileX) + localY * 160 + localX
height = uint16ToHeight(heightMap[globalIndex])
```

Source: `TerrainAnalyzer.getUInt16GroundHeightAt()`, `game-mock.service.ts:getTileHeightMapStart()`

#### Client-Side Per-Tile Array (161x161)

For rendering, the client extracts each tile into a separate `Uint16Array` of size **(NODE_X_COUNT + 1) * (NODE_Y_COUNT + 1) = 161 * 161 = 25,921**. The extra +1 column and +1 row are copied from the neighboring tiles (east and north) to create seamless edges between tiles.

```
Per-tile index = localY * 161 + localX     // stride = 161
```

The edge values come from:
- Column X=160: first column (X=0) of the tile to the east (tileX+1)
- Row Y=160: first row (Y=0) of the tile to the north (tileY+1)
- Corner (160,160): node (0,0) of the diagonal neighbor (tileX+1, tileY+1)

Source: `game-mock.service.ts:setupHeightMap()` (TypeScript copy of `ClientNativeTerrainShapeAccess.createTileGroundHeightMap()`)

## 3. Height Calculation (Conversion)

### Constants

| Constant | Value | Source |
|----------|-------|--------|
| `HEIGHT_PRECISION` | `0.1` | `TerrainUtil.java` |
| `HEIGHT_MIN` | `-200` | `TerrainUtil.java` |
| `WATER_LEVEL` | `0` | `TerrainUtil.java` |
| `WALL_HEIGHT_DIFF` | `0.5` | `TerrainUtil.java` |
| `HEIGHT_DEFAULT` | `0.5` | `TerrainUtil.java` |
| `NODE_SIZE` | `1.0` | `TerrainUtil.java` |
| `NODE_X_COUNT` | `160` | `TerrainUtil.java` |
| `NODE_Y_COUNT` | `160` | `TerrainUtil.java` |

### Uint16 to Height

```
height = uint16 * HEIGHT_PRECISION + HEIGHT_MIN
       = uint16 * 0.1 + (-200)
```

### Height to Uint16

```
uint16 = (height - HEIGHT_MIN) / HEIGHT_PRECISION
       = (height - (-200)) / 0.1
       = (height + 200) / 0.1
```

(Rounded to nearest 0.1 before conversion: `Math.round(value * 10) / 10`)

### Value Range

| Uint16 | Height |
|--------|--------|
| 0 | -200.0 m |
| 2000 | 0.0 m (water level) |
| 2005 | 0.5 m (default height) |
| 65535 | +6353.5 m |

Source: `TerrainUtil.uint16ToHeight()`, `TerrainUtil.heightToUnit16()`

## 4. Terrain Classification (TerrainType)

Each node is classified based on the heights of its four corner nodes (bottom-left, bottom-right, top-right, top-left).

### TerrainType Enum

| Value | Description |
|-------|-------------|
| `LAND` | Walkable flat terrain |
| `WATER` | Below water surface |
| `LAND_COAST` | Beach/shoreline |
| `WATER_COAST` | Water shore |
| `BLOCKED` | Impassable (too steep or obstructed) |

Source: `razarion-share/.../terrain/container/TerrainType.java`

### Classification Algorithm

```
1. If blocked by a TerrainObject (radius > 0 and position within radius) -> BLOCKED
2. Get heights of 4 corner nodes: BL, BR, TR, TL
3. avgHeight = (BL + BR + TR + TL) / 4
4. If avgHeight < WATER_LEVEL (0) -> WATER
5. maxHeight = max(BL, BR, TR, TL)
   minHeight = min(BL, BR, TR, TL)
6. If |maxHeight - minHeight| < WALL_HEIGHT_DIFF (0.5) -> LAND
7. Otherwise -> BLOCKED
```

Source: `TerrainAnalyzer.analyze()`

### Terrain Object Blocking

Terrain objects with `radius > 0` in their `TerrainObjectConfig` block the node at center position. The check uses the distance from the node center to each terrain object position:

```
if distance(nodeCenter, objectPosition) < objectRadius -> BLOCKED
```

Source: `TerrainAnalyzer.isBlockedByTerrainObject()`

## 5. BotGround / Slope System

BotGrounds are flat platforms (driveways) placed by bot configurations. They consist of flat areas at a defined height and slope boxes (ramps) connecting different elevations.

### BotGround Structure

| Field | Type | Description |
|-------|------|-------------|
| `model3DId` | int | 3D model reference |
| `height` | double | Platform height |
| `positions` | DecimalPosition[] | Platform polygon vertices |
| `botGroundSlopeBoxes` | SlopeBox[] | Ramp definitions |

### SlopeBox Structure

| Field | Type | Description |
|-------|------|-------------|
| `xPos` | double | Box start X position |
| `yPos` | double | Box start Y position |
| `height` | double | Base height of the ramp |
| `yRot` | double | Direction (rotation around Y axis, radians) |
| `zRot` | double | Slope angle (rotation around Z axis, radians) |

Slope boxes are 8 x 8 meter ramps (`BOT_BOX_LENGTH = 8`).

### Slope Height Calculation

The height at a position within a slope box depends on the direction (`yRot`) and slope angle (`zRot`):

```
heightDelta = tan(zRot) * BOT_BOX_LENGTH    // = tan(zRot) * 8

// Factor (0..1) based on direction:
if yRot < 90deg:   factor = (position.x - box.xPos) / 8
if yRot < 180deg:  factor = 1 - (position.y - box.yPos) / 8
if yRot < 270deg:  factor = 1 - (position.x - box.xPos) / 8
else:               factor = (position.y - box.yPos) / 8

height = box.height + heightDelta * factor - heightDelta / 2
```

This simplifies to:
```
height = box.height + tan(zRot) * 8 * factor - tan(zRot) * 4
```

Source: `TerrainAnalyzer.findHeightInSlopeBoxes()`

## 6. REST Endpoints

### Public Endpoints

#### Get Heightmap (binary)
```
GET /rest/terrainHeightMap/{planetId}
```
- **Response**: GZIP-compressed binary (`application/octet-stream`)
- **Headers**: `Content-Encoding: gzip`
- Returns the raw heightmap as GZIP-compressed byte array (Little-Endian uint16 pairs)

Source: `razarion-server/.../rest/engine/TerrainHeightMapControllerImpl.java`

#### Get TerrainShape (JSON)
```
GET /rest/terrainshape/{planetId}
```
- **Response**: `NativeTerrainShape` JSON (`application/json`)
- Contains terrain objects, decals, and bot grounds per tile

Source: `razarion-server/.../rest/engine/TerrainShapeControllerImpl.java`

### Editor Endpoints (require ADMIN role)

#### Upload Heightmap
```
POST /rest/editor/planeteditor/updateCompressedHeightMap/{planetId}
```
- **Request body**: GZIP-compressed byte array (`application/octet-stream`)
- Replaces the entire heightmap for the planet

#### Update Terrain Objects (CRUD)
```
PUT /rest/editor/planeteditor/updateTerrain/{planetId}
```
- **Request body**: `TerrainEditorUpdate` JSON (`application/json`)
- Supports creating, updating, and deleting terrain objects in a single request

**TerrainEditorUpdate structure:**
```json
{
  "createdTerrainObjects": [TerrainObjectPosition, ...],
  "updatedTerrainObjects": [TerrainObjectPosition, ...],
  "deletedTerrainObjectsIds": [int, ...]
}
```

#### Update Mini-Map Image
```
PUT /rest/editor/planeteditor/updateMiniMapImage/{planetId}
```
- **Request body**: Data URL string (`text/plain`)

Source: `razarion-server/.../rest/editor/TerrainEditorController.java`

## 7. Data Models (DTOs)

### TerrainObjectPosition

| Field | Type | Description |
|-------|------|-------------|
| `id` | int | Unique identifier |
| `terrainObjectConfigId` | int | Reference to TerrainObjectConfig |
| `position` | DecimalPosition | 2D placement (x, y) |
| `scale` | Vertex (nullable) | 3D scale (x, y, z) |
| `rotation` | Vertex (nullable) | 3D rotation in radians (x, y, z) |
| `offset` | Vertex (nullable) | 3D offset adjustment (x, y, z) |

Source: `razarion-share/.../dto/TerrainObjectPosition.java`

### TerrainObjectConfig

| Field | Type | Description |
|-------|------|-------------|
| `id` | int | Unique identifier |
| `internalName` | String | Config reference name |
| `radius` | double | Collision/blocking radius (0 = no blocking) |
| `model3DId` | Integer (nullable) | 3D model reference |

Source: `razarion-share/.../dto/TerrainObjectConfig.java`

### GroundConfig

| Field | Type | Description |
|-------|------|-------------|
| `id` | int | Unique identifier |
| `internalName` | String | Config name |
| `groundBabylonMaterialId` | Integer | Babylon.js ground material |
| `waterBabylonMaterialId` | Integer | Babylon.js water material |
| `underWaterBabylonMaterialId` | Integer | Babylon.js underwater material |
| `botBabylonMaterialId` | Integer | Babylon.js bot/slope material |
| `botWallBabylonMaterialId` | Integer | Babylon.js bot wall material |

Source: `razarion-share/.../dto/GroundConfig.java`

### PlanetConfig

| Field | Type | Description |
|-------|------|-------------|
| `id` | int | Unique identifier |
| `internalName` | String | Config name |
| `size` | DecimalPosition | Planet dimensions in meters (x, y) |
| `groundConfigId` | Integer | Reference to GroundConfig |
| `houseSpace` | int | Total building space |
| `startRazarion` | int | Starting currency |
| `startBaseItemTypeId` | Integer | Starting base item type |
| `itemTypeLimitation` | Map<Integer, Integer> | Item type ID to max count |

Source: `razarion-share/.../config/PlanetConfig.java`

### NativeTerrainShape (JSON response)

```
NativeTerrainShape
  nativeTerrainShapeTiles: NativeTerrainShapeTile[][]    // 2D array indexed by tile position

NativeTerrainShapeTile
  nativeTerrainShapeObjectLists: NativeTerrainShapeObjectList[]
  nativeBabylonDecals: NativeBabylonDecal[]
  nativeBotGrounds: NativeBotGround[]

NativeTerrainShapeObjectList
  terrainObjectConfigId: int
  terrainShapeObjectPositions: NativeTerrainShapeObjectPosition[]

NativeTerrainShapeObjectPosition
  terrainObjectId: int
  x: double
  y: double
  scale: NativeVertex (nullable)
  rotation: NativeVertex (nullable)
  offset: NativeVertex (nullable)

NativeBabylonDecal
  babylonMaterialId: int
  xPos: double
  yPos: double
  xSize: double
  ySize: double

NativeBotGround
  model3DId: int
  height: double
  positions: NativeDecimalPosition[]
  botGroundSlopeBoxes: NativeBotGroundSlopeBox[]

NativeBotGroundSlopeBox
  xPos: double
  yPos: double
  height: double
  yRot: double
  zRot: double

NativeVertex
  x: double
  y: double
  z: double
```

Source: `razarion-share/.../terrain/container/json/Native*.java`
