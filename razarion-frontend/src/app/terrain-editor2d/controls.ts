import {Slope} from "./slope";
import {Driveway} from "./driveway";

export class Controls {
  xPos?: number;
  yPos?: number;
  newSlopeConfigId?: number;
  newDrivewayConfigId?: number;
  selectedSlope?: Slope;
  selectedDriveway?: Driveway;
  cursorDiameter = 20;
  cursorCorners = 10;
  cursorAngleDegree = 0;


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
