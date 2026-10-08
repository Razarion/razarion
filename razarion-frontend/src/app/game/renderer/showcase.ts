import type {Scene} from "@babylonjs/core/scene";
import type {Material} from "@babylonjs/core/Materials/material";
import {NodeMaterial} from "@babylonjs/core/Materials/Node/nodeMaterial";
import {InputBlock} from "@babylonjs/core/Materials/Node/Blocks/Input/inputBlock";
import type {DirectionalLight} from "@babylonjs/core/Lights/directionalLight";
import {ImageProcessingPostProcess} from "@babylonjs/core/PostProcesses/imageProcessingPostProcess";
import {Color3} from "@babylonjs/core/Maths/math.color";
import {Vector3} from "@babylonjs/core/Maths/math.vector";
import {CLOUD} from "./cloud-shadow";
import {WindPlugin} from "./wind-plugin";

/**
 * Switches the terrain look improvements off and on again, so a recording can show the island before
 * and after each of them from the same camera (razarion-social/pipeline/record_showcase.mjs).
 *
 * Only the director client installs it (window.razShowcase); players never get it. "Off" is the look
 * before the improvement as closely as the current code allows: the old sun and no tone mapping for
 * light, neutral tints for relief, zero strength for clouds, wind, paths and growth. The hills are in
 * the height map and cannot be switched here.
 */
export type ShowcaseFeature = "light" | "clouds" | "relief" | "wind" | "paths" | "growth";
export const SHOWCASE_FEATURES: ShowcaseFeature[] = ["light", "clouds", "relief", "wind", "paths", "growth"];

/** What the showcase needs of BabylonRenderServiceAccessImpl. */
export interface ShowcaseRenderer {
  getScene(): Scene | undefined;
  directionalLight: DirectionalLight | undefined;
}

type BlockValue = number | Color3;

/** Ground shader inputs (ground-material.ts block names) and their value with the feature off. */
const GROUND_OFF: Record<ShowcaseFeature, Record<string, BlockValue>> = {
  light: {"ambient intensity": 0},   // the ground had no sky term before
  clouds: {"cloud shadow strength": 0},
  relief: {"hollow color": new Color3(1, 1, 1), "crest color": new Color3(1, 1, 1), "sun occlusion": 0, "slope earth strength": 0},
  wind: {},
  paths: {"path strength": 0},
  growth: {"growth strength": 0, "growth core darken": 1},
};

/** The sun before it was lowered and warmed (commit bde8a8c9a). */
const OLD_SUN = {direction: new Vector3(-3, -10, 3), intensity: 0.8, diffuse: new Color3(1, 1, 1)};

interface SavedLight {
  direction: Vector3;
  intensity: number;
  diffuse: Color3;
  cloudStrength: number;
  contrast: number;
  exposure: number;
}

export class Showcase {
  /** Recordings normally drop the shadows for a steady frame rate (DirectorService.recordStart). */
  static keepShadows = false;
  private static readonly off = new Set<ShowcaseFeature>();
  private static renderer: ShowcaseRenderer | null = null;
  private static scene: Scene | null = null;
  private static sun: DirectionalLight | null = null;
  private static saved: SavedLight | null = null;
  /** The value each ground input had when first switched, per material: that is "on". */
  private static readonly onValues = new WeakMap<InputBlock, BlockValue>();

  /** The director activates before the renderer has its scene and sun; they are read at the first switch. */
  static install(renderer: ShowcaseRenderer): void {
    Showcase.renderer = renderer;
    (window as any).razShowcase = {
      set: (feature: ShowcaseFeature, on: boolean) => Showcase.set(feature, on),
      /** Exactly these features on, all others off. */
      only: (features: ShowcaseFeature[]) => SHOWCASE_FEATURES.forEach(f => Showcase.set(f, features.includes(f))),
      state: () => SHOWCASE_FEATURES.filter(f => !Showcase.off.has(f)),
      features: SHOWCASE_FEATURES,
      keepShadows: (keep: boolean) => Showcase.keepShadows = keep,
      hideGui: (hide: boolean) => Showcase.hideGui(hide),
    };
    console.log("[Showcase] installed: window.razShowcase");
  }

