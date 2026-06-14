# Terrain Editor Brushes — Analysis & Redesign Plan

This document analyzes why the current height-map editor brushes produce poor terrain
shapes (especially **large mountains**) and proposes a shape- and validity-aware redesign.
It is a planning/reference document — no code has been changed yet.

Companion reference: [terrain-system.md](terrain-system.md) (heightmap format, classification, REST).

## 1. The Ground Truth (constraints every brush must respect)

Brushes only push vertex heights. The **game engine** and **render engine** then derive
passability and texturing from those heights using fixed thresholds. A brush that ignores
these thresholds produces terrain that is technically editable but looks wrong or plays wrong.

| Quantity | Value | Source | Meaning |
|---|---|---|---|
| `NODE_SIZE` | 1 m | `TerrainUtil.java`, `babylon-terrain-tile.impl.ts:62` | Quad grid resolution |
| `WALL_HEIGHT_DIFF` | **0.5 m** | `TerrainUtil.java:20`, `TerrainAnalyzer.java:105` | Corner delta ≥ 0.5 m over a 1 m node ⇒ `BLOCKED` |
| `WATER_LEVEL` | 0 m | engine + renderer | `avg` of 4 quad corners < 0 ⇒ `WATER` |
| `BEACH_HEIGHT` | 0.3 m | `babylon-terrain-tile.impl.ts:67` | Sand/beach band |
| Grass threshold | ≈ 0.3–0.4 m | `ground-material.ts` `beachStep` 0.23→0.30 (+ `height·1.2`) | Above ⇒ grass, below ⇒ sand |
| Underwater shading | y < 0 (gradient to −10 m) | `ground-material.ts` `underwaterStep` / gradient | Depth-based color |
| `HEIGHT_PRECISION` | 0.01 m | `babylon-terrain-tile.impl.ts:64` | Stored quantization (fine; no longer a limiter) |

### 1.1 The key coupling: rock ⟺ BLOCKED ⟺ slope ≥ 0.5 m/m

`GroundUtil.createGroundTypeTexture()` (`ground-util.ts:26-35`) writes the mountain/rock
channel (`GroundUtility.r`, blended in the shader via `mountainBlend`) **exactly where a node
is `BLOCKED`**. `BLOCKED` is defined in `TerrainAnalyzer.analyze()` as corner height delta
≥ `WALL_HEIGHT_DIFF` (0.5 m) over a 1 m node. Therefore:

> **A node renders as rock/mountain ⟺ it is `BLOCKED` ⟺ its 4-corner height delta ≥ 0.5 m/m.**

Consequences:
- A gentle hill (< 0.5 m/m) stays **grass** — it reads as a bump, not a mountain.
- A flank far above 0.5 m/m becomes an ugly near-vertical **1-node step** (triplanar stretch).
- The "good rock" band starts at 0.5 m/m and looks best as a *consistent* slope so a flank
  textures uniformly instead of as scattered rock patches.

## 2. Why large mountains are hard (the core problem)

Because rock requires ≥ 0.5 m/m, **peak height, footprint, and steepness are coupled**.
To raise a peak to height `H` above the surrounding ground, the flank must span:

`flankWidth = H / slope`

| Peak `H` | flank @ 0.5 m/m (gentlest rock, ~27°) | @ 1.0 m/m (~45°) | @ 2.0 m/m (~63°, wall-like) |
|---|---|---|---|
| 10 m | 20 m → ~40 m ⌀ | 10 m → ~20 m ⌀ | 5 m → ~10 m ⌀ |
| 25 m | 50 m → ~100 m ⌀ | 25 m → ~50 m ⌀ | 12.5 m → ~25 m ⌀ |
| 40 m | 80 m → ~160 m ⌀ | 40 m → ~80 m ⌀ | 20 m → ~40 m ⌀ |

A tall, good-looking mountain **needs a large footprint**. Raising height without widening the
footprint forces a wall-like flank (ugly). Flattening the flank enough to look gentle drops it
below 0.5 m/m → grass, so it stops reading as a mountain. This trade-off is the difficulty the
user hits when "making a mountain bigger."

### 2.1 Why today's brushes fail at this

