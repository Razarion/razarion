# Terrain Rendering Architecture

The terrain rendering system composites two layers per terrain tile: a **ground mesh** (with multi-material submeshes handling both land and underwater) and a **water surface mesh**. Together they create a seamless transition from deep water through shoreline to land.

## Scene Hierarchy

Each terrain tile is organized as:

```
TerrainTile Container (TransformNode)
├── Ground Mesh (MultiMaterial with 2 submeshes)
│   ├── SubMesh 0: Ground material     (GROUND) — includes underwater depth blending
│   └── SubMesh 1: Asphalt material    (ASPHALT)
├── Water Mesh (separate mesh at y=0, single material)
├── Terrain Objects (rocks, trees, etc.)
└── Bot Grounds (base/structure ground)
```

## Height Zones

The terrain uses vertex height (`position.y`) to define visual zones. The ground material handles all zones in a single shader using smooth blending:

```
                    GROUND material (land blend)
  height 0.1m ─── underwater edge1 ────────────────
                    GROUND material (beach/underwater transition)
  height 0.0m ─── WATER_LEVEL ──────────────────────
                    GROUND material (underwater depth gradient)
                    Water surface mesh floats here
  height -10m ─── deep water ───────────────────────
```

| Constant       | Value | Purpose                                       |
|----------------|-------|-----------------------------------------------|
| `WATER_LEVEL`  | 0.0   | Sea level; water mesh sits here               |
| `WALL_HEIGHT_DIFF` | 0.5 | Height diff threshold for BLOCKED terrain   |

## Material Assignment Logic

Each quad face (4 corner vertices) is evaluated during mesh construction. The material is selected based on terrain type:

```typescript
const terrainType = setupTerrainType(bLHeight, bRHeight, tRHeight, tLHeight);

if (decal) {
  materialIndex = terrainType === TerrainType.LAND ? ASPHALT : GROUND;
} else if (terrainType === TerrainType.BLOCKED) {
  materialIndex = ASPHALT;
} else {
  materialIndex = GROUND;           // Handles land, beach, and underwater via shader
}
```

The ground material's shader handles the land-to-underwater transition internally using `smoothstep` on vertex height, so there is no need for a separate underwater material index.

The `TerrainType` enum (from Java `TerrainType.java`) classifies quads by their corner heights:

| Type          | Condition                           | Buildable |
|---------------|-------------------------------------|-----------|
| `LAND`        | All corners > 0                     | Yes       |
| `WATER`       | All corners ≤ 0                     | Yes       |
| `LAND_COAST`  | Mixed, mostly land                  | No        |
| `WATER_COAST` | Mixed, mostly water                 | No        |
| `BLOCKED`     | Height difference ≥ 0.5m (steep)   | No        |

## Ground Material

**File:** `razarion-frontend/src/app/game/renderer/ground-material.ts`

The ground `NodeMaterial` handles all terrain rendering in a single shader — land, beach, and underwater — using height-based blending.

### Terrain Layers

1. **Ground Upper** — grass/soil (UV scale 20)
2. **Ground Under** — darker soil variant (UV scale 20)
3. **Mountain** — triplanar rocky texture (UV scale 6.39)

### Blending Chain

```
                          ┌─────────────────────────┐
  Ground Upper ──┐        │  GroundUtility.r channel │
                 ├─ Lerp (height texture) ──┐       │
  Ground Under ──┘                          ├─ Lerp (mountain blend)
                              Mountain ─────┘       │
                                                    │
                                    ┌───────────────┘
                                    │
  Beach Diffuse Texture ──┐         │
  (wet sand darkened) ────┤         │
                          ├─ Lerp (beach step) ──┐
  Mountain/Ground ────────┘                      ├─ Lerp (underwater step) ── Final Diffuse
                                                 │
  Underwater Gradient ──┐                        │
  (+ shallow sand tex)  ├───────────────────────┘
                        │
```

**Beach textures:** The beach uses dedicated diffuse (`ground-beach-diffuse.png`) and normal (`ground-beach-norm.png`) textures instead of a flat color. A UV scale of 4 controls texture tiling.

**Wet sand effect:** Near the waterline, the beach diffuse is darkened to 90% to simulate wet sand. This uses a `smoothstep(0.15, -0.5, position.y)` — the effect transitions from dry sand (above y=0.15) through the waterline into shallow underwater (y=-0.5). The darkened texture is also used for the shallow underwater sand blend.

**Wet sand bump reduction:** In a zone near the shoreline (controlled by UV2.x shore distance), the bump strength is reduced from the normal beach value (0.44) to near-flat (0.05), creating the smooth wet sand appearance seen on real beaches.

