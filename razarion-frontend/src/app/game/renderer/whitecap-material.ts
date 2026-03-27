import {Color3, NodeMaterial, Texture} from "@babylonjs/core";
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
import type {Scene} from "@babylonjs/core/scene";
import {generateWhitecapTexture} from "./whitecap-texture-generator";

const TEX_PATH = "renderer/textures/";

function floatInput(name: string, value: number): InputBlock {
  const b = new InputBlock(name, undefined, NodeMaterialBlockConnectionPointTypes.Float);
  b.value = value;
  return b;
}

/**
 * Builds a transparent whitecap material for the water surface.
 * Uses two animated noise layers to position foam patches,
 * and a procedural foam texture for realistic appearance.
 * UV2.x = ground height (used to mask: only show where underwater).
 */
export function buildWhitecapMaterial(scene: Scene): NodeMaterial {
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
  scene.onBeforeRenderObservable.add(() => {
    accumulatedTime += scene.getEngine().getDeltaTime() / 1000;
    time.value = accumulatedTime;
  });

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
  noiseTex1.texture = new Texture(TEX_PATH + "foam-noise.png", scene);

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
  noiseTex2.texture = new Texture(TEX_PATH + "foam-noise.png", scene);

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

  // ========== Fragment output ==========
  const fragmentOutput = new FragmentOutputBlock("FragmentOutput");
  foamTex.rgb.connectTo(fragmentOutput.rgb);
  finalAlpha.output.connectTo(fragmentOutput.a);

  // ========== Build ==========
  mat.addOutputNode(vertexOutput);
  mat.addOutputNode(fragmentOutput);
  mat.build();

  mat.alpha = 1;
  mat.alphaMode = 2; // ALPHA_COMBINE
  mat.backFaceCulling = false;
  mat.disableDepthWrite = true;

  return mat;
}
