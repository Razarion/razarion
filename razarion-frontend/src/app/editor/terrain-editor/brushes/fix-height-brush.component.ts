import { Component, OnInit } from '@angular/core';
import { AbstractBrush } from './abstract-brush';
import { Vector2, Vector3 } from '@babylonjs/core';
import {BrushConfigEntity, BrushConfigControllerClient} from 'src/app/generated/razarion-share';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { HttpClient } from '@angular/common/http';
import { MessageService } from 'primeng/api';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {Button} from 'primeng/button';
import {DropdownModule} from 'primeng/dropdown';
import {InputNumber} from 'primeng/inputnumber';

class Brush {
  constructor(public readonly id: number, public internalName: string, public brushValues: BrushValues) {
  }
}

class BrushValues {
  height: number = 1;
  diameter: number = 10;
  maxSlopeWidth: number = 10;
  slope: number = 30;
  random: number = 0;
}

@Component({
  selector: 'fix-height-brush',
  imports: [
    Slider,
    FormsModule,
    Button,
    DropdownModule,
    InputNumber
  ],
  template: `

    <div class="field grid grid-cols-12 gap-4 items-center">
      <div class="col-span-9">
        <p-dropdown [options]="brushes" [(ngModel)]="activeBrush"
                    optionLabel="name"
                    [style]="{ width: '100%' }"></p-dropdown>
      </div>

      <div class="col-span-1">
        <p-button type="button" icon="pi pi-plus"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-success"
                  (onClick)="onCreateBrush()">
        </p-button>
      </div>
      <div class="col-span-1">
        <p-button type="button" icon="pi pi-save"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-danger"
                  (onClick)="onSaveBrush()">
        </p-button>
      </div>
      <div class="col-span-1">
        <p-button type="button" icon="pi pi-trash"
                  styleClass="p-button-rounded p-button-text p-button-sm p-button-danger"
                  (onClick)="onDeleteBrush()">
        </p-button>
      </div>
    </div>

    <div class="field grid grid-cols-12 gap-4 items-center">
      <span class="col">Id</span>
      <div class="col">
        <p-inputNumber [disabled]="true"
                       [ngModel]="activeBrush.value.id"></p-inputNumber>
      </div>
    </div>

    <div class="field grid grid-cols-12 gap-4 items-center">
      <span class="col">Name <span [style]="{'color':'red','font-weight':'bolder'}">(Bug: jump on save)</span></span>
      <div class="col">
        <input [(ngModel)]="activeBrush.value.internalName"
               type="text"
               class="text-base text-color text-primary bg-primary-contrast p-2 border border-solid border-surface rounded-border appearance-none outline-0 focus:border-primary w-full">
      </div>
    </div>

    <div class="field grid grid-cols-12 gap-4 items-center">
      <span class="col">Size [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.brushValues.diameter" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.diameter" [step]="0.01" [min]="1" [max]="100"></p-slider>
      </div>
    </div>

    <div class="field grid grid-cols-12 gap-4 items-center">
      <span class="col">Height [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.brushValues.height" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.height" [step]="0.1" [min]="-20" [max]="50"></p-slider>
      </div>
    </div>

    <div class="field grid grid-cols-12 gap-4 items-center">
      <span class="col">Max slope width [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.brushValues.maxSlopeWidth" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.maxSlopeWidth" [step]="0.01" [min]="0"
                  [max]="100"></p-slider>
      </div>
    </div>

    <div class="field grid grid-cols-12 gap-4 items-center">
      <span class="col">Slope [&deg;]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.brushValues.slope" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.slope" [step]="0.01" [min]="0" [max]="90"></p-slider>
      </div>
    </div>

    <div class="field grid grid-cols-12 gap-4 items-center">
      <span class="col">Random (Slope) [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.brushValues.random" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.random" [step]="0.01" [min]="0" [max]="5"></p-slider>
      </div>
    </div>
  `
})
export class FixHeightBrushComponent extends AbstractBrush implements OnInit {
  brushes: { name: string, value: Brush }[] = [{ name: "Dummy", value: new Brush(-9999, "Dummy", new BrushValues()) }];
  activeBrush = this.brushes[0];
  private pendingBrushConfigEntityId: Number | null = null;

  private brushConfigControllerClient: BrushConfigControllerClient;

  constructor(httpClient: HttpClient, private messageService: MessageService) {
    super();
    this.brushConfigControllerClient = new BrushConfigControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.loadBrushes();
  }

  private loadBrushes(): void {
    this.brushConfigControllerClient
      .readAll()
      .then(brushConfigEntity => {
        this.brushes = [];
        brushConfigEntity.forEach(brushConfig => {
          let brushValues = JSON.parse(brushConfig.brushJson);
          if (!brushValues) {
            brushValues = new BrushValues();
          }
          let brush = {
            name: `${brushConfig.internalName} (${brushConfig.id})`,
            value: new Brush(brushConfig.id, brushConfig.internalName, brushValues),
          }
          this.brushes.push(brush);
          if (this.pendingBrushConfigEntityId === brushConfig.id) {
            this.pendingBrushConfigEntityId = null;
            this.activeBrush = brush;
          }
        });
        if (!this.activeBrush && this.brushes.length > 0) {
          this.activeBrush = this.brushes[0];
        }
      }).catch(err => {
        this.messageService.add({
          severity: 'error',
          summary: `Failed loading brushes`,
          detail: err.message,
          sticky: true
        });
      });
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    const radius = this.activeBrush.value.brushValues.diameter / 2.0;
    let distance = Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z));
    if (distance < (radius + this.activeBrush.value.brushValues.maxSlopeWidth)) {
      let newHeight: number | null = null;
      if (distance <= radius) {
        newHeight = this.activeBrush.value.brushValues.height;
      } else {
        let slopeDistance = distance - radius;
        let deltaHeight = Math.tan(this.activeBrush.value.brushValues.slope * Math.PI / 180) * slopeDistance;
        let direction = this.activeBrush.value.brushValues.height - oldPosition.y;
        let random = (this.activeBrush.value.brushValues.random * (Math.random() - 0.5) * 2.0)
        if (direction > 0) {
          // up
          let calculatedHeight = this.activeBrush.value.brushValues.height + random - deltaHeight;
          if (calculatedHeight > oldPosition.y) {
            newHeight = calculatedHeight;
          }
        } else if (direction < 0) {
          // down
          let calculatedHeight = this.activeBrush.value.brushValues.height + random + deltaHeight;
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
    this.brushConfigControllerClient
      .create()
      .then(brushConfig => {
        this.pendingBrushConfigEntityId = brushConfig.id
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
    let brushConfigEntity: BrushConfigEntity = {
      brushJson: JSON.stringify(this.activeBrush.value.brushValues),
      id: this.activeBrush.value.id,
      internalName: this.activeBrush.value.internalName
    }
    this.brushConfigControllerClient
      .update(brushConfigEntity)
      .then(() => {
        this.pendingBrushConfigEntityId = this.activeBrush.value.id;
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

  onDeleteBrush() {
    this.brushConfigControllerClient
      .delete(this.activeBrush.value.id)
      .then(() => {
        this.activeBrush = this.brushes.filter(brush => brush.value.id !== this.activeBrush.value.id)[0];
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