### Beach Detection (Splatter)

The sand-to-grass transition uses a dual-layer noise approach for organic, fringed edges:

**Splatter layers:**
- **Large scale** (`ground-splatter.jpg`, UV scale 2) — defines the overall shape of grass patches
- **Fine scale** (same texture, UV scale 16) — adds small fringe detail at the edges

Both layers are averaged, then combined with vertex height:

```
beachValue = (avg(splatterLarge.r, splatterFine.r) - 0.4) × 0.6 + position.y × 1.2
beachStep = smoothstep(0.23, 0.30, beachValue)
```

Where `beachStep = 0` → sand, `beachStep = 1` → grass/terrain.

The height bias factor (1.2×) makes higher terrain favor grass and lower terrain favor sand. With `HEIGHT_PRECISION` of 0.01m there are ~50 discrete height steps in the beach zone (y=0.0 to 0.5), enabling fine height-dependent gradients.

**Grass edge shadow:** A thin darkening strip (60% brightness) is applied on the sand side near the grass edge (`beachStep 0.0–0.3`), simulating the shadow cast by raised grass onto the sand below.

**Underwater blending:** A `smoothstep(0.0, 0.1, position.y)` determines the underwater factor. Below water level, a depth gradient replaces the land diffuse color. In the shallow underwater zone (y=0 to y=-1), the sand texture is blended over the gradient for a natural sand-to-water transition.

### Underwater Depth Gradient

Uses `position.y` scaled by 0.1 to drive a color gradient:

| Depth (y)  | Gradient | Color                          |
|------------|----------|--------------------------------|
| 0.0m       | 0.0      | Sand (0.906, 0.847, 0.792)    |
| -0.3m      | 0.03     | Darker sand                    |
| -0.7m      | 0.07     | Sand-to-water transition       |
| -1.5m      | 0.15     | Blue-green                     |
| -6.0m      | 0.60     | Dark blue                      |
| -10.0m     | 1.0      | Near black                     |

In the shallow zone (y=0 to y=-1), the sand diffuse texture (darkened) is blended over the gradient, providing a textured sand floor visible through shallow water.

**Normal mapping:** Separate normal maps per layer (beach, ground upper/under, mountain triplanar) are blended with the same lerp chain. Bump strengths: beach 0.44, ground 0.28, mountain 1.5, underwater 0.05. Near the shoreline, the wet sand zone reduces bump to 0.05 for a smooth flat appearance.

### GroundUtility Texture

A runtime-generated texture encoding terrain metadata. The **red channel** controls mountain/rock blending intensity. This texture is set externally by `BabylonTerrainTileImpl` after material creation.

## Asphalt Material

The asphalt material is a decorative pavement texture used wherever terrain decals are placed (e.g. player-built areas). It is loaded from `GroundConfig.asphaltBabylonMaterialId` as a `NodeMaterial`.

Previously called "Bot material", the asphalt serves purely as a visual surface that players can build on. It is applied to:
- **Decal zones on land** (`terrainType === LAND` with a decal present)
- **Blocked/steep terrain** (`terrainType === BLOCKED`) regardless of decal

The asphalt material is **not** programmatically built — it is a pre-authored `NodeMaterial` loaded from the database via `BabylonModelService`.

## Water Surface

**File:** `razarion-frontend/src/app/game/renderer/babylon-water-render.service.ts`

A separate `MeshBuilder.CreateGround` mesh positioned at `y = 0` (water level) with 160×160 subdivisions matching the tile size.

### Properties

- **Material:** Pre-authored `NodeMaterial` loaded from `GroundConfig.waterBabylonMaterialId` via `BabylonModelService`
- **Reflection:** Cube texture (`renderer/env/clouds.jpg`) set at runtime on the material's `Reflection` block
- **Transparency:** Enabled (`ignoreAlpha = false`); depth-based via UV2
- **UV2 data:** Encodes the ground height map so the water shader can adjust transparency and effects based on underlying terrain depth

### Depth-Based Transparency

The water mesh receives ground height data through its UV2 channel. The water material uses this to:
- Increase transparency in shallow areas (revealing the underwater ground below)
- Decrease transparency in deep areas (showing darker, more opaque water)
- Modulate reflection intensity based on depth

## Shore Foam (Wave Overlay)

The ground material includes an animated foam effect along the shoreline, creating the appearance of waves rolling from sea toward land. The foam is rendered directly in the ground shader using precomputed shore distance data — no separate mesh is needed.

### Shoreline Detection

**File:** `razarion-frontend/.../renderer/shoreline-detection.ts`

