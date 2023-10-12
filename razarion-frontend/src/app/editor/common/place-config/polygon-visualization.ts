import {SimpleMaterial} from "@babylonjs/materials";
import {
  Constants,
  Mesh,
  MeshBuilder,
  Node,
  Nullable,
  Observer,
  PlaneDragGizmo,
  PointerEventTypes,
  PointerInfo,
  PolygonMeshBuilder,
  UtilityLayerRenderer,
  Vector2,
  Vector3
} from "@babylonjs/core";
import {BabylonRenderServiceAccessImpl} from "../../../game/renderer/babylon-render-service-access-impl.service";
import {EventEmitter} from "@angular/core";
import {DecimalPosition, PlaceConfig} from "../../../generated/razarion-share";
import {SlopeTerrainEditorComponent} from "../../terrain-editor/slope-terrain-editor.component";
import {PlaceConfigComponent} from "./place-config.component";
import {Color3} from "@babylonjs/core/Maths/math.color";

export class PolygonVisualization {
  static polygonMarkerMaterial: SimpleMaterial;
  polygonMarker?: Mesh;
  vertexAddMode: boolean = false;
  vertexDeleteMode: boolean = false;
  private mouseObservable: Nullable<Observer<PointerInfo>> = null;
  private draggableCorners: DraggableCorner[] = [];
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;

  constructor(private placeConfigComponent: PlaceConfigComponent, private renderService: BabylonRenderServiceAccessImpl, private placeConfigChange: EventEmitter<PlaceConfig | null>) {
    if (!PolygonVisualization.polygonMarkerMaterial) {
      PolygonVisualization.polygonMarkerMaterial = new SimpleMaterial(`Location marker`, renderService.getScene());
      PolygonVisualization.polygonMarkerMaterial.diffuseColor.r = 1;
      PolygonVisualization.polygonMarkerMaterial.diffuseColor.g = 1;
      PolygonVisualization.polygonMarkerMaterial.diffuseColor.b = 0;
      PolygonVisualization.polygonMarkerMaterial.alpha = 0.66;
      PolygonVisualization.polygonMarkerMaterial.depthFunction = Constants.ALWAYS;
    }
    if (placeConfigComponent.visible && placeConfigComponent.placeConfig?.polygon2D?.corners && placeConfigComponent.placeConfig.polygon2D.corners.length > 2) {
      this.setupPolygonMesh();
    }
  }

  onVisibilityChange() {
    if (this.placeConfigComponent.visible && ((this.placeConfigComponent.placeConfig!.polygon2D?.corners?.length || 0) > 2) && !this.polygonMarker) {
      this.setupPolygonMesh();
    } else {
      this.dispose();
    }
  }

  editMode(active: boolean) {
    if (active) {
      this.mouseObservable = this.renderService.getScene().onPointerObservable.add((pointerInfo) => {
        if (pointerInfo.type === PointerEventTypes.POINTERDOWN) {
          let pickingInfo = this.renderService.setupMeshPickPoint();
          if (pickingInfo.hit) {
            if (this.placeConfigComponent.placeConfig?.polygon2D?.corners && this.placeConfigComponent.placeConfig.polygon2D.corners.length > 2) {
              if (this.vertexAddMode) {
                let polygon = PlaceConfigComponent.toVertex2Array(this.placeConfigComponent.placeConfig.polygon2D.corners);
                PolygonVisualization.addPointToPolygon(new Vector2(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z), polygon);
                this.placeConfigComponent.placeConfig.polygon2D.corners = PlaceConfigComponent.toArray2Vertex(polygon);
                if (this.polygonMarker) {
                  this.polygonMarker.dispose();
                }
              }
            } else {
              this.placeConfigComponent.placeConfig!.polygon2D = {corners: [], lines: []};
              this.placeConfigComponent.placeConfig!.polygon2D!.corners = [
                {
                  x: pickingInfo.pickedPoint!.x - PlaceConfigComponent.NEW_POLYGON_HALF_LENGTH,
                  y: pickingInfo.pickedPoint!.z - PlaceConfigComponent.NEW_POLYGON_HALF_LENGTH
                },
                {
                  x: pickingInfo.pickedPoint!.x + PlaceConfigComponent.NEW_POLYGON_HALF_LENGTH,
                  y: pickingInfo.pickedPoint!.z - PlaceConfigComponent.NEW_POLYGON_HALF_LENGTH
                },
                {
                  x: pickingInfo.pickedPoint!.x + PlaceConfigComponent.NEW_POLYGON_HALF_LENGTH,
                  y: pickingInfo.pickedPoint!.z + PlaceConfigComponent.NEW_POLYGON_HALF_LENGTH
                },
                {
                  x: pickingInfo.pickedPoint!.x - PlaceConfigComponent.NEW_POLYGON_HALF_LENGTH,
                  y: pickingInfo.pickedPoint!.z + PlaceConfigComponent.NEW_POLYGON_HALF_LENGTH
                }
              ];
            }
            this.setupPolygonMesh();
            this.rebuildDraggableCorners();
            this.placeConfigChange.emit(this.placeConfigComponent.placeConfig)
          }
        }
      });
      this.pointerObservable = UtilityLayerRenderer.DefaultUtilityLayer.utilityLayerScene.onPointerObservable.add(pointerInfo => {
        if (pointerInfo.type === PointerEventTypes.POINTERDOWN) {
          if (this.vertexDeleteMode) {
            let pickingInfo = UtilityLayerRenderer.DefaultUtilityLayer.utilityLayerScene.pick(UtilityLayerRenderer.DefaultUtilityLayer.utilityLayerScene.pointerX, UtilityLayerRenderer.DefaultUtilityLayer.utilityLayerScene.pointerY);
            let draggableCorner = this.recursivelyFindDraggableCorner(pickingInfo.pickedMesh);
            if (draggableCorner) {
              this.placeConfigComponent.placeConfig!.polygon2D?.corners.splice(draggableCorner.index, 1);
              this.setupPolygonMesh();
              this.rebuildDraggableCorners();
              this.placeConfigChange.emit(this.placeConfigComponent.placeConfig)
            }

          }
        }
      });
      this.rebuildDraggableCorners();
    } else {
      this.vertexAddMode = false;
      this.vertexDeleteMode = false;
      if (this.mouseObservable) {
        this.renderService.getScene().onPointerObservable.remove(this.mouseObservable);
        this.mouseObservable = null;
      }
      if (this.pointerObservable) {
        UtilityLayerRenderer.DefaultUtilityLayer.utilityLayerScene.onPointerObservable.remove(this.pointerObservable)
        this.pointerObservable = null;
      }
      this.draggableCorners.forEach(draggableCorner => draggableCorner.dispose());
      this.draggableCorners = [];
    }
  }

