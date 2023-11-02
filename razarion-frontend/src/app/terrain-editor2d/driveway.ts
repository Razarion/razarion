import * as turf from "@turf/turf";
import {Feature, Polygon} from "@turf/turf";
import {Slope} from "./slope";
import {Controls} from "./controls";

export class Driveway {
  private indexes: number[] = [];

  constructor(public drivewayConfigId: number, private slope: Slope) {
  }

  addIndex(index: number) {
    this.indexes.push(index);
  }

  draw(ctx: CanvasRenderingContext2D, controls: Controls) {
    if (this.indexes.length < 2) {
      return;
    }
    ctx.save();
    if(controls.selectedDriveway === this) {
      ctx.strokeStyle = "yellow";
    } else {
      ctx.strokeStyle = "red";
    }
    ctx.lineWidth = 5;
    ctx.beginPath();
    ctx.moveTo(this.slope.getPolygon()!.geometry.coordinates[0][this.indexes[0]][0], this.slope.getPolygon()!.geometry.coordinates[0][this.indexes[0]][1]);
    for (let i = 1; i < this.indexes.length; i++) {
      ctx.lineTo(this.slope.getPolygon()!.geometry.coordinates[0][this.indexes[i]][0], this.slope.getPolygon()!.geometry.coordinates[0][this.indexes[i]][1])
    }
    ctx.stroke();
    ctx.restore();

  }

  drivewayIntersection(polygon: Feature<Polygon, any>): boolean {
    return !!this.indexes.find(index => {
      let point = turf.point(
        [this.slope.getPolygon()!.geometry.coordinates[0][index][0],
          this.slope.getPolygon()!.geometry.coordinates[0][index][1]]);
      return turf.booleanPointInPolygon(point, polygon);
    });
  }

  append(polygon: Feature<Polygon, any>) {
    let indexesToAdd: number[] = [];

    for (let index = 0; index < this.slope.getPolygon()!.geometry.coordinates[0].length - 1; index++) {
      let point = turf.point(
        [this.slope.getPolygon()!.geometry.coordinates[0][index][0],
          this.slope.getPolygon()!.geometry.coordinates[0][index][1]]);
      if (turf.booleanPointInPolygon(point, polygon)) {
        indexesToAdd.push(index);
      }
    }

    this.indexes = Driveway.mergePaths(
      this.slope.getPolygon()!.geometry.coordinates[0].length - 1,
      this.indexes,
      indexesToAdd);
  }

  remove(polygon: Feature<Polygon, any>) {
    let indexesToRemove: number[] = [];

    for (let index = 0; index < this.slope.getPolygon()!.geometry.coordinates[0].length - 1; index++) {
      let point = turf.point(
        [this.slope.getPolygon()!.geometry.coordinates[0][index][0],
          this.slope.getPolygon()!.geometry.coordinates[0][index][1]]);
      if (turf.booleanPointInPolygon(point, polygon)) {
        let indexIndex = this.indexes.indexOf(index);
        if (indexIndex >= 0) {
          this.indexes.splice(indexIndex, 1);
        }
      }
    }

    this.indexes = Driveway.reduceToOnePiece(this.slope.getPolygon()!.geometry.coordinates[0].length - 1, this.indexes);
  }

  static reduceToOnePiece(polygonLength: number, indexes: number[]) {
    let marker: boolean[] = [];
    for (let index = 0; index < polygonLength; index++) {
      marker[index] = indexes.includes(index);
    }

    let emptyStartIndex = -1;
    for (let index = 0; index < polygonLength; index++) {
      if (!marker[index]) {
        emptyStartIndex = index;
        break;
      }
    }

    if (emptyStartIndex < 0) {
      throw new Error("Driveway.reduceToOnePiece() Start not found");
    }

    console.info(`emptyStartIndex: ${emptyStartIndex}`)

    let drivewayStartIndex = -1;
    for (let index = emptyStartIndex; (index - emptyStartIndex) < polygonLength; index++) {
      if (marker[index % polygonLength]) {
        drivewayStartIndex = index;
        break
      }
    }

    if (drivewayStartIndex < 0) {
      throw new Error("Driveway.reduceToOnePiece() driveway start not found");
    }

    console.info(`drivewayStartIndex: ${drivewayStartIndex}`)

    let newIndexes: number[] = [];
    for (let index = drivewayStartIndex; (index - drivewayStartIndex) < polygonLength; index++) {
      if (marker[index % polygonLength]) {
        newIndexes.push(index % polygonLength);
      } else {
        break;
      }
    }
    return newIndexes;
  }

  static mergePaths(polygonLength: number, path1: number[], path2: number[]): number[] {
    let marker: boolean[] = [];
    for (let index = 0; index < polygonLength; index++) {
      marker[index] = path1.indexOf(index) !== -1 || path2.indexOf(index) !== -1;
    }

    let emptyStartIndex = -1;
    for (let index = 0; index < polygonLength; index++) {
      if (!marker[index]) {
        emptyStartIndex = index;
        break;
      }
    }

    if (emptyStartIndex < 0) {
      throw new Error("Driveway.mergePathsCounterclockwise() Start not found");
    }

    let merged: number[] = [];
    for (let index = emptyStartIndex; (index - emptyStartIndex) < polygonLength; index++) {
      if (marker[index % polygonLength]) {
        merged.push(index % polygonLength);
      }
    }
    return merged;
  }

  containsIndex(index: number): boolean {
    return this.indexes.includes(index);
  }

  isEmpty() {
    return this.indexes.length === 0;
  }

  getCorners(): number {
    return this.indexes.length;
  }
}
