import {Component, NgZone, OnInit} from '@angular/core';
import {AngularTreeNodeData, PositionCallback} from "../../../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../../gwtangular/GwtAngularService";

@Component({
  selector: 'place-config-property-editor',
  template: `
    <div class="p-d-flex p-flex-column">
      <div class="p-mb-2">
        <div class="p-d-inline-flex">
          <div class="p-mr-2">
            <p-radioButton name="radio-button" value="position" [(ngModel)]="selected"></p-radioButton>
          </div>
          <div class="p-mr-2">
            <p-inputNumber [ngModel]="x" prefix="x: " [minFractionDigits]="1"
                           [size]=5
                           [maxFractionDigits]="6" (onInput)="onChangeX($event)"
                           [disabled]="selected !== 'position'">
            </p-inputNumber>
          </div>
          <div class="p-mr-2">
            <p-inputNumber [ngModel]="y" prefix="y: " [minFractionDigits]="1"
                           [size]=5
                           [maxFractionDigits]="6" (onInput)="onChangeY($event)"
                           [disabled]="selected !== 'position'">
            </p-inputNumber>
          </div>
          <div class="p-mr-2">
            <p-inputNumber [ngModel]="r" prefix="r: " [minFractionDigits]="1"
                           [size]=5
                           [maxFractionDigits]="6" (onInput)="onChangeR($event)"
                           [disabled]="selected !== 'position'">
            </p-inputNumber>
          </div>
          <div class="p-mr-2">
            <p-button icon="pi pi-map-marker"
                      title="Show"
                      styleClass="p-button-rounded p-button-text p-button-sm p-button-warning"
                      (onClick)="onShowPosition()"
                      [disabled]="selected !== 'position'"></p-button>
          </div>
          <div class="p-mr-2">
            <p-button icon="pi pi-pencil"
                      title="Edit"
                      styleClass="p-button-rounded p-button-text p-button-sm p-button-warning"
                      (onClick)="onActivatePositionCursor()"
                      [disabled]="selected !== 'position'"></p-button>
          </div>
        </div>
      </div>
      <div class="p-mb-2">
        <div class="p-mb-2">
          <div class="p-d-inline-flex">
            <div class="p-mr-2">
              <p-radioButton name="radio-button" value="polygon" [(ngModel)]="selected"></p-radioButton>
            </div>
            <div class="p-mr-2">
              <polygon-2d-editor [polygon]="polygon" (change)="onPolygon($event)"
                                 [disabled]="selected !== 'polygon'"></polygon-2d-editor>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class PlaceConfigPropertyEditorComponent implements OnInit {
  angularTreeNodeData!: AngularTreeNodeData;
  selected: any;
  x: any;
  y: any;
  r: any;
  polygon: any;

  constructor(private gwtAngularService: GwtAngularService, private zone: NgZone) {
  }

  ngOnInit(): void {
    if (this.angularTreeNodeData.value != undefined) {
      if (this.angularTreeNodeData.value.x != undefined) {
        this.selected = "position";
        this.x = this.angularTreeNodeData.value.x;
        this.y = this.angularTreeNodeData.value.y;
        this.r = this.angularTreeNodeData.value.r;
      } else if (this.angularTreeNodeData.value.p != undefined) {
        this.selected = "polygon";
        this.polygon = this.angularTreeNodeData.value.p;
      }
    }
  }

  onChangeX(event: any) {
    if(typeof event.value !== "number") {
      return;
    }
    this.x = event.value;
    this.save();
  }

  onChangeY(event: any) {
    if(typeof event.value !== "number") {
      return;
    }
    this.y = event.value;
    this.save();
  }

  onChangeR(event: any) {
    if(typeof event.value !== "number") {
      return;
    }
    this.r = event.value;
    this.save();
  }

  save() {
    if (this.selected === 'position' && this.x !== undefined && this.y !== undefined) {
      this.angularTreeNodeData.setValue({x: this.x, y: this.y, r: this.r})
    } else if (this.selected === 'polygon' && this.polygon !== undefined && this.polygon !== null) {
      this.angularTreeNodeData.setValue({p: this.polygon})
    } else {
      this.angularTreeNodeData.setValue(null)
    }
  }

  onShowPosition() {
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainMarkerService().showPosition(this.x, this.y);
  }

  onActivatePositionCursor() {
    const self = this;
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainMarkerService().activatePositionCursor(new class implements PositionCallback {
      position(x: number, y: number): void {
        self.zone.run(() => {
          self.x = x;
          self.y = y;
          self.save();
        });
      }
    });
  }

  onPolygon(polygon: any) {
    this.polygon = polygon;
    this.save();
  }
}