- **Raise** (`raise-height-brush.component.ts`): adds an increment per pointer-move with a
  linear falloff ring. Building a tall mountain means dragging repeatedly → uneven, dwell-
  dependent buildup, terracing. No awareness of the 0.5 m/m threshold.
- **Fix height** (`fix-height-brush.component.ts`): produces a **flat-topped mesa with a linear
  ramp** (cone frustum). It *can* be made rock-steep by hand-computing `maxSlopeWidth ≈ H/0.5`,
  but the result is a monotone geometric cone with a flat top — artificial at large scale.
- Neither brush has a **natural mountain profile** (gentle foot → steep mid → rounded top) or
  **ridge variation**. At large scale, a featureless cone always looks bad regardless of slope.
- **Tile seams** (per-node `Math.random()`, per-tile normals) are most visible on large forms.

## 3. Chosen approach — layered ("stacked") mountain model

Decision: build large mountains from **layered platforms** — a wide, lower socket with one or
more narrower, steeper rock caps on top. This is the most natural result for big mountains and
directly addresses the height/footprint trade-off by distributing height across layers.

### 3.1 Single layer

A layer is a radial "plateau + rock skirt" centered on the stamp:

```
layer_i(r) =
  baseHeight_i + height_i                                if r ≤ plateauRadius_i
  baseHeight_i + height_i · (1 − (r − plateauRadius_i)/skirtWidth_i)   (linear rock skirt)
                                                          if plateauRadius_i < r ≤ plateauRadius_i + skirtWidth_i
  baseHeight_i                                            otherwise
```

Each layer's skirt slope `= height_i / skirtWidth_i` is kept **≥ 0.5 m/m** (uniform rock).
The skirt is linear on purpose: a uniform slope textures as a clean, continuous rock face.

### 3.2 Composition by `max`

The mountain is `h(r) = max_i( layer_i(r) )` over a wide low layer and one or two narrow tall
layers. `max` naturally yields:
- the tall cap near the center (peak),
- a **shoulder/bench** where the cap's skirt drops below the socket plateau,
- the socket skirt down to ground.

Benches/terraces are exactly what reads as a *big* mountain rather than a cone.

### 3.3 Ridge variation (deterministic, seam-free)

Modulate each layer's `plateauRadius` (and optionally `skirtWidth`) by angle θ around the
center using **world-space** noise (reuse `perlin-noise.ts`), e.g.
`plateauRadius_i(θ) = plateauRadius_i · (1 + amp · noise(centerWorld, θ))`.
World-space (not per-tile) noise guarantees shared tile-edge vertices get identical values
(no seams) and gives spurs/ridges that break the cone silhouette.

### 3.4 Guidance / guard rails

- Apply the whole profile in **one stamp** (click), not by accumulation, so the shape is exact.
- Parameters in engine-meaningful terms: `peakHeight`, `footprintRadius`, per-layer slope
  (default ~0.8 m/m, min 0.5), cap count.
- If `footprintRadius` is too small for `peakHeight` (would force wall-like slope), **warn and
  suggest the minimum radius** (`≈ peakHeight / minSlope`). This removes the manual math that
  currently makes big mountains painful.
- Blend the foot smoothly into existing terrain (last skirt node → ground).

> Open item (deferred, verify during implementation): confirm whether the ground shader
> (triplanar diffuse/AO, `ground-material.ts`) degrades above ~30–50 m and whether a hard
> height cap is warranted, or whether ridge variation alone is sufficient.

## 4. Coast / water / beach (zone-aligned profiles)

The existing `coast-brush.component.ts` already builds an ideal profile via signed distance to
the waterline. Rework its target heights to align with the **render zone bands** so the result
looks right without manual tuning:
- Land plateau target ≥ `BEACH_HEIGHT + 0.1` (≈ 0.4 m) ⇒ guarantees grass above the beach.
- Beach ramp 0 → landHeight over `beachWidth`, slope kept < 0.5 m/m ⇒ passable `LAND` beach.
- Underwater reaches ≤ −0.5 m (so it reads as water), moderate slope.
- Set defaults so a single stamp lands in the good band.

## 5. Cross-cutting: validity mode (all brushes)