  dispose() {
    if (this.mouseObservable) {
      this.renderService.getScene().onPointerObservable.remove(this.mouseObservable);
      this.mouseObservable = null;
    }
    if (this.pointerObservable) {
      UtilityLayerRenderer.DefaultUtilityLayer.utilityLayerScene.onPointerObservable.remove(this.pointerObservable)
      this.pointerObservable = null;
    }
    if (this.polygonMarker) {
      this.polygonMarker.dispose();
      this.polygonMarker = undefined;
    }
    this.draggableCorners.forEach(draggableCorner => draggableCorner.dispose());
    this.draggableCorners = [];
  }

  private setupPolygonMesh() {
    if (this.polygonMarker) {
      this.polygonMarker.dispose();
    }
    let polygon = PlaceConfigComponent.toVertex2Array(this.placeConfigComponent.placeConfig!.polygon2D!.corners);
    const polygonMeshBuilder = new PolygonMeshBuilder(`Polygon marker`, polygon, this.renderService.getScene(), SlopeTerrainEditorComponent.EAR_CUT);
    this.polygonMarker = polygonMeshBuilder.build();
    this.polygonMarker.material = PolygonVisualization.polygonMarkerMaterial;
  }

  public static addPointToPolygon(point: Vector2, polygon: Vector2[]) {
    let index = SlopeTerrainEditorComponent.projectPointToPolygon(point, polygon);
    if (!index && index !== 0) {
      throw new Error("Invalid Polygon");
    }
    polygon.splice(index, 0, point);
  }

  private rebuildDraggableCorners() {
    this.draggableCorners.forEach(draggableCorner => draggableCorner.dispose());
    this.draggableCorners = [];
    if ((this.placeConfigComponent.placeConfig!.polygon2D?.corners?.length || 0) < 3) {
      return;
    }
    for (let i = 0; i < this.placeConfigComponent.placeConfig!.polygon2D!.corners.length; i++) {
      this.draggableCorners.push(new DraggableCorner(this.renderService, this.placeConfigComponent!.placeConfig!.polygon2D!.corners!, i, () => {
        this.setupPolygonMesh();
      }));
    }
  }

  private recursivelyFindDraggableCorner(pickedMesh: Nullable<Node>): DraggableCorner | undefined {
    if (!pickedMesh) {
      return undefined;
    }
    if (pickedMesh.metadata?.DRAGGABLE_CORNER) {
      return pickedMesh.metadata.DRAGGABLE_CORNER;
    }
    return this.recursivelyFindDraggableCorner(pickedMesh.parent);
  }
}

class DraggableCorner {
  static cornerDiscMaterial: SimpleMaterial;
  private readonly planeDragGizmo;
  public readonly disc;

  constructor(private renderService: BabylonRenderServiceAccessImpl, corners: DecimalPosition[], public readonly index: number, onChange: () => any) {
    if (!DraggableCorner.cornerDiscMaterial) {
      DraggableCorner.cornerDiscMaterial = new SimpleMaterial(`Location marker disc`, renderService.getScene());
      DraggableCorner.cornerDiscMaterial.diffuseColor.r = 1;
      DraggableCorner.cornerDiscMaterial.diffuseColor.g = 1;
      DraggableCorner.cornerDiscMaterial.diffuseColor.b = 0.5;
      DraggableCorner.cornerDiscMaterial.depthFunction = Constants.ALWAYS;
    }
    this.disc = MeshBuilder.CreateBox("Slope Editor Corner", {size: 0.1});
    this.disc.material = DraggableCorner.cornerDiscMaterial;
    this.disc.position.x = corners[index].x;
    this.disc.position.y = 0;
    this.disc.position.z = corners[index].y;

    this.planeDragGizmo = new PlaneDragGizmo(new Vector3(0, 1, 0), new Color3(1, 0, 0));
    this.planeDragGizmo.attachedMesh = this.disc;
    if (!this.planeDragGizmo._rootMesh.metadata) {
      this.planeDragGizmo._rootMesh.metadata = {};
    }
    this.planeDragGizmo._rootMesh.metadata.DRAGGABLE_CORNER = this;


    this.disc.onAfterWorldMatrixUpdateObservable.add(() => {
      corners[index].x = this.disc.position.x;
      corners[index].y = this.disc.position.z;
      onChange();
    });
  }

  dispose(): void {
    this.disc.dispose();
    this.planeDragGizmo.dispose();
  }

}
