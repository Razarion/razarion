import {Injectable} from "@angular/core";
import {GroundConfig, Index} from "../../gwtangular/GwtAngularFacade";
import {
  ActionManager,
  CubeTexture,
  ExecuteCodeAction,
  MeshBuilder,
  NodeMaterial,
  TransformNode,
  VertexBuffer
} from "@babylonjs/core";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonTerrainTileImpl} from "./babylon-terrain-tile.impl";
import type {FloatArray} from '@babylonjs/core/types';
import {ReflectionTextureBaseBlock} from '@babylonjs/core/Materials/Node/Blocks/Dual/reflectionTextureBaseBlock';
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {ActionService, SelectionInfo} from '../action.service';

@Injectable({
  providedIn: 'root'
})
export class BabylonWaterRenderService {

  constructor(private babylonModelService: BabylonModelService,
              private actionService: ActionService) {
  }

  public setup(index: Index, groundConfig: GroundConfig, container: TransformNode, uv2GroundHeightMap: FloatArray, rendererService: BabylonRenderServiceAccessImpl): void {
    const water = MeshBuilder.CreateGround("Water", {
      width: BabylonTerrainTileImpl.NODE_X_COUNT,
      height: BabylonTerrainTileImpl.NODE_Y_COUNT,
      subdivisions: 160
    });

    water.material = this.babylonModelService.getBabylonMaterial(groundConfig.getWaterBabylonMaterialId());
    (<NodeMaterial>water.material).ignoreAlpha = false; // Can not be saved in the NodeEditor
    (<ReflectionTextureBaseBlock>(<NodeMaterial>water.material).getBlockByName("Reflection")).texture = CubeTexture.CreateFromImages([
      "renderer/env/clouds.jpg", // +X
      "renderer/env/clouds.jpg", // +Y
      "renderer/env/clouds.jpg", // +Z
      "renderer/env/clouds.jpg", // -X
      "renderer/env/clouds.jpg", // -Y
      "renderer/env/clouds.jpg", // -Z
    ], rendererService.getScene());

    water.setVerticesData(VertexBuffer.UV2Kind, uv2GroundHeightMap)
    water.receiveShadows = true;
    water.parent = container;
    water.position.x = index.getX() * BabylonTerrainTileImpl.NODE_X_COUNT + BabylonTerrainTileImpl.NODE_X_COUNT / 2;
    water.position.z = index.getY() * BabylonTerrainTileImpl.NODE_Y_COUNT + BabylonTerrainTileImpl.NODE_Y_COUNT / 2;
    container.getChildren().push(water);

    let actionManager = new ActionManager(rendererService.getScene());
    actionManager.registerAction(
      new ExecuteCodeAction(
        ActionManager.OnPickTrigger,
        () => {
          let pickingInfo = rendererService.setupMeshPickPoint();
          if (pickingInfo.hit) {
            this.actionService.onTerrainClicked(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z);
          }
        }
      )
    );
    const cursorTypeHandler: (selectionInfo: SelectionInfo) => void = (selectionInfo: SelectionInfo) => {
      if (selectionInfo.hasOwnMovable) {
        actionManager.hoverCursor = "url(\"cursors/go.png\") 15 15, auto"
      } else {
        actionManager.hoverCursor = "default"
      }
    }
    this.actionService.addCursorHandler(cursorTypeHandler);

    water.actionManager = actionManager;
  }

}
