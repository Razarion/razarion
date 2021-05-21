import {Component, NgZone} from '@angular/core';
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
                           [maxFractionDigits]="6" (onInput)="onChangeX($event)">
            </p-inputNumber>
          </div>
          <div class="p-mr-2">
            <p-inputNumber [ngModel]="y" prefix="y: " [minFractionDigits]="1"
                           [size]=5
                           [maxFractionDigits]="6" (onInput)="onChangeY($event)">
            </p-inputNumber>
          </div>
          <div class="p-mr-2">
            <p-inputNumber [ngModel]="r" prefix="r: " [minFractionDigits]="1"
                           [size]=5
                           [maxFractionDigits]="6" (onInput)="onChangeR($event)">
            </p-inputNumber>
          </div>
          <div class="p-mr-2">
            <p-button icon="pi pi-map-marker"
                      title="Show"
                      styleClass="p-button-rounded p-button-text p-button-sm p-button-warning"
                      (onClick)="onShow()"></p-button>
          </div>
          <div class="p-mr-2">
            <p-button icon="pi pi-pencil"
                      title="Edit"
                      styleClass="p-button-rounded p-button-text p-button-sm p-button-warning"
                      (onClick)="onActivateCursor()"></p-button>
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
              {{angularTreeNodeData.value}}
            </div>
            <div class="p-mr-2">
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class PlaceConfigPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;
  x: any;
  y: any;
  r: any;
  selected: any;

  constructor(private gwtAngularService: GwtAngularService, private zone: NgZone) {
  }

  ngOnInit(): void {
    if (this.angularTreeNodeData.value != undefined) {
      if (this.angularTreeNodeData.value.x != undefined) {
        this.selected = "position";
      }
      this.x = this.angularTreeNodeData.value.x;
      this.y = this.angularTreeNodeData.value.y;
      this.r = this.angularTreeNodeData.value.r;
    }
  }

  onChangeX(event: any) {
    this.x = event.value;
    this.savePosition();
  }

  onChangeY(event: any) {
    this.y = event.value;
    this.savePosition();
  }

  onChangeR(event: any) {
    this.r = event.value;
    this.savePosition();
  }

  savePosition() {
    if (this.x == undefined || this.y == undefined) {
      this.angularTreeNodeData.setValue(null)
    } else {
      this.angularTreeNodeData.setValue({x: this.x, y: this.y, r: this.r})
    }
  }

  onShow() {
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainMarkerService().showPosition(this.x, this.y);
  }

  onActivateCursor() {
    const self = this;
    this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainMarkerService().activatePositionCursor(new class implements PositionCallback {
      position(x: number, y: number): void {
        self.zone.run(() => {
          self.x = x;
          self.y = y;
          self.savePosition();
        });
      }
    });
  }
}
