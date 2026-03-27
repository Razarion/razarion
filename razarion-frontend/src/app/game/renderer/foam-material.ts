import {Color3, NodeMaterial, Texture} from "@babylonjs/core";
import {InputBlock} from "@babylonjs/core/Materials/Node/Blocks/Input/inputBlock";
import {NodeMaterialBlockConnectionPointTypes} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialBlockConnectionPointTypes";
import {NodeMaterialSystemValues} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialSystemValues";
import {TransformBlock} from "@babylonjs/core/Materials/Node/Blocks/transformBlock";
import {VertexOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Vertex/vertexOutputBlock";
import {FragmentOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/fragmentOutputBlock";
import {TextureBlock} from "@babylonjs/core/Materials/Node/Blocks/Dual/textureBlock";
import {MultiplyBlock} from "@babylonjs/core/Materials/Node/Blocks/multiplyBlock";
import {SubtractBlock} from "@babylonjs/core/Materials/Node/Blocks/subtractBlock";
import {SmoothStepBlock} from "@babylonjs/core/Materials/Node/Blocks/smoothStepBlock";
import {VectorSplitterBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorSplitterBlock";
import {ScaleBlock} from "@babylonjs/core/Materials/Node/Blocks/scaleBlock";
import {OneMinusBlock} from "@babylonjs/core/Materials/Node/Blocks/oneMinusBlock";
import {AddBlock} from "@babylonjs/core/Materials/Node/Blocks/addBlock";
import {VectorMergerBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorMergerBlock";
import {TrigonometryBlock, TrigonometryBlockOperations} from "@babylonjs/core/Materials/Node/Blocks/trigonometryBlock";
import {ClampBlock} from "@babylonjs/core/Materials/Node/Blocks/clampBlock";
import type {Scene} from "@babylonjs/core/scene";
import type {NodeMaterialConnectionPoint} from "@babylonjs/core/Materials/Node/nodeMaterialBlockConnectionPoint";

const TEX_PATH = "renderer/textures/";

function floatInput(name: string, value: number): InputBlock {
  const b = new InputBlock(name, undefined, NodeMaterialBlockConnectionPointTypes.Float);
  b.value = value;
  return b;
}

/**
 * Creates a smoothstep foam band: smoothstep(e0,e1,h) * (1 - smoothstep(e2,e3,h))
 * Result is 1 inside the band, 0 outside, with smooth edges.
 */
function createBand(
  name: string,
  heightOut: NodeMaterialConnectionPoint,
  e0: number, e1: number,
  e2: number, e3: number
): MultiplyBlock {
  const lower = new SmoothStepBlock(`${name} lo`);
  heightOut.connectTo(lower.value);
  floatInput(`${name} lo e0`, e0).output.connectTo(lower.edge0);
  floatInput(`${name} lo e1`, e1).output.connectTo(lower.edge1);

  const upper = new SmoothStepBlock(`${name} up`);
  heightOut.connectTo(upper.value);
  floatInput(`${name} up e0`, e2).output.connectTo(upper.edge0);
  floatInput(`${name} up e1`, e3).output.connectTo(upper.edge1);

  const inv = new OneMinusBlock(`${name} inv`);
  upper.output.connectTo(inv.input);

  const band = new MultiplyBlock(name);
  lower.output.connectTo(band.left);
  inv.output.connectTo(band.right);
  return band;
}

/**
 * Builds a transparent foam NodeMaterial with pulsating wave bands.
 *
 * Two sin-driven wave pulses shift foam bands along the ground height axis,
 * creating the illusion of waves swelling toward shore and receding.
 * A thin bright shoreline band highlights the water-sand boundary.
 */
export function buildFoamMaterial(scene: Scene): NodeMaterial {
  const mat = new NodeMaterial("Foam", scene);

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

  // ========== Ground height from UV2 ==========
  const uv2Split = new VectorSplitterBlock("Split UV2");
  uv2.output.connectTo(uv2Split.xyIn);
  // uv2Split.x = ground height at this water vertex
  // uv2Split.y = shore direction angle (atan2 of height gradient)

  // ========== Water-only mask — foam only where ground is below water level ==========
  const waterMask = new SmoothStepBlock("water mask");
  uv2Split.x.connectTo(waterMask.value);
  floatInput("water mask e0", -0.02).output.connectTo(waterMask.edge0);
  floatInput("water mask e1", 0.02).output.connectTo(waterMask.edge1);

  const waterMaskInv = new OneMinusBlock("water mask inv");
  waterMask.output.connectTo(waterMaskInv.input);
  // waterMaskInv = 1 where underwater, 0 where on land

  // ========== Wave pulses — sin oscillation shifts foam bands toward/away from shore ==========
  // Concept: effectiveHeight = groundHeight - waveOffset
  // When waveOffset > 0 the foam band shifts shoreward (toward height 0).
  // When waveOffset < 0 it recedes seaward.

  // Wave 1: main swell (~9s period)
  const waveTime1 = new ScaleBlock("wave time 1");
  time.output.connectTo(waveTime1.input);
  floatInput("wave freq 1", 0.7).output.connectTo(waveTime1.factor);

  const waveSin1 = new TrigonometryBlock("wave sin 1");
  waveSin1.operation = TrigonometryBlockOperations.Sin;
  waveTime1.output.connectTo(waveSin1.input);

  const waveOffset1 = new ScaleBlock("wave offset 1");
  waveSin1.output.connectTo(waveOffset1.input);
  floatInput("wave amp 1", 0.35).output.connectTo(waveOffset1.factor);

  const height1 = new SubtractBlock("height wave 1");
  uv2Split.x.connectTo(height1.left);
  waveOffset1.output.connectTo(height1.right);

  // Wave 2: secondary swell (same period, phase-shifted ~120°)
  const waveTime2 = new ScaleBlock("wave time 2");
  time.output.connectTo(waveTime2.input);
  floatInput("wave freq 2", 0.7).output.connectTo(waveTime2.factor);

  const wavePhase2 = new AddBlock("wave phase 2");
  waveTime2.output.connectTo(wavePhase2.left);
  floatInput("phase offset", 2.1).output.connectTo(wavePhase2.right);

  const waveSin2 = new TrigonometryBlock("wave sin 2");
  waveSin2.operation = TrigonometryBlockOperations.Sin;
  wavePhase2.output.connectTo(waveSin2.input);

  const waveOffset2 = new ScaleBlock("wave offset 2");
  waveSin2.output.connectTo(waveOffset2.input);
  floatInput("wave amp 2", 0.25).output.connectTo(waveOffset2.factor);

  const height2 = new SubtractBlock("height wave 2");
  uv2Split.x.connectTo(height2.left);
  waveOffset2.output.connectTo(height2.right);

  // ========== Foam bands ==========

  // Band 1: main wave (narrow strip)
  const band1 = createBand("band1", height1.output, -0.5, -0.2, -0.08, 0.0);

  // Band 2: secondary wave (even narrower)
  const band2 = createBand("band2", height2.output, -0.35, -0.15, -0.06, 0.0);

  // Shoreline: thin bright line near height 0, subtle pulse from wave 1
  const shoreOffset = new ScaleBlock("shore offset");
  waveSin1.output.connectTo(shoreOffset.input);
  floatInput("shore amp", 0.08).output.connectTo(shoreOffset.factor);

  const heightShore = new SubtractBlock("height shore");
  uv2Split.x.connectTo(heightShore.left);
  shoreOffset.output.connectTo(heightShore.right);

  const shoreLine = createBand("shore", heightShore.output, -0.2, -0.08, -0.03, 0.03);

  // ========== Animated noise — two layers for organic foam breakup ==========

  // Layer 1: slow drift
  const timeSpeed1 = new ScaleBlock("time speed 1");
  time.output.connectTo(timeSpeed1.input);
  floatInput("speed 1", 0.02).output.connectTo(timeSpeed1.factor);

  const timeOffset1 = new VectorMergerBlock("time offset 1");
  timeSpeed1.output.connectTo(timeOffset1.x);
  timeSpeed1.output.connectTo(timeOffset1.y);

  const uvScaled1 = new ScaleBlock("uv scaled 1");
  uv.output.connectTo(uvScaled1.input);
  floatInput("uv noise scale 1", 6).output.connectTo(uvScaled1.factor);

  const uvAnimated1 = new AddBlock("uv animated 1");
  uvScaled1.output.connectTo(uvAnimated1.left);
  timeOffset1.xy.connectTo(uvAnimated1.right);

  const noiseTex1 = new TextureBlock("Noise texture 1");
  uvAnimated1.output.connectTo(noiseTex1.uv);
  noiseTex1.texture = new Texture(TEX_PATH + "foam-noise.png", scene);

  // Layer 2: faster counter-drift
  const timeSpeed2 = new ScaleBlock("time speed 2");
  time.output.connectTo(timeSpeed2.input);
  floatInput("speed 2", -0.035).output.connectTo(timeSpeed2.factor);

  const timeSpeed2y = new ScaleBlock("time speed 2y");
  time.output.connectTo(timeSpeed2y.input);
  floatInput("speed 2y", 0.015).output.connectTo(timeSpeed2y.factor);

  const timeOffset2 = new VectorMergerBlock("time offset 2");
  timeSpeed2.output.connectTo(timeOffset2.x);
  timeSpeed2y.output.connectTo(timeOffset2.y);

  const uvScaled2 = new ScaleBlock("uv scaled 2");
  uv.output.connectTo(uvScaled2.input);
  floatInput("uv noise scale 2", 10).output.connectTo(uvScaled2.factor);

  const uvAnimated2 = new AddBlock("uv animated 2");
  uvScaled2.output.connectTo(uvAnimated2.left);
  timeOffset2.xy.connectTo(uvAnimated2.right);

  const noiseTex2 = new TextureBlock("Noise texture 2");
  uvAnimated2.output.connectTo(noiseTex2.uv);
  noiseTex2.texture = new Texture(TEX_PATH + "foam-noise.png", scene);

  // Combine noise layers (red channel)
  const noiseCombined = new MultiplyBlock("noise combined");
  noiseTex1.r.connectTo(noiseCombined.left);
  noiseTex2.r.connectTo(noiseCombined.right);

  const noiseSharp = new SmoothStepBlock("noise sharp");
  noiseCombined.output.connectTo(noiseSharp.value);
  floatInput("noise sharp e0", 0.08).output.connectTo(noiseSharp.edge0);
  floatInput("noise sharp e1", 0.35).output.connectTo(noiseSharp.edge1);

  // ========== Combine all bands ==========

  // Band 1 * noise
  const foam1 = new MultiplyBlock("foam1");
  band1.output.connectTo(foam1.left);
  noiseSharp.output.connectTo(foam1.right);

  // Band 2 * noise * 0.7 (secondary wave is subtler)
  const foam2raw = new MultiplyBlock("foam2 raw");
  band2.output.connectTo(foam2raw.left);
  noiseSharp.output.connectTo(foam2raw.right);

  const foam2 = new ScaleBlock("foam2");
  foam2raw.output.connectTo(foam2.input);
  floatInput("band2 intensity", 0.7).output.connectTo(foam2.factor);

  // Shoreline * noise
  const shoreNoisy = new MultiplyBlock("shore noisy");
  shoreLine.output.connectTo(shoreNoisy.left);
  noiseSharp.output.connectTo(shoreNoisy.right);

  const shoreScaled = new ScaleBlock("shore scaled");
  shoreNoisy.output.connectTo(shoreScaled.input);
  floatInput("shore intensity", 0.8).output.connectTo(shoreScaled.factor);

  // Sum all layers
  const foamSum12 = new AddBlock("foam sum 1+2");
  foam1.output.connectTo(foamSum12.left);
  foam2.output.connectTo(foamSum12.right);

  const foamSum = new AddBlock("foam sum all");
  foamSum12.output.connectTo(foamSum.left);
  shoreScaled.output.connectTo(foamSum.right);

  // Clamp to [0, 1] then boost for visibility
  const foamClamped = new ClampBlock("foam clamp");
  foamSum.output.connectTo(foamClamped.value);

  // Apply water mask — no foam on land
  const foamMasked = new MultiplyBlock("foam masked");
  foamClamped.output.connectTo(foamMasked.left);
  waterMaskInv.output.connectTo(foamMasked.right);

  const foamAlpha = new MultiplyBlock("foam alpha");
  foamMasked.output.connectTo(foamAlpha.left);
  floatInput("foam alpha boost", 1.0).output.connectTo(foamAlpha.right);

  // ========== Fragment output ==========
  const fragmentOutput = new FragmentOutputBlock("FragmentOutput");

  const foamColor = new InputBlock("Foam color", undefined, NodeMaterialBlockConnectionPointTypes.Color3);
  foamColor.value = new Color3(0.95, 0.97, 1.0);
  foamColor.output.connectTo(fragmentOutput.rgb);
  foamAlpha.output.connectTo(fragmentOutput.a);

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
