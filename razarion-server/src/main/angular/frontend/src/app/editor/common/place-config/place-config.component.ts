import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DecimalPosition, PlaceConfig} from "../../../generated/razarion-share";
import {BabylonRenderServiceAccessImpl} from "../../../game/renderer/babylon-render-service-access-impl.service";
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
import {SimpleMaterial} from "@babylonjs/materials";
import {SlopeEditorComponent} from "../../terrain-editor/slope-editor.component";
import {Color3} from "@babylonjs/core/Maths/math.color";

@Component({
  selector: 'place-config',
  templateUrl: './place-config.component.html',
  styleUrls: ['./place-config.component.scss']
})
export class PlaceConfigComponent implements OnInit {
  static readonly NEW_POLYGON_HALF_LENGTH = 4;
  @Input("placeConfig")
  placeConfig: PlaceConfig | null = null;
  @Output()
  placeConfigChange = new EventEmitter<PlaceConfig>();
  locationMode = false
  locationVisualization?: LocationVisualization;
  polygonVisualization?: PolygonVisualization;

  constructor(private renderService: BabylonRenderServiceAccessImpl) {
  }

  ngOnInit(): void {
    if (this.placeConfig) {
      if (this.placeConfig.polygon2D) {
        this.locationMode = false;
      } else if (this.placeConfig.position) {
        this.locationMode = true;
      }
    }
  }

  onEditLocationButton(active: boolean) {
    if (active && !this.locationVisualization) {
      this.locationVisualization = new LocationVisualization(this.placeConfig!, this.renderService, this.placeConfigChange);
    } else if (!active && this.locationVisualization) {
      this.locationVisualization.dispose();
      this.locationVisualization = undefined
    }
  }

  onEditPolygonButton(active: boolean) {
    if (active && !this.polygonVisualization) {
      this.polygonVisualization = new PolygonVisualization(this.placeConfig!, this.renderService, this.placeConfigChange);
    } else if (!active && this.polygonVisualization) {
      this.polygonVisualization.dispose();
      this.polygonVisualization = undefined
    }
  }

  get locationPositionX(): number {
    return this.locationVisualization?.locationMarker?.position?.x || this.placeConfig?.position?.x || 0;
  }

  set locationPositionX(value: number) {
    if (this.locationVisualization) {
      this.locationVisualization.setPositionXY(value, this.locationVisualization.placeConfig.position?.y || 0);
    }
  }

  get locationPositionY(): number {
    return this.locationVisualization?.locationMarker?.position?.z || this.placeConfig?.position?.y || 0;
  }

  set locationPositionY(value: number) {
    if (this.locationVisualization) {
      this.locationVisualization.setPositionXY(this.locationVisualization.placeConfig.position?.x || 0, value);
    }
  }

  get locationRadius(): number | undefined {
    return this.locationVisualization?.placeConfig?.radius || this.placeConfig?.radius || undefined;
  }

  set locationRadius(value: number | undefined) {
    if (this.locationVisualization) {
      this.locationVisualization.setRadius(value);
    }
  }

  static toVertex2Array(decimalPositions: DecimalPosition[]): Vector2[] {
    const vector2s: any[] = [];
    decimalPositions.forEach(decimalPosition =>
      vector2s.push(new Vector2(decimalPosition.x, decimalPosition.y)));
    return vector2s;
  }

  static toArray2Vertex(vector2s: Vector2[]): DecimalPosition[] {
    const vertices: DecimalPosition[] = [];
    vector2s.forEach(vector => vertices.push({x: vector.x, y: vector.y}))
    return vertices;
  }

  onDeleteClicked() {
    this.placeConfig = null;
    this.placeConfigChange.emit(undefined);
  }

  onCreateClicked() {
    this.placeConfig = {
      polygon2D: null,
      position: null,
      radius: null
    };
    this.placeConfigChange.emit(this.placeConfig);
  }
}

class LocationVisualization {
  private readonly mouseObservable: Nullable<Observer<PointerInfo>>;
  locationMarker?: Mesh;
  radiusMarker?: Mesh;
  static locationMarkerMaterial: SimpleMaterial;
  static radiusMarkerMaterial: SimpleMaterial;

