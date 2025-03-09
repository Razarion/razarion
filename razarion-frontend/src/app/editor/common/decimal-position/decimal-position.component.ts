import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { DecimalPosition } from "../../../generated/razarion-share";
import { BabylonRenderServiceAccessImpl } from 'src/app/game/renderer/babylon-render-service-access-impl.service';
import { Nullable, Observer, PointerEventTypes, PointerInfo } from '@babylonjs/core';

@Component({
    selector: 'decimal-position',
    templateUrl: './decimal-position.component.html',
    standalone: false
})
export class DecimalPositionComponent implements OnInit {
  @Input("decimalPosition")
  decimalPosition: DecimalPosition | null = null;
  @Output()
  decimalPositionChange = new EventEmitter<DecimalPosition | null>();
  @Input("readOnly")
  readOnly: boolean = false;

  x?: number
  y?: number

  checkedEditMode = false;

  private mouseObservable: Nullable<Observer<PointerInfo>> = null;

  constructor(private renderService: BabylonRenderServiceAccessImpl) {
  }

  ngOnInit(): void {
    if (this.decimalPosition) {
      this.x = this.decimalPosition.x;
      this.y = this.decimalPosition.y;
    }
  }

  onX(value?: any) {
    this.x = value;
    this.fireDecimalPosition();
  }

  onY(value?: any) {
    this.y = value;
    this.fireDecimalPosition();
  }

  onPick() {
    if (this.checkedEditMode) {
      this.mouseObservable = this.renderService.getScene().onPointerObservable.add((pointerInfo) => {
        if (pointerInfo.type === PointerEventTypes.POINTERDOWN) {
          let pickingInfo = this.renderService.setupMeshPickPoint();
          if (pickingInfo.hit) {
            this.x = pickingInfo.pickedPoint!.x;
            this.y = pickingInfo.pickedPoint!.z;
            this.renderService.getScene().onPointerObservable.remove(this.mouseObservable);
            this.mouseObservable = null;
            this.checkedEditMode = false;
            this.fireDecimalPosition();
          }
        }
      });
    } else {
      if (this.mouseObservable) {
        this.renderService.getScene().onPointerObservable.remove(this.mouseObservable);
        this.mouseObservable = null;
      }
    }

  }

  private fireDecimalPosition() {
    if ((this.x || this.x === 0) && (this.y || this.y === 0)) {
      this.decimalPosition = {
        x: this.x!,
        y: this.y!,
      };
    } else {
      this.decimalPosition = null;
    }
    this.decimalPositionChange.emit(this.decimalPosition);
  }
}
