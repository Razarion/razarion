import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
    selector: 'acceleration',
    templateUrl: './acceleration.component.html'
})
export class AccelerationComponent implements OnInit {
  private readonly FINAL_SPEED_M_PER_S = 100 * 1000 / 3600; // Convert 100 km/h to m/s
  @Input("acceleration")
  acceleration: number | null = null;
  @Output()
  accelerationChange = new EventEmitter<number | null>();
  seconds0To100?: number;

  ngOnInit(): void {
    if (this.acceleration || this.acceleration === 0) {
      this.seconds0To100 = this.FINAL_SPEED_M_PER_S / this.acceleration;
    } else {
      this.seconds0To100 = undefined;
    }
  }

  onChange(seconds0To100: any) {
    this.acceleration = seconds0To100 ? (this.FINAL_SPEED_M_PER_S / seconds0To100) : null;
    this.accelerationChange.emit(this.acceleration);
  }
}
