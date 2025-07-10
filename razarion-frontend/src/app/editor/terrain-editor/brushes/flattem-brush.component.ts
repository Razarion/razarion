import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {Color3, Mesh, MeshBuilder, PointerEventTypes, StandardMaterial, Vector2, Vector3} from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';

@Component({
  selector: 'fix-height-brush',
  imports: [
    Slider,
    FormsModule
  ],
  template: `
    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Diameter</span>
      <div class="col-span-7">
        <input type="text" pInputText [(ngModel)]="diameter" class="w-full"/>
        <p-slider [(ngModel)]="diameter" [step]="0.01" [min]="1" [max]="100"></p-slider>
      </div>
    </div>
  `
})
export class FlattenBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  diameter: number = 10;
  private editorCursorMesh: Mesh | null = null;

  constructor(private renderService: BabylonRenderServiceAccessImpl) {
    super();
  }

  ngOnInit(): void {
    this.initEditorCursor();
  }

  ngOnDestroy(): void {
    if (this.editorCursorMesh) {
      this.editorCursorMesh.dispose();
      this.editorCursorMesh = null;
    }
  }

  override isInRadius(mousePosition: Vector3, oldPosition: Vector3): boolean {
    return Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z)) < (this.diameter / 2.0);
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    const radius = this.diameter / 2.0;
    const distance = Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z));
    if (distance < radius) {
      const force = (radius - distance) / radius;
      return (this.brushContext!.getAvgHeight() - oldPosition.y) * force + oldPosition.y;
    } else {
      return null;
    }
  }

  override isContextDependent(): boolean {
    return true;
  }

  override showCursor() {
    if (this.editorCursorMesh) {
      this.editorCursorMesh.visibility = 1;
    }
  }

  override hideCursor() {
    if (this.editorCursorMesh) {
      this.editorCursorMesh.visibility = 0;
    }
  }

  private initEditorCursor() {
    const scene = this.renderService.getScene();
    this.editorCursorMesh = MeshBuilder.CreateSphere("editorCursor inner", {
      diameter: 1
    }, scene);
    this.editorCursorMesh.isPickable = false;
    this.editorCursorMesh.setEnabled(false);

    let material = new StandardMaterial("cursorMaterial inner", scene);
    material.alpha = 0.5;
    material.diffuseColor = new Color3(1, 1, 0);
    this.editorCursorMesh.material = material;

    scene.onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERMOVE: {
          let pickingInfo = this.renderService.setupTerrainPickPoint();
          if (pickingInfo.hit) {
            if (this.editorCursorMesh) {
              this.editorCursorMesh.position.copyFrom(pickingInfo.pickedPoint!);
              this.editorCursorMesh.setEnabled(true);
              this.editorCursorMesh.scaling.set(this.diameter, this.diameter, this.diameter);
            }
          }
          break;
        }
      }
    })
  }
}
