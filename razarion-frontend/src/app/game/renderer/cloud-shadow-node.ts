import {InputBlock} from "@babylonjs/core/Materials/Node/Blocks/Input/inputBlock";
import {NodeMaterialBlockConnectionPointTypes} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialBlockConnectionPointTypes";
import {ScaleBlock} from "@babylonjs/core/Materials/Node/Blocks/scaleBlock";
import {AddBlock} from "@babylonjs/core/Materials/Node/Blocks/addBlock";
import {VectorSplitterBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorSplitterBlock";
import {VectorMergerBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorMergerBlock";
import {SimplexPerlin3DBlock} from "@babylonjs/core/Materials/Node/Blocks/simplexPerlin3DBlock";
import {SmoothStepBlock} from "@babylonjs/core/Materials/Node/Blocks/smoothStepBlock";
import {OneMinusBlock} from "@babylonjs/core/Materials/Node/Blocks/oneMinusBlock";
import type {NodeMaterial} from "@babylonjs/core/Materials/Node/nodeMaterial";
import type {NodeMaterialConnectionPoint} from "@babylonjs/core/Materials/Node/nodeMaterialBlockConnectionPoint";
import type {Scene} from "@babylonjs/core/scene";
import {FragmentOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/fragmentOutputBlock";
import {TransformBlock} from "@babylonjs/core/Materials/Node/Blocks/transformBlock";
import {LerpBlock} from "@babylonjs/core/Materials/Node/Blocks/lerpBlock";
import {MultiplyBlock} from "@babylonjs/core/Materials/Node/Blocks/multiplyBlock";
import {NodeMaterialSystemValues} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialSystemValues";
import {CLOUD, cloudTime} from "./cloud-shadow";

function floatInput(name: string, value: number): InputBlock {
  const b = new InputBlock(name, undefined, NodeMaterialBlockConnectionPointTypes.Float);
  b.value = value;
  return b;
}

/**
 * Seconds on the shared cloud clock (cloudTime), updated every frame. Shared, not per material:
 * every tile's clouds must be in the same place, or they would jump at tile seams for tiles built at
 * different moments. The per-frame observer goes with the material: materials built per tile would
 * otherwise leave a live callback behind for every tile ever scrolled into view.
 */
export function cloudTimeInput(scene: Scene, material: NodeMaterial, name: string): InputBlock {
  const time = new InputBlock(name, undefined, NodeMaterialBlockConnectionPointTypes.Float);
  time.value = 0;
  time.isConstant = false;
  const observer = scene.onBeforeRenderObservable.add(() => {
    time.value = cloudTime();
  });
  material.onDisposeObservable.add(() => scene.onBeforeRenderObservable.remove(observer));
  return time;
}

/**
 * The share of the sun that reaches a spot under the drifting clouds: 1 in the open, 1 - strength
 * under a cloud. Two octaves of simplex noise drifting with the wind and slowly changing shape, the
 * GPU twin of cloudSunShade (cloud-shadow.ts) - ground, bot grounds and asphalt must all show the same
 * cloud. Computed, not sampled: the ground's fragment shader already uses 15 of the 16 texture units a
 * WebGL2 GPU guarantees. Its strength input (strengthName) is what showcase.ts switches.
 */
export function cloudShadeNode(worldXZ: NodeMaterialConnectionPoint, time: NodeMaterialConnectionPoint,
                               strengthName = "cloud shadow strength"): NodeMaterialConnectionPoint {
  const cloudDrift = new ScaleBlock("cloud drift");
  time.connectTo(cloudDrift.input);
  floatInput("cloud speed", CLOUD.SPEED).output.connectTo(cloudDrift.factor);
  const cloudUvBase = new ScaleBlock("cloud uv base");
  worldXZ.connectTo(cloudUvBase.input);
  floatInput("cloud scale", CLOUD.SCALE).output.connectTo(cloudUvBase.factor);
  const cloudUv = new AddBlock("cloud uv");
  cloudUvBase.output.connectTo(cloudUv.left);
  cloudDrift.output.connectTo(cloudUv.right);
  const cloudUvSplit = new VectorSplitterBlock("Split cloud uv");
  cloudUv.output.connectTo(cloudUvSplit.xyIn);
  const cloudEvolve = new ScaleBlock("cloud evolve");
  time.connectTo(cloudEvolve.input);
  floatInput("cloud evolve speed", CLOUD.EVOLVE_SPEED).output.connectTo(cloudEvolve.factor);
  const cloudSeed = new VectorMergerBlock("cloud seed");
  cloudUvSplit.x.connectTo(cloudSeed.x);
  cloudUvSplit.y.connectTo(cloudSeed.y);
  cloudEvolve.output.connectTo(cloudSeed.z);
  const cloudNoise1 = new SimplexPerlin3DBlock("cloud noise 1");
  cloudSeed.xyz.connectTo(cloudNoise1.seed);
  const cloudSeedFine = new ScaleBlock("cloud seed fine");
  cloudSeed.xyz.connectTo(cloudSeedFine.input);
  floatInput("cloud fine scale", CLOUD.FINE_SCALE).output.connectTo(cloudSeedFine.factor);
  const cloudNoise2 = new SimplexPerlin3DBlock("cloud noise 2");
  cloudSeedFine.output.connectTo(cloudNoise2.seed);
  const cloudNoise2Weighted = new ScaleBlock("cloud noise 2 weighted");
  cloudNoise2.output.connectTo(cloudNoise2Weighted.input);
  floatInput("cloud fine weight", CLOUD.FINE_WEIGHT).output.connectTo(cloudNoise2Weighted.factor);
  const cloudCombined = new AddBlock("cloud combined");
  cloudNoise1.output.connectTo(cloudCombined.left);
  cloudNoise2Weighted.output.connectTo(cloudCombined.right);
  const cloudStep = new SmoothStepBlock("cloud step");
  cloudCombined.output.connectTo(cloudStep.value);
  floatInput("cloud edge0", CLOUD.EDGE0).output.connectTo(cloudStep.edge0);
  floatInput("cloud edge1", CLOUD.EDGE1).output.connectTo(cloudStep.edge1);
  const cloudDarken = new ScaleBlock("cloud darken");
  cloudStep.output.connectTo(cloudDarken.input);
  floatInput(strengthName, CLOUD.SHADOW_STRENGTH).output.connectTo(cloudDarken.factor);
  const cloudShade = new OneMinusBlock("cloud shade");
  cloudDarken.output.connectTo(cloudShade.input);
  return cloudShade.output;
}

/** Share of a lit surface's brightness that is sun (the rest is sky and ambient), for addCloudShadow. */
const SUN_SHARE = 0.8;
const CLOUD_SHADOW_ADDED = "razCloudShadowAdded";

/**
 * Puts the cloud shadows on a material that was authored elsewhere (the asphalt from the database):
 * its final colour is multiplied by the cloud shade, scaled to the sun's share since that graph has no
 * separate sun term. Once per material; it is shared by every tile. Returns false if the graph has no
 * fragment colour output to hook into.
 */
export function addCloudShadow(scene: Scene, material: NodeMaterial): boolean {
  if (material.metadata?.[CLOUD_SHADOW_ADDED]) {
    return true;
  }
  const output = material.getBlockByPredicate(b => b instanceof FragmentOutputBlock) as FragmentOutputBlock | null;
  const colorSource = output?.rgb.connectedPoint ?? null;
  const colorAlphaSource = output?.rgba.connectedPoint ?? null;
  if (!output || (!colorSource && !colorAlphaSource)) {
    return false;
  }
  const position = new InputBlock("cloud position");
  position.setAsAttribute("position");
  const world = new InputBlock("cloud world");
  world.setAsSystemValue(NodeMaterialSystemValues.World);
  const worldPos = new TransformBlock("cloud world pos");
  position.output.connectTo(worldPos.vector);
  world.output.connectTo(worldPos.transform);
  const worldSplit = new VectorSplitterBlock("cloud world split");
  worldPos.output.connectTo(worldSplit.xyzw);
  const worldXZ = new VectorMergerBlock("cloud world XZ");
  worldSplit.x.connectTo(worldXZ.x);
  worldSplit.z.connectTo(worldXZ.y);

  const shade = new LerpBlock("cloud sun share");
  floatInput("one", 1).output.connectTo(shade.left);
  cloudShadeNode(worldXZ.xy, cloudTimeInput(scene, material, "cloud time").output).connectTo(shade.right);
  floatInput("cloud sun share", SUN_SHARE).output.connectTo(shade.gradient);

  if (colorSource) {
    const shaded = new ScaleBlock("cloud shaded color");
    colorSource.connectTo(shaded.input);
    shade.output.connectTo(shaded.factor);
    output.rgb.disconnectFrom(colorSource);
    shaded.output.connectTo(output.rgb);
  } else {
    // Keep the alpha: (shade, shade, shade, 1)
    const factor = new VectorMergerBlock("cloud shade rgba");
    shade.output.connectTo(factor.x);
    shade.output.connectTo(factor.y);
    shade.output.connectTo(factor.z);
    floatInput("one alpha", 1).output.connectTo(factor.w);
    const shaded = new MultiplyBlock("cloud shaded color");
    colorAlphaSource!.connectTo(shaded.left);
    factor.xyzw.connectTo(shaded.right);
    output.rgba.disconnectFrom(colorAlphaSource!);
    shaded.output.connectTo(output.rgba);
  }
  material.metadata = {...material.metadata, [CLOUD_SHADOW_ADDED]: true};
  material.build();
  return true;
}
