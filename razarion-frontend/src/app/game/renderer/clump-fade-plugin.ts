import {MaterialPluginBase} from "@babylonjs/core/Materials/materialPluginBase";
import {Material} from "@babylonjs/core/Materials/material";
import {UniformBuffer} from "@babylonjs/core/Materials/uniformBuffer";
import type {Scene} from "@babylonjs/core/scene";

/** The thin instance attribute (one float per clump): the clump's fade draw, 0 - 1 (ClumpField). */
export const CLUMP_FADE = "clumpFade";

/**
 * The clumps' camera fade (grass-clumps.ts) in the vertex shader: all nearby, fewer from fadeNear,
 * one in ten from fadeFar. A clump whose draw is above what its distance keeps shrinks into its
 * foot - over a small band of draws, so zooming lets the clumps grow and sink instead of popping.
 * Worked out on the CPU, every zoom step had to copy the lists together anew (GroundClumps).
 */
export class ClumpFadePlugin extends MaterialPluginBase {
  constructor(material: Material, private readonly range: () => { fadeNear: number, fadeFar: number }) {
    super(material, "ClumpFade", 210, {CLUMPFADE: false});
    this._enable(true);
  }

  override prepareDefines(defines: any): void {
    defines["CLUMPFADE"] = true;
  }

  override getAttributes(attributes: string[]): void {
    attributes.push(CLUMP_FADE);
  }

  override getUniforms() {
    return {
      ubo: [{name: "clumpCamera", size: 3, type: "vec3"}, {name: "clumpRange", size: 2, type: "vec2"}],
      vertex: `#ifdef CLUMPFADE
        uniform vec3 clumpCamera;
        uniform vec2 clumpRange;
      #endif`,
    };
  }

  override bindForSubMesh(uniformBuffer: UniformBuffer, scene: Scene): void {
    const camera = scene.activeCamera?.globalPosition;
    // The ground a little below the camera's height over the island's base (GrassClumps.nearestKeep)
    uniformBuffer.updateFloat3("clumpCamera", camera?.x ?? 0, Math.max(0, (camera?.y ?? 0) - 2), camera?.z ?? 0);
    const {fadeNear, fadeFar} = this.range();
    if (fadeFar > 0) {
      uniformBuffer.updateFloat2("clumpRange", fadeNear, Math.max(fadeNear + 1, fadeFar));
    } else {
      // No thinning
      uniformBuffer.updateFloat2("clumpRange", 1e9, 1e9 + 1);
    }
  }

  override getClassName(): string {
    return "ClumpFadePlugin";
  }

  override getCustomCode(shaderType: string): { [pointName: string]: string } | null {
    if (shaderType !== "vertex") {
      return null;
    }
    return {
      CUSTOM_VERTEX_DEFINITIONS: `
        #ifdef CLUMPFADE
          attribute float ${CLUMP_FADE};
        #endif
      `,
      CUSTOM_VERTEX_UPDATE_WORLDPOS: `
        #ifdef CLUMPFADE
          vec3 clumpFoot = finalWorld[3].xyz;
          float clumpDistance = length(vec3(clumpFoot.x - clumpCamera.x, clumpCamera.y, clumpFoot.z - clumpCamera.z));
          float clumpKeep = 1.0 - 0.9 * smoothstep(clumpRange.x, clumpRange.y, clumpDistance);
          worldPos.xyz = mix(clumpFoot, worldPos.xyz, clamp((clumpKeep - ${CLUMP_FADE}) * 20.0, 0.0, 1.0));
        #endif
      `,
    };
  }
}
