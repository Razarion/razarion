import { Component } from '@angular/core';
import { BabylonRenderServiceAccessImpl, RazarionMetadataType } from "../../../game/renderer/babylon-render-service-access-impl.service";
import { ObjectNameId, TerrainObjectModel } from "../../../gwtangular/GwtAngularFacade";
import { GeneratorItem } from "./generator-item";
import { BabylonTerrainTileImpl } from 'src/app/game/renderer/babylon-terrain-tile.impl';
import { GwtInstance } from 'src/app/gwtangular/GwtInstance';
import { BabylonModelService } from 'src/app/game/renderer/babylon-model.service';
import { GwtAngularService } from 'src/app/gwtangular/GwtAngularService';
import { PickingInfo } from '@babylonjs/core/Collisions/pickingInfo';
import { EditorService } from '../../editor-service';
import { GeneratedTerrainObjects } from './generated-terrain-objects';
import { TransformNode } from '@babylonjs/core';

@Component({
  selector: 'terrain-object-generator',
  templateUrl: './terrain-object-generator.component.html'
})
export class TerrainObjectGeneratorComponent {
  terrainObjectConfigs: { objectNameId: ObjectNameId, name: string }[] = [];
  generatorItems: GeneratorItem[] = [];
  groundConfigs: { id: number, name: string }[] = [];
  excludedGroundConfigs: { entry?: { id: number, name: string } }[] = [];
  generatedTerrainObjectsEntries: GeneratedTerrainObjects[] = [];
  private terrainObjectCallback!: (terrainObjectModel: TerrainObjectModel, node: TransformNode) => void;

  constructor(private renderEngine: BabylonRenderServiceAccessImpl,
    private babylonModelService: BabylonModelService,
    private gwtAngularService: GwtAngularService,
    editorService: EditorService) {
    editorService.readGroundObjectNameIds().then(objectNameIds => {
      this.groundConfigs = [];
      objectNameIds.forEach(objectNameId => {
        this.groundConfigs.push({ name: `${objectNameId.internalName} '${objectNameId.id}'`, id: objectNameId.id });
      });
    })
  }

  init(terrainObjectConfigs: { objectNameId: ObjectNameId, name: string }[], terrainObjectCallback: (terrainObjectModel: TerrainObjectModel, node: TransformNode) => void) {
    this.terrainObjectConfigs = terrainObjectConfigs;
    this.terrainObjectCallback = terrainObjectCallback;
  }

  generate(): void {
    let generatedTerrainObjects = new GeneratedTerrainObjects();
    this.generatedTerrainObjectsEntries.push(generatedTerrainObjects);

    for (let generatorItem of this.generatorItems) {
      let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(generatorItem!.terrainObjectConfig!.objectNameId.id);
      generatedTerrainObjects.terrainObjectConfigs += `${terrainObjectConfig.getInternalName()} '${terrainObjectConfig.getId()}'`;


      for (let i = 0; i < generatorItem.count; i++) {
        let randomX = Math.random() * 2 - 1;
        let randomY = Math.random() * 2 - 1;

        let pickingInfo = this.renderEngine.setupPickInfoFromNDC(randomX, randomY);
        if (pickingInfo.hit) {
          if (this.isGroundRestricted(pickingInfo)) {
            continue;
          }

          let rotation = GwtInstance.newVertex(
            this.generateRandomRadian(generatorItem.xRot),
            this.generateRandomRadian(generatorItem.yRot),
            this.generateRandomRadian(generatorItem.zRot));

          let scale = this.generateRandomScale(generatorItem.minScale, generatorItem.maxScale);

          let terrainObjectModel = new class implements TerrainObjectModel {
            position = GwtInstance.newVertex(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z, pickingInfo.pickedPoint!.y);
            rotation = rotation;
            scale = GwtInstance.newVertex(scale, scale, scale);
            terrainObjectId = terrainObjectConfig.getId();
          }
          let newTerrainObjectMesh = BabylonTerrainTileImpl.createTerrainObject(terrainObjectModel, terrainObjectConfig, this.babylonModelService, null);
          this.renderEngine.addShadowCaster(newTerrainObjectMesh);
          generatedTerrainObjects.generatedObjects.push({ mesh: newTerrainObjectMesh, model: terrainObjectModel });
          generatedTerrainObjects.count++;
        }
      }
    }
  }

  private isGroundRestricted(pickingInfo: PickingInfo): boolean {
    let node = BabylonRenderServiceAccessImpl.findRazarionMetadataNode(pickingInfo.pickedMesh!);
    if (!node) {
      return true;
    }
    let metadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(node);
    if (!metadata) {
      return true
    }
    if (metadata.type !== RazarionMetadataType.GROUND) {
      return true;
    }
    return !!this.excludedGroundConfigs.find(excludedGroundConfig => excludedGroundConfig.entry?.id === metadata!.configId);
  }

  private generateRandomRadian(maxDegree: number): number {
    if (maxDegree === 0) {
      return 0;
    }

    if (maxDegree < 0 || maxDegree > 180) {
      throw new Error('Input degree must be between 0 and 180');
    }

    let maxRadian = maxDegree * Math.PI / 180;

    let randomRadian = Math.random() * maxRadian;

    randomRadian *= (Math.random() < 0.5 ? -1 : 1);

    return randomRadian;
  }

  private generateRandomScale(minScale: number, maxScale: number): number {
    if (minScale > maxScale) {
      throw new Error('minScale must be less than or equal to maxScale');
    }

    return Math.random() * (maxScale - minScale) + minScale;
  }

  deleteGeneratedTerrainObjectsEtry(generatedTerrainObjects: GeneratedTerrainObjects) {
    generatedTerrainObjects.generatedObjects.forEach(terrainObjectMesh => {
      terrainObjectMesh.mesh.dispose();
    });
    this.generatedTerrainObjectsEntries.splice(this.generatedTerrainObjectsEntries.indexOf(generatedTerrainObjects), 1);
  }

  moveGeneratedTerrainObjectsEtry(generatedTerrainObjects: GeneratedTerrainObjects) {
    generatedTerrainObjects.generatedObjects.forEach(generatedObjects => {
      this.terrainObjectCallback(generatedObjects.model, generatedObjects.mesh);
    });
    this.generatedTerrainObjectsEntries.splice(this.generatedTerrainObjectsEntries.indexOf(generatedTerrainObjects), 1);
  }

  addGeneratorItem(): void {
    this.generatorItems.push(new GeneratorItem);
  }

  deleteGeneratorItem(generatorItem: GeneratorItem): void {
    this.generatorItems.splice(this.generatorItems.indexOf(generatorItem), 1);
  }

  addExcludedGround(): void {
    this.excludedGroundConfigs.push({ entry: undefined });
  }

  deleteExcludedGround(excludedGroundConfig: { entry?: { id: number, name: string } }): void {
    this.excludedGroundConfigs.splice(this.excludedGroundConfigs.indexOf(excludedGroundConfig), 1);
  }
}
