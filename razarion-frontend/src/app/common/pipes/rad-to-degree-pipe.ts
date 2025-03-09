import {Pipe, PipeTransform} from '@angular/core';


@Pipe({
    name: 'radToDegree',
    standalone: false
})
export class RadToDegreePipe implements PipeTransform {
  transform(rad: number): number {
    return rad * 180 / Math.PI;
  }
}
