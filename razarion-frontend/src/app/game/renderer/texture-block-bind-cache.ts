import {TextureBlock} from "@babylonjs/core/Materials/Node/Blocks/Dual/textureBlock";
import type {Effect} from "@babylonjs/core/Materials/effect";

/**
 * TextureBlock.bind() runs for every texture of every node material, every frame, for every sub mesh,
 * and asks _isMixed each time - a getter that walks the shader graph upstream of the uv input
 * (_isTiedToFragment, recursively, nothing cached). Our ground, water and bot-ground materials feed
 * their uvs through long chains (world position, scale, clouds), and on a Pixel 7 that walk took a
 * fifth of the whole main thread (2026-10-11 CPU profile: _isTiedToFragment 2.0 s self, 2.8 s with
 * its callers, of 13 s).
 * <p>
 * The answer only changes with the graph, and a changed graph means a rebuilt material and with it a
 * new effect. So during bind() _isMixed is answered from a value worked out once per effect; outside
 * bind() (while a material is being built) the getter is Babylon's own.
 */
interface CachedTextureBlock {
  razMixedEffect?: Effect;
  razMixed?: boolean;
  razBinding?: boolean;
}

const prototype = TextureBlock.prototype as unknown as { bind: (effect: Effect) => void };
const isMixedGetter = Object.getOwnPropertyDescriptor(TextureBlock.prototype, "_isMixed")?.get;
const originalBind = prototype.bind;

if (isMixedGetter && originalBind) {
  Object.defineProperty(TextureBlock.prototype, "_isMixed", {
    configurable: true,
    get(this: TextureBlock & CachedTextureBlock): boolean {
      return this.razBinding ? this.razMixed! : isMixedGetter.call(this);
    }
  });
  prototype.bind = function (this: TextureBlock & CachedTextureBlock, effect: Effect): void {
    if (this.razMixedEffect !== effect) {
      this.razMixed = isMixedGetter.call(this);
      this.razMixedEffect = effect;
    }
    this.razBinding = true;
    try {
      originalBind.call(this, effect);
    } finally {
      this.razBinding = false;
    }
  };
} else {
  console.warn("[Razarion] TextureBlock._isMixed/bind not found - bind cache not installed");
}
