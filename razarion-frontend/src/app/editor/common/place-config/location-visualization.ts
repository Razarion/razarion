import {
  AbstractMesh,
  Mesh,
  MeshBuilder,
  Nullable,
  Observer,
  PointerEventTypes,
  PointerInfo,
  Ray,
  Vector3
} from "@babylonjs/core";
import {SimpleMaterial} from "@babylonjs/materials";
import {
  BabylonRenderServiceAccessImpl,
  RazarionMetadataType
} from "../../../game/renderer/babylon-render-service-access-impl.service";
import {EventEmitter} from "@angular/core";
import {PlaceConfig} from "../../../generated/razarion-share";
import {PlaceConfigComponent} from "./place-config.component";

export class LocationVisualization {
  private mouseObservable: Nullable<Observer<PointerInfo>> = null;
  locationMarker?: Mesh;
  radiusMarker?: Mesh;
  static locationMarkerMaterial: SimpleMaterial;
  static radiusMarkerMaterial: SimpleMaterial;

  constructor(private placeConfigComponent: PlaceConfigComponent, private renderService: BabylonRenderServiceAccessImpl, private placeConfigChange: EventEmitter<PlaceConfig | null>) {
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

    this.setupMeshes();
  }

  onVisibilityChange() {
    this.setupMeshes();
  }

  editMode(active: boolean) {
    if (active) {
      this.mouseObservable = this.renderService.getScene().onPointerObservable.add((pointerInfo) => {
        if (pointerInfo.type === PointerEventTypes.POINTERDOWN) {
          let pickingInfo = this.renderService.setupMeshPickPoint();
          if (pickingInfo.hit) {
            if (!this.placeConfigComponent.placeConfig?.position) {
              this.placeConfigComponent.placeConfig!.position = {
                x: pickingInfo.pickedPoint!.x,
                y: pickingInfo.pickedPoint!.z
              }
            }

            if (!this.locationMarker) {
              this.setupMeshes();
            }
            this.locationMarker!.position = pickingInfo.pickedPoint!;
            this.locationMarker!.position.y = LocationVisualization.getHeightFromTerrain(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z, this.renderService);
            if (this.placeConfigComponent.placeConfig?.position) {
              this.placeConfigComponent.placeConfig.position.x = pickingInfo.pickedPoint!.x;
              this.placeConfigComponent.placeConfig.position.y = pickingInfo.pickedPoint!.z;
              if (this.placeConfigComponent.placeConfig.radius) {
                this.radiusMarker!.position = this.locationMarker!.position;
              }
            } else {
              this.placeConfigComponent.placeConfig!.position = {
                x: pickingInfo.pickedPoint!.x,
                y: pickingInfo.pickedPoint!.z
              }
            }
            this.placeConfigChange.emit(this.placeConfigComponent!.placeConfig)
          }
        }
      });
    } else {
      if (this.mouseObservable) {
        this.renderService.getScene().onPointerObservable.remove(this.mouseObservable);
        this.mouseObservable = null;
      }
    }
  }

  private setupMeshes() {
    if (this.placeConfigComponent.visible && this.placeConfigComponent.placeConfig?.position && !this.locationMarker) {
      this.setupMarkers();
      this.locationMarker!.position = new Vector3(this.placeConfigComponent.placeConfig.position!.x, 0, this.placeConfigComponent.placeConfig.position!.y);
      this.locationMarker!.position.y = LocationVisualization.getHeightFromTerrain(this.placeConfigComponent.placeConfig.position!.x, this.placeConfigComponent.placeConfig.position!.y, this.renderService);
      if (this.placeConfigComponent.placeConfig.radius) {
        this.radiusMarker!.position = this.locationMarker!.position;
      }
    } else {
      this.dispose();
    }

  }

  public static getHeightFromTerrain(x: number, y: number, renderService: BabylonRenderServiceAccessImpl): number {
    let position = LocationVisualization.getTerrainPositionFromRay(
      new Ray(
        new Vector3(x, 100, y),
        new Vector3(0, -1, 0),
        1000),
      renderService);
    if (position) {
      return position.y;
    } else {
      return 0;
    }
  }

  public static getTerrainPositionFromRay(ray: Ray, renderService: BabylonRenderServiceAccessImpl): Vector3 | undefined {
    let pickingInfo = renderService.getScene().pickWithRay(ray,
      (mesh: AbstractMesh) => {
        let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(mesh);
        if (!razarionMetadata) {
          return false;
        }
        return razarionMetadata.type == RazarionMetadataType.GROUND || razarionMetadata.type == RazarionMetadataType.BOT_GROUND;
      });
    if (pickingInfo && pickingInfo.hit) {
      return pickingInfo.pickedPoint!;
    } else {
      return undefined;
    }
  }

  private setupMarkers() {
    this.locationMarker = MeshBuilder.CreateCylinder("Location marker", {
      diameterTop: 0.2,
      diameterBottom: 0.2,
      height: 10
    });
    this.locationMarker.material = LocationVisualization.locationMarkerMaterial;
    if (this.placeConfigComponent.placeConfig?.radius) {
      this.setupRadiusMarkers();
    }
  }

  private setupRadiusMarkers() {
    this.radiusMarker = MeshBuilder.CreateCylinder("Location marker radius", {
      diameterTop: 2 * this.placeConfigComponent.placeConfig!.radius!,
      diameterBottom: 2 * this.placeConfigComponent.placeConfig!.radius!,
      height: 10
    });
    this.radiusMarker.material = LocationVisualization.radiusMarkerMaterial;
  }

  setPositionXY() {
    if (!this.locationMarker) {
      this.setupMarkers();
    }
    this.locationMarker!.position.x = this.placeConfigComponent.placeConfig!.position!.x;
    this.locationMarker!.position.z = this.placeConfigComponent.placeConfig!.position!.y;
    if (this.placeConfigComponent.placeConfig?.radius) {
      this.radiusMarker!.position = this.locationMarker!.position;
    }
  }

  setRadius(value: number | string | null) {
    if (value) {
      this.placeConfigComponent.placeConfig!.radius = <number>value;
      if (this.radiusMarker) {
        this.radiusMarker.dispose()
      }
      if (this.placeConfigComponent.visible) {
        this.setupRadiusMarkers();
        this.radiusMarker!.position = this.locationMarker!.position;
      }
    } else {
      this.placeConfigComponent.placeConfig!.radius = null;
      if (this.radiusMarker) {
        this.radiusMarker.dispose();
        this.radiusMarker = undefined;
      }
    }
    this.placeConfigChange.emit(this.placeConfigComponent.placeConfig);
  }

  dispose() {
    if (this.mouseObservable) {
      this.renderService.getScene().onPointerObservable.remove(this.mouseObservable);
      this.mouseObservable = null;
    }
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
