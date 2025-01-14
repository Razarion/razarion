import {Injectable} from "@angular/core";
import {GroundConfig, Index} from "../../gwtangular/GwtAngularFacade";
import {MeshBuilder, NodeMaterial, TransformNode} from "@babylonjs/core";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonTerrainTileImpl} from "./babylon-terrain-tile.impl";

@Injectable()
export class ThreeJsWaterRenderService {
  constructor(private babylonModelService: BabylonModelService) {
  }

  public setup(index: Index, groundConfig: GroundConfig, container: TransformNode): void {
    const water = MeshBuilder.CreateGround("Water", {
      width: BabylonTerrainTileImpl.NODE_X_COUNT,
      height: BabylonTerrainTileImpl.NODE_Y_COUNT,
      subdivisions: 160
    });

    water.material = this.babylonModelService.getBabylonMaterial(groundConfig.getWaterBabylonMaterialId());
    (<NodeMaterial>water.material).ignoreAlpha = false; // Can not be saved in the NodeEditor

    water.receiveShadows = true;
    water.parent = container;
    water.position.x = index.getX() * BabylonTerrainTileImpl.NODE_X_COUNT + BabylonTerrainTileImpl.NODE_X_COUNT / 2;
    water.position.z = index.getY() * BabylonTerrainTileImpl.NODE_Y_COUNT + BabylonTerrainTileImpl.NODE_Y_COUNT / 2;
    container.getChildren().push(water);
  }

}
