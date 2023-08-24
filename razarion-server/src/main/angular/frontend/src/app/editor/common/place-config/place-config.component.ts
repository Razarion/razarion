import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PlaceConfig} from "../../../generated/razarion-share";
import {BabylonRenderServiceAccessImpl} from "../../../game/renderer/babylon-render-service-access-impl.service";
import {Mesh, MeshBuilder, Nullable, Observer, PointerEventTypes, PointerInfo, Vector3} from "@babylonjs/core";
import {SimpleMaterial} from "@babylonjs/materials";

@Component({
  selector: 'place-config',
  templateUrl: './place-config.component.html',
  styleUrls: ['./place-config.component.scss']
})
export class PlaceConfigComponent implements OnInit {
  @Input("placeConfig")
  placeConfig?: PlaceConfig;
  @Output()
  placeConfigChange = new EventEmitter<PlaceConfig>();
  locationMode = false
  locationVisualization?: LocationVisualization;

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

  onEditLocationButton(active: boolean | undefined) {
    if (active && !this.locationVisualization) {
      this.createPlaceConfigIfNeeded();
      this.locationVisualization = new LocationVisualization(this.placeConfig!, this.renderService, this.placeConfigChange);
    } else if (!active && this.locationVisualization) {
      this.locationVisualization.dispose();
      this.locationVisualization = undefined
    }
  }

  onEditPolygonButton(active: boolean | undefined) {
    console.info(`onEditPolygonButton ${active}`)
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
    return this.locationVisualization?.placeConfig?.radius ||  this.placeConfig?.radius || undefined;
  }

  set locationRadius(value: number | undefined) {
    if (this.locationVisualization) {
      this.locationVisualization.setRadius(value);
    }
  }

  private createPlaceConfigIfNeeded() {
    if (!this.placeConfig) {
      this.placeConfig = {
        polygon2D: null,
        position: null,
        radius: null
      };
    }
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
