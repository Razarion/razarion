import {SlopeTerrainEditorUpdate, TerrainSlopePosition} from "../generated/razarion-share";
import {Slope} from "./slope";

export class SaveContext {
  private updatedSlopes: Slope[] = [];
  private createdSlopes: Slope[] = [];

  onManipulated(slope: Slope) {
    if (this.createdSlopes.includes(slope)) {
      return;
    }

    this.updatedSlopes.includes(slope) || this.updatedSlopes.push(slope);
  }

  generateSlopeTerrainEditorUpdate(): SlopeTerrainEditorUpdate {
    let createdSlopes: TerrainSlopePosition[] = [];
    this.createdSlopes.forEach(slope => {
      createdSlopes.push(slope.generateTerrainSlopePosition());
    });

    let updatedSlopes: TerrainSlopePosition[] = [];
    this.updatedSlopes.forEach(slope => {
      updatedSlopes.push(slope.generateTerrainSlopePosition());
    });

    return new class implements SlopeTerrainEditorUpdate {
      createdSlopes = createdSlopes;
      deletedSlopeIds: number[] = [];
      updatedSlopes = updatedSlopes;
    };
  }

  clear() {
    this.createdSlopes = [];
    this.updatedSlopes = [];
  }

  onCreated(slope: Slope) {
    this.createdSlopes.push(slope);
  }
}
