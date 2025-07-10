import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {Color3, Mesh, MeshBuilder, PointerEventTypes, StandardMaterial, Vector2, Vector3} from '@babylonjs/core';
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
      <span class="col-span-5">Size [m]</span>
      <div class="col-span-7">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.brushValues.diameter" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.diameter" [step]="0.01" [min]="1" [max]="100"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Height [m]</span>
      <div class="col-span-7">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.brushValues.height" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.height" [step]="0.1" [min]="-20" [max]="50"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Max slope width [m]</span>
      <div class="col-span-7">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.brushValues.maxSlopeWidth" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.maxSlopeWidth" [step]="0.01" [min]="0"
                  [max]="100"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Slope [&deg;]</span>
      <div class="col-span-7">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.brushValues.slope" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.slope" [step]="0.01" [min]="0" [max]="90"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Random (Slope) [m]</span>
      <div class="col-span-7">
        <input type="number" pInputText [(ngModel)]="activeBrush.value.brushValues.random" class="w-full"/>
        <p-slider [(ngModel)]="activeBrush.value.brushValues.random" [step]="0.01" [min]="0" [max]="5"></p-slider>
      </div>
    </div>
  `
})
export class FixHeightBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  brushes: { name: string, value: Brush }[] = [{name: "Dummy", value: new Brush(-9999, "Dummy", new BrushValues())}];
  activeBrush = this.brushes[0];
  private pendingBrushConfigEntityId: Number | null = null;
  private editorCursorMeshOuter: Mesh | null = null;
  private editorCursorMeshInner: Mesh | null = null;

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
    if (this.editorCursorMeshOuter) {
      this.editorCursorMeshOuter.dispose();
      this.editorCursorMeshOuter = null;
    }

    if (this.editorCursorMeshInner) {
      this.editorCursorMeshInner.dispose();
      this.editorCursorMeshInner = null;
    }
  }

  override showCursor() {
    if (this.editorCursorMeshOuter) {
      this.editorCursorMeshOuter.visibility = 1;
    }
    if (this.editorCursorMeshInner) {
      this.editorCursorMeshInner.visibility = 1;
    }
  }

  override hideCursor() {
    if (this.editorCursorMeshOuter) {
      this.editorCursorMeshOuter.visibility = 0;
    }
    if (this.editorCursorMeshInner) {
      this.editorCursorMeshInner.visibility = 0;
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


  private initEditorCursor() {
    const scene = this.renderService.getScene();
    this.editorCursorMeshOuter = MeshBuilder.CreateSphere("editorCursor inner", {
      diameter: 1
    }, scene);
    this.editorCursorMeshOuter.isPickable = false;
    this.editorCursorMeshOuter.setEnabled(false);

    let material = new StandardMaterial("cursorMaterial inner", scene);
    material.alpha = 0.5;
    material.diffuseColor = new Color3(1, 1, 0);
    this.editorCursorMeshOuter.material = material;

    this.editorCursorMeshInner = MeshBuilder.CreateSphere("editorCursor outer", {
      diameter: 1
    }, scene);
    this.editorCursorMeshInner.isPickable = false;
    this.editorCursorMeshInner.setEnabled(false);

    material = new StandardMaterial("cursorMaterial outer", scene);
    material.alpha = 0.5;
    material.diffuseColor = new Color3(1, 0, 0);
    this.editorCursorMeshInner.material = material;

    scene.onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERMOVE: {
          let pickingInfo = this.renderService.setupTerrainPickPoint();
          if (pickingInfo.hit) {
            const diameterOuter = this.activeBrush.value.brushValues.diameter + 2 * this.activeBrush.value.brushValues.maxSlopeWidth;
            if (this.editorCursorMeshOuter) {
              this.editorCursorMeshOuter.position.copyFrom(pickingInfo.pickedPoint!);
              this.editorCursorMeshOuter.setEnabled(true);
              this.editorCursorMeshOuter.scaling.set(diameterOuter, diameterOuter, diameterOuter);
            }
            if (this.editorCursorMeshInner) {
              this.editorCursorMeshInner.position.copyFrom(pickingInfo.pickedPoint!);
              this.editorCursorMeshInner.setEnabled(true);
              this.editorCursorMeshInner.scaling.set(
                this.activeBrush.value.brushValues.diameter,
                this.activeBrush.value.brushValues.diameter,
                this.activeBrush.value.brushValues.diameter);
            }
          }
          break;
        }
      }
    })
  }

}
