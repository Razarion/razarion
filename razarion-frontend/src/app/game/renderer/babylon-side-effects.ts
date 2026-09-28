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
// The glTF 2.0 loader registers itself with GLTFFileLoader on import; its extensions (Draco, the
// KHR materials) are registered to load on demand, when a model first names one.
import '@babylonjs/loaders/glTF/2.0/glTFLoader';
import {registerBuiltInGLTFExtensions} from '@babylonjs/loaders/glTF/2.0/Extensions/dynamic';

registerBuiltInGLTFExtensions();
