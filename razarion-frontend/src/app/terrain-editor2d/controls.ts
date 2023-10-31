import {Slope} from "./slope";

export class Controls {
  xPos?: number;
  yPos?: number;
  newSlopeConfigId?: number;
  newDrivewayConfigId?: number;
  selectedSlope?: Slope;

  getCorners(): number | undefined {
    if (this.selectedSlope?.getPolygon()?.geometry?.coordinates[0].length) {
      return this.selectedSlope!.getPolygon()!.geometry!.coordinates[0].length - 1;
    }
    return undefined;
  }

  getDriveways() {
    if (this.selectedSlope?.getPolygon()?.geometry) {
      return this.selectedSlope?.getDrivewayCount();
    }
    return undefined;
  }
}
