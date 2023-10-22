import {Slope} from "./slope";

export class HoverContext {
  private intersectSlope?: Slope;
  private insideOf?: Slope;

  setIntersectSlope(slope: Slope) {
    this.intersectSlope = slope;
  }

  getIntersectSlope(): Slope | undefined {
    return this.intersectSlope;
  }

  setInsideOf(slope: Slope) {
    this.insideOf = slope;
  }

  getInsideOf(): Slope | undefined {
    return this.insideOf;
  }
}
