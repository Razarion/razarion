import { Component } from '@angular/core';
import { CrudContainerChild } from '../crud-container/crud-container.component';
import { DecimalPosition, ParticleSystemConfig, ParticleSystemEditorControllerClient } from 'src/app/generated/razarion-share';
import { BabylonRenderServiceAccessImpl } from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import { LocationVisualization } from '../../common/place-config/location-visualization';
import { ParticleSystem, Vector3 } from '@babylonjs/core';
import { BabylonModelService } from 'src/app/game/renderer/babylon-model.service';

@Component({
  selector: 'particle-system-editor',
  templateUrl: './particle-system-editor.component.html'
})
export class ParticleSystemEditorComponent implements CrudContainerChild<ParticleSystemConfig> {
  static editorControllerClient = ParticleSystemEditorControllerClient;
  particleSystemConfig!: ParticleSystemConfig;
  terrainPosition: DecimalPosition | null = null;
  particleSystem?: ParticleSystem;
  private babylonModelId?: number;
  currecntInfo?: string;

  constructor(private rendererService: BabylonRenderServiceAccessImpl,
    private babylonModelService: BabylonModelService
  ) {

  }

  init(particleSystemConfig: ParticleSystemConfig): void {
    this.particleSystemConfig = particleSystemConfig;
  }

  exportConfig(): ParticleSystemConfig {
    return this.particleSystemConfig;
  }

  getId(): number {
    return this.particleSystemConfig.id;
  }

  startParticleSystem() {
    if (this.terrainPosition) {
      const hight = LocationVisualization.getHeightFromTerrain(this.terrainPosition.x, this.terrainPosition.y, this.rendererService)
      if (this.particleSystemConfig.threeJsModelId || this.particleSystemConfig.threeJsModelId === 0) {
        const emittingPosition = new Vector3(this.terrainPosition.x,
          hight,
          this.terrainPosition.y);
        this.particleSystem = this.rendererService.createParticleSystem(this.particleSystemConfig.threeJsModelId,
          this.particleSystemConfig.imageId,
          emittingPosition,
          emittingPosition.add(new Vector3(0, 1, 0)),
          false);
        this.currecntInfo = `${this.particleSystemConfig.internalName} '${this.particleSystemConfig.id}' BablyonModel Id '${this.particleSystemConfig.threeJsModelId}'`;
        this.particleSystem.name = `Editor ${this.currecntInfo}`;
        this.particleSystem.start();
        this.babylonModelId = this.particleSystemConfig.threeJsModelId;
      }
    }
  }

  saveParticleSystem() {
    this.babylonModelService.updateParticleSystemJson(this.babylonModelId!, this.particleSystem!);
  }

}
