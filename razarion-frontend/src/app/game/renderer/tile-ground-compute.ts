import {computeShoreDistance, detectShoreline} from "./shoreline-detection";
import {createPathTileMask, GroundPath} from "./ground-paths";
import {createVegetationMask} from "./vegetation-mask";
import {VertexData} from "@babylonjs/core/Meshes/mesh.vertexData";

/**
 * The arithmetic of a terrain tile's ground that needs neither the scene nor the engine bridge: the
 * ground mesh's vertex data, the shore distance (UV2), the path and vegetation masks and the texels of
 * the ground type texture.
 * Runs in terrain-helper.worker.ts, and where there is no worker inline (terrain-helper.ts), so it
 * must not import anything that touches the DOM, Babylon or the WASM bridge.
 */

// See: GWT Java Code TerrainUtil / TerrainAnalyzer
export const WATER_LEVEL = 0;
export const WALL_HEIGHT_DIFF = 0.5;

export enum TerrainKind {
  LAND,
  WATER,
  BLOCKED,
}

/**
 * Canonical terrain-type classification. Mirrors the authoritative game-engine rule in
 * TerrainAnalyzer.analyze() (razarion-share): avg of the 4 quad corners < WATER_LEVEL => WATER;
 * corner height delta < WALL_HEIGHT_DIFF (0.5 m over a 1 m node) => LAND (passable, grass);
 * else => BLOCKED (cliff, rendered as rock). Single source of truth for the renderer, the ground
 * type texture and the editor terrain-type overlay (via BabylonTerrainTileImpl.setupTerrainType).
 */
export function terrainKind(bLHeight: number, bRHeight: number, tRHeight: number, tLHeight: number): TerrainKind {
  const avgHeight = (bLHeight + bRHeight + tRHeight + tLHeight) / 4.0;
  if (avgHeight < WATER_LEVEL) {
    return TerrainKind.WATER;
  }
  const maxHeight = Math.max(bLHeight, bRHeight, tRHeight, tLHeight);
  const minHeight = Math.min(bLHeight, bRHeight, tRHeight, tLHeight);
  if (Math.abs(maxHeight - minHeight) < WALL_HEIGHT_DIFF) {
    return TerrainKind.LAND;
  } else {
    return TerrainKind.BLOCKED;
  }
}

/**
 * One texel per metre cell, RGBA, row 0 first. Red: blocked (the shader shows rock). Green: path
 * strength from GroundPaths (trodden earth), or 0 where there is no path. Blue: growth under plants
 * (vegetation-mask.ts). Alpha 255.
 * @param heights the node heights, (size + 1) x (size + 1), row-major [y * (size + 1) + x]
 */
export function groundTypePixels(heights: ArrayLike<number>, size: number,
                                 pathMask: Float32Array | null, vegetationMask: Float32Array | null): Uint8Array {
  const nodes = size + 1;
  const data = new Uint8Array(size * size * 4);
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      const bottom = y * nodes + x, top = bottom + nodes;
      const blocked = terrainKind(heights[bottom], heights[bottom + 1], heights[top + 1], heights[top]) === TerrainKind.BLOCKED;
      const i = (y * size + x) * 4;
      data[i] = blocked ? 255 : 0;
      data[i + 1] = pathMask ? Math.round(pathMask[y * size + x] * 255) : 0;
      data[i + 2] = vegetationMask ? Math.round(vegetationMask[y * size + x] * 255) : 0;
      data[i + 3] = 255;
    }
  }
  return data;
}

/** The sub mesh material indices of the ground mesh (BabylonTerrainTileImpl's MaterialIndex). */
export const GROUND_MATERIAL = 0;
export const ASPHALT_MATERIAL = 1;

/**
 * The gradient direction of the height in uv2's x, written into uv2's y (atan2 of the central
 * differences, 0 where flat) - the way the water finds the shore.
 */
export function computeShoreDirections(uv2: number[], xCount: number, yCount: number): void {
  for (let y = 0; y < yCount; y++) {
    for (let x = 0; x < xCount; x++) {
      const idx = (y * xCount + x) * 2;
      const h = uv2[idx]; // ground height at this vertex

      // Central differences for gradient (clamped at edges)
      const xL = x > 0 ? uv2[((y * xCount) + (x - 1)) * 2] : h;
      const xR = x < xCount - 1 ? uv2[((y * xCount) + (x + 1)) * 2] : h;
      const yD = y > 0 ? uv2[(((y - 1) * xCount) + x) * 2] : h;
      const yU = y < yCount - 1 ? uv2[(((y + 1) * xCount) + x) * 2] : h;

      const dx = xR - xL;
      const dy = yU - yD;

      // atan2 gives angle of gradient (direction toward shore)
      // Default to 0 on flat areas (no gradient)
      uv2[idx + 1] = (dx === 0 && dy === 0) ? 0 : Math.atan2(dy, dx);
    }
  }
}

export interface TileMesh {
  positions: Float32Array;
  normals: Float32Array;
  uvs: Float32Array;
  /** Ground triangles first, asphalt after - groundIndexCount and asphaltIndexCount of them */
  indices: Uint32Array;
  groundIndexCount: number;
  asphaltIndexCount: number;
  /** The material of every quad, row-major (GROUND_MATERIAL / ASPHALT_MATERIAL) */
  quadMaterials: Uint8Array;
  /** For the water: the height in x, the shore direction in y, rows upside down */
  waterUv2: Float32Array;
}

