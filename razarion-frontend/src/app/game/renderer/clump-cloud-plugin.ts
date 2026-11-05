import {MaterialPluginBase} from "@babylonjs/core/Materials/materialPluginBase";
import {Material} from "@babylonjs/core/Materials/material";
import {UniformBuffer} from "@babylonjs/core/Materials/uniformBuffer";
import {CLOUD, cloudTime} from "./cloud-shadow";

/** Share of a lit surface's brightness that is sun, as for the asphalt (cloud-shadow-node.ts, SUN_SHARE). */
const SUN_SHARE = 0.8;

/**
 * The drifting cloud shadows on the grass clumps (grass-clumps.ts, earth-clumps.ts). Without them a
 * clump kept full sun under a cloud while the lawn around it went dark: in the sun the clumps blended
 * into the lawn, in a cloud's shadow they lit up as bright specks.
 * The same two octaves of noise as the ground's cloudShadeNode and the sprites' cloudSunShade, worked
 * out once per clump at its foot in the vertex shader - a clump is far smaller than a cloud's edge.
 */
export class ClumpCloudPlugin extends MaterialPluginBase {
  constructor(material: Material) {
    super(material, "ClumpCloud", 220, {CLUMPCLOUD: false});
    this._enable(true);
  }

  override prepareDefines(defines: any): void {
    defines["CLUMPCLOUD"] = true;
  }

  override getUniforms() {
    return {
      ubo: [{name: "clumpCloud", size: 4, type: "vec4"}],
      vertex: `#ifdef CLUMPCLOUD
        uniform vec4 clumpCloud;
      #endif`,
    };
  }

  override bindForSubMesh(uniformBuffer: UniformBuffer): void {
    // time, strength (showcase.ts sets it to 0 with the clouds off), sun share
    uniformBuffer.updateFloat4("clumpCloud", cloudTime(), CLOUD.SHADOW_STRENGTH, SUN_SHARE, 0);
  }

  override getClassName(): string {
    return "ClumpCloudPlugin";
  }

  override getCustomCode(shaderType: string): { [pointName: string]: string } | null {
    if (shaderType === "vertex") {
      return {
        CUSTOM_VERTEX_DEFINITIONS: `
          #ifdef CLUMPCLOUD
            varying float vClumpCloud;
            ${SIMPLEX_GLSL}
          #endif
        `,
        CUSTOM_VERTEX_UPDATE_WORLDPOS: `
          #ifdef CLUMPCLOUD
          {
            vec2 cloudFoot = finalWorld[3].xz;
            float cloudDrift = clumpCloud.x * ${f(CLOUD.SPEED)};
            vec3 cloudSeed = vec3(cloudFoot * ${f(CLOUD.SCALE)} + cloudDrift, clumpCloud.x * ${f(CLOUD.EVOLVE_SPEED)});
            float cloudNoise = clumpSimplex(cloudSeed) + ${f(CLOUD.FINE_WEIGHT)} * clumpSimplex(cloudSeed * ${f(CLOUD.FINE_SCALE)});
            float cloudShade = 1.0 - smoothstep(${f(CLOUD.EDGE0)}, ${f(CLOUD.EDGE1)}, cloudNoise) * clumpCloud.y;
            vClumpCloud = mix(1.0, cloudShade, clumpCloud.z);
          }
          #endif
        `,
      };
    }
    if (shaderType === "fragment") {
      return {
        CUSTOM_FRAGMENT_DEFINITIONS: `
          #ifdef CLUMPCLOUD
            varying float vClumpCloud;
          #endif
        `,
        CUSTOM_FRAGMENT_BEFORE_FRAGCOLOR: `
          #ifdef CLUMPCLOUD
            finalColor.rgb *= vClumpCloud;
          #endif
        `,
      };
    }
    return null;
  }
}

function f(value: number): string {
  return Number.isInteger(value) ? `${value}.0` : `${value}`;
}

