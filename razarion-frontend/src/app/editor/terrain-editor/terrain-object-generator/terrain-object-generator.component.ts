import {Component, OnInit, ViewChild} from '@angular/core';
import {TerrainObjectGeneratorControllerClient, TerrainObjectGeneratorEntity} from "../../../generated/razarion-share";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {TypescriptGenerator} from "../../../backend/typescript-generator";
import {TerrainObjectModel} from "../../../gwtangular/GwtAngularFacade";
import {TransformNode} from "@babylonjs/core";
import {GeneratedTerrainObjects} from "./generated-terrain-objects";
import {GwtInstance} from "../../../gwtangular/GwtInstance";
import {BabylonTerrainTileImpl} from "../../../game/renderer/babylon-terrain-tile.impl";
import {BabylonRenderServiceAccessImpl} from "../../../game/renderer/babylon-render-service-access-impl.service";
import {BabylonModelService} from "../../../game/renderer/babylon-model.service";
import {GwtAngularService} from "../../../gwtangular/GwtAngularService";
import {EditorService} from "../../editor-service";
import {
  TerrainObjectGeneratorEntityComponent
} from "./terrain-object-generator-entity/terrain-object-generator-entity.component";
import {Button} from 'primeng/button';
import {DatePipe} from '@angular/common';
import {TableModule} from 'primeng/table';
import {FormsModule} from '@angular/forms';
import {Divider} from 'primeng/divider';
import {SelectChangeEvent, SelectModule} from 'primeng/select';

@Component({
  selector: 'terrain-object-generator',
  imports: [
    Button,
    DatePipe,
    TableModule,
    TerrainObjectGeneratorEntityComponent,
    SelectModule,
    FormsModule,
    Divider
  ],
  templateUrl: './terrain-object-generator.component.html'
})
export class TerrainObjectGeneratorComponent implements OnInit {
  activeTerrainObjectGenerator: TerrainObjectGeneratorEntity | null = null;
  terrainObjectGeneratorOptions: { id: number, name: string }[] = [{id: -999999, name: "Dummy"}];
  terrainObjectGeneratorId? = this.terrainObjectGeneratorOptions[0];
  generatedTerrainObjectsEntries: GeneratedTerrainObjects[] = [];
  @ViewChild('terrainObjectGeneratorEntityComponent')
  terrainObjectGeneratorEntityComponent!: TerrainObjectGeneratorEntityComponent;
  groundConfigs: { id: number, name: string }[] = [];

  private terrainObjectCallback!: (terrainObjectModel: TerrainObjectModel, node: TransformNode) => void;

  private terrainObjectGeneratorControllerClient: TerrainObjectGeneratorControllerClient;

  constructor(private renderEngine: BabylonRenderServiceAccessImpl,
              private babylonModelService: BabylonModelService,
              private gwtAngularService: GwtAngularService,
              editorService: EditorService,
              httpClient: HttpClient,
              private messageService: MessageService) {
    this.terrainObjectGeneratorControllerClient = new TerrainObjectGeneratorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
    editorService.readGroundObjectNameIds().then(objectNameIds => {
      this.groundConfigs = [];
      objectNameIds.forEach(objectNameId => {
        this.groundConfigs.push({name: `${objectNameId.internalName} '${objectNameId.id}'`, id: objectNameId.id});
      });
    })
  }

  ngOnInit(): void {
    this.loadTerrainObjectGeneratorObjectNameIds();
  }

  init(terrainObjectCallback: (terrainObjectModel: TerrainObjectModel, node: TransformNode) => void) {
    this.terrainObjectCallback = terrainObjectCallback;
  }

  private loadTerrainObjectGeneratorObjectNameIds(): void {
    this.terrainObjectGeneratorControllerClient
      .getObjectNameIds()
      .then(objectNameIds => {
        this.terrainObjectGeneratorOptions = [];
        objectNameIds.forEach(objectNameId => {
          this.terrainObjectGeneratorOptions.push({
            id: objectNameId.id,
            name: `${objectNameId.internalName} '${objectNameId.id}'`
          });
          this.terrainObjectGeneratorId = this.terrainObjectGeneratorOptions[0];
        })
      }).catch(err => {
      this.messageService.add({
        severity: 'error',
        summary: `Failed loading terrain object generators`,
        detail: err.message,
        sticky: true
      });
    });
  }