/**
 * The ground mesh of a tile. Quads with a decal on land or rock are asphalt, the rest ground; the
 * triangles go out grouped by material, so the mesh needs two sub meshes and not one per run of
 * quads - see BabylonTerrainTileImpl. Worked out in plain arrays, as it was on the main thread, and
 * handed over as the 32-bit floats the GPU takes anyway.
 * @param decalBoxes the decals as [x0, y0, x1, y1] world rectangles
 */
export function computeTileMesh(heights: ArrayLike<number>, size: number, x0: number, y0: number,
                                decalBoxes: ArrayLike<number>): TileMesh {
  const nodes = size + 1;
  const positions: number[] = [];
  const normals: number[] = [];
  const uvs: number[] = [];
  const waterUv2: number[] = [];
  for (let y = 0; y < nodes; y++) {
    for (let x = 0; x < nodes; x++) {
      positions.push(x + x0, heights[x + y * nodes], y + y0);
      normals.push(0, 0, 0);
      uvs.push(x / (nodes - 1), 1.0 - y / (nodes - 1));
      waterUv2.push(heights[x + (nodes - y - 1) * nodes], 0);
    }
  }
  computeShoreDirections(waterUv2, nodes, nodes);

  const groundIndices: number[] = [];
  const asphaltIndices: number[] = [];
  const quadMaterials = new Uint8Array(size * size);
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      const bL = x + y * nodes, bR = bL + 1, tL = bL + nodes, tR = tL + 1;
      const kind = terrainKind(heights[bL], heights[bR], heights[tR], heights[tL]);
      const asphalt = kind !== TerrainKind.WATER && insideBox(decalBoxes, x + x0, y + y0);
      quadMaterials[x + y * size] = asphalt ? ASPHALT_MATERIAL : GROUND_MATERIAL;
      (asphalt ? asphaltIndices : groundIndices).push(bL, bR, tL, bR, tR, tL);
    }
  }
  const indices = groundIndices.concat(asphaltIndices);
  VertexData.ComputeNormals(positions, indices, normals);
  return {
    positions: new Float32Array(positions),
    normals: new Float32Array(normals),
    uvs: new Float32Array(uvs),
    indices: new Uint32Array(indices),
    groundIndexCount: groundIndices.length,
    asphaltIndexCount: asphaltIndices.length,
    quadMaterials,
    waterUv2: new Float32Array(waterUv2),
  };
}

function insideBox(boxes: ArrayLike<number>, x: number, y: number): boolean {
  for (let i = 0; i < boxes.length; i += 4) {
    if (x >= boxes[i] && x < boxes[i + 2] && y >= boxes[i + 1] && y < boxes[i + 3]) {
      return true;
    }
  }
  return false;
}

export interface TileGroundInput {
  /** Node heights, (size + 1) x (size + 1), row-major */
  heights: Float64Array;
  /** Tile size in metre cells (NODE_X_COUNT) */
  size: number;
  /** World position of the tile's first cell */
  x0: number;
  y0: number;
  /** Plants and rocks of the tile as [x, z, radius, isPlant] quadruples */
  anchors: Float64Array;
  /** The decals as [x0, y0, x1, y1] - only with withMesh */
  decalBoxes?: Float64Array;
  /** Work out the ground mesh too (the editor builds it right away instead) */
  withMesh?: boolean;
}

export interface TileGroundOutput {
  /** Shore distance per node, [signedDist, arcLength] - see computeShoreDistance */
  uv2: Float32Array;
  pathMask: Float32Array | null;
  vegetationMask: Float32Array | null;
  /** The texels of the ground type texture - see groundTypePixels */
  pixels: Uint8Array;
  mesh?: TileMesh;
}

export function computeTileGround(input: TileGroundInput, paths: GroundPath[]): TileGroundOutput {
  const nodes = input.size + 1;
  const heights = input.heights;
  const uv2 = new Float32Array(computeShoreDistance(detectShoreline(heights, nodes, nodes), heights, nodes, nodes));
  const pathMask = createPathTileMask(paths, input.x0, input.y0, input.size);
  const vegetationMask = createVegetationMask(input.x0, input.y0, input.size, input.anchors);
  const pixels = groundTypePixels(input.heights, input.size, pathMask, vegetationMask);
  const mesh = input.withMesh ? computeTileMesh(input.heights, input.size, input.x0, input.y0, input.decalBoxes ?? []) : undefined;
  return {uv2, pathMask, vegetationMask, pixels, mesh};
}

/** The buffers of an output, to hand over to postMessage as transferables. */
export function transferablesOf(output: TileGroundOutput): ArrayBuffer[] {
  const buffers = [output.uv2.buffer, output.pixels.buffer];
  if (output.pathMask) {
    buffers.push(output.pathMask.buffer);
  }
  if (output.vegetationMask) {
    buffers.push(output.vegetationMask.buffer);
  }
  if (output.mesh) {
    const m = output.mesh;
    buffers.push(m.positions.buffer, m.normals.buffer, m.uvs.buffer, m.indices.buffer, m.quadMaterials.buffer, m.waterUv2.buffer);
  }
  return buffers as ArrayBuffer[];
}
