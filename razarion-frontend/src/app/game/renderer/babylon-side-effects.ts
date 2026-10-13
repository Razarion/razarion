/**
 * The parts of Babylon that exist only as a side effect of being imported.
 *
 * Babylon attaches much of its API to Scene, Mesh and Engine from separate modules - scene.pick,
 * scene.beginAnimation, mesh.createInstance, the shadow and particle scene components, the .env
 * texture loader, the node material blocks. The barrel `@babylonjs/core` imported all of them, and
 * with them the whole engine: 2.40 MB gzip of JavaScript before a phone could start the game
 * (2026-09-27). The code now imports each class from its own file, which took that to 0.95 MB -
 * and made this list necessary, because nothing imports these modules by name.
 * <p>
 * A missing entry does not fail the build. It fails at runtime, often silently: without the shadow
 * component a ShadowGenerator renders no shadows, without the particle component a ParticleSystem
 * draws nothing. Check a feature in the running game after touching this file, not only the tests.
 */
import '@babylonjs/core/Animations/animatable';
import '@babylonjs/core/Culling/ray';
import '@babylonjs/core/Meshes/instancedMesh';
import '@babylonjs/core/Meshes/thinInstanceMesh';
import '@babylonjs/core/Helpers/sceneHelpers';
import '@babylonjs/core/Rendering/edgesRenderer';
import '@babylonjs/core/Lights/Shadows/shadowGeneratorSceneComponent';
import '@babylonjs/core/Particles/particleSystemComponent';
import '@babylonjs/core/Audio/audioSceneComponent';
import '@babylonjs/core/PostProcesses/RenderPipeline/postProcessRenderPipelineManagerSceneComponent';
import '@babylonjs/core/Materials/Textures/Loaders/envTextureLoader';
// NodeMaterial.Parse and NodeParticleSystemSet.Parse look their blocks up by class name, and the
// materials and particle systems come from the database: any block may be named in there.
import '@babylonjs/core/Materials/Node/Blocks/index';
import '@babylonjs/core/Particles/Node/Blocks/index';
// Shaders Babylon would otherwise import on demand when the first shadow map, particle system, sprite
// manager or layer is built. On the dev server those late imports did not reach the ShaderStore the
// effect reads from, so Babylon fetched the .fx by URL, got index.html and failed to compile: no
// shadows, no spawn particles, no sprites (2026-10-11). Same remedy as the post-process shaders in
// babylon-render-service-access-impl.service.ts. The glow layer and its blur: the buildup effect
// (babylon-buildup-effect.ts), failing on a phone every time a building went up.
import '@babylonjs/core/Shaders/shadowMap.vertex';
import '@babylonjs/core/Shaders/shadowMap.fragment';
import '@babylonjs/core/Shaders/particles.vertex';
import '@babylonjs/core/Shaders/particles.fragment';
import '@babylonjs/core/Shaders/sprites.vertex';
import '@babylonjs/core/Shaders/sprites.fragment';
import '@babylonjs/core/Shaders/layer.vertex';
import '@babylonjs/core/Shaders/layer.fragment';
import '@babylonjs/core/Shaders/glowMapGeneration.vertex';
import '@babylonjs/core/Shaders/glowMapGeneration.fragment';
import '@babylonjs/core/Shaders/glowMapMerge.vertex';
import '@babylonjs/core/Shaders/glowMapMerge.fragment';
import '@babylonjs/core/Shaders/glowBlurPostProcess.fragment';
import '@babylonjs/core/Shaders/kernelBlur.vertex';
import '@babylonjs/core/Shaders/kernelBlur.fragment';
// Lines (LinesMesh: the selection brackets) and the edges renderer
import '@babylonjs/core/Shaders/color.vertex';
import '@babylonjs/core/Shaders/color.fragment';
import '@babylonjs/core/Shaders/line.vertex';
import '@babylonjs/core/Shaders/line.fragment';
// Not a Babylon module: patches TextureBlock.bind, which walked the shader graph every frame
import './texture-block-bind-cache';
// The glTF 2.0 loader registers itself with GLTFFileLoader on import; its extensions (Draco, the
// KHR materials) are registered to load on demand, when a model first names one.
import '@babylonjs/loaders/glTF/2.0/glTFLoader';
import {registerBuiltInGLTFExtensions} from '@babylonjs/loaders/glTF/2.0/Extensions/dynamic';

registerBuiltInGLTFExtensions();
