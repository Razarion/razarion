import {Color3, NodeMaterial, Texture, Vector2} from "@babylonjs/core";
import {InputBlock} from "@babylonjs/core/Materials/Node/Blocks/Input/inputBlock";
import {NodeMaterialBlockConnectionPointTypes} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialBlockConnectionPointTypes";
import {NodeMaterialSystemValues} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialSystemValues";
import {TransformBlock} from "@babylonjs/core/Materials/Node/Blocks/transformBlock";
import {VertexOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Vertex/vertexOutputBlock";
import {FragmentOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/fragmentOutputBlock";
import {AddBlock} from "@babylonjs/core/Materials/Node/Blocks/addBlock";
import {SubtractBlock} from "@babylonjs/core/Materials/Node/Blocks/subtractBlock";
import {MultiplyBlock} from "@babylonjs/core/Materials/Node/Blocks/multiplyBlock";
import {ScaleBlock} from "@babylonjs/core/Materials/Node/Blocks/scaleBlock";
import {ClampBlock} from "@babylonjs/core/Materials/Node/Blocks/clampBlock";
import {ModBlock} from "@babylonjs/core/Materials/Node/Blocks/modBlock";
import {DotBlock} from "@babylonjs/core/Materials/Node/Blocks/dotBlock";
import {StepBlock} from "@babylonjs/core/Materials/Node/Blocks/stepBlock";
import {LerpBlock} from "@babylonjs/core/Materials/Node/Blocks/lerpBlock";
import {MaxBlock} from "@babylonjs/core/Materials/Node/Blocks/maxBlock";
import {SmoothStepBlock} from "@babylonjs/core/Materials/Node/Blocks/smoothStepBlock";
import {VectorSplitterBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorSplitterBlock";
import {VectorMergerBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorMergerBlock";
import {TextureBlock} from "@babylonjs/core/Materials/Node/Blocks/Dual/textureBlock";
import {TrigonometryBlock, TrigonometryBlockOperations} from "@babylonjs/core/Materials/Node/Blocks/trigonometryBlock";
import type {Scene} from "@babylonjs/core/scene";

const TEX_PATH = "renderer/textures/";

function floatInput(name: string, value: number): InputBlock {
  const b = new InputBlock(name, undefined, NodeMaterialBlockConnectionPointTypes.Float);
  b.value = value;
  return b;
}

function vec2Input(name: string, x: number, y: number): InputBlock {
  const b = new InputBlock(name, undefined, NodeMaterialBlockConnectionPointTypes.Vector2);
  b.value = new Vector2(x, y);
  return b;
}

function color3Input(name: string, r: number, g: number, b_: number): InputBlock {
  const b = new InputBlock(name, undefined, NodeMaterialBlockConnectionPointTypes.Color3);
  b.value = new Color3(r, g, b_);
  return b;
}

/**
 * Builds the quest place-marker NodeMaterial.
 *
 * Renders a green pointy-top honeycomb stencil over the place mesh: the
 * hex border masks visibility, and the noise texture scrolling
 * underneath modulates brightness and alpha so the lines look like
 * energy flowing through the grid. The grid itself is built from the
 * classic two-lattice trick (pick the closer of two offset triangular
 * lattices). Depth write is disabled so TerrainObjects (palms etc.)
 * occlude the marker via the default LEQUAL depth test.
 */
export function buildQuestPlaceVisualizationMaterial(scene: Scene): NodeMaterial {
  const mat = new NodeMaterial("Quest place visualization", scene);

  // ========== Vertex attributes ==========
  const position = new InputBlock("position");
  position.setAsAttribute("position");
  const uv = new InputBlock("uv");
  uv.setAsAttribute("uv");

  // ========== System values ==========
  const world = new InputBlock("World");
  world.setAsSystemValue(NodeMaterialSystemValues.World);
  const viewProjection = new InputBlock("ViewProjection");
  viewProjection.setAsSystemValue(NodeMaterialSystemValues.ViewProjection);

  // Animated time input — driven from onBeforeRender (same pattern as foam).
  const time = new InputBlock("Time", undefined, NodeMaterialBlockConnectionPointTypes.Float);
  time.value = 0;
  time.isConstant = false;
  let accumulatedTime = 0;
  const tickObserver = scene.onBeforeRenderObservable.add(() => {
    accumulatedTime += scene.getEngine().getDeltaTime() / 1000;
    time.value = accumulatedTime;
  });
  mat.onDisposeObservable.add(() => {
    scene.onBeforeRenderObservable.remove(tickObserver);
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

  // ========== Hex SDF — two-lattice trick ==========
  // Caller pre-scales UVs by the mesh extents (m), so vUv is in world meters.
  // hexScale converts meters → lattice units; one hex ≈ 1/hexScale meters wide.
  const hexScale = floatInput("hex scale", 0.8);
  const uvScaled = new ScaleBlock("uv scaled");
  uv.output.connectTo(uvScaled.input);
  hexScale.output.connectTo(uvScaled.factor);

  // r = (1, sqrt(3)) — hex tile dimensions; h = r/2 used for second lattice offset.
  const r = vec2Input("hex tile r", 1.0, 1.732050808);
  const h = new ScaleBlock("hex tile h");
  r.output.connectTo(h.input);
  floatInput("half", 0.5).output.connectTo(h.factor);

  // a = mod(p, r) - h
  const modA = new ModBlock("mod a");
  uvScaled.output.connectTo(modA.left);
  r.output.connectTo(modA.right);

  const a = new SubtractBlock("a");
  modA.output.connectTo(a.left);
  h.output.connectTo(a.right);

  // b = mod(p - h, r) - h
  const pMinusH = new SubtractBlock("p - h");
  uvScaled.output.connectTo(pMinusH.left);
  h.output.connectTo(pMinusH.right);

  const modB = new ModBlock("mod b");
  pMinusH.output.connectTo(modB.left);
  r.output.connectTo(modB.right);

  const b = new SubtractBlock("b");
  modB.output.connectTo(b.left);
  h.output.connectTo(b.right);

  // Pick whichever lattice point is closer (dot(v,v) = squared distance).
  const dotA = new DotBlock("dot a");
  a.output.connectTo(dotA.left);
  a.output.connectTo(dotA.right);

  const dotB = new DotBlock("dot b");
  b.output.connectTo(dotB.left);
  b.output.connectTo(dotB.right);

  // pickB = step(dotB, dotA) = 1 iff dotA >= dotB → pick b; else 0 → pick a.
  const pickB = new StepBlock("pick b");
  dotA.output.connectTo(pickB.value);
  dotB.output.connectTo(pickB.edge);

  const q = new LerpBlock("q");
  a.output.connectTo(q.left);
  b.output.connectTo(q.right);
  pickB.output.connectTo(q.gradient);

  // Pointy-top hex SDF (Voronoi cell of the two-lattice with r=(1, sqrt(3))):
  //   d = max(|qx|, |qx|*sin30 + |qy|*cos30)
  // Vertical side edges sit at |qx| = 0.5; the four inclined edges sit where
  // |qx|*0.5 + |qy|*0.866 = 0.5.
  const qSplit = new VectorSplitterBlock("split q");
  q.output.connectTo(qSplit.xyIn);

  const absQx = new TrigonometryBlock("abs qx");
  absQx.operation = TrigonometryBlockOperations.Abs;
  qSplit.x.connectTo(absQx.input);

  const absQy = new TrigonometryBlock("abs qy");
  absQy.operation = TrigonometryBlockOperations.Abs;
  qSplit.y.connectTo(absQy.input);

  const termX = new ScaleBlock("term x");
  absQx.output.connectTo(termX.input);
  floatInput("sin 30", 0.5).output.connectTo(termX.factor);

  const termY = new ScaleBlock("term y");
  absQy.output.connectTo(termY.input);
  floatInput("cos 30", 0.866025).output.connectTo(termY.factor);

  const termSum = new AddBlock("term sum");
  termX.output.connectTo(termSum.left);
  termY.output.connectTo(termSum.right);

  const d = new MaxBlock("d");
  termSum.output.connectTo(d.left);
  absQx.output.connectTo(d.right);

  // ========== Edge band ==========
  // edge = smoothstep(0.5 - thickness, 0.5, d) — 1 near hex border, 0 in center.
  const thickness = floatInput("edge thickness", 0.06);
  const halfConst = floatInput("0.5", 0.5);
  const edge0 = new SubtractBlock("edge0");
  halfConst.output.connectTo(edge0.left);
  thickness.output.connectTo(edge0.right);

  const edge = new SmoothStepBlock("edge");
  d.output.connectTo(edge.value);
  edge0.output.connectTo(edge.edge0);
  halfConst.output.connectTo(edge.edge1);

  // ========== Noise — two layers of the same texture, crossed directions ==========
  // World-meter UVs are tiled by noiseUvScale (shared). Two time-driven offsets
  // drift the lattice in different directions; multiplying the two samples
  // creates moving cellular highlights where both layers happen to be bright.
  const noiseUvScale = new ScaleBlock("noise uv scale");
  uv.output.connectTo(noiseUvScale.input);
  floatInput("noise uv scale", 0.02).output.connectTo(noiseUvScale.factor);

  // Layer 1 — diagonal drift toward upper-right.
  const scrollSpeedX1 = new ScaleBlock("noise1 scroll x");
  time.output.connectTo(scrollSpeedX1.input);
  floatInput("scroll1 speed x", 0.08).output.connectTo(scrollSpeedX1.factor);

  const scrollSpeedY1 = new ScaleBlock("noise1 scroll y");
  time.output.connectTo(scrollSpeedY1.input);
  floatInput("scroll1 speed y", 0.05).output.connectTo(scrollSpeedY1.factor);

  const noiseOffset1 = new VectorMergerBlock("noise1 offset");
  scrollSpeedX1.output.connectTo(noiseOffset1.x);
  scrollSpeedY1.output.connectTo(noiseOffset1.y);

  const noiseUv1 = new AddBlock("noise1 uv");
  noiseUvScale.output.connectTo(noiseUv1.left);
  noiseOffset1.xy.connectTo(noiseUv1.right);

  const noiseTex1 = new TextureBlock("noise 1");
  noiseUv1.output.connectTo(noiseTex1.uv);
  noiseTex1.texture = new Texture(TEX_PATH + "foam-noise.png", scene);

  // Layer 2 — counter-drift toward lower-left, slightly different speed so the
  // two layers never sync up and the interference pattern keeps evolving.
  const scrollSpeedX2 = new ScaleBlock("noise2 scroll x");
  time.output.connectTo(scrollSpeedX2.input);
  floatInput("scroll2 speed x", -0.07).output.connectTo(scrollSpeedX2.factor);

  const scrollSpeedY2 = new ScaleBlock("noise2 scroll y");
  time.output.connectTo(scrollSpeedY2.input);
  floatInput("scroll2 speed y", 0.09).output.connectTo(scrollSpeedY2.factor);

  const noiseOffset2 = new VectorMergerBlock("noise2 offset");
  scrollSpeedX2.output.connectTo(noiseOffset2.x);
  scrollSpeedY2.output.connectTo(noiseOffset2.y);

  const noiseUv2 = new AddBlock("noise2 uv");
  noiseUvScale.output.connectTo(noiseUv2.left);
  noiseOffset2.xy.connectTo(noiseUv2.right);

  const noiseTex2 = new TextureBlock("noise 2");
  noiseUv2.output.connectTo(noiseTex2.uv);
  noiseTex2.texture = new Texture(TEX_PATH + "foam-noise.png", scene);

  // Combine via screen blend: 1 - (1-a)(1-b) = a + b - a*b.
  // Brighter than multiply (mean ~0.5 vs ~0.25 for uniform layers) while
  // still dark where both layers are dark — keeps the interference feel.
  const noiseSum = new AddBlock("noise sum");
  noiseTex1.r.connectTo(noiseSum.left);
  noiseTex2.r.connectTo(noiseSum.right);

  const noiseProd = new MultiplyBlock("noise prod");
  noiseTex1.r.connectTo(noiseProd.left);
  noiseTex2.r.connectTo(noiseProd.right);

  const noiseCombined = new SubtractBlock("noise combined (screen)");
  noiseSum.output.connectTo(noiseCombined.left);
  noiseProd.output.connectTo(noiseCombined.right);

  // ========== RGB — color * (brightFloor + brightAmp * noise) ==========
  const brightnessNoise = new ScaleBlock("brightness noise");
  noiseCombined.output.connectTo(brightnessNoise.input);
  floatInput("bright amp", 0.35).output.connectTo(brightnessNoise.factor);

  const brightness = new AddBlock("brightness");
  floatInput("bright base", 0.75).output.connectTo(brightness.left);
  brightnessNoise.output.connectTo(brightness.right);

  // Clamp brightness to [0, 1] so finalRgb never exceeds hexColor — peaks stay
  // at the configured light-green tone instead of washing out toward white.
  const brightnessClamped = new ClampBlock("brightness clamp");
  brightnessClamped.minimum = 0;
  brightnessClamped.maximum = 1;
  brightness.output.connectTo(brightnessClamped.value);

  const hexColor = color3Input("hex color", 0.6, 1.0, 0.6);
  const finalRgb = new ScaleBlock("final rgb");
  hexColor.output.connectTo(finalRgb.input);
  brightnessClamped.output.connectTo(finalRgb.factor);

  // ========== Alpha — edge * (alphaFloor + alphaAmp * noise) ==========
  // alphaFloor keeps the entire hex border visible; noise just adds glow.
  const alphaNoise = new ScaleBlock("alpha noise");
  noiseCombined.output.connectTo(alphaNoise.input);
  floatInput("alpha amp", 0.6).output.connectTo(alphaNoise.factor);

  const alphaCurve = new AddBlock("alpha curve");
  floatInput("alpha base", 0.4).output.connectTo(alphaCurve.left);
  alphaNoise.output.connectTo(alphaCurve.right);

  const alpha = new MultiplyBlock("alpha");
  edge.output.connectTo(alpha.left);
  alphaCurve.output.connectTo(alpha.right);

  // ========== Fragment output ==========
  const fragmentOutput = new FragmentOutputBlock("FragmentOutput");
  finalRgb.output.connectTo(fragmentOutput.rgb);
  alpha.output.connectTo(fragmentOutput.a);

  // ========== Build ==========
  mat.addOutputNode(vertexOutput);
  mat.addOutputNode(fragmentOutput);
  mat.build();

  mat.alpha = 1;
  mat.alphaMode = 2; // ALPHA_COMBINE
  mat.backFaceCulling = false;
  mat.disableDepthWrite = true;
  // Depth bias toward the camera so the ground-draped marker is not occluded by
  // the terrain it conforms to (same trick as the resource decal). The bias is
  // small relative to scene depth, so terrain objects (palms) and units still
  // occlude the marker via the normal depth test.
  mat.zOffset = -4;

  return mat;
}
