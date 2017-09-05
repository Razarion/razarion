import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'duration'})
export class DurationPipe implements PipeTransform {
  transform(duration: number): string {
    let durationSeconds = duration / 1000;
    return DurationPipe.makeDigits(durationSeconds / 3600) + ":" + DurationPipe.makeTwoDigits((durationSeconds % 3600) / 60) + ":" + DurationPipe.makeTwoDigits(durationSeconds % 60);
  }

  private static makeTwoDigits(value: number): string {
    if (value < 10) {
      return "0" + Math.trunc(value)
    } else {
      return "" + Math.trunc(value)
    }
  }

  private static makeDigits(value: number): string {
    return "" + Math.trunc(value)
  }
}