  constructor(public placeConfig: PlaceConfig, private renderService: BabylonRenderServiceAccessImpl, private placeConfigChange: EventEmitter<PlaceConfig>) {
    if (!LocationVisualization.locationMarkerMaterial) {
      LocationVisualization.locationMarkerMaterial = new SimpleMaterial(`Location marker`, renderService.getScene());
      LocationVisualization.locationMarkerMaterial.diffuseColor.r = 1;
      LocationVisualization.locationMarkerMaterial.diffuseColor.g = 1;
      LocationVisualization.locationMarkerMaterial.diffuseColor.b = 0;
    }
    if (!LocationVisualization.radiusMarkerMaterial) {
      LocationVisualization.radiusMarkerMaterial = new SimpleMaterial(`Radius marker`, renderService.getScene());
      LocationVisualization.radiusMarkerMaterial.diffuseColor.r = 1;
      LocationVisualization.radiusMarkerMaterial.diffuseColor.g = 1;
      LocationVisualization.radiusMarkerMaterial.diffuseColor.b = 0;
      LocationVisualization.radiusMarkerMaterial.wireframe = true;
    }

    if (placeConfig.position) {
      this.setupMarkers();
      this.locationMarker!.position = new Vector3(placeConfig.position!.x, 0, placeConfig.position!.y);
      this.locationMarker!.position.y += 5;
      if (this.placeConfig.radius) {
        this.radiusMarker!.position = this.locationMarker!.position;
      }
    }

    this.mouseObservable = renderService.getScene().onPointerObservable.add((pointerInfo) => {
      if (pointerInfo.type === PointerEventTypes.POINTERDOWN) {
        let pickingInfo = renderService.setupMeshPickPoint();
        if (pickingInfo.hit) {
          if (!this.locationMarker) {
            this.setupMarkers();
          }
          this.locationMarker!.position = pickingInfo.pickedPoint!;
          this.locationMarker!.position.y += 5;
          if (placeConfig.position) {
            placeConfig.position.x = pickingInfo.pickedPoint!.x;
            placeConfig.position.y = pickingInfo.pickedPoint!.z;
            if (this.placeConfig.radius) {
              this.radiusMarker!.position = this.locationMarker!.position;
            }
          } else {
            placeConfig.position = {
              x: pickingInfo.pickedPoint!.x,
              y: pickingInfo.pickedPoint!.z
            }
          }
          placeConfigChange.emit(placeConfig)
        }
      }
    });
  }


  private setupMarkers() {
    this.locationMarker = MeshBuilder.CreateCylinder("Location marker", {
      diameterTop: 0.2,
      diameterBottom: 0.2,
      height: 10
    });
    this.locationMarker.material = LocationVisualization.locationMarkerMaterial;
    if (this.placeConfig.radius) {
      this.setupRadiusMarkers();
    }
  }

  private setupRadiusMarkers() {
    this.radiusMarker = MeshBuilder.CreateCylinder("Location marker radius", {
      diameterTop: 2 * this.placeConfig.radius!,
      diameterBottom: 2 * this.placeConfig.radius!,
      height: 10
    });
    this.radiusMarker.material = LocationVisualization.radiusMarkerMaterial;
  }

  setPositionXY(x: number, y: number) {
    if (!this.placeConfig.position) {
      this.placeConfig.position = {x: 0, y: 0};
    }
    this.placeConfig.position.x = x;
    this.placeConfig.position.y = y;
    if (!this.locationMarker) {
      this.setupMarkers();
    }
    this.locationMarker!.position.x = this.placeConfig.position.x;
    this.locationMarker!.position.z = this.placeConfig.position.y;
    if (this.placeConfig.radius) {
      this.radiusMarker!.position = this.locationMarker!.position;
    }
  }

  setRadius(value: number | undefined) {
    if (value) {
      this.placeConfig.radius = value;
      if (this.radiusMarker) {
        this.radiusMarker.dispose()
      }
      this.setupRadiusMarkers();
      this.radiusMarker!.position = this.locationMarker!.position;
    } else {
      this.placeConfig.radius = null;
      if (this.radiusMarker) {
        this.radiusMarker.dispose();
        this.radiusMarker = undefined;
      }
    }
    this.placeConfigChange.emit(this.placeConfig);
  }

  dispose() {
    this.renderService.getScene().onPointerObservable.remove(this.mouseObservable);
    if (this.locationMarker) {
      this.locationMarker.dispose();
      this.locationMarker = undefined;
    }
    if (this.radiusMarker) {
      this.radiusMarker.dispose();
      this.radiusMarker = undefined;
    }
  }
}

class PolygonVisualization {
  static polygonMarkerMaterial: SimpleMaterial;
  polygonMarker?: Mesh;
  vertexAddMode: boolean = false;
  vertexDeleteMode: boolean = false;
  private readonly mouseObservable: Nullable<Observer<PointerInfo>>;
  private draggableCorners: DraggableCorner[] = [];
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;