  static set(feature: ShowcaseFeature, on: boolean): void {
    if (!SHOWCASE_FEATURES.includes(feature)) {
      throw new Error(`Unknown showcase feature "${feature}"`);
    }
    if (!Showcase.ensureSaved()) {
      throw new Error("The renderer is not ready yet");
    }
    if (on) {
      Showcase.off.delete(feature);
    } else {
      Showcase.off.add(feature);
    }
    Showcase.applyScene();
    Showcase.scene?.materials.forEach(m => Showcase.applyToGround(m));
  }

  /** Called for every ground material as it is built, so tiles that stream in later match. */
  static applyToGround(material: Material): void {
    // Nothing was ever switched (every player's client): leave the material as built
    if (!Showcase.saved || !(material instanceof NodeMaterial) || material.name !== "Ground") {
      return;
    }
    for (const feature of SHOWCASE_FEATURES) {
      for (const [name, offValue] of Object.entries(GROUND_OFF[feature])) {
        const block = material.getBlockByName(name);
        if (!(block instanceof InputBlock)) {
          continue;
        }
        if (!Showcase.onValues.has(block)) {
          Showcase.onValues.set(block, Showcase.copy(block.value));
        }
        block.value = Showcase.copy(Showcase.off.has(feature) ? offValue : Showcase.onValues.get(block)!);
      }
    }
  }

  /** The values the features have when on - read once, before the first switch changes them. */
  private static ensureSaved(): boolean {
    if (Showcase.saved) {
      return true;
    }
    const scene = Showcase.renderer?.getScene();
    const sun = Showcase.renderer?.directionalLight;
    if (!scene || !sun) {
      return false;
    }
    Showcase.scene = scene;
    Showcase.sun = sun;
    Showcase.saved = {
      direction: sun.direction.clone(),
      intensity: sun.intensity,
      diffuse: sun.diffuse.clone(),
      cloudStrength: CLOUD.SHADOW_STRENGTH,
      contrast: Showcase.imageProcessing()?.contrast ?? 1,
      exposure: Showcase.imageProcessing()?.exposure ?? 1,
    };
    return true;
  }

  private static applyScene(): void {
    const scene = Showcase.scene, sun = Showcase.sun, saved = Showcase.saved;
    if (!scene || !sun || !saved) {
      return;
    }
    const light = !Showcase.off.has("light");
    sun.direction = (light ? saved.direction : OLD_SUN.direction).clone();
    sun.intensity = light ? saved.intensity : OLD_SUN.intensity;
    sun.diffuse = (light ? saved.diffuse : OLD_SUN.diffuse).clone();
    scene.getLightByName("SkyLight")?.setEnabled(light);
    // The image-based lighting did not load before either, but switching it off here turns every
    // model black - more than the old look ever was. It stays, so the comparison is the terrain's.
    const imageProcessing = Showcase.imageProcessing();
    if (imageProcessing) {
      imageProcessing.toneMappingEnabled = light;
      imageProcessing.colorCurvesEnabled = light;
      imageProcessing.contrast = light ? saved.contrast : 1;
      imageProcessing.exposure = light ? saved.exposure : 1;
    }
    // Sprites shade themselves from these on the CPU (cloud-shadow.ts)
    CLOUD.SHADOW_STRENGTH = Showcase.off.has("clouds") ? 0 : saved.cloudStrength;
    WindPlugin.strength = Showcase.off.has("wind") ? 0 : 1;
  }

  /**
   * Babylon GUI draws into the canvas the recording captures - a deploy prompt for the operator's
   * start unit came out in the middle of a take. Its full-screen layers go dark while filming.
   */
  static hideGui(hide: boolean): void {
    const scene = Showcase.renderer?.getScene();
    for (const texture of scene?.textures ?? []) {
      const layer = (texture as any).layer;
      if (layer && (texture as any).rootContainer) {
        layer.isEnabled = !hide;
      }
    }
  }

  private static imageProcessing(): ImageProcessingPostProcess | undefined {
    return Showcase.scene?.postProcesses.find(p => p instanceof ImageProcessingPostProcess) as ImageProcessingPostProcess | undefined;
  }

  private static copy(value: BlockValue): BlockValue {
    return typeof value === "number" ? value : value.clone();
  }
}