Optional post-pass after any brush, toggled in the editor header:
- **Free** (today's behavior),
- **Keep passable** — clamp corner deltas to < 0.5 m/m ⇒ everything stays `LAND`,
- **Force cliff** — push transitions to ≥ 0.5 m/m.

This generalizes the existing `fix-boundary-brush.component.ts` into a reusable constraint pass.

## 6. Correctness fixes (do first — otherwise the editor lies)

These threshold mismatches make the editor show "valid" where the game blocks (or vice versa):
- `editor-terrain-tile.ts:443` uses **0.7** instead of `WALL_HEIGHT_DIFF` (0.5). Align.
- `babylon-terrain-tile.impl.ts:81` (`setupTerrainType`) uses "all 4 corners ≤ 0 ⇒ WATER",
  but the engine (`TerrainAnalyzer.analyze:99`) uses "**avg** < 0". For editor feedback the
  **engine rule is authoritative**.
- Extract a single shared `classifyTerrainType(corners)` using the engine rule, used by the
  editor overlay and `GroundUtil`.

## 7. Live validity feedback

The editor holds all heights in memory, so it can compute the terrain-type overlay **locally**
from `positions` (engine rule, throttled per stroke) instead of waiting for the worker round-
trip. Painters then see rock(red)/land(green)/water(blue) update live as they edit — exactly
what the game and renderer will show. Worker ordinals remain available as the authoritative
check that also accounts for terrain-object (rock) blocking.

## 8. Companion fixes (quality of life)

- **Cursor shows the real brush profile**: `height-map-cursor.ts:89` is hard-wired to
  `FixHeightBrushComponent.staticCalculateHeight`. Make it render the *active* brush's profile
  (mountain cone, coast profile, …) and visibly distinguish round vs square.
- **Pointer-observer leak**: each brush adds `scene.onPointerObservable` in `initEditorCursor()`
  but `ngOnDestroy()` only disposes the mesh. Remove the observer on destroy.
- **Fix-height "jump on save"** (labeled red in the UI, `fix-height-brush.component.ts:80-81`).
- **Tile-edge normals**: recompute neighbor-tile edge normals after a stroke (or average edge
  normals) to remove lighting seams.

## 9. Implementation phases

| Phase | Scope | Key files |
|---|---|---|
| **A — Foundation/correctness** | Shared `classifyTerrainType` + `WALL_HEIGHT_DIFF`; fix 0.7→0.5; local live overlay | `editor-terrain-tile.ts`, `babylon-terrain-tile.impl.ts`, `ground-util.ts`, `height-map-terrain-editor.component.ts` |
| **B — Layered mountain brush** | New stamp brush (layered profile, ridge noise, guard rails); seam-free world-space noise; neighbor normals | new `brushes/mountain-brush.component.ts`, `height-map-terrain-editor.component.ts`, `editor-terrain-tile.ts` |
| **C — Coast rework + validity mode** | Zone-aligned coast targets; validity post-pass + header toggle | `coast-brush.component.ts`, `abstract-brush.ts`, `height-map-terrain-editor.component.ts` |
| **D — Companion fixes** | Cursor real profile, observer leak, save-jump | `height-map-cursor.ts`, all `brushes/*`, `fix-height-brush.component.ts` |

## 10. Source map (current brush system)

- Editor host: `razarion-frontend/src/app/editor/terrain-editor/height-map-terrain-editor.component.ts`
- Per-tile model + overlay: `.../terrain-editor/editor-terrain-tile.ts`
- Brush base + context: `.../terrain-editor/brushes/abstract-brush.ts`
- Brushes: `fix-height`, `flattem`, `raise-height`, `smooth`, `noise`, `erosion`, `coast`, `fix-boundary` `.component.ts`
- Cursor + noise util: `.../brushes/height-map-cursor.ts`, `.../brushes/perlin-noise.ts`
- Engine truth: `razarion-share/.../terrain/container/TerrainAnalyzer.java`, `.../terrain/TerrainUtil.java`
- Render truth: `.../renderer/babylon-terrain-tile.impl.ts`, `.../renderer/ground-material.ts`, `.../renderer/ground-util.ts`
