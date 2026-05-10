# 3D Model Pipeline

This document describes how 3D models are imported, stored, and rendered in Razarion.

## Overview

Razarion uses **glTF 2.0 in binary GLB format** as its 3D model format. Models are uploaded through the admin editor, stored in the database, and rendered at runtime via Babylon.js.

```
.glb file (Blender export)
    |
    v
GLTF Editor (Admin UI) --- parses meshes & materials
    |
    v
Database Storage (GltfEntity, Model3DEntity, BabylonMaterialEntity)
    |
    v
Runtime Loading (UiConfigCollection -> lazy AssetContainers)
    |
    v
Rendering (clone nodes, apply materials, attach animations)
```

## Data Model

### Entity Relationships

```
BaseItemType / TerrainObjectConfig
         |
         +-> Model3DEntity (references a specific mesh within a GLB)
                   |
                   +-> GltfEntity (stores the actual GLB binary)
                           |
                           +-> GltfBabylonMaterialEntity[] (material mappings)
                                   |
                                   +-> BabylonMaterialEntity (Babylon.js material definition)
```

### GltfEntity

Stores the actual GLB file as a binary blob. One GLB file can contain multiple meshes/nodes.

| Field | Type | Description |
|-------|------|-------------|
| `id` | int | Primary key |
| `internalName` | String | Human-readable name |
| `glb` | byte[] (BLOB) | Raw GLB binary data |
| `gltfBabylonMaterials` | List | Material name mappings (glTF material -> Babylon material) |

Source: `razarion-server/.../model/ui/GltfEntity.java`

### Model3DEntity

References a specific named mesh/node within a GltfEntity. Multiple Model3D entries can point to the same GltfEntity (e.g., different parts of a complex scene).

| Field | Type | Description |
|-------|------|-------------|
| `id` | int | Primary key (this is the `model3DId` used in configs) |
| `internalName` | String | Human-readable name |
| `gltfName` | String | Exact mesh/node name from the glTF file (e.g., `"Mesh.001"`) |
| `gltfEntity` | FK | Reference to parent GltfEntity |

Source: `razarion-server/.../model/ui/Model3DEntity.java`

### BabylonMaterialEntity

Stores a serialized Babylon.js material definition (standard PBR or NodeMaterial).

| Field | Type | Description |
|-------|------|-------------|
| `id` | int | Primary key |
| `internalName` | String | Human-readable name |
| `data` | byte[] (BLOB) | Serialized Babylon.js material JSON |
| `nodeMaterial` | boolean | `true` = NodeMaterial, `false` = standard material |
| `diplomacyColorNode` | String | Node name for faction color overrides |
| `overrideAlbedoTextureNode` | String | Node to replace with GLB's albedo texture |
| `overrideMetallicTextureNode` | String | Node to replace with GLB's metallic texture |
| `overrideBumpTextureNode` | String | Node to replace with GLB's bump texture |
| `overrideAmbientOcclusionTextureNode` | String | Node to replace with GLB's AO texture |

Source: `razarion-server/.../model/ui/BabylonMaterialEntity.java`

### GltfBabylonMaterialEntity

Maps a material name from the glTF file to a BabylonMaterialEntity.

| Field | Type | Description |
|-------|------|-------------|
| `gltfMaterialName` | String | Material name as it appears in the glTF file |
| `babylonMaterialEntity` | FK | Reference to BabylonMaterialEntity |

Source: `razarion-server/.../model/ui/GltfBabylonMaterialEntity.java`

## Import Workflow

### Step 1: Prepare the GLB File

Export your 3D model from Blender (or another tool) as a **GLB file** (binary glTF 2.0).

**Naming conventions for special meshes:**

| Prefix | Purpose | Example |
|--------|---------|---------|
| `RAZ_P_<id>` | Particle system attachment point | `RAZ_P_101` |
| `RAZ_M_P_<id>` | Muzzle flash particle system | `RAZ_M_P_102` |
| `RAZ_MUZZLE` | Beam origin / muzzle position marker (no particle system) | `RAZ_MUZZLE` |
| `RAZ_TURRET_` | Turret mount point | `RAZ_TURRET_Main` |

The `RAZ_MUZZLE` mesh defines the origin point for builder beams and harvester beams. It is hidden at runtime. If no `RAZ_MUZZLE` mesh is present, the beam falls back to `RAZ_M_P_` (muzzle flash emitter) or the model's root position.

