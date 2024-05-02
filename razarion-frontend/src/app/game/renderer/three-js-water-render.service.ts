import { Injectable } from "@angular/core";
import { Index, WaterConfig } from "../../gwtangular/GwtAngularFacade";
import { GwtAngularService } from "../../gwtangular/GwtAngularService";
import { MeshBuilder, NodeMaterial, Tools, TransformNode, VertexBuffer } from "@babylonjs/core";
import { BabylonModelService } from "./babylon-model.service";
import { BabylonTerrainTileImpl } from "./babylon-terrain-tile.impl";

@Injectable()
export class ThreeJsWaterRenderService {
  constructor(private gwtAngularService: GwtAngularService,
    private babylonModelService: BabylonModelService) {
  }

  public setup2(index: Index, waterConfig: WaterConfig, container: TransformNode): void {
    const water = MeshBuilder.CreateGround("Water", {width: BabylonTerrainTileImpl.TILE_X_SIZE, 
      height: BabylonTerrainTileImpl.TILE_X_SIZE,
      subdivisions:160});

    water.material = this.babylonModelService.getNodeMaterialNull(waterConfig.getMaterial(), `No material in WaterConfig ${waterConfig.getInternalName()} (${waterConfig.getId()})`);
    (<NodeMaterial>water.material).ignoreAlpha = false; // Can not be saved in the NodeEditor

    water.receiveShadows = true;
    water.parent = container;
    water.position.x = index.getX() * BabylonTerrainTileImpl.TILE_X_SIZE;
    water.position.z = index.getY() * BabylonTerrainTileImpl.TILE_Y_SIZE;
    container.getChildren().push(water);
  }

}
