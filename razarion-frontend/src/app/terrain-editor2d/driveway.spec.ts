import {Driveway} from "./driveway";
import * as turf from "@turf/turf";


it('#Driveway.correctIndexes',
  (done: DoneFn) => {
    const polygon = turf.polygon([[[0, 0], [1, 0], [1, 1], [0, 1], [0, 0]]]);
    const driveway1 = [3, 0, 1];
    const driveway2 = [0, 1];

    // let indexes = Driveway.mergePathsCounterclockwise(polygon.geometry, driveway1, driveway2);
    // console.log("Hallo4");
    // console.log(indexes);
    // expect(Driveway.correctIndexes(polygon.geometry, indexArray)).toBe([]);
    done();
  });
