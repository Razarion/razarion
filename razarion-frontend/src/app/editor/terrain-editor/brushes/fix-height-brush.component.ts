import {Component, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {Vector2, Vector3} from '@babylonjs/core';
import {BrushConfig, BrushEditorControllerClient} from 'src/app/generated/razarion-share';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {HttpClient} from '@angular/common/http';
import {MessageService} from 'primeng/api';

class BrushValues {
  height: number = 1;
  diameter: number = 10;
  maxSlopeWidth: number = 10;
  slope: number = 30;
  random: number = 0;
  internalName: string = "Unknown-brushValues-internalName";
  id: number = -999999;
}

@Component({
  selector: 'fix-height-brush',
  template: `

    <div class="field grid align-items-center">
      <div class="col-9">
        <p-dropdown [options]="brushes" [(ngModel)]="activeBrush"
                    optionLabel="name"
                    [style]="{ width: '100%' }"></p-dropdown>
      </div>

      <div class="col-1">
        <p-button type="button" icon="pi pi-plus"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-success"
                  (onClick)="onCreateBrush()">
        </p-button>
      </div>
      <div class="col-1">
        <p-button type="button" icon="pi pi-save"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-danger"
                  (onClick)="onSaveBrush()">
        </p-button>
      </div>
      <div class="col-1">
        <p-button type="button" icon="pi pi-trash"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-danger"
                  (onClick)="onDeleteBrush()">
        </p-button>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Id</span>
      <div class="col">
        <p-inputNumber [disabled]="true"
                       [(ngModel)]="activeBrush.value.id"></p-inputNumber>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Name</span>
      <div class="col">
        <input [(ngModel)]="activeBrush.value.internalName"
               type="text"
               class="text-base text-color bg-primary-reverse p-2 border-1 border-solid surface-border border-round appearance-none outline-none focus:border-primary w-full">
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Height [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.height" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.height" [step]="0.1" [min]="-20" [max]="50"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Diameter [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.diameter" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.diameter" [step]="0.01" [min]="1" [max]="100"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Max slope width [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.maxSlopeWidth" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.maxSlopeWidth" [step]="0.01" [min]="0" [max]="100"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Slope [&deg;]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.slope" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.slope" [step]="0.01" [min]="0" [max]="90"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Random (Slope) [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.random" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.random" [step]="0.01" [min]="0" [max]="5"></p-slider>
      </div>
    </div>
  `
})
export class FixHeightBrushComponent extends AbstractBrush implements OnInit {
  brushes: { name: string, value: BrushValues }[] = [{name: "Unknown-brushValues-internalName (?????)", value: new BrushValues()}];
  activeBrush = this.brushes[0];

  private brushEditorControllerClient: BrushEditorControllerClient;

  constructor(httpClient: HttpClient, private messageService: MessageService) {
    super();
    this.brushEditorControllerClient = new BrushEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.loadBrushes();
  }

  private loadBrushes(): void {
    this.brushEditorControllerClient
      .readAll()
      .then(brushConfigs => {
        this.brushes = [];
        brushConfigs.forEach(brushConfig => {
          let brushValues = JSON.parse(brushConfig.brushJson);
          if(!brushValues) {
            brushValues = new BrushValues();
            brushValues.id = brushConfig.id;
            brushValues.internalName = brushConfig.internalName;
          }
          this.brushes.push({
            name: `${brushConfig.internalName} (${brushConfig.id})`,
            value: brushValues,
          });
        });
      }).catch(err => {
      this.messageService.add({
        severity: 'error',
        summary: `Failed loading brushes`,
        detail: err.message,
        sticky: true
      });
    });
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3, avgHeight: number | undefined): number | null {
    const radius = this.activeBrush.value.diameter / 2.0;
    let distance = Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z));
    if (distance < (radius + this.activeBrush.value.maxSlopeWidth)) {
      let newHeight: number | null = null;
      if (distance <= radius) {
        newHeight = this.activeBrush.value.height;
      } else {
        let slopeDistance = distance - radius;
        let deltaHeight = Math.tan(this.activeBrush.value.slope * Math.PI / 180) * slopeDistance;
        let direction = this.activeBrush.value.height - oldPosition.y;
        let random = (this.activeBrush.value.random * (Math.random() - 0.5) * 2.0)
        if (direction > 0) {
          // up
          let calculatedHeight = this.activeBrush.value.height + random - deltaHeight;
          if (calculatedHeight > oldPosition.y) {
            newHeight = calculatedHeight;
          }
        } else if (direction < 0) {
          // down
          let calculatedHeight = this.activeBrush.value.height + random + deltaHeight;
          if (calculatedHeight < oldPosition.y) {
            newHeight = calculatedHeight;
          }
        }
      }
      return newHeight
    } else {
      return null;
    }
  }

  onCreateBrush() {
    this.brushEditorControllerClient
      .create()
      .then(() => {
        this.loadBrushes();
      }).catch(err => {
      this.messageService.add({
        severity: 'error',
        summary: `Failed creating a brush`,
        detail: err.message,
        sticky: true
      });
    });

  }

  onSaveBrush() {
    let brushConfig: BrushConfig = {
      brushJson: JSON.stringify(this.activeBrush.value),
      id: this.activeBrush.value.id,
      internalName: this.activeBrush.value.internalName
    }
    this.brushEditorControllerClient
      .update(brushConfig)
      .then(() => {
      }).catch(err => {
      this.messageService.add({
        severity: 'error',
        summary: `Failed save brushes`,
        detail: err.message,
        sticky: true
      });
    });
  }

  onDeleteBrush() {
    this.brushEditorControllerClient
      .delete(this.activeBrush.value.id)
      .then(() => {
        this.loadBrushes();
      }).catch(err => {
      this.messageService.add({
        severity: 'error',
        summary: `Failed save brushes`,
        detail: err.message,
        sticky: true
      });
    });
  }
}
