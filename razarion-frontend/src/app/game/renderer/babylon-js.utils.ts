import {Color3, StandardMaterial, Vector2, VertexData} from "@babylonjs/core";

export class BabylonJsUtils {

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
}
