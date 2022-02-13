import {Component, EventEmitter, Input, NgZone, Output} from '@angular/core';
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {PolygonCallback} from "../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'polygon-2d-editor',
  template: `
    <div class="inline-flex">
      <div class="mr-2">
        <p-button icon="pi pi-map-marker"
                  title="Show"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-warning"
                  (onClick)="onShow()"
                  [disabled]="disabled || polygon === undefined || polygon === null"></p-button>
      </div>
      <div class="mr-2">
        <p-button icon="pi pi-pencil"
                  title="Edit"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-warning"
                  (onClick)="onActivateCursor()"
                  [disabled]="disabled"></p-button>
      </div>
      <div class="mr-2">
        <p-button icon="pi pi-times"
                  title="Delete"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-warning"
                  (onClick)="onDelete()"
                  [disabled]="disabled || polygon === undefined || polygon === null"></p-button>
      </div>
    </div>
  `
})
export class Polygon2dEditorComponent {
  @Input()
  polygon: any;
  @Input()
  disabled: boolean = false;
  @Output()
  change: EventEmitter<any> = new EventEmitter();

  constructor(private gwtAngularService: GwtAngularService, private zone: NgZone) {
  }

  onShow() {
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainMarkerService().showPolygon(this.polygon);
  }

  onActivateCursor() {
    this.activateCursor();
  }

  onDelete() {
    this.polygon = null;
    this.change.emit(null);
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainMarkerService().showPolygon(null);
    this.activateCursor();
  }

  private activateCursor() {
    const self = this;
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainMarkerService().activatePolygonCursor(this.polygon, new class implements PolygonCallback {
      polygon(polygon: any) {
        self.zone.run(() => {
          self.polygon = polygon;
          self.change.emit(polygon);
        });
      }
    });
  }
}
