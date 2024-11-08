import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'angle',
  templateUrl: './angle.component.html'
})
export class AngleComponent implements OnInit {
  @Input("angle")
  angle: number | null = null;
  @Input("maxDegreeAngle")
  maxDegreeAngle = 360;
  @Output()
  angleChange = new EventEmitter<number | null>();
  degreeAngle?: number;

  ngOnInit(): void {
    this.degreeAngle = (this.angle || this.angle === 0) ? this.angle * 180 / Math.PI : undefined;
  }

  onChange(degreeAngle: any) {
    this.angle = (degreeAngle || degreeAngle === 0) ? degreeAngle! * Math.PI / 180 : null;
    this.angleChange.emit(this.angle);
  }

  onTextChange(event: any) {
    if ((event.srcElement.value == null
      || event.srcElement.value == undefined
      || (typeof event.srcElement.value === "string" && event.srcElement.value.trim().length === 0))) {
      this.degreeAngle = undefined;
      this.angle = null;
      this.angleChange.emit(this.angle);
      return;
    }
    let degreeAngle = Number.parseFloat(event.srcElement.value);
    if (degreeAngle > this.maxDegreeAngle) {
      degreeAngle = this.maxDegreeAngle;
    }
    this.onChange(degreeAngle);
  }
}
