import {Color3, NodeMaterial, Texture} from "@babylonjs/core";
import {InputBlock} from "@babylonjs/core/Materials/Node/Blocks/Input/inputBlock";
import {NodeMaterialBlockConnectionPointTypes} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialBlockConnectionPointTypes";
import {NodeMaterialSystemValues} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialSystemValues";
import {TransformBlock} from "@babylonjs/core/Materials/Node/Blocks/transformBlock";
import {VertexOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Vertex/vertexOutputBlock";
import {FragmentOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/fragmentOutputBlock";
import {LightBlock} from "@babylonjs/core/Materials/Node/Blocks/Dual/lightBlock";
import {PerturbNormalBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/perturbNormalBlock";
import {TextureBlock} from "@babylonjs/core/Materials/Node/Blocks/Dual/textureBlock";
import {AddBlock} from "@babylonjs/core/Materials/Node/Blocks/addBlock";
import {ScaleBlock} from "@babylonjs/core/Materials/Node/Blocks/scaleBlock";
import {VectorSplitterBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorSplitterBlock";
import {VectorMergerBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorMergerBlock";
import {TrigonometryBlock, TrigonometryBlockOperations} from "@babylonjs/core/Materials/Node/Blocks/trigonometryBlock";
import {MaxBlock} from "@babylonjs/core/Materials/Node/Blocks/maxBlock";
import {SmoothStepBlock} from "@babylonjs/core/Materials/Node/Blocks/smoothStepBlock";
import {StepBlock} from "@babylonjs/core/Materials/Node/Blocks/stepBlock";
import {LerpBlock} from "@babylonjs/core/Materials/Node/Blocks/lerpBlock";
import {MultiplyBlock} from "@babylonjs/core/Materials/Node/Blocks/multiplyBlock";
import type {Scene} from "@babylonjs/core/scene";

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

/**
 * Builds the BotGround top NodeMaterial entirely in code.
 *
 * UV source: world-space XZ of the vertex position (VectorSplitter on
 * `position` → VectorMerger of x,z into Vector2). This makes the asphalt
 * tile seamlessly across adjacent bot ground boxes instead of restarting
 * per face. Tuned values match the user's Inspector export (nodeMaterial
 * 92): uv scale 0.06, ambient 0.35, bump 1, gloss 0.1/64, spec 0.05.
 *
 * Local texture clones live at
 * `public/renderer/textures/bot-ground-top-{diffuse,norm}.jpg` so they
 * can be edited without affecting the planet's shared asphalt patches.
 */
export function buildBotGroundTopMaterial(scene: Scene): NodeMaterial {
  const mat = new NodeMaterial("BotGround top", scene);

  // ========== Vertex attributes ==========
  const position = new InputBlock("position");
  position.setAsAttribute("position");
  const normal = new InputBlock("normal");
  normal.setAsAttribute("normal");

  // ========== System values ==========
  const world = new InputBlock("World");
  world.setAsSystemValue(NodeMaterialSystemValues.World);
  const viewProjection = new InputBlock("ViewProjection");
  viewProjection.setAsSystemValue(NodeMaterialSystemValues.ViewProjection);
  const cameraPosition = new InputBlock("cameraPosition");
  cameraPosition.setAsSystemValue(NodeMaterialSystemValues.CameraPosition);

  // ========== Vertex shader ==========
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

  // ========== UV from world-space XZ of position ==========
  // Splits the world-space vertex position (after World transform) into
  // components, then merges x and z into a Vector2 UV. Result: the texture
  // repeats on a world-space grid, so the asphalt pattern is continuous
  // across every bot ground box on the planet (not restarting per box).
  const posSplit = new VectorSplitterBlock("VectorSplitter");
  worldPos.output.connectTo(posSplit.xyzw);

  const posXZ = new VectorMergerBlock("VectorMerger");
  posSplit.x.connectTo(posXZ.x);
  posSplit.z.connectTo(posXZ.y);

  const uvScale = floatInput("uv scale", 0.04);
  const scaledUv = new ScaleBlock("Scale uv");
  posXZ.xy.connectTo(scaledUv.input);
  uvScale.output.connectTo(scaledUv.factor);

  // ========== Diffuse + bump ==========
  const diffuseTex = new TextureBlock("BotGround top diffuse");
  scaledUv.output.connectTo(diffuseTex.uv);
  diffuseTex.texture = new Texture(TEX_PATH + "bot-ground-top-diffuse.jpg", scene);

  const normalTex = new TextureBlock("BotGround top norm");
  scaledUv.output.connectTo(normalTex.uv);
  normalTex.texture = new Texture(TEX_PATH + "bot-ground-top-norm.jpg", scene);

  // ========== Faction-colored border tint ==========
  // Detects proximity to the box edge in local XZ (box is 8 m centered on
  // its origin, so |x| and |z| each reach BOT_BOX_LENGTH/2 = 4 at the edge).
  // The tint lerps the asphalt diffuse with a user-chosen border color,
  // so the plateau perimeter reads as bot-owned from a distance.
  const localSplit = new VectorSplitterBlock("Local pos splitter");
  position.output.connectTo(localSplit.xyzIn);

  const absX = new TrigonometryBlock("abs local X");
  absX.operation = TrigonometryBlockOperations.Abs;
  localSplit.x.connectTo(absX.input);

  const absZ = new TrigonometryBlock("abs local Z");
  absZ.operation = TrigonometryBlockOperations.Abs;
  localSplit.z.connectTo(absZ.input);

  const edgeDist = new MaxBlock("edge distance");
  absX.output.connectTo(edgeDist.left);
  absZ.output.connectTo(edgeDist.right);

  const borderEdgeStart = floatInput("border edge start", 3.85);
  const borderEdgeEnd = floatInput("border edge end", 4.0);
  const edgeFactor = new SmoothStepBlock("edge factor");
  edgeDist.output.connectTo(edgeFactor.value);
  borderEdgeStart.output.connectTo(edgeFactor.edge0);
  borderEdgeEnd.output.connectTo(edgeFactor.edge1);

  // Dashed pattern carved into the border band for a futuristic/tech look.
  // Phase is taken along the diagonal (localX + localZ), which gives
  // perimeter-parallel dash segments on every side of the box — with a
  // chevron-like twist at corners where the dashes switch orientation.
  const dashCoord = new AddBlock("dash coord");
  localSplit.x.connectTo(dashCoord.left);
  localSplit.z.connectTo(dashCoord.right);

  const dashFrequency = floatInput("dash frequency", 1.5);
  const dashScaled = new MultiplyBlock("dash scaled");
  dashCoord.output.connectTo(dashScaled.left);
  dashFrequency.output.connectTo(dashScaled.right);

  const dashPhase = new TrigonometryBlock("dash phase");
  dashPhase.operation = TrigonometryBlockOperations.Fract;
  dashScaled.output.connectTo(dashPhase.input);

  const dashDuty = floatInput("dash duty", 0.45);
  const dashMask = new StepBlock("dash mask");
  dashDuty.output.connectTo(dashMask.edge);
  dashPhase.output.connectTo(dashMask.value);

  const borderPattern = new MultiplyBlock("border pattern");
  edgeFactor.output.connectTo(borderPattern.left);
  dashMask.output.connectTo(borderPattern.right);

  const borderColor = color3Input("Border color", 0x92 / 255, 0x32 / 255, 0x1B / 255);
  const tintedDiffuse = new LerpBlock("Tinted diffuse");
  diffuseTex.rgb.connectTo(tintedDiffuse.left);
  borderColor.output.connectTo(tintedDiffuse.right);
  borderPattern.output.connectTo(tintedDiffuse.gradient);

  // ========== PerturbNormal ==========
  const perturbNormal = new PerturbNormalBlock("Perturb normal");
  worldPos.output.connectTo(perturbNormal.worldPosition);
  worldNormal.output.connectTo(perturbNormal.worldNormal);
  scaledUv.output.connectTo(perturbNormal.uv);
  normalTex.rgb.connectTo(perturbNormal.normalMapColor);
  floatInput("bump strength", 1).output.connectTo(perturbNormal.strength);

  // ========== Specular / glossiness ==========
  const specularColor = color3Input("Specular color", 0.05, 0.05, 0.05);
  const glossiness = floatInput("Glossiness", 0.1);
  const glossPower = floatInput("Gloss power", 64);

  // ========== Light block ==========
  const light = new LightBlock("Lights");
  worldPos.output.connectTo(light.worldPosition);
  perturbNormal.output.connectTo(light.worldNormal);
  cameraPosition.output.connectTo(light.cameraPosition);
  glossiness.output.connectTo(light.glossiness);
  glossPower.output.connectTo(light.glossPower);
  tintedDiffuse.output.connectTo(light.diffuseColor);
  specularColor.output.connectTo(light.specularColor);

  const addLighting = new AddBlock("Add lighting");
  light.diffuseOutput.connectTo(addLighting.left);
  light.specularOutput.connectTo(addLighting.right);

  // Ambient baseline keeps the surface visible if no directional light
  // reaches the mesh (e.g. transient gaps between addToScene and the
  // LightBlock binding the light to this mesh via includedOnlyMeshes).
  const ambientStrength = floatInput("ambient strength", 0.35);
  const ambient = new ScaleBlock("Ambient");
  tintedDiffuse.output.connectTo(ambient.input);
  ambientStrength.output.connectTo(ambient.factor);

  const finalColor = new AddBlock("Final color");
  ambient.output.connectTo(finalColor.left);
  addLighting.output.connectTo(finalColor.right);

  // ========== Fragment output ==========
  const fragmentOutput = new FragmentOutputBlock("FragmentOutput");
  finalColor.output.connectTo(fragmentOutput.rgb);

  // ========== Build ==========
  mat.addOutputNode(vertexOutput);
  mat.addOutputNode(fragmentOutput);
  mat.build();

  return mat;
}
