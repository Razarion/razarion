import {Component, Input, OnInit} from '@angular/core';
import {TerrainObjectEditorControllerClient, TerrainObjectGeneratorEntity} from "../../../../generated/razarion-share";
import {GeneratorItem} from "../generator-item";
import {HttpClient} from "@angular/common/http";
import {TypescriptGenerator} from "../../../../backend/typescript-generator";

@Component({
    selector: 'terrain-object-generator-entity',
    templateUrl: './terrain-object-generator-entity.component.html'
})
export class TerrainObjectGeneratorEntityComponent implements OnInit {
  _terrainObjectGeneratorEntity: TerrainObjectGeneratorEntity | null = null;
  generatorItems: GeneratorItem[] = [];
  terrainObjectConfigs: { terrainObjectId: number, name: string }[] = [];
  private terrainObjectEditorControllerClient: TerrainObjectEditorControllerClient;

  constructor(httpClient: HttpClient) {
    this.terrainObjectEditorControllerClient = new TerrainObjectEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  @Input("terrainObjectGeneratorEntity")
  set terrainObjectGeneratorEntity(terrainObjectGeneratorEntity: TerrainObjectGeneratorEntity) {
    this._terrainObjectGeneratorEntity = terrainObjectGeneratorEntity;
    this.generatorItems = JSON.parse(terrainObjectGeneratorEntity.generatorJson);
    if (!this.generatorItems) {
      this.generatorItems = [];
    }
  }

  get terrainObjectGeneratorEntity(): TerrainObjectGeneratorEntity | null {
    return this._terrainObjectGeneratorEntity;
  }

  ngOnInit(): void {
    this.terrainObjectEditorControllerClient
      .getObjectNameIds()
      .then(objectNameIds => {
        this.terrainObjectConfigs = [];
        objectNameIds.forEach(objectNameId => {
          this.terrainObjectConfigs.push({
            name: `${objectNameId.internalName} '${objectNameId.id}'`,
            terrainObjectId: objectNameId.id
          })
        });
      })
  }

  addGeneratorItem(): void {
    this.generatorItems.push(new GeneratorItem);
  }

  deleteGeneratorItem(generatorItem: GeneratorItem): void {
    this.generatorItems.splice(this.generatorItems.indexOf(generatorItem), 1);
  }

  updateGeneratorJson() {
    this._terrainObjectGeneratorEntity!.generatorJson = JSON.stringify(this.generatorItems);
  }

}
