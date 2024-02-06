import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { DecimalPosition, PlaceConfig } from "../../../generated/razarion-share";
import { DecimalPosition as DecimalPositionGwt } from "../../../gwtangular/GwtAngularFacade";
import { BabylonRenderServiceAccessImpl } from "../../../game/renderer/babylon-render-service-access-impl.service";
import { Vector2 } from "@babylonjs/core";
import { PolygonVisualization } from "./polygon-visualization";
import { LocationVisualization } from "./location-visualization";

enum Type {
  LOCATION,
  POLYGON
}

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
  placeConfigChange = new EventEmitter<PlaceConfig | null>();
  locationVisualization?: LocationVisualization;
  polygonVisualization?: PolygonVisualization;
  visible: boolean = false;
  editable: boolean = false;
  types: { name: string, type: Type, icon: string }[] = [];
  selectedType?: { name: string, type: Type, icon: string };

  constructor(private renderService: BabylonRenderServiceAccessImpl) {
  }

  ngOnInit(): void {
    this.types = [
      { name: 'Location', type: Type.LOCATION, icon: "pi-map-marker" },
      { name: 'Region', type: Type.POLYGON, icon: "pi-map" },
    ];

    if (this.placeConfig) {
      if (this.placeConfig.position) {
        this.selectedType = this.types[0];
      } else if (this.placeConfig.polygon2D) {
        this.selectedType = this.types[1];
      }
    }

    this.display();
  }

  onVisibilityChange() {
    if (this.locationVisualization) {
      this.locationVisualization.onVisibilityChange();
    }
    if (this.polygonVisualization) {
      this.polygonVisualization.onVisibilityChange();
    }

    if (!this.visible) {
      this.editable = false;
      if (this.locationVisualization) {
        this.locationVisualization.editMode(false);
      }
      if (this.polygonVisualization) {
        this.polygonVisualization.editMode(false);
      }
    }

  }

  selectedTypeChanged() {
    this.placeConfig = null;
    if (this.selectedType) {
      this.placeConfig = {
        polygon2D: null,
        position: null,
        radius: null
      };
    } else {
      this.visible = false;
    }
    this.placeConfigChange.emit(this.placeConfig);
    this.display();
    this.editable = false;
    if (this.locationVisualization) {
      this.locationVisualization.editMode(false);
    }
    if (this.polygonVisualization) {
      this.polygonVisualization.editMode(false);
    }
  }

  onEditButton(active: boolean) {
    if (this.locationVisualization) {
      this.locationVisualization.editMode(active);
    }
    if (this.polygonVisualization) {
      this.polygonVisualization.editMode(active);
    }
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

  onScrollTo() {
    if (this.placeConfig) {
      if (this.placeConfig.position) {
        this.renderService.setViewFieldCenter(this.placeConfig.position.x, this.placeConfig.position.y);
      } else if (this.placeConfig.polygon2D) {
        if (this.placeConfig.polygon2D.corners?.length) {
          this.renderService.setViewFieldCenter(this.placeConfig.polygon2D.corners[0].x, this.placeConfig.polygon2D.corners[0].y);
        }
      }
    }
  }

  get locationPositionX(): number | undefined {
    if (this.placeConfig?.position) {
      return this.placeConfig.position.x;
    } else {
      return undefined;
    }
  }

  set locationPositionX(value: number | undefined) {
    if (!this.placeConfig) {
      return;
    }
    if (value) {
      if (this.placeConfig.position) {
        this.placeConfig!.position!.x = value;
      } else {
        this.placeConfig.position = { x: value, y: NaN }
      }
      if (this.visible) {
        this.locationVisualization!.setPositionXY()
      }
    } else {
      if (this.placeConfig.position) {
        this.placeConfig.position.x = NaN;
        if (isNaN(this.placeConfig.position.y)) {
          this.placeConfig.position = null;
          if (this.visible && this.locationVisualization) {
            this.locationVisualization.dispose();
            this.locationVisualization = undefined;
          }
        }
      }
    }
  }

  get locationPositionY(): number | undefined {
    if (this.placeConfig?.position) {
      return this.placeConfig.position.y;
    } else {
      return undefined;
    }
  }

  set locationPositionY(value: number | undefined) {
    if (!this.placeConfig) {
      return;
    }
    if (value) {
      if (this.placeConfig.position) {
        this.placeConfig!.position!.y = value;
      } else {
        this.placeConfig.position = { x: NaN, y: value }
      }
      if (this.visible) {
        this.locationVisualization!.setPositionXY()
      }
    } else {
      if (this.placeConfig.position) {
        this.placeConfig.position.y = NaN;
        if (isNaN(this.placeConfig.position.x)) {
          this.placeConfig.position = null;
          if (this.visible && this.locationVisualization) {
            this.locationVisualization.dispose();
            this.locationVisualization = undefined;
          }
        }
      }
    }
  }

  get locationRadius(): number | undefined {
    return this.placeConfig?.radius || undefined;
  }

  set locationRadius(value: number | undefined) {
    if (this.locationVisualization) {
      this.locationVisualization.setRadius(value);
    }
  }

  static toVertex2Array(decimalPositions: DecimalPosition[]): Vector2[] {
    const vector2s: Vector2[] = [];
    decimalPositions.forEach(decimalPosition =>
      vector2s.push(new Vector2(decimalPosition.x, decimalPosition.y)));
    return vector2s;
  }

  static toVertex2ArrayAngular(decimalPositions: DecimalPositionGwt[]): Vector2[] {
    const vector2s: Vector2[] = [];
    decimalPositions.forEach(decimalPosition =>
      vector2s.push(new Vector2(decimalPosition.getX(), decimalPosition.getY())));
    return vector2s;
  }

  static toArray2Vertex(vector2s: Vector2[]): DecimalPosition[] {
    const vertices: DecimalPosition[] = [];
    vector2s.forEach(vector => vertices.push({ x: vector.x, y: vector.y }))
    return vertices;
  }

  private display() {
    if (this.placeConfig) {
      if (this.placeConfig.position) {
        this.locationVisualization = new LocationVisualization(this, this.renderService, this.placeConfigChange);
        if (this.polygonVisualization) {
          this.polygonVisualization.dispose();
          this.polygonVisualization = undefined;
        }
      } else if (this.placeConfig.polygon2D) {
        this.polygonVisualization = new PolygonVisualization(this, this.renderService, this.placeConfigChange);
        if (this.locationVisualization) {
          this.locationVisualization.dispose();
          this.locationVisualization = undefined;
        }
      } else if (this.selectedType) {
        if (this.selectedType.type === Type.LOCATION) {
          this.locationVisualization = new LocationVisualization(this, this.renderService, this.placeConfigChange);
          if (this.polygonVisualization) {
            this.polygonVisualization.dispose();
            this.polygonVisualization = undefined;
          }
        } else if (this.selectedType.type === Type.POLYGON) {
          this.polygonVisualization = new PolygonVisualization(this, this.renderService, this.placeConfigChange);
          if (this.locationVisualization) {
            this.locationVisualization.dispose();
            this.locationVisualization = undefined;
          }
        }
      } else {
        if (this.polygonVisualization) {
          this.polygonVisualization.dispose();
          this.polygonVisualization = undefined;
        }
        if (this.locationVisualization) {
          this.locationVisualization.dispose();
          this.locationVisualization = undefined;
        }
      }
    } else {
      if (this.polygonVisualization) {
        this.polygonVisualization.dispose();
        this.polygonVisualization = undefined;
      }
      if (this.locationVisualization) {
        this.locationVisualization.dispose();
        this.locationVisualization = undefined;
      }
    }
  }

  protected readonly Type = Type;

}
