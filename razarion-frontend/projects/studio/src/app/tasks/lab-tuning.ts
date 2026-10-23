import type {Scene} from '@babylonjs/core/scene';
import type {NodeMaterial} from '@babylonjs/core/Materials/Node/nodeMaterial';
import type {InputBlock} from '@babylonjs/core/Materials/Node/Blocks/Input/inputBlock';
import {NodeMaterialBlockConnectionPointTypes} from '@babylonjs/core/Materials/Node/Enums/nodeMaterialBlockConnectionPointTypes';
import type {ImageProcessingPostProcess} from '@babylonjs/core/PostProcesses/imageProcessingPostProcess';
import type {HemisphericLight} from '@babylonjs/core/Lights/hemisphericLight';
import type {BabylonRenderServiceAccessImpl} from '../../../../../src/app/game/renderer/babylon-render-service-access-impl.service';

/** One adjustable number, with the range its slider offers. */
export interface TuningValue {
  name: string;
  value: number;
  original: number;
  min: number;
  max: number;
  step: number;
}

/**
 * The lab's sliders over the renderer's live values: the float inputs of the node materials (every
 * material of one name at once - each tile builds its own "Ground") and the scene's light. Changed
 * values are remembered by name, laid again over materials built later (a reload, a new tile), and
 * exported as the code to carry them into the game.
 */
export class LabTuning {
  /** material name -> input name -> value set in the lab */
  private readonly overrides = new Map<string, Map<string, number>>();
  /** material name -> input name -> value the renderer gave it */
  private readonly originals = new Map<string, Map<string, number>>();
  /** light value name -> value the scene had */
  private readonly lightOriginals = new Map<string, number>();

  constructor(private readonly renderer: BabylonRenderServiceAccessImpl) {
  }

  private get scene(): Scene {
    return this.renderer.getScene();
  }

  /** Node material names in the scene, with how many materials carry each. */
  materialNames(): { name: string, count: number }[] {
    const counts = new Map<string, number>();
    for (const material of this.nodeMaterials()) {
      counts.set(material.name, (counts.get(material.name) ?? 0) + 1);
    }
    return [...counts.entries()].map(([name, count]) => ({name, count})).sort((a, b) => a.name.localeCompare(b.name));
  }

  /** The float inputs of a material, as sliders. */
  inputs(materialName: string): TuningValue[] {
    const material = this.nodeMaterials().find(m => m.name === materialName);
    if (!material) {
      return [];
    }
    const originals = this.originalsOf(materialName);
    return this.floatInputs(material).map(block => {
      if (!originals.has(block.name)) {
        originals.set(block.name, block.value);
      }
      const original = originals.get(block.name)!;
      return LabTuning.slider(block.name, block.value, original);
    }).sort((a, b) => a.name.localeCompare(b.name));
  }

  setInput(materialName: string, inputName: string, value: number): void {
    let byInput = this.overrides.get(materialName);
    if (!byInput) {
      byInput = new Map();
      this.overrides.set(materialName, byInput);
    }
    byInput.set(inputName, value);
    this.applyTo(materialName, inputName, value);
  }

  resetMaterial(materialName: string): void {
    const originals = this.originalsOf(materialName);
    for (const inputName of this.overrides.get(materialName)?.keys() ?? []) {
      const original = originals.get(inputName);
      if (original !== undefined) {
        this.applyTo(materialName, inputName, original);
      }
    }
    this.overrides.delete(materialName);
  }

  /** Lays the lab's values over every material again: after a reload the tiles' materials are new. */
  reapply(): void {
    for (const [materialName, byInput] of this.overrides) {
      for (const [inputName, value] of byInput) {
        this.applyTo(materialName, inputName, value);
      }
    }
  }

