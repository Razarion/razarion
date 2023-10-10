import {Slope} from "./model";
import {SlopeTerrainEditorUpdate, TerrainSlopePosition} from "../generated/razarion-share";

export class SaveContext {
  private updatedSlopes: Slope[] = [];

  onManipulated(slope: Slope) {
    this.updatedSlopes.includes(slope) || this.updatedSlopes.push(slope);
  }

  generateSlopeTerrainEditorUpdate(): SlopeTerrainEditorUpdate {
    let createdSlopes: TerrainSlopePosition[] = [];
    for (let i = 0; i < this.updatedSlopes.length - 1; i++){
      const slope = this.updatedSlopes[i];
      createdSlopes.push(slope.generateTerrainSlopePosition());
    }

    return new class implements SlopeTerrainEditorUpdate {
      createdSlopes: TerrainSlopePosition[] = [];
      deletedSlopeIds: number[] = [];
      updatedSlopes = createdSlopes;
    };
  }

  clear() {
    this.updatedSlopes = [];
  }
}
