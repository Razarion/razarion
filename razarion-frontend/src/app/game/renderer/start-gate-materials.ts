import {UiConfigCollection} from "src/app/generated/razarion-share";

/**
 * The materials the game start waits for: every one that no glb names.
 *
 * A first visit downloads 15 MB before the game is playable, and 9.5 MB of that used to be the
 * start gate - every material and every particle system, in full, while the glb models beside them
 * had streamed individually for months. The largest single item was a 4.8 MB vehicle material,
 * waited for by a player who has not placed a base yet and owns no vehicles. Measured by
 * STARTUP_PAYLOAD across five sessions on three devices, 2026-09-02.
 *
 * The rule rather than a list of ids, because content changes and a list would not: a material a
 * glb names is one that glb can wait for itself, and does. Everything else - the ground, the
 * water, the quest markers - is painted by code that has nowhere to wait and would show the red
 * "missing material" placeholder instead, permanently.
 *
 * On the content of 2026-09-02 this leaves 2.4 MB in the gate (asphalt, water, two quest markers)
 * and moves 5.5 MB out of it (vehicles, buildings).
 */
export function materialsForFirstFrame(uiConfigCollection: UiConfigCollection): number[] {
  const namedByGlb = new Set<number>();
  for (const gltf of uiConfigCollection.gltfs ?? []) {
    for (const materialId of Object.values(gltf.materialGltfNames ?? {})) {
      namedByGlb.add(materialId);
    }
  }
  return (uiConfigCollection.babylonMaterials ?? [])
    .map(material => material.id)
    .filter(id => !namedByGlb.has(id));
}
