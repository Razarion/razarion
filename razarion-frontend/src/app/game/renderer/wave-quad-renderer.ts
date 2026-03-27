import {Mesh, VertexBuffer, VertexData} from "@babylonjs/core";
import type {Scene} from "@babylonjs/core/scene";
import type {WavePoint} from "./shoreline-detection";

/**
 * Creates a merged mesh of wave quads along the shoreline.
 * Each quad is a rectangle oriented along the shore tangent,
 * extending perpendicular toward water.
 *
 * @param wavePoints Evenly spaced shoreline points with tangent/normal
 * @param scene Babylon.js scene
 * @param quadLength Length of each quad along the shoreline (meters)
 * @param quadWidth Width of each quad perpendicular to shore (meters)
 * @returns A single merged mesh containing all wave quads
 */
export function createWaveQuadMesh(
  wavePoints: WavePoint[],
  scene: Scene,
  quadLength: number = 6,
  quadWidth: number = 4
): Mesh | null {
  if (wavePoints.length === 0) return null;

  const positions: number[] = [];
  const indices: number[] = [];
  const uvs: number[] = [];
  const uv2s: number[] = [];
  const normals: number[] = [];

  for (let i = 0; i < wavePoints.length; i++) {
    const wp = wavePoints[i];
    const baseIdx = i * 4;
    const halfLen = quadLength / 2;

    // Random phase per quad (deterministic from position)
    const phase = ((wp.x * 7.3 + wp.y * 13.7) % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);

    // 4 corners of the quad:
    // 0: shore-side left,  1: shore-side right
    // 2: water-side right, 3: water-side left
    const corners = [
      { u: 0, v: 0, tMul: -halfLen, nMul: 0 },         // shore left
      { u: 1, v: 0, tMul: halfLen, nMul: 0 },           // shore right
      { u: 1, v: 1, tMul: halfLen, nMul: quadWidth },   // water right
      { u: 0, v: 1, tMul: -halfLen, nMul: quadWidth },  // water left
    ];

    for (const c of corners) {
      const x = wp.x + wp.tangentX * c.tMul + wp.normalX * c.nMul;
      const z = wp.y + wp.tangentY * c.tMul + wp.normalY * c.nMul;
      positions.push(x, 0, z);  // y=0, positioned by parent
      normals.push(0, 1, 0);    // face up
      uvs.push(c.u, c.v);
      uv2s.push(phase, c.v);    // phase, perpendicular distance
    }

    // Two triangles: 0-1-2, 0-2-3
    indices.push(baseIdx, baseIdx + 1, baseIdx + 2);
    indices.push(baseIdx, baseIdx + 2, baseIdx + 3);
  }

  const mesh = new Mesh("WaveQuads", scene);
  const vertexData = new VertexData();
  vertexData.positions = positions;
  vertexData.indices = indices;
  vertexData.normals = normals;
  vertexData.uvs = uvs;
  vertexData.applyToMesh(mesh);
  mesh.setVerticesData(VertexBuffer.UV2Kind, uv2s);

  return mesh;
}
