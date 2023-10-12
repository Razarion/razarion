import {Slope} from "./slope";

export class SelectionContext {
  private slopes: Slope[] = [];
  private insideOf?: Slope;

  add(slope: Slope) {
    this.slopes.push(slope);
  }

  valid(): boolean {
    return this.slopes.length > 0
  }

  getSelectedSlope(): Slope {
    return this.slopes[0];
  }

  setInsideOf(slope: Slope) {
    this.insideOf = slope;
  }

  getInsideOf(): Slope | undefined {
    return this.insideOf;
  }
}
