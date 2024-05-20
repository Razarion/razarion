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

  public setup(index: Index, waterConfig: WaterConfig, container: TransformNode): void {
    const water = MeshBuilder.CreateGround("Water", {width: BabylonTerrainTileImpl.NODE_X_COUNT, 
      height: BabylonTerrainTileImpl.NODE_X_COUNT,
      subdivisions:160});

    water.material = this.babylonModelService.getNodeMaterialNull(waterConfig.getMaterial(), `No material in WaterConfig ${waterConfig.getInternalName()} (${waterConfig.getId()})`);
    (<NodeMaterial>water.material).ignoreAlpha = false; // Can not be saved in the NodeEditor

    water.receiveShadows = true;
    water.parent = container;
    water.position.x = index.getX() * BabylonTerrainTileImpl.NODE_X_COUNT + BabylonTerrainTileImpl.NODE_X_COUNT / 2;
    water.position.z = index.getY() * BabylonTerrainTileImpl.NODE_Y_COUNT + BabylonTerrainTileImpl.NODE_Y_COUNT / 2;
    container.getChildren().push(water);
  }

}
