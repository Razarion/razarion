import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'velocity',
  templateUrl: './velocity.component.html'
})
export class VelocityComponent implements OnInit {
  @Input("velocity")
  velocity: number | null = null;
  @Output()
  velocityChange = new EventEmitter<number | null>();
  kmh?: number;

  ngOnInit(): void {
    if (this.velocity || this.velocity === 0) {
      this.kmh = this.velocity * 3.6;
    } else {
      this.kmh = undefined;
    }
  }

  onChange(kmh: any) {
    this.velocity = (kmh || kmh === 0) ? kmh! / 3.6 : null;
    this.velocityChange.emit(this.velocity);
  }
}
