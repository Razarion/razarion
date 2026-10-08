import {MaterialPluginBase} from "@babylonjs/core/Materials/materialPluginBase";
import {Material} from "@babylonjs/core/Materials/material";
import {UniformBuffer} from "@babylonjs/core/Materials/uniformBuffer";
import type {Scene} from "@babylonjs/core/scene";

/**
 * Plants sway in the wind: the vertex shader moves each vertex sideways by its height above the
 * model's own origin, so the stem base stays put and the crown moves most. A slow wave of gusts
 * runs across the field, and every plant has its own phase from where it stands.
 *
 * Only the colour pass sways - the shadow pass has its own shader and keeps the plant still. At a
 * few centimetres per metre of height the difference does not show.
 *
 * Instanced meshes: finalWorld is the instance's matrix, so its translation is the plant's own
 * origin and every instance gets its own phase without any per-instance data.
 */
export class WindPlugin extends MaterialPluginBase {
  /** 1 = normal sway, 0 = still (Showcase switches it). */
  static strength = 1;
  private static time = 0;
  private static clockScene: Scene | null = null;

  constructor(material: Material) {
    super(material, "Wind", 200, {WIND: false});
    this._enable(true);
    WindPlugin.ensureClock(material.getScene());
  }

  /** One clock for all plants, so neighbours sway together and a gust is one wave. */
  private static ensureClock(scene: Scene): void {
    if (WindPlugin.clockScene === scene) {
      return;
    }
    WindPlugin.clockScene = scene;
    scene.onBeforeRenderObservable.add(() => WindPlugin.time = performance.now() / 1000);
  }

  override prepareDefines(defines: any): void {
    defines["WIND"] = true;
  }

  override getUniforms() {
    return {
      ubo: [{name: "windTime", size: 1, type: "float"}, {name: "windStrength", size: 1, type: "float"}],
      vertex: `#ifdef WIND
        uniform float windTime;
        uniform float windStrength;
      #endif`,
    };
  }

  override bindForSubMesh(uniformBuffer: UniformBuffer): void {
    uniformBuffer.updateFloat("windTime", WindPlugin.time);
    uniformBuffer.updateFloat("windStrength", WindPlugin.strength);
  }

  override getClassName(): string {
    return "WindPlugin";
  }

  override getCustomCode(shaderType: string): { [pointName: string]: string } | null {
    if (shaderType !== "vertex") {
      return null;
    }
    return {
      CUSTOM_VERTEX_UPDATE_WORLDPOS: `
        #ifdef WIND
          vec3 windOrigin = finalWorld[3].xyz;
          float windHeight = max(worldPos.y - windOrigin.y, 0.0);
          float windPhase = dot(windOrigin.xz, vec2(0.13, 0.09));
          float windGust = 0.55 + 0.45 * sin(dot(windOrigin.xz, vec2(0.021, 0.013)) - windTime * 0.6);
          float windSway = sin(windTime * 1.3 + windPhase) * 0.7 + sin(windTime * 2.9 + windPhase * 1.7) * 0.3;
          worldPos.xz += vec2(0.8, 0.6) * (windSway * windGust * windHeight * 0.08 * windStrength);
        #endif
      `,
    };
  }
}
