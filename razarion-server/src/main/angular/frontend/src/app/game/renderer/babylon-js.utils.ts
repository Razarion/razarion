import {Color3, Mesh, StandardMaterial, VertexData} from "@babylonjs/core";

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

  static addErrorMaterial(mesh: Mesh) {
    const material = new StandardMaterial("Error Material");
    material.diffuseColor = new Color3(1, 0, 0);
    material.emissiveColor = new Color3(1, 0, 0);
    material.specularColor = new Color3(1, 0, 0);
    material.backFaceCulling = false; // Camera looking in negative z direction. https://doc.babylonjs.com/features/featuresDeepDive/mesh/creation/custom/custom#visibility
    mesh.material = material;
  }

}
