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
 * Builds the BotGround side NodeMaterial entirely in code.
 *
 * Mirrors the BotGround top material (diffuse + bump + light block + ambient)
 * but drops the faction-colored dashed border — the perimeter tint is only
 * meaningful on the top face. Side faces use the mesh's standard box UV
 * attribute so each vertical face tiles the texture independently, which
 * avoids the top's world-XZ trick breaking on vertical geometry.
 */
export function buildBotGroundSideMaterial(scene: Scene): NodeMaterial {
  const mat = new NodeMaterial("BotGround side", scene);

  // ========== Vertex attributes ==========
  const position = new InputBlock("position");
  position.setAsAttribute("position");
  const normal = new InputBlock("normal");
  normal.setAsAttribute("normal");
  const uv = new InputBlock("uv");
  uv.setAsAttribute("uv");

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

  // ========== UV ==========
  const uvScale = floatInput("uv scale", 1.0);
  const scaledUv = new ScaleBlock("Scale uv");
  uv.output.connectTo(scaledUv.input);
  uvScale.output.connectTo(scaledUv.factor);

  // ========== Diffuse + bump ==========
  const diffuseTex = new TextureBlock("BotGround side diffuse");
  scaledUv.output.connectTo(diffuseTex.uv);
  diffuseTex.texture = new Texture(TEX_PATH + "bot-ground-side-diffuse.jpg", scene);

  const normalTex = new TextureBlock("BotGround side norm");
  scaledUv.output.connectTo(normalTex.uv);
  normalTex.texture = new Texture(TEX_PATH + "bot-ground-side-norm.png", scene);

  // ========== PerturbNormal ==========
  // invertY flips the green channel of the normal map. The source PNG is
  // authored in DirectX convention (Y-down) while Babylon expects OpenGL
  // (Y-up), so without this flip raised details read as indented and vice
  // versa.
  const perturbNormal = new PerturbNormalBlock("Perturb normal");
  perturbNormal.invertY = true;
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
  diffuseTex.rgb.connectTo(light.diffuseColor);
  specularColor.output.connectTo(light.specularColor);

  const addLighting = new AddBlock("Add lighting");
  light.diffuseOutput.connectTo(addLighting.left);
  light.specularOutput.connectTo(addLighting.right);

  const ambientStrength = floatInput("ambient strength", 0.35);
  const ambient = new ScaleBlock("Ambient");
  diffuseTex.rgb.connectTo(ambient.input);
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
