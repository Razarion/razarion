import {NodeMaterial} from "@babylonjs/core/Materials/Node/nodeMaterial";
import {Texture} from "@babylonjs/core/Materials/Textures/texture";
import {Color3} from "@babylonjs/core/Maths/math.color";
import {DerivativeBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/derivativeBlock";
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
import {SubtractBlock} from "@babylonjs/core/Materials/Node/Blocks/subtractBlock";
import {MultiplyBlock} from "@babylonjs/core/Materials/Node/Blocks/multiplyBlock";
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
import {TriPlanarBlock} from "@babylonjs/core/Materials/Node/Blocks/triPlanarBlock";
import {CLOUD, cloudTime} from "./cloud-shadow";
import {SimplexPerlin3DBlock} from "@babylonjs/core/Materials/Node/Blocks/simplexPerlin3DBlock";
import {NormalizeBlock} from "@babylonjs/core/Materials/Node/Blocks/normalizeBlock";
import type {Scene} from "@babylonjs/core/scene";
import {Showcase} from "./showcase";
import {GROUND} from "./ground-rules";
import type {NodeMaterialConnectionPoint} from "@babylonjs/core/Materials/Node/nodeMaterialBlockConnectionPoint";

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

/** Simplex noise over world XZ (roughly -1..1); scale = 1 / feature size in metres, z picks an independent field. */
function worldNoise(worldXZ: VectorMergerBlock, name: string, scale: number, z: number): NodeMaterialConnectionPoint {
  const scaled = new ScaleBlock(name + " uv");
  worldXZ.xy.connectTo(scaled.input);
  floatInput(name + " scale", scale).output.connectTo(scaled.factor);
  const split = new VectorSplitterBlock(name + " split");
  scaled.output.connectTo(split.xyIn);
  const seed = new VectorMergerBlock(name + " seed");
  split.x.connectTo(seed.x);
  split.y.connectTo(seed.y);
  floatInput(name + " z", z).output.connectTo(seed.z);
  const noise = new SimplexPerlin3DBlock(name);
  seed.xyz.connectTo(noise.seed);
  return noise.output;
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

/**
 * Lays one ground material over another. The weight says where it belongs; the height of its texture
 * (a channel standing in for a height map) decides which texels win near the edge, so the overlay
 * grows in tuft by tuft instead of fading through a mixed colour. Returns [colour, share 0..1].
 */
function overlayByHeight(name: string, base: NodeMaterialConnectionPoint, overlay: NodeMaterialConnectionPoint,
                         weight: NodeMaterialConnectionPoint, overlayHeight: NodeMaterialConnectionPoint,
                         heightInfluence: number, edge0 = GROUND.SHARE_EDGE0, edge1 = GROUND.SHARE_EDGE1): [NodeMaterialConnectionPoint, NodeMaterialConnectionPoint] {
  const value = weighted(name + " value", [[weight, 1], [overlayHeight, heightInfluence]], -0.4 * heightInfluence);
  const share = new SmoothStepBlock(name + " share");
  value.connectTo(share.value);
  floatInput(name + " edge0", edge0).output.connectTo(share.edge0);
  floatInput(name + " edge1", edge1).output.connectTo(share.edge1);
  const lerp = new LerpBlock(name);
  base.connectTo(lerp.left);
  overlay.connectTo(lerp.right);
  share.output.connectTo(lerp.gradient);
  return [lerp.output, share.output];
}

function tinted(name: string, color: NodeMaterialConnectionPoint, r: number, g: number, b: number): NodeMaterialConnectionPoint {
  const tint = new MultiplyBlock(name);
  color.connectTo(tint.left);
  color3Input(name + " color", r, g, b).output.connectTo(tint.right);
  return tint.output;
}

/**
 * Builds the Ground NodeMaterial entirely in code.
 *
 * The GroundUtility texture is handed in rather than attached afterwards. build() compiles
 * the graph as it stands at that moment, and the renderer runs with parallel shader
 * compilation - so a follow-up build() meant to pick up a late texture is refused outright
 * ("Build is already in progress") and the material keeps a sampler that was never wired.
 * The tile then renders a flat colour instead of ground.
 */
export function buildGroundMaterial(scene: Scene, groundUtilityTexture: Texture | null): NodeMaterial {
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

  // ========== World-space XZ for seamless splatter across tiles ==========
  const worldXZ = new VectorMergerBlock("World XZ");
  vectorSplitter.x.connectTo(worldXZ.x);
  vectorSplitter.z.connectTo(worldXZ.y);

  // ========== UV scales ==========
  const uvScaleMountain = floatInput("uv scale mountain", 20);
  const uvMountain = new ScaleBlock("Scale uv mountain");
  uv.output.connectTo(uvMountain.input);
  uvScaleMountain.output.connectTo(uvMountain.factor);

  const uvScaleGroundUnder = floatInput("uv scale ground under", 10);
  const uvGroundUnder = new ScaleBlock("Scale uv ground under");
  uv.output.connectTo(uvGroundUnder.input);
  uvScaleGroundUnder.output.connectTo(uvGroundUnder.factor);

  const uvScaleBeachVal = floatInput("uv scale beach", 4);
  const uvBeach = new ScaleBlock("uv scale beach scale");
  uv.output.connectTo(uvBeach.input);
  uvScaleBeachVal.output.connectTo(uvBeach.factor);

  const uvScaleGroundUpper = floatInput("uv scale ground upper", 10);
  const uvGroundUpper = new ScaleBlock("Scale uv ground upper");
  uv.output.connectTo(uvGroundUpper.input);
  uvScaleGroundUpper.output.connectTo(uvGroundUpper.factor);

  // Noise over world XZ at ~40 m and ~10 m: places the ground materials (below) and bends the ground
  // texture coordinates, so the 16 m repeat of the grass and earth textures does not line up in a grid
  const noiseMacro = worldNoise(worldXZ, "ground noise macro", GROUND.MACRO_SCALE, GROUND.MACRO_Z);
  const noisePatch = worldNoise(worldXZ, "ground noise patch", GROUND.PATCH_SCALE, GROUND.PATCH_Z);
  const uvWarpVector = new VectorMergerBlock("ground uv warp vector");
  noiseMacro.connectTo(uvWarpVector.x);
  noisePatch.connectTo(uvWarpVector.y);
  const uvWarp = new ScaleBlock("ground uv warp");
  uvWarpVector.xy.connectTo(uvWarp.input);
  // In repeats: ~1.5 m. More shears the texture into visible streaks where the 10 m noise turns.
  floatInput("ground uv warp amount", 0.1).output.connectTo(uvWarp.factor);
  const uvGroundUpperWarped = new AddBlock("uv ground upper warped");
  uvGroundUpper.output.connectTo(uvGroundUpperWarped.left);
  uvWarp.output.connectTo(uvGroundUpperWarped.right);
  const uvGroundUnderWarped = new AddBlock("uv ground under warped");
  uvGroundUnder.output.connectTo(uvGroundUnderWarped.left);
  uvWarp.output.connectTo(uvGroundUnderWarped.right);

  const uvSplatterScale = floatInput("uv beach splatter", 0.0125);
  const uvSplatter = new ScaleBlock("Scale splatter");
  worldXZ.xy.connectTo(uvSplatter.input);
  uvSplatterScale.output.connectTo(uvSplatter.factor);

  // ========== GroundUtility texture ==========
  const groundUtility = new TextureBlock("GroundUtility");
  uv.output.connectTo(groundUtility.uv);
  groundUtility.texture = groundUtilityTexture;

  // Mountain factor with noise for organic edge
  const mountainBlendRaw = new ScaleBlock("Scale mountain blend");
  groundUtility.r.connectTo(mountainBlendRaw.input);
  floatInput("mountain blend factor", 1).output.connectTo(mountainBlendRaw.factor);

  // Noise to break up the straight mountain/grass edgee
  const uvMountainNoise = new ScaleBlock("Scale uv mountain noise");
  worldXZ.xy.connectTo(uvMountainNoise.input);
  floatInput("mountain noise uv scale", 0.2).output.connectTo(uvMountainNoise.factor);

  const mountainNoiseTex = new TextureBlock("Mountain noise");
  uvMountainNoise.output.connectTo(mountainNoiseTex.uv);
  mountainNoiseTex.texture = new Texture(TEX_PATH + "ground-splatter.webp", scene);

  // noise centered around 0: (tex.r - 0.5) * strength
  const mountainNoiseCenter = new SubtractBlock("Mountain noise center");
  mountainNoiseTex.r.connectTo(mountainNoiseCenter.left);
  floatInput("mountain noise offset", 0.1).output.connectTo(mountainNoiseCenter.right);

  const mountainNoiseScaled = new ScaleBlock("Mountain noise scaled");
  mountainNoiseCenter.output.connectTo(mountainNoiseScaled.input);
  floatInput("mountain noise strength", 1).output.connectTo(mountainNoiseScaled.factor);

  const mountainBlendNoisy = new AddBlock("Mountain blend noisy");
  mountainBlendRaw.output.connectTo(mountainBlendNoisy.left);
  mountainNoiseScaled.output.connectTo(mountainBlendNoisy.right);

  const mountainBlend = new SmoothStepBlock("Mountain blend step");
  mountainBlendNoisy.output.connectTo(mountainBlend.value);
  floatInput("mountain edge0", 0.6).output.connectTo(mountainBlend.edge0);
  floatInput("mountain edge1", 1).output.connectTo(mountainBlend.edge1);

  // ========== Beach detection ==========
  // Splatter texture — large scale (overall shape)
  const splatterTex = new TextureBlock("Splatter texture");
  uvSplatter.output.connectTo(splatterTex.uv);
  splatterTex.texture = new Texture(TEX_PATH + "ground-splatter.webp", scene);

  // (splatterTex.r - 0.4) * 0.6 + position.y
  const splatterOffset = floatInput("splatter offset", 0.4);
  const splatterSub = new SubtractBlock("Subtract splatter");
  splatterTex.r.connectTo(splatterSub.left);
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

  // ========== Diffuse textures ==========
  const groundUpperDiffuse = new TextureBlock("Ground upper");
  uvGroundUpperWarped.output.connectTo(groundUpperDiffuse.uv);
  // Generated (razarion-ai-content/scripts/ground-textures.mjs): 16 m per repeat, alpha = height
  groundUpperDiffuse.texture = new Texture(TEX_PATH + "ground-grass-diffuse.webp", scene);

  const groundUnderDiffuse = new TextureBlock("Ground under");
  uvGroundUnderWarped.output.connectTo(groundUnderDiffuse.uv);
  groundUnderDiffuse.texture = new Texture(TEX_PATH + "ground-earth-diffuse.webp", scene);

  // ========== TriPlanar for mountain (no stretching on steep faces) ==========
  const triPlanarScale = floatInput("triplanar scale", 0.2);
  const triPlanarPos = new ScaleBlock("Scale triplanar pos");
  worldPos.output.connectTo(triPlanarPos.input);
  triPlanarScale.output.connectTo(triPlanarPos.factor);

  const mountainDiffuseTriplanar = new TriPlanarBlock("TriPlanar diffuse");
  triPlanarPos.output.connectTo(mountainDiffuseTriplanar.position);
  worldNormal.output.connectTo(mountainDiffuseTriplanar.normal);
  mountainDiffuseTriplanar.texture = new Texture(TEX_PATH + "ground-mountain-diffuse.webp", scene);

  // Mountain ambient occlusion (also triplanar)
  const mountainAOTriplanar = new TriPlanarBlock("TriPlanar AO");
  triPlanarPos.output.connectTo(mountainAOTriplanar.position);
  worldNormal.output.connectTo(mountainAOTriplanar.normal);
  mountainAOTriplanar.texture = new Texture(TEX_PATH + "ground-mountain-ao.webp", scene);

  // Darken mountain diffuse for better contrast against grass
  const mountainDiffuseDarken = new ScaleBlock("Mountain diffuse darken");
  mountainDiffuseTriplanar.rgb.connectTo(mountainDiffuseDarken.input);
  floatInput("mountain darken", 1.0).output.connectTo(mountainDiffuseDarken.factor);

  // Blend AO: lerp(diffuse * AO, diffuse, aoStrength) — 0 = full AO, 1 = no AO
  const mountainDiffuseMulAO = new MultiplyBlock("Mountain diffuse * AO");
  mountainDiffuseDarken.output.connectTo(mountainDiffuseMulAO.left);
  mountainAOTriplanar.rgb.connectTo(mountainDiffuseMulAO.right);
  const mountainDiffuseAO = new LerpBlock("Mountain AO blend");
  mountainDiffuseMulAO.output.connectTo(mountainDiffuseAO.left);
  mountainDiffuseDarken.output.connectTo(mountainDiffuseAO.right);
  floatInput("mountain ao strength", 0.7).output.connectTo(mountainDiffuseAO.gradient);

  // Lerp ground/mountain by mountainBlend; the ground side comes from the ground materials below
  const diffuseMountainLerp = new LerpBlock("Lerp diffuse mountain");
  mountainDiffuseAO.output.connectTo(diffuseMountainLerp.right);
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
  beachDiffuse.texture = new Texture(TEX_PATH + "ground-beach-diffuse.webp", scene);

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
  floatInput("grass shadow lo", 0.3).output.connectTo(grassShadowBand.edge0);
  floatInput("grass shadow hi", 0.5).output.connectTo(grassShadowBand.edge1);

  const grassShadowDarken = new ScaleBlock("grass shadow darken");
  beachDiffuseBlended.output.connectTo(grassShadowDarken.input);
  floatInput("grass shadow amount", 0.8).output.connectTo(grassShadowDarken.factor);

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

  // ========== Relief (UV3 from the worker: x = sky visibility, y = curvature) ==========
  // Only on land: beachStep and underwaterStep are both 1 there, so sand and sea floor stay as they were.
  const relief = new InputBlock("relief uv3");
  relief.setAsAttribute("uv3");
  const reliefSplit = new VectorSplitterBlock("Split relief");
  relief.output.connectTo(reliefSplit.xyIn);
  const landMask = new MultiplyBlock("land mask");
  beachStep.output.connectTo(landMask.left);
  underwaterStep.output.connectTo(landMask.right);

  // Hollows darker (neutral - a green tint made them read as a different grass), crests lighter and
  // drier: the eye reads the shape from the colour
  const curvatureNegated = new NegateBlock("hollow curvature");
  reliefSplit.y.connectTo(curvatureNegated.value);
  const hollowStep = new SmoothStepBlock("hollow step");
  curvatureNegated.output.connectTo(hollowStep.value);
  floatInput("hollow edge0", 0.0).output.connectTo(hollowStep.edge0);
  floatInput("hollow edge1", 0.08).output.connectTo(hollowStep.edge1);
  const crestStep = new SmoothStepBlock("crest step");
  reliefSplit.y.connectTo(crestStep.value);
  floatInput("crest edge0", 0.0).output.connectTo(crestStep.edge0);
  floatInput("crest edge1", 0.08).output.connectTo(crestStep.edge1);

  const hollowAmount = new MultiplyBlock("hollow amount");
  hollowStep.output.connectTo(hollowAmount.left);
  landMask.output.connectTo(hollowAmount.right);
  const hollowTint = new MultiplyBlock("hollow tint");
  diffuseFinal.output.connectTo(hollowTint.left);
  color3Input("hollow color", 0.80, 0.79, 0.75).output.connectTo(hollowTint.right);
  const diffuseHollow = new LerpBlock("Lerp diffuse hollow");
  diffuseFinal.output.connectTo(diffuseHollow.left);
  hollowTint.output.connectTo(diffuseHollow.right);
  hollowAmount.output.connectTo(diffuseHollow.gradient);

  const crestAmount = new MultiplyBlock("crest amount");
  crestStep.output.connectTo(crestAmount.left);
  landMask.output.connectTo(crestAmount.right);
  const crestTint = new MultiplyBlock("crest tint");
  diffuseHollow.output.connectTo(crestTint.left);
  color3Input("crest color", 1.18, 1.10, 0.86).output.connectTo(crestTint.right);
  const diffuseCrest = new LerpBlock("Lerp diffuse crest");
  diffuseHollow.output.connectTo(diffuseCrest.left);
  crestTint.output.connectTo(diffuseCrest.right);
  crestAmount.output.connectTo(diffuseCrest.gradient);

  // Steep but still passable slopes (corner range < 0.5 m, so at most ~25 degrees) show bare earth.
  // From the mesh normal, before the normal maps: the shape of the land, not the grain of the texture.
  const worldNormalSplit = new VectorSplitterBlock("Split world normal");
  worldNormal.output.connectTo(worldNormalSplit.xyzw);
  const slopeRaw = new OneMinusBlock("slope");
  worldNormalSplit.y.connectTo(slopeRaw.input);
  const slopeStep = new SmoothStepBlock("slope step");
  slopeRaw.output.connectTo(slopeStep.value);
  floatInput("slope edge0", GROUND.SLOPE_EDGE0).output.connectTo(slopeStep.edge0);
  floatInput("slope edge1", GROUND.SLOPE_EDGE1).output.connectTo(slopeStep.edge1);
  const diffuseRelief = diffuseCrest;

  // ========== Ground materials: grass, lush grass, dry grass, bare earth, gravel ==========
  // Placed by the shape of the land instead of a random mask: lush grass in hollows, dry grass on
  // crests, earth on slopes, gravel at the foot of rock. Noise at ~40 m, ~10 m and ~2.5 m makes the
  // patches and their edges ragged. Each overlay grows in by the height of its texture (overlayByHeight).
  // Lush, dry and gravel are tinted copies of the grass, earth and rock textures - no extra samplers.
  const noiseFine = worldNoise(worldXZ, "ground noise fine", GROUND.FINE_SCALE, GROUND.FINE_Z);
  const ragged = weighted("ground ragged", [[noisePatch, 1], [noiseFine, GROUND.FINE_WEIGHT]], 0);

  const grass = groundUpperDiffuse.rgb;
  const grassHeight = groundUpperDiffuse.a;
  const earth = groundUnderDiffuse.rgb;
  const earthHeight = groundUnderDiffuse.a;

  // The hills are 1-2.5 m over the 0.5 m of the flat land: dry grass on their tops, lush in between
  const hilltop = new SmoothStepBlock("hilltop");
  vectorSplitter.y.connectTo(hilltop.value);
  floatInput("hilltop edge0", 0.9).output.connectTo(hilltop.edge0);
  floatInput("hilltop edge1", 2.2).output.connectTo(hilltop.edge1);
  const dryWeight = weighted("dry weight", [[crestStep.output, 1], [hilltop.output, 0.6], [noiseMacro, -0.3], [ragged, 0.15]], -0.05);
  const [withDry] = overlayByHeight("ground dry", grass, tinted("dry grass", grass, 1.1, 1.02, 0.84), dryWeight, grassHeight, 0.35);

  const lushWeight = weighted("lush weight", [[hollowStep.output, 1], [hilltop.output, -0.4], [noiseMacro, 0.3], [ragged, 0.15]], 0);
  const [withLush] = overlayByHeight("ground lush", withDry, tinted("lush grass", grass, 0.86, 0.94, 0.82), lushWeight, grassHeight, 0.35);

  // Steep but still passable slopes (corner range < 0.5 m, so at most ~25 degrees) and a few bare spots
  // Mirrored on the CPU for the sprites (ground-rules.ts) - change the values there
  const earthWeight = weighted("earth weight", [[slopeStep.output, GROUND.EARTH_SLOPE], [noisePatch, GROUND.EARTH_PATCH],
    [noiseFine, GROUND.EARTH_FINE]], GROUND.EARTH_BIAS);
  const [withEarth, earthShare] = overlayByHeight("ground earth", withLush, tinted("bare earth", earth, 0.76, 0.74, 0.72), earthWeight, earthHeight, 0.4,
    GROUND.EARTH_EDGE0, GROUND.EARTH_EDGE1);

  // Stony ground: at the foot of rock (GroundUtility red, the blocked cells, filtered into a band
  // about a metre wide) and in stretches of 30-50 m on about a fifth of the land (ground-rules.ts)
  const gravelStep = new SmoothStepBlock("gravel step");
  mountainBlendRaw.output.connectTo(gravelStep.value);
  floatInput("gravel edge0", 0.02).output.connectTo(gravelStep.edge0);
  floatInput("gravel edge1", 0.5).output.connectTo(gravelStep.edge1);
  const gravelColor = new LerpBlock("gravel mix");
  earth.connectTo(gravelColor.left);
  mountainDiffuseTriplanar.rgb.connectTo(gravelColor.right);
  floatInput("gravel rock share", 0.6).output.connectTo(gravelColor.gradient);
  const gravelWeight = weighted("gravel weight", [[gravelStep.output, 1], [noiseMacro, GROUND.STONY_MACRO],
    [noisePatch, GROUND.STONY_PATCH], [noiseFine, GROUND.STONY_FINE], [slopeStep.output, GROUND.STONY_SLOPE]], GROUND.STONY_BIAS);
  const [groundMaterials, gravelShare] = overlayByHeight("ground gravel", withEarth, tinted("gravel", gravelColor.output, 1.05, 1.02, 0.98), gravelWeight, mountainDiffuseTriplanar.r, 0.6);
  groundMaterials.connectTo(diffuseMountainLerp.left);

  // Grass share for the normal map and bump strength: 1 = grass normals, 0 = earth normals
  const notEarth = new OneMinusBlock("not earth");
  earthShare.connectTo(notEarth.input);
  const notGravel = new OneMinusBlock("not gravel");
  gravelShare.connectTo(notGravel.input);
  const grassShare = new MultiplyBlock("grass share");
  notEarth.output.connectTo(grassShare.left);
  notGravel.output.connectTo(grassShare.right);

  // Edge bump where grass meets earth, from screen-space derivatives of the share
  const splatterDeriv = new DerivativeBlock("Splatter derivative");
  grassShare.output.connectTo(splatterDeriv.input);
  const derivNegX = new NegateBlock("negate dFdx");
  splatterDeriv.dx.connectTo(derivNegX.value);
  const derivNegY = new NegateBlock("negate dFdy");
  splatterDeriv.dy.connectTo(derivNegY.value);
  const splatterNormMerge = new VectorMergerBlock("Splatter normal merge");
  derivNegX.output.connectTo(splatterNormMerge.x);
  derivNegY.output.connectTo(splatterNormMerge.y);
  floatInput("splatter deriv strength", 0.3).output.connectTo(splatterNormMerge.z);

  // ========== Growth under plants (blue channel of GroundUtility, see vegetation-mask.ts) ==========
  // A palm or a bush gets its own patch of darker, lusher grass - also on sand, where the land mask
  // would otherwise keep everything off - and a shaded core at its foot.
  const growth = new MultiplyBlock("growth");
  groundUtility.b.connectTo(growth.left);
  underwaterStep.output.connectTo(growth.right);
  const growthGrass = new MultiplyBlock("growth grass");
  groundUpperDiffuse.rgb.connectTo(growthGrass.left);
  color3Input("growth color", 0.82, 0.92, 0.72).output.connectTo(growthGrass.right);
  const growthAmount = new ScaleBlock("growth amount");
  growth.output.connectTo(growthAmount.input);
  floatInput("growth strength", 0.85).output.connectTo(growthAmount.factor);
  const diffuseGrowth = new LerpBlock("Lerp diffuse growth");
  diffuseRelief.output.connectTo(diffuseGrowth.left);
  growthGrass.output.connectTo(diffuseGrowth.right);
  growthAmount.output.connectTo(diffuseGrowth.gradient);
  const growthCore = new SmoothStepBlock("growth core");
  growth.output.connectTo(growthCore.value);
  floatInput("growth core edge0", 0.6).output.connectTo(growthCore.edge0);
  floatInput("growth core edge1", 1.0).output.connectTo(growthCore.edge1);
  const growthShade = new LerpBlock("growth shade");
  floatInput("growth one", 1).output.connectTo(growthShade.left);
  floatInput("growth core darken", 0.72).output.connectTo(growthShade.right);
  growthCore.output.connectTo(growthShade.gradient);
  const diffuseGrowthShaded = new ScaleBlock("diffuse growth shaded");
  diffuseGrowth.output.connectTo(diffuseGrowthShaded.input);
  growthShade.output.connectTo(diffuseGrowthShaded.factor);

  // ========== Paths (green channel of GroundUtility, see ground-paths.ts) ==========
  // Trodden earth: half dirt, half sand, warmed and a little darker, so it reads as worn ground.
  const pathMix = new LerpBlock("path earth mix");
  groundUnderDiffuse.rgb.connectTo(pathMix.left);
  beachDiffuse.rgb.connectTo(pathMix.right);
  floatInput("path sand share", 0.35).output.connectTo(pathMix.gradient);
  const pathEarth = new MultiplyBlock("path earth");
  pathMix.output.connectTo(pathEarth.left);
  color3Input("path color", 0.92, 0.8, 0.64).output.connectTo(pathEarth.right);
  const pathAmountRaw = new MultiplyBlock("path amount raw");
  groundUtility.g.connectTo(pathAmountRaw.left);
  landMask.output.connectTo(pathAmountRaw.right);
  const pathAmount = new ScaleBlock("path amount");
  pathAmountRaw.output.connectTo(pathAmount.input);
  floatInput("path strength", 1).output.connectTo(pathAmount.factor);
  const diffusePath = new LerpBlock("Lerp diffuse path");
  diffuseGrowthShaded.output.connectTo(diffusePath.left);
  pathEarth.output.connectTo(diffusePath.right);
  pathAmount.output.connectTo(diffusePath.gradient);

  // ========== Normal map textures ==========
  const beachNorm = new TextureBlock("Beach texture");
  uvBeach.output.connectTo(beachNorm.uv);
  beachNorm.texture = new Texture(TEX_PATH + "ground-beach-norm.jpg", scene);

  const groundUpperNorm = new TextureBlock("Ground upper norm");
  uvGroundUpperWarped.output.connectTo(groundUpperNorm.uv);
  groundUpperNorm.texture = new Texture(TEX_PATH + "ground-upper-norm.jpg", scene);

  const groundUnderNormTex = new TextureBlock("Ground under norm");
  uvGroundUnderWarped.output.connectTo(groundUnderNormTex.uv);
  groundUnderNormTex.texture = new Texture(TEX_PATH + "ground-under-norm.webp", scene);

  // Flip green channel (DirectX → OpenGL normal map convention)
  const underNormFlipG = new OneMinusBlock("Flip under norm G");
  groundUnderNormTex.g.connectTo(underNormFlipG.input);
  const groundUnderNorm = new VectorMergerBlock("Under norm flipped");
  groundUnderNormTex.r.connectTo(groundUnderNorm.x);
  underNormFlipG.output.connectTo(groundUnderNorm.y);
  groundUnderNormTex.b.connectTo(groundUnderNorm.z);

  const mountainNormTriplanar = new TriPlanarBlock("TriPlanar norm");
  triPlanarPos.output.connectTo(mountainNormTriplanar.position);
  worldNormal.output.connectTo(mountainNormTriplanar.normal);
  mountainNormTriplanar.texture = new Texture(TEX_PATH + "ground-mountain-norm.jpg", scene);

  // Lerp upper/under normals by height
  const normHeightLerp = new LerpBlock("Lerp norm height");
  groundUnderNorm.xyz.connectTo(normHeightLerp.left);
  groundUpperNorm.rgb.connectTo(normHeightLerp.right);
  grassShare.output.connectTo(normHeightLerp.gradient);

  // Blend splatter normal at transition edges for 3D depth
  // borderIntensity peaks at 1.0 where grassShare = 0.5 (the transition zone)
  const heightStepInv = new OneMinusBlock("1 - grassShare");
  grassShare.output.connectTo(heightStepInv.input);
  const borderRaw = new MultiplyBlock("border raw");
  grassShare.output.connectTo(borderRaw.left);
  heightStepInv.output.connectTo(borderRaw.right);
  // Scale up raw (max 0.25 at edge) then clamp to widen the bump zone
  const borderWiden = new ScaleBlock("border widen");
  borderRaw.output.connectTo(borderWiden.input);
  floatInput("splatter border width", 1).output.connectTo(borderWiden.factor);
  const borderClamped = new ClampBlock("border clamp");
  borderWiden.output.connectTo(borderClamped.value);
  const borderIntensity = new ScaleBlock("border intensity");
  borderClamped.output.connectTo(borderIntensity.input);
  floatInput("splatter norm strength", 1).output.connectTo(borderIntensity.factor);

  const normWithSplatter = new LerpBlock("Lerp norm with splatter");
  normHeightLerp.output.connectTo(normWithSplatter.left);
  splatterNormMerge.xyz.connectTo(normWithSplatter.right);
  borderIntensity.output.connectTo(normWithSplatter.gradient);

  // Lerp ground/mountain normals
  const normMountainLerp = new LerpBlock("Lerp norm mountain");
  normWithSplatter.output.connectTo(normMountainLerp.left);
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
  uvGroundUnderWarped.output.connectTo(uvLerpMountainGround.right);
  mountainBlend.output.connectTo(uvLerpMountainGround.gradient);

  const uvFinal = new LerpBlock("Lerp uv final");
  uvLerpMountainGround.output.connectTo(uvFinal.left);
  uvBeach.output.connectTo(uvFinal.right);
  beachStep.output.connectTo(uvFinal.gradient);

  // ========== Bump strength ==========
  const strengthBeach = floatInput("strength beach", 0.44);
  const strengthGroundUpper = floatInput("strength ground upper", 0.3);
  const strengthGroundUnder = floatInput("strength ground under", 0.25);
  const strengthMountain = floatInput("strength mountain", 1);

  // Lerp upper/under bump strength by grassShare
  const strengthGroundLerp = new LerpBlock("Lerp strength ground upper/under");
  strengthGroundUnder.output.connectTo(strengthGroundLerp.left);
  strengthGroundUpper.output.connectTo(strengthGroundLerp.right);
  grassShare.output.connectTo(strengthGroundLerp.gradient);

  const strengthLerpGM = new LerpBlock("Lerp strength ground/mountain");
  strengthGroundLerp.output.connectTo(strengthLerpGM.left);
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
  // The hills rise 2 m over 40 m - a slope of a few percent the sun barely tells apart. For the
  // ground's light only, slopes count three times as steep, so light and shade draw the hills.
  const reliefNormalScale = floatInput("relief normal exaggeration", 3);
  const normalXScaled = new ScaleBlock("normal x exaggerated");
  worldNormalSplit.x.connectTo(normalXScaled.input);
  reliefNormalScale.output.connectTo(normalXScaled.factor);
  const normalZScaled = new ScaleBlock("normal z exaggerated");
  worldNormalSplit.z.connectTo(normalZScaled.input);
  reliefNormalScale.output.connectTo(normalZScaled.factor);
  const exaggeratedNormalRaw = new VectorMergerBlock("exaggerated normal");
  normalXScaled.output.connectTo(exaggeratedNormalRaw.x);
  worldNormalSplit.y.connectTo(exaggeratedNormalRaw.y);
  normalZScaled.output.connectTo(exaggeratedNormalRaw.z);
  floatInput("exaggerated normal w", 0).output.connectTo(exaggeratedNormalRaw.w);   // a direction, as Vector4
  const exaggeratedNormal = new NormalizeBlock("exaggerated normal normalized");
  exaggeratedNormalRaw.xyzw.connectTo(exaggeratedNormal.input);
  exaggeratedNormal.output.connectTo(perturbNormal.worldNormal);
  uvFinal.output.connectTo(perturbNormal.uv);
  normFinal.output.connectTo(perturbNormal.normalMapColor);
  strengthWithWetSand.output.connectTo(perturbNormal.strength);

  // ========== Glossiness ==========
  const glossGround = floatInput("glossiness ground", 0.45);
  const glossMountain = floatInput("glossiness mountain", 0.36);
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
  const specMountain = color3Input("Specular color mountain", 0.20, 0.16, 0.16);
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
  diffusePath.output.connectTo(light.diffuseColor);
  specLerp.output.connectTo(light.specularColor);

  // Time for animation (shore foam, cloud shadows)
  const foamTime = new InputBlock("FoamTime", undefined, NodeMaterialBlockConnectionPointTypes.Float);
  foamTime.value = 0;
  foamTime.isConstant = false;
  // Shared clock, not per tile: every tile's clouds must be in the same place, or they would
  // jump at tile seams for tiles built at different moments.
  // Tie the per-frame animation observer to the material's lifetime. Ground materials are
  // built once PER TILE; without this cleanup every tile ever scrolled into view left a live
  // onBeforeRender callback running forever, so the frame cost grew the more the player scrolled
  // (only a reload reset it). Disposing the material now removes the observer with it.
  const foamObserver = scene.onBeforeRenderObservable.add(() => {
    foamTime.value = cloudTime();
  });
  mat.onDisposeObservable.add(() => scene.onBeforeRenderObservable.remove(foamObserver));

  // ========== Cloud shadows ==========
  // Two octaves of simplex noise drifting with the wind and slowly changing shape; where they are
  // high a cloud takes the sun away. Computed, not sampled: the fragment shader already uses 15 of
  // the 16 texture units a WebGL2 GPU guarantees. Only the sun: sky light and foam stay as they are.
  const cloudDrift = new ScaleBlock("cloud drift");
  foamTime.output.connectTo(cloudDrift.input);
  floatInput("cloud speed", CLOUD.SPEED).output.connectTo(cloudDrift.factor);
  const cloudUvBase = new ScaleBlock("cloud uv base");
  worldXZ.xy.connectTo(cloudUvBase.input);
  floatInput("cloud scale", CLOUD.SCALE).output.connectTo(cloudUvBase.factor);
  const cloudUv = new AddBlock("cloud uv");
  cloudUvBase.output.connectTo(cloudUv.left);
  cloudDrift.output.connectTo(cloudUv.right);
  const cloudUvSplit = new VectorSplitterBlock("Split cloud uv");
  cloudUv.output.connectTo(cloudUvSplit.xyIn);
  const cloudEvolve = new ScaleBlock("cloud evolve");
  foamTime.output.connectTo(cloudEvolve.input);
  floatInput("cloud evolve speed", CLOUD.EVOLVE_SPEED).output.connectTo(cloudEvolve.factor);
  const cloudSeed = new VectorMergerBlock("cloud seed");
  cloudUvSplit.x.connectTo(cloudSeed.x);
  cloudUvSplit.y.connectTo(cloudSeed.y);
  cloudEvolve.output.connectTo(cloudSeed.z);
  const cloudNoise1 = new SimplexPerlin3DBlock("cloud noise 1");
  cloudSeed.xyz.connectTo(cloudNoise1.seed);
  const cloudSeedFine = new ScaleBlock("cloud seed fine");
  cloudSeed.xyz.connectTo(cloudSeedFine.input);
  floatInput("cloud fine scale", CLOUD.FINE_SCALE).output.connectTo(cloudSeedFine.factor);
  const cloudNoise2 = new SimplexPerlin3DBlock("cloud noise 2");
  cloudSeedFine.output.connectTo(cloudNoise2.seed);
  const cloudNoise2Weighted = new ScaleBlock("cloud noise 2 weighted");
  cloudNoise2.output.connectTo(cloudNoise2Weighted.input);
  floatInput("cloud fine weight", CLOUD.FINE_WEIGHT).output.connectTo(cloudNoise2Weighted.factor);
  const cloudCombined = new AddBlock("cloud combined");
  cloudNoise1.output.connectTo(cloudCombined.left);
  cloudNoise2Weighted.output.connectTo(cloudCombined.right);
  const cloudStep = new SmoothStepBlock("cloud step");
  cloudCombined.output.connectTo(cloudStep.value);
  floatInput("cloud edge0", CLOUD.EDGE0).output.connectTo(cloudStep.edge0);
  floatInput("cloud edge1", CLOUD.EDGE1).output.connectTo(cloudStep.edge1);
  const cloudDarken = new ScaleBlock("cloud darken");
  cloudStep.output.connectTo(cloudDarken.input);
  floatInput("cloud shadow strength", CLOUD.SHADOW_STRENGTH).output.connectTo(cloudDarken.factor);
  const cloudShade = new OneMinusBlock("cloud shade");
  cloudDarken.output.connectTo(cloudShade.input);

  // ========== Lighting ==========
  // Sun: dimmed under clouds, and a little in hollows (the sky visibility also stands for the light
  // that the ground around a hollow blocks at grazing angles).
  const skyForSun = new LerpBlock("sky for sun");
  floatInput("one", 1).output.connectTo(skyForSun.left);
  reliefSplit.x.connectTo(skyForSun.right);
  floatInput("sun occlusion", 0.4).output.connectTo(skyForSun.gradient);
  const sunShade = new MultiplyBlock("sun shade");
  cloudShade.output.connectTo(sunShade.left);
  skyForSun.output.connectTo(sunShade.right);
  const sunDiffuse = new ScaleBlock("sun diffuse");
  light.diffuseOutput.connectTo(sunDiffuse.input);
  sunShade.output.connectTo(sunDiffuse.factor);
  const sunSpecular = new ScaleBlock("sun specular");
  light.specularOutput.connectTo(sunSpecular.input);
  cloudShade.output.connectTo(sunSpecular.factor);

  // Sky light: cool from above, warm bounce from below, held back where the land around hides the sky.
  // There was no ambient term before, so shadowed ground fell to 40% of the sun and nothing else.
  const hemiGradient = new ScaleBlock("hemi gradient");
  worldNormalSplit.y.connectTo(hemiGradient.input);
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
  reliefSplit.x.connectTo(ambientLight.factor);
  const ambientStrength = new ScaleBlock("ambient strength");
  ambientLight.output.connectTo(ambientStrength.input);
  floatInput("ambient intensity", 0.3).output.connectTo(ambientStrength.factor);
  const ambient = new MultiplyBlock("ambient");
  diffusePath.output.connectTo(ambient.left);
  ambientStrength.output.connectTo(ambient.right);

  const sunLight = new AddBlock("sun light");
  sunDiffuse.output.connectTo(sunLight.left);
  sunSpecular.output.connectTo(sunLight.right);
  const addLighting = new AddBlock("Add");
  sunLight.output.connectTo(addLighting.left);
  ambient.output.connectTo(addLighting.right);

  // ========== Shore foam overlay (UV2.x = signed distance to shoreline) ==========
  const uv2 = new InputBlock("uv2");
  uv2.setAsAttribute("uv2");
  const uv2Split = new VectorSplitterBlock("Split UV2");
  uv2.output.connectTo(uv2Split.xyIn);
  // uv2Split.x = signed distance: positive on land, negative underwater

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
  foamTex.texture = new Texture(TEX_PATH + "foam-wave.webp", scene);

  // Foam alpha = textureAlpha * foamFade * foamOpacity
  const foamAlphaFaded = new MultiplyBlock("foam alpha faded");
  foamTex.a.connectTo(foamAlphaFaded.left);
  foamFade.output.connectTo(foamAlphaFaded.right);

  const foamAlpha = new ScaleBlock("foam alpha");
  foamAlphaFaded.output.connectTo(foamAlpha.input);
  floatInput("foam opacity", 0.7).output.connectTo(foamAlpha.factor);

  const foamAlphaClamped = new ClampBlock("foam alpha clamp");
  foamAlpha.output.connectTo(foamAlphaClamped.value);

  // Lerp ground to foam RGB based on alpha
  const finalColor = new LerpBlock("Lerp foam over ground");
  addLighting.output.connectTo(finalColor.left);
  foamTex.rgb.connectTo(finalColor.right);
  foamAlphaClamped.output.connectTo(finalColor.gradient);

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