// Babylon's SimplexPerlin3DBlock, as GLSL (the node material emits the same function), so the clumps
// see the same clouds as the ground.
const SIMPLEX_GLSL = `
float clumpSimplex(vec3 source) {
  const float SKEWFACTOR = 1.0 / 3.0;
  const float UNSKEWFACTOR = 1.0 / 6.0;
  const float SIMPLEX_CORNER_POS = 0.5;
  const float SIMPLEX_TETRAHADRON_HEIGHT = 0.70710678118654752440084436210485;
  vec3 P = source;
  P.x = (P.x == 0. && P.y == 0. && P.z == 0.) ? 0.00001 : P.x;
  P *= SIMPLEX_TETRAHADRON_HEIGHT;
  vec3 Pi = floor(P + dot(P, vec3(SKEWFACTOR)));
  vec3 x0 = P - Pi + dot(Pi, vec3(UNSKEWFACTOR));
  vec3 g = step(x0.yzx, x0.xyz);
  vec3 l = 1.0 - g;
  vec3 Pi_1 = min(g.xyz, l.zxy);
  vec3 Pi_2 = max(g.xyz, l.zxy);
  vec3 x1 = x0 - Pi_1 + UNSKEWFACTOR;
  vec3 x2 = x0 - Pi_2 + SKEWFACTOR;
  vec3 x3 = x0 - SIMPLEX_CORNER_POS;
  vec4 v1234_x = vec4(x0.x, x1.x, x2.x, x3.x);
  vec4 v1234_y = vec4(x0.y, x1.y, x2.y, x3.y);
  vec4 v1234_z = vec4(x0.z, x1.z, x2.z, x3.z);
  Pi = Pi.xyz - floor(Pi.xyz * (1.0 / 69.0)) * 69.0;
  vec3 Pi_inc1 = step(Pi, vec3(69.0 - 1.5)) * (Pi + 1.0);
  vec4 Pt = vec4(Pi.xy, Pi_inc1.xy) + vec2(50.0, 161.0).xyxy;
  Pt *= Pt;
  vec4 V1xy_V2xy = mix(Pt.xyxy, Pt.zwzw, vec4(Pi_1.xy, Pi_2.xy));
  Pt = vec4(Pt.x, V1xy_V2xy.xz, Pt.z) * vec4(Pt.y, V1xy_V2xy.yw, Pt.w);
  const vec3 SOMELARGEFLOATS = vec3(635.298681, 682.357502, 668.926525);
  const vec3 ZINC = vec3(48.500388, 65.294118, 63.934599);
  vec3 lowz_mods = vec3(1.0 / (SOMELARGEFLOATS.xyz + Pi.zzz * ZINC.xyz));
  vec3 highz_mods = vec3(1.0 / (SOMELARGEFLOATS.xyz + Pi_inc1.zzz * ZINC.xyz));
  Pi_1 = (Pi_1.z < 0.5) ? lowz_mods : highz_mods;
  Pi_2 = (Pi_2.z < 0.5) ? lowz_mods : highz_mods;
  vec4 hash_0 = fract(Pt * vec4(lowz_mods.x, Pi_1.x, Pi_2.x, highz_mods.x)) - 0.49999;
  vec4 hash_1 = fract(Pt * vec4(lowz_mods.y, Pi_1.y, Pi_2.y, highz_mods.y)) - 0.49999;
  vec4 hash_2 = fract(Pt * vec4(lowz_mods.z, Pi_1.z, Pi_2.z, highz_mods.z)) - 0.49999;
  vec4 grad_results = inversesqrt(hash_0 * hash_0 + hash_1 * hash_1 + hash_2 * hash_2) * (hash_0 * v1234_x + hash_1 * v1234_y + hash_2 * v1234_z);
  const float FINAL_NORMALIZATION = 37.837227241611314102871574478976;
  vec4 kernel_weights = v1234_x * v1234_x + v1234_y * v1234_y + v1234_z * v1234_z;
  kernel_weights = max(0.5 - kernel_weights, vec4(0.));
  kernel_weights = kernel_weights * kernel_weights * kernel_weights;
  return dot(kernel_weights, grad_results) * FINAL_NORMALIZATION;
}
`;
