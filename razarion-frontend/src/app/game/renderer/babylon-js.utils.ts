import {Color3, StandardMaterial, Vector2, VertexData} from "@babylonjs/core";
import {DecimalPosition, TerrainSlopeCorner, TerrainSlopePosition} from "../../gwtangular/GwtAngularFacade";

export class BabylonJsUtils {

  static createVertexData(positions: Float32Array, norms?: Float32Array): VertexData {
    for (let i = 0; i < positions.length / 3; i++) {
      const newPositionY = positions[i * 3 + 2];
      const newPositionZ = positions[i * 3 + 1];
      positions[i * 3 + 1] = newPositionY;
      positions[i * 3 + 2] = newPositionZ;
      if (norms) {
        const newNormalY = norms[i * 3 + 2];
        const newNormalZ = norms[i * 3 + 1];
        norms[i * 3 + 1] = newNormalY;
        norms[i * 3 + 2] = newNormalZ;
      }
    }

    const vertexData = new VertexData();
    vertexData.positions = positions;
    if (norms) {
      vertexData.normals = norms;
    }
    vertexData.indices = this.generateIndices(positions.length);

    return vertexData;
  }

  static generateIndices(positionCount: number): number[] {
    const indices = [];
    for (let i = 0; i < positionCount / 3; i++) {
      indices[i] = i;
    }
    return indices;
  }

  static createErrorMaterial(errorText: string) {
    console.warn(errorText);
    const material = new StandardMaterial(`Error Material '${errorText}'`);
    material.diffuseColor = new Color3(1, 0, 0);
    material.emissiveColor = new Color3(0.8, 0, 0);
    material.specularColor = new Color3(1, 0, 0);
    material.backFaceCulling = false; // Camera looking in negative z direction. https://doc.babylonjs.com/features/featuresDeepDive/mesh/creation/custom/custom#visibility
    return material;
  }

  static toVertex2ArrayFromTerrainSlopeCorner(terrainSlopeCorners: TerrainSlopeCorner[]): Vector2[] {
    const vector2s: any[] = [];
    terrainSlopeCorners.forEach(terrainSlopeCorner =>
      vector2s.push(new Vector2(terrainSlopeCorner.position.x, terrainSlopeCorner.position.y)));
    return vector2s;
  }

  static updateTerrainSlopeCornerFromVertex2Array(vector2s: Vector2[], terrainSlopePosition: TerrainSlopePosition): void {
    for (let i = 0; i < vector2s.length; i++) {
      const vector2 = vector2s[i];
      terrainSlopePosition.polygon[i].position = {x: vector2.x, y: vector2.y};
    }
  }

}
