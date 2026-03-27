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
import {TrigonometryBlock, TrigonometryBlockOperations} from "@babylonjs/core/Materials/Node/Blocks/trigonometryBlock";
import type {Scene} from "@babylonjs/core/scene";

const TEX_PATH = "renderer/textures/";

function floatInput(name: string, value: number): InputBlock {
  const b = new InputBlock(name, undefined, NodeMaterialBlockConnectionPointTypes.Float);
  b.value = value;
  return b;
}

/**
 * Builds a wave quad material with animated texture scrolling and alpha fade.
 * UV: standard texture coordinates on each quad.
 * UV2: (phase, perpDistance) — phase offset per quad, perpendicular distance 0-1.
 */
export function buildWaveMaterial(scene: Scene): NodeMaterial {
  const mat = new NodeMaterial("WaveQuad", scene);

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

  // ========== UV2: phase and perp distance ==========
  const uv2Split = new VectorSplitterBlock("Split UV2");
  uv2.output.connectTo(uv2Split.xyIn);
  // uv2Split.x = per-quad random phase (0 to 2*PI)
  // uv2Split.y = perpendicular distance from shore (0=shore, 1=water edge)

  // ========== Animated V offset — wave pulsing toward shore ==========
  const wavePhase = new AddBlock("wave phase");
  const timeScaled = new ScaleBlock("time scaled");
  time.output.connectTo(timeScaled.input);
  floatInput("wave speed", 0.7).output.connectTo(timeScaled.factor);
  timeScaled.output.connectTo(wavePhase.left);
  uv2Split.x.connectTo(wavePhase.right);

  const waveSin = new TrigonometryBlock("wave sin");
  waveSin.operation = TrigonometryBlockOperations.Sin;
  wavePhase.output.connectTo(waveSin.input);

  // UV split
  const uvSplit = new VectorSplitterBlock("UV split");
  uv.output.connectTo(uvSplit.xyIn);

  // Shift V by wave pulse
  const vOffset = new ScaleBlock("v offset");
  waveSin.output.connectTo(vOffset.input);
  floatInput("v offset amp", 0.15).output.connectTo(vOffset.factor);

  const vAnimated = new AddBlock("v animated");
  uvSplit.y.connectTo(vAnimated.left);
  vOffset.output.connectTo(vAnimated.right);

  // Recombine UV
  const uvAnimated = new VectorMergerBlock("uv animated");
  uvSplit.x.connectTo(uvAnimated.x);
  vAnimated.output.connectTo(uvAnimated.y);

  // ========== Texture ==========
  const waveTex = new TextureBlock("Wave texture");
  uvAnimated.xy.connectTo(waveTex.uv);
  waveTex.texture = new Texture(TEX_PATH + "foam-wave.png", scene);

  // ========== Alpha: texture * edge fade * temporal pulse ==========

  // Edge fade: fade out at water-side edge (v approaching 1)
  const edgeFade = new SmoothStepBlock("edge fade");
  uvSplit.y.connectTo(edgeFade.value);
  floatInput("fade start", 0.6).output.connectTo(edgeFade.edge0);
  floatInput("fade end", 1.0).output.connectTo(edgeFade.edge1);

  const edgeFadeInv = new OneMinusBlock("edge fade inv");
  edgeFade.output.connectTo(edgeFadeInv.input);

  // Shore-side fade: fade out at shore edge (v approaching 0)
  const shoreFade = new SmoothStepBlock("shore fade");
  uvSplit.y.connectTo(shoreFade.value);
  floatInput("shore fade start", 0.0).output.connectTo(shoreFade.edge0);
  floatInput("shore fade end", 0.15).output.connectTo(shoreFade.edge1);

  // Temporal pulse: stronger when wave is at shore
  const pulse01 = new ScaleBlock("pulse 0-1");
  waveSin.output.connectTo(pulse01.input);
  floatInput("pulse scale", 0.3).output.connectTo(pulse01.factor);

  const pulseOffset = new AddBlock("pulse offset");
  pulse01.output.connectTo(pulseOffset.left);
  floatInput("pulse base", 0.7).output.connectTo(pulseOffset.right);

  // Combined alpha
  const texAlpha = waveTex.r; // foam-wave.png has foam baked into RGB

  const alpha1 = new MultiplyBlock("alpha tex*edge");
  texAlpha.connectTo(alpha1.left);
  edgeFadeInv.output.connectTo(alpha1.right);

  const alpha2 = new MultiplyBlock("alpha *shore");
  alpha1.output.connectTo(alpha2.left);
  shoreFade.output.connectTo(alpha2.right);

  const alpha3 = new MultiplyBlock("alpha *pulse");
  alpha2.output.connectTo(alpha3.left);
  pulseOffset.output.connectTo(alpha3.right);

  const finalAlpha = new MultiplyBlock("final alpha");
  alpha3.output.connectTo(finalAlpha.left);
  floatInput("alpha boost", 1.5).output.connectTo(finalAlpha.right);

  // ========== Fragment output ==========
  const fragmentOutput = new FragmentOutputBlock("FragmentOutput");

  const foamColor = new InputBlock("Foam color", undefined, NodeMaterialBlockConnectionPointTypes.Color3);
  foamColor.value = new Color3(0.95, 0.97, 1.0);
  foamColor.output.connectTo(fragmentOutput.rgb);
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
