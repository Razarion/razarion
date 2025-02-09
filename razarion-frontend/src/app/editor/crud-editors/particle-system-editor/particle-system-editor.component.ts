import {Component} from '@angular/core';
import {CrudContainerChild} from '../crud-container/crud-container.component';
import {
  DecimalPosition,
  ParticleSystemEntity,
  UiConfigCollectionControllerClient
} from 'src/app/generated/razarion-share';
import {BabylonRenderServiceAccessImpl} from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import {LocationVisualization} from '../../common/place-config/location-visualization';
import {ParticleSystem, Vector3} from '@babylonjs/core';
import {BabylonModelService} from 'src/app/game/renderer/babylon-model.service';

@Component({
  selector: 'particle-system-editor',
  templateUrl: './particle-system-editor.component.html'
})
export class ParticleSystemEditorComponent implements CrudContainerChild<ParticleSystemEntity> {
  static editorControllerClient = UiConfigCollectionControllerClient;
  particleSystemEntity!: ParticleSystemEntity;
  terrainPosition: DecimalPosition | null = null;
  particleSystem?: ParticleSystem;
  private babylonModelId?: number;
  currentInfo?: string;

  constructor(private rendererService: BabylonRenderServiceAccessImpl,
              private babylonModelService: BabylonModelService
  ) {

  }

  init(particleSystemEntity: ParticleSystemEntity): void {
    this.particleSystemEntity = particleSystemEntity;
  }

  exportConfig(): ParticleSystemEntity {
    return this.particleSystemEntity;
  }

  getId(): number {
    return this.particleSystemEntity.id;
  }

  startParticleSystem() {
    // if (this.terrainPosition) {
    //   const hight = LocationVisualization.getHeightFromTerrain(this.terrainPosition.x, this.terrainPosition.y, this.rendererService)
    //   if (this.particleSystemEntity.threeJsModelId || this.particleSystemEntity.threeJsModelId === 0) {
    //     const emittingPosition = new Vector3(this.terrainPosition.x,
    //       hight,
    //       this.terrainPosition.y);
    //     this.particleSystem = this.rendererService.createParticleSystem(this.particleSystemEntity.threeJsModelId,
    //       this.particleSystemEntity.imageId,
    //       emittingPosition,
    //       null,
    //       false);
    //     this.babylonModelId = this.particleSystemEntity.threeJsModelId;
    //     this.currentInfo = `${this.particleSystemEntity.internalName} '${this.particleSystemEntity.id}' BablyonModel Id '${this.particleSystemEntity.threeJsModelId}'`;
    //     this.particleSystem.name = `Editor ${this.currentInfo}`;
    //     this.particleSystem.start();
    //   }
    // }
  }

  saveParticleSystem() {
    this.babylonModelService.updateParticleSystemJson(this.babylonModelId!, this.particleSystem!);
  }

}
