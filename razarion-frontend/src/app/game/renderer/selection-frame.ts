import { MeshBuilder, Nullable, PointerEventTypes, Scene, Vector2, Vector3 } from "@babylonjs/core";
import { LinesMesh } from "@babylonjs/core/Meshes/linesMesh";
import { BabylonRenderServiceAccessImpl } from "./babylon-render-service-access-impl.service";
import { GwtAngularService } from "src/app/gwtangular/GwtAngularService";

export class SelectionFrame {
  private readonly MIN_DISTANCE = 0.5;
  private mousePos0: Vector2 | undefined;
  private mousePos1: Vector2 | undefined;
  private lines: LinesMesh | undefined;
  private startTerrainPosition: Nullable<Vector3> = null;

  constructor(private scene: Scene,
    private renderService: BabylonRenderServiceAccessImpl,
    private gwtAngularService: GwtAngularService) {
    this.scene.onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERDOWN: {
          if(renderService.baseItemPlacerActive) {
            return;
          }
          this.onPointerDown(this.scene.pointerX, this.scene.pointerY);
          break;
        }
        case PointerEventTypes.POINTERUP: {
          if(renderService.baseItemPlacerActive) {
            return;
          }
          this.onPointerUp(this.scene.pointerX, this.scene.pointerY);
          break;
        }
        case PointerEventTypes.POINTERMOVE: {
          this.onPointerMove(this.scene.pointerX, this.scene.pointerY);
          break;
        }
      }
    });
  }

  private onPointerDown(x: number, y: number) {
    this.mousePos0 = new Vector2(x, y);
    let pickingInfo = this.renderService.setupTerrainPickPoint();
    if (pickingInfo.hit) {
      this.startTerrainPosition = pickingInfo.pickedPoint;
    }
  }

  private onPointerMove(x: number, y: number) {
    if (!this.mousePos0) {
      return;
    }

    if (!this.lines) {
      this.lines = this.setupLines();
    }

    this.mousePos1 = new Vector2(x, y);
    let rayBL = this.scene.createPickingRay(this.mousePos0.x, this.mousePos0.y, null, null)
    let rayBR = this.scene.createPickingRay(this.mousePos1.x, this.mousePos0.y, null, null)
    let rayTL = this.scene.createPickingRay(this.mousePos0.x, this.mousePos1.y, null, null)

    let bl = rayBL.origin.add(rayBL.direction);
    let br = rayBR.origin.add(rayBR.direction);
    let tl = rayTL.origin.add(rayTL.direction);

    this.lines.position.copyFrom(bl);
    this.lines.scaling.x = Vector3.Distance(bl, br);
    this.lines.scaling.y = -Vector3.Distance(bl, tl);

    this.lines.lookAt(br, -Math.PI * 0.5);

    if (this.mousePos1.y < this.mousePos0.y) {
      this.lines.scaling.y *= -1;
    }
  }

  private onPointerUp(x: number, y: number) {
    if (this.lines) {
      this.lines.dispose();
      this.lines = undefined;
    }
    this.mousePos0 = undefined;

    if (!this.startTerrainPosition) {
      console.warn("No startTerrainPosition in SelectionFrame")
      return;
    }
    let pickingInfo = this.renderService.setupTerrainPickPoint();
    if (!pickingInfo.hit) {
      console.warn("No pickingInfo in SelectionFrame")
      return;
    }
    let endTerrainPosition = pickingInfo.pickedPoint!;

    if (Math.abs(this.startTerrainPosition.x - endTerrainPosition.x) < this.MIN_DISTANCE &&
      Math.abs(this.startTerrainPosition.z - endTerrainPosition.z) < this.MIN_DISTANCE) {
      return;
    }

    this.gwtAngularService.gwtAngularFacade.selectionService.selectRectangle(
      Math.min(this.startTerrainPosition.x, endTerrainPosition.x),
      Math.min(this.startTerrainPosition.z, endTerrainPosition.z),
      Math.abs(this.startTerrainPosition.x - endTerrainPosition.x),
      Math.abs(this.startTerrainPosition.z - endTerrainPosition.z),
    );
  }

  private setupLines() {
    let lines = MeshBuilder.CreateLines("Selection Frame",
      {
        points: [
          new Vector3(0, 0, 0),
          new Vector3(1, 0, 0),
          new Vector3(1, 1, 0),
          new Vector3(0, 1, 0),
          new Vector3(0, 0, 0)
        ]
      }, this.scene);
    lines.billboardMode = 7;
    return lines;
  }
}
