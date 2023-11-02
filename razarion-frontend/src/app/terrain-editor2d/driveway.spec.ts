import {Driveway} from "./driveway";
import * as turf from "@turf/turf";


it('#Driveway.reduceToOnePiece',
  (done: DoneFn) => {
    const indexes = [1, 2, 3];

    let resultIndexes = Driveway.reduceToOnePiece(10, indexes);
    console.log("Hallo7");
    console.log(resultIndexes);
    // expect(Driveway.correctIndexes(polygon.geometry, indexArray)).toBe([]);
    done();
  });