  onTerrainObjectGeneratorChange(event: SelectChangeEvent) {
    this.terrainObjectGeneratorControllerClient
      .read(event.value)
      .then(value => this.activeTerrainObjectGenerator = value)
      .catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Failed to read  a TerrainObjectGeneratorEntity`,
          detail: err.message,
          sticky: true
        })
      });
  }

  onCreateTerrainObjectGenerator() {
    this.terrainObjectGeneratorControllerClient
      .create()
      .then(terrainObjectGeneratorEntity => {
        this.activeTerrainObjectGenerator = terrainObjectGeneratorEntity;
        this.terrainObjectGeneratorId = {
          id: terrainObjectGeneratorEntity.id,
          name: `${terrainObjectGeneratorEntity.internalName} '${terrainObjectGeneratorEntity.id}'`
        };
        this.loadTerrainObjectGeneratorObjectNameIds();
      }).catch(err => {
      this.messageService.add({
        severity: 'error',
        summary: `Failed creating a brush`,
        detail: err.message,
        sticky: true
      });
    });

  }

  onSaveTerrainObjectGenerator() {
    this.terrainObjectGeneratorEntityComponent.updateGeneratorJson();
    this.terrainObjectGeneratorControllerClient
      .update(this.activeTerrainObjectGenerator!)
      .then(() => {
        this.messageService.add({
          severity: 'success',
          summary: `TerrainObjectGeneratorEntity saved`,
        });
      })
      .catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Failed to save a terrainObjectGeneratorEntity`,
          detail: err.message,
          sticky: true
        });
      });
  }

  onDeleteTerrainObjectGenerator() {
    this.terrainObjectGeneratorControllerClient
      .delete(this.activeTerrainObjectGenerator!.id)
      .then(() => {
        this.loadTerrainObjectGeneratorObjectNameIds();
        this.activeTerrainObjectGenerator = null;
        this.terrainObjectGeneratorId = undefined;
        this.messageService.add({
          severity: 'success',
          summary: `TerrainObjectGeneratorEntity deleted`,
        });
      })
      .catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Failed to delete a terrainObjectGeneratorEntity`,
          detail: err.message,
          sticky: true
        });
      });
  }

  generate(): void {
    let generatedTerrainObjects = new GeneratedTerrainObjects();
    this.generatedTerrainObjectsEntries.push(generatedTerrainObjects);

    for (let generatorItem of this.terrainObjectGeneratorEntityComponent.generatorItems) {
      let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(generatorItem!.terrainObjectConfig!);
      generatedTerrainObjects.terrainObjectConfigs += `${terrainObjectConfig.getInternalName()} '${terrainObjectConfig.getId()}'`;


      for (let i = 0; i < generatorItem.count; i++) {
        let randomX = Math.random() * 2 - 1;
        let randomY = Math.random() * 2 - 1;

        let pickingInfo = this.renderEngine.setupPickInfoFromNDC(randomX, randomY);
        if (pickingInfo.hit) {
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
          generatedTerrainObjects.generatedObjects.push({mesh: newTerrainObjectMesh, model: terrainObjectModel});
          generatedTerrainObjects.count++;
        }
      }
    }
  }

  deleteGeneratedTerrainObjectsEntry(generatedTerrainObjects: GeneratedTerrainObjects) {
    generatedTerrainObjects.generatedObjects.forEach(terrainObjectMesh => {
      terrainObjectMesh.mesh.dispose();
    });
    this.generatedTerrainObjectsEntries.splice(this.generatedTerrainObjectsEntries.indexOf(generatedTerrainObjects), 1);
  }

  moveGeneratedTerrainObjectsEntry(generatedTerrainObjects: GeneratedTerrainObjects) {
    generatedTerrainObjects.generatedObjects.forEach(generatedObjects => {
      this.terrainObjectCallback(generatedObjects.model, generatedObjects.mesh);
    });
    this.generatedTerrainObjectsEntries.splice(this.generatedTerrainObjectsEntries.indexOf(generatedTerrainObjects), 1);
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

}
