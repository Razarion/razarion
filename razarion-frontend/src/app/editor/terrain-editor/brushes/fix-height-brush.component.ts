import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {PointerEventTypes, Vector2, Vector3} from '@babylonjs/core';
import {BrushConfigControllerClient, BrushConfigEntity} from 'src/app/generated/razarion-share';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {HttpClient} from '@angular/common/http';
import {MessageService} from 'primeng/api';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {Button} from 'primeng/button';
import {SelectModule} from 'primeng/select';
import {InputNumber} from 'primeng/inputnumber';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';
import {HeightMapCursor} from './height-map-cursor';

class Brush {
  constructor(public readonly id: number, public internalName: string, public brushValues: BrushValues) {
  }
}

export enum BrushType {
  ROUND = "ROUND",
  SQUARE = "SQUARE"
}

export class BrushValues {
  type: BrushType = BrushType.SQUARE;
  height: number = 1;
  size: number = 10;
  maxSlopeWidth: number = 10;
  random: number = 0;
}

@Component({
  selector: 'fix-height-brush',
  imports: [
    Slider,
    FormsModule,
    Button,
    SelectModule,
    InputNumber
  ],
  template: `
    <div class="grid grid-cols-12 gap-1 p-1">
      <div class="col-span-9">
        <p-select [options]="brushes" [(ngModel)]="activeBrush"
                  optionLabel="name"
                  [style]="{ width: '100%' }"></p-select>
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

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Id</span>
      <div class="col-span-7">
        <p-inputNumber [disabled]="true"
                       [ngModel]="activeBrush.value.id"></p-inputNumber>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Name <span
        [style]="{'color':'red','font-weight':'bolder'}">(Bug: jump on save)</span></span>
      <div class="col-span-7">
        <input [(ngModel)]="activeBrush.value.internalName"
               type="text"
               class="text-base text-color text-primary bg-primary-contrast p-2 border border-solid border-surface rounded-border appearance-none outline-0 focus:border-primary w-full">
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Type</span>
      <div class="col-span-7">
        <p-select
          [options]="[{label: 'Square', value: BrushType.SQUARE}, {label: 'Round', value: BrushType.ROUND}]"
          [(ngModel)]="activeBrush.value.brushValues.type"
          [style]="{ width: '100%' }">
        </p-select>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Size [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="activeBrush.value.brushValues.size" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.size" [step]="1" [min]="1" [max]="100"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Height [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="activeBrush.value.brushValues.height" [step]="0.1" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.height" [step]="0.1" [min]="-20" [max]="50"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Max slope width [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="activeBrush.value.brushValues.maxSlopeWidth" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.maxSlopeWidth" [step]="1" [min]="0"
                  [max]="100"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Random [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="activeBrush.value.brushValues.random" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.random" [step]="0.01" [min]="0" [max]="5"></p-slider>
      </div>
    </div>
  `
})
export class FixHeightBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  BrushType = BrushType;
  brushes: { name: string, value: Brush }[] = [{name: "Dummy", value: new Brush(-9999, "Dummy", new BrushValues())}];
  activeBrush = this.brushes[0];
  private pendingBrushConfigEntityId: Number | null = null;
  private heightMapCursor: HeightMapCursor | null = null;

  private brushConfigControllerClient: BrushConfigControllerClient;

  constructor(httpClient: HttpClient,
              private renderService: BabylonRenderServiceAccessImpl,
              private messageService: MessageService) {
    super();
    this.brushConfigControllerClient = new BrushConfigControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.loadBrushes();
    this.initEditorCursor();
  }

  ngOnDestroy(): void {
    if (this.heightMapCursor) {
      this.heightMapCursor.dispose();
      this.heightMapCursor = null;
    }
  }

  override showCursor() {
    if (this.heightMapCursor) {
      this.heightMapCursor.setVisibility(true);
    }
  }

  override hideCursor() {
    if (this.heightMapCursor) {
      this.heightMapCursor.setVisibility(false);
    }
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

  override calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    return FixHeightBrushComponent.staticCalculateHeight(mousePosition, oldPosition, oldPosition.y, this.activeBrush.value.brushValues);
  }

  public static staticCalculateHeight(centerPosition: Vector3, position: Vector3, terrainHeight: number | null, brushValues: BrushValues): number | null {
    switch (brushValues.type) {
      case BrushType.ROUND:
        return FixHeightBrushComponent.calculateHeightRound(centerPosition, position, terrainHeight, brushValues);
      case BrushType.SQUARE:
        return FixHeightBrushComponent.calculateHeightSquare(centerPosition, position, terrainHeight, brushValues);
    }
  }

  private static calculateHeightRound(centerPosition: Vector3, position: Vector3, terrainHeight: number | null, brushValues: BrushValues): number | null {
    const radius = brushValues.size / 2.0;
    let distance = Vector2.Distance(new Vector2(position.x, position.z), new Vector2(centerPosition.x, centerPosition.z));
    if (distance < (radius + brushValues.maxSlopeWidth)) {
      let newHeight: number | null = null;
      if (distance <= radius) {
        newHeight = brushValues.height;
      } else {
        let slopeDistance = distance - radius;
        let deltaHeight = brushValues.height * slopeDistance / brushValues.maxSlopeWidth;
        if (terrainHeight !== null) {
          let direction = brushValues.height - terrainHeight;
          let random = (brushValues.random * (Math.random() - 0.5) * 2.0)
          let calculatedHeight = brushValues.height + random - deltaHeight;
          if (direction > 0) {
            // up
            if (calculatedHeight > terrainHeight) {
              newHeight = calculatedHeight;
            }
          } else if (direction < 0) {
            // down
            if (calculatedHeight < terrainHeight) {
              newHeight = calculatedHeight;
            }
          }
        } else {
          let random = (brushValues.random * (Math.random() - 0.5) * 2.0)
          newHeight = brushValues.height + random - deltaHeight;
        }
      }
      return newHeight
    } else {
      return null;
    }
  }

  private static calculateHeightSquare(centerPosition: Vector3, position: Vector3, terrainHeight: number | null, brushValues: BrushValues): number | null {
    const distanceX = Math.abs(centerPosition.x - position.x);
    const distanceZ = Math.abs(centerPosition.z - position.z);

    if ((distanceX <= brushValues.size / 2.0)
      && (distanceZ <= brushValues.size / 2.0)) {
      return brushValues.height;
    } else if ((distanceX <= brushValues.size / 2.0 + brushValues.maxSlopeWidth)
      && (distanceZ <= brushValues.size / 2.0 + brushValues.maxSlopeWidth)) {
      let newHeight: number | null = null;

      let distance = Math.max(distanceX, distanceZ);
      let slopeDistance = distance - brushValues.size / 2.0;
      let deltaHeight = brushValues.height * slopeDistance / brushValues.maxSlopeWidth;
      if (terrainHeight !== null) {
        let direction = brushValues.height - position.y;
        let random = (brushValues.random * (Math.random() - 0.5) * 2.0)
        let calculatedHeight = brushValues.height + random - deltaHeight;
        if (direction > 0) {
          // up
          if (calculatedHeight > terrainHeight) {
            newHeight = calculatedHeight;
          }
        } else if (direction < 0) {
          // down
          if (calculatedHeight < terrainHeight) {
            newHeight = calculatedHeight;
          }
        }
      } else {
        let random = (brushValues.random * (Math.random() - 0.5) * 2.0)
        newHeight = brushValues.height + random - deltaHeight;
      }
      return newHeight;
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


  private initEditorCursor() {
    const scene = this.renderService.getScene();
    this.heightMapCursor = new HeightMapCursor(this.renderService.getScene(), this.activeBrush.value.brushValues);

    scene.onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERMOVE: {
          let pickingInfo = this.renderService.setupTerrainPickPoint();
          if (pickingInfo.hit) {
            if (this.heightMapCursor) {
              this.heightMapCursor.update(pickingInfo.pickedPoint!, this.activeBrush.value.brushValues);
            }
          }
          break;
        }
      }
    });
  }

}
