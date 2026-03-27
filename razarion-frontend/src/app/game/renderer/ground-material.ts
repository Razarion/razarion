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
import {TriPlanarBlock} from "@babylonjs/core/Materials/Node/Blocks/triPlanarBlock";
import {AddBlock} from "@babylonjs/core/Materials/Node/Blocks/addBlock";
import {SubtractBlock} from "@babylonjs/core/Materials/Node/Blocks/subtractBlock";
import {MultiplyBlock} from "@babylonjs/core/Materials/Node/Blocks/multiplyBlock";
import {MaxBlock} from "@babylonjs/core/Materials/Node/Blocks/maxBlock";
import {ScaleBlock} from "@babylonjs/core/Materials/Node/Blocks/scaleBlock";
import {NegateBlock} from "@babylonjs/core/Materials/Node/Blocks/negateBlock";
import {LerpBlock} from "@babylonjs/core/Materials/Node/Blocks/lerpBlock";
import {SmoothStepBlock} from "@babylonjs/core/Materials/Node/Blocks/smoothStepBlock";
import {VectorSplitterBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorSplitterBlock";
import {VectorMergerBlock} from "@babylonjs/core/Materials/Node/Blocks/vectorMergerBlock";
import {ClampBlock} from "@babylonjs/core/Materials/Node/Blocks/clampBlock";
import {OneMinusBlock} from "@babylonjs/core/Materials/Node/Blocks/oneMinusBlock";
import {PowBlock} from "@babylonjs/core/Materials/Node/Blocks/powBlock";
import {GradientBlock, GradientBlockColorStep} from "@babylonjs/core/Materials/Node/Blocks/gradientBlock";
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
 * Builds the Ground NodeMaterial entirely in code.
 * The GroundUtility texture must be set externally after creation via getBlockByName("GroundUtility").
 */
export function buildGroundMaterial(scene: Scene): NodeMaterial {
  const mat = new NodeMaterial("Ground", scene);

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

  // ========== Height extraction ==========
  const vectorSplitter = new VectorSplitterBlock("VectorSplitter");
  position.output.connectTo(vectorSplitter.xyzIn);

  // ========== UV scales ==========
  const uvScaleMountain = floatInput("uv scale mountain", 6.39);
  const uvMountain = new ScaleBlock("Scale uv mountain");
  uv.output.connectTo(uvMountain.input);
  uvScaleMountain.output.connectTo(uvMountain.factor);

  const uvScaleGroundUnder = floatInput("uv scale ground under", 20);
  const uvGroundUnder = new ScaleBlock("Scale uv ground under");
  uv.output.connectTo(uvGroundUnder.input);
  uvScaleGroundUnder.output.connectTo(uvGroundUnder.factor);

  const uvScaleBeachVal = floatInput("uv scale beach", 4);
  const uvBeach = new ScaleBlock("uv scale beach scale");
  uv.output.connectTo(uvBeach.input);
  uvScaleBeachVal.output.connectTo(uvBeach.factor);

  const uvScaleGroundUpper = floatInput("uv scale ground upper", 20);
  const uvGroundUpper = new ScaleBlock("Scale uv ground upper");
  uv.output.connectTo(uvGroundUpper.input);
  uvScaleGroundUpper.output.connectTo(uvGroundUpper.factor);

  const uvSplatterScale = floatInput("uv beach splatter", 2);
  const uvSplatter = new ScaleBlock("Scale splatter");
  uv.output.connectTo(uvSplatter.input);
  uvSplatterScale.output.connectTo(uvSplatter.factor);

  const uvHeightScale = floatInput("uv height scale", 1.8);
  const uvHeight = new ScaleBlock("Scale uv height");
  uv.output.connectTo(uvHeight.input);
  uvHeightScale.output.connectTo(uvHeight.factor);

  // TriPlanar position = WorldPos * 0.22
  const triPlanarScale = floatInput("triplanar scale", 0.22);
  const triPlanarPos = new ScaleBlock("Scale triplanar pos");
  worldPos.output.connectTo(triPlanarPos.input);
  triPlanarScale.output.connectTo(triPlanarPos.factor);

  // ========== GroundUtility texture (set externally) ==========
  const groundUtility = new TextureBlock("GroundUtility");
  uv.output.connectTo(groundUtility.uv);
  // Texture set at runtime by BabylonTerrainTileImpl

  // Mountain factor = GroundUtility.r * 1.0
  const mountainBlendFactor = floatInput("mountain blend factor", 1);
  const mountainBlend = new ScaleBlock("Scale mountain blend");
  groundUtility.r.connectTo(mountainBlend.input);
  mountainBlendFactor.output.connectTo(mountainBlend.factor);

  // ========== Beach detection ==========
  // Splatter texture — large scale (overall shape)
  const splatterTex = new TextureBlock("Splatter texture");
  uvSplatter.output.connectTo(splatterTex.uv);
  splatterTex.texture = new Texture(TEX_PATH + "ground-splatter.jpg", scene);

  // Splatter texture — small scale (fine fringe detail)
  const uvSplatterFine = new ScaleBlock("Scale splatter fine");
  uv.output.connectTo(uvSplatterFine.input);
  floatInput("uv splatter fine", 16).output.connectTo(uvSplatterFine.factor);

  const splatterTexFine = new TextureBlock("Splatter texture fine");
  uvSplatterFine.output.connectTo(splatterTexFine.uv);
  splatterTexFine.texture = new Texture(TEX_PATH + "ground-splatter.jpg", scene);

  // Combine: average of large + fine for organic fringed edges
  const splatterCombined = new AddBlock("Add splatter layers");
  splatterTex.r.connectTo(splatterCombined.left);
  splatterTexFine.r.connectTo(splatterCombined.right);

  const splatterAvg = new ScaleBlock("Avg splatter");
  splatterCombined.output.connectTo(splatterAvg.input);
  floatInput("splatter avg", 0.5).output.connectTo(splatterAvg.factor);

  // (splatterAvg - 0.4) * 0.6 + position.y
  const splatterOffset = floatInput("splatter offset", 0.4);
  const splatterSub = new SubtractBlock("Subtract splatter");
  splatterAvg.output.connectTo(splatterSub.left);
  splatterOffset.output.connectTo(splatterSub.right);

  const splatterMul = floatInput("splatter mul", 0.6);
  const splatterScaled = new MultiplyBlock("Multiply splatter");
  splatterSub.output.connectTo(splatterScaled.left);
  splatterMul.output.connectTo(splatterScaled.right);

  // Height bias: amplify height influence so higher = more grass, lower = more sand
  const heightBias = new ScaleBlock("Height bias");
  vectorSplitter.y.connectTo(heightBias.input);
  floatInput("height bias factor", 1.2).output.connectTo(heightBias.factor);

  const beachValue = new AddBlock("Add beach value");
  splatterScaled.output.connectTo(beachValue.left);
  heightBias.output.connectTo(beachValue.right);

  const beachEdge0 = floatInput("beach edge0", 0.23);
  const beachEdge1 = floatInput("beach edge1", 0.30);
  const beachStep = new SmoothStepBlock("Smooth step beach");
  beachValue.output.connectTo(beachStep.value);
  beachEdge0.output.connectTo(beachStep.edge0);
  beachEdge1.output.connectTo(beachStep.edge1);
  // beachStep: 0 = beach, 1 = land

  // ========== Height blending (ground upper/under) ==========
  const heightTex = new TextureBlock("Height texture");
  uvHeight.output.connectTo(heightTex.uv);
  heightTex.texture = new Texture(TEX_PATH + "ground-height.jpg", scene);

  const heightEdge0 = floatInput("height edge0", 0.5);
  const heightEdge1 = floatInput("height edge1", 0.56);
  const heightStep = new SmoothStepBlock("Smooth step height");
  heightTex.r.connectTo(heightStep.value);
  heightEdge0.output.connectTo(heightStep.edge0);
  heightEdge1.output.connectTo(heightStep.edge1);

  // ========== Diffuse textures ==========
  const groundUpperDiffuse = new TextureBlock("Ground upper");
  uvGroundUpper.output.connectTo(groundUpperDiffuse.uv);
  groundUpperDiffuse.texture = new Texture(TEX_PATH + "ground-upper-diffuse.jpg", scene);

  const groundUnderDiffuse = new TextureBlock("Ground under");
  uvGroundUnder.output.connectTo(groundUnderDiffuse.uv);
  groundUnderDiffuse.texture = new Texture(TEX_PATH + "ground-under-diffuse.jpg", scene);

  const mountainDiffuseTriplanar = new TriPlanarBlock("TriPlanar diffuse");
  triPlanarPos.output.connectTo(mountainDiffuseTriplanar.position);
  worldNormal.output.connectTo(mountainDiffuseTriplanar.normal);
  mountainDiffuseTriplanar.texture = new Texture(TEX_PATH + "ground-mountain-diffuse-triplanar.jpg", scene);

  // Lerp ground upper/under by height
  const diffuseHeightLerp = new LerpBlock("Lerp diffuse height");
  groundUpperDiffuse.rgb.connectTo(diffuseHeightLerp.left);
  groundUnderDiffuse.rgb.connectTo(diffuseHeightLerp.right);
  heightStep.output.connectTo(diffuseHeightLerp.gradient);

  // Lerp ground/mountain by mountainBlend
  const diffuseMountainLerp = new LerpBlock("Lerp diffuse mountain");
  diffuseHeightLerp.output.connectTo(diffuseMountainLerp.left);
  mountainDiffuseTriplanar.rgb.connectTo(diffuseMountainLerp.right);
  mountainBlend.output.connectTo(diffuseMountainLerp.gradient);

  // ========== Underwater depth gradient ==========
  const heightScaleFactor = floatInput("underwaterDepthScale", 0.1);
  const underwaterHeightScale = new ScaleBlock("Underwater height scale");
  vectorSplitter.y.connectTo(underwaterHeightScale.input);
  heightScaleFactor.output.connectTo(underwaterHeightScale.factor);

  const underwaterNegate = new NegateBlock("Underwater negate");
  underwaterHeightScale.output.connectTo(underwaterNegate.value);
  // y=0 → 0, y=-3 → 0.3, y=-10 → 1.0

  const underwaterGradient = new GradientBlock("Underwater gradient");
  underwaterNegate.output.connectTo(underwaterGradient.gradient);
  underwaterGradient.colorSteps = [
    new GradientBlockColorStep(0.0, new Color3(0.906, 0.847, 0.792)),   // y=0: sand
    new GradientBlockColorStep(0.03, new Color3(0.85, 0.78, 0.70)),     // y=-0.3: darker sand
    new GradientBlockColorStep(0.07, new Color3(0.55, 0.75, 0.72)),     // y=-0.7: sand-to-water
    new GradientBlockColorStep(0.15, new Color3(0.30, 0.55, 0.60)),     // y=-1.5: blue-green
    new GradientBlockColorStep(0.60, new Color3(0.10, 0.25, 0.40)),     // y=-6: dark blue
    new GradientBlockColorStep(1.0, new Color3(0.012, 0.004, 0.004)),   // y=-10: deep dark
  ];

  // Underwater step: smoothstep on position.y, transition at 0 to 0.1
  // underwaterStep: 0 = underwater, 1 = above water
  const underwaterStep = new SmoothStepBlock("Smooth step underwater");
  vectorSplitter.y.connectTo(underwaterStep.value);
  floatInput("underwater edge0", 0.0).output.connectTo(underwaterStep.edge0);
  floatInput("underwater edge1", 0.1).output.connectTo(underwaterStep.edge1);

  // Beach diffuse texture
  const beachDiffuse = new TextureBlock("Beach diffuse");
  uvBeach.output.connectTo(beachDiffuse.uv);
  beachDiffuse.texture = new Texture(TEX_PATH + "ground-beach-diffuse.png", scene);

  // Darken sand near waterline (wet sand effect)
  const beachDiffuseWet = new ScaleBlock("beach diffuse wet");
  beachDiffuse.rgb.connectTo(beachDiffuseWet.input);
  floatInput("wet sand darken", 0.9).output.connectTo(beachDiffuseWet.factor);

  const wetDiffuseStep = new SmoothStepBlock("wet diffuse step");
  vectorSplitter.y.connectTo(wetDiffuseStep.value);
  floatInput("wet diffuse e0", 0.15).output.connectTo(wetDiffuseStep.edge0);
  floatInput("wet diffuse e1", -0.5).output.connectTo(wetDiffuseStep.edge1);

  const beachDiffuseBlended = new LerpBlock("Lerp beach dry/wet");
  beachDiffuse.rgb.connectTo(beachDiffuseBlended.left);
  beachDiffuseWet.output.connectTo(beachDiffuseBlended.right);
  wetDiffuseStep.output.connectTo(beachDiffuseBlended.gradient);

  // Grass edge shadow — thin dark strip on sand side to simulate raised grass
  // beachStep 0.05–0.4: shadow on sand right next to grass
  const grassShadowBand = new SmoothStepBlock("grass shadow band");
  beachStep.output.connectTo(grassShadowBand.value);
  floatInput("grass shadow lo", 0.0).output.connectTo(grassShadowBand.edge0);
  floatInput("grass shadow hi", 0.3).output.connectTo(grassShadowBand.edge1);

  const grassShadowDarken = new ScaleBlock("grass shadow darken");
  beachDiffuseBlended.output.connectTo(grassShadowDarken.input);
  floatInput("grass shadow amount", 0.6).output.connectTo(grassShadowDarken.factor);

  const beachWithShadow = new LerpBlock("Lerp beach with shadow");
  beachDiffuseBlended.output.connectTo(beachWithShadow.left);
  grassShadowDarken.output.connectTo(beachWithShadow.right);
  grassShadowBand.output.connectTo(beachWithShadow.gradient);

  // Lerp beach/land by beachStep
  const diffuseLand = new LerpBlock("Lerp diffuse land");
  beachWithShadow.output.connectTo(diffuseLand.left);
  diffuseMountainLerp.output.connectTo(diffuseLand.right);
  beachStep.output.connectTo(diffuseLand.gradient);

  // Blend sand texture into shallow underwater area before it transitions to deep gradient
  // shallowSandStep: 1 near surface (y=0), 0 at depth (y=-1)
  const shallowSandStep = new SmoothStepBlock("Smooth step shallow sand");
  vectorSplitter.y.connectTo(shallowSandStep.value);
  floatInput("shallow sand edge0", -1.0).output.connectTo(shallowSandStep.edge0);
  floatInput("shallow sand edge1", -0.1).output.connectTo(shallowSandStep.edge1);

  const underwaterWithSand = new LerpBlock("Lerp underwater sand");
  underwaterGradient.output.connectTo(underwaterWithSand.left);
  beachDiffuseWet.output.connectTo(underwaterWithSand.right);
  shallowSandStep.output.connectTo(underwaterWithSand.gradient);

  // Lerp underwater/land by underwaterStep
  const diffuseFinal = new LerpBlock("Lerp diffuse final");
  underwaterWithSand.output.connectTo(diffuseFinal.left);
  diffuseLand.output.connectTo(diffuseFinal.right);
  underwaterStep.output.connectTo(diffuseFinal.gradient);

  // ========== Normal map textures ==========
  const beachNorm = new TextureBlock("Beach texture");
  uvBeach.output.connectTo(beachNorm.uv);
  beachNorm.texture = new Texture(TEX_PATH + "ground-beach-norm.png", scene);

  const groundUpperNorm = new TextureBlock("Ground upper norm");
  uvGroundUnder.output.connectTo(groundUpperNorm.uv);
  groundUpperNorm.texture = new Texture(TEX_PATH + "ground-upper-norm.jpg", scene);

  const groundUnderNorm = new TextureBlock("Ground under norm");
  uvGroundUnder.output.connectTo(groundUnderNorm.uv);
  groundUnderNorm.texture = new Texture(TEX_PATH + "ground-under-norm.jpg", scene);

  const mountainNormTriplanar = new TriPlanarBlock("TriPlanar norm");
  triPlanarPos.output.connectTo(mountainNormTriplanar.position);
  worldNormal.output.connectTo(mountainNormTriplanar.normal);
  mountainNormTriplanar.texture = new Texture(TEX_PATH + "ground-mountain-norm-triplanar.jpg", scene);

  // Lerp upper/under normals by height
  const normHeightLerp = new LerpBlock("Lerp norm height");
  groundUpperNorm.rgb.connectTo(normHeightLerp.left);
  groundUnderNorm.rgb.connectTo(normHeightLerp.right);
  heightStep.output.connectTo(normHeightLerp.gradient);

  // Lerp ground/mountain normals
  const normMountainLerp = new LerpBlock("Lerp norm mountain");
  normHeightLerp.output.connectTo(normMountainLerp.left);
  mountainNormTriplanar.rgb.connectTo(normMountainLerp.right);
  mountainBlend.output.connectTo(normMountainLerp.gradient);

  // Lerp beach/land normals
  const normBeachLand = new LerpBlock("Lerp norm beach/land");
  beachNorm.rgb.connectTo(normBeachLand.left);
  normMountainLerp.output.connectTo(normBeachLand.right);
  beachStep.output.connectTo(normBeachLand.gradient);

  const normFinal = normBeachLand;

  // ========== UV for PerturbNormal ==========
  // Lerp(Lerp(uvMountain, uvGroundUnder, mountainBlend), uvBeach, beachStep)
  const uvLerpMountainGround = new LerpBlock("Lerp uv mountain/ground");
  uvMountain.output.connectTo(uvLerpMountainGround.left);
  uvGroundUnder.output.connectTo(uvLerpMountainGround.right);
  mountainBlend.output.connectTo(uvLerpMountainGround.gradient);

  const uvFinal = new LerpBlock("Lerp uv final");
  uvLerpMountainGround.output.connectTo(uvFinal.left);
  uvBeach.output.connectTo(uvFinal.right);
  beachStep.output.connectTo(uvFinal.gradient);

  // ========== Bump strength ==========
  const strengthBeach = floatInput("strength beach", 0.44);
  const strengthGround = floatInput("strength ground", 0.28);
  const strengthMountain = floatInput("strength mountain", 1.5);

  const strengthLerpGM = new LerpBlock("Lerp strength ground/mountain");
  strengthGround.output.connectTo(strengthLerpGM.left);
  strengthMountain.output.connectTo(strengthLerpGM.right);
  mountainBlend.output.connectTo(strengthLerpGM.gradient);

  const strengthLand = new LerpBlock("Lerp strength land");
  strengthBeach.output.connectTo(strengthLand.left);
  strengthLerpGM.output.connectTo(strengthLand.right);
  beachStep.output.connectTo(strengthLand.gradient);

  // Underwater: reduce bump strength heavily
  const strengthUnderwater = floatInput("strength underwater", 0.05);
  const strengthFinal = new LerpBlock("Lerp strength final");
  strengthUnderwater.output.connectTo(strengthFinal.left);
  strengthLand.output.connectTo(strengthFinal.right);
  underwaterStep.output.connectTo(strengthFinal.gradient);

  // ========== Wet sand zone — smooth flat sand near waterline ==========
  const shoreUv2 = new InputBlock("shore uv2");
  shoreUv2.setAsAttribute("uv2");
  const shoreUv2Split = new VectorSplitterBlock("Split shore UV2");
  shoreUv2.output.connectTo(shoreUv2Split.xyIn);

  // Wet sand band: 1 near waterline, 0 on dry land and deep water
  // Land side: ramps up as we approach the shore from dry sand
  const wetSandLand = new SmoothStepBlock("wet sand land");
  shoreUv2Split.x.connectTo(wetSandLand.value);
  floatInput("wet sand dry", 2.0).output.connectTo(wetSandLand.edge0);
  floatInput("wet sand wet", 0.3).output.connectTo(wetSandLand.edge1);

  // Water side: ramps up from underwater toward shore
  const wetSandWater = new SmoothStepBlock("wet sand water");
  shoreUv2Split.x.connectTo(wetSandWater.value);
  floatInput("wet sand deep", -1.5).output.connectTo(wetSandWater.edge0);
  floatInput("wet sand shallow", -0.2).output.connectTo(wetSandWater.edge1);

  // Combined: 1 in wet zone near shore, 0 away
  const wetSandBand = new MultiplyBlock("wet sand band");
  wetSandLand.output.connectTo(wetSandBand.left);
  wetSandWater.output.connectTo(wetSandBand.right);

  // Reduce bump strength in wet zone (smooth flat sand)
  const wetSandStrength = floatInput("wet sand bump", 0.05);
  const strengthWithWetSand = new LerpBlock("Lerp strength wet sand");
  strengthFinal.output.connectTo(strengthWithWetSand.left);
  wetSandStrength.output.connectTo(strengthWithWetSand.right);
  wetSandBand.output.connectTo(strengthWithWetSand.gradient);

  // ========== PerturbNormal ==========
  const perturbNormal = new PerturbNormalBlock("Perturb normal");
  worldPos.output.connectTo(perturbNormal.worldPosition);
  worldNormal.output.connectTo(perturbNormal.worldNormal);
  uvFinal.output.connectTo(perturbNormal.uv);
  normFinal.output.connectTo(perturbNormal.normalMapColor);
  strengthWithWetSand.output.connectTo(perturbNormal.strength);

  // ========== Glossiness ==========
  const glossGround = floatInput("glossiness ground", 0.45);
  const glossMountain = floatInput("glossiness mountain", 0.51);
  const glossLerp = new LerpBlock("Lerp glossiness");
  glossGround.output.connectTo(glossLerp.left);
  glossMountain.output.connectTo(glossLerp.right);
  mountainBlend.output.connectTo(glossLerp.gradient);

  const glossPowerExp = floatInput("gloss power exp", 4);
  const glossPow = new PowBlock("Pow");
  glossLerp.output.connectTo(glossPow.value);
  glossPowerExp.output.connectTo(glossPow.power);

  const glossPower = floatInput("Gloss power", 512);

  // ========== Specular color ==========
  const specGround = color3Input("Specular color ground", 0.227, 0.239, 0.227);
  const specMountain = color3Input("Specular color mountain", 0.192, 0.192, 0.192);
  const specLerp = new LerpBlock("Lerp specular");
  specGround.output.connectTo(specLerp.left);
  specMountain.output.connectTo(specLerp.right);
  mountainBlend.output.connectTo(specLerp.gradient);

  // ========== Light block ==========
  const light = new LightBlock("Lights");
  worldPos.output.connectTo(light.worldPosition);
  perturbNormal.output.connectTo(light.worldNormal);
  cameraPosition.output.connectTo(light.cameraPosition);
  glossPow.output.connectTo(light.glossiness);
  glossPower.output.connectTo(light.glossPower);
  diffuseFinal.output.connectTo(light.diffuseColor);
  specLerp.output.connectTo(light.specularColor);

  // ========== Lighting ==========
  const addLighting = new AddBlock("Add");
  light.diffuseOutput.connectTo(addLighting.left);
  light.specularOutput.connectTo(addLighting.right);

  // ========== Shore foam overlay (UV2.x = signed distance to shoreline) ==========
  const uv2 = new InputBlock("uv2");
  uv2.setAsAttribute("uv2");
  const uv2Split = new VectorSplitterBlock("Split UV2");
  uv2.output.connectTo(uv2Split.xyIn);
  // uv2Split.x = signed distance: positive on land, negative underwater

  // Time for animation
  const foamTime = new InputBlock("FoamTime", undefined, NodeMaterialBlockConnectionPointTypes.Float);
  foamTime.value = 0;
  foamTime.isConstant = false;
  let accumulatedTime = 0;
  scene.onBeforeRenderObservable.add(() => {
    accumulatedTime += scene.getEngine().getDeltaTime() / 1000;
    foamTime.value = accumulatedTime;
  });

  // Foam band: visible where |distance| < threshold
  // Use abs(distance) via negate + max trick, or just smoothstep on both sides
  // Shore foam: visible near shoreline on both sides (land + water)
  // Ramp up from deep water, peak at shoreline, ramp down on land
  // Water side: smoothstep from -8 to -0.5 → 0 to 1
  const foamWaterSide = new SmoothStepBlock("foam water side");
  uv2Split.x.connectTo(foamWaterSide.value);
  floatInput("foam fade deep", -3).output.connectTo(foamWaterSide.edge0);
  floatInput("foam fade shallow", -0.5).output.connectTo(foamWaterSide.edge1);

  // Land side: smoothstep from 3 to 0.5 → 0 to 1 (inverted: fade out away from shore)
  const foamLandSide = new SmoothStepBlock("foam land side");
  uv2Split.x.connectTo(foamLandSide.value);
  floatInput("foam land far", 0.8).output.connectTo(foamLandSide.edge0);
  floatInput("foam land near", 0.1).output.connectTo(foamLandSide.edge1);

  // Combine: min(waterSide, landSide) — both must be high for foam
  const foamFade = new MultiplyBlock("foam fade");
  foamWaterSide.output.connectTo(foamFade.left);
  foamLandSide.output.connectTo(foamFade.right);

  // Foam UV: U = along-shore position (precomputed on CPU in UV2.y)
  //          V = signed distance to shore (UV2.x)
  // Both U and V use the same scale so the texture maps 1:1 (square)
  const foamU = new ScaleBlock("foam U scale");
  uv2Split.y.connectTo(foamU.input);
  floatInput("foam u scale", 0.1).output.connectTo(foamU.factor);

  // V = shore distance, scrolling with time (toward/away from shore)
  const foamTexSpeed = new ScaleBlock("foam tex speed");
  foamTime.output.connectTo(foamTexSpeed.input);
  floatInput("foam scroll speed", 0.2).output.connectTo(foamTexSpeed.factor);

  const foamVBase = new ScaleBlock("foam V base");
  uv2Split.x.connectTo(foamVBase.input);
  floatInput("foam v scale", -0.25).output.connectTo(foamVBase.factor);

  // Scroll V with time (waves moving from water toward land)
  const foamV = new AddBlock("foam V animated");
  foamVBase.output.connectTo(foamV.left);
  foamTexSpeed.output.connectTo(foamV.right);

  const foamUv = new VectorMergerBlock("foam uv");
  foamU.output.connectTo(foamUv.x);
  foamV.output.connectTo(foamUv.y);

  const foamTex = new TextureBlock("Foam texture");
  foamUv.xy.connectTo(foamTex.uv);
  foamTex.texture = new Texture(TEX_PATH + "foam-wave.png", scene);

  // Second foam layer: different scale and scroll speed for more wave crests
  const foamU2 = new ScaleBlock("foam U2 scale");
  uv2Split.y.connectTo(foamU2.input);
  floatInput("foam u2 scale", 0.15).output.connectTo(foamU2.factor);

  const foamTexSpeed2 = new ScaleBlock("foam tex speed 2");
  foamTime.output.connectTo(foamTexSpeed2.input);
  floatInput("foam scroll speed 2", 0.13).output.connectTo(foamTexSpeed2.factor);

  const foamVBase2 = new ScaleBlock("foam V2 base");
  uv2Split.x.connectTo(foamVBase2.input);
  floatInput("foam v2 scale", -0.35).output.connectTo(foamVBase2.factor);

  const foamV2 = new AddBlock("foam V2 animated");
  foamVBase2.output.connectTo(foamV2.left);
  foamTexSpeed2.output.connectTo(foamV2.right);

  const foamUv2 = new VectorMergerBlock("foam uv 2");
  foamU2.output.connectTo(foamUv2.x);
  foamV2.output.connectTo(foamUv2.y);

  const foamTex2 = new TextureBlock("Foam texture 2");
  foamUv2.xy.connectTo(foamTex2.uv);
  foamTex2.texture = new Texture(TEX_PATH + "foam-wave.png", scene);

  // Combine both layers: max(layer1, layer2) for more visible crests
  const foamCombinedRgb = new MaxBlock("foam combined rgb");
  foamTex.rgb.connectTo(foamCombinedRgb.left);
  foamTex2.rgb.connectTo(foamCombinedRgb.right);

  const foamCombinedAlpha = new MaxBlock("foam combined alpha");
  foamTex.a.connectTo(foamCombinedAlpha.left);
  foamTex2.a.connectTo(foamCombinedAlpha.right);

  // Foam alpha = textureAlpha * foamFade * foamOpacity
  const foamAlphaFaded = new MultiplyBlock("foam alpha faded");
  foamCombinedAlpha.output.connectTo(foamAlphaFaded.left);
  foamFade.output.connectTo(foamAlphaFaded.right);

  const foamAlpha = new ScaleBlock("foam alpha");
  foamAlphaFaded.output.connectTo(foamAlpha.input);
  floatInput("foam opacity", 0.7).output.connectTo(foamAlpha.factor);

  const foamAlphaClamped = new ClampBlock("foam alpha clamp");
  foamAlpha.output.connectTo(foamAlphaClamped.value);

  // Lerp ground to foam RGB based on alpha
  const finalColor = new LerpBlock("Lerp foam over ground");
  addLighting.output.connectTo(finalColor.left);
  foamCombinedRgb.output.connectTo(finalColor.right);
  foamAlphaClamped.output.connectTo(finalColor.gradient);

  // ========== Fragment output ==========
  const fragmentOutput = new FragmentOutputBlock("FragmentOutput");
  finalColor.output.connectTo(fragmentOutput.rgb);

  // ========== Build ==========
  mat.addOutputNode(vertexOutput);
  mat.addOutputNode(fragmentOutput);
  mat.build();

  return mat;
}
