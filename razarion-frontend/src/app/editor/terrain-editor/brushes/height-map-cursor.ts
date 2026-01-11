import {Constants, Mesh, MeshBuilder, Scene, StandardMaterial, Vector3, VertexData} from '@babylonjs/core';
import {Color3} from '@babylonjs/core/Maths/math.color';
import {BabylonTerrainTileImpl} from '../../../game/renderer/babylon-terrain-tile.impl';
import {BrushValues, FixHeightBrushComponent} from './fix-height-brush.component';

export class HeightMapCursor {
  private mesh: Mesh | null = null;
  private currentBrush!: BrushValues;

  constructor(private scene: Scene, brushValues: BrushValues) {
    this.setupMesh(brushValues);
  }

  private setupMesh(brushValues: BrushValues) {
    if (this.mesh) {
      this.mesh.dispose();
    }
    this.mesh = MeshBuilder.CreateSphere("Height map cursor", {
      diameter: 1
    }, this.scene);
    this.mesh.isPickable = false;
    this.currentBrush = {
      type: brushValues.type,
      size: brushValues.size,
      height: brushValues.height,
      maxSlopeWidth: brushValues.maxSlopeWidth,
      random: brushValues.random,
    }
    let vertexData = this.createVertexData();
    vertexData.applyToMesh(this.mesh, true);

    const wireframeOverMaterial = new StandardMaterial("wireframeOver");
    wireframeOverMaterial.reservedDataStore = { hidden: true };
    wireframeOverMaterial.disableLighting = true;
    wireframeOverMaterial.backFaceCulling = false;
    wireframeOverMaterial.emissiveColor = Color3.White();
    wireframeOverMaterial.wireframe = true;
    wireframeOverMaterial.disableDepthWrite = true;
    wireframeOverMaterial.depthFunction = Constants.ALWAYS;

    this.mesh.material = wireframeOverMaterial;
    // Render cursor after terrain tiles to ensure visibility when new tiles are loaded
    this.mesh.renderingGroupId = 1;
  }

  update(position: Vector3, brushValues: BrushValues) {
    this.mesh!.position.x = position.x;
    this.mesh!.position.y = position.y;
    this.mesh!.position.z = position.z;
    if (this.currentBrush.type !== brushValues.type ||
      this.currentBrush.size !== brushValues.size ||
      this.currentBrush.height !== brushValues.height ||
      this.currentBrush.maxSlopeWidth !== brushValues.maxSlopeWidth ||
      this.currentBrush.random !== brushValues.random) {
      this.setupMesh(brushValues);
    }
  }

  setVisibility(visible: boolean) {
    this.mesh!.isVisible = visible;
  }

  dispose() {
    if (this.mesh) {
      this.mesh.dispose();
      this.mesh = null;
    }
  }

  private createVertexData(): VertexData {
    const indices = [];
    const positions = [];
    const normals = [];
    const uvs = [];

    const size = this.currentBrush.maxSlopeWidth * 2 + this.currentBrush.size;

    let xCount = (size / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    let yCount = (size / BabylonTerrainTileImpl.NODE_SIZE) + 1;

    const startX = -xCount / 2 * BabylonTerrainTileImpl.NODE_SIZE;
    const startY = -yCount / 2 * BabylonTerrainTileImpl.NODE_SIZE;

    // Vertices
    for (let y = 0; y < yCount; y++) {
      for (let x = 0; x < xCount; x++) {
        const currentX = startX + x * BabylonTerrainTileImpl.NODE_SIZE;
        const currentY = startY + y * BabylonTerrainTileImpl.NODE_SIZE;
        let height = FixHeightBrushComponent.staticCalculateHeight(new Vector3(0, 0, 0), new Vector3(currentX, 0, currentY), null, this.currentBrush);
        if (height === null) {
          height = 0;
        }
        positions.push(
          currentX,
          height,
          currentY);
        normals.push(0, 0, 0);
        uvs.push(x / xCount, 1.0 - y / yCount);
      }
    }

    // Indices
    for (let y = 0; y < yCount - 1; y++) {
      for (let x = 0; x < xCount - 1; x++) {
        const bLIdx = x + y * xCount;
        const bRIdx = x + 1 + y * xCount;
        const tLIdx = x + (y + 1) * xCount;
        const tRIdx = x + 1 + (y + 1) * xCount;

        indices.push(bLIdx);
        indices.push(bRIdx);
        indices.push(tLIdx);

        indices.push(bRIdx);
        indices.push(tRIdx);
        indices.push(tLIdx);
      }
    }

    VertexData.ComputeNormals(positions, indices, normals);

    const vertexData = new VertexData();

    vertexData.indices = indices;
    vertexData.positions = positions;
    vertexData.normals = normals;

    return vertexData;

  }
}
