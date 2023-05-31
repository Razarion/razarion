import {Injectable} from "@angular/core";
import {SlopeConfig, TerrainWaterTile, WaterConfig} from "../../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {Mesh, NodeMaterial, TransformNode} from "@babylonjs/core";
import {BabylonJsUtils} from "./babylon-js.utils";
import {BabylonModelService} from "./babylon-model.service";

@Injectable()
export class ThreeJsWaterRenderService {
  constructor(private gwtAngularService: GwtAngularService,
              private babylonModelService: BabylonModelService) {
  }

  public setup(terrainWaterTiles: TerrainWaterTile[], container: TransformNode): void {
    if (!terrainWaterTiles) {
      return;
    }
    terrainWaterTiles.forEach(terrainWaterTile => {
      let slopeConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getSlopeConfig(terrainWaterTile.slopeConfigId);
      let waterConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getWaterConfig(slopeConfig.getWaterConfigId());


      if (terrainWaterTile.positions) {
        this.setupWater(terrainWaterTile.positions, waterConfig, container);
      }
      if (terrainWaterTile.shallowPositions) {
        this.setupShallowWater(terrainWaterTile.shallowPositions, terrainWaterTile.shallowUvs, slopeConfig, waterConfig, container);
      }
    });
  }

  private setupWater(positions: Float32Array, waterConfig: WaterConfig, container: TransformNode) {
    const vertexData = BabylonJsUtils.createVertexData(positions)
    const water = new Mesh(`Water ${waterConfig.getInternalName()} (${waterConfig.getId()})`, null);
    vertexData.applyToMesh(water)

    water.material = this.babylonModelService.getNodeMaterialNull(waterConfig.getMaterial(), `No material in WaterConfig ${waterConfig.getInternalName()} (${waterConfig.getId()})`);
    (<NodeMaterial>water.material).ignoreAlpha = false; // Can not be saved in the NodeEditor

    water.receiveShadows = true;
    water.parent = container;
    container.getChildren().push(water);
  }

  private setupShallowWater(shallowPositions: Float32Array, shallowUvs: Float32Array, slopeConfig: SlopeConfig, waterConfig: WaterConfig, container: TransformNode) {
    const vertexData = BabylonJsUtils.createVertexData(shallowPositions);
    vertexData.uvs = shallowUvs;

    const shallowWater = new Mesh(`Shallow Water ${waterConfig.getInternalName()} (${waterConfig.getId()}) SlopeConfig ${slopeConfig.getInternalName()} (${slopeConfig.getId()})`, null);
    vertexData.applyToMesh(shallowWater)

    shallowWater.material = this.babylonModelService.getNodeMaterialNull(slopeConfig.getShallowWaterThreeJsMaterial(), `No shallowWaterThreeJsMaterial in SlopeConfig ${slopeConfig.getInternalName()} (${slopeConfig.getId()})`);
    (<NodeMaterial>shallowWater.material).ignoreAlpha = false; // Can not be saved in the NodeEditor

    shallowWater.receiveShadows = true;
    shallowWater.parent = container;
    container.getChildren().push(shallowWater);
  }

}
