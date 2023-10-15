import {Slope} from "./slope";

export class SelectionContext {
  private intersect?: Slope;
  private insideOf?: Slope;

  setIntersect(slope: Slope) {
    this.intersect = slope;
  }

  getIntersect(): Slope | undefined {
    return this.intersect;
  }

  setInsideOf(slope: Slope) {
    this.insideOf = slope;
  }

  getInsideOf(): Slope | undefined {
    return this.insideOf;
  }
}
