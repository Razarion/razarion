import {InputBlock} from "@babylonjs/core/Materials/Node/Blocks/Input/inputBlock";
import {NodeMaterialBlockConnectionPointTypes} from "@babylonjs/core/Materials/Node/Enums/nodeMaterialBlockConnectionPointTypes";
import {LightBlock} from "@babylonjs/core/Materials/Node/Blocks/Dual/lightBlock";
import {AddBlock} from "@babylonjs/core/Materials/Node/Blocks/addBlock";
import {MultiplyBlock} from "@babylonjs/core/Materials/Node/Blocks/multiplyBlock";
import {ScaleBlock} from "@babylonjs/core/Materials/Node/Blocks/scaleBlock";
import {LerpBlock} from "@babylonjs/core/Materials/Node/Blocks/lerpBlock";
import {Color3} from "@babylonjs/core/Maths/math.color";
import type {NodeMaterialConnectionPoint} from "@babylonjs/core/Materials/Node/nodeMaterialBlockConnectionPoint";

/** What the terrain light needs from a material graph. */
export interface TerrainLightInputs {
  worldPosition: NodeMaterialConnectionPoint;
  cameraPosition: NodeMaterialConnectionPoint;
  /** The normal the sun sees: with the normal map. */
  litNormal: NodeMaterialConnectionPoint;
  /** Up component of the shape's normal, before the normal map: picks sky or ground bounce. */
  normalY: NodeMaterialConnectionPoint;
  diffuse: NodeMaterialConnectionPoint;
  specular: NodeMaterialConnectionPoint;
  glossiness: NodeMaterialConnectionPoint;
  glossPower: NodeMaterialConnectionPoint;
  /** 0..1, how much sky a point sees: the relief on the ground, the baked occlusion on a rock. */
  skyVisibility: NodeMaterialConnectionPoint;
  /** 0..1 from cloudShadeNode: what the clouds leave of the sun. */
  cloudShade: NodeMaterialConnectionPoint;
}

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
 * The light of the terrain: the sun (dimmed by clouds and a little where the sky is hidden) plus a sky
 * light, cool from above and warm bounce from below. The ground (ground-material.ts) and the rock models
 * (rock-material.ts) both go through this, so a boulder at a cliff is lit as the cliff is. The block names
 * are what the Showcase switches ("sun occlusion", "ambient intensity").
 */
export function terrainLight(i: TerrainLightInputs): NodeMaterialConnectionPoint {
  const light = new LightBlock("Lights");
  i.worldPosition.connectTo(light.worldPosition);
  i.litNormal.connectTo(light.worldNormal);
  i.cameraPosition.connectTo(light.cameraPosition);
  i.glossiness.connectTo(light.glossiness);
  i.glossPower.connectTo(light.glossPower);
  i.diffuse.connectTo(light.diffuseColor);
  i.specular.connectTo(light.specularColor);

  // Sun: dimmed under clouds, and a little where the sky is hidden (the sky visibility also stands for
  // the light that the land around a hollow blocks at grazing angles).
  const skyForSun = new LerpBlock("sky for sun");
  floatInput("one", 1).output.connectTo(skyForSun.left);
  i.skyVisibility.connectTo(skyForSun.right);
  floatInput("sun occlusion", 0.4).output.connectTo(skyForSun.gradient);
  const sunShade = new MultiplyBlock("sun shade");
  i.cloudShade.connectTo(sunShade.left);
  skyForSun.output.connectTo(sunShade.right);
  const sunDiffuse = new ScaleBlock("sun diffuse");
  light.diffuseOutput.connectTo(sunDiffuse.input);
  sunShade.output.connectTo(sunDiffuse.factor);
  const sunSpecular = new ScaleBlock("sun specular");
  light.specularOutput.connectTo(sunSpecular.input);
  i.cloudShade.connectTo(sunSpecular.factor);

  // Sky light: cool from above, warm bounce from below, held back where the sky is hidden.
  const hemiGradient = new ScaleBlock("hemi gradient");
  i.normalY.connectTo(hemiGradient.input);
  floatInput("half", 0.5).output.connectTo(hemiGradient.factor);
  const hemiGradientOffset = new AddBlock("hemi gradient offset");
  hemiGradient.output.connectTo(hemiGradientOffset.left);
  floatInput("half offset", 0.5).output.connectTo(hemiGradientOffset.right);
  const hemiColor = new LerpBlock("hemi color");
  color3Input("ground bounce", 0.32, 0.27, 0.20).output.connectTo(hemiColor.left);
  color3Input("sky color", 0.52, 0.53, 0.52).output.connectTo(hemiColor.right);
  hemiGradientOffset.output.connectTo(hemiColor.gradient);
  const ambientLight = new ScaleBlock("ambient light");
  hemiColor.output.connectTo(ambientLight.input);
  i.skyVisibility.connectTo(ambientLight.factor);
  const ambientStrength = new ScaleBlock("ambient strength");
  ambientLight.output.connectTo(ambientStrength.input);
  floatInput("ambient intensity", 0.3).output.connectTo(ambientStrength.factor);
  const ambient = new MultiplyBlock("ambient");
  i.diffuse.connectTo(ambient.left);
  ambientStrength.output.connectTo(ambient.right);

  const sunLight = new AddBlock("sun light");
  sunDiffuse.output.connectTo(sunLight.left);
  sunSpecular.output.connectTo(sunLight.right);
  const lit = new AddBlock("Add");
  sunLight.output.connectTo(lit.left);
  ambient.output.connectTo(lit.right);
  return lit.output;
}
