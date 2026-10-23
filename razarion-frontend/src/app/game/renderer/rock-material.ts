import type {Scene} from "@babylonjs/core/scene";
import type {Material} from "@babylonjs/core/Materials/material";
import type {Mesh} from "@babylonjs/core/Meshes/mesh";
import type {BaseTexture} from "@babylonjs/core/Materials/Textures/baseTexture";
import {Texture} from "@babylonjs/core/Materials/Textures/texture";
import {Color3} from "@babylonjs/core/Maths/math.color";
import {NodeMaterial} from "@babylonjs/core/Materials/Node/nodeMaterial";
import {InputBlock} from "@babylonjs/core/Materials/Node/Blocks/Input/inputBlock";
import {NodeMaterialBlockConnectionPointTypes} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialBlockConnectionPointTypes";
import {NodeMaterialSystemValues} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialSystemValues";
import {TransformBlock} from "@babylonjs/core/Materials/Node/Blocks/transformBlock";
import {InstancesBlock} from "@babylonjs/core/Materials/Node/Blocks/Vertex/instancesBlock";
import {VertexOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Vertex/vertexOutputBlock";
import {FragmentOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/fragmentOutputBlock";
import {PerturbNormalBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/perturbNormalBlock";
import {TextureBlock} from "@babylonjs/core/Materials/Node/Blocks/Dual/textureBlock";
import {TriPlanarBlock} from "@babylonjs/core/Materials/Node/Blocks/triPlanarBlock";
import {AddBlock} from "@babylonjs/core/Materials/Node/Blocks/addBlock";
import {ScaleBlock} from "@babylonjs/core/Materials/Node/Blocks/scaleBlock";
import {LerpBlock} from "@babylonjs/core/Materials/Node/Blocks/lerpBlock";
import {PowBlock} from "@babylonjs/core/Materials/Node/Blocks/powBlock";
import {SmoothStepBlock} from "@babylonjs/core/Materials/Node/Blocks/smoothStepBlock";
import {VectorSplitterBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorSplitterBlock";
import {VectorMergerBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorMergerBlock";
import type {NodeMaterialConnectionPoint} from "@babylonjs/core/Materials/Node/nodeMaterialBlockConnectionPoint";
import {PBRMaterial} from "@babylonjs/core/Materials/PBR/pbrMaterial";
import {cloudShadeNode, cloudTimeInput} from "./cloud-shadow-node";
import {terrainLight} from "./terrain-light";
import {Showcase} from "./showcase";

/** The one rock of the cliffs (ground-material.ts) and the rock models (buildRockMaterial). */
export const ROCK = {
  DIFFUSE: "renderer/textures/rock-diffuse.webp",
  NORMAL: "renderer/textures/rock-normal.webp",
  /** 1 / metres per repeat. */
  SCALE: 1 / 5,
  /** Triplanar blend: 1 smears the three projections over every curve, 4 keeps them apart. */
  SHARPNESS: 4,
  MOSS: 1,
  MOSS_EDGE0: 0.72,
  MOSS_EDGE1: 0.95,
  /** Moss patches of ~25 m: the rock texture's height read at a large scale. */
  MOSS_PATCH_SCALE: 1 / 25,
  /** Moss colour, darkened in the rock's cracks by its height. */
  MOSS_COLOR: [0.22, 0.28, 0.11],
  /** Sun highlights on rock - the cliffs' values since before the rock models shared them. */
  GLOSSINESS: 0.36,
  SPECULAR: [0.20, 0.16, 0.16],
};

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

/** Sum of terms, each scaled by its weight, plus a constant. */
function weighted(name: string, terms: [NodeMaterialConnectionPoint, number][], constant: number): NodeMaterialConnectionPoint {
  let sum: NodeMaterialConnectionPoint = floatInput(name + " const", constant).output;
  terms.forEach(([term, weight], i) => {
    const scaled = new ScaleBlock(`${name} term ${i}`);
    term.connectTo(scaled.input);
    floatInput(`${name} weight ${i}`, weight).output.connectTo(scaled.factor);
    const add = new AddBlock(`${name} add ${i}`);
    sum.connectTo(add.left);
    scaled.output.connectTo(add.right);
    sum = add.output;
  });
  return sum;
}

const textures = new WeakMap<Scene, { diffuse: Texture, normal: Texture }>();

function rockTextures(scene: Scene): { diffuse: Texture, normal: Texture } {
  let t = textures.get(scene);
  if (!t) {
    t = {diffuse: new Texture(ROCK.DIFFUSE, scene), normal: new Texture(ROCK.NORMAL, scene)};
    textures.set(scene, t);
  }
  return t;
}

/**
 * The rock's colour and normal map, laid on in world space (triplanar), with moss where it looks up.
 * One graph for cliffs and rock models alike - the Showcase switches its blocks by these names.
 *
 * worldPosition and worldNormal are Vector4 world-space outputs; worldXZ is the position's x and z.
 */
export function rockSurface(scene: Scene, worldPosition: NodeMaterialConnectionPoint, worldNormal: NodeMaterialConnectionPoint,
                            worldXZ: NodeMaterialConnectionPoint): { color: NodeMaterialConnectionPoint, normalMap: NodeMaterialConnectionPoint } {
  const {diffuse, normal} = rockTextures(scene);
  const position = new ScaleBlock("Scale triplanar pos");
  worldPosition.connectTo(position.input);
  floatInput("triplanar scale", ROCK.SCALE).output.connectTo(position.factor);
  const sharpness = floatInput("triplanar sharpness", ROCK.SHARPNESS);

  const color = new TriPlanarBlock("TriPlanar diffuse");
  position.output.connectTo(color.position);
  worldNormal.connectTo(color.normal);
  sharpness.output.connectTo(color.sharpness);
  // Scanned (ambientCG Rock034, CC0) via razarion-ai-content/scripts/rock-texture-scan.mjs, alpha = height
  color.texture = diffuse;

  const normalMap = new TriPlanarBlock("TriPlanar norm");
  position.output.connectTo(normalMap.position);
  worldNormal.connectTo(normalMap.normal);
  sharpness.output.connectTo(normalMap.sharpness);
  normalMap.texture = normal;

  // Moss where the rock looks up, in patches of ~25 m (the rock's height read large), frayed along the
  // rock's relief: it takes the bumps first
  const patchUv = new ScaleBlock("rock moss patch uv");
  worldXZ.connectTo(patchUv.input);
  floatInput("rock moss patch scale", ROCK.MOSS_PATCH_SCALE).output.connectTo(patchUv.factor);
  const patch = new TextureBlock("rock moss patch");
  patchUv.output.connectTo(patch.uv);
  patch.texture = diffuse;
  const up = new VectorSplitterBlock("rock moss normal");
  worldNormal.connectTo(up.xyzw);
  const value = weighted("rock moss value", [[up.y, 1], [patch.a, 0.6], [color.a, 0.7]], -0.9);
  const step = new SmoothStepBlock("rock moss step");
  value.connectTo(step.value);
  floatInput("rock moss edge0", ROCK.MOSS_EDGE0).output.connectTo(step.edge0);
  floatInput("rock moss edge1", ROCK.MOSS_EDGE1).output.connectTo(step.edge1);
  const share = new ScaleBlock("rock moss share");
  step.output.connectTo(share.input);
  floatInput("rock moss strength", ROCK.MOSS).output.connectTo(share.factor);
  const shade = weighted("rock moss shade", [[color.a, 0.6]], 0.7);
  const moss = new ScaleBlock("rock moss");
  color3Input("rock moss color", ROCK.MOSS_COLOR[0], ROCK.MOSS_COLOR[1], ROCK.MOSS_COLOR[2]).output.connectTo(moss.input);
  shade.connectTo(moss.factor);
  const withMoss = new LerpBlock("rock with moss");
  color.rgb.connectTo(withMoss.left);
  moss.output.connectTo(withMoss.right);
  share.output.connectTo(withMoss.gradient);

  return {color: withMoss.output, normalMap: normalMap.rgb};
}

/**
 * A rock model in the cliffs' stone and the cliffs' light: rockSurface for colour and normal map,
 * terrainLight for sun, sky and clouds. Of the glb material only the baked occlusion stays - it is the
 * rock's own sky visibility, as the relief is the ground's. Hardware instanced like every terrain object.
 */
function buildRockMaterial(scene: Scene, original: Material): NodeMaterial {
  const mat = new NodeMaterial(`Rock (${original.name})`, scene);

  const position = new InputBlock("position");
  position.setAsAttribute("position");
  const normal = new InputBlock("normal");
  normal.setAsAttribute("normal");
  const uv = new InputBlock("uv");
  uv.setAsAttribute("uv");

  // The world matrix per instance
  const instances = new InstancesBlock("Instances");
  for (const [i, input] of [instances.world0, instances.world1, instances.world2, instances.world3].entries()) {
    const attribute = new InputBlock(`world${i}`);
    attribute.setAsAttribute(`world${i}`);
    attribute.output.connectTo(input);
  }
  const world = new InputBlock("World");
  world.setAsSystemValue(NodeMaterialSystemValues.World);
  world.output.connectTo(instances.world);
  const viewProjection = new InputBlock("ViewProjection");
  viewProjection.setAsSystemValue(NodeMaterialSystemValues.ViewProjection);
  const cameraPosition = new InputBlock("cameraPosition");
  cameraPosition.setAsSystemValue(NodeMaterialSystemValues.CameraPosition);

  const worldPos = new TransformBlock("WorldPos");
  position.output.connectTo(worldPos.vector);
  instances.output.connectTo(worldPos.transform);
  const worldViewProj = new TransformBlock("WorldPos * ViewProjectionTransform");
  worldPos.output.connectTo(worldViewProj.vector);
  viewProjection.output.connectTo(worldViewProj.transform);
  const vertexOutput = new VertexOutputBlock("VertexOutput");
  worldViewProj.output.connectTo(vertexOutput.vector);

  // A direction: no translation (w = 0)
  const worldNormal = new TransformBlock("World normal");
  worldNormal.complementW = 0;
  normal.output.connectTo(worldNormal.vector);
  instances.output.connectTo(worldNormal.transform);
  const worldNormalSplit = new VectorSplitterBlock("Split world normal");
  worldNormal.output.connectTo(worldNormalSplit.xyzw);
  const worldPosSplit = new VectorSplitterBlock("Split world pos");
  worldPos.output.connectTo(worldPosSplit.xyzw);
  const worldXZ = new VectorMergerBlock("World XZ");
  worldPosSplit.x.connectTo(worldXZ.x);
  worldPosSplit.z.connectTo(worldXZ.y);

  const surface = rockSurface(scene, worldPos.output, worldNormal.output, worldXZ.xy);

  const perturbNormal = new PerturbNormalBlock("Perturb normal");
  worldPos.output.connectTo(perturbNormal.worldPosition);
  worldNormal.output.connectTo(perturbNormal.worldNormal);
  uv.output.connectTo(perturbNormal.uv);
  surface.normalMap.connectTo(perturbNormal.normalMapColor);
  floatInput("strength mountain", 1).output.connectTo(perturbNormal.strength);

  // The baked occlusion of the glb: how much sky the rock's folds see
  const occlusionTexture: BaseTexture | null = original instanceof PBRMaterial ? original.ambientTexture : null;
  let skyVisibility: NodeMaterialConnectionPoint = floatInput("sky visibility", 1).output;
  if (occlusionTexture) {
    const occlusion = new TextureBlock("rock occlusion");
    uv.output.connectTo(occlusion.uv);
    occlusion.texture = occlusionTexture as Texture;
    skyVisibility = occlusion.r;
  }

  const gloss = new PowBlock("Pow");
  floatInput("glossiness mountain", ROCK.GLOSSINESS).output.connectTo(gloss.value);
  floatInput("gloss power exp", 4).output.connectTo(gloss.power);

  const time = cloudTimeInput(scene, mat, "RockTime");
  const lighting = terrainLight({
    worldPosition: worldPos.output, cameraPosition: cameraPosition.output, litNormal: perturbNormal.output,
    normalY: worldNormalSplit.y, diffuse: surface.color,
    specular: color3Input("Specular color mountain", ROCK.SPECULAR[0], ROCK.SPECULAR[1], ROCK.SPECULAR[2]).output,
    glossiness: gloss.output, glossPower: floatInput("Gloss power", 512).output,
    skyVisibility, cloudShade: cloudShadeNode(worldXZ.xy, time.output),
  });

  const fragmentOutput = new FragmentOutputBlock("FragmentOutput");
  lighting.connectTo(fragmentOutput.rgb);

  mat.addOutputNode(vertexOutput);
  mat.addOutputNode(fragmentOutput);
  mat.build();
  Showcase.register(mat);
  return mat;
}

/**
 * The rock models' source meshes with both their materials: the Showcase ("rockMaterial") puts the glb
 * one back. Instances draw with their source mesh's material.
 */
export class RockMaterials {
  private static readonly byOriginal = new Map<Material, NodeMaterial>();
  private static readonly meshes: { mesh: Mesh, original: Material, rock: NodeMaterial }[] = [];

  /** Gives a rock model's source mesh the rock material (one per glb material, shared). */
  static apply(mesh: Mesh): void {
    const original = mesh.material;
    if (!original) {
      return;
    }
    let rock = RockMaterials.byOriginal.get(original);
    if (!rock) {
      rock = buildRockMaterial(mesh.getScene(), original);
      RockMaterials.byOriginal.set(original, rock);
    }
    RockMaterials.meshes.push({mesh, original, rock});
    RockMaterials.use(mesh, Showcase.isOn("rockMaterial") ? rock : original);
  }

  /**
   * The rock material brings the terrain's sky light along (terrain-light.ts), so like the ground it opts
   * out of the scene's sky light (BabylonRenderServiceAccessImpl.SKY_LIGHT_NAME) - lit twice from the sky,
   * the shaded side filled up and the rocks lost their shape and their shadows.
   */
  private static use(mesh: Mesh, material: Material): void {
    mesh.material = material;
    const excluded = mesh.getScene().getLightByName("SkyLight")?.excludedMeshes;
    if (!excluded) {
      return;
    }
    const index = excluded.indexOf(mesh);
    if (material instanceof NodeMaterial && index === -1) {
      excluded.push(mesh);
    } else if (!(material instanceof NodeMaterial) && index !== -1) {
      excluded.splice(index, 1);
    }
  }

  /** Showcase: the rock material on or the glb's own back. */
  static show(on: boolean): void {
    RockMaterials.meshes.forEach(({mesh, original, rock}) => RockMaterials.use(mesh, on ? rock : original));
  }
}

Showcase.listen("rockMaterial", on => RockMaterials.show(on));