  constructor(public placeConfig: PlaceConfig, private renderService: BabylonRenderServiceAccessImpl, private placeConfigChange: EventEmitter<PlaceConfig>) {
    if (!PolygonVisualization.polygonMarkerMaterial) {
      PolygonVisualization.polygonMarkerMaterial = new SimpleMaterial(`Location marker`, renderService.getScene());
      PolygonVisualization.polygonMarkerMaterial.diffuseColor.r = 1;
      PolygonVisualization.polygonMarkerMaterial.diffuseColor.g = 1;
      PolygonVisualization.polygonMarkerMaterial.diffuseColor.b = 0;
      PolygonVisualization.polygonMarkerMaterial.alpha = 0.66;
      PolygonVisualization.polygonMarkerMaterial.depthFunction = Constants.ALWAYS;
    }
    if (placeConfig.polygon2D && placeConfig.polygon2D.corners && placeConfig.polygon2D.corners.length > 2) {
      this.setupPolygonMesh();
      this.rebuildDraggableCorners();
    }
    this.mouseObservable = renderService.getScene().onPointerObservable.add((pointerInfo) => {
      if (pointerInfo.type === PointerEventTypes.POINTERDOWN) {
        let pickingInfo = renderService.setupMeshPickPoint();
        if (pickingInfo.hit) {
          if (this.placeConfig!.polygon2D! && this.placeConfig!.polygon2D!.corners && this.placeConfig!.polygon2D!.corners.length > 2) {
            if (this.vertexAddMode) {
              let polygon = PlaceConfigComponent.toVertex2Array(this.placeConfig!.polygon2D!.corners);
              PolygonVisualization.addPointToPolygon(new Vector2(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z), polygon);
              placeConfig.polygon2D!.corners = PlaceConfigComponent.toArray2Vertex(polygon);
              if (this.polygonMarker) {
                this.polygonMarker.dispose();
              }
            }
          } else {
            placeConfig.polygon2D = {corners: [], lines: []};
            placeConfig.polygon2D!.corners = [
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
          placeConfigChange.emit(placeConfig)
        }
      }
    });
    this.pointerObservable = UtilityLayerRenderer.DefaultUtilityLayer.utilityLayerScene.onPointerObservable.add(pointerInfo => {
      if (pointerInfo.type === PointerEventTypes.POINTERDOWN) {
        if (this.vertexDeleteMode) {
          let pickingInfo = UtilityLayerRenderer.DefaultUtilityLayer.utilityLayerScene.pick(UtilityLayerRenderer.DefaultUtilityLayer.utilityLayerScene.pointerX, UtilityLayerRenderer.DefaultUtilityLayer.utilityLayerScene.pointerY);
          let draggableCorner = this.recursivelyFindDraggableCorner(pickingInfo.pickedMesh);
          if (draggableCorner) {
            this.placeConfig.polygon2D?.corners.splice(draggableCorner.index, 1);
            this.setupPolygonMesh();
            this.rebuildDraggableCorners();
            placeConfigChange.emit(placeConfig)
          }

        }
      }
    });
  }

  dispose() {
    this.renderService.getScene().onPointerObservable.remove(this.mouseObservable);
    if (this.polygonMarker) {
      this.polygonMarker.dispose();
      this.polygonMarker = undefined;
    }
    UtilityLayerRenderer.DefaultUtilityLayer.utilityLayerScene.onPointerObservable.remove(this.pointerObservable)
    this.pointerObservable = null;
    this.draggableCorners.forEach(draggableCorner => draggableCorner.dispose());
    this.draggableCorners = [];
  }

  private setupPolygonMesh() {
    if (this.polygonMarker) {
      this.polygonMarker.dispose();
    }
    let polygon = PlaceConfigComponent.toVertex2Array(this.placeConfig!.polygon2D!.corners);
    const polygonMeshBuilder = new PolygonMeshBuilder(`Polygon marker`, polygon, this.renderService.getScene(), SlopeEditorComponent.EAR_CUT);
    this.polygonMarker = polygonMeshBuilder.build();
    this.polygonMarker.material = PolygonVisualization.polygonMarkerMaterial;
  }

  public static addPointToPolygon(point: Vector2, polygon: Vector2[]) {
    let index = SlopeEditorComponent.projectPointToPolygon(point, polygon);
    if (!index && index !== 0) {
      throw new Error("Invalid Polygon");
    }
    polygon.splice(index, 0, point);
  }

  private rebuildDraggableCorners() {
    this.draggableCorners.forEach(draggableCorner => draggableCorner.dispose());
    this.draggableCorners = [];
    if ((this.placeConfig.polygon2D?.corners?.length || 0) < 3) {
      return;
    }
    for (let i = 0; i < this.placeConfig.polygon2D!.corners.length; i++) {
      this.draggableCorners.push(new DraggableCorner(this.renderService, this.placeConfig.polygon2D!.corners!, i, () => {
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