  /** The light: sun, sky light, shadow and the tone mapping, all as in internalSetup. */
  lightValues(): TuningValue[] {
    const sun = this.renderer.directionalLight;
    const sky = this.skyLight();
    const imageProcessing = this.imageProcessing();
    const values: TuningValue[] = [
      {...LabTuning.slider('Sun intensity', sun.intensity, 0.95), min: 0, max: 3},
      {...LabTuning.slider('Shadow darkness', this.renderer.shadowGenerator.darkness, 0.4), min: 0, max: 1},
    ];
    if (sky) {
      values.push({...LabTuning.slider('Sky light', sky.intensity, 0.8), min: 0, max: 3});
    }
    if (imageProcessing) {
      values.push({...LabTuning.slider('Exposure', imageProcessing.exposure, 0.95), min: 0.2, max: 3});
      values.push({...LabTuning.slider('Contrast', imageProcessing.contrast, 1.2), min: 0.5, max: 3});
      values.push({...LabTuning.slider('Saturation', imageProcessing.colorCurves?.globalSaturation ?? 0, 25), min: -100, max: 100, step: 1});
    }
    // The original is what the scene had when the lab first read it, not what internalSetup set
    // (the director's showcase changes some of it)
    for (const value of values) {
      if (!this.lightOriginals.has(value.name)) {
        this.lightOriginals.set(value.name, value.value);
      }
      value.original = this.lightOriginals.get(value.name)!;
    }
    return values;
  }

  setLight(name: string, value: number): void {
    const imageProcessing = this.imageProcessing();
    switch (name) {
      case 'Sun intensity':
        this.renderer.directionalLight.intensity = value;
        break;
      case 'Shadow darkness':
        this.renderer.shadowGenerator.darkness = value;
        break;
      case 'Sky light': {
        const sky = this.skyLight();
        if (sky) {
          sky.intensity = value;
        }
        break;
      }
      case 'Exposure':
        if (imageProcessing) {
          imageProcessing.exposure = value;
        }
        break;
      case 'Contrast':
        if (imageProcessing) {
          imageProcessing.contrast = value;
        }
        break;
      case 'Saturation':
        if (imageProcessing?.colorCurves) {
          imageProcessing.colorCurves.globalSaturation = value;
        }
        break;
    }
  }

  /** The changed material values as code: per material the inputs and their new values. */
  export(): string {
    const lines: string[] = [];
    for (const [materialName, byInput] of this.overrides) {
      const originals = this.originalsOf(materialName);
      const changed = [...byInput.entries()].filter(([inputName, value]) => originals.get(inputName) !== value);
      if (!changed.length) {
        continue;
      }
      lines.push(`// ${materialName}`);
      for (const [inputName, value] of changed) {
        lines.push(`floatInput("${inputName}", ${LabTuning.round(value)}),   // was ${LabTuning.round(originals.get(inputName)!)}`);
      }
    }
    return lines.join('\n') || '// nothing changed';
  }

  private applyTo(materialName: string, inputName: string, value: number): void {
    for (const material of this.nodeMaterials()) {
      if (material.name !== materialName) {
        continue;
      }
      const block = this.floatInputs(material).find(b => b.name === inputName);
      if (block) {
        block.value = value;
      }
    }
  }

  private nodeMaterials(): NodeMaterial[] {
    return this.scene.materials.filter(m => m.getClassName() === 'NodeMaterial') as NodeMaterial[];
  }

  private floatInputs(material: NodeMaterial): InputBlock[] {
    return material.getInputBlocks().filter(b => b.type === NodeMaterialBlockConnectionPointTypes.Float
      && !b.isAttribute && !b.isSystemValue && typeof b.value === 'number');
  }

  private originalsOf(materialName: string): Map<string, number> {
    let byInput = this.originals.get(materialName);
    if (!byInput) {
      byInput = new Map();
      this.originals.set(materialName, byInput);
    }
    return byInput;
  }

  private skyLight(): HemisphericLight | null {
    return (this.scene.getLightByName('SkyLight') as HemisphericLight) ?? null;
  }

  private imageProcessing(): ImageProcessingPostProcess | null {
    return (this.scene.postProcesses.find(p => p.name === 'Image processing') as ImageProcessingPostProcess) ?? null;
  }

  /** A slider around the original: from zero (or three times below a negative one) to three times it. */
  private static slider(name: string, value: number, original: number): TuningValue {
    const magnitude = Math.abs(original) || 1;
    const min = original < 0 ? original * 3 : 0;
    const max = original > 0 ? original * 3 : original < 0 ? magnitude : 1;
    return {name, value, original, min, max, step: magnitude / 200};
  }

  private static round(value: number): number {
    return Math.round(value * 10000) / 10000;
  }
}
