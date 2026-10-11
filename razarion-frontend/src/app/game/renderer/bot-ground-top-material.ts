import {NodeMaterial} from "@babylonjs/core/Materials/Node/nodeMaterial";
import {Texture} from "@babylonjs/core/Materials/Textures/texture";
import {Color3} from "@babylonjs/core/Maths/math.color";
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
import {LerpBlock} from "@babylonjs/core/Materials/Node/Blocks/lerpBlock";
import {TrigonometryBlock, TrigonometryBlockOperations} from "@babylonjs/core/Materials/Node/Blocks/trigonometryBlock";
import {MaxBlock} from "@babylonjs/core/Materials/Node/Blocks/maxBlock";
import {SmoothStepBlock} from "@babylonjs/core/Materials/Node/Blocks/smoothStepBlock";
import {StepBlock} from "@babylonjs/core/Materials/Node/Blocks/stepBlock";
import {OneMinusBlock} from "@babylonjs/core/Materials/Node/Blocks/oneMinusBlock";
import {MultiplyBlock} from "@babylonjs/core/Materials/Node/Blocks/multiplyBlock";
import {SubtractBlock} from "@babylonjs/core/Materials/Node/Blocks/subtractBlock";
import {Vector2, Vector3} from "@babylonjs/core/Maths/math.vector";
import {NormalizeBlock} from "@babylonjs/core/Materials/Node/Blocks/normalizeBlock";
import {Showcase} from "./showcase";
import type {BotGroundMarkings} from "./bot-ground-markings";
import type {Scene} from "@babylonjs/core/scene";
import {cloudShadeNode, cloudTimeInput} from "./cloud-shadow-node";

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
export function buildBotGroundTopMaterial(scene: Scene, markings: BotGroundMarkings): NodeMaterial {
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
  diffuseTex.texture = new Texture(TEX_PATH + "bot-ground-top-diffuse.webp", scene);

  // The photo carries large dark smudges, bands and tyre arcs. Repeating every 25 m they landed as
  // dark streaks along the plate edges. High pass: keep the grain (texture minus a blurred mip),
  // put it on the texture's mean colour (its 1 x 1 mip); stains and marks come from the markings.
  const blurredTex = new TextureBlock("BotGround top diffuse blurred");
  scaledUv.output.connectTo(blurredTex.uv);
  floatInput("blur lod", 5).output.connectTo(blurredTex.lod);
  blurredTex.texture = diffuseTex.texture;
  const meanTex = new TextureBlock("BotGround top diffuse mean");
  scaledUv.output.connectTo(meanTex.uv);
  floatInput("mean lod", 12).output.connectTo(meanTex.lod);
  meanTex.texture = diffuseTex.texture;
  const grain = new SubtractBlock("asphalt grain");
  diffuseTex.rgb.connectTo(grain.left);
  blurredTex.rgb.connectTo(grain.right);
  const highPassed = new AddBlock("asphalt high passed");
  grain.output.connectTo(highPassed.left);
  meanTex.rgb.connectTo(highPassed.right);
  // 0 = the raw photo as before (showcase.ts films the before/after)
  const asphalt = new LerpBlock("asphalt");
  diffuseTex.rgb.connectTo(asphalt.left);
  highPassed.output.connectTo(asphalt.right);
  floatInput("grain strength", 1).output.connectTo(asphalt.gradient);

  const normalTex = new TextureBlock("BotGround top norm");
  scaledUv.output.connectTo(normalTex.uv);
  normalTex.texture = new Texture(TEX_PATH + "bot-ground-top-norm.webp", scene);

  // ========== Painted markings (bot-ground-markings.ts) ==========
  // One baked texture per bot ground over its whole footprint: stripes along the outline of the
  // plate, joints, stains and the emblem. Sampled by world XZ, so neighbouring boxes of one plate
  // share one picture instead of each box drawing its own border.
  const markingOrigin = new InputBlock("markings origin", undefined, NodeMaterialBlockConnectionPointTypes.Vector2);
  markingOrigin.value = new Vector2(markings.originX, markings.originZ);
  const markingInvSize = new InputBlock("markings inverse size", undefined, NodeMaterialBlockConnectionPointTypes.Vector2);
  markingInvSize.value = new Vector2(1 / markings.sizeX, 1 / markings.sizeZ);
  const markingLocal = new SubtractBlock("markings local");
  posXZ.xy.connectTo(markingLocal.left);
  markingOrigin.output.connectTo(markingLocal.right);
  const markingUv = new MultiplyBlock("markings uv");
  markingLocal.output.connectTo(markingUv.left);
  markingInvSize.output.connectTo(markingUv.right);

  const markingTex = new TextureBlock("BotGround markings");
  markingUv.output.connectTo(markingTex.uv);
  markingTex.texture = markings.texture;

  // The paint is premultiplied: asphalt * (1 - alpha) + paint. Strength 0 = no markings (showcase).
  const markingStrength = floatInput("markings strength", 1);
  const markingAlpha = new ScaleBlock("markings alpha");
  markingTex.a.connectTo(markingAlpha.input);
  markingStrength.output.connectTo(markingAlpha.factor);
  const markingPaint = new ScaleBlock("markings paint");
  markingTex.rgb.connectTo(markingPaint.input);
  markingStrength.output.connectTo(markingPaint.factor);
  const uncovered = new OneMinusBlock("markings uncovered");
  markingAlpha.output.connectTo(uncovered.input);
  const asphaltLeft = new ScaleBlock("asphalt under markings");
  asphalt.output.connectTo(asphaltLeft.input);
  uncovered.output.connectTo(asphaltLeft.factor);
  const painted = new AddBlock("painted");
  asphaltLeft.output.connectTo(painted.left);
  markingPaint.output.connectTo(painted.right);

  // ========== The old per-box border (showcase only) ==========
  // Before the markings every 8 m box drew its own dashed border in its local XZ, which read as a
  // grid. Kept at strength 0 so a recording can show the plates as they were.
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
  const edgeFactor = new SmoothStepBlock("edge factor");
  edgeDist.output.connectTo(edgeFactor.value);
  floatInput("border edge start", 3.85).output.connectTo(edgeFactor.edge0);
  floatInput("border edge end", 4.0).output.connectTo(edgeFactor.edge1);
  const dashCoord = new AddBlock("dash coord");
  localSplit.x.connectTo(dashCoord.left);
  localSplit.z.connectTo(dashCoord.right);
  const dashScaled = new ScaleBlock("dash scaled");
  dashCoord.output.connectTo(dashScaled.input);
  floatInput("dash frequency", 1.5).output.connectTo(dashScaled.factor);
  const dashPhase = new TrigonometryBlock("dash phase");
  dashPhase.operation = TrigonometryBlockOperations.Fract;
  dashScaled.output.connectTo(dashPhase.input);
  const dashMask = new StepBlock("dash mask");
  floatInput("dash duty", 0.45).output.connectTo(dashMask.edge);
  dashPhase.output.connectTo(dashMask.value);
  const borderPattern = new MultiplyBlock("border pattern");
  edgeFactor.output.connectTo(borderPattern.left);
  dashMask.output.connectTo(borderPattern.right);
  const oldBorder = new ScaleBlock("old border");
  borderPattern.output.connectTo(oldBorder.input);
  floatInput("old border strength", 0).output.connectTo(oldBorder.factor);
  const tintedDiffuse = new LerpBlock("Tinted diffuse");
  painted.output.connectTo(tintedDiffuse.left);
  color3Input("Border color", 0x92 / 255, 0x32 / 255, 0x1B / 255).output.connectTo(tintedDiffuse.right);
  oldBorder.output.connectTo(tintedDiffuse.gradient);

  // ========== Light normal ==========
  // Lit as if tilted half as much: a ramp facing away from the sun got only half the sun of the flat
  // plate and read as a dark hole. Flat boxes are unchanged (their normal is up already).
  const normalSplit = new VectorSplitterBlock("World normal splitter");
  worldNormal.output.connectTo(normalSplit.xyzw);
  const up = new InputBlock("up", undefined, NodeMaterialBlockConnectionPointTypes.Vector3);
  up.value = new Vector3(0, 1, 0);
  const halfTilted = new LerpBlock("half tilted normal");
  normalSplit.xyzOut.connectTo(halfTilted.left);
  up.output.connectTo(halfTilted.right);
  floatInput("light normal flattening", 0.5).output.connectTo(halfTilted.gradient);
  const halfTiltedUnit = new NormalizeBlock("half tilted normal unit");
  halfTilted.output.connectTo(halfTiltedUnit.input);
  const lightNormal = new VectorMergerBlock("light normal");
  halfTiltedUnit.output.connectTo(lightNormal.xyzIn);
  floatInput("light normal w", 0).output.connectTo(lightNormal.w);

  // ========== PerturbNormal ==========
  const perturbNormal = new PerturbNormalBlock("Perturb normal");
  worldPos.output.connectTo(perturbNormal.worldPosition);
  lightNormal.xyzw.connectTo(perturbNormal.worldNormal);
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

  // The sun goes under the drifting clouds like on the ground around (cloud-shadow-node.ts)
  const cloudShade = cloudShadeNode(posXZ.xy, cloudTimeInput(scene, mat, "cloud time").output, "bot cloud shadow strength");
  const sunDiffuse = new ScaleBlock("sun diffuse");
  light.diffuseOutput.connectTo(sunDiffuse.input);
  cloudShade.connectTo(sunDiffuse.factor);
  const sunSpecular = new ScaleBlock("sun specular");
  light.specularOutput.connectTo(sunSpecular.input);
  cloudShade.connectTo(sunSpecular.factor);
  const addLighting = new AddBlock("Add lighting");
  sunDiffuse.output.connectTo(addLighting.left);
  sunSpecular.output.connectTo(addLighting.right);

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
  Showcase.applyToGround(mat);

  return mat;
}