### Step 1b: Compress the GLB (optional)

Source GLBs from Blender are typically large (~30 MB for the full Razarion asset bundle). Compress them with `gltf-transform` before uploading. Three default optimizations must be **disabled** because they break Razarion-specific content:

```bash
npx -y -p @gltf-transform/cli@latest gltf-transform optimize \
  razarion.glb razarion_post.glb \
  --compress draco \
  --texture-compress webp \
  --texture-size 1024 \
  --flatten false --join false --prune-attributes false
```

Typical result: **~30 MB → ~8 MB** (≈3.85×).

**Why each disabled flag matters:**

| Flag | Default | Razarion impact when default is kept |
|------|---------|---------------------------------------|
| `--flatten false` | `true` | Flattens scene graph, dropping the `RAZ_MUZZLE`, `RAZ_M_P_<id>`, `RAZ_P_<id>`, and `RAZ_TURRET_<name>` marker nodes. Beams, muzzle flashes, particle attachments, and turret animations stop working. |
| `--join false` | `true` | Joins meshes to reduce draw calls; same destruction of marker-named nodes. Requires `--flatten`, but disable explicitly to be safe. |
| `--prune-attributes false` | `true` | Removes vertex attributes the heuristic considers "unused". Razarion's diplomacy materials are assigned at **runtime** (see `gltf-helper.ts`), so the heuristic cannot see that `TEXCOORD_0` is needed and strips UVs from ~30 building primitives (`ArmoredCabin*`, `Base*`, `Beam*`, `Body*`, `Cabin*`, `Cannon*`, `Flanc*`, `Turret*`). Result: untextured buildings. |

**Other defaults (kept):**

- `--compress draco` — Draco geometry compression. The alternative `meshopt` is faster to decode but compresses less.
- `--texture-compress webp` — WebP for albedo/normal/AO textures. KTX2 saves VRAM but bloats download size; AVIF is slower to decode.
- `--texture-size 1024` — caps texture dimensions at 1024 px. Higher values (2048) double texture bytes with no visible quality gain in-game.

**Tools that don't work:**

