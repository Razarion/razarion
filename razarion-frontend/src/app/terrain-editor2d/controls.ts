import {Slope} from "./slope";

export class Controls {
  xPos?: number;
  yPos?: number;
  newSlopeConfigId?: number;
  selectedSLope?: Slope;

  getCorners(): number | undefined {
    if (this.selectedSLope?.getPolygon()?.geometry) {
      return this.selectedSLope?.getPolygon()?.geometry!.coordinates[0].length
    }
    return undefined;
  }

}
