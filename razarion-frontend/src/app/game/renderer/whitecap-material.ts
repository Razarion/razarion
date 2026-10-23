import {NodeMaterial} from "@babylonjs/core/Materials/Node/nodeMaterial";
import {Texture} from "@babylonjs/core/Materials/Textures/texture";
import {Color3} from "@babylonjs/core/Maths/math.color";
import {InputBlock} from "@babylonjs/core/Materials/Node/Blocks/Input/inputBlock";
import {NodeMaterialBlockConnectionPointTypes} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialBlockConnectionPointTypes";
import {NodeMaterialSystemValues} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialSystemValues";
import {TransformBlock} from "@babylonjs/core/Materials/Node/Blocks/transformBlock";
import {VertexOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Vertex/vertexOutputBlock";
import {FragmentOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/fragmentOutputBlock";
import {TextureBlock} from "@babylonjs/core/Materials/Node/Blocks/Dual/textureBlock";
import {MultiplyBlock} from "@babylonjs/core/Materials/Node/Blocks/multiplyBlock";
import {SmoothStepBlock} from "@babylonjs/core/Materials/Node/Blocks/smoothStepBlock";
import {VectorSplitterBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorSplitterBlock";
import {ScaleBlock} from "@babylonjs/core/Materials/Node/Blocks/scaleBlock";
import {OneMinusBlock} from "@babylonjs/core/Materials/Node/Blocks/oneMinusBlock";
import {AddBlock} from "@babylonjs/core/Materials/Node/Blocks/addBlock";
import {VectorMergerBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorMergerBlock";
import {MinBlock} from "@babylonjs/core/Materials/Node/Blocks/minBlock";
import {MaxBlock} from "@babylonjs/core/Materials/Node/Blocks/maxBlock";
import {LerpBlock} from "@babylonjs/core/Materials/Node/Blocks/lerpBlock";
import {NegateBlock} from "@babylonjs/core/Materials/Node/Blocks/negateBlock";
import type {Scene} from "@babylonjs/core/scene";
import {generateWhitecapTexture} from "./whitecap-texture-generator";
import {Showcase} from "./showcase";

const TEX_PATH = "renderer/textures/";

function floatInput(name: string, value: number): InputBlock {
  const b = new InputBlock(name, undefined, NodeMaterialBlockConnectionPointTypes.Float);
  b.value = value;
  return b;
}

function color3Input(name: string, r: number, g: number, b_: number): InputBlock {
  const b = new InputBlock(name, undefined, NodeMaterialBlockConnectionPointTypes.Color3);
  b.value = new Color3(r, g, b_);
  return b;
}

let sharedScene: Scene | null = null;
let sharedMaterial: NodeMaterial | null = null;

/**
 * The whitecap material of the scene, shared by every water tile. Nothing in it belongs to a tile;
 * a material of its own per tile cost 19 ms on average, up to 38, for every water tile built
 * (2026-10-11) - and each started its animation at 0, so neighbouring tiles drifted apart. It lives
 * as long as the scene; the tiles do not dispose it.
 */
export function whitecapMaterial(scene: Scene): NodeMaterial {
  if (!sharedMaterial || sharedScene !== scene) {
    sharedScene = scene;
    sharedMaterial = buildWhitecapMaterial(scene);
  }
  return sharedMaterial;
}

/**
 * Builds a transparent whitecap material for the water surface.
 * Uses two animated noise layers to position foam patches,
 * and a procedural foam texture for realistic appearance.
 * UV2.x = ground height (used to mask: only show where underwater).
 */
function buildWhitecapMaterial(scene: Scene): NodeMaterial {
  const mat = new NodeMaterial("Whitecaps", scene);

  // ========== Vertex attributes ==========
  const position = new InputBlock("position");
  position.setAsAttribute("position");
  const uv = new InputBlock("uv");
  uv.setAsAttribute("uv");
  const uv2 = new InputBlock("uv2");
  uv2.setAsAttribute("uv2");

  // ========== System values ==========
  const world = new InputBlock("World");
  world.setAsSystemValue(NodeMaterialSystemValues.World);
  const viewProjection = new InputBlock("ViewProjection");
  viewProjection.setAsSystemValue(NodeMaterialSystemValues.ViewProjection);

  const time = new InputBlock("Time", undefined, NodeMaterialBlockConnectionPointTypes.Float);
  time.value = 0;
  time.isConstant = false;
  let accumulatedTime = 0;
  // Bind the animation observer to the material lifetime so disposing the material (with the
  // scene) also removes this per-frame callback (see ground-material.ts).
  const timeObserver = scene.onBeforeRenderObservable.add(() => {
    accumulatedTime += scene.getEngine().getDeltaTime() / 1000;
    time.value = accumulatedTime;
  });
  mat.onDisposeObservable.add(() => scene.onBeforeRenderObservable.remove(timeObserver));

  // ========== Vertex shader ==========
  const worldPos = new TransformBlock("WorldPos");
  position.output.connectTo(worldPos.vector);
  world.output.connectTo(worldPos.transform);

  const worldViewProj = new TransformBlock("WorldViewProj");
  worldPos.output.connectTo(worldViewProj.vector);
  viewProjection.output.connectTo(worldViewProj.transform);

  const vertexOutput = new VertexOutputBlock("VertexOutput");
  worldViewProj.output.connectTo(vertexOutput.vector);

  // ========== Water-only mask from UV2.x (ground height) ==========
  const uv2Split = new VectorSplitterBlock("Split UV2");
  uv2.output.connectTo(uv2Split.xyIn);

  const waterMask = new SmoothStepBlock("water mask");
  uv2Split.x.connectTo(waterMask.value);
  floatInput("water mask e0", -0.1).output.connectTo(waterMask.edge0);
  floatInput("water mask e1", 0.0).output.connectTo(waterMask.edge1);

  const waterMaskInv = new OneMinusBlock("water mask inv");
  waterMask.output.connectTo(waterMaskInv.input);

  // ========== Noise layer 1: positioning mask, slow drift ==========
  const timeSpeed1 = new ScaleBlock("wc time speed 1");
  time.output.connectTo(timeSpeed1.input);
  floatInput("wc speed 1", 0.012).output.connectTo(timeSpeed1.factor);

  const timeSpeed1y = new ScaleBlock("wc time speed 1y");
  time.output.connectTo(timeSpeed1y.input);
  floatInput("wc speed 1y", 0.008).output.connectTo(timeSpeed1y.factor);

  const timeOffset1 = new VectorMergerBlock("wc time offset 1");
  timeSpeed1.output.connectTo(timeOffset1.x);
  timeSpeed1y.output.connectTo(timeOffset1.y);

  const uvScaled1 = new ScaleBlock("wc uv scaled 1");
  uv.output.connectTo(uvScaled1.input);
  floatInput("wc uv scale 1", 8).output.connectTo(uvScaled1.factor);

  const uvAnim1 = new AddBlock("wc uv anim 1");
  uvScaled1.output.connectTo(uvAnim1.left);
  timeOffset1.xy.connectTo(uvAnim1.right);

  const noiseTex1 = new TextureBlock("WC noise 1");
  uvAnim1.output.connectTo(noiseTex1.uv);
  noiseTex1.texture = new Texture(TEX_PATH + "foam-noise.webp", scene);

  // ========== Noise layer 2: counter-drift ==========
  const timeSpeed2 = new ScaleBlock("wc time speed 2");
  time.output.connectTo(timeSpeed2.input);
  floatInput("wc speed 2", -0.018).output.connectTo(timeSpeed2.factor);

  const timeSpeed2y = new ScaleBlock("wc time speed 2y");
  time.output.connectTo(timeSpeed2y.input);
  floatInput("wc speed 2y", 0.01).output.connectTo(timeSpeed2y.factor);

  const timeOffset2 = new VectorMergerBlock("wc time offset 2");
  timeSpeed2.output.connectTo(timeOffset2.x);
  timeSpeed2y.output.connectTo(timeOffset2.y);

  const uvScaled2 = new ScaleBlock("wc uv scaled 2");
  uv.output.connectTo(uvScaled2.input);
  floatInput("wc uv scale 2", 12).output.connectTo(uvScaled2.factor);

  const uvAnim2 = new AddBlock("wc uv anim 2");
  uvScaled2.output.connectTo(uvAnim2.left);
  timeOffset2.xy.connectTo(uvAnim2.right);

  const noiseTex2 = new TextureBlock("WC noise 2");
  uvAnim2.output.connectTo(noiseTex2.uv);
  noiseTex2.texture = new Texture(TEX_PATH + "foam-noise.webp", scene);

  // ========== Combine noise: multiply → sparse positioning mask ==========
  const noiseCombined = new MultiplyBlock("wc noise combined");
  noiseTex1.r.connectTo(noiseCombined.left);
  noiseTex2.r.connectTo(noiseCombined.right);

  const noiseThreshold = new SmoothStepBlock("wc threshold");
  noiseCombined.output.connectTo(noiseThreshold.value);
  floatInput("wc thresh e0", 0.15).output.connectTo(noiseThreshold.edge0);
  floatInput("wc thresh e1", 0.35).output.connectTo(noiseThreshold.edge1);

  // ========== Foam appearance texture — sampled at high frequency ==========
  const foamUvScale = new ScaleBlock("foam uv scale");
  uv.output.connectTo(foamUvScale.input);
  floatInput("foam detail scale", 25).output.connectTo(foamUvScale.factor);

  // Slow drift for the foam texture too
  const foamTimeSpeed = new ScaleBlock("foam time speed");
  time.output.connectTo(foamTimeSpeed.input);
  floatInput("foam drift speed", 0.005).output.connectTo(foamTimeSpeed.factor);

  const foamTimeOffset = new VectorMergerBlock("foam time offset");
  foamTimeSpeed.output.connectTo(foamTimeOffset.x);
  floatInput("foam drift y", 0.003).output.connectTo(foamTimeOffset.y);

  const foamUvAnim = new AddBlock("foam uv anim");
  foamUvScale.output.connectTo(foamUvAnim.left);
  foamTimeOffset.xy.connectTo(foamUvAnim.right);

  const foamTex = new TextureBlock("Foam detail");
  foamUvAnim.output.connectTo(foamTex.uv);
  foamTex.texture = new Texture(generateWhitecapTexture(256), scene);
  foamTex.texture.hasAlpha = true;

  // ========== Combine: foam RGB + (foam alpha * noise mask * water mask) ==========
  // Alpha = foam texture alpha * noise positioning * water mask * intensity
  const foamNoiseAlpha = new MultiplyBlock("foam noise alpha");
  foamTex.a.connectTo(foamNoiseAlpha.left);
  noiseThreshold.output.connectTo(foamNoiseAlpha.right);

  const foamWaterAlpha = new MultiplyBlock("foam water alpha");
  foamNoiseAlpha.output.connectTo(foamWaterAlpha.left);
  waterMaskInv.output.connectTo(foamWaterAlpha.right);

  const finalAlpha = new ScaleBlock("wc final alpha");
  foamWaterAlpha.output.connectTo(finalAlpha.input);
  floatInput("wc intensity", 0.6).output.connectTo(finalAlpha.factor);

  // ========== Caustics: the net of light the waves focus onto a shallow sea floor ==========
  // Two drifting copies of the net (razarion-ai-content/scripts/caustics-texture.mjs) at 5 and 6.15 m per
  // repeat - whole repeats per tile, so the seams match - and the dimmer of the two: where the nets cross
  // the light flickers. Only over shallow ground (UV2.x), strongest at half a metre to two and a half.
  const causticsTexture = new Texture(TEX_PATH + "water-caustics.webp", scene);
  const causticLayer = (name: string, scale: number, speedX: number, speedY: number) => {
    const scaled = new ScaleBlock(name + " uv");
    uv.output.connectTo(scaled.input);
    floatInput(name + " scale", scale).output.connectTo(scaled.factor);
    const driftX = new ScaleBlock(name + " drift x");
    time.output.connectTo(driftX.input);
    floatInput(name + " speed x", speedX).output.connectTo(driftX.factor);
    const driftY = new ScaleBlock(name + " drift y");
    time.output.connectTo(driftY.input);
    floatInput(name + " speed y", speedY).output.connectTo(driftY.factor);
    const drift = new VectorMergerBlock(name + " drift");
    driftX.output.connectTo(drift.x);
    driftY.output.connectTo(drift.y);
    const animated = new AddBlock(name + " uv animated");
    scaled.output.connectTo(animated.left);
    drift.xy.connectTo(animated.right);
    const sample = new TextureBlock(name);
    animated.output.connectTo(sample.uv);
    sample.texture = causticsTexture;
    return sample.r;
  };
  const causticNet = new MinBlock("caustic net");
  causticLayer("caustics 1", 32, 0.05, 0.03).connectTo(causticNet.left);
  causticLayer("caustics 2", 26, -0.035, 0.045).connectTo(causticNet.right);

  const depth = new NegateBlock("water depth");
  uv2Split.x.connectTo(depth.value);
  const causticShallow = new SmoothStepBlock("caustic shallow");
  depth.output.connectTo(causticShallow.value);
  floatInput("caustic shallow e0", 0.05).output.connectTo(causticShallow.edge0);
  floatInput("caustic shallow e1", 0.5).output.connectTo(causticShallow.edge1);
  const causticDeep = new SmoothStepBlock("caustic deep");
  depth.output.connectTo(causticDeep.value);
  floatInput("caustic deep e0", 2).output.connectTo(causticDeep.edge0);
  floatInput("caustic deep e1", 4).output.connectTo(causticDeep.edge1);
  const causticNotDeep = new OneMinusBlock("caustic not deep");
  causticDeep.output.connectTo(causticNotDeep.input);
  const causticBand = new MultiplyBlock("caustic band");
  causticShallow.output.connectTo(causticBand.left);
  causticNotDeep.output.connectTo(causticBand.right);
  const causticLight = new MultiplyBlock("caustic light");
  causticNet.output.connectTo(causticLight.left);
  causticBand.output.connectTo(causticLight.right);
  const causticAlpha = new ScaleBlock("caustic alpha");
  causticLight.output.connectTo(causticAlpha.input);
  floatInput("caustic strength", 0.36).output.connectTo(causticAlpha.factor);

  // Foam over caustics: the foam's colour wherever there is foam, the caustics' light elsewhere
  const foamShare = new SmoothStepBlock("foam share");
  finalAlpha.output.connectTo(foamShare.value);
  floatInput("foam share e0", 0).output.connectTo(foamShare.edge0);
  floatInput("foam share e1", 0.15).output.connectTo(foamShare.edge1);
  const overlayColor = new LerpBlock("overlay color");
  color3Input("caustic color", 0.9, 1.0, 0.92).output.connectTo(overlayColor.left);
  foamTex.rgb.connectTo(overlayColor.right);
  foamShare.output.connectTo(overlayColor.gradient);
  const overlayAlpha = new MaxBlock("overlay alpha");
  finalAlpha.output.connectTo(overlayAlpha.left);
  causticAlpha.output.connectTo(overlayAlpha.right);

  // ========== Fragment output ==========
  const fragmentOutput = new FragmentOutputBlock("FragmentOutput");
  overlayColor.output.connectTo(fragmentOutput.rgb);
  overlayAlpha.output.connectTo(fragmentOutput.a);

  // ========== Build ==========
  mat.addOutputNode(vertexOutput);
  mat.addOutputNode(fragmentOutput);
  mat.build();
  Showcase.applyToGround(mat);

  mat.alpha = 1;
  mat.alphaMode = 2; // ALPHA_COMBINE
  mat.backFaceCulling = false;
  mat.disableDepthWrite = true;

  return mat;
}
