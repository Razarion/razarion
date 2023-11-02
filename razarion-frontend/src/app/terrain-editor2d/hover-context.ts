import {Slope} from "./slope";
import {Driveway} from "./driveway";

export class HoverContext {
  private intersectSlope?: Slope;
  private intersectDriveway?: Driveway;
  private insideOf?: Slope;

  setIntersectSlope(slope: Slope) {
    this.intersectSlope = slope;
  }

  getIntersectSlope(): Slope | undefined {
    return this.intersectSlope;
  }

  getIntersectDriveway(): Driveway | undefined {
    return this.intersectDriveway;
  }
  setIntersectDriveway(driveway: Driveway | undefined) {
    this.intersectDriveway = driveway;
  }

  setInsideOf(slope: Slope) {
    this.insideOf = slope;
  }

  getInsideOf(): Slope | undefined {
    return this.insideOf;
  }
}