The shoreline is detected using **Marching Squares** on the terrain height grid, extracting the 0m contour (water/land boundary) as line segments. These segments are then chained into connected polylines with cumulative arc-length for UV mapping.

### Shore Distance (UV2)

For each ground mesh vertex, two values are computed and stored in UV2:

| Channel | Value | Description |
|---------|-------|-------------|
| UV2.x | Signed distance | Distance to nearest shoreline. Positive on land, negative underwater |
| UV2.y | Arc-length | Position along the nearest shoreline polyline (continuous U coordinate) |

The arc-length approach ensures smooth, artifact-free UV mapping along the shore. A smoothing pass removes discontinuities at polyline seams and sharp bends.

### Foam Shader (in Ground Material)

The foam is composited on top of the lit ground color as a white overlay controlled by two parameters:

**Fade band** — controls where foam is visible using two `smoothstep` functions:
- Water side: fades in from distance -3 to -0.5 (deep water → shallow)
- Land side: fades out from distance 0.1 to 0.8 (shore → inland)

**Texture mapping** — two foam layers with different scales and scroll speeds are combined via `max()` for a richer wave pattern:

| Layer | U Scale | V Scale | Scroll Speed |
|-------|---------|---------|--------------|
| 1 | 0.1 | 0.25 | 0.2 |
| 2 | 0.15 | 0.35 | 0.13 |

- **U axis** = along shore (from precomputed arc-length in UV2.y)
- **V axis** = perpendicular to shore (from signed distance in UV2.x), scrolling with time

The foam texture (`foam-wave.png`) uses RGB for color and alpha for visibility. Both channels are combined via `max()` across layers. The foam RGB is lerped over the ground color using the combined alpha scaled by fade and opacity (currently 0.7).

### Pipeline

```
Height Grid
    │
    ▼
Marching Squares ──► Shore Segments ──► Polylines with Arc-Length
    │                                           │
    ▼                                           ▼
Signed Distance per vertex              Arc-Length per vertex
    │                                           │
    └──────────── UV2 [dist, arcLen] ───────────┘
                        │
                        ▼
              Ground Shader (foam)
              ├── Fade band (smoothstep on dist)
              ├── Layer 1: foam texture (scale A, speed A)
              ├── Layer 2: foam texture (scale B, speed B)
              └── max(layer1, layer2) × fade × opacity → white overlay
```

## Whitecaps (Open Water Foam)

**Files:** `razarion-frontend/.../renderer/whitecap-material.ts`, `whitecap-texture-generator.ts`

A separate transparent mesh sits just above the water surface (`y = 0.02`) with the same dimensions and UV2 data as the water mesh. It renders sporadic foam patches on open water using:

- **Two noise layers** (foam-noise.png) with different UV scales (8x, 12x) and counter-drifting animation
- **Threshold** (`smoothstep(0.15, 0.35)`) on multiplied noise — only where both layers are bright, whitecaps appear
- **Procedural foam texture** (generated at runtime) providing bubbly/frothy detail within each patch
- **Water mask** — only visible where ground height < 0 (underwater areas)

### Water Material Tuning

The water material is a pre-authored NodeMaterial loaded from the database. At runtime, two parameters are adjusted to reduce visible tiling:

| Parameter | Default | Adjusted | Effect |
|-----------|---------|----------|--------|
| Ground Scale | (15, 15, 15) | (40, 40, 40) | Larger wave pattern, less repetition |
| Wave Speed | 0.1 | 0.03 | Slower, more natural animation |

## Key Source Files

| File | Purpose |
|------|---------|
| `razarion-frontend/.../renderer/babylon-terrain-tile.impl.ts` | Terrain tile mesh construction, material assignment, height processing |
| `razarion-frontend/.../renderer/ground-material.ts` | Ground NodeMaterial (beach/terrain/mountain/underwater blending) |
| `razarion-frontend/.../renderer/babylon-water-render.service.ts` | Water surface mesh creation and UV2 setup |
| `razarion-frontend/.../renderer/shoreline-detection.ts` | Marching Squares shoreline detection, shore distance computation |
| `razarion-frontend/.../renderer/whitecap-material.ts` | Whitecap foam material for open water |
| `razarion-frontend/.../renderer/whitecap-texture-generator.ts` | Procedural foam texture generation |
| `razarion-frontend/.../renderer/ground-util.ts` | Height texture generation utilities |
| `razarion-share/.../terrain/container/TerrainType.java` | TerrainType enum (LAND, WATER, BLOCKED, etc.) |
| `razarion-share/.../dto/GroundConfig.java` | Configuration holding material IDs |
