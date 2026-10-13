import {RawTexture} from "@babylonjs/core/Materials/Textures/rawTexture";
import {Texture} from "@babylonjs/core/Materials/Textures/texture";
import type {Scene} from "@babylonjs/core/scene";
import type {BotGroundMarkingsPixels} from "./bot-ground-markings-compute";

export {GRAVEL_FULL, GRAVEL_REACH} from "./bot-ground-markings-compute";
export type {BotGroundFootprint} from "./bot-ground-markings-compute";

/**
 * The painted layer of one bot ground: premultiplied rgb paint, alpha coverage, over the rectangle
 * origin..origin+size. gravel: the coverage of the gravel bed around the plate (red), same rectangle.
 */
export interface BotGroundMarkings {
  texture: Texture;
  gravel: Texture;
  originX: number;
  originZ: number;
  sizeX: number;
  sizeZ: number;
}

/** The textures of baked markings (computeBotGroundMarkings, bot-ground-markings-compute.ts). */
export function botGroundMarkingsTextures(scene: Scene, name: string, pixels: BotGroundMarkingsPixels): BotGroundMarkings {
  const w = pixels.width, h = pixels.height;
  // Row 0 is the southern edge (minZ): no flip, so v = 0 there
  const texture = RawTexture.CreateRGBATexture(pixels.paint, w, h, scene, true, false, Texture.TRILINEAR_SAMPLINGMODE);
  texture.name = name;
  texture.wrapU = Texture.CLAMP_ADDRESSMODE;
  texture.wrapV = Texture.CLAMP_ADDRESSMODE;
  texture.anisotropicFilteringLevel = 4;
  const gravelTexture = RawTexture.CreateLuminanceTexture(pixels.gravel, w, h, scene, true, false, Texture.TRILINEAR_SAMPLINGMODE);
  gravelTexture.name = name + " gravel";
  gravelTexture.wrapU = Texture.CLAMP_ADDRESSMODE;
  gravelTexture.wrapV = Texture.CLAMP_ADDRESSMODE;
  return {texture, gravel: gravelTexture, originX: pixels.originX, originZ: pixels.originZ, sizeX: pixels.sizeX, sizeZ: pixels.sizeZ};
}
