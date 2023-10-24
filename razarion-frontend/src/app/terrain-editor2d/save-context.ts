import {SlopeTerrainEditorUpdate, TerrainSlopePosition} from "../generated/razarion-share";
import {Slope} from "./slope";

export class SaveContext {
  private updatedSlopes: Slope[] = [];
  private createdSlopes: Slope[] = [];
  private deletedSlopes: number[] = [];

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

    let deletedSlopes = this.deletedSlopes;
    return new class implements SlopeTerrainEditorUpdate {
      createdSlopes = createdSlopes;
      deletedSlopeIds = deletedSlopes;
      updatedSlopes = updatedSlopes;
    };
  }

  clear() {
    this.createdSlopes = [];
    this.updatedSlopes = [];
    this.deletedSlopes = [];
  }

  onCreated(slope: Slope) {
    this.createdSlopes.push(slope);
  }

  onDeleted(slope: Slope) {
    if (slope.terrainSlopePosition.id) {
      this.deletedSlopes.push(slope.terrainSlopePosition.id);
      this.remove(slope, this.updatedSlopes);
    } else {
      this.remove(slope, this.createdSlopes);
    }
  }

  private remove(removeSlope: Slope, slopes: Slope[]) {
    const index = slopes.indexOf(removeSlope);
    if (index >= 0) {
      slopes.splice(index, 1);
    }
  }

  slopeConfigConfigIdChanged(slope: Slope) {
    this.updatedSlopes.includes(slope) || this.createdSlopes.includes(slope) || this.updatedSlopes.push(slope);
  }
}
