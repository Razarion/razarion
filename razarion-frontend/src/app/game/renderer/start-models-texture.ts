import {Texture} from "@babylonjs/core/Materials/Textures/texture";
import {Scene} from "@babylonjs/core/scene";
import {isStartModelsGateOpen, whenStartModelsReady} from "./boot-gate";

/**
 * A texture that is handed out at once but only downloaded once the start models are in (see
 * boot-gate.ts whenStartModelsReady): the caller gets its object now - a material can be built
 * around it, a sprite sheet cached - and Babylon fills it in later via updateURL. Until then it is
 * not ready, which is what it would have been anyway while it was still on the wire.
 * <p>
 * For what is not needed to see one's own builder: effect sheets, bot ground, foam.
 */
export function textureAfterStartModels(url: string, scene: Scene, noMipmap?: boolean, invertY?: boolean, samplingMode?: number): Texture {
  if (isStartModelsGateOpen()) {
    return new Texture(url, scene, noMipmap, invertY, samplingMode);
  }
  const texture = new Texture(null, scene, noMipmap, invertY, samplingMode);
  texture.name = url;
  whenStartModelsReady().then(() => {
    // Disposed in the meantime (a tile scrolled away): BaseTexture.dispose clears the scene
    if (texture.getScene()) {
      texture.updateURL(url);
    }
  });
  return texture;
}
