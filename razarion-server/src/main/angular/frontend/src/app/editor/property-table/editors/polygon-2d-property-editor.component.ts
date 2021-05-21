import {Component, NgZone} from '@angular/core';
import {AngularTreeNodeData, PolygonCallback} from "../../../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../../gwtangular/GwtAngularService";

@Component({
  selector: 'polygon-2d-property-editor',
  template: `
    <div class="p-d-inline-flex">
      <div class="p-mr-2">
        <p-button icon="pi pi-map-marker"
                  title="Show"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-warning"
                  (onClick)="onShow()"
                  [disabled]="this.angularTreeNodeData.value === undefined || this.angularTreeNodeData.value === null"></p-button>
      </div>
      <div class="p-mr-2">
        <p-button icon="pi pi-pencil"
                  title="Edit"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-warning"
                  (onClick)="onActivateCursor()"></p-button>
      </div>
      <div class="p-mr-2">
        <p-button icon="pi pi-times"
                  title="Delete"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-warning"
                  (onClick)="onDelete()"
                  [disabled]="this.angularTreeNodeData.value === undefined || this.angularTreeNodeData.value === null"></p-button>
      </div>
    </div>
  `
})
export class Polygon2dPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;

  constructor(private gwtAngularService: GwtAngularService, private zone: NgZone) {
  }

  onShow() {
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainMarkerService().showPolygon(this.angularTreeNodeData.value);
  }

  onActivateCursor() {
    this.activateCursor(this.angularTreeNodeData);
  }

  onDelete() {
    this.angularTreeNodeData.setValue(null);
    this.angularTreeNodeData.value = null;
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainMarkerService().showPolygon(null);
    this.activateCursor(this.angularTreeNodeData);
  }

  private activateCursor(angularTreeNodeData: AngularTreeNodeData) {
    const self = this;
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainMarkerService().activatePolygonCursor(this.angularTreeNodeData.value, new class implements PolygonCallback {
      polygon(polygon: any) {
        self.zone.run(() => {
          angularTreeNodeData.setValue(polygon);
          angularTreeNodeData.value = polygon;
        });
      }
    });
  }
}
