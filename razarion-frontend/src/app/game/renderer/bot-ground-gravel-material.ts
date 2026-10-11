import {NodeMaterial} from "@babylonjs/core/Materials/Node/nodeMaterial";
import {RawTexture} from "@babylonjs/core/Materials/Textures/rawTexture";
import {Texture} from "@babylonjs/core/Materials/Textures/texture";
import {Color3} from "@babylonjs/core/Maths/math.color";
import {Vector2} from "@babylonjs/core/Maths/math.vector";
import {InputBlock} from "@babylonjs/core/Materials/Node/Blocks/Input/inputBlock";
import {NodeMaterialBlockConnectionPointTypes} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialBlockConnectionPointTypes";
import {NodeMaterialSystemValues} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialSystemValues";
import {TransformBlock} from "@babylonjs/core/Materials/Node/Blocks/transformBlock";
import {VertexOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Vertex/vertexOutputBlock";
import {DiscardBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/discardBlock";
import {FragmentOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/fragmentOutputBlock";
import {LightBlock} from "@babylonjs/core/Materials/Node/Blocks/Dual/lightBlock";
import {TextureBlock} from "@babylonjs/core/Materials/Node/Blocks/Dual/textureBlock";
import {AddBlock} from "@babylonjs/core/Materials/Node/Blocks/addBlock";
import {SubtractBlock} from "@babylonjs/core/Materials/Node/Blocks/subtractBlock";
import {MultiplyBlock} from "@babylonjs/core/Materials/Node/Blocks/multiplyBlock";
import {ScaleBlock} from "@babylonjs/core/Materials/Node/Blocks/scaleBlock";
import {OneMinusBlock} from "@babylonjs/core/Materials/Node/Blocks/oneMinusBlock";
import {VectorSplitterBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorSplitterBlock";
import {VectorMergerBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorMergerBlock";
import type {Scene} from "@babylonjs/core/scene";
import {cloudShadeNode, cloudTimeInput} from "./cloud-shadow-node";
import type {BotGroundMarkings} from "./bot-ground-markings";

// One repeat of the pebble texture (m) and its size in pixels
const PEBBLE_TILE = 2;
const PEBBLE_TEXTURE_SIZE = 512;
// Pebbles per row of the texture: about 10 cm apart - real gravel (3-5 cm) would not show from the
// game camera
const PEBBLE_CELLS = 20;

function floatInput(name: string, value: number): InputBlock {
  const b = new InputBlock(name, undefined, NodeMaterialBlockConnectionPointTypes.Float);
  b.value = value;
  return b;
}

let pebbleScene: Scene | null = null;
let pebbleTexture: Texture | null = null;

/**
 * A bed of rounded pebbles, light grey to beige, tiling: rgb the stone (darker in the gaps, rounded
 * by shading towards the stone's edge), alpha its height (0 in the gaps). Made once per scene - a
 * cellular pattern, the nearest of jittered points per cell with wrap-around, so it repeats seamlessly.
 */
function pebbles(scene: Scene): Texture {
  if (pebbleTexture && pebbleScene === scene) {
    return pebbleTexture;
  }
  const n = PEBBLE_CELLS, size = PEBBLE_TEXTURE_SIZE;
  let seed = 9173;
  const random = () => {
    seed = (seed * 16807) % 2147483647;
    return (seed - 1) / 2147483646;
  };
  const pointX = new Float32Array(n * n), pointY = new Float32Array(n * n), tone = new Float32Array(n * n * 3);
  const radius = new Float32Array(n * n);
  for (let k = 0; k < n * n; k++) {
    pointX[k] = (k % n + 0.15 + random() * 0.7) / n;
    pointY[k] = (Math.floor(k / n) + 0.15 + random() * 0.7) / n;
    // Grey to beige, a few darker and a few nearly white ones
    const light = 0.58 + random() * 0.3 - (random() < 0.12 ? 0.18 : 0);
    const warm = random() * 0.06;
    tone[k * 3] = light + warm;
    tone[k * 3 + 1] = light + warm * 0.6;
    tone[k * 3 + 2] = light - warm * 0.4;
    radius[k] = 0.6 + random() * 0.5;
  }
  const data = new Uint8Array(size * size * 4);
  for (let j = 0; j < size; j++) {
    for (let i = 0; i < size; i++) {
      const u = (i + 0.5) / size, v = (j + 0.5) / size;
      const ci = Math.floor(u * n), cj = Math.floor(v * n);
      // Nearest and second nearest point (F1, F2) in cell units, with wrap-around
      let f1 = Infinity, f2 = Infinity, nearest = 0;
      for (let dj = -1; dj <= 1; dj++) {
        for (let di = -1; di <= 1; di++) {
          const ni = (ci + di + n) % n, nj = (cj + dj + n) % n, k = nj * n + ni;
          let dx = pointX[k] - u, dy = pointY[k] - v;
          dx -= Math.round(dx);
          dy -= Math.round(dy);
          const d = Math.hypot(dx, dy) * n / radius[k];
          if (d < f1) {
            f2 = f1;
            f1 = d;
            nearest = k;
          } else if (d < f2) {
            f2 = d;
          }
        }
      }
      // Height: 1 in the middle of a stone, 0 at the gap to the next one
      const edge = Math.min(1, Math.max(0, (f2 - f1) / 0.35));
      const height = Math.sqrt(edge) * Math.max(0, 1 - f1 * 0.45);
      const shade = 0.35 + 0.65 * height;
      const k = (j * size + i) * 4;
      data[k] = Math.round(Math.min(1, tone[nearest * 3] * shade) * 255);
      data[k + 1] = Math.round(Math.min(1, tone[nearest * 3 + 1] * shade) * 255);
      data[k + 2] = Math.round(Math.min(1, tone[nearest * 3 + 2] * shade) * 255);
      data[k + 3] = Math.round(height * 255);
    }
  }
  pebbleTexture = RawTexture.CreateRGBATexture(data, size, size, scene, true, false, Texture.TRILINEAR_SAMPLINGMODE);
  pebbleTexture.name = "BotGround pebbles";
  // RawTexture clamps by default: the world-space uv would read one edge pixel everywhere
  pebbleTexture.wrapU = Texture.WRAP_ADDRESSMODE;
  pebbleTexture.wrapV = Texture.WRAP_ADDRESSMODE;
  pebbleTexture.anisotropicFilteringLevel = 8;
  pebbleScene = scene;
  return pebbleTexture;
}

/**
 * The gravel bed around one bot ground (the mesh BabylonTerrainTileImpl lays on the terrain): the
 * pebble texture by world XZ, where the bed ends single pebbles thin out instead of a cut edge. The
 * coverage comes from the plate's bake (markings.gravel); a pebble shows where it is higher than the
 * coverage leaves room for, so the stones in the gaps go first and the tall ones last.
 */
export function buildBotGroundGravelMaterial(scene: Scene, name: string, markings: BotGroundMarkings): NodeMaterial {
  const mat = new NodeMaterial(name, scene);

  const position = new InputBlock("position");
  position.setAsAttribute("position");
  const normal = new InputBlock("normal");
  normal.setAsAttribute("normal");
  const world = new InputBlock("World");
  world.setAsSystemValue(NodeMaterialSystemValues.World);
  const viewProjection = new InputBlock("ViewProjection");
  viewProjection.setAsSystemValue(NodeMaterialSystemValues.ViewProjection);
  const cameraPosition = new InputBlock("cameraPosition");
  cameraPosition.setAsSystemValue(NodeMaterialSystemValues.CameraPosition);

  const worldPos = new TransformBlock("WorldPos");
  position.output.connectTo(worldPos.vector);
  world.output.connectTo(worldPos.transform);
  const worldViewProj = new TransformBlock("WorldPos * ViewProjectionTransform");
  worldPos.output.connectTo(worldViewProj.vector);
  viewProjection.output.connectTo(worldViewProj.transform);
  const vertexOutput = new VertexOutputBlock("VertexOutput");
  worldViewProj.output.connectTo(vertexOutput.vector);
  const worldNormal = new TransformBlock("World normal");
  normal.output.connectTo(worldNormal.vector);
  world.output.connectTo(worldNormal.transform);

  const posSplit = new VectorSplitterBlock("World pos splitter");
  worldPos.output.connectTo(posSplit.xyzw);
  const posXZ = new VectorMergerBlock("World XZ");
  posSplit.x.connectTo(posXZ.x);
  posSplit.z.connectTo(posXZ.y);

  // ========== Pebbles ==========
  const pebbleUv = new ScaleBlock("pebble uv");
  posXZ.xy.connectTo(pebbleUv.input);
  floatInput("pebble scale", 1 / PEBBLE_TILE).output.connectTo(pebbleUv.factor);
  const pebbleTex = new TextureBlock("BotGround pebbles");
  pebbleUv.output.connectTo(pebbleTex.uv);
  pebbleTex.texture = pebbles(scene);

  // ========== Coverage from the bake ==========
  const coverageOrigin = new InputBlock("gravel origin", undefined, NodeMaterialBlockConnectionPointTypes.Vector2);
  coverageOrigin.value = new Vector2(markings.originX, markings.originZ);
  const coverageInvSize = new InputBlock("gravel inverse size", undefined, NodeMaterialBlockConnectionPointTypes.Vector2);
  coverageInvSize.value = new Vector2(1 / markings.sizeX, 1 / markings.sizeZ);
  const coverageLocal = new SubtractBlock("gravel local");
  posXZ.xy.connectTo(coverageLocal.left);
  coverageOrigin.output.connectTo(coverageLocal.right);
  const coverageUv = new MultiplyBlock("gravel coverage uv");
  coverageLocal.output.connectTo(coverageUv.left);
  coverageInvSize.output.connectTo(coverageUv.right);
  const coverageTex = new TextureBlock("BotGround gravel coverage");
  coverageUv.output.connectTo(coverageTex.uv);
  coverageTex.texture = markings.gravel;

  // A pebble stays if its height + 0.15 beats the room the coverage leaves: alpha 0.5 is the cut
  const missing = new OneMinusBlock("gravel missing");
  coverageTex.r.connectTo(missing.input);
  const missingScaled = new ScaleBlock("gravel missing scaled");
  missing.output.connectTo(missingScaled.input);
  floatInput("gravel thinning", 1.15).output.connectTo(missingScaled.factor);
  const pebbleLifted = new AddBlock("pebble lifted");
  pebbleTex.a.connectTo(pebbleLifted.left);
  floatInput("pebble lift", 0.65).output.connectTo(pebbleLifted.right);
  const alpha = new SubtractBlock("gravel alpha");
  pebbleLifted.output.connectTo(alpha.left);
  missingScaled.output.connectTo(alpha.right);

  const tint = new MultiplyBlock("gravel tint");
  pebbleTex.rgb.connectTo(tint.left);
  const tintColor = new InputBlock("gravel color", undefined, NodeMaterialBlockConnectionPointTypes.Color3);
  // A little darker than the pebble texture: next to the asphalt the bright stones glared
  tintColor.value = new Color3(0.72, 0.71, 0.69);
  tintColor.output.connectTo(tint.right);

  // ========== Light, clouds ==========
  const light = new LightBlock("Lights");
  worldPos.output.connectTo(light.worldPosition);
  worldNormal.output.connectTo(light.worldNormal);
  cameraPosition.output.connectTo(light.cameraPosition);
  tint.output.connectTo(light.diffuseColor);
  const cloudShade = cloudShadeNode(posXZ.xy, cloudTimeInput(scene, mat, "cloud time").output, "bot cloud shadow strength");
  const sunDiffuse = new ScaleBlock("sun diffuse");
  light.diffuseOutput.connectTo(sunDiffuse.input);
  cloudShade.connectTo(sunDiffuse.factor);
  const ambient = new ScaleBlock("Ambient");
  tint.output.connectTo(ambient.input);
  floatInput("ambient strength", 0.35).output.connectTo(ambient.factor);
  const finalColor = new AddBlock("Final color");
  ambient.output.connectTo(finalColor.left);
  sunDiffuse.output.connectTo(finalColor.right);

  const fragmentOutput = new FragmentOutputBlock("FragmentOutput");
  finalColor.output.connectTo(fragmentOutput.rgb);

  mat.addOutputNode(vertexOutput);
  // Alpha test by discard: no blending, so nothing to sort against the water and the plants
  const discard = new DiscardBlock("gravel cut");
  alpha.output.connectTo(discard.value);
  floatInput("gravel cutoff", 0.5).output.connectTo(discard.cutoff);
  mat.addOutputNode(fragmentOutput);
  mat.addOutputNode(discard);
  mat.build();
  return mat;
}
