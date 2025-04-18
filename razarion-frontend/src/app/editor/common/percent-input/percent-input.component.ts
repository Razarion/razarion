import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MathUtils } from 'src/app/common/math-utils';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'percent-input',
  imports: [
    InputNumber,
    FormsModule
  ],
  templateUrl: './percent-input.component.html'
})
export class PercentInputComponent {
  @Input()
  modelValue: number = 0;
  @Output()
  modelValueChange = new EventEmitter<number>();

  get displayValue(): number {
    return this.modelValue * 100;
  }

  onInput(value: any): void {
    this.modelValue = value / 100;
    this.modelValue = MathUtils.clamp(this.modelValue, 0, 1);
    this.modelValueChange.emit(this.modelValue);
  }
}
