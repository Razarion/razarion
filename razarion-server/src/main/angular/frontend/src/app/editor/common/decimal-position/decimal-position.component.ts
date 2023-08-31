import {Component, EventEmitter, Input, Output} from '@angular/core';
import {DecimalPosition} from "../../../generated/razarion-share";

@Component({
  selector: 'decimal-position',
  templateUrl: './decimal-position.component.html'
})
export class DecimalPositionComponent {
  @Input("decimalPosition")
  decimalPosition: DecimalPosition | null = null;
  @Output()
  decimalPositionChange = new EventEmitter<DecimalPosition | null>();
  @Input("readOnly")
  readOnly: boolean = false;

  x?: number
  y?: number

  constructor() {
  }

  ngOnInit(): void {
    if(this.decimalPosition) {
       this.x = this.decimalPosition.x;
       this.y = this.decimalPosition.y;
    }
  }

  onX(value?: number) {
    this.fireDecimalPosition();
  }

  onY(value?: number) {
    this.fireDecimalPosition();
  }

  private fireDecimalPosition() {
    if ((!this.x && this.x !== 0) && (!this.y && this.y !== 0)) {
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