- `gltf-pipeline` (Cesium's CLI) does Draco only — it cannot do WebP texture compression. Use `gltf-transform` for the full pipeline.

**Verification:** After compression, confirm node count, RAZ_-marker count, and `TEXCOORD_0` coverage match the source. Quick Node.js check:

```javascript
const fs = require('fs');
function readJson(p) {
  const d = fs.readFileSync(p);
  const jsonLen = d.readUInt32LE(12);
  return JSON.parse(d.slice(20, 20 + jsonLen).toString('utf8'));
}
for (const p of ['razarion.glb', 'razarion_post.glb']) {
  const j = readJson(p);
  const raz = (j.nodes || []).filter(n => (n.name || '').startsWith('RAZ_'));
  let total = 0, withUV = 0;
  for (const m of j.meshes || [])
    for (const prim of m.primitives || []) {
      total++;
      if (prim.attributes?.TEXCOORD_0 !== undefined) withUV++;
    }
  console.log(p, 'nodes:', j.nodes.length, 'RAZ_:', raz.length,
              'prims:', total, 'withUV:', withUV);
}
```

Both files must report identical `RAZ_:` and `withUV:` counts.

### Step 2: Upload via GLTF Editor

1. Open the admin editor at `/editor`
2. Navigate to the **GLTF** editor
3. Create a new GltfEntity or select an existing one
4. Use the file upload to import the `.glb` file

The editor will:
- Parse the GLB using Babylon.js `SceneLoader`
- Detect all meshes and create **Model3DRow** entries (states: `NEW`, `EXIST`, `DELETE`, `UNUSED`)
- Detect all materials and create **MaterialRow** entries

### Step 3: Configure Materials

For each material detected in the GLB file, assign a **BabylonMaterialEntity**:

1. In the GLTF editor, each material row shows the glTF material name
2. Select the corresponding Babylon material from the dropdown
3. If no suitable material exists, create one in the **Babylon Material** editor first

**Material types:**
- **Standard materials**: PBR material JSON
- **NodeMaterial**: Visual shader graph (supports diplomacy colors, texture overrides)

### Step 4: Save

On save, the editor:
1. Uploads the GLB binary via `PUT /rest/gltf/upload-glb/{id}`
2. Creates a `Model3DEntity` for each new mesh detected
3. Saves material mappings to the `GltfEntity`

### Step 5: Assign to Game Config

Reference the `Model3DEntity.id` in game configurations:

- **BaseItemType** (units/buildings): Set `model3DId` in the BaseItemType editor
- **TerrainObjectConfig** (trees, rocks, decorations): Set `model3DId` in the TerrainObject editor
- **ResourceItemType** (resource nodes): Set `model3DId` in the ResourceItemType editor

## Runtime Loading

### Startup

1. `UiConfigCollectionService` fetches all metadata via `GET /rest/ui-config-collection/get`
2. This returns all `GltfEntity`, `Model3DEntity`, `BabylonMaterialEntity` records (metadata only, not binary data)

### Lazy Loading (babylon-model-container.ts)

Three containers handle lazy loading of binary assets:

| Container | Loads via | Caches |
|-----------|----------|--------|
| `GlbContainer` | `GET /rest/gltf/glb/{id}` | `Map<gltfId, AssetContainer>` |
| `BabylonMaterialContainer` | `GET /rest/babylon-material/data/{id}` | `Map<materialId, Material>` |
| `ParticleSystemSetContainer` | `GET /rest/particle-system/data/{id}` | `Map<particleId, NodeParticleSystemSet>` |

### Rendering (BabylonModelService.cloneModel3D)

When a game item needs to be rendered:

```
1. Look up Model3DEntity by model3DId
2. Get AssetContainer from GlbContainer (by gltfEntityId)
3. Find the node in AssetContainer by gltfName
4. Deep-clone the node hierarchy
5. Apply Babylon materials via GltfHelper:
   - Map each mesh's glTF material -> BabylonMaterialEntity
   - Clone the material
   - Apply diplomacy colors (faction-specific)
   - Apply texture overrides from GLB
6. Attach animations (filtered by mesh name prefix)
7. Instantiate particle systems (RAZ_P_ / RAZ_M_P_ meshes)
8. Return RenderObject with node, animations, and effects
```

## REST API Reference

### GLTF

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/rest/gltf/` | List all GltfEntities | ADMIN |
| `GET` | `/rest/gltf/{id}` | Get GltfEntity metadata | ADMIN |
| `POST` | `/rest/gltf/create` | Create new GltfEntity | ADMIN |
| `PUT` | `/rest/gltf/update` | Update GltfEntity metadata | ADMIN |
| `DELETE` | `/rest/gltf/delete/{id}` | Delete GltfEntity | ADMIN |
| `PUT` | `/rest/gltf/upload-glb/{id}` | Upload GLB binary | ADMIN |
| `GET` | `/rest/gltf/glb/{id}` | Download GLB binary | PUBLIC |

### Model3D

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/editor/model-3d/` | List all Model3DEntities | ADMIN |
| `GET` | `/editor/model-3d/{id}` | Get Model3DEntity | ADMIN |
| `GET` | `/editor/model-3d/getModel3DsByGltf/{gltfId}` | Get Model3Ds for a GltfEntity | ADMIN |

### Babylon Material

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/rest/babylon-material/` | List all materials | ADMIN |
| `GET` | `/rest/babylon-material/data/{id}` | Download material data | PUBLIC |
| `POST` | `/rest/babylon-material/upload/{id}` | Upload material data | ADMIN |
| `GET` | `/rest/babylon-material/sizes` | Get material storage sizes | ADMIN |

## Key Source Files

**Server:**
- `razarion-server/.../model/ui/GltfEntity.java` - GLB storage entity
- `razarion-server/.../model/ui/Model3DEntity.java` - Mesh reference entity
- `razarion-server/.../model/ui/BabylonMaterialEntity.java` - Material storage
- `razarion-server/.../service/ui/GltfService.java` - GLB service layer
- `razarion-server/.../rest/ui/GltfController.java` - REST endpoints

**Frontend Editor:**
- `razarion-frontend/src/app/editor/crud-editors/gltf-editor/gltf-editor.component.ts` - Upload UI

**Frontend Runtime:**
- `razarion-frontend/src/app/game/renderer/babylon-model.service.ts` - Model cloning & animation
- `razarion-frontend/src/app/game/renderer/babylon-model-container.ts` - Lazy loading containers
- `razarion-frontend/src/app/game/renderer/gltf-helper.ts` - Material assignment
